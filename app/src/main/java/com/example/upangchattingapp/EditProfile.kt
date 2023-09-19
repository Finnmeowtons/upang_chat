package com.example.upangchattingapp

import android.content.Intent
import android.icu.lang.UCharacter.GraphemeClusterBreak.L
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import com.example.upangchattingapp.customDialogs.CustomDialogFriendRequest
import com.example.upangchattingapp.databinding.ActivityEditProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale


class EditProfile : AppCompatActivity() {

    private lateinit var binding : ActivityEditProfileBinding
    private var isUserNameChanged = false
    private var isCourseChanged = false
    private var isYearLevelChanged = false

    private var isUserNameExpired = true
    private var isCourseExpired = true
    private var isYearLevelExpired = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditProfileBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        val currentUserKey = FirebaseAuth.getInstance().currentUser?.uid.toString()
        val reference = FirebaseDatabase.getInstance().reference

        val userNameChange = "Please note that you can only change your name once every 1 month.\n\n"
        val yearLevelChange = "Please note that you can only change your year level once every 3 months.\n\n"
        val courseChange = "Please note that you can only change your course once every 3 months.\n\n"

        val userNameCooldownNotExpired = "You recently changed your user name. Please wait before making another change.\n\n"
        val courseCooldownNotExpired = "You recently changed your course. Please wait before making another change.\n\n"
        val yearLevelCooldownNotExpired = "You recently changed your year level. Please wait before making another change.\n\n"

