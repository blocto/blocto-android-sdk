package com.portto.sdk.core

import android.content.Context

interface Account {

    val blockchain: Blockchain

    fun requestAccount(
        context: Context,
        onSuccess: (String) -> Unit,
        onError: (BloctoSDKError) -> Unit
    )
}
