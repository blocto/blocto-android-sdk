package com.portto.sdk.wallet

enum class EvmSignType(val type: String) {
    ETH_SIGN("sign"),
    PERSONAL_SIGN("personal_sign"),
    TYPED_DATA_SIGN("typed_data_sign"),
    TYPED_DATA_SIGN_V3("typed_data_sign_v3"),
    TYPED_DATA_SIGN_V4("typed_data_sign_v4")
}
