package com.example.upangchattingapp

import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ChecksActiveStatus : Service() {

    private val ref =  FirebaseDatabase.getInstance().reference
    private val onlineList = ref.child("OnlineUserList")
    private val currentUserKey = FirebaseAuth.getInstance().currentUser?.uid

    override fun onBind(intent: Intent?): IBinder? {
        TODO("Not yet implemented")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        ref.child(currentUserKey.toString()).child("activeStatus").addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                val activeStatus = snapshot.getValue(String::class.java)

                if (activeStatus != "Offline"){
                    onlineList.child(currentUserKey.toString()).setValue(true)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })



        onlineList.child(currentUserKey.toString()).onDisconnect().removeValue()
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {

        onlineList.child(currentUserKey.toString()).removeValue()
        super.onDestroy()
    }
}