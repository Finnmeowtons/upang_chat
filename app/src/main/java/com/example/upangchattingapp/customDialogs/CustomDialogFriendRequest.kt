package com.example.upangchattingapp.customDialogs

import android.app.Dialog
import android.content.Context
import android.view.WindowManager
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import com.example.upangchattingapp.R

class CustomDialogFriendRequest(context: Context, text : String, additional : String, private val confirmCallback: (String) -> Unit) : Dialog(context) {

    init {
        setContentView(R.layout.activity_custom_dialog_friend_request)

        val window = window
        window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )

        val titleTextView = findViewById<TextView>(R.id.tvDialogTitle)
        val closeButton = findViewById<ImageButton>(R.id.btnClose)
        val cancelButton = findViewById<Button>(R.id.btnDecline)
        val confirmButton = findViewById<Button>(R.id.btnConfirm)
        val additionalInfoTextView = findViewById<TextView>(R.id.tvAdditionalInfo)

        titleTextView.text = text
        additionalInfoTextView.text = additional


        cancelButton.setOnClickListener {
            confirmCallback("cancel")
            dismiss()
        }

        confirmButton.setOnClickListener {
            confirmCallback("confirm")
            dismiss()
        }

        closeButton.setOnClickListener {
            confirmCallback("close")
            dismiss()
        }


    }

}