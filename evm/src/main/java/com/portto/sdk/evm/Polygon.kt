package com.portto.sdk.evm

import com.portto.sdk.core.Blockchain
import com.portto.sdk.core.BloctoSDK

val BloctoSDK.polygon by lazy { Polygon() }

class Polygon : Evm() {

    override val blockchain: Blockchain
        get() = Blockchain.POLYGON
}
