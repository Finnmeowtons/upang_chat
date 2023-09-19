package com.example.upangchattingapp.loginandregister

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.view.WindowManager
import android.widget.Toast
import androidx.core.app.ActivityCompat.finishAffinity
import com.example.upangchattingapp.MainActivity
import com.example.upangchattingapp.User
import com.example.upangchattingapp.databinding.ActivityLogInBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class LogInAndRegister : AppCompatActivity() {

    private lateinit var binding : ActivityLogInBinding
    private lateinit var auth : FirebaseAuth

    private lateinit var email : String
    private lateinit var password : String

    private var isEmailValid = false
    private var isPasswordValid = false

    private var backPressedTime: Long = 0


//    private val REQ_ONE_TAP = 2  // Can be any integer unique to the Activity
//    private var showOneTapUI = true
//    private lateinit var oneTapClient: SignInClient
//    private lateinit var signInRequest: BeginSignInRequest

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLogInBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        auth = FirebaseAuth.getInstance()



        val firebaseUser = auth.currentUser
        if (firebaseUser != null) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }







        binding.apply {

            btnForgetPassword.setOnClickListener {
                email = etEmailSignIn.text.toString().trim()

                if (email.isNotEmpty()) {
                        auth.sendPasswordResetEmail(email)
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    Toast.makeText(
                                        this@LogInAndRegister,
                                        "Password reset email sent to $email",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                } else {
                                    Toast.makeText(
                                        this@LogInAndRegister,
                                        "Failed to send password reset email",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }

                } else {
                    Toast.makeText(
                        this@LogInAndRegister,
                        "Please enter your email address",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            btnLogIn.setOnClickListener {

                if (etEmailSignIn.text.isEmpty()) {
                    tvEmailError.text = "Email is empty!"
                    tvEmailError.visibility = VISIBLE
                    isEmailValid = false
                } else {
                    isEmailValid = true
                    email = etEmailSignIn.text.toString().trim().lowercase()
                    tvEmailError.visibility = INVISIBLE
                }

                if (etPasswordSignIn.text.isEmpty()) {
                    tvPasswordError.text = "Password is empty!"
                    tvPasswordError.visibility = VISIBLE
                    isPasswordValid = false
                } else {
                    isPasswordValid = true
                    password = etPasswordSignIn.text.toString()
                    tvPasswordError.visibility = INVISIBLE
                }

                if (isEmailValid && isPasswordValid) {
                    progressBar.visibility = VISIBLE
                    this@LogInAndRegister.window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                    auth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(this@LogInAndRegister) { task ->
                            progressBar.visibility = INVISIBLE
                            this@LogInAndRegister.window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                            if (task.isSuccessful) {

                                etEmailSignIn.setText("")
                                etPasswordSignIn.setText("")

                                val reference = FirebaseDatabase.getInstance().reference
                                val currentUserKey = FirebaseAuth.getInstance().currentUser?.uid

                                reference.child("deactivatedUser").child(currentUserKey.toString()).addListenerForSingleValueEvent(object : ValueEventListener{
                                    override fun onDataChange(snapshot: DataSnapshot) {
                                        if (snapshot.exists()){
                                            reference.child("deactivatedUser").child(currentUserKey.toString())
                                                .addListenerForSingleValueEvent(object :
                                                    ValueEventListener {
                                                    override fun onDataChange(snapshot: DataSnapshot) {
                                                        val info =
                                                            snapshot.getValue(User::class.java)
                                                        val email = info?.email
                                                        val userID = info?.userID
                                                        val userName = info?.userName


                                                        val reactivatedUser =
                                                            reference.child("user")
                                                                .child(userID.toString())

                                                        reactivatedUser.child("email").setValue(email)
                                                        reactivatedUser.child("userID").setValue(userID)
                                                        reactivatedUser.child("userName")
                                                            .setValue(userName).addOnSuccessListener {
                                                                startActivity(
                                                                    Intent(
                                                                        this@LogInAndRegister,
                                                                        GettingStarted::class.java
                                                                    )
                                                                )
                                                            }

                                                        reference.child("deactivatedUser")
                                                            .child(currentUserKey.toString())
                                                            .removeValue()
                                                    }

                                                    override fun onCancelled(error: DatabaseError) {
                                                        TODO("Not yet implemented")
                                                    }
                                                })

                                            val friendsRef = reference.child("deactivatedUser").child(currentUserKey.toString()).child("Friends")
                                            friendsRef.addListenerForSingleValueEvent(object : ValueEventListener {
                                                override fun onDataChange(snapshot: DataSnapshot) {
                                                    for (friendSnapshot in snapshot.children) {
                                                        val friendKey = friendSnapshot.key
                                                        val friendData = friendSnapshot.value

                                                        val deactivatedUserFriendsRef =
                                                            reference.child("user").child(currentUserKey.toString()).child("Friends")
                                                        if (friendKey != null) {
                                                            deactivatedUserFriendsRef.child(friendKey).setValue(friendData)
                                                        }

                                                        friendSnapshot.ref.removeValue()
                                                    }
                                                }

                                                override fun onCancelled(error: DatabaseError) {
                                                    TODO("Not yet implemented")
                                                }
                                            })

                                            val incomingFriendsRef = reference.child("deactivatedUser").child(currentUserKey.toString()).child("IncomingFriendRequests")
                                            incomingFriendsRef.addListenerForSingleValueEvent(object : ValueEventListener {
                                                override fun onDataChange(snapshot: DataSnapshot) {
                                                    for (friendSnapshot in snapshot.children) {
                                                        val friendKey = friendSnapshot.key
                                                        val friendData = friendSnapshot.value

                                                        val deactivatedUserFriendsRef =
                                                            reference.child("user").child(currentUserKey.toString()).child("IncomingFriendRequests")
                                                        if (friendKey != null) {
                                                            deactivatedUserFriendsRef.child(friendKey).setValue(friendData)
                                                        }

                                                        friendSnapshot.ref.removeValue()
                                                    }
                                                }

                                                override fun onCancelled(error: DatabaseError) {
                                                    TODO("Not yet implemented")
                                                }
                                            })

                                            val sentFriendsRef = reference.child("deactivatedUser").child(currentUserKey.toString()).child("SentFriendRequests")
                                            sentFriendsRef.addListenerForSingleValueEvent(object : ValueEventListener {
                                                override fun onDataChange(snapshot: DataSnapshot) {
                                                    for (friendSnapshot in snapshot.children) {
                                                        val friendKey = friendSnapshot.key
                                                        val friendData = friendSnapshot.value

                                                        val deactivatedUserFriendsRef =
                                                            reference.child("user").child(currentUserKey.toString()).child("SentFriendRequests")
                                                        if (friendKey != null) {
                                                            deactivatedUserFriendsRef.child(friendKey).setValue(friendData)
                                                        }

                                                        friendSnapshot.ref.removeValue()
                                                    }
                                                }

                                                override fun onCancelled(error: DatabaseError) {
                                                    TODO("Not yet implemented")
                                                }
                                            })

                                            val conversations = reference.child("deactivatedUser").child(currentUserKey.toString()).child("conversations")
                                            conversations.addListenerForSingleValueEvent(object : ValueEventListener {
                                                override fun onDataChange(snapshot: DataSnapshot) {
                                                    for (convoSnapshot in snapshot.children) {
                                                        val friendKey = convoSnapshot.key
                                                        val friendData = convoSnapshot.value

                                                        val deactivatedUserFriendsRef =
                                                            reference.child("user").child(currentUserKey.toString()).child("conversations")
                                                        if (friendKey != null) {
                                                            deactivatedUserFriendsRef.child(friendKey).setValue(friendData)
                                                        }

                                                        convoSnapshot.ref.removeValue()
                                                    }
                                                }

                                                override fun onCancelled(error: DatabaseError) {
                                                    TODO("Not yet implemented")
                                                }
                                            })


                                        } else {
                                            val user: FirebaseUser? = auth.currentUser

                                            //Check if email is verified
                                            if (user != null && user.isEmailVerified) {
                                                // Email is verified, proceed to main activity
                                                startActivity(
                                                    Intent(
                                                        this@LogInAndRegister,
                                                        MainActivity::class.java
                                                    )
                                                )
                                            } else if (user != null && !user.isEmailVerified) {
                                                startActivity(
                                                    Intent(
                                                        this@LogInAndRegister,
                                                        VerificationPage::class.java
                                                    )
                                                )
                                            } else {
                                                // If sign in fails, display a message to the user.
                                                Toast.makeText(
                                                    baseContext,
                                                    "Please try again.",
                                                    Toast.LENGTH_SHORT,
                                                ).show()
                                            }
                                        }
                                    }

                                    override fun onCancelled(error: DatabaseError) {
                                        TODO("Not yet implemented")
                                    }
                                })


                            } else {
                                // If sign in fails, display a message to the user.
                                Toast.makeText(
                                    baseContext,
                                    "Invalid email and password.",
                                    Toast.LENGTH_SHORT,
                                ).show()
                            }
                        }
                }

                }

                tvNoAccountRegister.setOnClickListener {
                    startActivity(Intent(this@LogInAndRegister, Register::class.java))
                }
            }
        }





    override fun onBackPressed() {

        if(backPressedTime + 3000 > System.currentTimeMillis()) {
            super.onBackPressed()
            finishAffinity()
        } else {
            Toast.makeText(baseContext, "Press back again to exit app.", Toast.LENGTH_SHORT).show()
        }
        backPressedTime= System.currentTimeMillis()
    }

}