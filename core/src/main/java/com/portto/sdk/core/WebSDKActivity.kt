package com.portto.sdk.core

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Rect
import android.net.Uri
import android.os.Bundle
import android.os.Message
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.core.view.isGone
import com.portto.sdk.core.databinding.ActivityWebSdkBinding
import com.portto.sdk.wallet.BloctoSDKError
import com.portto.sdk.wallet.Const

@SuppressLint("SetJavaScriptEnabled")
class WebSDKActivity : AppCompatActivity() {

    companion object {
        private const val KEY_URL = "url"

        fun newIntent(
            context: Context,
            requestId: String,
            url: String
        ) = Intent(context, WebSDKActivity::class.java).putExtras(
            bundleOf(
                Const.KEY_REQUEST_ID to requestId,
                KEY_URL to url,
            )
        )
    }

    private lateinit var binding: ActivityWebSdkBinding
    private val requestId by lazy { intent.getStringExtra(Const.KEY_REQUEST_ID) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWebSdkBinding.inflate(layoutInflater)
        setContentView(binding.root)

        onBackPressedDispatcher.addCallback(this) {
            if (requestId != null) {
                val uri = Uri.Builder()
                    .scheme(Const.BLOCTO_SCHEME)
                    .appendQueryParameter(Const.KEY_REQUEST_ID, requestId)
                    .appendQueryParameter(Const.KEY_ERROR, BloctoSDKError.USER_REJECTED.message)
                    .build()
                BloctoSDK.handleCallback(uri)
            }
            finish()
        }

        binding.toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        val url = intent.getStringExtra(KEY_URL) ?: return

        with(binding.webView) {
            settings.setup()

            webViewClient = object : WebViewClient() {
                override fun shouldOverrideUrlLoading(
                    view: WebView?,
                    request: WebResourceRequest?
                ): Boolean {
                    val uri = request?.url ?: return false
                    return if (uri.scheme == Const.BLOCTO_SCHEME) {
                        BloctoSDK.handleCallback(uri)
                        finish()
                        true
                    } else {
                        false
                    }
                }

                override fun onPageFinished(view: WebView?, url: String?) {
                    binding.toolbar.title = view?.title
                }
            }

            webChromeClient = object : WebChromeClient() {
                override fun onProgressChanged(view: WebView?, newProgress: Int) {
                    super.onProgressChanged(view, newProgress)
                    binding.progressBar.progress = newProgress
                    binding.progressBar.isGone = newProgress == 100
                }

                override fun onCreateWindow(
                    view: WebView?,
                    isDialog: Boolean,
                    isUserGesture: Boolean,
                    resultMsg: Message?
                ): Boolean = onCreateWebWindow(resultMsg)
            }
            loadUrl(url)
        }
    }

    private fun onCreateWebWindow(resultMsg: Message?): Boolean {
        val webView = WebView(this@WebSDKActivity)
        webView.settings.setup()

        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                // Make WebView height match parent
                val displayRectangle = Rect()
                window.decorView.getWindowVisibleDisplayFrame(displayRectangle)
                view?.layoutParams = view?.layoutParams?.apply {
                    height = displayRectangle.height()
                }
            }
        }

        webView.webChromeClient = object : WebChromeClient() {
            override fun onCloseWindow(window: WebView?) {
                super.onCloseWindow(window)
                binding.webView.removeView(window)
            }
        }

        binding.webView.addView(webView)
        val transport = resultMsg?.obj as WebView.WebViewTransport
        transport.webView = webView
        resultMsg.sendToTarget()
        return true
    }

    private fun WebSettings.setup() {
        javaScriptEnabled = true
        domStorageEnabled = true
        setSupportMultipleWindows(true)
        // Remove "wv" from user agent to make Google login work
        userAgentString = userAgentString.replace(oldValue = "; wv", newValue = "")
    }
}
