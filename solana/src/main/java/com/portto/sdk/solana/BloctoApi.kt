package com.portto.sdk.solana

import androidx.annotation.WorkerThread
import com.portto.sdk.core.BloctoSDK
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.logging.HttpLoggingInterceptor
import java.util.concurrent.TimeUnit

@WorkerThread
class BloctoApi {

    private val jsonType = "application/json; charset=utf-8".toMediaType()

    private val baseUrl: String = if (BloctoSDK.debug) {
        "https://dev-api.blocto.app"
    } else {
        "https://api.blocto.app"
    }

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

    fun createRawTransaction(requestBody: SolanaRawTxRequest): SolanaRawTxResponse {
        val body = json.encodeToString(requestBody).toRequestBody(jsonType)
        val request = Request.Builder()
            .url("$baseUrl/solana/createRawTransaction")
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

@Serializable
data class SolanaRawTxRequest(
    @SerialName("sol_address")
    val address: String,
    @SerialName("raw_tx")
    val rawTx: String
)

@Serializable
data class SolanaRawTxResponse(
    @SerialName("raw_tx")
    val rawTx: String,
    @SerialName("extra_data")
    val extraData: ExtraData
) {

    @Serializable
    data class ExtraData(
        @SerialName("append_tx")
        val appendTx: Map<String, String> = emptyMap()
    )
}
