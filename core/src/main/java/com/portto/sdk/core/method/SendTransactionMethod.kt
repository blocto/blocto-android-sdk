package com.portto.sdk.core.method

import android.net.Uri
import com.portto.sdk.core.Blockchain
import com.portto.sdk.wallet.BloctoSDKError
import com.portto.sdk.wallet.Const
import java.math.BigInteger

class SendTransactionMethod(
    val fromAddress: String,
    val toAddress: String,
    val data: String,
    val value: BigInteger = BigInteger.ZERO,
    blockchain: Blockchain,
    onSuccess: (String) -> Unit,
    onError: (BloctoSDKError) -> Unit
) : Method<String>(blockchain, onSuccess, onError) {

    override val name: String
        get() = "send_transaction"

    override fun encodeToUri(authority: String, appId: String, requestId: String): Uri.Builder {
        return super.encodeToUri(authority, appId, requestId)
            .appendQueryParameter(Const.KEY_FROM, fromAddress)
            .appendQueryParameter(Const.KEY_TO, toAddress)
            .appendQueryParameter(Const.KEY_DATA, data)
            .appendQueryParameter(Const.KEY_VALUE, value.toString(16))

    }
}
