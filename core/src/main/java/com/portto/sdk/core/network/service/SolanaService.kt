package com.portto.sdk.core.network.service

import com.portto.sdk.core.network.BloctoApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

object SolanaService {

    fun createRawTransaction(requestBody: SolanaRawTxRequest): SolanaRawTxResponse {
        return BloctoApi.post("/solana/createRawTransaction", requestBody)
    }


    @Serializable
    data class SolanaRawTxRequest(
        @SerialName("sol_address")
        val address: String,
        @SerialName("raw_tx")
        val rawTx: String
    )

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
}