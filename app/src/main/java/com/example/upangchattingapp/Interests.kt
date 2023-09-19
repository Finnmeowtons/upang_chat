package com.example.upangchattingapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View.VISIBLE
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.upangchattingapp.databinding.ActivityInterestsBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class Interests : AppCompatActivity() {

    private lateinit var binding : ActivityInterestsBinding
    private val reference = FirebaseDatabase.getInstance().reference
    private val currentUserKey = FirebaseAuth.getInstance().currentUser?.uid

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInterestsBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)




        val mode = intent.getStringExtra("mode")

        binding.rvInterests.layoutManager = LinearLayoutManager(this)
        val interests = resources.getStringArray(R.array.Interests)
        val adapter = RecyclerViewInterestAdapter(interests)
        binding.rvInterests.adapter = adapter

        if (mode == "start") {
            binding.btnSubmit.setOnClickListener {

                binding.progressBar.visibility = VISIBLE
                val selectedItems = adapter.getSelectedItems()

                if (selectedItems.isEmpty()){
                    startActivity(Intent(this@Interests, MainActivity::class.java))
                }

                for (item in selectedItems) {
                    reference.child("user").child(currentUserKey.toString()).child("interests")
                        .child(item).setValue(true)
                        .addOnSuccessListener {
                            startActivity(Intent(this@Interests, MainActivity::class.java))
                        }
                }

                startActivity(Intent(this@Interests, MainActivity::class.java))
            }
        } else {

            binding.btnBackInterest.visibility = VISIBLE

            binding.btnBackInterest.setOnClickListener {
                startActivity(Intent(this@Interests, EditProfile::class.java))
            }

            binding.btnSubmit.setOnClickListener {

                binding.progressBar.visibility = VISIBLE
                val selectedItems = adapter.getSelectedItems()

                if (selectedItems.isEmpty()){
                    startActivity(Intent(this@Interests, EditProfile::class.java))
                }

                //Delete
                reference.child("user").child(currentUserKey.toString()).child("interests").addListenerForSingleValueEvent(object : ValueEventListener{
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val interestList = mutableListOf<String>()
                        for (dataSnapshot in snapshot.children){
                            interestList.add(dataSnapshot.key.toString())
                        }
                        val removed = interestList.subtract(selectedItems)
                        for (removedItem in removed) {
                            reference.child("user").child(currentUserKey.toString())
                                .child("interests").child(removedItem).removeValue()
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        TODO("Not yet implemented")
                    }
                })

                //Add
                for (item in selectedItems) {
                    reference.child("user").child(currentUserKey.toString()).child("interests")
                        .child(item).setValue(true)
                        .addOnSuccessListener {
                            startActivity(Intent(this@Interests, EditProfile::class.java))
                        }
                }

                startActivity(Intent(this@Interests, EditProfile::class.java))
            }

            binding.btnSubmit.text = "Submit"

        }
    }

}