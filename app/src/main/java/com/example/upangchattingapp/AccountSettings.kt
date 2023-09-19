package com.example.upangchattingapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.upangchattingapp.customDialogs.CustomDialogFriendRequest
import com.example.upangchattingapp.databinding.ActivityAccountSettingsBinding
import com.example.upangchattingapp.loginandregister.LogInAndRegister
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class AccountSettings : AppCompatActivity() {

    private lateinit var binding : ActivityAccountSettingsBinding
    private val auth = FirebaseAuth.getInstance()
    private val reference = FirebaseDatabase.getInstance().reference
    private val currentUserKey = auth.currentUser?.uid


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAccountSettingsBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        binding.apply{

            reference.child("user").child(currentUserKey.toString()).addListenerForSingleValueEvent(object :
                ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val info = snapshot.getValue(User::class.java)
                    val email = info?.email.toString()
                    val userID = info?.userID.toString()
                    val userName = info?.userName.toString()

                    rlChangePassword.setOnClickListener {
                        val intent = Intent(this@AccountSettings, Reauthenticate::class.java)
                        intent.putExtra("email", email)
                        intent.putExtra("mode", 2)
                        startActivity(intent)
                    }

                    rlDeleteAccount.setOnClickListener {

                        val dialog = CustomDialogFriendRequest(this@AccountSettings, "Are you sure you want to delete your account?", "Please note that deleting your account is permanent and irreversible. You will not be able to recover your account once it is deleted.") {action ->

                            if (action == "confirm"){
                                val intent = Intent(this@AccountSettings, Reauthenticate::class.java)
                                intent.putExtra("email", email)
                                intent.putExtra("mode", 1)
                                startActivity(intent)

                            }

                        }
                        dialog.show()
                    }

                    rlDeactiveAccount.setOnClickListener {
                        val dialog = CustomDialogFriendRequest(this@AccountSettings, "Are you sure you want to deactivate your account?", "By deactivating your account, other users including your friends, would not see your profile\nTo activate it again, you just need to log in.") {action ->

                            if (action == "confirm"){
                                val intent = Intent(this@AccountSettings, Reauthenticate::class.java)
                                intent.putExtra("email", email)
                                intent.putExtra("mode", 3)
                                startActivity(intent)

                            }

                        }
                        dialog.show()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }
            })



            btnBackAccountSettings.setOnClickListener {
                onBackPressed()
            }




        }

    }
}