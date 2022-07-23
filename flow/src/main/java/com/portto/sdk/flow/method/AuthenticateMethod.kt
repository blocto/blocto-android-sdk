package com.portto.sdk.flow.method

import android.net.Uri
import com.portto.sdk.core.Blockchain
import com.portto.sdk.core.method.Method
import com.portto.sdk.flow.parse
import com.portto.sdk.wallet.BloctoSDKError
import com.portto.sdk.wallet.Const
import com.portto.sdk.wallet.METHOD_FLOW_AUTHN
import com.portto.sdk.wallet.flow.AccountProofData

/**
 * Flow authentication
 * If the authentication is success, the callback includes [AccountProofData]
 *
 * @param flowAppId the app identifier required by Flow
 * @param flowNonce nonce required by Flow
 */
class AuthenticateMethod(
    private val flowAppId: String,
    private val flowNonce: String,
    blockchain: Blockchain = Blockchain.FLOW,
    onSuccess: (AccountProofData) -> Unit,
    onError: (BloctoSDKError) -> Unit
) : Method<AccountProofData>(blockchain, onSuccess, onError) {

    override val name: String
        get() = METHOD_FLOW_AUTHN

    override fun handleCallback(uri: Uri) {
        val address = uri.getQueryParameter(Const.KEY_ADDRESS)
        if (address.isNullOrEmpty()) {
            onError(BloctoSDKError.INVALID_RESPONSE)
            return
        }
        val signatures = uri.parse(name, address)
        if (signatures.isNullOrEmpty()) {
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
    }

    override fun encodeToUri(authority: String, appId: String, requestId: String): Uri.Builder {
        return super.encodeToUri(authority, appId, requestId)
            .appendQueryParameter(Const.KEY_FLOW_APP_ID, flowAppId)
            .appendQueryParameter(Const.KEY_FLOW_NONCE, flowNonce)
    }
}