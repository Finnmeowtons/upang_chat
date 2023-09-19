package com.example.upangchattingapp.messaging

import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.example.upangchattingapp.R
import com.example.upangchattingapp.customDialogs.CustomDialogMessage
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class RecyclerViewMessageAdapter(private val conversationId: String) : RecyclerView.Adapter<MyMessageViewHolder>() {


    private val messages: MutableList<Message> = mutableListOf()


    fun addMessage(message: Message) {
        messages.add(message)
        notifyDataSetChanged()
    }

    fun setMessages(message: List<Message>) {
        messages.clear()
        messages.addAll(message)
        notifyDataSetChanged()
    }

    fun removeMessage(message: Message) {
        val position = messages.indexOf(message)
        messages.removeAt(position)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyMessageViewHolder {
        val item = LayoutInflater.from(parent.context).inflate(R.layout.message_item, parent, false)
        return MyMessageViewHolder(item)
    }

    override fun getItemCount(): Int {
        return messages.size
    }

    override fun onBindViewHolder(holder: MyMessageViewHolder, position: Int) {
        val message = messages[position]
        holder.bind(message.userID, message.message, message.timeStamp, conversationId)
    }
}

class MyMessageViewHolder(val view : View) : RecyclerView.ViewHolder(view){

    private val currentUserKey = FirebaseAuth.getInstance().currentUser?.uid
    private val reference = FirebaseDatabase.getInstance().reference


    fun bind(userID : String, messageContent : String, timeStamp : String, conversationID: String){

        val currentUserLayout = view.findViewById<ConstraintLayout>(R.id.clCurrentUserMessage)
        val otherUserLayout = view.findViewById<ConstraintLayout>(R.id.clOtherUserMessage)
        val currentUserTime = view.findViewById<TextView>(R.id.tvCurrentUserTime)
        val otherUserTime = view.findViewById<TextView>(R.id.tvOtherUserTime)
        val currentUserMessageHolder = view.findViewById<CardView>(R.id.cvCurrentUserMessageHolder)
        val otherUserMessageHolder = view.findViewById<TextView>(R.id.tvOtherUserMessage)
        val systemMessage = view.findViewById<TextView>(R.id.tvSystemMessage)


        currentUserTime.text = timeStamp
        otherUserTime.text = timeStamp


        if (userID == currentUserKey) {

            view.findViewById<TextView>(R.id.tvUserMessage).text = messageContent
            currentUserLayout.visibility = VISIBLE
            otherUserLayout.visibility = GONE
            systemMessage.visibility = GONE




            currentUserMessageHolder.setOnClickListener {
                currentUserTime.visibility = if (currentUserTime.visibility == VISIBLE) {
                    GONE
                } else {
                    VISIBLE
                }
            }

            currentUserMessageHolder.setOnLongClickListener {

                if (conversationID.contains("random")) {

                    val dialog = CustomDialogMessage(view.context, "current") { action ->

                        when (action) {
                            "remove" -> reference.child("RandomConversations").child(conversationID)
                                .child("messages").orderByChild("userID")
                                .equalTo(currentUserKey)
                                .addListenerForSingleValueEvent(object : ValueEventListener {
                                    override fun onDataChange(snapshot: DataSnapshot) {
                                        for (childSnapshot in snapshot.children) {
                                            val message =
                                                childSnapshot.getValue(Message::class.java)

                                            if (message != null && message.message == messageContent && message.timeStamp == timeStamp) {
                                                val uniqueKey = childSnapshot.key

                                                // Moving the message
                                                reference.child("RandomConversations")
                                                    .child(conversationID).child("deletedMessages")
                                                    .child(uniqueKey.toString())
                                                    .setValue(message)

                                                // Remove message from the messages parent
                                                childSnapshot.ref.removeValue()

                                            }
                                        }
                                    }

                                    override fun onCancelled(error: DatabaseError) {
                                        TODO("Not yet implemented")
                                    }
                                })
                        }
                    }
                    dialog.show()
                    true
                } else {

                    val dialog = CustomDialogMessage(view.context, "current") { action ->

                        when (action) {
                            "remove" -> reference.child("conversations").child(conversationID)
                                .child("messages").orderByChild("userID")
                                .equalTo(currentUserKey)
                                .addListenerForSingleValueEvent(object : ValueEventListener {
                                    override fun onDataChange(snapshot: DataSnapshot) {
                                        for (childSnapshot in snapshot.children) {
                                            val message =
                                                childSnapshot.getValue(Message::class.java)

                                            if (message != null && message.message == messageContent && message.timeStamp == timeStamp) {
                                                val uniqueKey = childSnapshot.key

                                                // Moving the message
                                                reference.child("conversations")
                                                    .child(conversationID).child("deletedMessages")
                                                    .child(uniqueKey.toString())
                                                    .setValue(message)

                                                // Remove message from the messages parent
                                                childSnapshot.ref.removeValue()

                                            }
                                        }
                                    }

                                    override fun onCancelled(error: DatabaseError) {
                                        TODO("Not yet implemented")
                                    }
                                })
                        }
                    }
                    dialog.show()
                    true
                }
            }

        } else if (userID == "..Admin") {

            currentUserLayout.visibility = GONE
            otherUserLayout.visibility = GONE
            systemMessage.visibility = VISIBLE
            systemMessage.text = messageContent

        } else {
            systemMessage.visibility = GONE
            otherUserMessageHolder.text = messageContent
            currentUserLayout.visibility = GONE
            otherUserLayout.visibility = VISIBLE

            view.findViewById<CardView>(R.id.cvOtherUserMessageHolder).setOnClickListener {
                otherUserTime.visibility = if (otherUserTime.visibility == VISIBLE){
                    GONE
                } else{
                    VISIBLE
                }
            }

            view.findViewById<CardView>(R.id.cvOtherUserMessageHolder).setOnLongClickListener {
                if (conversationID.contains("random")){

                    val dialog = CustomDialogMessage(view.context, "other") { action ->

                        when (action) {
                            "remove" -> reference.child("RandomConversations").child(conversationID)
                                .child("messages").orderByChild("userID")
                                .equalTo(currentUserKey)
                                .addListenerForSingleValueEvent(object : ValueEventListener {
                                    override fun onDataChange(snapshot: DataSnapshot) {
                                        for (childSnapshot in snapshot.children) {
                                            val message =
                                                childSnapshot.getValue(Message::class.java)

                                            if (message != null && message.message == messageContent && message.timeStamp == timeStamp) {
                                                val uniqueKey = childSnapshot.key

                                                // Moving the message
                                                reference.child("RandomConversations")
                                                    .child(conversationID).child("deletedMessages")
                                                    .child(uniqueKey.toString())
                                                    .setValue(message)

                                                // Remove message from the messages parent
                                                childSnapshot.ref.removeValue()

                                            }
                                        }
                                    }

                                    override fun onCancelled(error: DatabaseError) {
                                        TODO("Not yet implemented")
                                    }
                                })
                            "report" -> {
                                reference.child("RandomConversations").child(conversationID)
                                    .child("messages").orderByChild("userID")
                                    .equalTo(userID)
                                    .addListenerForSingleValueEvent(object : ValueEventListener {
                                        override fun onDataChange(snapshot: DataSnapshot) {
                                            for (childSnapshot in snapshot.children) {
                                                val message = childSnapshot.getValue(Message::class.java)

                                                if (message != null && message.message == messageContent && message.timeStamp == timeStamp) {
                                                    val uniqueKey = childSnapshot.key

                                                    reference.child("RandomConversations")
                                                        .child(conversationID).child("reportedMessage")
                                                        .child(uniqueKey.toString())
                                                        .setValue(message)

                                                }
                                            }
                                        }

                                        override fun onCancelled(error: DatabaseError) {
                                            TODO("Not yet implemented")
                                        }


                                    })
                            }
                        }
                    }
                    dialog.show()
                    true
                } else {

                    val dialog = CustomDialogMessage(view.context, "other") { action ->

                        when (action) {
                            "remove" -> reference.child("conversations").child(conversationID)
                                .child("messages").orderByChild("userID")
                                .equalTo(userID)
                                .addListenerForSingleValueEvent(object : ValueEventListener {
                                    override fun onDataChange(snapshot: DataSnapshot) {
                                        for (childSnapshot in snapshot.children) {
                                            val message =
                                                childSnapshot.getValue(Message::class.java)

                                            if (message != null && message.message == messageContent && message.timeStamp == timeStamp) {
                                                val uniqueKey = childSnapshot.key

                                                // Moving the message
                                                reference.child("conversations")
                                                    .child(conversationID).child("deletedMessages")
                                                    .child(uniqueKey.toString())
                                                    .setValue(message)

                                                // Remove message from the messages parent
                                                childSnapshot.ref.removeValue()

                                            }
                                        }
                                    }

                                    override fun onCancelled(error: DatabaseError) {
                                        TODO("Not yet implemented")
                                    }
                                })

                            "report" -> {
                                reference.child("conversations").child(conversationID)
                                    .child("messages").orderByChild("userID")
                                    .equalTo(userID)
                                    .addListenerForSingleValueEvent(object : ValueEventListener {
                                        override fun onDataChange(snapshot: DataSnapshot) {
                                            for (childSnapshot in snapshot.children) {
                                                val message = childSnapshot.getValue(Message::class.java)

                                                if (message != null && message.message == messageContent && message.timeStamp == timeStamp) {
                                                    val uniqueKey = childSnapshot.key

                                                    reference.child("conversations")
                                                        .child(conversationID).child("reportedMessage")
                                                        .child(uniqueKey.toString())
                                                        .setValue(message)

                                                }
                                            }
                                        }

                                        override fun onCancelled(error: DatabaseError) {
                                            TODO("Not yet implemented")
                                        }


                                    })
                            }
                        }
                    }
                    dialog.show()
                    true
                }
            }
        }



    }
}

//package com.example.upangchattingapp.messaging
//
//import android.content.Context
//import android.util.Log
//import android.view.LayoutInflater
//import android.view.TextureView
//import android.view.View
//import android.view.View.GONE
//import android.view.View.INVISIBLE
//import android.view.View.VISIBLE
//import android.view.ViewGroup
//import android.widget.TextView
//import androidx.cardview.widget.CardView
//import androidx.constraintlayout.widget.ConstraintLayout
//import androidx.core.content.ContentProviderCompat.requireContext
//import androidx.core.view.marginBottom
//import androidx.recyclerview.widget.LinearLayoutManager
//import androidx.recyclerview.widget.RecyclerView
//import com.example.upangchattingapp.R
//import com.example.upangchattingapp.customDialogs.CustomDialogMessage
//import com.google.firebase.auth.FirebaseAuth
//import com.google.firebase.database.DataSnapshot
//import com.google.firebase.database.DatabaseError
//import com.google.firebase.database.FirebaseDatabase
//import com.google.firebase.database.ValueEventListener
//
//class RecyclerViewMessageAdapter(private val conversationID : String) : RecyclerView.Adapter<MyMessageViewHolder>() {
//    private val messages: MutableList<Message> = mutableListOf()
//
//
//    fun addMessage(message: List<Message>) {
//        val startPosition = messages.size
//
//        messages.addAll(message)
//
//        Log.d("MyTag", "$startPosition, ${message.size}")
//        notifyItemRangeInserted(messages.size, message.size)
//    }
//
//    fun removeMessage(message: Message) {
//        val position = messages.indexOf(message)
//        messages.removeAt(position)
//        notifyDataSetChanged()
//    }
//
//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyMessageViewHolder {
//        val item = LayoutInflater.from(parent.context).inflate(R.layout.message_item, parent, false)
//        return MyMessageViewHolder(item)
//    }
//
//    override fun getItemCount(): Int {
//        return messages.size
//    }
//
//    override fun onBindViewHolder(holder: MyMessageViewHolder, position: Int) {
//        val message = messages[position]
//        val previousMessage = if(position > 0){
//            messages[position - 1]
//        }else {
//            null
//        }
//        val nextMessage = if(position < messages.size - 1){
//            messages[position + 1]
//        }else {
//            null
//        }
//        holder.bind(message.userID, message.message, message.timeStamp,
//            nextMessage?.userID.toString(), previousMessage?.userID.toString(),
//            conversationID)
//
//    }
//
//
//
//}

//class MyMessageViewHolder(val view : View) : RecyclerView.ViewHolder(view){
//
//    private val currentUserKey = FirebaseAuth.getInstance().currentUser?.uid
//    private val reference = FirebaseDatabase.getInstance().reference
//
//
//    fun bind(userID : String, messageContent : String, timeStamp : String, nextMessage : String, previousMessage : String, conversationID: String){
//
//        val currentUserLayout = view.findViewById<ConstraintLayout>(R.id.clCurrentUserMessage)
//        val otherUserLayout = view.findViewById<ConstraintLayout>(R.id.clOtherUserMessage)
//        val otherUserProfilePicture = view.findViewById<CardView>(R.id.cvProfileHolder)
//        val currentUserTime = view.findViewById<TextView>(R.id.tvCurrentUserTime)
//        val otherUserTime = view.findViewById<TextView>(R.id.tvOtherUserTime)
//        val currentUserMessageHolder = view.findViewById<CardView>(R.id.cvCurrentUserMessageHolder)
//        val otherUserMessageHolder = view.findViewById<TextView>(R.id.tvOtherUserMessage)
//        val systemMessage = view.findViewById<TextView>(R.id.tvSystemMessage)
//
//        currentUserTime.text = timeStamp
//        otherUserTime.text = timeStamp
//
//        if (userID == currentUserKey) {
//
//            view.findViewById<TextView>(R.id.tvUserMessage).text = messageContent
//            currentUserLayout.visibility = VISIBLE
//            otherUserLayout.visibility = GONE
//            systemMessage.visibility = GONE
//
//
//            currentUserMessageHolder.setOnClickListener {
//                currentUserTime.visibility = if (currentUserTime.visibility == VISIBLE){
//                    GONE
//                } else{
//                    VISIBLE
//                }
//            }
//
//            currentUserMessageHolder.setOnLongClickListener {
//
//                if (conversationID.contains("random")){
//
//                    val dialog = CustomDialogMessage(view.context, "current") { action ->
//
//                        when (action) {
//                            "remove" -> reference.child("RandomConversations").child(conversationID)
//                                .child("messages").orderByChild("userID")
//                                .equalTo(currentUserKey)
//                                .addListenerForSingleValueEvent(object : ValueEventListener {
//                                    override fun onDataChange(snapshot: DataSnapshot) {
//                                        for (childSnapshot in snapshot.children) {
//                                            val message =
//                                                childSnapshot.getValue(Message::class.java)
//
//                                            if (message != null && message.message == messageContent && message.timeStamp == timeStamp) {
//                                                val uniqueKey = childSnapshot.key
//
//                                                // Moving the message
//                                                reference.child("RandomConversations")
//                                                    .child(conversationID).child("deletedMessages")
//                                                    .child(uniqueKey.toString())
//                                                    .setValue(message)
//
//                                                // Remove message from the messages parent
//                                                childSnapshot.ref.removeValue()
//
//                                            }
//                                        }
//                                    }
//
//                                    override fun onCancelled(error: DatabaseError) {
//                                        TODO("Not yet implemented")
//                                    }
//                                })
//                        }
//                    }
//                    dialog.show()
//                    true
//                } else {
//
//                    val dialog = CustomDialogMessage(view.context, "current") { action ->
//
//                        when (action) {
//                            "remove" -> reference.child("conversations").child(conversationID)
//                                .child("messages").orderByChild("userID")
//                                .equalTo(currentUserKey)
//                                .addListenerForSingleValueEvent(object : ValueEventListener {
//                                    override fun onDataChange(snapshot: DataSnapshot) {
//                                        for (childSnapshot in snapshot.children) {
//                                            val message =
//                                                childSnapshot.getValue(Message::class.java)
//
//                                            if (message != null && message.message == messageContent && message.timeStamp == timeStamp) {
//                                                val uniqueKey = childSnapshot.key
//
//                                                // Moving the message
//                                                reference.child("conversations")
//                                                    .child(conversationID).child("deletedMessages")
//                                                    .child(uniqueKey.toString())
//                                                    .setValue(message)
//
//                                                // Remove message from the messages parent
//                                                childSnapshot.ref.removeValue()
//
//                                            }
//                                        }
//                                    }
//
//                                    override fun onCancelled(error: DatabaseError) {
//                                        TODO("Not yet implemented")
//                                    }
//                                })
//                        }
//                    }
//                    dialog.show()
//                    true
//                }
//            }
//
//        } else if (userID == "..Admin") {
//
//            currentUserLayout.visibility = GONE
//            otherUserLayout.visibility = GONE
//            systemMessage.visibility = VISIBLE
//            systemMessage.text = messageContent
//
//        } else {
//            systemMessage.visibility = GONE
//            otherUserMessageHolder.text = messageContent
//            currentUserLayout.visibility = GONE
//            otherUserLayout.visibility = VISIBLE
//
//            view.findViewById<CardView>(R.id.cvOtherUserMessageHolder).setOnClickListener {
//                otherUserTime.visibility = if (otherUserTime.visibility == VISIBLE){
//                    GONE
//                } else{
//                    VISIBLE
//                }
//            }
//
//            view.findViewById<CardView>(R.id.cvOtherUserMessageHolder).setOnLongClickListener {
//                if (conversationID.contains("random")){
//
//                    val dialog = CustomDialogMessage(view.context, "other") { action ->
//
//                        when (action) {
//                            "remove" -> reference.child("RandomConversations").child(conversationID)
//                                .child("messages").orderByChild("userID")
//                                .equalTo(currentUserKey)
//                                .addListenerForSingleValueEvent(object : ValueEventListener {
//                                    override fun onDataChange(snapshot: DataSnapshot) {
//                                        for (childSnapshot in snapshot.children) {
//                                            val message =
//                                                childSnapshot.getValue(Message::class.java)
//
//                                            if (message != null && message.message == messageContent && message.timeStamp == timeStamp) {
//                                                val uniqueKey = childSnapshot.key
//
//                                                // Moving the message
//                                                reference.child("RandomConversations")
//                                                    .child(conversationID).child("deletedMessages")
//                                                    .child(uniqueKey.toString())
//                                                    .setValue(message)
//
//                                                // Remove message from the messages parent
//                                                childSnapshot.ref.removeValue()
//
//                                            }
//                                        }
//                                    }
//
//                                    override fun onCancelled(error: DatabaseError) {
//                                        TODO("Not yet implemented")
//                                    }
//                                })
//                            "report" -> {
//                                reference.child("RandomConversations").child(conversationID)
//                                    .child("messages").orderByChild("userID")
//                                    .equalTo(userID)
//                                    .addListenerForSingleValueEvent(object : ValueEventListener {
//                                        override fun onDataChange(snapshot: DataSnapshot) {
//                                            for (childSnapshot in snapshot.children) {
//                                                val message = childSnapshot.getValue(Message::class.java)
//
//                                                if (message != null && message.message == messageContent && message.timeStamp == timeStamp) {
//                                                    val uniqueKey = childSnapshot.key
//
//                                                    reference.child("RandomConversations")
//                                                        .child(conversationID).child("reportedMessage")
//                                                        .child(uniqueKey.toString())
//                                                        .setValue(message)
//
//                                                }
//                                            }
//                                        }
//
//                                        override fun onCancelled(error: DatabaseError) {
//                                            // Handle the error appropriately
//                                        }
//                                    })
//                            }
//                        }
//                    }
//                    dialog.show()
//                    true
//                } else {
//
//                    val dialog = CustomDialogMessage(view.context, "other") { action ->
//
//                        when (action) {
//                            "remove" -> reference.child("conversations").child(conversationID)
//                                .child("messages").orderByChild("userID")
//                                .equalTo(userID)
//                                .addListenerForSingleValueEvent(object : ValueEventListener {
//                                    override fun onDataChange(snapshot: DataSnapshot) {
//                                        for (childSnapshot in snapshot.children) {
//                                            val message =
//                                                childSnapshot.getValue(Message::class.java)
//
//                                            if (message != null && message.message == messageContent && message.timeStamp == timeStamp) {
//                                                val uniqueKey = childSnapshot.key
//
//                                                // Moving the message
//                                                reference.child("conversations")
//                                                    .child(conversationID).child("deletedMessages")
//                                                    .child(uniqueKey.toString())
//                                                    .setValue(message)
//
//                                                // Remove message from the messages parent
//                                                childSnapshot.ref.removeValue()
//
//                                            }
//                                        }
//                                    }
//
//                                    override fun onCancelled(error: DatabaseError) {
//                                        TODO("Not yet implemented")
//                                    }
//                                })
//
//                            "report" -> {
//                                reference.child("conversations").child(conversationID)
//                                    .child("messages").orderByChild("userID")
//                                    .equalTo(userID)
//                                    .addListenerForSingleValueEvent(object : ValueEventListener {
//                                        override fun onDataChange(snapshot: DataSnapshot) {
//                                            for (childSnapshot in snapshot.children) {
//                                                val message = childSnapshot.getValue(Message::class.java)
//
//                                                if (message != null && message.message == messageContent && message.timeStamp == timeStamp) {
//                                                    val uniqueKey = childSnapshot.key
//
//                                                    reference.child("conversations")
//                                                        .child(conversationID).child("reportedMessage")
//                                                        .child(uniqueKey.toString())
//                                                        .setValue(message)
//
//                                                }
//                                            }
//                                        }
//
//                                        override fun onCancelled(error: DatabaseError) {
//                                            // Handle the error appropriately
//                                        }
//                                    })
//                            }
//                        }
//                    }
//                    dialog.show()
//                    true
//                }
//            }
//        }
//
//        if (nextMessage == userID){
//            otherUserProfilePicture.visibility = INVISIBLE
//        } else {
//            otherUserProfilePicture.visibility = VISIBLE
//        }
//
//    }
//}