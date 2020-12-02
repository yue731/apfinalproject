package edu.utap.sharein.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.ServerTimestamp

data class Comment(
    var commentID: String = "",
    var userUID: String = "",
    var userName: String = "",
    var postID: String = "",
    var text: String = "",
    @ServerTimestamp val timeStamp: Timestamp? = null
)