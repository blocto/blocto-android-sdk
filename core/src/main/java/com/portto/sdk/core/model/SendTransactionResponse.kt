package com.portto.sdk.core.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SendTransactionResponse(
    @SerialName("authorizationId")
    val authorizationId: String
)
