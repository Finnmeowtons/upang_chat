package com.example.upangchattingapp.customDialogs

import android.app.Dialog
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import com.example.upangchattingapp.R

class CustomDialogRegister(context : Context, private val confirmCallback: (String) -> Unit) : Dialog(context) {



    init {
        setContentView(R.layout.activity_custom_dialog_register)

        val confirm = findViewById<Button>(R.id.btnConfirm)
        val cancel = findViewById<Button>(R.id.btnCancel)

        confirm.setOnClickListener {
            confirmCallback("confirm")
            dismiss()
        }

        cancel.setOnClickListener {
            dismiss()
        }

    }
}