package com.portto.sdk.solana.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SendTransactionRequest(
    @SerialName("from")
    val from: String,
    @SerialName("message")
    val message: String,
    @SerialName("isInvokeWrapped")
    val isInvokeWrapped: Boolean,
    @SerialName("publicKeySignaturePairs")
    val publicKeySignaturePairs: Map<String, String>?,
    @SerialName("appendTx")
    val appendTx: Map<String, String>?
)
