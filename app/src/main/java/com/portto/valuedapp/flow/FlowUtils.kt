package com.portto.valuedapp.flow

import androidx.annotation.WorkerThread
import com.nftco.flow.sdk.Flow
import com.nftco.flow.sdk.FlowAddress
import com.nftco.flow.sdk.simpleFlowScript
import com.portto.sdk.wallet.flow.CompositeSignature
import kotlinx.coroutines.flow.flow

object FlowUtils {

    @WorkerThread
    fun getAccount(address: String, isMainnet: Boolean) = flow {
        emit(getFlowApi(isMainnet).getAccountAtLatestBlock(FlowAddress(address)))
    }

    @WorkerThread
    fun getLatestBlock(isMainnet: Boolean) = flow {
        emit(getFlowApi(isMainnet).getLatestBlock(true))
    }

    @WorkerThread
    fun sendQuery(isMainnet: Boolean, queryScript: String) = flow {
        val result = getFlowApi(isMainnet).simpleFlowScript {
            script(queryScript)
        }
        emit(result.jsonCadence.value)
    }

    /**
     * Flow only
     * Map Flow [CompositeSignature] to string for display
     */
    fun List<CompositeSignature>.mapToString() = joinToString("\n\n") {
        "Address: ${it.address}\nKey ID: ${it.keyId}\nSignature: ${it.signature}"
    }

    private const val FLOW_MAINNET_ENDPOINT = "access.mainnet.nodes.onflow.org"
    private const val FLOW_TESTNET_ENDPOINT = "access.devnet.nodes.onflow.org"

    private fun getFlowApi(isMainNet: Boolean) = Flow.newAccessApi(
        host = if (isMainNet) FLOW_MAINNET_ENDPOINT else FLOW_TESTNET_ENDPOINT,
        port = 9000
    )
}