package com.portto.valuedapp.evm

import android.os.Bundle
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayout
import com.portto.sdk.core.BloctoSDK
import com.portto.sdk.evm.avalanche
import com.portto.sdk.evm.bnb
import com.portto.sdk.evm.ethereum
import com.portto.sdk.evm.polygon
import com.portto.sdk.wallet.BloctoSDKError
import com.portto.valuedapp.databinding.ActivityEvmValueDappBinding
import com.portto.valuedapp.hideKeyboard

class EvmValueDappActivity : AppCompatActivity() {

    private enum class EvmChain(val title: String) {
        ETHEREUM("ETH"),
        BNB("BNB"),
        POLYGON("POLYGON"),
        AVALANCHE("AVAX")
    }

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

    private val requestAccountOnSuccess: (String) -> Unit = {
        val address = "${it.substring(0, 6)}...${it.substring(it.length - 6, it.length)}"
        binding.connectButton.text = address
        currentAddress = it
    }

    private val onError: (BloctoSDKError) -> Unit = {
        showError(it)
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
                currentAddress = null
                binding.connectButton.text = getString(com.portto.valuedapp.R.string.button_connect)
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) = Unit

            override fun onTabReselected(tab: TabLayout.Tab?) = Unit
        })

        binding.connectButton.setOnClickListener {
            it.hideKeyboard()
            requestAccount()
        }
    }

    private fun setEnv(env: Env) {
        BloctoSDK.init(appId = env.appId, debug = env.debug)
        currentAddress = null
        binding.connectButton.text = getString(com.portto.valuedapp.R.string.button_connect)
    }

    private fun requestAccount() {
        when (currentChain) {
            EvmChain.ETHEREUM -> BloctoSDK.ethereum.requestAccount(
                context = this,
                onSuccess = requestAccountOnSuccess,
                onError = onError
            )
            EvmChain.BNB -> BloctoSDK.bnb.requestAccount(
                context = this,
                onSuccess = requestAccountOnSuccess,
                onError = onError
            )
            EvmChain.POLYGON -> BloctoSDK.polygon.requestAccount(
                context = this,
                onSuccess = requestAccountOnSuccess,
                onError = onError
            )
            EvmChain.AVALANCHE -> BloctoSDK.avalanche.requestAccount(
                context = this,
                onSuccess = requestAccountOnSuccess,
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
}
