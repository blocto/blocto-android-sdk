package com.portto.sdk.core

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.annotation.VisibleForTesting
import com.portto.sdk.core.method.Method
import com.portto.sdk.core.method.RequestAccountMethod
import com.portto.sdk.core.method.SignAndSendTransactionMethod
import java.util.*

object BloctoSDK {

    private var appId: String? = null
    private val requestMap = mutableMapOf<String, Method<*>>()

    @JvmStatic
    var debug: Boolean = false
        private set

    @JvmStatic
    fun init(appId: String, debug: Boolean = false) {
        this.appId = appId
        this.debug = debug
    }

    @JvmStatic
    fun send(context: Context, method: Method<*>) {
        val appId = this.appId.takeIf { !it.isNullOrEmpty() } ?: kotlin.run {
            throw NullPointerException("Need to set app id before use Blocto SDK")
        }
        val requestId = UUID.randomUUID().toString()
        requestMap[requestId] = method
        val uri = method.encodeToUri(
            authority = Const.bloctoAuthority(debug),
            appId = appId,
            requestId = requestId
        ).build()
        val intent = Intent(Intent.ACTION_VIEW, uri).apply {
            setPackage(Const.bloctoPackage(debug))
        }
        try {
            context.startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            TODO("handle request using browser")
        }
    }

    @JvmStatic
    fun handleCallback(uri: Uri?) {
        if (uri == null) return
        val requestId = uri.getQueryParameter(Const.KEY_REQUEST_ID)
        val method = requestMap[requestId] ?: return
        requestMap.clear()
        if (handleError(method, uri)) return
        when (method) {
            is RequestAccountMethod -> handleRequestAccount(method, uri)
            is SignAndSendTransactionMethod -> handleSignAndSendTransaction(method, uri)
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

    private fun handleSignAndSendTransaction(method: SignAndSendTransactionMethod, uri: Uri) {
        val txHash = uri.getQueryParameter(Const.KEY_TX_HASH)
        if (txHash.isNullOrEmpty()) {
            method.onError(BloctoSDKError.INVALID_RESPONSE)
            return
        }
        method.onSuccess(txHash)
    }

    private fun handleError(method: Method<*>, uri: Uri): Boolean {
        val message = uri.getQueryParameter(Const.KEY_ERROR) ?: return false
        val error = BloctoSDKError.values().find { it.message == message }
            ?: BloctoSDKError.UNEXPECTED_ERROR
        method.onError(error)
        return true
    }

    @VisibleForTesting
    fun resetForTesting() {
        appId = null
        debug = false
    }
}
