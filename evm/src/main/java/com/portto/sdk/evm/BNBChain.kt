package com.portto.sdk.evm

import android.content.Context
import com.portto.sdk.core.Account
import com.portto.sdk.core.Blockchain
import com.portto.sdk.core.BloctoSDK
import com.portto.sdk.core.Chain
import com.portto.sdk.core.method.RequestAccountMethod
import com.portto.sdk.wallet.BloctoSDKError

val BloctoSDK.bnb by lazy { BNBChain() }

class BNBChain : Chain, Account {

    override val blockchain: Blockchain
        get() = Blockchain.BNB_CHAIN

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
