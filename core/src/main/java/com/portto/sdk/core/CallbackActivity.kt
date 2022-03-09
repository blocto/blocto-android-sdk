package com.portto.sdk.core

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class CallbackActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        BloctoSDK.handleCallback(intent?.data)
        finish()
    }
}
