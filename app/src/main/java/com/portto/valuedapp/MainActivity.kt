package com.portto.valuedapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.portto.sdk.core.BloctoSDK
import com.portto.sdk.solana.solana
import com.portto.valuedapp.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        BloctoSDK.init(appId = "your app id")

        binding.button.setOnClickListener {
            BloctoSDK.solana.requestAccount(
                context = this,
                onSuccess = {
                    binding.text.text = "address: $it"
                },
                onError = {
                    binding.text.text = "error: $it"
                }
            )
        }
    }
}
