package com.portto.valuedapp.solana

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Base64
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.google.android.material.snackbar.Snackbar
import com.portto.sdk.core.BloctoSDK
import com.portto.sdk.solana.solana
import com.portto.sdk.wallet.BloctoSDKError
import com.portto.solana.web3.Connection
import com.portto.solana.web3.KeyPair
import com.portto.solana.web3.PublicKey
import com.portto.solana.web3.Transaction
import com.portto.solana.web3.programs.SystemProgram
import com.portto.solana.web3.util.Cluster
import com.portto.valuedapp.*
import com.portto.valuedapp.Config.APP_ID_MAINNET
import com.portto.valuedapp.Config.APP_ID_TESTNET
import com.portto.valuedapp.databinding.ActivitySolanaValueDappBinding
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.near.borshj.BorshBuffer

class SolanaValueDappActivity : AppCompatActivity() {

    private data class Env(
        val name: String,
        val cluster: Cluster,
        val appId: String
    )

    private val envs = listOf(
        Env(
            name = "Mainnet Beta",
            cluster = Cluster.MAINNET_BETA,
            appId = APP_ID_MAINNET
        ),
        Env(
            name = "Devnet",
            cluster = Cluster.DEVNET,
            appId = APP_ID_TESTNET
        )
    )

    private lateinit var binding: ActivitySolanaValueDappBinding
    private lateinit var connection: Connection
    private var currentAddress: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySolanaValueDappBinding.inflate(layoutInflater)
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

        binding.valueInput.textChanges()
            .onEach {
                binding.setValueButton.isEnabled = !it.isNullOrEmpty()
                binding.partialSignButton.isEnabled = !it.isNullOrEmpty()
            }
            .launchIn(lifecycleScope)

        binding.connectButton.setOnClickListener {
            it.hideKeyboard()
            requestAccount()
        }

        binding.setValueButton.setOnClickListener {
            it.hideKeyboard()
            setValue()
        }

        binding.partialSignButton.setOnClickListener {
            it.hideKeyboard()
            createAccountAndSetValue()
        }

        binding.getValueButton.setOnClickListener {
            it.hideKeyboard()
            getValue()
        }

        binding.setValueTxHash.setOnClickListener {
            val txHash = binding.setValueTxHash.text.toString()
            openExplorer(txHash)
        }

