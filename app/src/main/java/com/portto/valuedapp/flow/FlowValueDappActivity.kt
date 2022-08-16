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
import com.portto.valuedapp.Utils.shortenAddress
import com.portto.valuedapp.databinding.ActivityFlowValueDappBinding
import com.portto.valuedapp.databinding.LayoutSetValueBinding
import com.portto.valuedapp.databinding.LayoutSignMessageBinding
import com.portto.valuedapp.flow.FlowUtils.mapToString
import com.portto.valuedapp.hideLoading
import com.portto.valuedapp.showLoading
import timber.log.Timber

class FlowValueDappActivity : AppCompatActivity() {
    private lateinit var binding: ActivityFlowValueDappBinding
    private lateinit var signMsgBinding: LayoutSignMessageBinding
    private lateinit var setValueBinding: LayoutSetValueBinding

    private val viewModel by viewModels<FlowViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFlowValueDappBinding.inflate(layoutInflater)
        signMsgBinding = LayoutSignMessageBinding.bind(binding.root)
        setValueBinding = LayoutSetValueBinding.bind(binding.root)
        setContentView(binding.root)

        binding.setUpUi()

        signMsgBinding.setUpUi()

        setValueBinding.setUpUi()

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
            setOnItemClickListener { _, _, pos, _ ->
                viewModel.reset()
                viewModel.setEnv(viewModel.envs[pos])
            }
        }
    }

    private fun LayoutSignMessageBinding.setUpUi() {
        signButton.setOnClickListener {
            signMessage(input.text?.toString())
        }
    }

    private fun LayoutSetValueBinding.setUpUi() {
        setValueButton.setOnClickListener { sendTransaction() }
        setValueTxHash.setOnClickListener {
            val txHash = setValueTxHash.text.toString()
            openExplorer(txHash)
        }
    }

    private fun FlowViewModel.bindUi() {
        val lifecycleOwner = this@FlowValueDappActivity
        currentEnv.observe(lifecycleOwner) {
            // Init SDK with updated env
            BloctoSDK.init(appId = it.appId, debug = it == FlowEnv.TESTNET)
        }

        // Update Connect button to address once authenticated
        address.observe(lifecycleOwner) {
            if (it == null) {
                binding.connectButton.text = getString(R.string.button_connect)
                binding.connectButton.setOnClickListener { showAuthenticationDialog() }
            } else {
                binding.connectButton.text = it.shortenAddress()
                binding.connectButton.setOnClickListener { logOut() }
            }
        }

        accountProofSignatures.observe(lifecycleOwner) { signatures ->
            if (signatures == null) {
                binding.showAccountProofDataButton.isVisible = false
            } else {
                binding.showAccountProofDataButton.isVisible = true
                binding.showAccountProofDataButton.setOnClickListener {
                    showCompositeSignaturesDialog(signatures.mapToString())
                }
            }
        }

        userSignatureData.observe(lifecycleOwner) { data ->
            if (data == null) {
                signMsgBinding.showSignatureButton.isEnabled = false
            } else {
                signMsgBinding.showSignatureButton.isEnabled = true
                signMsgBinding.showSignatureButton.setOnClickListener {
                    showCompositeSignaturesDialog(data.mapToString())
                }
            }
        }

        errorMsg.observe(lifecycleOwner) {
            it?.let {
                Snackbar.make(binding.container, it, Snackbar.LENGTH_SHORT).show()
            }
        }

        // Navigate to blocto app once the tx is composed
        sendTxData.observe(lifecycleOwner) { txData ->
            txData?.let {
                val address = viewModel.address.value
                if (address.isNullOrEmpty()) return@observe

                BloctoSDK.flow.sendTransaction(
                    context = this@FlowValueDappActivity,
                    address = address,
                    transaction = txData,
                    onSuccess = {
                        setValueBinding.setValueButton.hideLoading(getString(R.string.button_send_transaction))
                        viewModel.setTxHash(it)
                    },
                    onError = {
                        setValueBinding.setValueButton.hideLoading(getString(R.string.button_send_transaction))
                        viewModel.setErrorMessage(it)
                    }
                )
                viewModel.resetTxData()
            }
        }

        txHash.observe(lifecycleOwner) {
            if (it.isNullOrEmpty()) {
                setValueBinding.setValueTxHash.text = ""
                setValueBinding.setValueTxHash.isVisible = false
            } else {
                setValueBinding.setValueTxHash.text = it
                setValueBinding.setValueTxHash.isVisible = true
            }
        }
    }

    private fun logIn(withAccountProof: Boolean) {
        BloctoSDK.flow.authenticate(
            context = this,
            flowAppId = if (withAccountProof) Config.FLOW_APP_IDENTIFIER else null,
            flowNonce = if (withAccountProof) Config.FLOW_NONCE else null,
            onSuccess = { viewModel.setAccountProofData(it) },
            onError = { viewModel.setErrorMessage(it) })
    }

    private fun logOut() {
        viewModel.reset()
    }

    private fun signMessage(inputMsg: String?) {
        val address = viewModel.address.value
        if (address == null) {
            viewModel.setErrorMessage("wallet not connected")
            return
        }

        val msg = inputMsg?.trim()
        if (msg.isNullOrBlank()) {
            viewModel.setErrorMessage("empty message")
            return
        }

        BloctoSDK.flow.signUserMessage(
            context = this,
            address = address,
            message = msg,
            onSuccess = { viewModel.setUserSignatureData(it) },
            onError = { viewModel.setErrorMessage(it) })
    }

    private fun sendTransaction() {
        val address = viewModel.address.value
        if (address == null) {
            viewModel.setErrorMessage("wallet not connected")
            return
        }

        val env = viewModel.currentEnv.value
        if (env == null) {
            viewModel.setErrorMessage("env not set")
            return
        }

        val inputText = setValueBinding.valueInput.text?.toString()
        if (inputText.isNullOrEmpty()) {
            viewModel.setErrorMessage("Input text is empty")
            return
        }
        setValueBinding.setValueButton.showLoading()
        viewModel.composeTransaction(address, inputText, env == FlowEnv.MAINNET)
    }

    private fun showAuthenticationDialog() {
        val items = arrayOf("With Account Proof", "Without Account Proof")
        MaterialAlertDialogBuilder(this)
            .setTitle("Authentication")
            .setItems(items) { dialog, which ->
                logIn(which == 0)
                dialog.dismiss()
            }
            .show()
    }

    private fun showCompositeSignaturesDialog(message: String) {
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.title_composite_signatures)
            .setMessage(message)
            .setPositiveButton("Copy") { dialog, _ ->
                copyToClipboard(message)
                dialog.dismiss()
            }.setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }.show()
    }

    /**
     * Copy the composite signatures to clipboard for verifying
     */
    private fun copyToClipboard(message: String) {
        val clipboard = ContextCompat.getSystemService(
            this@FlowValueDappActivity, ClipboardManager::class.java
        )
        val clip = ClipData.newPlainText("Flow Signatures", message)
        clipboard?.setPrimaryClip(clip)
        Toast.makeText(this, "Copied!", Toast.LENGTH_SHORT).show()
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
