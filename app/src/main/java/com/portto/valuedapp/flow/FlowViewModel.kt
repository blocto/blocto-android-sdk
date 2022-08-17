package com.portto.valuedapp.flow

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nftco.flow.sdk.*
import com.nftco.flow.sdk.cadence.JsonCadenceBuilder
import com.nftco.flow.sdk.cadence.UFix64NumberField
import com.portto.sdk.wallet.BloctoSDKError
import com.portto.sdk.wallet.flow.AccountProofData
import com.portto.sdk.wallet.flow.CompositeSignature
import com.portto.valuedapp.Config.FLOW_MAINNET_PAYER_ADDRESS
import com.portto.valuedapp.Config.FLOW_TESTNET_PAYER_ADDRESS
import com.portto.valuedapp.Config.getGetValueScript
import com.portto.valuedapp.Config.getSetValueScript
import com.portto.valuedapp.flow.FlowUtils.getAccount
import com.portto.valuedapp.flow.FlowUtils.getLatestBlock
import com.portto.valuedapp.flow.FlowUtils.sendQuery
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class FlowViewModel : ViewModel() {
    // Network envs
    val envs = listOf(FlowEnv.MAINNET, FlowEnv.TESTNET)

    // Flow network
    private val _currentEnv = MutableLiveData(FlowEnv.MAINNET)
    val currentEnv: LiveData<FlowEnv> get() = _currentEnv

    // Authenticated address
    private val _address = MutableLiveData<String?>(null)
    val address: LiveData<String?> get() = _address

    // Composite signatures from account proof
    private val _accountProofSignatures = MutableLiveData<List<CompositeSignature>?>(null)
    val accountProofSignatures: LiveData<List<CompositeSignature>?> get() = _accountProofSignatures

    // Composite signatures from user signature
    private val _userSignatureData = MutableLiveData<List<CompositeSignature>?>(null)
    val userSignatureData: LiveData<List<CompositeSignature>?> get() = _userSignatureData

    // Payload to be sent
    private val _sendTxData = MutableLiveData<String?>(null)
    val sendTxData: LiveData<String?> get() = _sendTxData

    // Transaction hash being sent
    private val _txHash = MutableLiveData<String?>(null)
    val txHash: LiveData<String?> get() = _txHash

    // Value being set in this demo app
    private val _valueUiState = MutableStateFlow<GetValueUiState>(GetValueUiState.Success(""))
    val valueUiState: StateFlow<GetValueUiState> = _valueUiState

    private val _errorMsg = MutableLiveData<String?>(null)
    val errorMsg: LiveData<String?> get() = _errorMsg

    fun setEnv(env: FlowEnv) {
        _currentEnv.value = env
    }

    fun setAccountProofData(data: AccountProofData) {
        _address.value = data.address
        _accountProofSignatures.value = data.signatures
    }

    fun setUserSignatureData(data: List<CompositeSignature>) {
        _userSignatureData.value = data
    }

    fun reset() {
        _errorMsg.value = null
        _address.value = null
        _accountProofSignatures.value = null
        _userSignatureData.value = null
        _txHash.value = null
    }

    fun setErrorMessage(error: BloctoSDKError) {
        val message = error.message.split("_").joinToString(" ")
        setErrorMessage(message)
    }

    fun setErrorMessage(message: String) {
        _errorMsg.value = message
    }

    fun setTxHash(hash: String) {
        _txHash.value = hash
    }

    fun composeTransaction(userAddress: String, inputValue: String, isMainnet: Boolean) {
        viewModelScope.launch {
            combineTransform(
                getAccount(userAddress, isMainnet),
                getLatestBlock(isMainnet)
            ) { account, block ->
                val cosignerKey = account?.keys?.first { it.weight == 999 && !it.revoked }
                    ?: throw Exception("No cosigner found")

                val proposalKey = FlowTransactionProposalKey(
                    address = FlowAddress(userAddress),
                    keyIndex = cosignerKey.id,
                    sequenceNumber = cosignerKey.sequenceNumber.toLong(),
                )

                val transaction = FlowTransaction(
                    script = FlowScript(getSetValueScript(isMainnet)),
                    arguments = listOf(FlowArgument(JsonCadenceBuilder().ufix64(inputValue))),
                    referenceBlockId = block.id,
                    gasLimit = 500L,
                    proposalKey = proposalKey,
                    payerAddress = FlowAddress(if (isMainnet) FLOW_MAINNET_PAYER_ADDRESS else FLOW_TESTNET_PAYER_ADDRESS),
                    authorizers = listOf(FlowAddress(userAddress))
                )

                emit(transaction.canonicalTransaction)
            }.flowOn(Dispatchers.IO).collect {
                _sendTxData.value = it.bytesToHex()
            }
        }
    }

    fun getValue(isMainnet: Boolean) {
        viewModelScope.launch {
            sendQuery(isMainnet, getGetValueScript(isMainnet))
                .flowOn(Dispatchers.IO)
                .catch { _valueUiState.value = GetValueUiState.Failure(it) }
                .collect { _valueUiState.value = GetValueUiState.Success(it as String) }
        }
    }

    fun resetTxData() {
        _sendTxData.value = null
    }
}

sealed class GetValueUiState {
    class Success(val data: String) : GetValueUiState()
    class Failure(val exception: Throwable) : GetValueUiState()
}