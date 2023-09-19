package com.example.upangchattingapp.messaging

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View.VISIBLE
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.upangchattingapp.MainActivity
import com.example.upangchattingapp.R
import com.example.upangchattingapp.databinding.ActivityRandomMessagingBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.Random

class RandomMessaging : AppCompatActivity() {

    private lateinit var binding : ActivityRandomMessagingBinding
    private val reference = FirebaseDatabase.getInstance().reference
    private val currentUserKey = FirebaseAuth.getInstance().currentUser?.uid
    private lateinit var adapter : RecyclerViewMessageAdapter
    private var randomUserListener: ValueEventListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRandomMessagingBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        val userID = intent.getStringExtra("userID")



        binding.apply {

            cvChatBackButton.setOnClickListener {
                onBackPressed()
            }

            reference.child("FindingRandomUser").addListenerForSingleValueEvent(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.hasChild(currentUserKey.toString())) {
                        Log.e("MyTag", "User Inside FindingRandomUser")
                        btnRandomSearchAndStop.text = "Searching"
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }
            })

            Log.e("MyTag", userID.toString())

            if(userID == "None" || userID == null || userID == "null" || userID.isBlank() || userID.isEmpty()){
                btnRandomSearchAndStop.setOnClickListener {
                    if (btnRandomSearchAndStop.text == "Searching") {
                        Log.e("MyTag", "Stopped Searching")
                        btnRandomSearchAndStop.text = "Search"

                        randomUserListener?.let {
                            reference.child("FindingRandomUser").removeEventListener(it)
                        }
                        reference.child("FindingRandomUser").child(currentUserKey.toString())
                            .removeValue()

                    } else if(btnRandomSearchAndStop.text == "Search"){
                        Log.e("MyTag", "Searching Start")
                        btnRandomSearchAndStop.text = "Searching"

                        reference.child("user").child(currentUserKey.toString()).child("randomConversation").removeValue()
                        reference.child("FindingRandomUser").child(currentUserKey.toString()).removeValue()

                        reference.child("FindingRandomUser").child(currentUserKey.toString())
                            .setValue(true)


                        randomUserListener = object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                Log.e("MyTag", "Something Happen")
                                val randomUsers = mutableListOf<String>()

                                for (userSnapshot in snapshot.children) {
                                    val userKey = userSnapshot.key
                                    if (userKey != currentUserKey) {
                                        randomUsers.add(userKey.toString())
                                    }
                                }

                                if (randomUsers.isNotEmpty()) {
                                    Log.e("MyTag", "Not Empty")

                                        val randomIndex = Random().nextInt(randomUsers.size)
                                        val selectedUser = randomUsers[randomIndex]


                                        reference.child("user").child(currentUserKey.toString())
                                            .child("interests")
                                            .addListenerForSingleValueEvent(object :
                                                ValueEventListener {
                                                override fun onDataChange(snapshot: DataSnapshot) {
                                                    Log.e("MyTag", "getting current user interests")
                                                    val currentUserInterestsList =
                                                        mutableListOf<String>()
                                                    for (dataSnapshot in snapshot.children) {
                                                        val interestItem =
                                                            dataSnapshot.key.toString()
                                                        currentUserInterestsList.add(interestItem)
                                                    }

                                                    reference.child("user").child(selectedUser)
                                                        .child("interests")
                                                        .addListenerForSingleValueEvent(object :
                                                            ValueEventListener {
                                                            override fun onDataChange(snapshot: DataSnapshot) {
                                                                Log.e("MyTag", "getting other user interests")
                                                                val otherUserInterestList =
                                                                    mutableListOf<String>()
                                                                for (dataSnapshot in snapshot.children) {
                                                                    val interestItem =
                                                                        dataSnapshot.key.toString()
                                                                    otherUserInterestList.add(
                                                                        interestItem
                                                                    )
                                                                }

                                                                val result =
                                                                    currentUserInterestsList.intersect(
                                                                        otherUserInterestList.toSet()
                                                                    )

                                                                if (result.isNotEmpty()) {

                                                                    reference.child("user")
                                                                        .child(selectedUser)
                                                                        .child("randomConversation")
                                                                        .setValue(currentUserKey.toString())
                                                                    reference.child("user")
                                                                        .child(currentUserKey.toString())
                                                                        .child("randomConversation")
                                                                        .setValue(selectedUser)

                                                                    reference.child("FindingRandomUser")
                                                                        .child(currentUserKey.toString())
                                                                        .removeValue()

                                                                    reference.child("FindingRandomUser")
                                                                        .child(selectedUser)
                                                                        .removeValue()


                                                                    var systemMessage = ""

                                                                    Log.e(
                                                                        "MyTag",
                                                                        result.toString()
                                                                    )
                                                                    if (result.isEmpty()) {
                                                                        systemMessage =
                                                                            "You have nothing in common"
                                                                    } else {
                                                                        systemMessage =
                                                                            "You both like ${
                                                                                result.joinToString(
                                                                                    ", "
                                                                                )
                                                                            }"
                                                                    }
                                                                    Log.e("MyTag", "Found User")
                                                                    var conversationId =
                                                                        listOf(
                                                                            currentUserKey.toString(),
                                                                            selectedUser
                                                                        ).sorted().joinToString("+")
                                                                    conversationId =
                                                                        "random_$conversationId"
                                                                    val addSystemMessage =
                                                                        reference.child("RandomConversations")
                                                                            .child(conversationId)
                                                                            .child("messages")
                                                                            .child("---SystemMessage")
                                                                    addSystemMessage.child("message")
                                                                        .setValue(systemMessage)
                                                                    addSystemMessage.child("timeStamp")
                                                                        .setValue("1980-06-21 15:00:44")
                                                                    addSystemMessage.child("userID")
                                                                        .setValue("..Admin")
                                                                    val intent = Intent(
                                                                        this@RandomMessaging,
                                                                        RandomMessaging::class.java
                                                                    )
                                                                    intent.putExtra(
                                                                        "userID",
                                                                        selectedUser
                                                                    )
                                                                    Log.e("MyTag", "GG")
                                                                    startActivity(intent)
                                                                }
                                                            }

                                                            override fun onCancelled(error: DatabaseError) {
                                                                TODO("Not yet implemented")
                                                            }
                                                        })
                                                }

                                                override fun onCancelled(error: DatabaseError) {
                                                    TODO("Not yet implemented")
                                                }
                                            })

                                } else {
                                    Log.e("MyTag", "Empty")
                                }
                            }

                            override fun onCancelled(error: DatabaseError) {
                                TODO("Not yet implemented")
                            }
                        }

                        reference.child("FindingRandomUser").addValueEventListener(randomUserListener!!)



                    }
                }
            } else {
                Log.e("MyTag", "$userID hiii2")
                llBottomPanel.visibility = VISIBLE
                btnRandomSearchAndStop.text = "End Conversation"


                var conversationId =
                    listOf(currentUserKey.toString(), userID.toString()).sorted().joinToString("+")
                conversationId = "random_$conversationId"
                adapter = RecyclerViewMessageAdapter(conversationId)

                btnRandomSearchAndStop.setOnClickListener {
                    randomUserListener?.let {
                        reference.child("FindingRandomUser").removeEventListener(it)
                        randomUserListener = null
                    }
                    reference.child("RandomConversations").child(conversationId).removeValue()
                    reference.child("user").child(currentUserKey.toString()).child("randomConversation").removeValue()
                    reference.child("user").child(userID).child("randomConversation").removeValue()
                    Log.e("MyTag", "Stop Convo")
                    val intent2 =  Intent(this@RandomMessaging, RandomMessaging::class.java)
                    intent2.putExtra("userID", "None")
                    startActivity(intent2)
                    finish()
                }




                val llm = LinearLayoutManager(this@RandomMessaging)
                llm.stackFromEnd = true     // items gravity sticks to bottom
                llm.reverseLayout = false   // item list sorting (new messages start from the bottom)
                binding.rvShowMessages.layoutManager = llm
                binding.rvShowMessages.adapter = adapter

                cvSendMessage.setOnClickListener {

                    if(etMessage.text.isNotEmpty()) {

                        val messageContent = etMessage.text.trim().toString()
                        val timestamp =
                            SimpleDateFormat(
                                "yyyy-MM-dd HH:mm:ss",
                                Locale.getDefault()
                            ).format(Date())


                        //Add conversationID to the current user
                        reference.child("user").child(currentUserKey.toString())
                            .child("conversations").child("randomChat")
                            .child(conversationId).setValue(userID.toString())

                        //Add conversationID to the other user
                        reference.child("user").child(userID.toString()).child("conversations")
                            .child("randomChat")
                            .child(conversationId).setValue(currentUserKey.toString())

                        //Create the conversation
                        val message = Message(messageContent, currentUserKey.toString(), timestamp)
                        reference.child("RandomConversations")
                            .child(conversationId).child("messages").push().setValue(message)

                        etMessage.text.clear()
                    }

                }



                reference.child("user").child(userID).child("randomConversation").addValueEventListener(object : ValueEventListener{
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (!snapshot.exists()) {
                            Log.e("MyTag", "${snapshot.value.toString()} hiii")
                            startActivity(Intent(this@RandomMessaging, RandomMessaging::class.java))
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        TODO("Not yet implemented")
                    }
                })

                reference.child("RandomConversations").child(conversationId).child("messages")
                    .addChildEventListener(object : ChildEventListener {
                        override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                            val messageContent = snapshot.child("message").getValue(String::class.java)
                            val timestamp = snapshot.child("timeStamp").getValue(String::class.java)
                            val sentUserID = snapshot.child("userID").getValue(String::class.java)

                            if (messageContent != null && timestamp != null && sentUserID != null) {
                                val message = Message(messageContent, sentUserID, timestamp)
                                Log.d("MyTag",message.toString())
                                adapter.addMessage(message)
                            }

                            binding.rvShowMessages.smoothScrollToPosition(1)
                        }

                        override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                            Log.e("MyTag", "hehe")
                        }

                        override fun onChildRemoved(snapshot: DataSnapshot) {
                            val messageContent = snapshot.child("message").getValue(String::class.java)
                            val timestamp = snapshot.child("timeStamp").getValue(String::class.java)
                            val sentUserID = snapshot.child("userID").getValue(String::class.java)

                            if (messageContent != null && timestamp != null && sentUserID != null) {
                                val message = Message(messageContent, sentUserID, timestamp)
                                adapter.removeMessage(message)
                            }
                        }

                        override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                            TODO("Not yet implemented")
                        }

                        override fun onCancelled(error: DatabaseError) {
                            TODO("Not yet implemented")
                        }
                    })


            }



        }
    }


    override fun onBackPressed() {
        Log.e("MyTag", "Back pressed")
        val intent = Intent(this, MainActivity::class.java)
        intent.putExtra("fragment", 2)
        startActivity(intent)
        super.onBackPressed()
    }

    override fun onDestroy() {
        randomUserListener?.let {
            reference.child("FindingRandomUser").removeEventListener(it)
        }
        finish()
        super.onDestroy()
    }
}
