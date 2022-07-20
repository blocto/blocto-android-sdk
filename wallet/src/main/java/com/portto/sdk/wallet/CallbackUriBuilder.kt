package com.portto.sdk.wallet

import android.net.Uri

object CallbackUriBuilder {

    @JvmStatic
    fun build(callback: Callback): Uri {
        val builder = Uri.Builder()
            .scheme(Const.BLOCTO_SCHEME)
            .appendQueryParameter(Const.KEY_REQUEST_ID, callback.requestId)

        when (callback) {
            is Callback.Authentication -> builder.appendAuthnQueryParameters(callback)
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

    private fun Uri.Builder.appendAuthnQueryParameters(callback: Callback.Authentication) {
        appendQueryParameter(Const.KEY_ADDRESS, callback.address)

        callback.signatures.forEachIndexed { index, compositeSignature ->
            appendQueryParameter(
                "${Const.KEY_ACCOUNT_PROOF}[$index][${Const.KEY_ADDRESS}]",
                compositeSignature.address
            )
            appendQueryParameter(
                "${Const.KEY_ACCOUNT_PROOF}[$index][${Const.KEY_KEY_ID}]",
                compositeSignature.keyId
            )
            appendQueryParameter(
                "${Const.KEY_ACCOUNT_PROOF}[$index][${Const.KEY_SIGNATURE}]",
                compositeSignature.signature
            )
        }
    }
}
