package com.example.upangchattingapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageButton
import android.widget.ProgressBar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.upangchattingapp.friendsAndRequests.FriendsData
import com.example.upangchattingapp.friendsAndRequests.RecyclerViewFriendRequestsAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class OtherUsersFriendsList : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_other_users_friends_list)

        val userID = intent.getStringExtra("userID")

        val back = findViewById<ImageButton>(R.id.btnBackFriends)
        val progressBar = findViewById<ProgressBar>(R.id.progressBar)
        val recyclerView = findViewById<RecyclerView>(R.id.rvOtherUserFriends)
        recyclerView.layoutManager = LinearLayoutManager(this)

        val userRef = FirebaseDatabase.getInstance().reference.child("user")

        userRef.child(userID.toString()).child("Friends").addValueEventListener(object :
            ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!snapshot.hasChildren()){
                    progressBar.visibility = View.GONE
                }
                val friendList = mutableListOf<User>()
                for(dataSnapshot in snapshot.children){
                    val userKey = dataSnapshot.key
                    userRef.child(userKey.toString()).addListenerForSingleValueEvent(object :
                        ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            val user = snapshot.getValue(User::class.java)
                            if (user != null) {
                                friendList.add(user)
                            }

                            val adapter = RecyclerViewFriendRequestsAdapter(friendList, R.layout.friend_list)
                            recyclerView.adapter = adapter

                            progressBar.visibility = View.GONE

                            userRef.child(userID.toString()).child("Friends").addChildEventListener(object :
                                ChildEventListener {
                                override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                                    Log.e("MyTag", snapshot.toString())
                                }

                                override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                                    // Implement your logic when a child is changed
                                }

                                override fun onChildRemoved(snapshot: DataSnapshot) {
                                    Log.e("MyTag", "removeddd $snapshot")
                                    val removeUserKey = snapshot.key
                                    val boolean = snapshot.value

                                    if (removeUserKey != null && boolean != null) {
                                        val removeSentFriendRequestList =
                                            FriendsData(removeUserKey, boolean as Boolean)
                                        adapter.removeUser(removeSentFriendRequestList)
                                    }
                                }

                                override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                                    // Implement your logic when a child is moved
                                }

                                override fun onCancelled(error: DatabaseError) {
                                    // Handle database error
                                }
                            })
                        }

                        override fun onCancelled(error: DatabaseError) {
                            TODO("Not yet implemented")
                        }

                    })

                }



            }

            override fun onCancelled(error: DatabaseError) {

                TODO("Not yet implemented")
            }

        })


        back.setOnClickListener {
            onBackPressed()
        }
    }
}