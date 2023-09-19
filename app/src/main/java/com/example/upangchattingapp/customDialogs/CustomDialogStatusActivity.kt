package com.example.upangchattingapp.customDialogs

import android.app.Dialog
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.cardview.widget.CardView
import com.example.upangchattingapp.R

class CustomDialogStatusActivity(context: Context, private val confirmCallback: (String) -> Unit) : Dialog(context) {

    init {
        setContentView(R.layout.activity_custom_dialog_status)



        val online = findViewById<CardView>(R.id.cvCustomDialogOnline)
        val offline = findViewById<CardView>(R.id.cvCustomDialogOffline)

        online.setOnClickListener {
            confirmCallback("Online")
            dismiss()
        }

        offline.setOnClickListener {
            confirmCallback("Offline")
            dismiss()
        }
    }
}