        binding.apply{

            etBiography.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable) {}
                override fun beforeTextChanged(q: CharSequence, s: Int, c: Int, a: Int) {}
                override fun onTextChanged(q: CharSequence, s: Int, b: Int, c: Int) {
                    Log.d("MyTag", " LINES = " + etBiography.lineCount)
                    val L = etBiography.lineCount
                    if (L > 4) {
                        etBiography.text
                            .delete(etBiography.selectionEnd - 1, etBiography.selectionStart)
                    }
                }
            })

            clEditInterest.setOnClickListener {
                startActivity(Intent(this@EditProfile, Interests::class.java))
            }

            reference.child("user").child(currentUserKey).addListenerForSingleValueEvent(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    val userInfo = snapshot.getValue(User::class.java)

                    etFullNameRegister.setText(userInfo?.userName.toString().lowercase(Locale.getDefault()).split(" ").joinToString(" ") { it.capitalize(
                        Locale.ROOT) })
                    etCourseEdit.setText("")
                    etYearLevelEdit.setText("")
                    etBiography.setText(userInfo?.aboutMe)

                    etCourseEdit.hint = userInfo?.course
                    etYearLevelEdit.hint = userInfo?.yearLevel
                    etFullNameRegister.hint = "Full Name"



                    btnConfirm.setOnClickListener {


                        reference.child("user").child(currentUserKey).child("aboutMe").setValue(etBiography.text.toString().trim())


                        val messages = StringBuilder()


                        //UserName
                        if (etFullNameRegister.text.length < 6) {

                            tvFullNameError.text = "It should consist of at least 6 characters!"
                            tvFullNameError.visibility = VISIBLE
                            isUserNameChanged = false

                        }else if (userInfo?.userName.toString().lowercase(Locale.getDefault()).split(" ").joinToString(" ") { it.capitalize(
                                Locale.ROOT) } == etFullNameRegister.text.toString() || userInfo?.userName == etFullNameRegister.text.toString()){

                            tvFullNameError.visibility = INVISIBLE
                            isUserNameChanged = false

                        } else { //TRUE

                            val userNameCooldown = snapshot.child("ChangeInformationCooldown").child("userNameCooldown").getValue(String::class.java)
                            isUserNameChanged = true
                            tvFullNameError.visibility = INVISIBLE
                            if (isCooldownExpired(userNameCooldown, 1)) {
                                Log.e("MyTag", "Expired")
                                messages.append(userNameChange)
                            } else {
                                messages.append(userNameCooldownNotExpired)
                                Log.e("MyTag", "notExpired")
                            }
                        }

                        //Course
                        if (etCourseEdit.text.isEmpty() || etCourseEdit.text.toString() == userInfo?.course){

                            isCourseChanged = false

                        } else if (etCourseEdit.text.toString() != userInfo?.course) { //TRUE

                            isCourseChanged = true
                            val courseCooldown = snapshot.child("ChangeInformationCooldown").child("courseCooldown").getValue(String::class.java)
                            if (isCooldownExpired(courseCooldown, 3)) {

                                messages.append(courseChange)


                            } else {
                                messages.append(courseCooldownNotExpired)
                            }


                        } else {
                            isCourseChanged = false
                        }

                        //Year Level
                        if (etYearLevelEdit.text.isEmpty() || etYearLevelEdit.text.toString() == userInfo?.yearLevel){

                            isYearLevelChanged = false

                        } else if (etYearLevelEdit.text.toString() != userInfo?.yearLevel) { //TRUE

                            val yearLevelCooldown = snapshot.child("ChangeInformationCooldown").child("yearLevelCooldown").getValue(String::class.java)
                            isYearLevelChanged = true

                            if (isCooldownExpired(yearLevelCooldown, 3)) {
                                messages.append(yearLevelChange)
                            } else {
                                messages.append(yearLevelCooldownNotExpired)

                            }


                        } else {
                            isYearLevelChanged = false
                        }

                        if (isYearLevelChanged || isCourseChanged || isUserNameChanged) {

                            Log.e("MyTag", "${etYearLevelEdit.text.toString()}, ${etCourseEdit.text.toString()}, ${etFullNameRegister.text.toString()}")
                            val dialog = CustomDialogFriendRequest(
                                this@EditProfile,
                                "Are you sure about the changes?",
                                messages.toString()
                            ) { action ->
                                when (action) {
                                    "confirm" -> {

                                        isUserNameExpired = false
                                        isCourseExpired = false
                                        isYearLevelExpired = false

                                        val timestamp =
                                            SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(
                                                Date()
                                            )

                                        reference.child("user").child(currentUserKey).addListenerForSingleValueEvent(object : ValueEventListener{
                                            override fun onDataChange(snapshot: DataSnapshot) {
                                                val userNameCooldown = snapshot.child("ChangeInformationCooldown").child("userNameCooldown").getValue(String::class.java)
                                                if (isUserNameChanged) {
                                                    if (isCooldownExpired(userNameCooldown, 1)) {
                                                        reference.child("user").child(currentUserKey).child("userName").setValue(etFullNameRegister.text.toString().lowercase(Locale.getDefault()).split(" ").joinToString(" ") { it.capitalize(
                                                            Locale.ROOT) })
                                                        reference.child("user").child(currentUserKey).child("ChangeInformationCooldown").child("userNameCooldown").setValue(timestamp)
                                                    }
                                                }

                                                val courseCooldown = snapshot.child("ChangeInformationCooldown").child("courseCooldown").getValue(String::class.java)
                                                if (isCourseChanged) {
                                                    if (isCooldownExpired(courseCooldown, 3)) {
                                                        reference.child("user").child(currentUserKey).child("course").setValue(etCourseEdit.text.toString())
                                                        reference.child("user").child(currentUserKey).child("ChangeInformationCooldown").child("courseCooldown").setValue(timestamp)
                                                    }
                                                }

                                                val yearLevelCooldown = snapshot.child("ChangeInformationCooldown").child("yearLevelCooldown").getValue(String::class.java)
                                                if (isYearLevelChanged) {
                                                    if (isCooldownExpired(yearLevelCooldown, 3)) {
                                                        reference.child("user").child(currentUserKey).child("yearLevel").setValue(etYearLevelEdit.text.toString())
                                                        reference.child("user").child(currentUserKey).child("ChangeInformationCooldown").child("yearLevelCooldown").setValue(timestamp)
                                                    }
                                                }

                                                val intent = Intent(this@EditProfile, MainActivity::class.java)
                                                intent.putExtra("fragment", 3)
                                                startActivity(intent)

                                            }

                                            override fun onCancelled(error: DatabaseError) {
                                                TODO("Not yet implemented")
                                            }
                                        })



                                    }

                                    "decline" -> {


                                    }
                                }
                            }
                            dialog.show()
                        }
                    }

                    btnDecline.setOnClickListener {
                        etFullNameRegister.setText(userInfo?.userName.toString().lowercase(Locale.getDefault()).split(" ").joinToString(" ") { it.capitalize(
                            Locale.ROOT) })
                        etCourseEdit.setText("")
                        etYearLevelEdit.setText("")
                        etBiography.setText(userInfo?.aboutMe)

                        etCourseEdit.hint = userInfo?.course
                        etYearLevelEdit.hint = userInfo?.yearLevel
                        etFullNameRegister.hint = "Full Name"

                        tvFullNameError.visibility = INVISIBLE
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }
            })


            btnBackEditProfile.setOnClickListener {
                val intent = Intent(this@EditProfile, MainActivity::class.java)
                intent.putExtra("fragment", 3)
                startActivity(intent)
            }


        }
    }

    override fun onResume() {
        super.onResume()

        val courses = resources.getStringArray(R.array.courses)
        val arrayCourseAdapter = ArrayAdapter(this, R.layout.dropdown_courses, courses)
        binding.etCourseEdit.setAdapter(arrayCourseAdapter)



        val yearLevel = resources.getStringArray(R.array.YearLevel)
        val arrayYearLevelAdapter = ArrayAdapter(this, R.layout.dropdown_courses, yearLevel)
        binding.etYearLevelEdit.setAdapter(arrayYearLevelAdapter)
    }

    // Function to check if the cooldown period has expired
    private fun isCooldownExpired(cooldownTimestamp: String?, cooldownDurationMonths: Int): Boolean {
        if (!cooldownTimestamp.isNullOrEmpty()) {
            val cooldownDate = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).parse(cooldownTimestamp)
            val currentDate = Date()

            val calendar = Calendar.getInstance()
            if (cooldownDate != null) {
                calendar.time = cooldownDate
            }
            calendar.add(Calendar.MONTH, cooldownDurationMonths)

            return currentDate.after(calendar.time)
        }
        return true // expired
    }
}