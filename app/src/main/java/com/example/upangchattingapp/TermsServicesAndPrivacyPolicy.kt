package com.example.upangchattingapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.upangchattingapp.databinding.ActivityTermsServicesAndPrivacyPolicyBinding

class TermsServicesAndPrivacyPolicy : AppCompatActivity() {

    private lateinit var binding : ActivityTermsServicesAndPrivacyPolicyBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTermsServicesAndPrivacyPolicyBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        binding.btnBackTermsPrivacy.setOnClickListener {

            onBackPressed()

        }
    }
}