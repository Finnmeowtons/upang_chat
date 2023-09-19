package com.example.upangchattingapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View.GONE
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.upangchattingapp.databinding.ActivityReauthenticateBinding
import com.example.upangchattingapp.loginandregister.LogInAndRegister
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener


class Reauthenticate : AppCompatActivity() {

    private lateinit var binding : ActivityReauthenticateBinding
    private val auth = FirebaseAuth.getInstance()
    private val reference = FirebaseDatabase.getInstance().reference
    private val currentUserKey = auth.currentUser?.uid
    private lateinit var userID : String
    private lateinit var userName : String

    private var isEmailValid = false
    private var isPasswordValid = false
    private var isNewPasswordValid = false



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReauthenticateBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        val mode = intent.getIntExtra("mode", 1)
        val userEmail = intent.getStringExtra("email")

        binding.apply {

            if (mode == 1) { //Delete Account

                textInputLayout.visibility = GONE
                etPasswordNewPassword.visibility = GONE
                tvPasswordNewError.visibility = GONE
                etPasswordReAuthenticate.hint = "Password"

                btnAccountDeleteConfirm.setOnClickListener {

                    val emailEditText = etEmailReAuthenticate.text.toString()
                    val passwordEditText = etPasswordReAuthenticate.text.toString()

                    //Email
                    if (emailEditText == userEmail) {

                        progressBar.visibility = VISIBLE

                        isEmailValid = true
                        tvEmailError.visibility = INVISIBLE

                    } else if (etEmailReAuthenticate.text.isEmpty()) {

                        isEmailValid = false
                        tvEmailError.text = "Email is empty!"
                        tvEmailError.visibility = VISIBLE

                    } else {

                        isEmailValid = false
                        tvEmailError.text = "Incorrect email"
                        tvEmailError.visibility = VISIBLE

                    }

                    //Password
                    if (etPasswordReAuthenticate.text.isEmpty()) {
                        tvPasswordError.text = "Password is empty!"
                        tvPasswordError.visibility = VISIBLE
                        isPasswordValid = false
                    } else {
                        isPasswordValid = true
                        tvPasswordError.visibility = INVISIBLE
                    }



                    if (isEmailValid && isPasswordValid) {
                        val credential =
                            EmailAuthProvider.getCredential(emailEditText, passwordEditText)

                        auth.currentUser?.reauthenticate(credential)
                            ?.addOnCompleteListener { task1 ->

                                if (task1.isSuccessful) {

                                    auth.currentUser!!.delete().addOnCompleteListener { task ->
                                        if (task.isSuccessful) {


                                            val usersRef = reference.child("user")
                                            usersRef.addListenerForSingleValueEvent(object :
                                                ValueEventListener {
                                                override fun onDataChange(snapshot: DataSnapshot) {
                                                    val userIDs = mutableListOf<String>()
                                                    for (userSnapshot in snapshot.children) {
                                                        val userID = userSnapshot.key
                                                        if (userID != currentUserKey) {
                                                            userIDs.add(userID.toString())
                                                        }
                                                    }



                                                    for (userID in userIDs) {
                                                        reference.child("user").child(userID)
                                                            .child("Friends")
                                                            .child(currentUserKey.toString())
                                                            .removeValue()
                                                        reference.child("user").child(userID)
                                                            .child("IncomingFriendRequests")
                                                            .child(currentUserKey.toString())
                                                            .removeValue()
                                                        reference.child("user").child(userID)
                                                            .child("SentFriendRequests")
                                                            .child(currentUserKey.toString())
                                                            .removeValue()
                                                    }
                                                }

                                                override fun onCancelled(error: DatabaseError) {
                                                    TODO("Not yet implemented")
                                                }


                                            })


                                            reference.child("user").child(currentUserKey.toString())
                                                .addListenerForSingleValueEvent(object :
                                                    ValueEventListener {
                                                    override fun onDataChange(snapshot: DataSnapshot) {
                                                        val info =
                                                            snapshot.getValue(User::class.java)
                                                        val email = info?.email
                                                        val userID = info?.userID
                                                        val userName = info?.userName

                                                        val deletedUser =
                                                            reference.child("deletedUser")
                                                                .child(userID.toString())
                                                        deletedUser.child("email").setValue(email)
                                                        deletedUser.child("userID").setValue(userID)
                                                        deletedUser.child("userName")
                                                            .setValue(userName)

                                                        reference.child("user")
                                                            .child(currentUserKey.toString())
                                                            .removeValue()

                                                        progressBar.visibility = GONE
                                                        auth.signOut()
                                                        auth.currentUser?.delete()

                                                        startActivity(
                                                            Intent(
                                                                this@Reauthenticate,
                                                                LogInAndRegister::class.java
                                                            )
                                                        )
                                                        stopService(
                                                            Intent(
                                                                this@Reauthenticate,
                                                                ChecksActiveStatus::class.java
                                                            )
                                                        )
                                                    }

                                                    override fun onCancelled(error: DatabaseError) {
                                                        TODO("Not yet implemented")
                                                    }
                                                })
                                        }
                                    }
                                } else {
                                    progressBar.visibility = GONE
                                    Toast.makeText(
                                        this@Reauthenticate,
                                        "Invalid email and password",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                    }

                }
            }
            else if (mode == 2){ // Change Password


                textViewMain.text = "To update your password, please input your email and old password and your desired new password"
                tvReAuthenticatePassword.text = "Account Change Password"

                btnAccountDeleteConfirm.setOnClickListener {
                    progressBar.visibility = VISIBLE
                    val emailEditText = etEmailReAuthenticate.text.toString()
                    val passwordEditText = etPasswordReAuthenticate.text.toString()

                    //Email
                    if (emailEditText == userEmail) {
                        isEmailValid = true
                        tvEmailError.visibility = INVISIBLE

                    } else if (etEmailReAuthenticate.text.isEmpty()) {

                        isEmailValid = false
                        tvEmailError.text = "Email is empty!"
                        tvEmailError.visibility = VISIBLE

                    } else {

                        isEmailValid = false
                        tvEmailError.text = "Incorrect email"
                        tvEmailError.visibility = VISIBLE

                    }

                    //Password
                    if (etPasswordReAuthenticate.text.isEmpty()) {
                        tvPasswordError.text = "Password is empty!"
                        tvPasswordError.visibility = VISIBLE
                        isPasswordValid = false
                    } else {
                        isPasswordValid = true
                        tvPasswordError.visibility = INVISIBLE
                    }

                    //New Password
                    if(etPasswordNewPassword.text.trim().isEmpty()){
                        tvPasswordNewError.text = "Password is empty!"
                        tvPasswordNewError.visibility = VISIBLE
                        isNewPasswordValid = false
                    }
                    else if(etPasswordNewPassword.text.length <= 7){
                        tvPasswordNewError.text = "It should consist of at least 8 characters!"
                        tvPasswordNewError.visibility = VISIBLE
                        isNewPasswordValid = false
                    } else {
                        tvPasswordNewError.visibility = INVISIBLE
                        isNewPasswordValid = true
                    }



                    if (isEmailValid && isPasswordValid && isNewPasswordValid) {
                        val credential =
                            EmailAuthProvider.getCredential(emailEditText, passwordEditText)

                        auth.currentUser?.reauthenticate(credential)
                            ?.addOnCompleteListener { task1 ->

                                if (task1.isSuccessful) {
                                    auth.currentUser!!.updatePassword(etPasswordNewPassword.text.toString()).addOnCompleteListener { task ->
                                        progressBar.visibility = INVISIBLE
                                        if (!task.isSuccessful) {
                                            val snackbar_fail = Toast
                                                .makeText(
                                                    this@Reauthenticate,
                                                    "Something went wrong. Please try again later",
                                                    Toast.LENGTH_LONG
                                                )
                                            snackbar_fail.show()
                                        } else {
                                            finish()
                                            startActivity(Intent(this@Reauthenticate, MainActivity::class.java))
                                            val snackbar_su = Toast
                                                .makeText(
                                                    this@Reauthenticate,
                                                    "Password Successfully Modified",
                                                    Toast.LENGTH_LONG
                                                )
                                            snackbar_su.show()
                                        }
                                    }
                                } else{
                                    progressBar.visibility = INVISIBLE
                                    progressBar.visibility = GONE
                                    Toast.makeText(
                                        this@Reauthenticate,
                                        "Invalid password",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                    }

                }
            }
            else if (mode == 3){ //Deactivate
                textInputLayout.visibility = GONE
                textViewMain.text = "To continue to deactivate you account, please input your correct information"
                tvReAuthenticatePassword.text = "Account Deactivate Confirmation"
                etPasswordReAuthenticate.hint = "Password"

                etPasswordNewPassword.visibility = GONE
                tvPasswordNewError.visibility = GONE

                btnAccountDeleteConfirm.setOnClickListener {

                    val emailEditText = etEmailReAuthenticate.text.toString()
                    val passwordEditText = etPasswordReAuthenticate.text.toString()

                    //Email
                    if (emailEditText == userEmail) {

                        progressBar.visibility = VISIBLE

                        isEmailValid = true
                        tvEmailError.visibility = INVISIBLE

                    } else if (etEmailReAuthenticate.text.isEmpty()) {

                        isEmailValid = false
                        tvEmailError.text = "Email is empty!"
                        tvEmailError.visibility = VISIBLE

                    } else {

                        isEmailValid = false
                        tvEmailError.text = "Incorrect email"
                        tvEmailError.visibility = VISIBLE

                    }

                    //Password
                    if (etPasswordReAuthenticate.text.isEmpty()) {
                        tvPasswordError.text = "Password is empty!"
                        tvPasswordError.visibility = VISIBLE
                        isPasswordValid = false
                    } else {
                        isPasswordValid = true
                        tvPasswordError.visibility = INVISIBLE
                    }



                    if (isEmailValid && isPasswordValid) {
                        val credential =
                            EmailAuthProvider.getCredential(emailEditText, passwordEditText)

                        auth.currentUser?.reauthenticate(credential)
                            ?.addOnCompleteListener { task1 ->

                                if (task1.isSuccessful) {

                                    Log.d("MyTag", "Success")
                                    reference.child("user").child(currentUserKey.toString())
                                        .addListenerForSingleValueEvent(object :
                                            ValueEventListener {
                                            override fun onDataChange(snapshot: DataSnapshot) {
                                                val info =
                                                    snapshot.getValue(User::class.java)
                                                val email = info?.email
                                                val userID = info?.userID
                                                val userName = info?.userName


                                                val deactivatedUser =
                                                    reference.child("deactivatedUser")
                                                        .child(userID.toString())

                                                deactivatedUser.child("email").setValue(email)
                                                deactivatedUser.child("userID").setValue(userID)
                                                deactivatedUser.child("userName")
                                                    .setValue(userName)

                                                reference.child("user")
                                                    .child(currentUserKey.toString())
                                                    .removeValue()
                                            }

                                            override fun onCancelled(error: DatabaseError) {
                                                TODO("Not yet implemented")
                                            }
                                        })

                                    val friendsRef = reference.child("user").child(currentUserKey.toString()).child("Friends")
                                    friendsRef.addListenerForSingleValueEvent(object : ValueEventListener {
                                        override fun onDataChange(snapshot: DataSnapshot) {
                                            for (friendSnapshot in snapshot.children) {
                                                val friendKey = friendSnapshot.key
                                                val friendData = friendSnapshot.value

                                                val deactivatedUserFriendsRef =
                                                    reference.child("deactivatedUser").child(currentUserKey.toString()).child("Friends")
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

                                    val incomingFriendsRef = reference.child("user").child(currentUserKey.toString()).child("IncomingFriendRequests")
                                    incomingFriendsRef.addListenerForSingleValueEvent(object : ValueEventListener {
                                        override fun onDataChange(snapshot: DataSnapshot) {
                                            for (friendSnapshot in snapshot.children) {
                                                val friendKey = friendSnapshot.key
                                                val friendData = friendSnapshot.value

                                                val deactivatedUserFriendsRef =
                                                    reference.child("deactivatedUser").child(currentUserKey.toString()).child("IncomingFriendRequests")
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

                                    val sentFriendsRef = reference.child("user").child(currentUserKey.toString()).child("SentFriendRequests")
                                    sentFriendsRef.addListenerForSingleValueEvent(object : ValueEventListener {
                                        override fun onDataChange(snapshot: DataSnapshot) {
                                            for (friendSnapshot in snapshot.children) {
                                                val friendKey = friendSnapshot.key
                                                val friendData = friendSnapshot.value

                                                val deactivatedUserFriendsRef =
                                                    reference.child("deactivatedUser").child(currentUserKey.toString()).child("SentFriendRequests")
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

                                    val conversations = reference.child("user").child(currentUserKey.toString()).child("conversations")
                                    conversations.addListenerForSingleValueEvent(object : ValueEventListener {
                                        override fun onDataChange(snapshot: DataSnapshot) {
                                            for (convoSnapshot in snapshot.children) {
                                                val friendKey = convoSnapshot.key
                                                val friendData = convoSnapshot.value

                                                val deactivatedUserFriendsRef =
                                                    reference.child("deactivatedUser").child(currentUserKey.toString()).child("conversations")
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

                                    progressBar.visibility = GONE
                                    auth.signOut()

                                    startActivity(
                                        Intent(
                                            this@Reauthenticate,
                                            LogInAndRegister::class.java
                                        )
                                    )
                                    stopService(
                                        Intent(
                                            this@Reauthenticate,
                                            ChecksActiveStatus::class.java
                                        )
                                    )



                                        } else {
                                    progressBar.visibility = GONE
                                    Toast.makeText(
                                        this@Reauthenticate,
                                        "Invalid email and password",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }

                            }
                    }

                }

            }
            btnBackAccountDeleteConfirmation.setOnClickListener {
                onBackPressed()
            }

        }

    }
}