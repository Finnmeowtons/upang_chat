package com.example.upangchattingapp.loginandregister

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.upangchattingapp.TermsServicesAndPrivacyPolicy
import com.example.upangchattingapp.customDialogs.CustomDialogRegister
import com.example.upangchattingapp.databinding.ActivityRegisterBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase


class Register : AppCompatActivity() {
    private lateinit var binding : ActivityRegisterBinding
    private lateinit var auth : FirebaseAuth

    private lateinit var email : String
    private lateinit var password : String
    private lateinit var fullName : String

    private var isFullNameValid = false
    private var isEmailValid = false
    private var isPasswordValid = false
    private var isConfirmPasswordValid = false

    private var backPressedTime: Long = System.currentTimeMillis()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        auth = FirebaseAuth.getInstance()


        binding.apply {

            tvRegisterTermsAndPolicy.setOnClickListener {

                startActivity(Intent(this@Register, TermsServicesAndPrivacyPolicy::class.java))

            }

            tvHaveAnAccount.setOnClickListener {
                startActivity(Intent(this@Register, LogInAndRegister::class.java))
            }

            btnSignUp.setOnClickListener{

                //Full Name
                if(etFullNameRegister.text.isEmpty()){
                    tvFullNameError.text = "Name is empty!"
                    tvFullNameError.visibility = VISIBLE
                    isFullNameValid = false
                } else if(etFullNameRegister.text.length < 5){
                    tvFullNameError.text = "It should consist of at least 6 characters!"
                    tvFullNameError.visibility = VISIBLE
                    isFullNameValid = false
                } else {
                    tvFullNameError.visibility = INVISIBLE
                    fullName = etFullNameRegister.text.toString().trim()
                    isFullNameValid = true
                }

                //Email
                if(etEmailRegister.text.isEmpty()){
                    tvEmailError.text = "Email is empty!"
                    tvEmailError.visibility = VISIBLE
                    isEmailValid = false
                } else if (!etEmailRegister.text.contains("@") || !etEmailRegister.text.contains(".com")){
                    tvEmailError.text = "Invalid Email"
                    tvEmailError.visibility = VISIBLE
                    isEmailValid = false

//                }else if(!etEmailRegister.text.contains(".up@phinmaed.com")){
//                    tvEmailError.text = "Only UPANG accounts only!"
//                    tvEmailError.visibility = VISIBLE
//                    isEmailValid = false
                } else {
                    tvEmailError.visibility = INVISIBLE
                    email = etEmailRegister.text.toString().lowercase().trim()
                    isEmailValid = true
                }

                //Password
                if(etPasswordSignUp.text.trim().isEmpty()){
                    tvPasswordError.text = "Password is empty!"
                    tvPasswordError.visibility = VISIBLE
                    isPasswordValid = false
                }
                else if(etPasswordSignUp.text.length <= 7){
                    tvPasswordError.text = "It should consist of at least 8 characters!"
                    tvPasswordError.visibility = VISIBLE
                    isPasswordValid = false
                } else {
                    tvPasswordError.visibility = INVISIBLE
                    isPasswordValid = true
                }

                if(etPasswordConfirmation.text.trim().isEmpty()){
                    tvPasswordConfirmationError.text = "Password field is empty!"
                    tvPasswordConfirmationError.visibility = VISIBLE
                    isConfirmPasswordValid = false
                } else if(etPasswordConfirmation.text.toString().trim() != etPasswordSignUp.text.toString().trim()){
                    tvPasswordConfirmationError.text = "Password does not match!"
                    tvPasswordConfirmationError.visibility = VISIBLE
                    isConfirmPasswordValid = false
                } else {
                    password = etPasswordConfirmation.text.toString()
                    tvPasswordConfirmationError.visibility = INVISIBLE
                    isConfirmPasswordValid = true
                }
                Log.e("MyTag", "$isEmailValid, $isFullNameValid, $isPasswordValid, $isConfirmPasswordValid")

                if (isEmailValid && isFullNameValid && isPasswordValid && isConfirmPasswordValid) {

                    val dialog = CustomDialogRegister(this@Register){action ->

                        if(action == "confirm"){
                            progressBar.visibility = VISIBLE
                            this@Register.window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                            auth.createUserWithEmailAndPassword(email, password)
                                .addOnCompleteListener(this@Register) { task ->
                                    progressBar.visibility = INVISIBLE
                                    this@Register.window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                                    if (task.isSuccessful) {
                                        val userUid = auth.currentUser?.uid.toString()
                                        val myRef = Firebase.database.getReference("unverifiedUser").child(userUid)
                                        myRef.child("userName").setValue(fullName)
                                        myRef.child("email").setValue(email)

                                        startActivity(Intent(this@Register, LogInAndRegister::class.java))
                                        Firebase.auth.signOut()
                                    } else {
                                        if (task.exception is FirebaseAuthUserCollisionException){
                                            Toast.makeText(
                                                this@Register,
                                                "Account already exists.",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }else {
                                            // If sign in fails, display a message to the user.
                                            Toast.makeText(
                                                this@Register,
                                                "Authentication failed.",
                                                Toast.LENGTH_SHORT,
                                            ).show()
                                        }
                                    }
                                }
                        }
                    }
                    dialog.show()

                }
            }
        }
    }

    override fun onBackPressed() {

        startActivity(Intent(this, LogInAndRegister::class.java))

    }


}