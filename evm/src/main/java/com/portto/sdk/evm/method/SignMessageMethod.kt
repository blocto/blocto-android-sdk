package com.portto.sdk.evm.method

import android.net.Uri
import androidx.annotation.WorkerThread
import com.portto.sdk.core.Blockchain
import com.portto.sdk.core.BloctoSDK
import com.portto.sdk.core.method.Method
import com.portto.sdk.core.model.SignMessageResponse
import com.portto.sdk.core.post
import com.portto.sdk.evm.model.SignMessageRequest
import com.portto.sdk.wallet.BloctoSDKError
import com.portto.sdk.wallet.Const
import com.portto.sdk.wallet.evm.EvmSignType

class SignMessageMethod(
    private val fromAddress: String,
    private val signType: EvmSignType,
    private val message: String,
    private val blockchain: Blockchain,
    onSuccess: (String) -> Unit,
    onError: (BloctoSDKError) -> Unit
) : Method<String>(blockchain, onSuccess, onError) {

    override val name: String
        get() = "sign_message"

    override fun handleCallback(uri: Uri) {
        val signature = uri.getQueryParameter(Const.KEY_SIGNATURE)
        if (signature.isNullOrEmpty()) {
            onError(BloctoSDKError.INVALID_RESPONSE)
            return
        }
        onSuccess(signature)
    }

    override fun encodeToUri(authority: String, appId: String, requestId: String): Uri.Builder {
        return super.encodeToUri(authority, appId, requestId)
            .appendQueryParameter(Const.KEY_FROM, fromAddress)
            .appendQueryParameter(Const.KEY_TYPE, signType.type)
            .appendQueryParameter(Const.KEY_MESSAGE, message)
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

        val requestBody = SignMessageRequest(
            from = fromAddress,
            message = message,
            method = signType.type
        )

        val headers = mapOf(
            Const.HEADER_SESSION_ID to sessionId,
            Const.HEADER_REQUEST_ID to requestId,
            Const.HEADER_REQUEST_SOURCE to Const.SDK_RESOURCE
        )

        val url = "${Const.webApiUrl(BloctoSDK.env)}/${blockchain.value}/${Const.PATH_DAPP}/${Const.PATH_USER_SIGNATURE}"
        val response: SignMessageResponse = post(url, requestBody, headers)
        return super.encodeToWebUri(authority, appId, requestId, webSessionId)
            .appendPath(Const.PATH_USER_SIGNATURE)
            .appendPath(response.signatureId)
    }
}
