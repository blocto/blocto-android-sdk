package com.portto.sdk.flow


import android.net.Uri
import androidx.core.net.toUri
import com.portto.sdk.wallet.METHOD_FLOW_AUTHN
import com.portto.sdk.wallet.METHOD_FLOW_SIGN_MESSAGE
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config


@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class UtilsTest {
    @Test
    fun parse_authn_url() {
        val uri: Uri =
            ("blocto://?address=0x16cf982698f55d39&" +
                    "account_proof%5B0%5D%5B" +
                    "address%5D=0x16cf982698f55d39&" +
                    "account_proof%5B0%5D%5B" +
                    "key_id%5D=7&" +
                    "account_proof%5B0%5D%5B" +
                    "signature%5D=850207b&" +
                    "account_proof%5B1%5D%5B" +
                    "address%5D=0x16cf982698f55d39&" +
                    "account_proof%5B1%5D%5Bkey_id%5D=0&" +
                    "account_proof%5B1%5D%5Bsignature%5D=02dfd2822bc93c7cf7fbe670d0bd3439&request_id=EA365ECC").toUri()
        val parsedAddress: String? = uri.getQueryParameter("address")
        assertEquals("0x16cf982698f55d39", parsedAddress)
        uri.parse(method = METHOD_FLOW_AUTHN, address = parsedAddress!!)?.let { signatures ->
            assertEquals(true, signatures[0].address == parsedAddress)
            assertEquals(true, signatures[1].address == parsedAddress)
            assertEquals("0", signatures[1].keyId)
        }
    }

    @Test
    fun parse_signature_url() {
        val uri: Uri =
            ("blocto64776cec-5953-4a58-8025-772f55a3917b://?" +
                    "user_signature%5B0%5D%5Baddress%5D=0x16cf982698f55d39&" +
                    "user_signature%5B0%5D%5Bkey_id%5D=9&" +
                    "user_signature%5B0%5D%5Bsignature%5D=53ce209e1a1a272de072e7d9a1f0dc901b00b2b9b4fb1bdffd80f470bd1b87701f072d7deaabb3023ec52ea6a3dd26dc2e81b60023c72ae3b4fbdb64eda6cfc7&" +
                    "user_signature%5B1%5D%5Baddress%5D=0x16cf982698f55d39&" +
                    "user_signature%5B1%5D%5Bkey_id%5D=0&" +
                    "user_signature%5B1%5D%255Bsignature%5D=71fc1f13d79b786f219c379387bd041ea66ff3857d032a6f542e2a817576a589b34f58d2d72b53006b114d0c991eabc0c428153e83a84c4d0ad9ee3f80cb8577&" +
                    "request_id=A890061C-AE8E-4B90-8FFA-0EF5F99E4CA5").toUri()
        val address = "0x16cf982698f55d39"
        uri.parse(method = METHOD_FLOW_SIGN_MESSAGE, address = address)?.let { signatures ->
            assertEquals(true, signatures[0].address == address)
            assertEquals(true, signatures[1].address == address)
            assertEquals("9", signatures[0].keyId)
        }
    }
}