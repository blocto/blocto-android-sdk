package com.portto.sdk.core.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SignMessageResponse(
    @SerialName("signatureId")
    val signatureId: String
)
