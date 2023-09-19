package com.example.upangchattingapp.messaging

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.upangchattingapp.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [Chats.newInstance] factory method to
 * create an instance of this fragment.
 */
class Chats : Fragment() {

    private val conversationList = mutableListOf<ChatList>()

    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_chats, container, false)
        val progressBar = view.findViewById<ProgressBar>(R.id.progressBar)

        val recyclerViewChatMessages = view.findViewById<RecyclerView>(R.id.rvChats)
        recyclerViewChatMessages.layoutManager = LinearLayoutManager(view.context)

        val recyclerViewChatFeature = view.findViewById<RecyclerView>(R.id.rvChatFeature)
        val llm = LinearLayoutManager(view.context, LinearLayoutManager.HORIZONTAL, false)
        recyclerViewChatFeature.layoutManager = llm


        val reference = FirebaseDatabase.getInstance().reference
        val currentUserKey = FirebaseAuth.getInstance().currentUser?.uid
        val userRef = reference.child("user")
        val conversationRef = reference.child("conversations")

        //Chat Feature
        userRef.child(currentUserKey.toString()).child("Friends").addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {

                    val friendsMessageCountList = mutableListOf<FriendsMessageCount>()
                    for (dataSnapshot in snapshot.children) {
                        val userFriendID = dataSnapshot.key
                        val conversationWithFriendID =
                            listOf(currentUserKey.toString(), userFriendID.toString()).sorted()
                                .joinToString("+")
                        conversationRef.child(conversationWithFriendID).child("messages")
                            .addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onDataChange(snapshot: DataSnapshot) {
                                    val messageCount = snapshot.childrenCount

                                    userRef.child(userFriendID.toString())
                                        .addListenerForSingleValueEvent(object :
                                            ValueEventListener {
                                            override fun onDataChange(snapshot: DataSnapshot) {
                                                val userName =
                                                    snapshot.child("userName").getValue(String :: class.java)

                                                if (userName == null || userName == "null"){
                                                    Log.e("MyTag", "Do nothing")
                                                    progressBar.visibility = GONE
                                                } else {

                                                    val friend = FriendsMessageCount(
                                                        userFriendID.toString(),
                                                        messageCount,
                                                        userName.toString()
                                                    )
                                                    friendsMessageCountList.add(friend)

                                                    friendsMessageCountList.sortByDescending { it.messageCount }

                                                    val adapter =
                                                        RecyclerViewChatFeatureAdapter(
                                                            friendsMessageCountList
                                                        )
                                                    recyclerViewChatFeature.adapter = adapter

                                                    progressBar.visibility = GONE
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
                    }
                } else {
                    Log.e("MyTag", "lol")
                    progressBar.visibility = GONE


                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })

        //Chat Lists
        userRef.child(currentUserKey.toString()).child("conversations").addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                val newConversationList = mutableListOf<ChatList>()
                for (dataSnapshot in snapshot.children){
                    val conversationID = dataSnapshot.key
                    val userID = dataSnapshot.value


                    conversationRef.child(conversationID.toString()).child("messages")
                        .orderByChild("timeStamp").limitToLast(1)
                        .addValueEventListener(object : ValueEventListener{
                        override fun onDataChange(snapshot: DataSnapshot) {

                            val messageInfo =
                                snapshot.children.firstOrNull()?.getValue(Message::class.java)
                            if (messageInfo != null) {
                                val latestTimeStamp = messageInfo.timeStamp
                                val lastMessage = messageInfo.message

                                userRef.child(userID.toString())
                                    .addListenerForSingleValueEvent(object : ValueEventListener {
                                        override fun onDataChange(snapshot: DataSnapshot) {
                                            val userName = snapshot.child("userName")
                                                .getValue(String::class.java)
                                            val existingConversationIndex =
                                                conversationList.indexOfFirst { it.userID == userID.toString() }
                                            if (userName == null || userName == "null") {
                                                Log.d("MyTag", "Do nothing")
                                                progressBar.visibility = GONE
                                            }else if (existingConversationIndex == -1) {
                                                // Conversation doesn't exist, add it to the new conversation list
                                                val message = ChatList(
                                                    lastMessage.toString(),
                                                    latestTimeStamp.toString(),
                                                    userName.toString(),
                                                    userID.toString(),
                                                    conversationID.toString()
                                                )
                                                newConversationList.add(message)
                                                newConversationList.sortByDescending { it.timeStamp }
                                                conversationList.clear()
                                                conversationList.addAll(newConversationList)
                                                val adapter =
                                                    RecyclerViewChatListAdapter(conversationList)
                                                Log.e("MyTag", conversationList.toString())
                                                recyclerViewChatMessages.adapter = adapter
                                            } else {
                                                // Conversation already exists, update it with the latest message
                                                val existingConversation =
                                                    conversationList[existingConversationIndex]
                                                existingConversation.message =
                                                    lastMessage.toString()
                                                existingConversation.timeStamp =
                                                    latestTimeStamp.toString()
                                                newConversationList.sortByDescending { it.timeStamp }
                                                conversationList.clear()
                                                conversationList.addAll(newConversationList)
                                                val adapter =
                                                    RecyclerViewChatListAdapter(conversationList)
                                                Log.e("MyTag", conversationList.toString())
                                                recyclerViewChatMessages.adapter = adapter
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
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })

        userRef.child(currentUserKey.toString()).child("conversations").addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {

                for (dataSnapshot in snapshot.children){

                    conversationRef.child(dataSnapshot.key.toString()).addChildEventListener(object : ChildEventListener{
                        override fun onChildAdded(
                            snapshot: DataSnapshot,
                            previousChildName: String?
                        ) {
                            Log.e("MyTag", "convo ${snapshot.ref}")
                            Log.e("MyTag", "added")
                        }

                        override fun onChildChanged(
                            snapshot: DataSnapshot,
                            previousChildName: String?
                        ) {
                            Log.e("MyTag", "changed")
                        }

                        override fun onChildRemoved(snapshot: DataSnapshot) {
                            Log.e("MyTag", "removed ${dataSnapshot.key}")


                        }

                        override fun onChildMoved(
                            snapshot: DataSnapshot,
                            previousChildName: String?
                        ) {
                            TODO("Not yet implemented")
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


        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment Chats.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            Chats().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}