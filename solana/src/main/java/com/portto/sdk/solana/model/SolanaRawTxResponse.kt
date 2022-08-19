package com.portto.sdk.solana.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SolanaRawTxResponse(
    @SerialName("raw_tx")
    val rawTx: String,
    @SerialName("extra_data")
    val extraData: ExtraData
) {

    @Serializable
    data class ExtraData(
        @SerialName("append_tx")
        val appendTx: Map<String, String> = emptyMap()
    )
}