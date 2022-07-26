package com.portto.sdk.flow

import android.net.Uri
import com.portto.sdk.wallet.Const.KEY_ACCOUNT_PROOF
import com.portto.sdk.wallet.Const.KEY_ADDRESS
import com.portto.sdk.wallet.Const.KEY_KEY_ID
import com.portto.sdk.wallet.Const.KEY_SIGNATURE
import com.portto.sdk.wallet.Const.KEY_USER_SIGNATURE
import com.portto.sdk.wallet.METHOD_FLOW_AUTHN
import com.portto.sdk.wallet.METHOD_FLOW_SIGN_MESSAGE
import com.portto.sdk.wallet.flow.CompositeSignature


/**
 * Convert the Uri query params into a list of [CompositeSignature]
 * Example of the query parameters:
 *  {account_proof/user_signature}[{index}][{address/key_id/signature}]={}
 *
 * @param method type shall be either [METHOD_FLOW_AUTHN] or [METHOD_FLOW_SIGN_MESSAGE]
 * @param address the address to validate whether the addresses in [CompositeSignature] are identical
 * @return list of [CompositeSignature]; this can be null when either a parameter is not found or the signature list is empty
 */
internal fun Uri.parse(method: String, address: String): List<CompositeSignature>? {
    if (method !in setOf(METHOD_FLOW_AUTHN, METHOD_FLOW_SIGN_MESSAGE))
        throw Exception("Invalid method type: $method")

    val compositeSignatureList = mutableListOf<CompositeSignature>()

    var index = 0

    val prefix = if (method == METHOD_FLOW_AUTHN) KEY_ACCOUNT_PROOF else KEY_USER_SIGNATURE

    while (getQueryParameter("$prefix[$index][$KEY_ADDRESS]") != null) {
        compositeSignatureList.add(
            CompositeSignature(
                // Addresses shall be the same
                address = getQueryParameter("$prefix[$index][$KEY_ADDRESS]")
                    .takeIf { it == address } ?: return null,

                keyId = getQueryParameter("$prefix[$index][$KEY_KEY_ID]")
                    ?: return null,

                signature = getQueryParameter("$prefix[$index][$KEY_SIGNATURE]")
                    ?: return null
            )
        )
        index++
    }
    return compositeSignatureList.takeIf { it.isNotEmpty() }
}