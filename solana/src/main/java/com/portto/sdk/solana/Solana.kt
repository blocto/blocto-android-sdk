package com.portto.sdk.solana

import android.content.Context
import com.portto.sdk.core.*

val BloctoSDK.solana by lazy { Solana() }

class Solana : Account {

    override val blockchain: Blockchain
        get() = Blockchain.SOLANA

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
