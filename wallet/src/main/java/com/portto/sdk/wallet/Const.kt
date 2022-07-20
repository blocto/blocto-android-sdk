package com.portto.sdk.wallet

const val METHOD_AUTHN = "authn"
const val METHOD_REQUEST_ACCOUNT = "request_account"
const val METHOD_SEND_TX = "send_transaction"
const val METHOD_SIGN_AND_SEND_TX = "sign_and_send_transaction"
const val METHOD_SIGN_MESSAGE = "sign_message"

object Const {

    private const val BLOCTO_PACKAGE = "com.portto.blocto"
    private const val BLOCTO_PACKAGE_DEBUG = "com.portto.blocto.staging"

    private const val BLOCTO_URI_AUTHORITY = "blocto.app"
    private const val BLOCTO_URI_AUTHORITY_DEBUG = "staging.blocto.app"

    private const val WEB_SDK_URL = "wallet.blocto.app"
    private const val WEB_SDK_URL_DEBUG = "wallet-testnet.blocto.app"

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
    const val KEY_TO = "to"
    const val KEY_IS_INVOKE_WRAPPED = "is_invoke_wrapped"
    const val KEY_PUBLIC_KEY_SIGNATURE_PAIRS = "public_key_signature_pairs"
    const val KEY_APPEND_TX = "append_tx"
    const val KEY_DATA = "data"
    const val KEY_VALUE = "value"
    const val KEY_TYPE = "type"
    const val KEY_SIGNATURE = "signature"
    const val KEY_ACCOUNT_PROOF = "account_proof" // Since 0.3.0 (Flow)
    const val KEY_KEY_ID = "key_id" // Since 0.3.0 (Flow)
    const val KEY_FLOW_APP_ID = "flow_app_id" // Since 0.3.0 (Flow)
    const val KEY_FLOW_NONCE = "flow_nonce" // Since 0.3.0 (Flow)


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

    fun webSDKUrl(debug: Boolean): String = if (debug) {
        WEB_SDK_URL_DEBUG
    } else {
        WEB_SDK_URL
    }
}
