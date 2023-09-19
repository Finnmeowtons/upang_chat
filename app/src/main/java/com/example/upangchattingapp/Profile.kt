package com.example.upangchattingapp

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import com.example.upangchattingapp.customDialogs.CustomDialogStatusActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.getValue
import java.util.Locale

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [Profile.newInstance] factory method to
 * create an instance of this fragment.
 */
class Profile : Fragment() {


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
        val view =inflater.inflate(R.layout.fragment_profile, container, false)

        val currentUserKey = FirebaseAuth.getInstance().currentUser?.uid
        val reference = FirebaseDatabase.getInstance().reference
        val userReference = reference.child("user").child(currentUserKey.toString())

        val fullNameProfile = view.findViewById<TextView>(R.id.tvProfileName)
        val yearLevelProfile = view.findViewById<TextView>(R.id.tvYearLevelProfile)
        val courseProfile = view.findViewById<TextView>(R.id.tvCourseProfile)
        val biographyProfile = view.findViewById<TextView>(R.id.tvBiography)
        val status = view.findViewById<TextView>(R.id.tvStatus)
        val statusImage = view.findViewById<ImageView>(R.id.imgStatus)

        val onlineStatusProfile = view.findViewById<CardView>(R.id.cvStatus)
        val friendCountProfile = view.findViewById<TextView>(R.id.tvFriendsCount)
        val loading = view.findViewById<ProgressBar>(R.id.profileProgressBar)



        userReference.addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                val info = snapshot.getValue(User::class.java)
                val course = info?.course
                val yearLevel = info?.yearLevel
                val userName = info?.userName
                val friendCount = snapshot.child("Friends").childrenCount.toInt()
                val biography = info?.aboutMe
                val statusActivity = info?.activeStatus

                Log.e("MyTag", statusActivity.toString())
                if (statusActivity.toString() == "Offline"){
                    status.text = "Offline"
                    statusImage.setColorFilter(ContextCompat.getColor(view.context, R.color.dark_gray))
                }


                fullNameProfile.text =
                    userName.toString().lowercase(Locale.getDefault()).split(" ").joinToString(" ") { it.capitalize(
                        Locale.ROOT) }
                yearLevelProfile.text = yearLevel
                courseProfile.text = course

                if (friendCount < 2){
                    friendCountProfile.text = "$friendCount Friend"
                } else {
                    friendCountProfile.text = "$friendCount Friends"
                }

                biographyProfile.text = biography

                loading.visibility = GONE

            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })


        onlineStatusProfile.setOnClickListener {

            val dialog = CustomDialogStatusActivity(view.context) { action ->

                when(action) {
                    "Online" -> {
                        status.text = "Online"
                        statusImage.setColorFilter(ContextCompat.getColor(view.context, R.color.light_green))
                        userReference.child("activeStatus").removeValue()
                    }

                    "Offline" -> {
                        status.text = "Offline"
                        statusImage.setColorFilter(ContextCompat.getColor(view.context, R.color.dark_gray))
                        userReference.child("activeStatus").setValue("Offline")
                        reference.child("OnlineUserList").child(currentUserKey.toString()).removeValue()
                    }
                }

            }
            dialog.show()
        }

        return view
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment Profile.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            Profile().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}