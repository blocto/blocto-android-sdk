package com.portto.sdk.core

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.annotation.VisibleForTesting
import com.portto.sdk.core.method.Method
import com.portto.sdk.wallet.BloctoSDKError
import com.portto.sdk.wallet.Const
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
            throw NullPointerException("App ID is required to use Blocto SDK. Check https://docs.blocto.app/blocto-sdk/register-app-id for more info.")
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
            val url = method.encodeToUri(
                authority = Const.webSDKUrl(debug),
                appId = appId,
                requestId = requestId
            ).build().toString()
            context.startActivity(WebSDKActivity.newIntent(context, requestId, url))
        }
    }

    @JvmStatic
    fun handleCallback(uri: Uri?) {
        if (uri == null) return
        val requestId = uri.getQueryParameter(Const.KEY_REQUEST_ID)
        val method = requestMap[requestId] ?: return
        requestMap.clear()
        if (handleError(method, uri)) return
        method.handleCallback(uri)
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
