package com.example.upangchattingapp.friendsAndRequests

import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.upangchattingapp.OtherUsersProfile
import com.example.upangchattingapp.R
import com.example.upangchattingapp.User
import com.example.upangchattingapp.messaging.Message
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.Locale

class RecyclerViewFriendRequestsAdapter(private val friendRequestReceivedList : MutableList<User>, private val layout : Int) : RecyclerView.Adapter<MyFriendRequestsViewHolder>() {

    fun removeUser(removeFriend : FriendsData) {
        val listFren = mutableListOf<FriendsData>()
        for (fren in friendRequestReceivedList){
            val userID = fren.userID

            listFren.add(FriendsData(userID, true))
        }

        if (listFren.size >= friendRequestReceivedList.size) {


            val position = listFren.indexOf(removeFriend)
            if (position != -1 && friendRequestReceivedList.size != 0) {
                val removeTheUser = friendRequestReceivedList.elementAt(position)
                Log.d("asd", "$position, ${listFren.size}, ${friendRequestReceivedList.size}")
                notifyItemRemoved(position)

                friendRequestReceivedList.remove(removeTheUser)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyFriendRequestsViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(layout, parent, false)
        return MyFriendRequestsViewHolder(view)
    }

    override fun getItemCount(): Int {
        return friendRequestReceivedList.size
    }

    override fun onBindViewHolder(holder: MyFriendRequestsViewHolder, position: Int) {
        if (friendRequestReceivedList.size >= position) {
            val user = friendRequestReceivedList[position]

            holder.bind(user.userName, user.course, user.yearLevel, user.userID, user.aboutMe)
        }
    }
}

class MyFriendRequestsViewHolder (val view : View): RecyclerView.ViewHolder(view){

    private val databaseRef = FirebaseDatabase.getInstance().reference
    private val currentUserKey = FirebaseAuth.getInstance().currentUser?.uid

    private var image : Int = R.drawable.baseline_close_24
    private  lateinit var friendTextButton : String

    fun bind(userName : String, course : String, yearLevel : String, userID : String, aboutMe : String){



        val otherName = view.findViewById<TextView>(R.id.tvOtherName)
        val otherCourse = view.findViewById<TextView>(R.id.tvOtherCourse)
        val otherYearLevel = view.findViewById<TextView>(R.id.tvOtherYearLevel)
        val acceptButton = view.findViewById<Button>(R.id.btnRequestAccept)
        val declineButton = view.findViewById<Button>(R.id.btnRequestDecline)



        acceptButton.setOnClickListener {
            databaseRef.child("user").child(userID).child("Friends").child(currentUserKey.toString()).setValue(true)
            databaseRef.child("user").child(currentUserKey.toString()).child("Friends").child(userID).setValue(true)

            databaseRef.child("user").child(userID)
                .child("SentFriendRequests").child(currentUserKey.toString()).removeValue()

            databaseRef.child("user").child(currentUserKey.toString())
                .child("IncomingFriendRequests").child(userID).removeValue()

        }

        declineButton.setOnClickListener {
            if (declineButton.text == "Decline") {

                databaseRef.child("user").child(userID)
                    .child("SentFriendRequests").child(currentUserKey.toString()).removeValue()

                databaseRef.child("user").child(currentUserKey.toString())
                    .child("IncomingFriendRequests").child(userID).removeValue()



            }else {

                databaseRef.child("user").child(currentUserKey.toString())
                    .child("SentFriendRequests").child(userID).removeValue()

                databaseRef.child("user").child(userID)
                    .child("IncomingFriendRequests").child(currentUserKey.toString()).removeValue()
            }
        }



        otherName.text = userName.lowercase(Locale.getDefault()).split(" ").joinToString(" ") { it.capitalize(
            Locale.ROOT) }
        otherCourse.text = course
        otherYearLevel.text = yearLevel

        databaseRef.child("user").child(currentUserKey.toString())
            .child("SentFriendRequests").child(userID).addListenerForSingleValueEvent(object :
                ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if(snapshot.exists()){
                        //If the current users sent a friend request to the other user
                        image = R.drawable.sent_friend_request
                        friendTextButton = "Pending Request"
                    } else {
                        databaseRef.child("user").child(currentUserKey.toString())
                            .child("IncomingFriendRequests").child(userID).addListenerForSingleValueEvent(object :
                                ValueEventListener {
                                override fun onDataChange(snapshot: DataSnapshot) {
                                    if(snapshot.exists()){
                                        //If the current user has a friend request from the other user
                                        image = R.drawable.incoming_friend_request
                                        friendTextButton = "Respond"
                                    } else {
                                        databaseRef.child("user").child(currentUserKey.toString())
                                            .child("Friends").child(userID).addListenerForSingleValueEvent(object :
                                                ValueEventListener {
                                                override fun onDataChange(snapshot: DataSnapshot) {
                                                    if(snapshot.exists()){
                                                        //Friends
                                                        image = R.drawable.friends
                                                        friendTextButton = "Friends"
                                                    } else {
                                                        //No interactions
                                                        image = R.drawable.baseline_person_add_24
                                                        friendTextButton = "Add Friend"
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

        if (userID != currentUserKey) {

            val otherUsersProfile = view.findViewById<RelativeLayout>(R.id.rlFriendList)
            otherUsersProfile.setOnClickListener {

                val intent = Intent(view.context, OtherUsersProfile::class.java)
                intent.putExtra("yearLevel", yearLevel)
                intent.putExtra("name", userName)
                intent.putExtra("course", course)
                intent.putExtra("userID", userID)
                intent.putExtra("image", image)
                intent.putExtra("friendTextButton", friendTextButton)
                intent.putExtra("aboutMe", aboutMe)

                view.context.startActivity(intent)

            }
        }

    }
}