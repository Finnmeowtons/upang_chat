package com.example.upangchattingapp.messaging

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View.GONE
import android.view.View.VISIBLE
import com.example.upangchattingapp.MainActivity
import com.example.upangchattingapp.OtherUsersProfile
import android.widget.ListView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.upangchattingapp.R
import com.example.upangchattingapp.User
import com.example.upangchattingapp.customDialogs.CustomDialogFriendRequest
import com.example.upangchattingapp.customDialogs.CustomDialogMessage
import com.example.upangchattingapp.customDialogs.CustomDialogReport
import com.example.upangchattingapp.databinding.ActivityMessagingBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ServerValue
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class Messaging : AppCompatActivity() {

    private lateinit var binding : ActivityMessagingBinding
    private val reference = FirebaseDatabase.getInstance().reference
    private val currentUserKey = FirebaseAuth.getInstance().currentUser?.uid
    private lateinit var adapter : RecyclerViewMessageAdapter
    private lateinit var userID : String
    private lateinit var name : String
    private var image = 0
    private var friendTextButton = ""
    private var currentPage = 1
    private val messagesPerPage = 20
    private var isLastMessageReached = false

    private var yearLevel = ""
    private var course = ""
    private var aboutMe = ""


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMessagingBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)


        userID = intent.getStringExtra("userID").toString()
        name = intent.getStringExtra("name").toString()
        val conversationId =
            listOf(currentUserKey.toString(), userID.toString()).sorted().joinToString("+")
        adapter = RecyclerViewMessageAdapter(conversationId)

        binding.apply {

            btnInfoChat.setOnClickListener {
                val dialog2 = CustomDialogMessage(view.context, "both"){action ->

                    if (action == "report"){
                        val dialog = CustomDialogReport(this@Messaging, userID.toString())
                        dialog.show()
                    } else if (action == "remove"){
                        val dialog2 = CustomDialogFriendRequest(view.context, "Confirm delete conversation?", "Once you delete it will never come back"){ action ->

                            if (action == "confirm"){



                                reference.child("conversations").child(listOf(currentUserKey.toString(), userID.toString()).sorted().joinToString("+"))
                                    .child("messages").addListenerForSingleValueEvent(object : ValueEventListener{
                                        override fun onDataChange(snapshot: DataSnapshot) {
                                            val messageList = mutableListOf<Message>()
                                            for (dataSnapshot in snapshot.children){
                                                val key = dataSnapshot.key.toString()
                                                val message = snapshot.child(key.toString()).getValue(Message::class.java)
                                                if (message != null) {
                                                    messageList.add(message)
                                                }
                                                Log.e("MyTag", "hehe ${key.toString()}")
                                                Log.e("MyTag", message.toString())
                                                reference.child("conversations").child(listOf(currentUserKey.toString(), userID.toString()).sorted().joinToString("+"))
                                                    .child("messages").child(key.toString()).removeValue()

                                                reference.child("conversations").child(listOf(currentUserKey.toString(), userID.toString()).sorted().joinToString("+"))
                                                    .child("deletedMessages").child(key.toString()).child("message").setValue(message?.message)

                                                reference.child("conversations").child(listOf(currentUserKey.toString(), userID.toString()).sorted().joinToString("+"))
                                                    .child("deletedMessages").child(key.toString()).child("timeStamp").setValue(message?.timeStamp)

                                                reference.child("conversations").child(listOf(currentUserKey.toString(), userID.toString()).sorted().joinToString("+"))
                                                    .child("deletedMessages").child(key.toString()).child("userID").setValue(message?.userID)

                                                startActivity(Intent(this@Messaging, MainActivity::class.java))
                                            }
                                        }

                                        override fun onCancelled(error: DatabaseError) {
                                            TODO("Not yet implemented")
                                        }

                                    })
                            }
                        }
                        dialog2.show()
                    }

                }
                dialog2.show()
                }

            reference.child("user").child(currentUserKey.toString())
                .child("SentFriendRequests").child(userID)
                .addListenerForSingleValueEvent(object :
                    ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()) {
                            //If the current users sent a friend request to the other user
                            image = R.drawable.sent_friend_request
                            friendTextButton = "Pending Request"
                            tvNotFriends.text = "You are not friends with ${name.lowercase(Locale.getDefault()).split(" ").joinToString(" ") { it.capitalize(Locale.ROOT) }}.\n You cannot send a message"
                            tvNotFriends.visibility = VISIBLE
                            cvMessageBox.visibility = GONE
                            cvSendMessage.visibility = GONE
                        } else {

                            reference.child("user").child(currentUserKey.toString())
                                .child("IncomingFriendRequests").child(userID)
                                .addListenerForSingleValueEvent(object :
                                    ValueEventListener {
                                    override fun onDataChange(snapshot: DataSnapshot) {
                                        if (snapshot.exists()) {
                                            //If the current user has a friend request from the other user
                                            image = R.drawable.incoming_friend_request
                                            friendTextButton = "Respond"
                                            tvNotFriends.text = "You are not friends with ${name.lowercase(Locale.getDefault()).split(" ").joinToString(" ") { it.capitalize(Locale.ROOT) }}.\n You cannot send a message"
                                            tvNotFriends.visibility = VISIBLE
                                            cvMessageBox.visibility = GONE
                                            cvSendMessage.visibility = GONE
                                        } else {

                                            reference.child("user")
                                                .child(currentUserKey.toString())
                                                .child("Friends").child(userID.toString())
                                                .addListenerForSingleValueEvent(object :
                                                    ValueEventListener {
                                                    override fun onDataChange(snapshot: DataSnapshot) {
                                                        if (snapshot.exists()) {
                                                            //Friends
                                                            image = R.drawable.friends
                                                            friendTextButton = "Friends"
                                                        } else {
                                                            //No interactions
                                                            image =
                                                                R.drawable.baseline_person_add_24
                                                            friendTextButton = "Add Friend"
                                                            tvNotFriends.text = "You are not friends with ${name.lowercase(Locale.getDefault()).split(" ").joinToString(" ") { it.capitalize(Locale.ROOT) }}.\n You cannot send a message"
                                                            tvNotFriends.visibility = VISIBLE
                                                            cvMessageBox.visibility = GONE
                                                            cvSendMessage.visibility = GONE
                                                        }
                                                        reference.child("user")
                                                            .child(userID.toString())
                                                            .addListenerForSingleValueEvent(
                                                                object : ValueEventListener {
                                                                    override fun onDataChange(
                                                                        snapshot: DataSnapshot
                                                                    ) {
                                                                        Log.e("MyTag", "Sendddd")
                                                                        val info =
                                                                            snapshot.getValue(
                                                                                User::class.java
                                                                            )
                                                                        val aboutMe =
                                                                            info?.aboutMe.toString()
                                                                        val yearLevel =
                                                                            info?.yearLevel.toString()
                                                                        val course =
                                                                            info?.course.toString()

                                                                        llChatProfile.setOnClickListener {

                                                                            Log.e("MyTag", "hehe")

                                                                            val intent = Intent(
                                                                                this@Messaging,
                                                                                OtherUsersProfile::class.java
                                                                            )
                                                                            intent.putExtra(
                                                                                "yearLevel",
                                                                                yearLevel
                                                                            )
                                                                            intent.putExtra(
                                                                                "name",
                                                                                name
                                                                            )
                                                                            intent.putExtra(
                                                                                "course",
                                                                                course
                                                                            )
                                                                            intent.putExtra(
                                                                                "userID",
                                                                                userID
                                                                            )
                                                                            intent.putExtra(
                                                                                "image",
                                                                                image
                                                                            )
                                                                            intent.putExtra(
                                                                                "friendTextButton",
                                                                                friendTextButton
                                                                            )
                                                                            intent.putExtra(
                                                                                "aboutMe",
                                                                                aboutMe
                                                                            )
                                                                            startActivity(intent)
                                                                        }

                                                                            if (image == R.drawable.friends){

                                                                                cvSendMessage.setOnClickListener {

                                                                                    if (etMessage.text.isNotEmpty()) {

                                                                                        val messageContent = etMessage.text.trim().toString()
                                                                                        val timestamp =
                                                                                            SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())


                                                                                        //Add conversationID to the current user
                                                                                        reference.child("user").child(currentUserKey.toString()).child("conversations")
                                                                                            .child(conversationId).setValue(userID.toString())

                                                                                        //Add conversationID to the other user
                                                                                        reference.child("user").child(userID.toString()).child("conversations")
                                                                                            .child(conversationId).setValue(currentUserKey.toString())

                                                                                        //Create the conversation
                                                                                        val message = Message(messageContent, currentUserKey.toString(), timestamp)
                                                                                        reference.child("conversations").child(conversationId).child("messages").push()
                                                                                            .setValue(message)

                                                                                        etMessage.text.clear()
                                                                                    }

                                                                                }
                                                                            } else {
                                                                                Log.e("MyTag", "Cant send")

                                                                                tvNotFriends.text = "You are not friends with ${name.lowercase(Locale.getDefault()).split(" ").joinToString(" ") { it.capitalize(Locale.ROOT) }}.\n You cannot send a message"
                                                                                tvNotFriends.visibility = VISIBLE
                                                                                cvMessageBox.visibility = GONE
                                                                                cvSendMessage.visibility = GONE
                                                                            }


                                                                    }

                                                                    override fun onCancelled(
                                                                        error: DatabaseError
                                                                    ) {
                                                                        TODO("Not yet implemented")
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
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        TODO("Not yet implemented")
                    }

                })


            //Status
            reference.child("OnlineUserList").addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (dataSnapshot in snapshot.children) {
                        val id = dataSnapshot.key
                        if (id == userID) {
                            tvUserActivityStatus.text = "Active now"
                        } else {
                            tvUserActivityStatus.text = "Offline"
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }
            })


            tvChatName.text =
                name.toString().lowercase(Locale.getDefault()).split(" ").joinToString(" ") {
                    it.capitalize(
                        Locale.ROOT
                    )
                }

            val llm = LinearLayoutManager(this@Messaging)
            llm.stackFromEnd = true     // items gravity sticks to bottom
            llm.reverseLayout = false   // item list sorting (new messages start from the bottom)
            rvShowMessages.layoutManager = llm
            rvShowMessages.adapter = adapter

            cvChatBackButton.setOnClickListener {
                onBackPressed()
            }

            binding.rvShowMessages.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)

                    val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                    val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()

                    // Check if the user has scrolled to the top of the list
                    if (firstVisibleItemPosition == 0 && !isLastMessageReached) {
                        // load more messages
                        currentPage++
                        loadMessages(currentPage)

                    }
                }
            })

        }


        //Loads messages
        reference.child("conversations").child(conversationId).child("messages")
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

                    binding.rvShowMessages.adapter = adapter
                }

                override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {

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

                }

                override fun onCancelled(error: DatabaseError) {

                }
            })


        loadMessages(currentPage)
    }

    private fun loadMessages(page: Int) {
        val conversationId = listOf(currentUserKey.toString(), userID.toString()).sorted().joinToString("+")

        val startIndex = (page - 1) * messagesPerPage


        val messagesQuery = reference.child("conversations")
            .child(conversationId)
            .child("messages")
            .orderByKey()
            .limitToLast(startIndex + messagesPerPage)

        messagesQuery.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val messageList = mutableListOf<Message>()
                for (dataSnapshot in snapshot.children) {
                    val message = dataSnapshot.getValue(Message::class.java)
                    message?.let {
                        messageList.add(it)
                    }
                }

                adapter.setMessages(messageList)
                binding.rvShowMessages.scrollToPosition(startIndex-1)
                if (startIndex >= messageList.size){
                    isLastMessageReached = true
                }


            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }


        })
    }


}


