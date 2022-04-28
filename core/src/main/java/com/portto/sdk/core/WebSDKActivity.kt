package com.portto.sdk.core

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.core.view.isGone
import com.portto.sdk.core.databinding.ActivityWebSdkBinding
import com.portto.sdk.wallet.Const

class WebSDKActivity : AppCompatActivity() {

    companion object {
        private const val KEY_URL = "url"

        fun newIntent(
            context: Context,
            url: String
        ) = Intent(context, WebSDKActivity::class.java).putExtras(
            bundleOf(KEY_URL to url)
        )
    }

    private lateinit var binding: ActivityWebSdkBinding

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWebSdkBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.toolbar.setNavigationOnClickListener {
            onBackPressed()
        }

        val url = intent.getStringExtra(KEY_URL) ?: return
        with(binding.webView) {
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
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
            }
            loadUrl(url)
        }
    }
}
