package com.portto.sdk.wallet

object Const {

    private const val BLOCTO_PACKAGE = "com.portto.blocto"
    private const val BLOCTO_PACKAGE_DEBUG = "com.portto.blocto.staging"

    private const val BLOCTO_URI_AUTHORITY = "blocto.app"
    private const val BLOCTO_URI_AUTHORITY_DEBUG = "staging.blocto.app"

    const val HTTPS_SCHEME = "https"
    const val BLOCTO_SCHEME = "blocto"
    const val BLOCTO_URI_PATH = "sdk"

    const val KEY_APP_ID = "app_id"
    const val KEY_REQUEST_ID = "request_id"
    const val KEY_ADDRESS = "address"
    const val KEY_ERROR = "error"
    const val KEY_METHOD = "method"
    const val KEY_BLOCKCHAIN = "blockchain"
    const val KEY_MESSAGE = "message"
    const val KEY_TX_HASH = "tx_hash"
    const val KEY_FROM = "from"
    const val KEY_IS_INVOKE_WRAPPED = "is_invoke_wrapped"
    const val KEY_PUBLIC_KEY_SIGNATURE_PAIRS = "public_key_signature_pairs"
    const val KEY_APPEND_TX = "append_tx"

    fun bloctoAuthority(debug: Boolean): String = if (debug) {
        BLOCTO_URI_AUTHORITY_DEBUG
    } else {
        BLOCTO_URI_AUTHORITY
    }

    fun bloctoPackage(debug: Boolean): String = if (debug) {
        BLOCTO_PACKAGE_DEBUG
    } else {
        BLOCTO_PACKAGE
    }
}