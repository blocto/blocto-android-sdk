package com.portto.valuedapp.flow

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.portto.sdk.wallet.BloctoSDKError
import com.portto.sdk.wallet.flow.AccountProofData

class FlowViewModel : ViewModel() {
    // Network envs
    val envs = listOf(FlowEnv.MAINNET, FlowEnv.TESTNET)

    private val _currentEnv = MutableLiveData(FlowEnv.MAINNET)
    val currentEnv: LiveData<FlowEnv> get() = _currentEnv

    private val _accountProofData = MutableLiveData<AccountProofData?>(null)
    val accountProofData: LiveData<AccountProofData?> get() = _accountProofData

    private val _errorMsg = MutableLiveData<String?>(null)
    val errorMsg: LiveData<String?> get() = _errorMsg

    fun setEnv(env: FlowEnv) {
        _currentEnv.value = env
    }

    fun setAccountProofData(data: AccountProofData?) {
        _accountProofData.value = data
    }

    fun showError(error: BloctoSDKError) {
        val message = error.message.split("_").joinToString(" ")
        showError(message)
    }

    fun showError(message: String) {
        _errorMsg.value = message
    }
}