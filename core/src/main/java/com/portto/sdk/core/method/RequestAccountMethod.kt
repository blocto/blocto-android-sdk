package com.portto.sdk.core.method

import com.portto.sdk.core.Blockchain
import com.portto.sdk.core.BloctoSDKError

class RequestAccountMethod(
    blockchain: Blockchain,
    onSuccess: (String) -> Unit,
    onError: (BloctoSDKError) -> Unit
) : Method<String>(blockchain, onSuccess, onError) {

    override val name: String
        get() = "request_account"
}
