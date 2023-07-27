package com.portto.valuedapp.evm

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.portto.sdk.wallet.BloctoSDKError

class EvmViewModel : ViewModel() {

    var currentChain = EvmChain.ETHEREUM
    var currentAddress: String? = null
    val _rpcUrl = MutableLiveData<String?>()
    val rpcUrl: LiveData<String?> = _rpcUrl

    private var _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private var _resetView = MutableLiveData<Boolean>()
    val resetView: LiveData<Boolean> = _resetView

    fun setRpcUrl(url: String?) {
        _rpcUrl.value = url
    }

    fun showError(error: BloctoSDKError) {
        val message = error.message.split("_").joinToString(" ")
        showError(message)
    }

    fun showError(message: String?) {
        _error.value = message
    }

    fun resetView() {
        currentAddress = null
        _resetView.value = true
    }
}
