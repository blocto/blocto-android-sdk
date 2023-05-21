package com.portto.sdk.wallet

const val METHOD_REQUEST_ACCOUNT = "request_account"
const val METHOD_SEND_TX = "send_transaction"
const val METHOD_SIGN_AND_SEND_TX = "sign_and_send_transaction"
const val METHOD_SIGN_MESSAGE = "sign_message"

const val METHOD_FLOW_AUTHN = "authn"
const val METHOD_FLOW_SIGN_MESSAGE = "user_signature"
const val METHOD_FLOW_SEND_TRANSACTION = "flow_send_transaction"

object Const {

    private const val BLOCTO_PACKAGE_PROD = "com.portto.blocto"
    private const val BLOCTO_PACKAGE_DEV = "com.portto.blocto.dev"

    private const val BLOCTO_URI_AUTHORITY_PROD = "blocto.app"
    private const val BLOCTO_URI_AUTHORITY_DEV = "dev.blocto.app"

    private const val BLOCTO_API_URL_PROD = "https://api.blocto.app"
    private const val BLOCTO_API_URL_DEV = "https://api-dev.blocto.app"

    private const val WEB_SDK_URL_PROD = "wallet-v2.blocto.app"
    private const val WEB_SDK_URL_DEV = "wallet-v2-dev.blocto.app"

    private const val WEB_API_URL_PROD = "https://wallet-v2.blocto.app/api"
    private const val WEB_API_URL_DEV = "https://wallet-v2-dev.blocto.app/api"

    const val HTTPS_SCHEME = "https"
    const val BLOCTO_SCHEME = "blocto"
    const val BLOCTO_URI_PATH = "sdk"
    const val SDK_SOURCE = "sdk_android"

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
    const val KEY_USER_SIGNATURE = "user_signature" // Since 0.3.0 (Flow)
    const val KEY_KEY_ID = "key_id" // Since 0.3.0 (Flow)
    const val KEY_FLOW_APP_ID = "flow_app_id" // Since 0.3.0 (Flow)
    const val KEY_FLOW_NONCE = "flow_nonce" // Since 0.3.0 (Flow)
    const val KEY_FLOW_TX = "flow_transaction" // Since 0.3.0 (Flow)
    const val KEY_SESSION_ID = "session_id"
    const val KEY_REQUEST_SOURCE = "request_source"
    const val KEY_PLATFORM = "platform"

    const val PATH_DAPP = "dapp"
    const val PATH_AUTHN = "authn"
    const val PATH_AUTHZ = "authz"
    const val PATH_USER_SIGNATURE = "user-signature"

    const val HEADER_SESSION_ID = "Blocto-Session-Identifier"
    const val HEADER_REQUEST_ID = "Blocto-Request-Identifier"
    const val HEADER_REQUEST_SOURCE = "Blocto-Request-Source"

    fun bloctoAuthority(env: BloctoEnv): String = when (env) {
        BloctoEnv.PROD -> BLOCTO_URI_AUTHORITY_PROD
        BloctoEnv.DEV -> BLOCTO_URI_AUTHORITY_DEV
    }

    fun bloctoPackage(env: BloctoEnv): String = when (env) {
        BloctoEnv.PROD -> BLOCTO_PACKAGE_PROD
        BloctoEnv.DEV -> BLOCTO_PACKAGE_DEV
    }

    fun bloctoApiUrl(env: BloctoEnv): String = when (env) {
        BloctoEnv.PROD -> BLOCTO_API_URL_PROD
        BloctoEnv.DEV -> BLOCTO_API_URL_DEV
    }

    fun webSDKUrl(env: BloctoEnv): String = when (env) {
        BloctoEnv.PROD -> WEB_SDK_URL_PROD
        BloctoEnv.DEV -> WEB_SDK_URL_DEV
    }

    fun webApiUrl(env: BloctoEnv): String = when (env) {
        BloctoEnv.PROD -> WEB_API_URL_PROD
        BloctoEnv.DEV -> WEB_API_URL_DEV
    }
}
