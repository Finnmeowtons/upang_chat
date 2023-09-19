package com.example.upangchattingapp

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.fragment.app.replace
import androidx.lifecycle.LifecycleObserver
import androidx.recyclerview.widget.RecyclerView
import com.example.upangchattingapp.friendsAndRequests.FriendRequestReceived
import com.example.upangchattingapp.friendsAndRequests.FriendRequestSent
import com.example.upangchattingapp.friendsAndRequests.Friends
import com.example.upangchattingapp.messaging.Message
import com.example.upangchattingapp.messaging.RandomMessaging
import com.example.upangchattingapp.messaging.RecyclerViewChatListAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.Random

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [FindAFriend.newInstance] factory method to
 * create an instance of this fragment.
 */
class FindAFriend : Fragment(), LifecycleObserver {

    private val reference = FirebaseDatabase.getInstance().reference
    private val currentUserKey = FirebaseAuth.getInstance().currentUser?.uid
    private var userID : String = "None"

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
        val view = inflater.inflate(R.layout.fragment_find_a_friend, container, false)



        val friendsButton = view.findViewById<Button>(R.id.btnFriends)
        val incomingFriendRequestButton = view.findViewById<Button>(R.id.btnRequestReceived)
        val friendRequestSentButton = view.findViewById<Button>(R.id.btnRequestSent)
        val randomChat = view.findViewById<RelativeLayout>(R.id.rlRandomChat)
        val randomLatestMessage = view.findViewById<TextView>(R.id.tvLatestRandomMessage)

        replaceFragment(Friends())

        friendsButton.setOnClickListener {
            replaceFragment(Friends())
        }

        incomingFriendRequestButton.setOnClickListener {
            replaceFragment(FriendRequestReceived())
        }

        friendRequestSentButton.setOnClickListener {
            replaceFragment(FriendRequestSent())
        }


        reference.child("user").child(currentUserKey.toString()).addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.hasChild("randomConversation")) {
                    val userID = snapshot.child("randomConversation").value

                    randomChat.setOnClickListener {
                        val intent = Intent(view.context, RandomMessaging::class.java)
                        intent.putExtra("userID", userID.toString())
                        startActivity(intent)
                    }

                    var conversationID = listOf(currentUserKey.toString(), userID.toString()).sorted().joinToString("+")
                    conversationID = "random_$conversationID"

                    reference.child("RandomConversations").child(conversationID).child("messages")
                        .orderByChild("timeStamp").limitToLast(1)
                        .addValueEventListener(object : ValueEventListener{
                            override fun onDataChange(snapshot: DataSnapshot) {
                                val messageInfo =
                                    snapshot.children.firstOrNull()?.getValue(Message::class.java)

                                if (messageInfo != null) {
                                    val lastMessage = messageInfo.message

                                    randomLatestMessage.text = lastMessage
                                }
                            }

                            override fun onCancelled(error: DatabaseError) {
                                TODO("Not yet implemented")
                            }
                        })

                } else {
                    randomChat.setOnClickListener {
                        val intent = Intent(view.context, RandomMessaging::class.java)
                        intent.putExtra("userID", userID)
                        startActivity(intent)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })




        return view
    }

    private fun replaceFragment(fragment : Fragment) {
        val fragmentManager = childFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.friendsFrameLayout, fragment)
        fragmentTransaction.commit()
    }


    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment FindAFriend.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            FindAFriend().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}