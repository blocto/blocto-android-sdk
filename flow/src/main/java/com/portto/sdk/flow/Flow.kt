package com.portto.sdk.flow

import android.content.Context
import com.portto.sdk.core.Blockchain
import com.portto.sdk.core.BloctoSDK
import com.portto.sdk.core.Chain
import com.portto.sdk.flow.method.AuthenticateMethod
import com.portto.sdk.wallet.BloctoSDKError
import com.portto.sdk.wallet.flow.AccountProofData

val BloctoSDK.flow by lazy { Flow() }

class Flow : Chain {

    override val blockchain: Blockchain = Blockchain.FLOW

    /**
     * Request for composite signatures
     */
    fun authenticate(
        context: Context,
        flowAppId: String,
        flowNonce: String,
        onSuccess: (AccountProofData) -> Unit,
        onError: (BloctoSDKError) -> Unit
    ) {
        val method = AuthenticateMethod(
            flowAppId = flowAppId,
            flowNonce = flowNonce,
            onSuccess = onSuccess,
            onError = onError
        )
        BloctoSDK.send(context, method)
    }
}