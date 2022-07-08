package com.portto.valuedapp.flow

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import com.portto.sdk.core.BloctoSDK
import com.portto.sdk.flow.flow

class FlowViewModel(val app: Application) : AndroidViewModel(app) {
    // Env options
    val envs = listOf(FlowEnv.MAINNET, FlowEnv.TESTNET)

    private val _currentEnv = MutableLiveData(FlowEnv.MAINNET)
    val currentEnv: LiveData<FlowEnv> get() = _currentEnv

    private val _currentAddress = MutableLiveData<String?>(null)
    val currentAddress: LiveData<String?> get() = _currentAddress

    private val _errorMsg = MutableLiveData<String?>(null)
    val errorMsg: LiveData<String?> get() = _errorMsg

    fun setEnv(env: FlowEnv) {
        _currentEnv.value = env
    }

    fun setCurrentAddress(address: String?) {
        _currentAddress.value = address
    }

    fun logOut() {
        _currentAddress.value = null
    }

    fun setErrorMsg(msg: String?) {
        _errorMsg.value = msg
    }
}