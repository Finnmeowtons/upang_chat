package com.example.upangchattingapp.messaging

data class ChatList(
    var message: String = "",
    var timeStamp: String = "",
    val userName: String = "",
    val userID: String = "",
    val conversationID : String = ""
)
