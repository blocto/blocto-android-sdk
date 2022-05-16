package com.portto.sdk.evm

import android.content.Context
import com.portto.sdk.core.Account
import com.portto.sdk.core.Blockchain
import com.portto.sdk.core.BloctoSDK
import com.portto.sdk.core.method.RequestAccountMethod
import com.portto.sdk.wallet.BloctoSDKError

val BloctoSDK.polygon by lazy { Polygon() }

class Polygon : Evm(), Account {

    override val blockchain: Blockchain
        get() = Blockchain.POLYGON

    override fun requestAccount(
        context: Context,
        onSuccess: (String) -> Unit,
        onError: (BloctoSDKError) -> Unit
    ) {
        val method = RequestAccountMethod(
            blockchain = blockchain,
            onSuccess = onSuccess,
            onError = onError
        )
        BloctoSDK.send(context, method)
    }
}
