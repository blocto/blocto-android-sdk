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
 * Flow - authn
 * Allow to authenticate with Blocto app
 * @param flowAppId optional; flow app identifier required by account proof
 * @param flowNonce optional; nonce required by account proof
 * @param blockchain Flow exclusive
 * @param onSuccess the callback that includes [AccountProofData]
 * @param onError the callback that includes [BloctoSDKError]
 */
class AuthenticateMethod(
    private val flowAppId: String?,
    private val flowNonce: String?,
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
        // If flow app id and nonce are provided, signatures shall not be null or empty
        if ((flowAppId != null && flowNonce != null) && signatures.isNullOrEmpty()) {
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
            .apply {
                flowAppId?.let { appendQueryParameter(Const.KEY_FLOW_APP_ID, it) }
                flowNonce?.let { appendQueryParameter(Const.KEY_FLOW_NONCE, it) }
            }
    }
}