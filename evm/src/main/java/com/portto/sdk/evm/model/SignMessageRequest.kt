package com.portto.sdk.evm.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SignMessageRequest(
    @SerialName("from")
    val from: String,
    @SerialName("message")
    val message: String,
    @SerialName("method")
    val method: String
)
