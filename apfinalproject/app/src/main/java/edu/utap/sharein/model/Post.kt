package edu.utap.sharein.model

import android.graphics.Color
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import com.google.firebase.Timestamp
import com.google.firebase.firestore.ServerTimestamp

data class Post(
    // Auth info
    var name: String = "",
    var ownerUid: String = "",
    // Text
    var title: String = "",
    var text: String = "",
    var pictureUUIDs: List<String> = listOf(),
    // Written on the server
    @ServerTimestamp val timeStamp: Timestamp? = null,
    // postID is generated by firestore, used as primary key
    var postID: String = "",
    var musicRawID: Int = -1,
    var address: String = ""


    )