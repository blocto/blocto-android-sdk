package com.portto.valuedapp.evm

import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.widget.ArrayAdapter
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.core.widget.doOnTextChanged
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.portto.sdk.core.BloctoSDK
import com.portto.sdk.evm.evm
import com.portto.sdk.wallet.BloctoEnv
import com.portto.sdk.wallet.BloctoSDKError
import com.portto.valuedapp.Config.APP_ID_MAINNET
import com.portto.valuedapp.Config.APP_ID_TESTNET
import com.portto.valuedapp.R
import com.portto.valuedapp.Utils.shortenAddress
import com.portto.valuedapp.databinding.ActivityEvmValueDappBinding
import com.portto.valuedapp.hideKeyboard

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
            onBackPressedDispatcher.onBackPressed()
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

        binding.rpcUrl.doOnTextChanged { text, _, _, _ ->
            viewModel.setRpcUrl(text?.toString())
        }
        binding.pasteButton.setOnClickListener {
            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            binding.rpcUrl.setText(clipboard.primaryClip?.getItemAt(0)?.text)
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
            binding.rpcUrl.text = null
        }
    }

    private fun setEnv(env: Env) {
        BloctoSDK.init(
            appId = env.appId,
            env = when (env.debug) {
                false -> BloctoEnv.PROD
                true -> BloctoEnv.DEV
            }
        )
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

        BloctoSDK.evm.requestAccount(
            context = this,
            blockchain = viewModel.currentChain.blockchain,
            onSuccess = requestAccountOnSuccess,
            onError = requestAccountOnError
        )
    }
}
