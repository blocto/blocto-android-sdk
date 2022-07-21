package com.portto.sdk.wallet.flow

data class AccountProofData(
    // A human readable string to identify flow application during signing
    val flowAppId: String,
    // Flow account address
    val address: String,
    // Random string in hexadecimal format (minimum 32 bytes in total, i.e 64 hex characters)
    val nonce: String,
    // Array of composite signatures
    val signatures: List<CompositeSignature>
) {
}
