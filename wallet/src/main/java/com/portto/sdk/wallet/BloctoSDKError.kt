package com.portto.sdk.wallet

enum class BloctoSDKError(val message: String) {
    USER_REJECTED("user_rejected"),
    USER_NOT_MATCH("user_not_match"),
    FORBIDDEN_BLOCKCHAIN("forbidden_blockchain"),
    INVALID_RESPONSE("invalid_response"),
    UNEXPECTED_ERROR("unexpected_error"),
    ETH_SIGN_INVALID_HEX_STRING("eth_sign_invalid_hex_string"),
    FLOW_MISSING_ARG("flow_app_id_and_nonce_are_required_by_authn"),
    SESSION_ID_REQUIRED("session_id_required"),
    INVALID_SESSION_ID("invalid_session_id"),
    MESSAGE_REQUIRED("message_required"),
    METHOD_NOT_SUPPORTED("method_not_supported"),
}
