package com.portto.sdk.wallet

sealed class ParseResult {

    data class RequestAccount(
        val appId: String,
        val requestId: String,
        val blockchain: String
    ) : ParseResult()

    data class SignAndSendTransaction(
        val appId: String,
        val requestId: String,
        val blockchain: String,
        val fromAddress: String,
        val message: String,
        val isInvokeWrapped: Boolean,
        val publicKeySignaturePairs: String? = null,
        val appendTx: Map<String, String>? = null
    ) : ParseResult()

    data class SendTransaction(
        val appId: String,
        val requestId: String,
        val blockchain: String,
        val fromAddress: String,
        val toAddress: String,
        val data: String,
        val value: String
    ) : ParseResult()

    data class SignMessage(
        val appId: String,
        val requestId: String,
        val blockchain: String,
        val fromAddress: String,
        val signType: String,
        val message: String
    ) : ParseResult()

    /**
     * Flow authn
     * @since 0.3.0
     */
    data class Authentication(
        val appId: String,
        val requestId: String,
        val blockchain: String,
        val flowAppId: String?,
        val flowNonce: String?,
    ) : ParseResult()

    /**
     * Flow user_signature
     * @since 0.3.0
     */
    data class UserSignatures(
        val appId: String,
        val requestId: String,
        val blockchain: String,
        val fromAddress: String,
        val message: String,
    ) : ParseResult()

    object Error : ParseResult()
}
