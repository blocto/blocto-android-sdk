package com.portto.sdk.solana.method

import android.net.Uri
import androidx.annotation.WorkerThread
import com.portto.sdk.core.Blockchain
import com.portto.sdk.core.BloctoSDK
import com.portto.sdk.core.method.Method
import com.portto.sdk.core.model.SendTransactionResponse
import com.portto.sdk.core.post
import com.portto.sdk.solana.model.SendTransactionRequest
import com.portto.sdk.wallet.BloctoSDKError
import com.portto.sdk.wallet.Const

class SignAndSendTransactionMethod(
    val fromAddress: String,
    val message: String,
    val isInvokeWrapped: Boolean,
    val publicKeySignaturePairs: Map<String, String>? = null,
    val appendTx: Map<String, String>? = null,
    private val blockchain: Blockchain,
    onSuccess: (String) -> Unit,
    onError: (BloctoSDKError) -> Unit
) : Method<String>(blockchain, onSuccess, onError) {

    override val name: String
        get() = "sign_and_send_transaction"

    override fun handleCallback(uri: Uri) {
        val txHash = uri.getQueryParameter(Const.KEY_TX_HASH)
        if (txHash.isNullOrEmpty()) {
            onError(BloctoSDKError.INVALID_RESPONSE)
            return
        }
        onSuccess(txHash)
    }

    override fun encodeToUri(authority: String, appId: String, requestId: String): Uri.Builder {
        return super.encodeToUri(authority, appId, requestId)
            .appendQueryParameter(Const.KEY_FROM, fromAddress)
            .appendQueryParameter(Const.KEY_MESSAGE, message)
            .appendQueryParameter(Const.KEY_IS_INVOKE_WRAPPED, isInvokeWrapped.toString())
            .apply {
                publicKeySignaturePairs?.entries?.forEach { (publicKey, signature) ->
                    appendQueryParameter(
                        "${Const.KEY_PUBLIC_KEY_SIGNATURE_PAIRS}[$publicKey]",
                        signature
                    )
                }
                appendTx?.entries?.forEach { (hash, message) ->
                    appendQueryParameter(
                        "${Const.KEY_APPEND_TX}[$hash]",
                        message
                    )
                }
            }
    }

    @WorkerThread
    override fun encodeToWebUri(
        authority: String,
        appId: String,
        requestId: String,
        webSessionId: String?
    ): Uri.Builder {
        val sessionId = webSessionId ?: kotlin.run {
            throw Throwable(BloctoSDKError.SESSION_ID_REQUIRED.message)
        }

        val requestBody = SendTransactionRequest(
            from = fromAddress,
            message = message,
            isInvokeWrapped = isInvokeWrapped,
            publicKeySignaturePairs = publicKeySignaturePairs,
            appendTx = appendTx
        )

        val headers = mapOf(
            Const.HEADER_SESSION_ID to sessionId,
            Const.HEADER_REQUEST_ID to requestId,
            Const.HEADER_REQUEST_SOURCE to Const.SDK_RESOURCE
        )

        val url = "${Const.webApiUrl(BloctoSDK.env)}/${blockchain.value}/${Const.PATH_DAPP}/${Const.PATH_AUTHZ}"
        val response: SendTransactionResponse = post(url, requestBody, headers)
        return super.encodeToWebUri(authority, appId, requestId, webSessionId)
            .appendPath(Const.PATH_AUTHZ)
            .appendPath(response.authorizationId)
    }
}
