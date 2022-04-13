package com.portto.sdk.solana

import android.content.Context
import androidx.annotation.WorkerThread
import com.portto.sdk.core.*
import com.portto.sdk.core.method.RequestAccountMethod
import com.portto.sdk.core.method.SignAndSendTransactionMethod
import com.portto.solana.web3.AccountMeta
import com.portto.solana.web3.Message
import com.portto.solana.web3.Transaction
import com.portto.solana.web3.TransactionInstruction
import org.komputing.kbase58.decodeBase58

val BloctoSDK.solana by lazy { Solana() }

class Solana : Chain, Account {

    private val api by lazy { BloctoApi() }

    private val walletProgramId = if (BloctoSDK.debug) {
        "Ckv4czD7qPmQvy2duKEa45WRp3ybD2XuaJzQAWrhAour"
    } else {
        "JBn9VwAiqpizWieotzn6FjEXrBu4fDe2XFjiFqZwp8Am"
    }

    override val blockchain: Blockchain
        get() = Blockchain.SOLANA

    override fun requestAccount(
        context: Context,
        onSuccess: (String) -> Unit,
        onError: (BloctoSDKError) -> Unit
    ) {
        val method = RequestAccountMethod(
            blockchain = blockchain,
            onSuccess = onSuccess,
            onError = onError
        )
        BloctoSDK.send(context, method)
    }

    fun signAndSendTransaction(
        context: Context,
        fromAddress: String,
        transaction: Transaction,
        appendTx: Map<String, String>? = null,
        onSuccess: (String) -> Unit,
        onError: (BloctoSDKError) -> Unit
    ) {
        val publicKeySignaturePairs = mutableMapOf<String, String>()
        transaction.signatures.forEach {
            it.signature?.let { signature ->
                publicKeySignaturePairs[it.publicKey.toBase58()] = signature.toHexString()
            }
        }

        val isInvokeWrapped = transaction.instructions.any {
            it.programId.toBase58() == walletProgramId
        }

        val method = SignAndSendTransactionMethod(
            fromAddress = fromAddress,
            message = transaction.serializeMessage().toHexString(),
            isInvokeWrapped = isInvokeWrapped,
            publicKeySignaturePairs = publicKeySignaturePairs,
            appendTx = appendTx,
            blockchain = blockchain,
            onSuccess = onSuccess,
            onError = onError
        )
        BloctoSDK.send(context, method)
    }

    @WorkerThread
    fun convertToProgramWalletTransaction(
        address: String,
        transaction: Transaction
    ): ProgramWallet {
        val rawTx = transaction.serializeMessage().toHexString()
        val request = SolanaRawTxRequest(address, rawTx)
        val response = api.createRawTransaction(request)
        val message = Message.from(response.rawTx.decodeHex())
        val newTransaction = Transaction().apply {
            this.recentBlockhash = message.recentBlockhash

            if (message.header.numRequiredSignatures > 0) {
                this.feePayer = message.accountKeys[0]
            }
            message.instructions.forEach { instruction ->
                val keys = instruction.accounts.map { account ->
                    val publicKey = message.accountKeys[account]
                    AccountMeta(
                        publicKey,
                        isSigner = account < message.header.numRequiredSignatures,
                        isWritable = message.isAccountWritable(account)
                    )
                }
                this.add(
                    TransactionInstruction(
                        programId = message.accountKeys[instruction.programIdIndex],
                        keys = keys,
                        data = instruction.data.decodeBase58()
                    )
                )
            }
        }
        return ProgramWallet(newTransaction, response.extraData.appendTx)
    }
}

data class ProgramWallet(
    val transaction: Transaction,
    val appendTx: Map<String, String>
)
