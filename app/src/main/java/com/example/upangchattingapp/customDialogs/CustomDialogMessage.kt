package com.example.upangchattingapp.customDialogs

import android.app.Dialog
import android.content.Context
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.TextView
import androidx.cardview.widget.CardView
import com.example.upangchattingapp.R

class CustomDialogMessage(context: Context, user : String, private val confirmCallback: (String) -> Unit) : Dialog(context) {

    init {
        setContentView(R.layout.activity_custom_dialog_message)

        val remove = findViewById<CardView>(R.id.cvCustomDialogRemove)
        val report = findViewById<CardView>(R.id.cvCustomDialogReport)
        val removeText = findViewById<TextView>(R.id.tvCustomDialogRemove)

        if (user == "current"){
            remove.visibility = VISIBLE
            report.visibility = GONE
        } else if (user == "both") {
            removeText.text = "Delete Conversation"
            remove.visibility = VISIBLE
            report.visibility = VISIBLE
        } else {
            report.visibility = VISIBLE
            remove.visibility = GONE
        }

        remove.setOnClickListener{
            confirmCallback("remove")
            dismiss()
        }

        report.setOnClickListener {
            confirmCallback("report")
            dismiss()
        }
    }
}