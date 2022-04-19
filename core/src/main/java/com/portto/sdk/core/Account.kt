package com.portto.sdk.core

import android.content.Context

interface Account {
    fun requestAccount(
        context: Context,
        onSuccess: (String) -> Unit,
        onError: (BloctoSDKError) -> Unit
    )
}
