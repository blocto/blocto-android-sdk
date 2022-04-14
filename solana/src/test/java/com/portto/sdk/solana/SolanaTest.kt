package com.portto.sdk.solana

import android.content.Context
import com.portto.sdk.core.BloctoSDK
import com.portto.sdk.core.method.RequestAccountMethod
import com.portto.sdk.core.method.SignAndSendTransactionMethod
import com.portto.sdk.core.toHexString
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
        mockkObject(BloctoSDK)
        BloctoSDK.init(appId = appId, debug = true)
    }

    @After
    fun tearDown() {
        unmockkObject(BloctoSDK)
        BloctoSDK.resetForTesting()
    }

    private val context = mockk<Context>(relaxUnitFun = true)
    private val api = mockk<BloctoApi>()
    private val appId = "57f397df-263c-4e97-b61f-15b67b9ce285"
    private val solAddress = "zJ9A5VfCFdUsXAxouniMbDPMuj8MrWBosDwoQA3D78j"
    private val programId = PublicKey("G4YkbRN4nFQGEUg4SXzPsrManWzuk8bNq9JaMhXepnZ6")
    private val accountPublicKey = PublicKey("4AXy5YYCXpMapaVuzKkz25kVHzrdLDgKN3TiQvtf1Eu8")

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
        val response = SolanaRawTxResponse(
            rawTx = "03020208dac7dab484e8d0f174360daad76fe7b849aa929c5103eff76cd6d30fd0ea947d08866fad950c85a54a4ab0300aac9870ee52cbdb85f0b587f16e1ae6b8e50800b14ade9ea1f267ce8f7d709c69f1614f9803d298833ce1259862772c2ccc913b0ead7cb1d9d3067d07ce4cbae4ee82e8a108586e96dd2dec95a8b09844e63ff82f044d6abceb87a88416562a21f1bb49e216f5f7a829bc88763a2b0664680fa3abd1ab6c38a200af3e21ef9790f98dd8857dcae9229e4b74de929f8c0b6fccc8aeb08762d01b472df8e2ba21e20132a5c218e2435e02d7e4a84030bcd6b9c2b9dfc7f2af100827ea33addbb9d430e457f5311bb905cb3a86a721bc58d72b270193c3ee6fd4e79da4349061266c77e2e3f4c47c1fd1f112bd5cb923170abe9988010607050300070201040d030302000601010300be150000",
            extraData = SolanaRawTxResponse.ExtraData(
                appendTx = emptyMap()
            )
        )
        every { api.createRawTransaction(any()) } returns response

        val solana = Solana(api)

        val transaction = createTransaction().apply {
            add(setValueInstruction(PublicKey(solAddress)))
        }

        val programWallet = solana.convertToProgramWalletTransaction(solAddress, transaction)
        val newTransaction = programWallet.transaction
        val appendTx = programWallet.appendTx

        solana.signAndSendTransaction(
            context = context,
            fromAddress = solAddress,
            transaction = newTransaction,
            appendTx = appendTx,
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
        assertEquals(null, methodSlot.captured.appendTx)
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