        binding.partialSignTxHash.setOnClickListener {
            val txHash = binding.partialSignTxHash.text.toString()
            openExplorer(txHash)
        }
    }

    private fun setEnv(env: Env) {
        BloctoSDK.init(appId = env.appId, debug = env.cluster == Cluster.DEVNET)
        connection = Connection(env.cluster)
        currentAddress = null
        binding.connectButton.text = getString(R.string.button_connect)
        binding.valueInput.text = null
        binding.setValueTxHash.isVisible = false
        binding.partialSignTxHash.isVisible = false
        binding.value.text = ""
    }

    private fun requestAccount() {
        BloctoSDK.solana.requestAccount(
            context = this,
            onSuccess = {
                binding.connectButton.text = it.shortenAddress()
                currentAddress = it
            },
            onError = {
                showError(it)
            }
        )
    }

    private fun setValue() {
        binding.setValueTxHash.isVisible = false

        val address = currentAddress ?: kotlin.run {
            showError("wallet not connected")
            return
        }

        val value = binding.valueInput.text.toString().toIntOrNull() ?: kotlin.run {
            showError("not a valid integer")
            return
        }

        binding.setValueButton.showLoading()

        val walletAddress = PublicKey(address)
        val transaction = Transaction()
        transaction.feePayer = walletAddress

        val instruction = ValueProgram.createSetValueInstruction(value, walletAddress)
        transaction.add(instruction)

        val exceptionHandler = CoroutineExceptionHandler { _, error ->
            binding.setValueButton.hideLoading(getString(R.string.button_send_transaction))
            showError(error.message)
        }

        lifecycleScope.launch(exceptionHandler) {
            val blockhash = getLatestBlockhash() ?: return@launch
            transaction.setRecentBlockHash(blockhash)

            BloctoSDK.solana.signAndSendTransaction(
                context = this@SolanaValueDappActivity,
                fromAddress = address,
                transaction = transaction,
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
    }

    private fun createAccountAndSetValue() {
        binding.partialSignTxHash.isVisible = false

        val address = currentAddress ?: kotlin.run {
            showError("wallet not connected")
            return
        }

        val value = binding.valueInput.text.toString().toIntOrNull() ?: kotlin.run {
            showError("not a valid integer")
            return
        }

        binding.partialSignButton.showLoading()

        val walletAddress = PublicKey(address)
        val transaction = Transaction()
        transaction.feePayer = walletAddress

        val setValueInstruction = ValueProgram.createSetValueInstruction(value, walletAddress)
        transaction.add(setValueInstruction)

        val exceptionHandler = CoroutineExceptionHandler { _, error ->
            binding.partialSignButton.hideLoading(getString(R.string.button_send_transaction))
            showError(error.message)
        }

        lifecycleScope.launch(exceptionHandler) {
            val blockhash = getLatestBlockhash() ?: return@launch
            transaction.setRecentBlockHash(blockhash)

            val newAccount = KeyPair.generate()
            val createAccountInstruction = SystemProgram.createAccount(
                fromPublicKey = walletAddress,
                newAccountPublicKey = newAccount.publicKey,
                lamports = getMinimumBalanceForRentExemption(10),
                space = 10,
                programId = ValueProgram.programId
            )
            transaction.add(createAccountInstruction)

            val newTransaction = convertToProgramWalletTransaction(address, transaction)
            newTransaction.partialSign(newAccount)

            BloctoSDK.solana.signAndSendTransaction(
                context = this@SolanaValueDappActivity,
                fromAddress = address,
                transaction = newTransaction,
                onSuccess = {
                    binding.partialSignButton.hideLoading(getString(R.string.button_send_transaction))
                    with(binding.partialSignTxHash) {
                        text = it
                        isVisible = true
                    }
                },
                onError = {
                    binding.partialSignButton.hideLoading(getString(R.string.button_send_transaction))
                    showError(it)
                }
            )
        }
    }

    private fun getValue() {
        binding.getValueButton.showLoading()

        val exceptionHandler = CoroutineExceptionHandler { _, error ->
            binding.getValueButton.hideLoading(getString(R.string.button_get_value))
            showError(error.message)
        }

        lifecycleScope.launch(exceptionHandler) {
            val accountInfo = getAccountInfo(ValueProgram.accountPublicKey)
                ?: throw Throwable("cannot find the account")
            val data = accountInfo.data.firstOrNull() ?: throw Throwable("empty data")
            val byteArray = Base64.decode(data, Base64.DEFAULT)
            val buffer = BorshBuffer.wrap(byteArray)
            val value = buffer.let {
                it.readU8()     // isInit
                it.readU32()    // value
            }
            binding.value.text = "$value"
            binding.getValueButton.hideLoading(getString(R.string.button_get_value))
        }
    }

    private suspend fun getLatestBlockhash(): String? = withContext(Dispatchers.Default) {
        connection.getLatestBlockhash()
    }

    private suspend fun getMinimumBalanceForRentExemption(
        dataLength: Int
    ): Long = withContext(Dispatchers.Default) {
        connection.getMinimumBalanceForRentExemption(dataLength)
    }

    private suspend fun getAccountInfo(publicKey: PublicKey) = withContext(Dispatchers.Default) {
        connection.getAccountInfo(publicKey.toBase58())
    }

    private suspend fun convertToProgramWalletTransaction(
        address: String,
        transaction: Transaction
    ): Transaction = withContext(Dispatchers.Default) {
        BloctoSDK.solana.convertToProgramWalletTransaction(address, transaction)
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
            .authority("explorer.solana.com")
            .path("tx/$txHash")
            .apply {
                if (BloctoSDK.debug) {
                    appendQueryParameter("cluster", "devnet")
                }
            }
            .build()
        startActivity(Intent(Intent.ACTION_VIEW, uri))
    }
}
