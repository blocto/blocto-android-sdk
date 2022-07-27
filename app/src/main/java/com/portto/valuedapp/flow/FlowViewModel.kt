package com.portto.valuedapp.flow

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.portto.sdk.wallet.BloctoSDKError
import com.portto.sdk.wallet.flow.AccountProofData
import com.portto.sdk.wallet.flow.CompositeSignature

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
    }

    fun setErrorMessage(error: BloctoSDKError) {
        val message = error.message.split("_").joinToString(" ")
        setErrorMessage(message)
    }

    fun setErrorMessage(message: String) {
        _errorMsg.value = message
    }
}