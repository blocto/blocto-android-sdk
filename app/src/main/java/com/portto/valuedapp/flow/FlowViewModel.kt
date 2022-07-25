package com.portto.valuedapp.flow

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.portto.sdk.wallet.BloctoSDKError
import com.portto.sdk.wallet.flow.AccountProofData
import com.portto.sdk.wallet.flow.CompositeSignature
import kotlinx.coroutines.flow.flow

class FlowViewModel : ViewModel() {
    // Network envs
    val envs = listOf(FlowEnv.MAINNET, FlowEnv.TESTNET)

    private val _currentEnv = MutableLiveData(FlowEnv.MAINNET)
    val currentEnv: LiveData<FlowEnv> get() = _currentEnv

    private val _accountProofData = MutableLiveData<AccountProofData?>(null)
    val accountProofData: LiveData<AccountProofData?> get() = _accountProofData

    private val _userSignatureData = MutableLiveData<List<CompositeSignature>?>(null)
    val userSignatureData: LiveData<List<CompositeSignature>?> get() = _userSignatureData

    private val _errorMsg = MutableLiveData<String?>(null)
    val errorMsg: LiveData<String?> get() = _errorMsg

    fun setEnv(env: FlowEnv) {
        _currentEnv.value = env
    }

    fun setAccountProofData(data: AccountProofData) {
        _accountProofData.value = data
    }

    fun setUserSignatureData(data: List<CompositeSignature>) {
        _userSignatureData.value = data
    }

    fun reset() {
        _errorMsg.value = null
        _accountProofData.value = null
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