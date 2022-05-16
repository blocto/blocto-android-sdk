package com.portto.valuedapp.evm

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.portto.valuedapp.R
import com.portto.valuedapp.databinding.FragmentEvmSignMessageBinding

class EvmSignMessageFragment : Fragment(R.layout.fragment_evm_sign_message) {

    private lateinit var binding: FragmentEvmSignMessageBinding
    private val viewModel: EvmViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentEvmSignMessageBinding.bind(view)
    }
}
