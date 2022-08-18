package com.portto.sdk.solana

import androidx.annotation.WorkerThread
import com.portto.sdk.core.network.post
import com.portto.sdk.solana.model.SolanaRawTxRequest
import com.portto.sdk.solana.model.SolanaRawTxResponse

object SolanaService {

    @WorkerThread
    fun createRawTransaction(requestBody: SolanaRawTxRequest): SolanaRawTxResponse {
        return post("/solana/createRawTransaction", requestBody)
    }
}