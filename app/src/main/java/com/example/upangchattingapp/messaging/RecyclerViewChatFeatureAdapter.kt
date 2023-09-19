package com.example.upangchattingapp.messaging

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.upangchattingapp.R
import java.util.*

class RecyclerViewChatFeatureAdapter(private val friendMessageCountList: MutableList<FriendsMessageCount> = mutableListOf()) :
    RecyclerView.Adapter<ViewHolderChatFeature>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolderChatFeature {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.chat_feature_item, parent, false )
        return ViewHolderChatFeature(view)
    }

    override fun getItemCount(): Int {
        return friendMessageCountList.size
    }

    override fun onBindViewHolder(holder: ViewHolderChatFeature, position: Int) {
        val item = friendMessageCountList[position]
        holder.bind(item.userName, item.userID)
    }
}

class ViewHolderChatFeature(private val view : View) : RecyclerView.ViewHolder(view){

    private val name = view.findViewById<TextView>(R.id.tvChatFeatureName)
    private val chatFeatureHolder = view.findViewById<LinearLayout>(R.id.llChatFeature)

    fun bind(userName : String, userID : String){
        if (userName == "null"){
            name.text = "Deleted Account"
        } else {
            name.text = userName.lowercase(Locale.getDefault()).split(" ").joinToString(" ") { it.capitalize(
                Locale.ROOT) }
        }

        chatFeatureHolder.setOnClickListener {
            val intentMessage = Intent(view.context, Messaging::class.java)
            intentMessage.putExtra("name", userName)
            intentMessage.putExtra("userID", userID)

            view.context.startActivity(intentMessage)
        }
    }
}