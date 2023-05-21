package com.portto.valuedapp.evm

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.portto.sdk.core.BloctoSDK
import com.portto.sdk.evm.evm
import com.portto.sdk.wallet.BloctoEnv
import com.portto.sdk.wallet.BloctoSDKError
import com.portto.valuedapp.R
import com.portto.valuedapp.databinding.FragmentEvmSendTransactionBinding
import com.portto.valuedapp.hideKeyboard
import com.portto.valuedapp.hideLoading
import com.portto.valuedapp.showLoading
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.web3j.abi.FunctionEncoder
import org.web3j.abi.TypeReference
import org.web3j.abi.datatypes.Function
import org.web3j.abi.datatypes.Utf8String
import org.web3j.abi.datatypes.generated.Uint256
import org.web3j.protocol.Web3j
import org.web3j.protocol.core.DefaultBlockParameterName
import org.web3j.protocol.core.methods.request.Transaction
import org.web3j.protocol.http.HttpService
import org.web3j.utils.Convert
import java.math.BigInteger

class EvmSendTransactionFragment : Fragment(R.layout.fragment_evm_send_transaction) {

    private lateinit var binding: FragmentEvmSendTransactionBinding
    private val viewModel: EvmViewModel by activityViewModels()

    private val rpcUrl get() = when (BloctoSDK.env) {
        BloctoEnv.PROD -> viewModel.currentChain.mainnetRpcUrl
        BloctoEnv.DEV -> viewModel.currentChain.testnetRpcUrl
    }

    private val explorerDomain get() = when (BloctoSDK.env) {
        BloctoEnv.PROD -> viewModel.currentChain.mainnetExplorerDomain
        BloctoEnv.DEV -> viewModel.currentChain.testnetExplorerDomain
    }

    private val contractAddress get() = when (BloctoSDK.env) {
        BloctoEnv.PROD -> viewModel.currentChain.mainnetContractAddress
        BloctoEnv.DEV -> viewModel.currentChain.testnetContractAddress
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentEvmSendTransactionBinding.bind(view)

        binding.setValueButton.setOnClickListener {
            it.hideKeyboard()
            setValue()
        }

        binding.setValueTxHash.setOnClickListener {
            val txHash = binding.setValueTxHash.text.toString()
            openExplorer(txHash)
        }

        binding.getValueButton.setOnClickListener {
            it.hideKeyboard()
            getValue()
        }

        binding.donateButton.setOnClickListener {
            it.hideKeyboard()
            donate()
        }

        binding.donateTxHash.setOnClickListener {
            val txHash = binding.donateTxHash.text.toString()
            openExplorer(txHash)
        }

        viewModel.resetView.observe(viewLifecycleOwner) {
            binding.setValueTxHash.isVisible = false
            binding.value.text = ""
            binding.amountInputLayout.suffixText = viewModel.currentChain.symbol
            binding.donateTxHash.isVisible = false
        }
    }

    override fun onPause() {
        super.onPause()
        view?.clearFocus()
    }

    private fun setValue() {
        binding.setValueTxHash.isVisible = false

        val address = viewModel.currentAddress ?: kotlin.run {
            viewModel.showError("wallet not connected")
            return
        }

        val value = binding.valueInput.text.toString().toLongOrNull() ?: kotlin.run {
            viewModel.showError("not a valid integer")
            return
        }

        binding.setValueButton.showLoading()

        val setValueFunction = Function("setValue", listOf(Uint256(value)), emptyList())
        val data = FunctionEncoder.encode(setValueFunction)

        sendTransaction(
            fromAddress = address,
            data = data,
            onSuccess = {
                binding.setValueButton.hideLoading(getString(R.string.button_send_transaction))
                with(binding.setValueTxHash) {
                    text = it
                    isVisible = true
                }
            },
            onError = {
                binding.setValueButton.hideLoading(getString(R.string.button_send_transaction))
                viewModel.showError(it)
            }
        )
    }

    private fun getValue() {
        binding.getValueButton.showLoading()

        val exceptionHandler = CoroutineExceptionHandler { _, error ->
            binding.getValueButton.hideLoading(getString(R.string.button_get_value))
            viewModel.showError(error.message)
        }

        lifecycleScope.launch(exceptionHandler) {
            val value = withContext(Dispatchers.Default) { getValueCall() }
            binding.value.text = "$value"
            binding.getValueButton.hideLoading(getString(R.string.button_get_value))
        }
    }

    private fun getValueCall(): BigInteger? {
        val web3j = Web3j.build(HttpService(rpcUrl))
        val function = Function("value", emptyList(), listOf<TypeReference<Uint256>>())
        val encodedFunction = FunctionEncoder.encode(function)
        return web3j.ethCall(
            Transaction.createEthCallTransaction(
                viewModel.currentAddress,
                contractAddress,
                encodedFunction
            ),
            DefaultBlockParameterName.LATEST
        ).send().value.removePrefix("0x").toBigIntegerOrNull(16)
    }

    private fun donate() {
        binding.donateTxHash.isVisible = false

        val address = viewModel.currentAddress ?: kotlin.run {
            viewModel.showError("wallet not connected")
            return
        }

        val amount = binding.amountInput.text.toString().takeIf { it.isNotBlank() }?.let {
            Convert.toWei(it, Convert.Unit.ETHER).toBigInteger()
        } ?: BigInteger.ZERO

        val message = binding.messageInput.text.toString().trim()

        binding.donateButton.showLoading()

        val donateFunction = Function("donate", listOf(Utf8String(message)), emptyList())
        val data = FunctionEncoder.encode(donateFunction)

        sendTransaction(
            fromAddress = address,
            data = data,
            value = amount,
            onSuccess = {
                binding.donateButton.hideLoading(getString(R.string.button_send_transaction))
                with(binding.donateTxHash) {
                    text = it
                    isVisible = true
                }
            },
            onError = {
                binding.donateButton.hideLoading(getString(R.string.button_send_transaction))
                viewModel.showError(it)
            }
        )
    }

    private fun sendTransaction(
        fromAddress: String,
        data: String,
        value: BigInteger = BigInteger.ZERO,
        onSuccess: (String) -> Unit,
        onError: (BloctoSDKError) -> Unit
    ) {
        BloctoSDK.evm.sendTransaction(
            context = requireContext(),
            blockchain = viewModel.currentChain.blockchain,
            fromAddress = fromAddress,
            toAddress = contractAddress,
            data = data,
            value = value,
            onSuccess = onSuccess,
            onError = onError
        )
    }

    private fun openExplorer(txHash: String) {
        val uri = Uri.Builder()
            .scheme("https")
            .authority(explorerDomain)
            .path("tx/$txHash")
            .build()
        startActivity(Intent(Intent.ACTION_VIEW, uri))
    }
}
