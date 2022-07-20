package com.portto.sdk.wallet.flow

data class CompositeSignature(
    val address: String,
    val keyId: String,
    val signature: String,
)
