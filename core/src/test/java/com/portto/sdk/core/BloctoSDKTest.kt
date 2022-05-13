package com.portto.sdk.core

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.portto.sdk.core.method.RequestAccountMethod
import com.portto.sdk.wallet.BloctoSDKError
import com.portto.sdk.wallet.Const
import io.mockk.*
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
            .scheme(Const.BLOCTO_SCHEME)
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
            .scheme(Const.BLOCTO_SCHEME)
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
}
