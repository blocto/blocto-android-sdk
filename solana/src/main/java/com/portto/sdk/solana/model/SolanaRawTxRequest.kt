package com.portto.sdk.solana.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SolanaRawTxRequest(
    @SerialName("sol_address")
    val address: String,
    @SerialName("raw_tx")
    val rawTx: String
)