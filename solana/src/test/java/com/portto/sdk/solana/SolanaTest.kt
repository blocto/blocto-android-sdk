package com.portto.sdk.solana

import android.content.Context
import android.net.Uri
import com.portto.sdk.core.BloctoSDK
import com.portto.sdk.core.method.RequestAccountMethod
import com.portto.sdk.core.toHexString
import com.portto.sdk.solana.method.SignAndSendTransactionMethod
import com.portto.sdk.solana.model.SolanaRawTxResponse
import com.portto.sdk.wallet.BloctoSDKError
import com.portto.sdk.wallet.Const
import com.portto.solana.web3.AccountMeta
import com.portto.solana.web3.PublicKey
import com.portto.solana.web3.Transaction
import com.portto.solana.web3.TransactionInstruction
import com.portto.solana.web3.programs.Program
import io.mockk.*
import org.junit.*
import org.junit.Assert.assertEquals
import org.junit.runner.RunWith
import org.near.borshj.BorshBuffer
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.*

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class SolanaTest {

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

    @Before
    fun setUp() {
        mockkStatic(BloctoSDK::class)
        BloctoSDK.init(appId = appId, debug = true)
    }

    @After
    fun tearDown() {
        unmockkStatic(BloctoSDK::class)
        BloctoSDK.resetForTesting()
    }

    private val context = mockk<Context>(relaxUnitFun = true)
    private val api = SolanaService
    private val appId = "57f397df-263c-4e97-b61f-15b67b9ce285"
    private val solAddress = "zJ9A5VfCFdUsXAxouniMbDPMuj8MrWBosDwoQA3D78j"
    private val programId = PublicKey("G4YkbRN4nFQGEUg4SXzPsrManWzuk8bNq9JaMhXepnZ6")
    private val accountPublicKey = PublicKey("4AXy5YYCXpMapaVuzKkz25kVHzrdLDgKN3TiQvtf1Eu8")
    private val onSuccess: (String) -> Unit = mockk(relaxed = true)
    private val onError: (BloctoSDKError) -> Unit = mockk(relaxed = true)
    private val txHash =
        "2ZTRtryBTAPkbgALqtX9zi9TuuHCxzPBXDVQicDNRdQ7rSNH9Pb1zvAbKoissLSQ4vUeeNn2FhYegYmCDaQY6Jhw"

    @Test
    fun `test request account`() {
        val solana = Solana(api)

        solana.requestAccount(
            context = context,
            onSuccess = {},
            onError = {}
        )

        val methodSlot = slot<RequestAccountMethod>()
        verify { BloctoSDK.send(context, capture(methodSlot)) }
        assertEquals("request_account", methodSlot.captured.name)
    }

    @Test
    fun `test sign and send transaction`() {
        val solana = Solana(api)

        val transaction = createTransaction().apply {
            add(setValueInstruction(PublicKey(solAddress)))
        }

        solana.signAndSendTransaction(
            context = context,
            fromAddress = solAddress,
            transaction = transaction,
            onSuccess = {},
            onError = {}
        )

        val methodSlot = slot<SignAndSendTransactionMethod>()
        verify { BloctoSDK.send(context, capture(methodSlot)) }
        assertEquals("sign_and_send_transaction", methodSlot.captured.name)
        assertEquals(solAddress, methodSlot.captured.fromAddress)
        assertEquals(transaction.serializeMessage().toHexString(), methodSlot.captured.message)
        assertEquals(false, methodSlot.captured.isInvokeWrapped)
        assertEquals(null, methodSlot.captured.publicKeySignaturePairs)
        assertEquals(null, methodSlot.captured.appendTx)
    }

    @Test
    fun `test sign and send program wallet transaction`() {

        val solana = Solana(api)

        val transaction = createTransaction().apply {
            add(setValueInstruction(PublicKey(solAddress)))
        }

        val newTransaction = solana.convertToProgramWalletTransaction(solAddress, transaction)

        solana.signAndSendTransaction(
            context = context,
            fromAddress = solAddress,
            transaction = newTransaction,
            onSuccess = {},
            onError = {}
        )

        val methodSlot = slot<SignAndSendTransactionMethod>()
        verify { BloctoSDK.send(context, capture(methodSlot)) }
        assertEquals("sign_and_send_transaction", methodSlot.captured.name)
        assertEquals(solAddress, methodSlot.captured.fromAddress)
        assertEquals(newTransaction.serializeMessage().toHexString(), methodSlot.captured.message)
        assertEquals(true, methodSlot.captured.isInvokeWrapped)
        assertEquals(null, methodSlot.captured.publicKeySignaturePairs)
    }

    @Test
    fun `test sign and send transaction success callback`() {
        val solana = Solana(api)

        val transaction = createTransaction().apply {
            add(setValueInstruction(PublicKey(solAddress)))
        }

        solana.signAndSendTransaction(
            context = context,
            fromAddress = solAddress,
            transaction = transaction,
            onSuccess = onSuccess,
            onError = onError
        )

        val successCallbackUri = Uri.Builder()
            .scheme(Const.BLOCTO_SCHEME)
            .appendQueryParameter(Const.KEY_REQUEST_ID, requestId)
            .appendQueryParameter(Const.KEY_TX_HASH, txHash)
            .build()

        BloctoSDK.handleCallback(successCallbackUri)

        val txHashSlot = slot<String>()
        verify { onSuccess(capture(txHashSlot)) }
        assertEquals(txHash, txHashSlot.captured)
    }

    @Test
    fun `test sign and send transaction error callback`() {
        val solana = Solana(api)

        val transaction = createTransaction().apply {
            add(setValueInstruction(PublicKey(solAddress)))
        }

        solana.signAndSendTransaction(
            context = context,
            fromAddress = solAddress,
            transaction = transaction,
            onSuccess = onSuccess,
            onError = onError
        )

        val error = BloctoSDKError.USER_NOT_MATCH
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

    private fun createTransaction(): Transaction {
        return Transaction().apply {
            feePayer = PublicKey(solAddress)
            setRecentBlockHash("9epqjooJdxbGwrMXsfQnnV2hsJjDrPTRfcrkXa8MrJJS")
        }
    }

    private fun setValueInstruction(addressPublicKey: PublicKey): TransactionInstruction {
        val buffer = BorshBuffer.allocate(Byte.SIZE_BYTES + Int.SIZE_BYTES).apply {
            writeU8(0)
            writeU32(10)
        }
        return Program.createTransactionInstruction(
            programId = programId,
            keys = listOf(
                AccountMeta(publicKey = accountPublicKey, isSigner = false, isWritable = true),
                AccountMeta(publicKey = addressPublicKey, isSigner = true, isWritable = false)
            ),
            data = buffer.toByteArray()
        )
    }
}
