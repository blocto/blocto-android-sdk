package com.portto.sdk.core.method

import android.net.Uri
import com.portto.sdk.core.Blockchain
import com.portto.sdk.core.BloctoSDKError
import com.portto.sdk.core.Const
import org.json.JSONObject

class SignAndSendTransactionMethod(
    private val fromAddress: String,
    private val message: String,
    private val isInvokeWrapped: Boolean,
    private val publicKeySignaturePairs: Map<String, String>? = null,
    private val appendTx: Map<String, String>? = null,
    blockchain: Blockchain,
    onSuccess: (String) -> Unit,
    onError: (BloctoSDKError) -> Unit
) : Method<String>(blockchain, onSuccess, onError) {

    override val name: String
        get() = "sign_and_send_transaction"

    override fun encodeToUri(authority: String, appId: String, requestId: String): Uri.Builder {
        return super.encodeToUri(authority, appId, requestId)
            .appendQueryParameter(Const.KEY_FROM, fromAddress)
            .appendQueryParameter(Const.KEY_MESSAGE, message)
            .appendQueryParameter(Const.KEY_IS_INVOKE_WRAPPED, isInvokeWrapped.toString())
            .apply {
                publicKeySignaturePairs?.let {
                    appendQueryParameter(
                        Const.KEY_PUBLIC_KEY_SIGNATURE_PAIRS,
                        JSONObject(it).toString()
                    )
                }
                appendTx?.let {
                    appendQueryParameter(
                        Const.KEY_APPEND_TX,
                        JSONObject(it).toString()
                    )
                }
            }
    }
}
