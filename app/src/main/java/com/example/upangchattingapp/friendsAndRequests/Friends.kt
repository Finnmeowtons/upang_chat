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

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [Friends.newInstance] factory method to
 * create an instance of this fragment.
 */
class Friends : Fragment() {
    private val userRef = FirebaseDatabase.getInstance().reference.child("user")
    private val currentUserKey = FirebaseAuth.getInstance().currentUser?.uid
    private val friendsRef = userRef.child(currentUserKey.toString()).child("Friends")
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
        val view = inflater.inflate(R.layout.fragment_friends, container, false)
        val progressBar = view.findViewById<ProgressBar>(R.id.progressBar)

        val recyclerView = view.findViewById<RecyclerView>(R.id.rvFriendList)
        recyclerView.layoutManager = LinearLayoutManager(view.context)



        friendsRef.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                Log.d("asd", "Friends 1")
                if (!snapshot.hasChildren()){
                    progressBar.visibility = GONE
                }
                val friendList = mutableListOf<User>()
                for(dataSnapshot in snapshot.children){
                    val userKey = dataSnapshot.key
                    userRef.child(userKey.toString()).addListenerForSingleValueEvent(object : ValueEventListener{
                        override fun onDataChange(snapshot: DataSnapshot) {
                            Log.d("asd", "Friends 2")
                            val user = snapshot.getValue(User::class.java)
                            if (user != null) {
                                friendList.add(user)
                            }
                            val adapter = RecyclerViewFriendRequestsAdapter(friendList, R.layout.friend_list)
                            recyclerView.adapter = adapter

                            progressBar.visibility = GONE

                            userRef.child(currentUserKey.toString()).child("Friends").addChildEventListener(object : ChildEventListener {
                                override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                                    Log.e("asd", snapshot.toString())
                                }

                                override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                                    // Implement your logic when a child is changed
                                }

                                override fun onChildRemoved(snapshot: DataSnapshot) {
                                    Log.d("asd", "Friends 3")
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



        return view
    }

    override fun onDestroy() {
        super.onDestroy()
        friendsRef.removeEventListener(valueEventListener)

        friendsRef.removeEventListener(childEventListener)



    }



    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment Friends.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            Friends().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}