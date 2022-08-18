package com.portto.sdk.core.network.service

import androidx.annotation.WorkerThread
import com.portto.sdk.core.network.BloctoApi
import kotlinx.serialization.Serializable


object FlowService {
    @WorkerThread
    fun getFeePayer(): FeePayerResponse = BloctoApi.get("flow/feePayer")

    @Serializable
    data class FeePayerResponse(val address: String)
}