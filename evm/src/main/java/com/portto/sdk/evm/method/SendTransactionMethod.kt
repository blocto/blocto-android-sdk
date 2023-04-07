package com.portto.sdk.evm.method

import android.net.Uri
import androidx.annotation.WorkerThread
import com.portto.sdk.core.Blockchain
import com.portto.sdk.core.BloctoSDK
import com.portto.sdk.core.method.Method
import com.portto.sdk.core.model.SendTransactionResponse
import com.portto.sdk.core.post
import com.portto.sdk.evm.model.SendTransactionRequest
import com.portto.sdk.wallet.BloctoSDKError
import com.portto.sdk.wallet.Const
import java.math.BigInteger

class SendTransactionMethod(
    private val fromAddress: String,
    private val toAddress: String,
    private val data: String,
    private val value: BigInteger = BigInteger.ZERO,
    private val blockchain: Blockchain,
    onSuccess: (String) -> Unit,
    onError: (BloctoSDKError) -> Unit
) : Method<String>(blockchain, onSuccess, onError) {

    override val name: String
        get() = "send_transaction"

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
            .appendQueryParameter(Const.KEY_TO, toAddress)
            .appendQueryParameter(Const.KEY_DATA, data)
            .appendQueryParameter(Const.KEY_VALUE, String.format("%#x", value))
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
            to = toAddress,
            data = data,
            value = String.format("%#x", value)
        )

        val headers = mapOf(
            Const.HEADER_SESSION_ID to sessionId,
            Const.HEADER_REQUEST_ID to requestId,
            Const.HEADER_REQUEST_SOURCE to Const.SDK_RESOURCE
        )

        val url = "${Const.webApiUrl(BloctoSDK.env)}/${blockchain.value}/${Const.PATH_AUTHZ_DAPP}"
        val response: SendTransactionResponse = post(url, listOf(requestBody), headers)
        return super.encodeToWebUri(authority, appId, requestId, webSessionId)
            .appendPath(Const.PATH_AUTHZ)
            .appendPath(response.authorizationId)
    }
}
