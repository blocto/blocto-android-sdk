package com.portto.valuedapp.flow

enum class FlowEnv(val value: String, val appId: String) {
    MAINNET(value = "Mainnet", appId = ""),
    TESTNET(value = "Testnet", appId = "57f397df-263c-4e97-b61f-15b67b9ce285"),
//    CANARY(value = "Canary", appId = ""),
//    LOCAL(value = "Local", appId = ""),
}