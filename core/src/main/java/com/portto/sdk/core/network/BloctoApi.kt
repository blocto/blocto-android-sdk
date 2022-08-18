package com.portto.sdk.core.network

import androidx.annotation.WorkerThread
import androidx.viewbinding.BuildConfig
import com.portto.sdk.core.BloctoSDK
import com.portto.sdk.core.BuildConfig.VERSION_NAME
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.logging.HttpLoggingInterceptor
import java.util.concurrent.TimeUnit

@WorkerThread
internal object BloctoApi {

    private val jsonType = "application/json; charset=utf-8".toMediaType()

    private val baseUrl
        get() = if (BloctoSDK.debug) "https://api-staging.blocto.app/"
        else "https://api.blocto.app"

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = if (BuildConfig.DEBUG) {
            HttpLoggingInterceptor.Level.BODY
        } else {
            HttpLoggingInterceptor.Level.NONE
        }
    }

    private val headerInterceptor = Interceptor { chain ->
        val request = chain.request()
        val builder = request.newBuilder().apply {
            addHeader("blocto-sdk-platform", "Android")
            addHeader("blocto-sdk-version", VERSION_NAME)
        }.build()
        chain.proceed(builder)
    }

    private val json = Json {
        ignoreUnknownKeys = true
    }

    private val client = OkHttpClient.Builder()
        .connectTimeout(10L, TimeUnit.SECONDS)
        .readTimeout(10L, TimeUnit.SECONDS)
        .addInterceptor(headerInterceptor)
        .addInterceptor(loggingInterceptor)
        .build()


    inline fun <reified T> get(path: String): T {
        val request = Request.Builder()
            .url("$baseUrl/$path")
            .get()
            .build()

        client.newCall(request).execute().use {
            if (it.isSuccessful) {
                return json.decodeFromString(it.body?.string().orEmpty())
            } else {
                throw Exception("code: ${it.code}, message=${it.body?.string()}")
            }
        }
    }

    inline fun <reified T, reified U> post(path: String, requestBody: U): T {
        val body = json.encodeToString(requestBody).toRequestBody(jsonType)

        val request = Request.Builder()
            .url("$baseUrl/$path")
            .post(body)
            .build()

        client.newCall(request).execute().use {
            if (it.isSuccessful) {
                return json.decodeFromString(it.body?.string().orEmpty())
            } else {
                throw Exception("code: ${it.code}, message=${it.body?.string()}")
            }
        }
    }
}