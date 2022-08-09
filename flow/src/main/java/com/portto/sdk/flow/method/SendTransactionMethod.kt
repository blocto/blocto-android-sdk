package com.portto.sdk.flow.method

import android.net.Uri
import com.portto.sdk.core.Blockchain
import com.portto.sdk.core.method.Method
import com.portto.sdk.wallet.BloctoSDKError
import com.portto.sdk.wallet.Const
import com.portto.sdk.wallet.METHOD_FLOW_SEND_TRANSACTION

class SendTransactionMethod(
    private val fromAddress: String,
    private val encodedTxData: String,
    blockchain: Blockchain = Blockchain.FLOW,
    onSuccess: (String) -> Unit,
    onError: (BloctoSDKError) -> Unit
) : Method<String>(blockchain, onSuccess, onError) {

    override val name: String
        get() = METHOD_FLOW_SEND_TRANSACTION

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
            .appendQueryParameter(Const.KEY_FLOW_TX, encodedTxData)
    }
}
