package com.portto.sdk.core

import android.content.Context
import com.portto.sdk.wallet.BloctoSDKError

interface Account {
    fun requestAccount(
        context: Context,
        onSuccess: (String) -> Unit,
        onError: (BloctoSDKError) -> Unit
    )
}
