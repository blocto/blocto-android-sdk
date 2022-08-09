package com.portto.valuedapp.flow

import androidx.annotation.WorkerThread
import com.nftco.flow.sdk.Flow
import com.nftco.flow.sdk.FlowAddress
import com.portto.sdk.wallet.flow.CompositeSignature
import kotlinx.coroutines.flow.flow

object FlowUtils {

    @WorkerThread
    fun getAccount(address: String, isMainNet: Boolean) = flow {
        emit(getFlowApi(isMainNet).getAccountAtLatestBlock(FlowAddress(address)))
    }

    @WorkerThread
    fun getLatestBlock(isMainNet: Boolean) = flow {
        emit(getFlowApi(isMainNet).getLatestBlock(true))
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