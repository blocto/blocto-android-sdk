package com.portto.valuedapp.evm

import android.os.Bundle
import android.widget.ArrayAdapter
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.portto.sdk.core.BloctoSDK
import com.portto.sdk.evm.avalanche
import com.portto.sdk.evm.bnb
import com.portto.sdk.evm.ethereum
import com.portto.sdk.evm.polygon
import com.portto.sdk.wallet.BloctoSDKError
import com.portto.valuedapp.Config.APP_ID_MAINNET
import com.portto.valuedapp.Config.APP_ID_TESTNET
import com.portto.valuedapp.R
import com.portto.valuedapp.databinding.ActivityEvmValueDappBinding
import com.portto.valuedapp.hideKeyboard
import com.portto.valuedapp.shortenAddress

class EvmValueDappActivity : AppCompatActivity() {

    private data class Env(
        val name: String,
        val appId: String,
        val debug: Boolean
    )

    private val envs = listOf(
        Env(
            name = "Mainnet",
            appId = APP_ID_MAINNET,
            debug = false
        ),
        Env(
            name = "Testnet",
            appId = APP_ID_TESTNET,
            debug = true
        )
    )

    private lateinit var binding: ActivityEvmValueDappBinding
    private val viewModel: EvmViewModel by viewModels()

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
            val tab = binding.chainTabLayout.newTab().apply { text = it.title }
            binding.chainTabLayout.addTab(tab)
        }
        binding.chainTabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                val position = tab?.position ?: return
                viewModel.currentChain = EvmChain.values()[position]
                viewModel.resetView()
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) = Unit

            override fun onTabReselected(tab: TabLayout.Tab?) = Unit
        })

        binding.connectButton.setOnClickListener {
            it.hideKeyboard()
            requestAccount()
        }

        binding.viewPager.adapter = EvmPagerAdapter(this)
        binding.viewPager.isUserInputEnabled = false

        TabLayoutMediator(
            binding.methodTabLayout,
            binding.viewPager,
            true
        ) { tab, position ->
            tab.text = when (position) {
                0 -> getString(R.string.title_sign_message)
                else -> getString(R.string.title_send_transaction)
            }
        }.attach()

        viewModel.error.observe(this) {
            val message = it ?: "unexpected error"
            Snackbar.make(binding.container, message, Snackbar.LENGTH_SHORT).show()
        }

        viewModel.resetView.observe(this) {
            binding.connectButton.text = getString(R.string.button_connect)
        }
    }

    private fun setEnv(env: Env) {
        BloctoSDK.init(appId = env.appId, debug = env.debug)
        viewModel.resetView()
    }

    private fun requestAccount() {
        val requestAccountOnSuccess: (String) -> Unit = {
            binding.connectButton.text = it.shortenAddress()
            viewModel.currentAddress = it
        }

        val requestAccountOnError: (BloctoSDKError) -> Unit = {
            viewModel.showError(it)
        }

        when (viewModel.currentChain) {
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
}
