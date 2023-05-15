package com.portto.sdk.flow

import androidx.annotation.WorkerThread
import com.portto.sdk.core.BloctoSDK
import com.portto.sdk.core.get
import com.portto.sdk.flow.model.FeePayerResponse
import com.portto.sdk.wallet.Const

object FlowService {
    @WorkerThread
    fun getFeePayer(): FeePayerResponse = get(
        url = "${Const.bloctoApiUrl(BloctoSDK.env)}/flow/feePayer"
    )
}
