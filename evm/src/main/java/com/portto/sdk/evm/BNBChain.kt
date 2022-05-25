package com.portto.sdk.evm

import com.portto.sdk.core.Blockchain
import com.portto.sdk.core.BloctoSDK

val BloctoSDK.bnb by lazy { BNBChain() }

class BNBChain : Evm() {

    override val blockchain: Blockchain
        get() = Blockchain.BNB_CHAIN
}
