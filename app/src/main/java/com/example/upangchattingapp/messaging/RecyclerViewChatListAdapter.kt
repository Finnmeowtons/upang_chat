package com.example.upangchattingapp.messaging

import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.upangchattingapp.R
import com.example.upangchattingapp.customDialogs.CustomDialogFriendRequest
import com.example.upangchattingapp.customDialogs.CustomDialogMessage
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.Locale

class RecyclerViewChatListAdapter(private val conversationList: MutableList<ChatList> = mutableListOf<ChatList>()) : RecyclerView.Adapter<MyChatListViewHolder>() {

//    fun removeConversation(item : String?){
//        Log.e("MyTag", item)
//        val convoList = mutableListOf<String>()
//        for (convoItem in conversationList){
//            convoList.add(convoItem.conversationID)
//        }
//        Log.e("MyTag", convoList.toString())
//
//        val position = convoList.indexOf(item)
//        conversationList.removeAt(position)
//        notifyItemRemoved(position)
//    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyChatListViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.list_chattedpeople, parent, false)
        return  MyChatListViewHolder(view)
    }

    override fun getItemCount(): Int {
        return conversationList.size

    }

    override fun onBindViewHolder(holder: MyChatListViewHolder, position: Int) {
        val item = conversationList[position]

        holder.bind(item.message, item.timeStamp, item.userName, item.userID)
    }
}

class MyChatListViewHolder(val view : View) : RecyclerView.ViewHolder(view){

    private var name = view.findViewById<TextView>(R.id.tvName)
    private val message = view.findViewById<TextView>(R.id.tvLastMessage)
    private val clickChat = view.findViewById<RelativeLayout>(R.id.rlChatList)

    private val reference = FirebaseDatabase.getInstance().reference
    private val currentUserKey = FirebaseAuth.getInstance().currentUser?.uid.toString()


    fun bind(lastMessage : String, latestTimeStamp : String, userName : String, userID : String){





        if (userName == "null"){
            name.text = "User"
        } else {
            name.text = userName.lowercase(Locale.getDefault()).split(" ").joinToString(" ") { it.capitalize(
                Locale.ROOT) }
        }
        message.text = lastMessage

        clickChat.setOnClickListener {
            val intentMessage = Intent(view.context, Messaging::class.java)
            intentMessage.putExtra("name", userName)
            intentMessage.putExtra("userID", userID)

            view.context.startActivity(intentMessage)
        }

        clickChat.setOnLongClickListener {

            val dialog = CustomDialogMessage(view.context, "current") { action ->

                if (action == "remove") {

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
            dialog.show()
            true
        }

    }
}