package com.portto.valuedapp.flow

import com.portto.valuedapp.Config.APP_ID_MAINNET
import com.portto.valuedapp.Config.APP_ID_TESTNET

enum class FlowEnv(val value: String, val appId: String) {
    MAINNET(value = "Mainnet", appId = APP_ID_MAINNET),
    TESTNET(value = "Testnet", appId = APP_ID_TESTNET),
}