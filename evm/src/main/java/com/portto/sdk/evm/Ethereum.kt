package com.portto.sdk.evm

import com.portto.sdk.core.Blockchain
import com.portto.sdk.core.BloctoSDK

val BloctoSDK.ethereum by lazy { Ethereum() }

class Ethereum : Evm() {

    override val blockchain: Blockchain
        get() = Blockchain.ETHEREUM
}
