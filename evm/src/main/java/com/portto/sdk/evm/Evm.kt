package com.portto.sdk.evm

import android.content.Context
import com.portto.sdk.core.Blockchain
import com.portto.sdk.core.BloctoSDK
import com.portto.sdk.core.isValidHex
import com.portto.sdk.core.method.RequestAccountMethod
import com.portto.sdk.evm.method.SendTransactionMethod
import com.portto.sdk.evm.method.SignMessageMethod
import com.portto.sdk.wallet.BloctoSDKError
import com.portto.sdk.wallet.evm.EvmSignType
import java.math.BigInteger

val BloctoSDK.evm by lazy { Evm() }

class Evm {

    fun requestAccount(
        context: Context,
        blockchain: Blockchain,
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

    fun signMessage(
        context: Context,
        blockchain: Blockchain,
        fromAddress: String,
        signType: EvmSignType,
        message: String,
        onSuccess: (String) -> Unit,
        onError: (BloctoSDKError) -> Unit
    ) {
        if (signType == EvmSignType.ETH_SIGN && !message.isValidHex(need0xPrefix = true)) {
            onError(BloctoSDKError.ETH_SIGN_INVALID_HEX_STRING)
            return
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
        blockchain: Blockchain,
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
