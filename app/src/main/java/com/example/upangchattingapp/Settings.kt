package com.example.upangchattingapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.upangchattingapp.customDialogs.CustomDialogReport
import com.example.upangchattingapp.databinding.ActivitySettingsBinding
import com.example.upangchattingapp.loginandregister.LogInAndRegister
import com.google.firebase.auth.FirebaseAuth

class Settings : AppCompatActivity() {

    private lateinit var binding : ActivitySettingsBinding
    private var auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        binding.apply{

            rlAccountSettings.setOnClickListener {

                startActivity(Intent(this@Settings, AccountSettings::class.java))

            }

            rlLogOut.setOnClickListener {


                auth.signOut()
                startActivity(Intent(this@Settings, LogInAndRegister::class.java))
                finish()
                stopService(Intent(this@Settings, ChecksActiveStatus::class.java))
            }

            rlTermsAndPrivacy.setOnClickListener {

                startActivity(Intent(this@Settings, TermsServicesAndPrivacyPolicy::class.java))

            }

            rlReportTechnicalProblem.setOnClickListener {

                val dialog = CustomDialogReport(this@Settings, "Settings")
                dialog.show()

            }

            btnBackSettings.setOnClickListener {

                onBackPressed()

            }

        }
    }
}