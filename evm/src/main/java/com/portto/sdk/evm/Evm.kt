package com.portto.sdk.evm

import android.content.Context
import com.portto.sdk.core.BloctoSDK
import com.portto.sdk.core.Chain
import com.portto.sdk.core.isValidHex
import com.portto.sdk.evm.method.SendTransactionMethod
import com.portto.sdk.evm.method.SignMessageMethod
import com.portto.sdk.wallet.BloctoSDKError
import com.portto.sdk.wallet.EvmSignType
import java.math.BigInteger

abstract class Evm : Chain {

    fun signMessage(
        context: Context,
        fromAddress: String,
        signType: EvmSignType,
        message: String,
        onSuccess: (String) -> Unit,
        onError: (BloctoSDKError) -> Unit
    ) {
        if (signType == EvmSignType.ETH_SIGN && !message.isValidHex(need0xPrefix = true)) {
            throw IllegalArgumentException(BloctoSDKError.ETH_SIGN_INVALID_HEX_STRING.message)
        }
        val method = SignMessageMethod(
            fromAddress,
            signType,
            message,
            blockchain,
            onSuccess,
            onError
        )
        BloctoSDK.send(context, method)
    }

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
