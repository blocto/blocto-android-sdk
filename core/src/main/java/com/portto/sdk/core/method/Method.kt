package com.portto.sdk.core.method

import android.net.Uri
import com.portto.sdk.core.Blockchain
import com.portto.sdk.core.BloctoSDKError
import com.portto.sdk.core.Const

sealed class Method<T>(
    private val blockchain: Blockchain,
    val onSuccess: (T) -> Unit,
    val onError: (BloctoSDKError) -> Unit
) {

    protected abstract val name: String

    open fun encodeToUri(authority: String, appId: String, requestId: String): Uri.Builder {
        return Uri.Builder()
            .scheme(Const.HTTPS_SCHEME)
            .authority(authority)
            .appendPath(Const.BLOCTO_URI_PATH)
            .appendQueryParameter(Const.KEY_APP_ID, appId)
            .appendQueryParameter(Const.KEY_REQUEST_ID, requestId)
            .appendQueryParameter(Const.KEY_METHOD, name)
            .appendQueryParameter(Const.KEY_BLOCKCHAIN, blockchain.value)
    }
}
