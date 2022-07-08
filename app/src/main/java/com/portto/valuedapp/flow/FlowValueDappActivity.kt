package com.portto.valuedapp.flow

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.ArrayAdapter
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import com.portto.sdk.core.BloctoSDK
import com.portto.sdk.flow.flow
import com.portto.valuedapp.databinding.ActivityFlowValueDappBinding

class FlowValueDappActivity : AppCompatActivity() {
    private lateinit var binding: ActivityFlowValueDappBinding

    private val viewModel by viewModels<FlowViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFlowValueDappBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.setUpUi()

        viewModel.bindUi()
    }

    private fun ActivityFlowValueDappBinding.setUpUi() {
        toolbar.setNavigationOnClickListener {
            onBackPressed()
        }

        dropdown.apply {
            val envAdapter = ArrayAdapter(
                this@FlowValueDappActivity,
                android.R.layout.simple_list_item_1,
                viewModel.envs.map { it.value }
            )
            setAdapter(envAdapter)
            setText(viewModel.envs.first().value, false)
            setOnItemClickListener { _, _, pos, _ -> viewModel.setEnv(viewModel.envs[pos]) }
        }
    }

    private fun FlowViewModel.bindUi() {
        val lifecycleOwner = this@FlowValueDappActivity
        currentEnv.observe(lifecycleOwner) {
            // Init SDK with updated env
            BloctoSDK.init(appId = it.appId, debug = it == FlowEnv.TESTNET)
            // Reset UI
            // TODO: reset UI
        }

        // Update address label and Connect button
        currentAddress.observe(lifecycleOwner) {
            if (it.isNullOrEmpty()) {
                binding.connectButton.text = "Log in"
                binding.connectButton.setOnClickListener { logIn() }
                binding.currentAddress.text = ""
            } else {
                binding.connectButton.text = "Log out"
                binding.connectButton.setOnClickListener { logOut() }
                binding.currentAddress.text = "Address: $it"
            }
        }

        errorMsg.observe(lifecycleOwner)
        {
            it?.let {
                Snackbar.make(binding.container, it, Snackbar.LENGTH_SHORT).show()
                setErrorMsg(null)
            }
        }
    }

    private fun logIn() {
        BloctoSDK.flow.requestAccount(
            context = this,
            onSuccess = { viewModel.setCurrentAddress(it) },
            onError = { viewModel.setErrorMsg(it.message) })
    }

    private fun openExplorer(txHash: String) {
        val uri = Uri.Builder()
            .scheme("https")
            .authority(if (BloctoSDK.debug) "testnet.flowscan.org" else "flowscan.org")
            .path("transaction/$txHash")
            .build()
        startActivity(Intent(Intent.ACTION_VIEW, uri))
    }
}
