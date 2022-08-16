package com.portto.valuedapp

object Config {
    const val INFURA_ID = ""

    // Your Blocto App ID
    const val APP_ID_MAINNET = "0896e44c-20fd-443b-b664-d305b52fe8e8"
    const val APP_ID_TESTNET = "57f397df-263c-4e97-b61f-15b67b9ce285"

    // Required by Flow
    const val FLOW_APP_IDENTIFIER = "Awesome App (v0.0)"
    const val FLOW_NONCE = "75f8587e5bd5f9dcc9909d0dae1f0ac5814458b2ae129620502cb936fde7120a"


    private const val SCRIPT_TESTNET_ADDRESS = "0x5a8143da8058740c"
    private const val SCRIPT_MAINNET_ADDRESS = "0x8320311d63f3b336"

    /**
     * Sample script for sending transaction
     */
    fun getSetValueScript(isMainnet: Boolean = false): String = """
        import ValueDapp from ${if (isMainnet) SCRIPT_MAINNET_ADDRESS else SCRIPT_TESTNET_ADDRESS}
        
        transaction(value: UFix64) {
            prepare(authorizer: AuthAccount) {
                    ValueDapp.setValue(value)
            }
        }
    """.trimIndent()

    /**
     * Sample script for querying
     */
    fun getGetValueScript(isMainnet: Boolean = false): String = """
        import ValueDapp from ${if (isMainnet) SCRIPT_MAINNET_ADDRESS else SCRIPT_TESTNET_ADDRESS}
        
        pub fun main(): UFix64 {
	        return ValueDapp.value
        }
    """.trimIndent()
}
