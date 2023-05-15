package com.portto.sdk.core

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.annotation.VisibleForTesting
import com.portto.sdk.core.method.Method
import com.portto.sdk.core.method.RequestAccountMethod
import com.portto.sdk.wallet.BloctoEnv
import com.portto.sdk.wallet.BloctoSDKError
import com.portto.sdk.wallet.Const
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*

object BloctoSDK {

    private var appId: String? = null
    private var webSessionId: String? = null
    private val requestMap = mutableMapOf<String, Method<*>>()

    @JvmStatic
    var env: BloctoEnv = BloctoEnv.PROD
        private set

    @JvmStatic
    fun init(appId: String, env: BloctoEnv = BloctoEnv.PROD) {
        this.appId = appId
        this.env = env
    }

    @JvmStatic
    fun send(context: Context, method: Method<*>) {
        val appId = this.appId.takeIf { !it.isNullOrEmpty() } ?: kotlin.run {
            throw NullPointerException("App ID is required to use Blocto SDK. Check https://docs.blocto.app/blocto-sdk/register-app-id for more info.")
        }
        val requestId = UUID.randomUUID().toString()
        requestMap[requestId] = method
        val uri = method.encodeToUri(
            authority = Const.bloctoAuthority(env),
            appId = appId,
            requestId = requestId
        ).build()
        val intent = Intent(Intent.ACTION_VIEW, uri).apply {
            setPackage(Const.bloctoPackage(env))
        }
        try {
            context.startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            if (method.blockchain == Blockchain.FLOW)
                Log.w("BloctoSDK", "Flow does not support web fallback")
            else
                launchWebSDK(context, method, appId, requestId)
        }
    }

    private fun launchWebSDK(
        context: Context,
        method: Method<*>,
        appId: String,
        requestId: String
    ) {
        val exceptionHandler = CoroutineExceptionHandler { _, error ->
            val err = BloctoSDKError.values().find { it.message == error.message }
                ?: BloctoSDKError.UNEXPECTED_ERROR
            method.onError(err)
        }

        MainScope().launch(exceptionHandler) {
            val url = withContext(Dispatchers.IO) {
                method.encodeToWebUri(
                    authority = Const.webSDKUrl(env),
                    appId = appId,
                    requestId = requestId,
                    webSessionId = webSessionId
                ).build().toString()
            }
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
        if (method is RequestAccountMethod) {
            webSessionId = uri.getQueryParameter(Const.KEY_SESSION_ID)
        }
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
        webSessionId = null
        env = BloctoEnv.PROD
    }
}
