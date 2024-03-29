package com.portto.sdk.core.method

import android.net.Uri
import androidx.annotation.WorkerThread
import com.portto.sdk.core.Blockchain
import com.portto.sdk.wallet.BloctoSDKError
import com.portto.sdk.wallet.Const

abstract class Method<T>(
    internal val blockchain: Blockchain,
    val onSuccess: (T) -> Unit,
    val onError: (BloctoSDKError) -> Unit
) {

    abstract val name: String

    abstract fun handleCallback(uri: Uri)

    open fun encodeToUri(authority: String, appId: String, requestId: String): Uri.Builder {
        return Uri.Builder()
            .scheme(Const.HTTPS_SCHEME)
            .authority(authority)
            .appendPath(Const.BLOCTO_URI_PATH)
            .appendQueryParameter(Const.KEY_APP_ID, appId)
            .appendQueryParameter(Const.KEY_REQUEST_ID, requestId)
            .appendQueryParameter(Const.KEY_METHOD, name)
            .appendQueryParameter(Const.KEY_BLOCKCHAIN, blockchain.value)
            .appendQueryParameter(Const.KEY_PLATFORM, Const.SDK_SOURCE)
    }

    @WorkerThread
    open fun encodeToWebUri(
        authority: String,
        appId: String,
        requestId: String,
        webSessionId: String? = null
    ): Uri.Builder {
        return Uri.Builder()
            .scheme(Const.HTTPS_SCHEME)
            .authority(authority)
            .appendPath(appId)
            .appendPath(blockchain.value)
    }
}
