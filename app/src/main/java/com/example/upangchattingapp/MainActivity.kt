package com.example.upangchattingapp

import android.content.ClipData
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.example.upangchattingapp.databinding.ActivityMainBinding
import com.example.upangchattingapp.loginandregister.GettingStarted
import com.example.upangchattingapp.loginandregister.LogInAndRegister
import com.example.upangchattingapp.messaging.Chats
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlin.system.exitProcess


class MainActivity : AppCompatActivity() {

    private lateinit var binding : ActivityMainBinding
    private val auth = FirebaseAuth.getInstance()
    private val user = auth.currentUser
    private val userUid = user?.uid.toString()
    private val reference = FirebaseDatabase.getInstance().reference
    private var otherActivity: FindAFriend? = null

    private var backPressedTime: Long = 0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        otherActivity = FindAFriend()
        otherActivity?.let { lifecycle.addObserver(it) }

        Log.e("MyTag", user?.isEmailVerified.toString())

        val fragmentNumber = intent.getIntExtra("fragment", 1) // Get the fragment number from the extra

        when (fragmentNumber) {
            1 -> {
                replaceFragment(Chats())
                binding.bottomNav.selectedItemId = R.id.chats
                otherActivity?.onDestroy()
            }
            2 -> {
                replaceFragment(FindAFriend())
                binding.bottomNav.selectedItemId = R.id.findAFriend
                binding.cvEditProfile.visibility = GONE
                binding.cvSettings.visibility = GONE
                binding.cvAddFriend.visibility = VISIBLE
                binding.tvMessages.text = "Find a Friend"


            }
            3 -> {
                replaceFragment(Profile())
                binding.bottomNav.selectedItemId = R.id.profile
                binding.tvMessages.text = "Profile"
                binding.cvEditProfile.visibility = VISIBLE
                binding.cvSettings.visibility = VISIBLE
                binding.cvAddFriend.visibility = GONE
                otherActivity?.onDestroy()
            }
            else -> replaceFragment(Chats()) // Default fragment
        }



        reference.child("user").child(userUid).child("activeStatus").addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                val dataSnapshot = snapshot.getValue(String::class.java)
                Log.e("MyTag", dataSnapshot.toString())
                if(dataSnapshot.toString() != "Offline"){

                    val checkStatus = Intent(this@MainActivity, ChecksActiveStatus::class.java)
                    startService(checkStatus)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })

        if(user != null) {
            checkCourse()
        }

        binding.apply {
            bottomNav.setOnItemSelectedListener {


                when (it.itemId) {

                    R.id.chats -> chatFragment()
                    R.id.findAFriend -> findAFriendFragment()
                    R.id.profile -> profileFragment()
                    else -> {

                    }

                }
                true
            }

            cvAddFriend.setOnClickListener {
                finish()
                startActivity(Intent(this@MainActivity, SearchAddAFriend::class.java))
            }

            cvEditProfile.setOnClickListener {
                finish()
                startActivity(Intent(this@MainActivity, EditProfile::class.java))
            }

            cvSettings.setOnClickListener {
                finish()
                startActivity(Intent(this@MainActivity, Settings::class.java))
            }

        }








    }

    private fun replaceFragment(fragment : Fragment){

        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.frameLayout, fragment)
        fragmentTransaction.commit()

    }

    private fun chatFragment(){
        replaceFragment(Chats())
        binding.apply {

            tvMessages.text = "Messages"
//            cvCreateGroup.visibility = VISIBLE
            cvEditProfile.visibility = GONE
            cvSettings.visibility = GONE
            cvAddFriend.visibility = GONE
//            cvMessageRequest.visibility = VISIBLE

        }
    }

    private fun findAFriendFragment(){
        replaceFragment(FindAFriend())
        binding.apply {

            tvMessages.text = "Find a Friend"
//            cvCreateGroup.visibility = GONE
            cvEditProfile.visibility = GONE
            cvSettings.visibility = GONE
            cvAddFriend.visibility = VISIBLE
//            cvMessageRequest.visibility = GONE

        }
    }

    private fun profileFragment(){
        replaceFragment(Profile())
        binding.apply {

            tvMessages.text = "Profile"

            cvEditProfile.visibility = VISIBLE
            cvSettings.visibility = VISIBLE
            cvAddFriend.visibility = GONE
//            cvMessageRequest.visibility = GONE
            //            cvCreateGroup.visibility = GONE

        }
    }


    private fun checkCourse() {
        val rootRef = FirebaseDatabase.getInstance().reference.child("user").child(userUid)
        rootRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.hasChild("course")) {
                    //Course Exists
                } else {
                    //Year Level Exists
                    startActivity(Intent(this@MainActivity, GettingStarted::class.java))
                }
            }
            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(
                    this@MainActivity,
                    "ERROR",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    override fun onBackPressed() {

        finishAffinity()

    }

    override fun finish() {
        super.finish()
    }
}
