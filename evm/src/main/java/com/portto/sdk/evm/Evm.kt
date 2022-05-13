package com.portto.sdk.evm

import android.content.Context
import com.portto.sdk.core.BloctoSDK
import com.portto.sdk.core.Chain
import com.portto.sdk.evm.method.SendTransactionMethod
import com.portto.sdk.wallet.BloctoSDKError
import java.math.BigInteger

abstract class Evm : Chain {

    fun sendTransaction(
        context: Context,
        fromAddress: String,
        toAddress: String,
        data: String,
        value: BigInteger = BigInteger.ZERO,
        onSuccess: (String) -> Unit,
        onError: (BloctoSDKError) -> Unit
    ) {
        val method = SendTransactionMethod(
            fromAddress,
            toAddress,
            data,
            value,
            blockchain,
            onSuccess,
            onError
        )
        BloctoSDK.send(context, method)
    }
}
