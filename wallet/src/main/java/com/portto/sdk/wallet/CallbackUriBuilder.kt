package com.portto.sdk.wallet

import android.net.Uri

object CallbackUriBuilder {

    @JvmStatic
    fun build(callback: Callback): Uri {
        val builder = Uri.Builder()
            .scheme(Const.BLOCTO_SCHEME)
            .appendQueryParameter(Const.KEY_REQUEST_ID, callback.requestId)

        when (callback) {
            is Callback.RequestAccount ->
                builder.appendQueryParameter(Const.KEY_ADDRESS, callback.address)
            is Callback.SignAndSendTransaction ->
                builder.appendQueryParameter(Const.KEY_TX_HASH, callback.txHash)
            is Callback.SendTransaction ->
                builder.appendQueryParameter(Const.KEY_TX_HASH, callback.txHash)
            is Callback.SignMessage ->
                builder.appendQueryParameter(Const.KEY_SIGNATURE, callback.signature)
            is Callback.Error ->
                builder.appendQueryParameter(Const.KEY_ERROR, callback.error)
        }

        return builder.build()
    }
}
