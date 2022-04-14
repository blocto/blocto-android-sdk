package com.portto.valuedapp.solana

import android.os.Bundle
import android.util.Base64
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.google.android.material.snackbar.Snackbar
import com.portto.sdk.core.BloctoSDK
import com.portto.sdk.core.BloctoSDKError
import com.portto.sdk.solana.ProgramWallet
import com.portto.sdk.solana.solana
import com.portto.solana.web3.Connection
import com.portto.solana.web3.KeyPair
import com.portto.solana.web3.PublicKey
import com.portto.solana.web3.Transaction
import com.portto.solana.web3.programs.SystemProgram
import com.portto.solana.web3.util.Cluster
import com.portto.valuedapp.*
import com.portto.valuedapp.databinding.ActivitySolanaValueDappBinding
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.near.borshj.BorshBuffer

class SolanaValueDappActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySolanaValueDappBinding
    private val connection by lazy { Connection(Cluster.DEVNET) }
    private var currentAddress: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySolanaValueDappBinding.inflate(layoutInflater)
        setContentView(binding.root)

        BloctoSDK.init(appId = "57f397df-263c-4e97-b61f-15b67b9ce285", debug = true)

        binding.toolbar.setNavigationOnClickListener {
            onBackPressed()
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
    }

    private fun requestAccount() {
        BloctoSDK.solana.requestAccount(
            context = this,
            onSuccess = {
                val address = "${it.substring(0, 6)}...${it.substring(it.length - 6, it.length)}"
                binding.connectButton.text = address
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
                        text = getString(R.string.tx_hash, it)
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
                programId = ValueProgram.PROGRAM_ID
            )
            transaction.add(createAccountInstruction)

            val programWallet = convertToProgramWalletTransaction(address, transaction)
            programWallet.transaction.partialSign(newAccount)

            BloctoSDK.solana.signAndSendTransaction(
                context = this@SolanaValueDappActivity,
                fromAddress = address,
                transaction = programWallet.transaction,
                appendTx = programWallet.appendTx,
                onSuccess = {
                    binding.partialSignButton.hideLoading(getString(R.string.button_send_transaction))
                    with(binding.partialSignTxHash) {
                        text = getString(R.string.tx_hash, it)
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
            val accountInfo = getAccountInfo(ValueProgram.ACCOUNT_PUBLIC_KEY)
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
        return@withContext connection.getLatestBlockhash()
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
    ): ProgramWallet = withContext(Dispatchers.Default) {
        return@withContext BloctoSDK.solana.convertToProgramWalletTransaction(address, transaction)
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
