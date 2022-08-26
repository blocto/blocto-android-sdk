package com.portto.sdk.flow

import android.content.Context
import androidx.annotation.WorkerThread
import com.portto.sdk.core.Blockchain
import com.portto.sdk.core.BloctoSDK
import com.portto.sdk.core.Chain
import com.portto.sdk.flow.method.AuthenticateMethod
import com.portto.sdk.flow.method.SendTransactionMethod
import com.portto.sdk.flow.method.SignMessageMethod
import com.portto.sdk.wallet.BloctoSDKError
import com.portto.sdk.wallet.flow.AccountProofData
import com.portto.sdk.wallet.flow.CompositeSignature

val BloctoSDK.flow by lazy { Flow(FlowService) }

class Flow(private val api: FlowService) : Chain {

    override val blockchain: Blockchain = Blockchain.FLOW

    /**
     * Request for composite signatures
     */
    fun authenticate(
        context: Context,
        flowAppId: String?,
        flowNonce: String?,
        onSuccess: (AccountProofData) -> Unit,
        onError: (BloctoSDKError) -> Unit
    ) {
        val method = AuthenticateMethod(
            flowAppId = flowAppId,
            flowNonce = flowNonce,
            onSuccess = onSuccess,
            onError = onError
        )
        BloctoSDK.send(context, method, false)
    }

    /**
     * Sign user message
     */
    fun signUserMessage(
        context: Context,
        address: String,
        message: String,
        onSuccess: (List<CompositeSignature>) -> Unit,
        onError: (BloctoSDKError) -> Unit
    ) {
        val method = SignMessageMethod(
            fromAddress = address,
            message = message,
            onSuccess = onSuccess,
            onError = onError
        )
        BloctoSDK.send(context, method, false)
    }

    /**
     * Send transaction
     */
    fun sendTransaction(
        context: Context,
        address: String,
        transaction: String,
        onSuccess: (String) -> Unit,
        onError: (BloctoSDKError) -> Unit
    ) {
        val method = SendTransactionMethod(
            fromAddress = address,
            encodedTxData = transaction,
            onSuccess = onSuccess,
            onError = onError
        )
        BloctoSDK.send(context, method, false)
    }

    /**
     * Get Blocto fee payer address to compose transaction
     * @return fee payer address
     */
    @WorkerThread
    fun getFeePayerAddress(): String = api.getFeePayer().address
}