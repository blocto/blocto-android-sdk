package com.portto.sdk.evm.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
class SendTransactionRequest(
    @SerialName("from")
    val from: String,
    @SerialName("to")
    val to: String,
    @SerialName("data")
    val data: String,
    @SerialName("value")
    val value: String
)
