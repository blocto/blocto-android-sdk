package com.portto.sdk.solana

import androidx.annotation.WorkerThread
import com.portto.sdk.core.BloctoSDK
import com.portto.sdk.core.post
import com.portto.sdk.solana.model.SolanaRawTxRequest
import com.portto.sdk.solana.model.SolanaRawTxResponse
import com.portto.sdk.wallet.Const

object SolanaService {

    @WorkerThread
    fun createRawTransaction(requestBody: SolanaRawTxRequest): SolanaRawTxResponse {
        val url = "${Const.bloctoApiUrl(BloctoSDK.env)}/solana/createRawTransaction"
        return post(url, requestBody)
    }
}
