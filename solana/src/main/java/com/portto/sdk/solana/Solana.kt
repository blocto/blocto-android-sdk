package com.portto.sdk.solana

import android.content.Context
import androidx.annotation.WorkerThread
import com.portto.sdk.core.*
import com.portto.sdk.core.method.RequestAccountMethod
import com.portto.sdk.solana.method.SignAndSendTransactionMethod
import com.portto.sdk.solana.model.SolanaRawTxRequest
import com.portto.sdk.wallet.BloctoSDKError
import com.portto.solana.web3.AccountMeta
import com.portto.solana.web3.Message
import com.portto.solana.web3.Transaction
import com.portto.solana.web3.TransactionInstruction
import org.komputing.kbase58.decodeBase58

val BloctoSDK.solana by lazy { Solana(SolanaService) }

class Solana(private val api: SolanaService) : Chain, Account {

    private val walletProgramId
        get() = (if (BloctoSDK.debug) "Ckv4czD7qPmQvy2duKEa45WRp3ybD2XuaJzQAWrhAour"
        else "JBn9VwAiqpizWieotzn6FjEXrBu4fDe2XFjiFqZwp8Am")

    private var appendTxs = mutableMapOf<String, Map<String, String>>()

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

        val message = transaction.serializeMessage().toHexString()

        val method = SignAndSendTransactionMethod(
            fromAddress = fromAddress,
            message = message,
            isInvokeWrapped = isInvokeWrapped,
            publicKeySignaturePairs = publicKeySignaturePairs.takeIf { it.isNotEmpty() },
            appendTx = appendTxs[message]?.takeIf { it.isNotEmpty() },
            blockchain = blockchain,
            onSuccess = onSuccess,
            onError = onError
        )
        BloctoSDK.send(context, method)
        appendTxs.remove(message)
    }

    @WorkerThread
    fun convertToProgramWalletTransaction(
        address: String,
        transaction: Transaction
    ): Transaction {
        val rawTx = transaction.serializeMessage().toHexString()
        val request = SolanaRawTxRequest(address, rawTx)
        val response = api.createRawTransaction(request)
        val message = Message.from(response.rawTx.decodeHex())
        return Transaction().apply {
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
        }.also {
            appendTxs[it.serializeMessage().toHexString()] = response.extraData.appendTx
        }
    }
}
