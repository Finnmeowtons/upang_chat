package com.example.upangchattingapp

import android.R
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import android.widget.ListView
import com.example.upangchattingapp.databinding.ActivitySearchAddAfriendBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.Locale


class SearchAddAFriend : AppCompatActivity() {

    private lateinit var binding : ActivitySearchAddAfriendBinding
    private lateinit var searchQuery: String
    private var loadedAllItems: Boolean = false
    private var loadingInProgress: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchAddAfriendBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        binding.apply {






            etSearchBar.setOnEditorActionListener { _, keyCode, _ ->
                if (keyCode == EditorInfo.IME_ACTION_DONE || keyCode == EditorInfo.IME_NULL || keyCode == EditorInfo.IME_ACTION_SEARCH) {

                    etSearchBar.clearFocus()
                    progressBar.visibility = VISIBLE
                    val searchQuery = etSearchBar.text.toString().lowercase(Locale.getDefault()).split(" ").joinToString(" ") { it.capitalize(
                        Locale.ROOT) }
                    val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    inputMethodManager.hideSoftInputFromWindow(etSearchBar.windowToken, 0)
                    performSearch(searchQuery)
                    true
                } else {
                    false
                }
            }

            btnSearch.setOnClickListener {
                progressBar.visibility = VISIBLE
                searchQuery = etSearchBar.text.toString().lowercase(Locale.getDefault()).split(" ").joinToString(" ") { it.capitalize(
                    Locale.ROOT) }
                performSearch(searchQuery)
            }

            cvSearchBackButton.setOnClickListener {
                val intent = Intent(this@SearchAddAFriend, MainActivity::class.java)
                intent.putExtra("fragment", 2)
                startActivity(intent)
            }
        }
    }

    private fun performSearch(query: String) {
        val currentUserKey = FirebaseAuth.getInstance().currentUser?.uid
        val userRef = FirebaseDatabase.getInstance().reference.child("user")
        val searchQuery = userRef.orderByChild("userName").startAt(query).endAt(query + "\uf8ff")

        searchQuery.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val searchResults = mutableListOf<User>()
                for (snapshot in dataSnapshot.children) {
                    val user = snapshot.getValue(User::class.java)
                    if (user != null && user.userID != currentUserKey) {
                        if (user.userName != "Deleted Account") {
                            Log.e("MyTag", user.userName)
                            searchResults.add(user)
                        }
                    }
                }
                displaySearchResults(searchResults)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("MyTag", "Search cancelled: $error")
            }
        })
    }


    private fun displaySearchResults(results: List<User>) {
        val adapter = ListViewSearchListAdapter(results)
        binding.listViewSearchList.adapter = adapter
        binding.progressBar.visibility = GONE
    }


    override fun onBackPressed() {
        val intent = Intent(this, MainActivity::class.java)
        intent.putExtra("fragment", 2)
        startActivity(intent)
    }
}
