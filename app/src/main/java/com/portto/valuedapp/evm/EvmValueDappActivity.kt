package com.portto.valuedapp.evm

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayout
import com.portto.sdk.core.BloctoSDK
import com.portto.sdk.evm.avalanche
import com.portto.sdk.evm.bnb
import com.portto.sdk.evm.ethereum
import com.portto.sdk.evm.polygon
import com.portto.sdk.wallet.BloctoSDKError
import com.portto.valuedapp.R
import com.portto.valuedapp.databinding.ActivityEvmValueDappBinding
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

class EvmValueDappActivity : AppCompatActivity() {

    private data class Env(
        val name: String,
        val appId: String,
        val debug: Boolean
    )

    private val envs = listOf(
        Env(
            name = "Mainnet",
            appId = "0896e44c-20fd-443b-b664-d305b52fe8e8",
            debug = false
        ),
        Env(
            name = "Testnet",
            appId = "57f397df-263c-4e97-b61f-15b67b9ce285",
            debug = true
        )
    )

    private lateinit var binding: ActivityEvmValueDappBinding
    private var currentChain = EvmChain.ETHEREUM
    private var currentAddress: String? = null

    private val rpcUrl get() = if (BloctoSDK.debug) {
        currentChain.testnetRpcUrl
    } else {
        currentChain.mainnetRpcUrl
    }

    private val explorerDomain get() = if (BloctoSDK.debug) {
        currentChain.testnetExplorerDomain
    } else {
        currentChain.mainnetExplorerDomain
    }

    private val contractAddress get() = if (BloctoSDK.debug) {
        currentChain.testnetContractAddress
    } else {
        currentChain.mainnetContractAddress
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEvmValueDappBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val defaultEnv = envs.first()
        setEnv(defaultEnv)

        binding.toolbar.setNavigationOnClickListener {
            onBackPressed()
        }

        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_list_item_1,
            envs.map { it.name }
        )
        binding.dropdown.setAdapter(adapter)
        binding.dropdown.setText(defaultEnv.name, false)
        binding.dropdown.setOnItemClickListener { _, _, position, _ ->
            setEnv(envs[position])
        }

        EvmChain.values().forEach {
            val tab = binding.tabLayout.newTab().apply { text = it.title }
            binding.tabLayout.addTab(tab)
        }
        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                val position = tab?.position ?: return
                currentChain = EvmChain.values()[position]
                resetView()
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) = Unit

            override fun onTabReselected(tab: TabLayout.Tab?) = Unit
        })

        binding.connectButton.setOnClickListener {
            it.hideKeyboard()
            requestAccount()
        }

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
    }

    private fun setEnv(env: Env) {
        BloctoSDK.init(appId = env.appId, debug = env.debug)
        resetView()
    }

    private fun resetView() {
        currentAddress = null
        binding.connectButton.text = getString(R.string.button_connect)
        binding.valueInput.text = null
        binding.setValueTxHash.isVisible = false
        binding.value.text = ""
        binding.amountInput.text = null
        binding.messageInput.text = null
        binding.donateTxHash.isVisible = false
    }

    private fun requestAccount() {
        val requestAccountOnSuccess: (String) -> Unit = {
            val address = "${it.substring(0, 6)}...${it.substring(it.length - 6, it.length)}"
            binding.connectButton.text = address
            currentAddress = it
        }

        val requestAccountOnError: (BloctoSDKError) -> Unit = {
            showError(it)
        }

        when (currentChain) {
            EvmChain.ETHEREUM -> BloctoSDK.ethereum.requestAccount(
                context = this,
                onSuccess = requestAccountOnSuccess,
                onError = requestAccountOnError
            )
            EvmChain.BNB_CHAIN -> BloctoSDK.bnb.requestAccount(
                context = this,
                onSuccess = requestAccountOnSuccess,
                onError = requestAccountOnError
            )
            EvmChain.POLYGON -> BloctoSDK.polygon.requestAccount(
                context = this,
                onSuccess = requestAccountOnSuccess,
                onError = requestAccountOnError
            )
            EvmChain.AVALANCHE -> BloctoSDK.avalanche.requestAccount(
                context = this,
                onSuccess = requestAccountOnSuccess,
                onError = requestAccountOnError
            )
        }
    }

    private fun setValue() {
        binding.setValueTxHash.isVisible = false

        val address = currentAddress ?: kotlin.run {
            showError("wallet not connected")
            return
        }

        val value = binding.valueInput.text.toString().toLongOrNull() ?: kotlin.run {
            showError("not a valid integer")
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
                showError(it)
            }
        )
    }

    private fun getValue() {
        binding.getValueButton.showLoading()

        val exceptionHandler = CoroutineExceptionHandler { _, error ->
            binding.getValueButton.hideLoading(getString(R.string.button_get_value))
            showError(error.message)
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
                currentAddress,
                contractAddress,
                encodedFunction
            ),
            DefaultBlockParameterName.LATEST
        ).send().value.removePrefix("0x").toBigIntegerOrNull(16)
    }

    private fun donate() {
        binding.donateTxHash.isVisible = false

        val address = currentAddress ?: kotlin.run {
            showError("wallet not connected")
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
                showError(it)
            }
        )
    }

    private fun sendTransaction(
        fromAddress: String,
        data: String, value:
        BigInteger = BigInteger.ZERO,
        onSuccess: (String) -> Unit,
        onError: (BloctoSDKError) -> Unit
    ) {
        when (currentChain) {
            EvmChain.ETHEREUM -> BloctoSDK.ethereum.sendTransaction(
                context = this,
                fromAddress = fromAddress,
                toAddress = contractAddress,
                data = data,
                value = value,
                onSuccess = onSuccess,
                onError = onError
            )
            EvmChain.BNB_CHAIN -> BloctoSDK.bnb.sendTransaction(
                context = this,
                fromAddress = fromAddress,
                toAddress = contractAddress,
                data = data,
                value = value,
                onSuccess = onSuccess,
                onError = onError
            )
            EvmChain.POLYGON -> BloctoSDK.polygon.sendTransaction(
                context = this,
                fromAddress = fromAddress,
                toAddress = contractAddress,
                data = data,
                value = value,
                onSuccess = onSuccess,
                onError = onError
            )
            EvmChain.AVALANCHE -> BloctoSDK.avalanche.sendTransaction(
                context = this,
                fromAddress = fromAddress,
                toAddress = contractAddress,
                data = data,
                value = value,
                onSuccess = onSuccess,
                onError = onError
            )
        }
    }

    private fun showError(error: BloctoSDKError) {
        val message = error.message.split("_").joinToString(" ")
        showError(message)
    }

    private fun showError(message: String?) {
        val msg = message ?: "unexpected error"
        Snackbar.make(binding.container, msg, Snackbar.LENGTH_SHORT).show()
    }

    private fun openExplorer(txHash: String) {
        val uri = Uri.Builder()
            .scheme("https")
            .authority(explorerDomain)
            .path(
                if (currentChain == EvmChain.AVALANCHE) {
                    "blockchain/c/tx/$txHash"
                } else {
                    "tx/$txHash"
                }
            )
            .build()
        startActivity(Intent(Intent.ACTION_VIEW, uri))
    }
}
