package com.portto.sdk.core

import androidx.annotation.WorkerThread
import androidx.viewbinding.BuildConfig
import com.portto.sdk.core.BloctoApi.toErrorCode
import com.portto.sdk.core.BuildConfig.VERSION_NAME
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.logging.HttpLoggingInterceptor
import java.util.concurrent.TimeUnit

@PublishedApi
@WorkerThread
internal object BloctoApi {

    val jsonType = "application/json; charset=utf-8".toMediaType()

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

    val json = Json {
        ignoreUnknownKeys = true
    }

    val client = OkHttpClient.Builder()
        .connectTimeout(10L, TimeUnit.SECONDS)
        .readTimeout(10L, TimeUnit.SECONDS)
        .addInterceptor(headerInterceptor)
        .addInterceptor(loggingInterceptor)
        .build()

    fun String.toErrorCode() = json.parseToJsonElement(this)
        .jsonObject["error_code"]
        ?.jsonPrimitive
        ?.content
        ?: this
}

inline fun <reified T> get(url: String): T {
    val request = Request.Builder()
        .url(url)
        .get()
        .build()

    BloctoApi.client.newCall(request).execute().use {
        if (it.isSuccessful) {
            return BloctoApi.json.decodeFromString(it.body?.string().orEmpty())
        } else {
            throw Exception(it.body?.string()?.toErrorCode())
        }
    }
}

inline fun <reified T, reified U> post(
    url: String,
    requestBody: U,
    headers: Map<String, String>? = null
): T {
    val body = BloctoApi.json.encodeToString(requestBody).toRequestBody(BloctoApi.jsonType)

    val request = Request.Builder()
        .url(url)
        .post(body)
        .apply {
            for ((key, value) in (headers ?: emptyMap())) {
                addHeader(key, value)
            }
        }
        .build()

    BloctoApi.client.newCall(request).execute().use {
        if (it.isSuccessful) {
            return BloctoApi.json.decodeFromString(it.body?.string().orEmpty())
        } else {
            throw Exception(it.body?.string()?.toErrorCode())
        }
    }
}
