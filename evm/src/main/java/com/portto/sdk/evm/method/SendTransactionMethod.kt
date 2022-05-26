package com.portto.sdk.evm.method

import android.net.Uri
import com.portto.sdk.core.Blockchain
import com.portto.sdk.core.method.Method
import com.portto.sdk.wallet.BloctoSDKError
import com.portto.sdk.wallet.Const
import java.math.BigInteger

class SendTransactionMethod(
    private val fromAddress: String,
    private val toAddress: String,
    private val data: String,
    private val value: BigInteger = BigInteger.ZERO,
    blockchain: Blockchain,
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
}
