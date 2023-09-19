package com.example.upangchattingapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.example.upangchattingapp.customDialogs.CustomDialogFriendRequest
import com.example.upangchattingapp.customDialogs.CustomDialogMessage
import com.example.upangchattingapp.customDialogs.CustomDialogReport
import com.example.upangchattingapp.databinding.ActivityOtherUsersProfileBinding
import com.example.upangchattingapp.messaging.Messaging
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.Locale

class OtherUsersProfile : AppCompatActivity() {

    private lateinit var binding : ActivityOtherUsersProfileBinding
    private var currentUserKey = FirebaseAuth.getInstance().currentUser?.uid
    private var databaseRef = FirebaseDatabase.getInstance().reference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOtherUsersProfileBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)


        val name = intent.getStringExtra("name")
        val course = intent.getStringExtra("course")
        val yearLevel = intent.getStringExtra("yearLevel")
        val userID = intent.getStringExtra("userID")
        val aboutMe = intent.getStringExtra("aboutMe")
        val image = intent.getIntExtra("image", 0)
        val friendTextButton = intent.getStringExtra("friendTextButton")


        binding.apply {

            databaseRef.child("user").child(userID.toString()).child("Friends").addValueEventListener(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    val frenList = mutableListOf<String>()
                    for (dataSnapshot in snapshot.children){
                        frenList.add(dataSnapshot.key.toString())
                    }
                    if (frenList.contains(currentUserKey.toString())) {
                        rlOtherUserFriends.setOnClickListener {
                            val intent2 =
                                Intent(this@OtherUsersProfile, OtherUsersFriendsList::class.java)
                            intent2.putExtra("userID", userID)
                            startActivity(intent2)


                        }
                    } else {
                        rlOtherUserFriends.setOnClickListener {
                            Toast.makeText(
                                this@OtherUsersProfile,
                                "You are not friends with the user",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }
            })

            tvAddFriend.text = friendTextButton
            imgAddFriend.setImageResource(image)

            tvBiography.text = aboutMe

            btnMore.setOnClickListener {

                val dialog2 = CustomDialogMessage(view.context, "other"){action ->

                    if (action == "report"){
                        val dialog = CustomDialogReport(this@OtherUsersProfile, userID.toString())
                        dialog.show()
                    }

                }
                dialog2.show()

            }


            databaseRef.child("user").child(userID.toString()).child("Friends").addListenerForSingleValueEvent(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    val friendCount = snapshot.childrenCount.toInt()
                    if (friendCount < 2){
                        tvFriendsCount.text = "$friendCount Friend"
                    } else {
                        tvFriendsCount.text = "$friendCount Friends"
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }
            })

            databaseRef.child("OnlineUserList").addListenerForSingleValueEvent(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (dataSnapshot in snapshot.children){
                        val id = dataSnapshot.key
                        if (id == userID){
                            tvStatus.text = "Online"
                            imgStatus.setColorFilter(ContextCompat.getColor(view.context, R.color.light_green))

                        } else {
                            tvStatus.text = "Offline"
                            imgStatus.setColorFilter(ContextCompat.getColor(view.context, R.color.dark_gray))
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }
            })

            tvProfileName.text = name.toString().lowercase(Locale.getDefault()).split(" ").joinToString(" ") { it.capitalize(
                Locale.ROOT) }
            tvCourseProfile.text = course
            tvYearLevelProfile.text = yearLevel

            btnBack.setOnClickListener {
                onBackPressed()
            }

            cvAddFriend.setOnClickListener {

                when (tvAddFriend.text) {
                    "Add Friend" -> {
                        //Add it to Current User
                        databaseRef.child("user").child(currentUserKey.toString())
                            .child("SentFriendRequests").child(userID.toString()).setValue(true)
                        //Add it to the other User
                        databaseRef.child("user").child(userID.toString()).child("IncomingFriendRequests").child(currentUserKey.toString()).setValue(true)
                        imgAddFriend.setImageResource(R.drawable.sent_friend_request)
                        tvAddFriend.text = "Pending Request"
                    }
                    "Respond" -> {

                        //
                        val dialog = CustomDialogFriendRequest(this@OtherUsersProfile, "Accept Friend Request?", "") { action ->
                            when (action) {
                                "confirm" -> {

                                    databaseRef.child("user").child(userID.toString()).child("Friends").child(currentUserKey.toString()).setValue(true)
                                    databaseRef.child("user").child(currentUserKey.toString()).child("Friends").child(userID.toString()).setValue(true)

                                    databaseRef.child("user").child(userID.toString())
                                        .child("SentFriendRequests").child(currentUserKey.toString()).removeValue()

                                    databaseRef.child("user").child(currentUserKey.toString())
                                        .child("IncomingFriendRequests").child(userID.toString()).removeValue()

                                    imgAddFriend.setImageResource(R.drawable.friends)
                                    tvAddFriend.text = "Friends"

                                }

                                "decline" -> {

                                    databaseRef.child("user").child(userID.toString())
                                        .child("SentFriendRequests").child(currentUserKey.toString()).removeValue()

                                    databaseRef.child("user").child(currentUserKey.toString())
                                        .child("IncomingFriendRequests").child(userID.toString()).removeValue()

                                    imgAddFriend.setImageResource(R.drawable.baseline_person_add_24)
                                    tvAddFriend.text = "Add Friend"

                                }
                            }
                        }
                        dialog.show()

                    }
                    "Pending Request" -> {

                        //Current User added User #1. If current user opened user 1# profile, it will show pending request
                        //If pending request is clicked it will ask if you wanna delete the friend request you sent
                        val dialog = CustomDialogFriendRequest(this@OtherUsersProfile, "Delete Friend Request?", "") { action ->
                            when (action) {
                                "confirm" -> {

                                    databaseRef.child("user").child(currentUserKey.toString())
                                        .child("SentFriendRequests").child(userID.toString()).removeValue()

                                    databaseRef.child("user").child(userID.toString())
                                        .child("IncomingFriendRequests").child(currentUserKey.toString()).removeValue()

                                    imgAddFriend.setImageResource(R.drawable.baseline_person_add_24)
                                    tvAddFriend.text = "Add Friend"

                                }

                                "decline" -> {

                                }
                            }
                        }
                        dialog.show()

                    }
                    else -> {
                        val dialog = CustomDialogFriendRequest(this@OtherUsersProfile, "Remove Friend?", "") { action ->
                            when (action) {
                                "confirm" -> {

                                    databaseRef.child("user").child(currentUserKey.toString())
                                        .child("Friends").child(userID.toString()).removeValue()

                                    databaseRef.child("user").child(userID.toString())
                                        .child("Friends").child(currentUserKey.toString()).removeValue()

                                    imgAddFriend.setImageResource(R.drawable.baseline_person_add_24)
                                    tvAddFriend.text = "Add Friend"
                                }
                            }
                        }
                        dialog.show()
                    }
                }

            }

            cvMessage.setOnClickListener {

                val intentMessage = Intent(this@OtherUsersProfile, Messaging::class.java)
                intentMessage.putExtra("name", name)
                intentMessage.putExtra("userID", userID)
                startActivity(intentMessage)

            }

        }
    }
}
