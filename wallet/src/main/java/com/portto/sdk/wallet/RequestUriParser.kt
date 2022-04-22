package com.portto.sdk.wallet

import android.net.Uri
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

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
                val publicKeySignaturePairs = uri.getQueryParameter(Const.KEY_PUBLIC_KEY_SIGNATURE_PAIRS)
                val appendTx = uri.getQueryParameter(Const.KEY_APPEND_TX)?.let { appendTx ->
                    Json.parseToJsonElement(appendTx).jsonObject.mapValues {
                        it.value.jsonPrimitive.content
                    }
                }
                return ParseResult.SignAndSendTransaction(
                    appId = appId,
                    requestId = requestId,
                    blockchain = blockchain,
                    fromAddress = fromAddress,
                    message = message,
                    isInvokeWrapped = isInvokeWrapped,
                    publicKeySignaturePairs = publicKeySignaturePairs,
                    appendTx = appendTx
                )
            }
            else -> return ParseResult.Error
        }
    }
}
