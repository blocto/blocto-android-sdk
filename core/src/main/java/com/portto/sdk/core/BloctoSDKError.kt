package com.portto.sdk.core

enum class BloctoSDKError(val message: String) {
    USER_REJECTED("user_rejected"),
    FORBIDDEN_BLOCKCHAIN("forbidden_blockchain"),
    INVALID_RESPONSE("invalid_response"),
    UNEXPECTED_ERROR("unexpected_error")
}
