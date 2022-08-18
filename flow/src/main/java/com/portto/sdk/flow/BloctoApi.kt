package com.portto.sdk.flow

import androidx.annotation.WorkerThread
import androidx.viewbinding.BuildConfig
import com.portto.sdk.core.BloctoSDK
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import okhttp3.*
import okhttp3.logging.HttpLoggingInterceptor
import java.util.concurrent.TimeUnit

@WorkerThread
internal object BloctoApi {
    private val baseUrl
        get() = if (BloctoSDK.debug) "https://dev-api.blocto.app" else "https://api.blocto.app"

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = if (BuildConfig.DEBUG) {
            HttpLoggingInterceptor.Level.BODY
        } else {
            HttpLoggingInterceptor.Level.NONE
        }
    }

    private val client = OkHttpClient.Builder()
        .connectTimeout(10L, TimeUnit.SECONDS)
        .readTimeout(10L, TimeUnit.SECONDS)
        .addInterceptor(loggingInterceptor)
        .build()

    private val json = Json {
        ignoreUnknownKeys = true
    }

    private fun buildRequest(path: String) = Request.Builder()
        .url("$baseUrl/$path")
        .build()

    fun getFeePayer(): String {
        val request = buildRequest("flow/feePayer")

        client.newCall(request).execute().use {
            if (it.isSuccessful) {
                return json.decodeFromString<FeePayerResponse>(it.body?.string().orEmpty()).address
            } else {
                throw Exception("code: ${it.code}, message=${it.body?.string()}")
            }
        }
    }

    @Serializable
    data class FeePayerResponse(val address: String)
}
