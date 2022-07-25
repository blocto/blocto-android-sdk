package com.portto.sdk.flow.method

import android.net.Uri
import com.portto.sdk.core.Blockchain
import com.portto.sdk.core.method.Method
import com.portto.sdk.flow.parse
import com.portto.sdk.wallet.BloctoSDKError
import com.portto.sdk.wallet.Const
import com.portto.sdk.wallet.METHOD_FLOW_SIGN_MESSAGE
import com.portto.sdk.wallet.flow.CompositeSignature

/**
 * Flow - user_signatures
 * Sign the specified message by Blocto app
 * @param fromAddress the account
 * @param message the plain text to sign
 * @param blockchain Flow exclusive
 * @param onSuccess the callback that includes a list of [CompositeSignature]
 * @param onError the callback that includes [BloctoSDKError]
 */
class SignMessageMethod(
    private val fromAddress: String,
    private val message: String,
    blockchain: Blockchain = Blockchain.FLOW,
    onSuccess: (List<CompositeSignature>) -> Unit,
    onError: (BloctoSDKError) -> Unit
) : Method<List<CompositeSignature>>(blockchain, onSuccess, onError) {

    override val name: String
        get() = METHOD_FLOW_SIGN_MESSAGE

    override fun handleCallback(uri: Uri) {
        val signatures = uri.parse(name, fromAddress)
        if (signatures.isNullOrEmpty()) {
            onError(BloctoSDKError.INVALID_RESPONSE)
            return
        }
        onSuccess(signatures)
    }

    override fun encodeToUri(authority: String, appId: String, requestId: String): Uri.Builder {
        return super.encodeToUri(authority, appId, requestId)
            .appendQueryParameter(Const.KEY_FROM, fromAddress)
            .appendQueryParameter(Const.KEY_MESSAGE, message)
    }
}
