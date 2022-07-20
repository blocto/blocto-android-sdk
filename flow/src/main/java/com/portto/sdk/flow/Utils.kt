package com.portto.sdk.flow

import android.net.Uri
import com.portto.sdk.wallet.Const.KEY_ACCOUNT_PROOF
import com.portto.sdk.wallet.Const.KEY_ADDRESS
import com.portto.sdk.wallet.Const.KEY_KEY_ID
import com.portto.sdk.wallet.Const.KEY_SIGNATURE
import com.portto.sdk.wallet.flow.CompositeSignature


/**
 * Convert the Uri query params into a list of [CompositeSignature]
 *
 * Example of the query parameters:
 *      address={}
 *      account_proof[index][address/key_id/signature]={}
 */
internal fun Uri.parse(): Pair<String, List<CompositeSignature>>? {
    val address = getQueryParameter(KEY_ADDRESS) ?: return null
    val compositeSignatureList = mutableListOf<CompositeSignature>()

    var index = 0
    while (getQueryParameter("$KEY_ACCOUNT_PROOF[$index][$KEY_ADDRESS]") != null) {
        compositeSignatureList.add(
            CompositeSignature(
                // Addresses shall be the same
                address = getQueryParameter("$KEY_ACCOUNT_PROOF[$index][$KEY_ADDRESS]")
                    .takeIf { it == address } ?: return null,

                keyId = getQueryParameter("$KEY_ACCOUNT_PROOF[$index][$KEY_KEY_ID]")
                    ?: return null,

                signature = getQueryParameter("$KEY_ACCOUNT_PROOF[$index][$KEY_SIGNATURE]")
                    ?: return null
            )
        )
        index++
    }
    return Pair(address, compositeSignatureList)
}