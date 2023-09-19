package com.example.upangchattingapp.customDialogs

import android.app.Dialog
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import com.example.upangchattingapp.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import java.text.SimpleDateFormat
import java.util.*

class CustomDialogReport(context : Context, userID : String) : Dialog(context) {

    private val handler = Handler()
    private val closeDialog = Runnable { dismiss() }

    init {
        setContentView(R.layout.activity_custom_dialog_report)

        val close = findViewById<ImageButton>(R.id.btnClose)

        val submit = findViewById<Button>(R.id.btnSubmitReport)
        val report = findViewById<EditText>(R.id.etReport)

        val check = findViewById<ImageView>(R.id.imgReportCheck)
        val success = findViewById<TextView>(R.id.tvSuccessfullySubmitted)

        val layout = findViewById<LinearLayout>(R.id.llReport)

        close.setOnClickListener {

            dismiss()

        }

        submit.setOnClickListener {

            val currentUserKey = FirebaseAuth.getInstance().currentUser?.uid.toString()
            val reference = FirebaseDatabase.getInstance().reference
            val parentKey = reference.child("Report").push().key

            val timestamp =
                SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())

            reference.child("Report").child(parentKey.toString()).child("timeStamp").setValue(timestamp)
            reference.child("Report").child(parentKey.toString()).child("report").setValue(report.text.toString())
            reference.child("Report").child(parentKey.toString()).child("reportedUser").setValue(userID)
            reference.child("Report").child(parentKey.toString()).child("reporter").setValue(currentUserKey).addOnCompleteListener { task ->

                if (task.isSuccessful){
                    layout.visibility = GONE

                    check.visibility = VISIBLE
                    success.visibility = VISIBLE

                    handler.postDelayed(closeDialog,3000)
                }

            }
        }
    }

    override fun dismiss() {
        handler.removeCallbacks(closeDialog)
        super.dismiss()
    }
}