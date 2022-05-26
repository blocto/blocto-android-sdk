package com.portto.sdk.wallet

sealed class Callback(open val requestId: String) {

    data class RequestAccount(
        override val requestId: String,
        val address: String
    ) : Callback(requestId)

    data class SignAndSendTransaction(
        override val requestId: String,
        val txHash: String
    ) : Callback(requestId)

    data class SendTransaction(
        override val requestId: String,
        val txHash: String
    ) : Callback(requestId)

    data class SignMessage(
        override val requestId: String,
        val signature: String
    ) : Callback(requestId)

    data class Error(
        override val requestId: String,
        val error: String
    ) : Callback(requestId)
}
