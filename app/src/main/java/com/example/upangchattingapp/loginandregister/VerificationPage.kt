package com.example.upangchattingapp.loginandregister

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.view.WindowManager
import android.widget.Toast
import com.example.upangchattingapp.ChecksActiveStatus
import com.example.upangchattingapp.MainActivity
import com.example.upangchattingapp.databinding.ActivityVerificationPageBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.Timer
import java.util.TimerTask

class VerificationPage : AppCompatActivity() {
    private lateinit var binding : ActivityVerificationPageBinding
    private lateinit var auth : FirebaseAuth
    private var isResendEnabled = true
    private lateinit var countdownTimer: CountDownTimer
    private var timerDuration  = 300000L
    private lateinit var timer: Timer

    private var backPressedTime: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVerificationPageBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        auth = FirebaseAuth.getInstance()

        val user : FirebaseUser? = auth.currentUser


        binding.apply {
            btnResend.setOnClickListener {
                btnResend.text = "Resend verification email"
                binding.progressBar.visibility = VISIBLE
                this@VerificationPage.window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                if (isResendEnabled) {
                    user?.let {
                        user.sendEmailVerification()
                            .addOnCompleteListener { task ->
                                binding.progressBar.visibility = INVISIBLE
                                this@VerificationPage.window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                                if (task.isSuccessful) {


                                    Toast.makeText(this@VerificationPage, "Verification email sent", Toast.LENGTH_SHORT).show()
                                    timerDuration = 300000



                                } else {
                                    Toast.makeText(this@VerificationPage, "Failed to send verification email", Toast.LENGTH_SHORT).show()
                                    timerDuration = 3000
                                }
                            }
                    }
                    isResendEnabled = false
                    btnResend.isEnabled = false

                    // Start the countdown timer
                    countdownTimer = object : CountDownTimer(timerDuration, 1000) {
                        override fun onTick(millisUntilFinished: Long) {
                            val minutes = millisUntilFinished / 60000
                            val seconds = (millisUntilFinished % 60000) / 1000
                            val formattedTime = String.format("%d:%02d", minutes, seconds)
                            tvCountDown.text = "Resend available in $formattedTime"
                        }

                        override fun onFinish() {
                            isResendEnabled = true
                            btnResend.isEnabled = true
                            tvCountDown.text = "" // Reset the countdown text
                        }
                    }.start()
                } else{
                    Toast.makeText(this@VerificationPage, "Please wait before resending", Toast.LENGTH_SHORT).show()
                }
            }
        }
        startVerificationCheck()
    }

    override fun onDestroy() {
        super.onDestroy()
        // Cancel the countdown timer to avoid memory leaks
        countdownTimer.cancel()
        timer.cancel()
    }
    private fun startVerificationCheck() {
        Log.e("MyTag", "Hello")

        timer = Timer()
        timer.scheduleAtFixedRate(object : TimerTask() {

            override fun run() {
                auth = FirebaseAuth.getInstance()
                val user= FirebaseAuth.getInstance().currentUser
                Log.e("MyTag", user?.uid.toString())
                Log.e("MyTag", user?.isEmailVerified.toString())
                user?.reload()
                if (user?.isEmailVerified == true) {
                    Log.e("MyTag", "Verified")
                    // User is verified, proceed to next screen
                    val handler = Handler(Looper.getMainLooper())
                    handler.post {
                        binding.progressBar.visibility = VISIBLE
                        this@VerificationPage.window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                        Toast.makeText(
                            this@VerificationPage,
                            "Account Verified",
                            Toast.LENGTH_SHORT
                        ).show()
                        timer.cancel()

                        val reference = FirebaseDatabase.getInstance().reference
                        val unverifiedUserRef = reference.child("unverifiedUser").child(user.uid)
                        val userRef = reference.child("user").child(user.uid)
                        unverifiedUserRef.addListenerForSingleValueEvent(object :
                            ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                // Retrieve the user details from "unverifiedUser" node
                                val userName =
                                    snapshot.child("userName").getValue(String::class.java)
                                val email = snapshot.child("email").getValue(String::class.java)

                                // Move the user details to "user" node
                                if (userName != null && email != null) {
                                    userRef.child("userName").setValue(userName)
                                    userRef.child("email").setValue(email)
                                    userRef.child("userID").setValue(FirebaseAuth.getInstance().currentUser?.uid.toString())

                                    unverifiedUserRef.removeValue()
                                }
                                startActivity(
                                    Intent(
                                        this@VerificationPage,
                                        MainActivity::class.java
                                    )
                                )
                                binding.progressBar.visibility = INVISIBLE
                                this@VerificationPage.window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                                finish()
                            }

                            override fun onCancelled(error: DatabaseError) {
                                TODO("Not yet implemented")
                            }

                        })
                    }
                }
            }
        }, 0, 3000) // Check every 3 seconds
    }

    override fun onBackPressed() {

        if(backPressedTime + 3000 > System.currentTimeMillis()) {
            finish()
            startActivity(Intent(this, LogInAndRegister::class.java))
            stopService(Intent(this@VerificationPage, ChecksActiveStatus::class.java))
            auth.signOut()

        } else {
            Toast.makeText(baseContext, "Press back again to sign out.", Toast.LENGTH_SHORT).show()
        }
        backPressedTime= System.currentTimeMillis()
    }
}