package com.portto.valuedapp

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.portto.valuedapp.databinding.ActivityMainBinding
import com.portto.valuedapp.evm.EvmValueDappActivity
import com.portto.valuedapp.flow.FlowValueDappActivity
import com.portto.valuedapp.solana.SolanaValueDappActivity

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.solanaDapp.setOnClickListener {
            startActivity(Intent(this, SolanaValueDappActivity::class.java))
        }

        binding.evmDapp.setOnClickListener {
            startActivity(Intent(this, EvmValueDappActivity::class.java))
        }

        binding.flowDapp.setOnClickListener {
            startActivity(Intent(this, FlowValueDappActivity::class.java ))
        }
    }
}
