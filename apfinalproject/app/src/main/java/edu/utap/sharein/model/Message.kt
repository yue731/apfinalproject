package edu.utap.sharein.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.ServerTimestamp

data class Message (
    var messageID: String = "",
    var senderUID: String = "",
    var senderName: String = "",
    var receiverUID: String = "",
    var receiverName: String = "",
    var messageText: String = "",
    @ServerTimestamp val timeStamp: Timestamp? = null
)