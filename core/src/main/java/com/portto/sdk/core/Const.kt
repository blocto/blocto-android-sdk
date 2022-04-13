package com.portto.sdk.core

internal object Const {

    private const val BLOCTO_PACKAGE = "com.portto.blocto"
    private const val BLOCTO_PACKAGE_DEBUG = "com.portto.blocto.staging"

    private const val BLOCTO_URI_AUTHORITY = "blocto.app"
    private const val BLOCTO_URI_AUTHORITY_DEBUG = "staging.blocto.app"

    internal const val HTTPS_SCHEME = "https"
    internal const val BLOCTO_URI_PATH = "sdk"

    internal const val KEY_APP_ID = "app_id"
    internal const val KEY_REQUEST_ID = "request_id"
    internal const val KEY_ADDRESS = "address"
    internal const val KEY_ERROR = "error"
    internal const val KEY_METHOD = "method"
    internal const val KEY_BLOCKCHAIN = "blockchain"
    internal const val KEY_MESSAGE = "message"
    internal const val KEY_TX_HASH = "tx_hash"
    internal const val KEY_FROM = "from"
    internal const val KEY_IS_INVOKE_WRAPPED = "is_invoke_wrapped"
    internal const val KEY_PUBLIC_KEY_SIGNATURE_PAIRS = "public_key_signature_pairs"
    internal const val KEY_APPEND_TX = "append_tx"

    internal fun bloctoAuthority(debug: Boolean): String = if (debug) {
        BLOCTO_URI_AUTHORITY_DEBUG
    } else {
        BLOCTO_URI_AUTHORITY
    }

    internal fun bloctoPackage(debug: Boolean): String = if (debug) {
        BLOCTO_PACKAGE_DEBUG
    } else {
        BLOCTO_PACKAGE
    }
}
