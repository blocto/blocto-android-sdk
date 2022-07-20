package com.portto.sdk.flow.method

import android.net.Uri
import com.portto.sdk.core.Blockchain
import com.portto.sdk.core.method.Method
import com.portto.sdk.flow.parse
import com.portto.sdk.wallet.BloctoSDKError
import com.portto.sdk.wallet.Const
import com.portto.sdk.wallet.METHOD_AUTHN
import com.portto.sdk.wallet.flow.AccountProofData

class AuthenticateMethod(
    private val flowAppId: String,
    private val flowNonce: String,
    blockchain: Blockchain = Blockchain.FLOW,
    onSuccess: (AccountProofData) -> Unit,
    onError: (BloctoSDKError) -> Unit
) : Method<AccountProofData>(blockchain, onSuccess, onError) {

    override val name: String
        get() = METHOD_AUTHN

    override fun handleCallback(uri: Uri) {
        val result = uri.parse()
        if (result != null) {
            val (address, signatures) = result
            if (address.isEmpty() || signatures.isEmpty()) {
                onError(BloctoSDKError.INVALID_RESPONSE)
                return
            }
            onSuccess(
                AccountProofData(
                    flowAppId = flowAppId,
                    nonce = flowNonce,
                    address = address,
                    signatures = signatures
                )
            )
        } else onError(BloctoSDKError.INVALID_RESPONSE)
    }

    override fun encodeToUri(authority: String, appId: String, requestId: String): Uri.Builder {
        return super.encodeToUri(authority, appId, requestId)
            .appendQueryParameter(Const.KEY_FLOW_APP_ID, flowAppId)
            .appendQueryParameter(Const.KEY_FLOW_NONCE, flowNonce)
    }
}