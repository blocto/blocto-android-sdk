package com.portto.valuedapp.evm

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.portto.sdk.core.BloctoSDK
import com.portto.sdk.core.decodeHex
import com.portto.sdk.evm.evm
import com.portto.sdk.wallet.BloctoEnv
import com.portto.sdk.wallet.BloctoSDKError
import com.portto.sdk.wallet.evm.EvmSignType
import com.portto.valuedapp.R
import com.portto.valuedapp.databinding.FragmentEvmSignMessageBinding
import com.portto.valuedapp.hideKeyboard
import com.portto.valuedapp.hideLoading
import com.portto.valuedapp.showLoading
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.komputing.khash.keccak.Keccak
import org.komputing.khash.keccak.KeccakParameter
import org.web3j.abi.FunctionEncoder
import org.web3j.abi.TypeReference
import org.web3j.abi.datatypes.DynamicBytes
import org.web3j.abi.datatypes.Function
import org.web3j.abi.datatypes.generated.Bytes32
import org.web3j.abi.datatypes.generated.Bytes4
import org.web3j.crypto.StructuredDataEncoder
import org.web3j.protocol.Web3j
import org.web3j.protocol.core.DefaultBlockParameterName
import org.web3j.protocol.core.methods.request.Transaction
import org.web3j.protocol.http.HttpService

class EvmSignMessageFragment : Fragment(R.layout.fragment_evm_sign_message) {

    companion object {
        private const val ERC1271_MAGIC_VALUE =
            "0x1626ba7e00000000000000000000000000000000000000000000000000000000"
    }

    private lateinit var binding: FragmentEvmSignMessageBinding
    private val viewModel: EvmViewModel by activityViewModels()

    private var signType = EvmSignType.ETH_SIGN

    private val rpcUrl
        get() = when (BloctoSDK.env) {
            BloctoEnv.PROD -> viewModel.currentChain.mainnetRpcUrl
            BloctoEnv.DEV -> viewModel.currentChain.testnetRpcUrl
        }

    private val chainId
        get() = when (BloctoSDK.env) {
            BloctoEnv.PROD -> viewModel.currentChain.mainnetChainId
            BloctoEnv.DEV -> viewModel.currentChain.testnetChainId
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentEvmSignMessageBinding.bind(view)

        binding.chipGroup.setOnCheckedStateChangeListener { _, _ ->
            setupMessage()
            resetView()
        }

        binding.signButton.setOnClickListener {
            it.hideKeyboard()
            signMessage(signType = signType)
        }

        viewModel.resetView.observe(viewLifecycleOwner) {
            setupMessage()
            resetView()
        }
    }

    override fun onPause() {
        super.onPause()
        view?.clearFocus()
    }

    private fun setupMessage() {
        val checkedId = binding.chipGroup.checkedChipIds.firstOrNull() ?: return
        when (checkedId) {
            binding.ethSign.id -> {
                signType = EvmSignType.ETH_SIGN
                binding.input.setText("0x416e79206d65737361676520796f752077616e6e61207369676e")
            }

            binding.personalSign.id -> {
                signType = EvmSignType.PERSONAL_SIGN
                binding.input.setText("Any message you wanna sign")
            }

            binding.typedDataV3.id -> {
                signType = EvmSignType.TYPED_DATA_SIGN_V3
                binding.input.setText(getString(R.string.default_typed_data_v3, chainId))
            }

            binding.typedDataV4.id -> {
                signType = EvmSignType.TYPED_DATA_SIGN_V4
                binding.input.setText(getString(R.string.default_typed_data_v4, chainId))
            }

            binding.typedData.id -> {
                signType = EvmSignType.TYPED_DATA_SIGN
                binding.input.setText(getString(R.string.default_typed_data_v4, chainId))
            }
        }
    }

    private fun signMessage(signType: EvmSignType) {
        resetView()

        val address = viewModel.currentAddress ?: kotlin.run {
            viewModel.showError("wallet not connected")
            return
        }

        val message = binding.input.text.toString().trim().takeIf {
            it.isNotBlank()
        } ?: kotlin.run {
            viewModel.showError("empty message")
            return
        }

        binding.signButton.showLoading()

        val onSuccess: (String) -> Unit = { signature ->
            binding.signButton.hideLoading(getString(R.string.button_sign))
            with(binding.signature) {
                text = getString(R.string.signature, signature)
                isVisible = true
            }

            val exceptionHandler = CoroutineExceptionHandler { _, error ->
                viewModel.showError(error.message)
            }

            lifecycleScope.launch(exceptionHandler) {
                val isAuthorizedSigner = withContext(Dispatchers.IO) {
                    val messageByteArray = when (signType) {
                        EvmSignType.ETH_SIGN -> message.removePrefix("0x").decodeHex()
                        EvmSignType.PERSONAL_SIGN -> {
                            val data = message.toByteArray()
                            ByteArray(0)
                                .plus(0x19)
                                .plus(0x45)
                                .plus("thereum Signed Message:\n${data.size}".toByteArray())
                                .plus(data)
                        }

                        EvmSignType.TYPED_DATA_SIGN,
                        EvmSignType.TYPED_DATA_SIGN_V3,
                        EvmSignType.TYPED_DATA_SIGN_V4 ->
                            StructuredDataEncoder(message).structuredData
                    }
                    val hash = Keccak.digest(messageByteArray, KeccakParameter.KECCAK_256)
                    val signatureByteArray = signature.removePrefix("0x").decodeHex()
                    verifySignature(hash, signatureByteArray)
                }
                with(binding.verification) {
                    text = getString(R.string.is_authorized_signer, isAuthorizedSigner.toString())
                    isVisible = true
                }
            }
        }

        val onError: (BloctoSDKError) -> Unit = {
            binding.signButton.hideLoading(getString(R.string.button_sign))
            viewModel.showError(it)
        }

        BloctoSDK.evm.signMessage(
            context = requireContext(),
            blockchain = viewModel.currentChain.blockchain,
            fromAddress = address,
            signType = signType,
            message = message,
            onSuccess = onSuccess,
            onError = onError
        )
    }

    private fun verifySignature(hash: ByteArray, signature: ByteArray): Boolean {
        val web3j = Web3j.build(HttpService(rpcUrl))
        val function = Function(
            "isValidSignature",
            listOf(Bytes32(hash), DynamicBytes(signature)),
            listOf<TypeReference<Bytes4>>()
        )
        val encodedFunction = FunctionEncoder.encode(function)
        return web3j.ethCall(
            Transaction.createEthCallTransaction(
                viewModel.currentAddress,
                viewModel.currentAddress,
                encodedFunction
            ),
            DefaultBlockParameterName.LATEST
        ).send().value.orEmpty() == ERC1271_MAGIC_VALUE
    }

    private fun resetView() {
        binding.signature.isVisible = false
        binding.verification.isVisible = false
    }
}
