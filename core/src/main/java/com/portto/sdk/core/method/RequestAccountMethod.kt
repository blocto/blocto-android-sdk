package com.portto.sdk.core.method

import android.net.Uri
import com.portto.sdk.core.Blockchain
import com.portto.sdk.wallet.BloctoSDKError
import com.portto.sdk.wallet.Const

class RequestAccountMethod(
    blockchain: Blockchain,
    onSuccess: (String) -> Unit,
    onError: (BloctoSDKError) -> Unit
) : Method<String>(blockchain, onSuccess, onError) {

    override val name: String
        get() = "request_account"

    override fun handleCallback(uri: Uri) {
        val address = uri.getQueryParameter(Const.KEY_ADDRESS)
        if (address.isNullOrEmpty()) {
            onError(BloctoSDKError.INVALID_RESPONSE)
            return
        }
        onSuccess(address)
    }

    override fun encodeToWebUri(
        authority: String,
        appId: String,
        requestId: String,
        webSessionId: String?
    ): Uri.Builder {
        return super.encodeToWebUri(authority, appId, requestId, webSessionId)
            .appendPath(Const.PATH_AUTHN)
            .appendQueryParameter(Const.KEY_REQUEST_ID, requestId)
            .appendQueryParameter(Const.KEY_REQUEST_SOURCE, Const.SDK_SOURCE)
    }
}
