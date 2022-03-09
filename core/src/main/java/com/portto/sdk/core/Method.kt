package com.portto.sdk.core

import android.net.Uri

sealed class Method<T>(
    private val blockchain: Blockchain,
    val onSuccess: (T) -> Unit,
    val onError: (BloctoSDKError) -> Unit
) {

    protected abstract val name: String

    open fun encodeToUri(): Uri.Builder {
        return Uri.Builder()
            .scheme(Const.BLOCTO_URI_SCHEME)
            .authority(Const.BLOCTO_URI_AUTHORITY)
            .appendPath(Const.BLOCTO_URI_PATH)
            .appendQueryParameter(Const.KEY_BLOCKCHAIN, blockchain.value)
    }
}

class RequestAccountMethod(
    blockchain: Blockchain,
    onSuccess: (String) -> Unit,
    onError: (BloctoSDKError) -> Unit
) : Method<String>(blockchain, onSuccess, onError) {

    override val name: String
        get() = "request_account"

    override fun encodeToUri(): Uri.Builder {
        return super.encodeToUri()
            .appendQueryParameter(Const.KEY_METHOD, name)
    }
}
