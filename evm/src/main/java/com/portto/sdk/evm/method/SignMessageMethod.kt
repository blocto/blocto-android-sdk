package com.portto.sdk.evm.method

import android.net.Uri
import com.portto.sdk.core.Blockchain
import com.portto.sdk.core.method.Method
import com.portto.sdk.wallet.BloctoSDKError
import com.portto.sdk.wallet.Const
import com.portto.sdk.wallet.EvmSignType

class SignMessageMethod(
    private val fromAddress: String,
    private val signType: EvmSignType,
    private val message: String,
    blockchain: Blockchain,
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
}
