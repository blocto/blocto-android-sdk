package com.portto.sdk.core

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.portto.sdk.core.method.RequestAccountMethod
import com.portto.sdk.core.method.SignAndSendTransactionMethod
import com.portto.sdk.wallet.BloctoSDKError
import com.portto.sdk.wallet.Const
import io.mockk.*
import org.json.JSONObject
import org.junit.After
import org.junit.AfterClass
import org.junit.Assert.assertEquals
import org.junit.BeforeClass
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.*

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class BloctoSDKTest {

    companion object {
        private const val requestId = "13e1705a-ed0d-4d1a-b4ef-92d1b50db802"

        @BeforeClass
        @JvmStatic
        fun beforeClass() {
            mockkStatic(UUID::class)
            every { UUID.randomUUID().toString() } returns requestId
        }

        @AfterClass
        @JvmStatic
        fun afterClass() {
            unmockkStatic(UUID::class)
        }
    }

    @After
    fun tearDown() {
        BloctoSDK.resetForTesting()
    }

    private val context = mockk<Context>(relaxUnitFun = true)
    private val onSuccess: (String) -> Unit = mockk(relaxed = true)
    private val onError: (BloctoSDKError) -> Unit = mockk(relaxed = true)
    private val appId = "57f397df-263c-4e97-b61f-15b67b9ce285"
    private val solAddress = "zJ9A5VfCFdUsXAxouniMbDPMuj8MrWBosDwoQA3D78j"
    private val txHash =
        "2ZTRtryBTAPkbgALqtX9zi9TuuHCxzPBXDVQicDNRdQ7rSNH9Pb1zvAbKoissLSQ4vUeeNn2FhYegYmCDaQY6Jhw"

    @Test(expected = NullPointerException::class)
    fun `test without app id setup`() {
        val method = RequestAccountMethod(
            blockchain = Blockchain.SOLANA,
            onSuccess = {},
            onError = {}
        )
        BloctoSDK.send(context, method)
    }

    @Test
    fun `test request account with success callback`() {
        requestAccount()

        val successCallbackUri = Uri.Builder()
            .scheme("blocto")
            .appendQueryParameter(Const.KEY_REQUEST_ID, requestId)
            .appendQueryParameter(Const.KEY_ADDRESS, solAddress)
            .build()

        BloctoSDK.handleCallback(successCallbackUri)

        val addressSlot = slot<String>()
        verify { onSuccess(capture(addressSlot)) }
        assertEquals(solAddress, addressSlot.captured)
    }

    @Test
    fun `test request account with error callback`() {
        requestAccount()

        val error = BloctoSDKError.USER_REJECTED
        val errorCallbackUri = Uri.Builder()
            .scheme("blocto")
            .appendQueryParameter(Const.KEY_REQUEST_ID, requestId)
            .appendQueryParameter(Const.KEY_ERROR, error.message)
            .build()

        BloctoSDK.handleCallback(errorCallbackUri)

        val errorSlot = slot<BloctoSDKError>()
        verify { onError(capture(errorSlot)) }
        assertEquals(error.message, errorSlot.captured.message)
    }

    @Test
    fun `test sign and send transaction with success callback`() {
        signAndSendTransaction()

        val successCallbackUri = Uri.Builder()
            .scheme("blocto")
            .appendQueryParameter(Const.KEY_REQUEST_ID, requestId)
            .appendQueryParameter(Const.KEY_TX_HASH, txHash)
            .build()

        BloctoSDK.handleCallback(successCallbackUri)

        val txHashSlot = slot<String>()
        verify { onSuccess(capture(txHashSlot)) }
        assertEquals(txHash, txHashSlot.captured)
    }

    @Test
    fun `test sign and send transaction with error callback`() {
        signAndSendTransaction()

        val error = BloctoSDKError.USER_NOT_MATCH
        val errorCallbackUri = Uri.Builder()
            .scheme("blocto")
            .appendQueryParameter(Const.KEY_REQUEST_ID, requestId)
            .appendQueryParameter(Const.KEY_ERROR, error.message)
            .build()

        BloctoSDK.handleCallback(errorCallbackUri)

        val errorSlot = slot<BloctoSDKError>()
        verify { onError(capture(errorSlot)) }
        assertEquals(error.message, errorSlot.captured.message)
    }

    private fun requestAccount() {
        val intentSlot = slot<Intent>()
        val blockchain = Blockchain.SOLANA

        BloctoSDK.init(appId)

        val method = spyk(
            RequestAccountMethod(
                blockchain = blockchain,
                onSuccess = onSuccess,
                onError = onError
            )
        )
        BloctoSDK.send(context, method)

        verify { context.startActivity(capture(intentSlot)) }

        val capturedData = intentSlot.captured.data
        assertEquals(Const.bloctoAuthority(BloctoSDK.debug), capturedData?.authority)
        assertEquals(method.name, capturedData?.getQueryParameter(Const.KEY_METHOD))
        assertEquals(appId, capturedData?.getQueryParameter(Const.KEY_APP_ID))
        assertEquals(requestId, capturedData?.getQueryParameter(Const.KEY_REQUEST_ID))
        assertEquals(blockchain.value, capturedData?.getQueryParameter(Const.KEY_BLOCKCHAIN))
    }

    private fun signAndSendTransaction() {
        val intentSlot = slot<Intent>()
        val blockchain = Blockchain.SOLANA
        val message = "test_message"
        val isInvokeWrapped = true
        val publicKeySignaturePairs = mapOf(
            "public_key_1" to "signature_1",
            "public_key_2" to "signature_2"
        )
        val appendTx = mapOf(
            "id_1" to "tx_1",
            "id_2" to "tx_2"
        )

        BloctoSDK.init(appId)

        val method = spyk(
            SignAndSendTransactionMethod(
                fromAddress = solAddress,
                message = message,
                isInvokeWrapped = true,
                publicKeySignaturePairs = publicKeySignaturePairs,
                appendTx = appendTx,
                blockchain = blockchain,
                onSuccess = onSuccess,
                onError = onError
            )
        )
        BloctoSDK.send(context, method)

        verify { context.startActivity(capture(intentSlot)) }

        val capturedData = intentSlot.captured.data
        assertEquals(Const.bloctoAuthority(BloctoSDK.debug), capturedData?.authority)
        assertEquals(method.name, capturedData?.getQueryParameter(Const.KEY_METHOD))
        assertEquals(appId, capturedData?.getQueryParameter(Const.KEY_APP_ID))
        assertEquals(requestId, capturedData?.getQueryParameter(Const.KEY_REQUEST_ID))
        assertEquals(blockchain.value, capturedData?.getQueryParameter(Const.KEY_BLOCKCHAIN))
        assertEquals(message, capturedData?.getQueryParameter(Const.KEY_MESSAGE))
        assertEquals(
            isInvokeWrapped,
            capturedData?.getQueryParameter(Const.KEY_IS_INVOKE_WRAPPED)?.toBoolean()
        )
        assertEquals(
            publicKeySignaturePairs,
            capturedData?.getQueryParameter(Const.KEY_PUBLIC_KEY_SIGNATURE_PAIRS)?.let {
                JSONObject(it).toStringMap()
            }
        )
        assertEquals(
            appendTx,
            capturedData?.getQueryParameter(Const.KEY_APPEND_TX)?.let {
                JSONObject(it).toStringMap()
            }
        )
    }

    private fun JSONObject.toStringMap(): Map<String, String> {
        val map = mutableMapOf<String, String>()
        this.keys().forEach { key ->
            map[key] = this.getString(key)
        }
        return map
    }
}
