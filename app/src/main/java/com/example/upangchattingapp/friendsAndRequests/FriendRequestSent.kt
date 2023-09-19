package com.example.upangchattingapp.friendsAndRequests

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
import com.example.upangchattingapp.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.getValue

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [FriendRequestSent.newInstance] factory method to
 * create an instance of this fragment.
 */
class FriendRequestSent : Fragment() {


    private val currentUserKey = FirebaseAuth.getInstance().currentUser?.uid
    private val userRef = FirebaseDatabase.getInstance().reference.child("user")
    private val sentFriendRequest = userRef.child(currentUserKey.toString()).child("SentFriendRequests")
    private val valueEventListener = object : ValueEventListener{
        override fun onDataChange(snapshot: DataSnapshot) {
            TODO("Not yet implemented")
        }

        override fun onCancelled(error: DatabaseError) {
            TODO("Not yet implemented")
        }
    }
    private val childEventListener = object : ChildEventListener{
        override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
            TODO("Not yet implemented")
        }

        override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
            TODO("Not yet implemented")
        }

        override fun onChildRemoved(snapshot: DataSnapshot) {
            TODO("Not yet implemented")
        }

        override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
            TODO("Not yet implemented")
        }

        override fun onCancelled(error: DatabaseError) {
            TODO("Not yet implemented")
        }
    }
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
        val view = inflater.inflate(R.layout.fragment_friend_request_sent, container, false)
        val progressBar = view.findViewById<ProgressBar>(R.id.progressBar)

        val recyclerView = view.findViewById<RecyclerView>(R.id.rvFriendRequestsSentList)
        recyclerView.layoutManager = LinearLayoutManager(view.context)


        sentFriendRequest.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                Log.d("MyTag", "FriendSent 1")
                if (!snapshot.hasChildren()){
                    progressBar.visibility = GONE
                }
                val sentFriendRequestList = mutableListOf<User>()
                for(dataSnapshot in snapshot.children){
                    val userKey = dataSnapshot.key
                    userRef.child(userKey.toString()).addListenerForSingleValueEvent(object : ValueEventListener{
                        override fun onDataChange(snapshot: DataSnapshot) {
                            Log.d("asd", "Friendsent 2")
                            val user = snapshot.getValue(User::class.java)
                            if (user != null) {
                                sentFriendRequestList.add(user)
                            }
                            val adapter = RecyclerViewFriendRequestsAdapter(sentFriendRequestList, R.layout.friend_request_sent_list)
                            recyclerView.adapter = adapter

                            progressBar.visibility = GONE

                            sentFriendRequest.addChildEventListener(object : ChildEventListener{
                                override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                                    Log.e("asd", snapshot.toString())
                                }

                                override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                                    TODO("Not yet implemented")
                                }

                                override fun onChildRemoved(snapshot: DataSnapshot) {
                                    Log.e("asd", "removeddd $snapshot")
                                    val removeUserKey = snapshot.key
                                    val boolean = snapshot.value

                                    if (removeUserKey != null && boolean != null) {
                                        val removeSentFriendRequestList =
                                            FriendsData(removeUserKey, boolean as Boolean)
                                        adapter.removeUser(removeSentFriendRequestList)
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



    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment FriendRequests.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            FriendRequestSent().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}