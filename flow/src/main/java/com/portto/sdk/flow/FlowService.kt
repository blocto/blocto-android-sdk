package com.portto.sdk.flow

import androidx.annotation.WorkerThread
import com.portto.sdk.core.get
import com.portto.sdk.flow.model.FeePayerResponse


object FlowService {
    @WorkerThread
    fun getFeePayer(): FeePayerResponse = get("flow/feePayer")
}