package com.portto.valuedapp.flow

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.portto.sdk.core.BloctoSDK
import com.portto.sdk.flow.flow
import com.portto.valuedapp.Config
import com.portto.valuedapp.R
import com.portto.valuedapp.databinding.ActivityFlowValueDappBinding
import com.portto.valuedapp.databinding.LayoutSignMessageBinding
import com.portto.valuedapp.mapToString
import com.portto.valuedapp.shortenAddress

class FlowValueDappActivity : AppCompatActivity() {
    private lateinit var binding: ActivityFlowValueDappBinding
    private lateinit var signMsgBinding: LayoutSignMessageBinding

    private val viewModel by viewModels<FlowViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFlowValueDappBinding.inflate(layoutInflater)
        signMsgBinding = LayoutSignMessageBinding.bind(binding.root)
        setContentView(binding.root)

        binding.setUpUi()

        signMsgBinding.setUpUi()

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

    private fun LayoutSignMessageBinding.setUpUi() {
        signButton.setOnClickListener {
            signMessage(input.text?.toString())
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
        accountProofData.observe(lifecycleOwner) { data ->
            if (data == null) {
                binding.connectButton.text = getString(R.string.button_connect)
                binding.connectButton.setOnClickListener { logIn() }
                binding.showAccountProofDataButton.isVisible = false
            } else {
                binding.connectButton.text = data.address.shortenAddress()
                binding.connectButton.setOnClickListener { logOut() }
                binding.showAccountProofDataButton.isVisible = true
                binding.showAccountProofDataButton.setOnClickListener {
                    showAccountProofDataDialog(data.signatures.mapToString())
                }
            }
        }

        errorMsg.observe(lifecycleOwner)
        {
            it?.let {
                Snackbar.make(binding.container, it, Snackbar.LENGTH_SHORT).show()
            }
        }
    }

    private fun logIn() {
        BloctoSDK.flow.authenticate(
            context = this,
            flowAppId = Config.FLOW_APP_IDENTIFIER,
            flowNonce = Config.FLOW_NONCE,
            onSuccess = { viewModel.setAccountProofData(it) },
            onError = { viewModel.showError(it) })
    }

    private fun logOut() {
        viewModel.setAccountProofData(null)
    }

    private fun signMessage(inputMsg: String?) {
        val address = viewModel.accountProofData.value?.address
        if (address == null) {
            viewModel.showError("wallet not connected")
            return
        }

        val msg = inputMsg?.trim()
        if (msg.isNullOrBlank()) {
            viewModel.showError("empty message")
            return
        }

//        BloctoSDK.flow.signMessage(
//            context = this,
//            fromAddress = address,
//            signType = SignTypeFlow.USER_SIGNATURE,
//            message = msg,
//            onSuccess = {
//                signMsgBinding.signature.text = it
//                signMsgBinding.signature.isVisible = true
//            },
//            onError = {
//                signMsgBinding.signature.text = ""
//                signMsgBinding.signature.isVisible = false
//                viewModel.showError(it)
//            })
    }

    private fun showAccountProofDataDialog(signatures: String) {
        MaterialAlertDialogBuilder(this@FlowValueDappActivity)
            .setTitle("Composite Signatures")
            .setMessage(signatures)
            .setPositiveButton("Copy") { dialog, _ ->
                val clipboard = ContextCompat.getSystemService(
                    this@FlowValueDappActivity,
                    ClipboardManager::class.java
                )
                val clip = ClipData.newPlainText("signatures", signatures)
                clipboard?.setPrimaryClip(clip)
                Toast.makeText(this, "Copied!", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            }.setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }.show()
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
