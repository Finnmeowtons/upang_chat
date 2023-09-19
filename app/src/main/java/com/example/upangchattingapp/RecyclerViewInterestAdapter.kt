package com.example.upangchattingapp

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.AppCompatCheckBox
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class RecyclerViewInterestAdapter(private val interestList : Array<String>
) : RecyclerView.Adapter<ViewHolderInterest>() {

    private val selectedItems = HashSet<String>()
    private val reference = FirebaseDatabase.getInstance().reference
    private val currentUserKey = FirebaseAuth.getInstance().currentUser?.uid

    init {
        getInterests {
            notifyDataSetChanged()
        }
    }

    private fun getInterests(callback: () -> Unit) {

        reference.child("user").child(currentUserKey.toString()).child("interests")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        for (dataSnapshot in snapshot.children) {
                            Log.e("MyTag", dataSnapshot.key.toString())
                            selectedItems.add(dataSnapshot.key.toString())
                        }
                    }
                    callback.invoke()
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }
            })
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolderInterest {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.interest_item, parent, false)



        return ViewHolderInterest(view)
    }

    override fun getItemCount(): Int {
        return interestList.size / 2
    }

    override fun onBindViewHolder(holder: ViewHolderInterest, position: Int) {
        val view = holder.itemView
        val itemIndex = position * 2

        if (itemIndex < interestList.size) {
            val item = interestList[itemIndex]
            val item2 = if (itemIndex + 1 < interestList.size) interestList[itemIndex + 1] else null

            if (item2 != null) {
                holder.bind(item, item2)
            }

            val checkBox = view.findViewById<AppCompatCheckBox>(R.id.check)
            val checkBox2 = view.findViewById<AppCompatCheckBox>(R.id.check2)
            checkBox.isChecked = selectedItems.contains(item)
            checkBox2.isChecked = selectedItems.contains(item2)

            checkBox.setOnClickListener {
                val checked = checkBox.isChecked
                if (checked) {
                    selectedItems.add(item)
                } else {
                    selectedItems.remove(item)
                }
            }

            checkBox2.setOnClickListener {
                val checked = checkBox2.isChecked
                if (checked) {
                    if (item2 != null) {
                        selectedItems.add(item2)
                    }
                } else {
                    selectedItems.remove(item2)
                }
            }
        }


    }

    fun getSelectedItems():HashSet<String> {


        return selectedItems
    }


}

class ViewHolderInterest(private val view : View) : RecyclerView.ViewHolder(view) {

    fun bind(item: String, item2: String) {

        val firstItemText = view.findViewById<TextView>(R.id.tvInterest)
        val firstItemTextBorder = view.findViewById<TextView>(R.id.tvInterestBorder)
        val secondItemText = view.findViewById<TextView>(R.id.tvInterest2)
        val secondItemTextBorder = view.findViewById<TextView>(R.id.tvInterest2Border)

        val image = view.findViewById<ImageView>(R.id.imgInterest)
        val image2 = view.findViewById<ImageView>(R.id.imgInterest2)



        firstItemText.text = item
        firstItemTextBorder.text = item
        val formattedImageName = if (item.contains(" ")) {
            item.lowercase().replace(" ", "_")
        } else {
            item.lowercase().replace("-", "_")
        }
        val resourceId =
            view.resources.getIdentifier(formattedImageName, "drawable", view.context.packageName)
        if (resourceId != 0) {
            Glide.with(view)
                .load(resourceId)
                .into(image)
        } else {
            // resource not found
            Glide.with(view)
                .load(R.drawable.basketball)
                .into(image)
        }
        Log.e("MyTag", item)
        Log.e("MyTag", formattedImageName)


        secondItemText.text = item2
        secondItemTextBorder.text = item2
        val formattedImageName2 = if (item2.contains(" ")) {
            item2.lowercase().replace(" ", "_")
        } else {
            item2.lowercase().replace("-", "_")
        }


        val resourceId2 =
            view.resources.getIdentifier(formattedImageName2, "drawable", view.context.packageName)
        if (resourceId2 != 0) {
            Glide.with(view)
                .load(resourceId2)
                .into(image2)
        } else {
            // resource not found
            Glide.with(view)
                .load(R.drawable.basketball)
                .into(image2)
        }
        Log.e("MyTag", item2)
        Log.e("MyTag", formattedImageName2)


    }
}