package com.portto.sdk.wallet

import android.net.Uri
import org.json.JSONObject

object RequestUriParser {

    @JvmStatic
    fun parse(uri: Uri): ParseResult {
        val appId = uri.getQueryParameter(Const.KEY_APP_ID)
        val method = uri.getQueryParameter(Const.KEY_METHOD)
        val requestId = uri.getQueryParameter(Const.KEY_REQUEST_ID)
        val blockchain = uri.getQueryParameter(Const.KEY_BLOCKCHAIN)

        if (appId == null || method == null || requestId == null || blockchain == null) {
            return ParseResult.Error
        }

        when (method) {
            "request_account" -> return ParseResult.RequestAccount(
                appId = appId,
                requestId = requestId,
                blockchain = blockchain
            )
            "sign_and_send_transaction" -> {
                val fromAddress = uri.getQueryParameter(Const.KEY_FROM)
                val message = uri.getQueryParameter(Const.KEY_MESSAGE)
                if (fromAddress == null || message == null) {
                    return ParseResult.Error
                }

                val isInvokeWrapped = uri.getQueryParameter(Const.KEY_IS_INVOKE_WRAPPED)?.toBoolean() ?: false

                val publicKeySignaturePairs = uri.queryParameterNames
                    .filter { it.startsWith(Const.KEY_PUBLIC_KEY_SIGNATURE_PAIRS) }
                    .associate {
                        val publicKey = it.substringAfter("[").substringBefore("]")
                        publicKey to (uri.getQueryParameter(it) ?: "")
                    }

                val appendTx = uri.queryParameterNames
                    .filter { it.startsWith(Const.KEY_APPEND_TX) }
                    .associate {
                        val hash = it.substringAfter("[").substringBefore("]")
                        hash to (uri.getQueryParameter(it) ?: "")
                    }

                return ParseResult.SignAndSendTransaction(
                    appId = appId,
                    requestId = requestId,
                    blockchain = blockchain,
                    fromAddress = fromAddress,
                    message = message,
                    isInvokeWrapped = isInvokeWrapped,
                    publicKeySignaturePairs = JSONObject(publicKeySignaturePairs).toString(),
                    appendTx = appendTx
                )
            }
            else -> return ParseResult.Error
        }
    }
}
