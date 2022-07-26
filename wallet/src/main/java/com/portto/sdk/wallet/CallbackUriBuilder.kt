package com.portto.sdk.wallet

import android.net.Uri
import com.portto.sdk.wallet.flow.CompositeSignature

object CallbackUriBuilder {

    @JvmStatic
    fun build(callback: Callback): Uri {
        val builder = Uri.Builder()
            .scheme(Const.BLOCTO_SCHEME)
            .appendQueryParameter(Const.KEY_REQUEST_ID, callback.requestId)

        when (callback) {
            is Callback.FlowAuthenticate -> {
                builder.appendQueryParameter(Const.KEY_ADDRESS, callback.address)
                builder.appendCompositeSignatures(Const.KEY_ACCOUNT_PROOF, callback.signatures)
            }
            is Callback.FlowSignMessage ->
                builder.appendCompositeSignatures(Const.KEY_USER_SIGNATURE, callback.signatures)
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

    /**
     * Append flow query parameter (composite signature)
     * including address, keyId and signature
     *
     * @param key [Const.KEY_USER_SIGNATURE] or [Const.KEY_ACCOUNT_PROOF]
     * @param signatures list of [CompositeSignature]
     */
    private fun Uri.Builder.appendCompositeSignatures(
        key: String,
        signatures: List<CompositeSignature>?
    ) {
        signatures?.forEachIndexed { index, compositeSignature ->
            appendQueryParameter(
                "${key}[$index][${Const.KEY_ADDRESS}]",
                compositeSignature.address
            )
            appendQueryParameter(
                "${key}[$index][${Const.KEY_KEY_ID}]",
                compositeSignature.keyId
            )
            appendQueryParameter(
                "${key}[$index][${Const.KEY_SIGNATURE}]",
                compositeSignature.signature
            )
        }
    }
}
