package com.portto.sdk.wallet

enum class BloctoSDKError(val message: String) {
    USER_REJECTED("user_rejected"),
    USER_NOT_MATCH("user_not_match"),
    FORBIDDEN_BLOCKCHAIN("forbidden_blockchain"),
    INVALID_RESPONSE("invalid_response"),
    UNEXPECTED_ERROR("unexpected_error"),
    ETH_SIGN_INVALID_HEX_STRING("eth_sign_invalid_hex_string")
}
