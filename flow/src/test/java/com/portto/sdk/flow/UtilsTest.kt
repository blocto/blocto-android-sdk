package com.portto.sdk.flow


import android.net.Uri
import androidx.core.net.toUri
import org.junit.Test

import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config


@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class UtilsTest {
    @Test
    fun parse_url() {
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
        uri.parse()?.let { (address, signatures) ->
            assertEquals(true, signatures[0].address == address)
            assertEquals(true, signatures[1].address == address)
            assertEquals("0", signatures[1].keyId)
        }
    }
}