package com.portto.sdk.core

import android.content.Context
import android.content.Intent
import android.net.Uri
import java.util.*

object BloctoSDK {

    private var appId: String? = null
    private val requestMap = mutableMapOf<String, Method<*>>()

    fun init(appId: String) {
        this.appId = appId
    }

    fun send(context: Context, method: Method<*>) {
        if (appId == null) throw Throwable("Need to set app id before use Blocto SDK")
        val requestId = UUID.randomUUID().toString()
        requestMap[requestId] = method
        val uri = method.encodeToUri()
            .appendQueryParameter(Const.KEY_APP_ID, appId)
            .appendQueryParameter(Const.KEY_REQUEST_ID, requestId)
            .build()
        val intent = Intent(Intent.ACTION_VIEW, uri).apply {
            setPackage(Const.BLOCTO_PACKAGE)
        }
        context.startActivity(intent)
    }

    fun handleCallback(uri: Uri?) {
        if (uri == null) return
        val requestId = uri.getQueryParameter(Const.KEY_REQUEST_ID)
        val method = requestMap[requestId] ?: return
        requestMap.clear()
        if (handleError(method, uri)) return
        when (method) {
            is RequestAccountMethod -> handleRequestAccount(method, uri)
        }
    }

    private fun handleRequestAccount(method: RequestAccountMethod, uri: Uri) {
        val address = uri.getQueryParameter(Const.KEY_ADDRESS)
        if (address.isNullOrEmpty()) {
            method.onError(BloctoSDKError.INVALID_RESPONSE)
            return
        }
        method.onSuccess(address)
    }

    private fun handleError(method: Method<*>, uri: Uri): Boolean {
        val message = uri.getQueryParameter(Const.KEY_ERROR) ?: return false
        val error = BloctoSDKError.values().find { it.message == message }
            ?: BloctoSDKError.UNEXPECTED_ERROR
        method.onError(error)
        return true
    }
}
