package com.example.upangchattingapp.loginandregister

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.view.WindowManager
import android.widget.ArrayAdapter
import android.widget.Toast
import com.example.upangchattingapp.ChecksActiveStatus
import com.example.upangchattingapp.Interests
import com.example.upangchattingapp.MainActivity
import com.example.upangchattingapp.R
import com.example.upangchattingapp.databinding.ActivityGettingStartedBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class GettingStarted : AppCompatActivity() {

    private lateinit var binding : ActivityGettingStartedBinding
    private lateinit var auth : FirebaseAuth

    private var backPressedTime: Long = 0

    private var isCourseValid = false
    private var isYearLevelValid = false

    private var isCourseSubmitted = false
    private var isYearLevelSubmitted = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGettingStartedBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        auth = FirebaseAuth.getInstance()




        binding.apply {

            btnProceed.setOnClickListener {
                val user = auth.currentUser


                if (courseSelect.text.isEmpty()) {
                    tvCourseError.visibility = VISIBLE
                    isCourseValid = false
                } else{
                    tvCourseError.visibility = INVISIBLE

                    isCourseValid = true
                }

                if (yearLevelSelect.text.isEmpty()){
                    tvYearLevelError.visibility = VISIBLE
                    isYearLevelValid = false
                } else {
                    tvYearLevelError.visibility = INVISIBLE
                    isYearLevelValid = true
                }

                if (isCourseValid && isYearLevelValid){
                    progressBar.visibility = VISIBLE
                    this@GettingStarted.window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                    val userUid = user?.uid

                    val database = FirebaseDatabase.getInstance().reference
                    val usersReference = database.child("user")
                    if (userUid != null) {
                        val userReference = usersReference.child(userUid)

                        userReference.child("course").setValue(courseSelect.text.toString())
                            .addOnSuccessListener {
                                isCourseSubmitted = true

                            }
                            .addOnFailureListener {
                                isCourseSubmitted = false
                            }
                        userReference.child("yearLevel").setValue(yearLevelSelect.text.toString())
                            .addOnSuccessListener {
                                isYearLevelSubmitted = true

                            }
                            .addOnFailureListener {
                                isYearLevelSubmitted = false
                            }


                        val intent = Intent(this@GettingStarted, Interests::class.java)
                        intent.putExtra("mode", "start")
                        startActivity(intent)
                        progressBar.visibility = INVISIBLE
                        this@GettingStarted.window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                    }
                }
            }
        }



    }

    override fun onResume() {
        super.onResume()

        val courses = resources.getStringArray(R.array.courses)
        val arrayCourseAdapter = ArrayAdapter(this, R.layout.dropdown_courses, courses)
        binding.courseSelect.setAdapter(arrayCourseAdapter)

        val yearLevel = resources.getStringArray(R.array.YearLevel)
        val arrayYearLevelAdapter = ArrayAdapter(this, R.layout.dropdown_courses, yearLevel)
        binding.yearLevelSelect.setAdapter(arrayYearLevelAdapter)
    }

    override fun onBackPressed() {

        if(backPressedTime + 3000 > System.currentTimeMillis()) {
            auth.signOut()
            startActivity(Intent(this, LogInAndRegister::class.java))
            stopService(Intent(this@GettingStarted, ChecksActiveStatus::class.java))
            finish()
        } else {
            Toast.makeText(baseContext, "Press back again to sign out.", Toast.LENGTH_SHORT).show()
        }
        backPressedTime= System.currentTimeMillis()
    }

}