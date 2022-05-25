package com.portto.sdk.evm

import com.portto.sdk.core.Blockchain
import com.portto.sdk.core.BloctoSDK

val BloctoSDK.avalanche by lazy { Avalanche() }

class Avalanche : Evm() {

    override val blockchain: Blockchain
        get() = Blockchain.AVALANCHE
}
