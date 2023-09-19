package com.example.upangchattingapp

import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.Locale

class ListViewSearchListAdapter(private val searchResults: List<User>) : BaseAdapter(){

    private var currentItemCount = 1

    override fun getCount(): Int {
        return currentItemCount
    }

    override fun getItem(position: Int): Any {
        return searchResults[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view: View
        val holder: MySearchListViewHolder

        if(searchResults.isEmpty()){
            view = LayoutInflater.from(parent?.context).inflate(R.layout.no_results, parent, false)
            holder = MySearchListViewHolder(view)
            view.tag = holder
            return view
        }


        if (convertView == null) {
            view = LayoutInflater.from(parent?.context).inflate(R.layout.item_search_result, parent, false)
            holder = MySearchListViewHolder(view)
            view.tag = holder
        } else {
            view = convertView
            holder = view.tag as MySearchListViewHolder
        }



        val user = searchResults[position]
        holder.bind(user.userName, user.course, user.yearLevel, user.userID)

        if (position == currentItemCount - 1 && currentItemCount < searchResults.size) {
            loadMoreItems()
        }

        return view
    }
    private fun loadMoreItems() {
        val remainingItems = searchResults.size - currentItemCount
        val itemsToLoad = minOf(1, remainingItems)
        currentItemCount += itemsToLoad
        notifyDataSetChanged()
    }
}

class MySearchListViewHolder(val view : View){

    private var currentUserKey = FirebaseAuth.getInstance().currentUser?.uid
    private var databaseRef = FirebaseDatabase.getInstance().reference

    private var image : Int = 0
    private lateinit var friendTextButton : String

    fun bind(name : String, course : String, yearLevel : String, userID : String){



        val profileName = view.findViewById<TextView>(R.id.tvSearchName)
        val profileCourse = view.findViewById<TextView>(R.id.tvSearchCourse)
        val profileYearLevel = view.findViewById<TextView>(R.id.tvSearchYear)

        profileName.text = name.lowercase(Locale.getDefault()).split(" ").joinToString(" ") { it.capitalize(
            Locale.ROOT) }
        profileCourse.text = course
        profileYearLevel.text = yearLevel

        databaseRef.child("user").child(currentUserKey.toString())
            .child("SentFriendRequests").child(userID).addValueEventListener(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    if(snapshot.exists()){
                        //If the current users sent a friend request to the other user
                        image = R.drawable.sent_friend_request
                        friendTextButton = "Pending Request"
                    } else {
                        databaseRef.child("user").child(currentUserKey.toString())
                            .child("IncomingFriendRequests").child(userID).addListenerForSingleValueEvent(object : ValueEventListener{
                                override fun onDataChange(snapshot: DataSnapshot) {
                                    if(snapshot.exists()){
                                        //If the current user has a friend request from the other user
                                        image = R.drawable.incoming_friend_request
                                        friendTextButton = "Respond"
                                    } else {
                                        databaseRef.child("user").child(currentUserKey.toString())
                                            .child("Friends").child(userID).addListenerForSingleValueEvent(object : ValueEventListener{
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

        val otherUsersProfile = view.findViewById<RelativeLayout>(R.id.rlOtherUserProfile)
        otherUsersProfile.setOnClickListener {

            val intent = Intent(view.context, OtherUsersProfile::class.java)
            intent.putExtra("yearLevel", yearLevel)
            intent.putExtra("name", name)
            intent.putExtra("course", course)
            intent.putExtra("userID", userID)
            intent.putExtra("image", image)
            intent.putExtra("friendTextButton", friendTextButton)

            view.context.startActivity(intent)
        }



    }
}

