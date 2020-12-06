package edu.utap.sharein

import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import androidx.lifecycle.MutableLiveData
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import edu.utap.sharein.glide.Glide
import edu.utap.sharein.model.*

class ViewModelDBHelper(postsList: MutableLiveData<List<Post>>) {
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()


    init {
        dbFetchPostsAll(postsList)

    }

    private fun ellipsizeString(string: String): String {
        // if the string has 10 or more chars, ellipsize it
        if (string.length < 10) {
            return string
        }
        return string.substring(0..9) + "..."
    }

    /*
     Interact with cloud firestore
     */
    fun dbFetchPostsAll(postsList: MutableLiveData<List<Post>>) {
        db.collection("allPosts")
                .orderBy("timeStamp", Query.Direction.DESCENDING) // descending
                .limit(100)
                .get()
                .addOnSuccessListener { result ->
                    Log.d(javaClass.simpleName, "allPosts fetch ${result.documents.size}")
                    postsList.value = result.documents.mapNotNull {
                        it.toObject(Post::class.java)
                    }
                }
                .addOnFailureListener {
                    Log.d(javaClass.simpleName, "allPosts fetch FAILED", it)
                }
    }

    fun dbFetchPostsFollow(followings: List<String>, postsList: MutableLiveData<List<Post>>) {

        var temp = mutableListOf<Post>()
        for (i in followings.indices) {
            db.collection("allPosts")
                .orderBy("timeStamp", Query.Direction.DESCENDING)
                .whereEqualTo("ownerUid", followings[i])
                .limit(100)
                .get()
                .addOnSuccessListener {result ->

                    temp.addAll(result.documents.mapNotNull {
                        it.toObject(Post::class.java)
                    })
                    if (i == followings.size - 1) {
                        postsList.value = temp
                    }
                }
                .addOnFailureListener {

                }
        }

    }

    fun dbFetchPostsUser(uid: String, postsList: MutableLiveData<List<Post>>) {
        db.collection("allPosts")
            .orderBy("timeStamp", Query.Direction.DESCENDING)
            .whereEqualTo("ownerUid", uid)
            .limit(100)
            .get()
            .addOnSuccessListener { result ->
                Log.d(javaClass.simpleName, "fetch curr user posts success")
                postsList.value = result.documents.mapNotNull {
                    it.toObject(Post::class.java)
                }

            }
            .addOnFailureListener {
                Log.d(javaClass.simpleName, "fetch curr posts FAILED")
            }
    }

    /*
     After successfully modify db, re-fetch the contents to update live data
     Thus dbFetchPosts is called here
     */
    fun updatePost(fetchStatus: Int, post: Post, postsList: MutableLiveData<List<Post>>) {
        val pictureUUIDs = post.pictureUUIDs
        db.collection("allPosts")
                .document(post.postID)
                .set(post)
                .addOnSuccessListener {
                    Log.d(javaClass.simpleName, "Post update success \"${ellipsizeString(post.text)}\" len ${pictureUUIDs.size} id ${post.postID}")
                    when(fetchStatus) {
                        Constants.FETCH_FOLLOW -> {

                        }
                        Constants.FETCH_ALL -> {
                            dbFetchPostsAll(postsList)
                        }
                        Constants.FETCH_TREND -> {
                            dbFetchTrend(postsList)
                        }
                        Constants.FETCH_CURR_USER_POSTS -> {
                            dbFetchPostsUser(post.ownerUid, postsList)
                        }
                        Constants.FETCH_LIKED -> {

                        }
                    }



                }
                .addOnFailureListener {
                    Log.d(javaClass.simpleName, "Post update FAILED \"${ellipsizeString(post.text)}\"")
                    Log.w(javaClass.simpleName, "Error ", it)
                }
    }

    /*
     After creating the post, need to re-fetch the contents
     */
    fun createPost(fetchStatus: Int, post: Post, postsList: MutableLiveData<List<Post>>) {
        // cloud firestore generates the postID
        post.postID = db.collection("allPosts").document().id
        db.collection("allPosts")

                .document(post.postID)
                .set(post)
                .addOnSuccessListener {
                    Log.d(javaClass.simpleName, "Post create success \"${ellipsizeString(post.text)}\" id ${post.postID}")
                    when(fetchStatus) {
                        Constants.FETCH_FOLLOW -> {

                        }
                        Constants.FETCH_ALL -> {
                            Log.d("create new", "fetch all")
                            dbFetchPostsAll(postsList)
                        }
                        Constants.FETCH_TREND -> {
                            dbFetchTrend(postsList)
                        }
                        Constants.FETCH_CURR_USER_POSTS -> {
                            dbFetchPostsUser(post.ownerUid, postsList)
                        }
                        Constants.FETCH_LIKED -> {

                        }
                    }
                }
                .addOnFailureListener {
                    Log.d(javaClass.simpleName, "Post create FAILED \"${ellipsizeString(post.text)}\"")
                    Log.w(javaClass.simpleName, "Error ", it)
                }
    }

    /*
     After deleting the post, need to re-fetch the contents
     */
    fun removePost(fetchStatus: Int, post: Post, postsList: MutableLiveData<List<Post>>) {
        db.collection("allPosts").document(post.postID).delete()
                .addOnSuccessListener {
                    Log.d(javaClass.simpleName, "Post delete success \"${ellipsizeString(post.text)}\" id ${post.postID}")
                    when(fetchStatus) {
                        Constants.FETCH_FOLLOW -> {

                        }
                        Constants.FETCH_ALL -> {
                            dbFetchPostsAll(postsList)
                        }
                        Constants.FETCH_TREND -> {
                            dbFetchTrend(postsList)
                        }
                        Constants.FETCH_CURR_USER_POSTS -> {
                            dbFetchPostsUser(post.ownerUid, postsList)
                        }
                        Constants.FETCH_LIKED -> {

                        }
                    }
                }
                .addOnFailureListener {
                    Log.d(javaClass.simpleName, "Post delete FAILED \"${ellipsizeString(post.text)}\" id ${post.postID}")
                    Log.w(javaClass.simpleName, "Error ", it)
                }
    }

    ////////////////////////////////////////////////////////////////////////////////////////
    /*
     Interact with firestore cloud to deal with user
     */

    fun dbFetchUser(currUser: MutableLiveData<User>, currUserId: MutableLiveData<String>, uid: String) {
        db.collection("allUsers")
                .document(uid)
                .get()
                .addOnSuccessListener { result ->
                    currUser.value = result.toObject(User::class.java)
                    currUserId.value = uid
                    Log.d(javaClass.simpleName, "user is ${currUser.value?.uid}")
                    Log.d(javaClass.simpleName, "user fetch success")

                }
                .addOnFailureListener {
                    Log.d(javaClass.simpleName, "user fetch FAILED", it)
                    currUser.value = null
                    currUserId.value = uid
                }

    }

    fun dbFetchUserName(uid: String, view: TextView) {
        db.collection("allUsers")
            .document(uid)
            .get()
            .addOnSuccessListener { result ->
                view.text = result.toObject(User::class.java)!!.name

            }
            .addOnFailureListener {
                view.text = "Unknown"
            }

    }

    fun dbFetchOwner(imageView: ImageView, uid: String, storage: Storage){
        // fetch post owner and bind profile photo too image view
        Log.d(javaClass.simpleName, "trying to fetch $uid")
        db.collection("allUsers")
            .document(uid)
            .get()
            .addOnSuccessListener { result ->
                val u: User? = result.toObject(User::class.java)

                if (u != null && u.profilePhotoUUID != "") {
                    Glide.fetch(storage.uuid2StorageReference(u.profilePhotoUUID), imageView)
                    Log.d(javaClass.simpleName, "user fetch profile photo ${u.name}")
                } else {
                    Log.d(javaClass.simpleName, "user fetch profile photo failed")
                    imageView.setImageResource(R.drawable.profile)
                }


            }
            .addOnFailureListener {
                Log.d(javaClass.simpleName, "user fetch profile photo failed e")
            }
    }

    fun createUser(user: User) {
        db.collection("allUsers")
                .document(user.uid)
                .set(user)
                .addOnSuccessListener {
                    Log.d(javaClass.simpleName, "User create success id ${user.uid} name ${user.name}")

                }
                .addOnFailureListener {
                    Log.d(javaClass.simpleName, "User create FAILED id ${user.uid} name ${user.name}")
                    Log.d(javaClass.simpleName, "Error ", it)
                }

    }

    fun updateUser(user: User) {
        db.collection("allUsers")
                .document(user.uid)
                .set(user)
                .addOnSuccessListener {
                    Log.d(javaClass.simpleName, "User update suceess id ${user.uid} name ${user.name}")

                }
                .addOnFailureListener {
                    Log.d(javaClass.simpleName, "User update FAILED id ${user.uid} name ${user.name}")
                    Log.d(javaClass.simpleName, "Error ", it)
                }
    }

    /*
     Deal with follow
     */
    fun dbFetchAllFollow(followList: MutableLiveData<List<Follow>>) {
        db.collection("follow")
            .get()
            .addOnSuccessListener { result ->
                followList.value = result.documents.mapNotNull {
                    it.toObject(Follow::class.java)
                }
            }
            .addOnFailureListener {

            }
    }
    fun dbFetchFollowing(followerUID: String, followingList: MutableLiveData<List<Follow>>) {
        // given a user uid, fetch all users he/she is following
        db.collection("follow")
            .whereEqualTo("follower", followerUID)
            .get()
            .addOnSuccessListener {result ->
                followingList.value = result.documents.mapNotNull {
                    it.toObject(Follow::class.java)
                }
                Log.d(javaClass.simpleName, "fetch following success size is ${followingList.value!!.size}")
            }
            .addOnFailureListener {
                Log.d(javaClass.simpleName, "fetch following FAILED")
            }

    }

    fun dbFetchFollower(followingUID: String, followersList: MutableLiveData<List<Follow>>) {
        // given a user uid, fetch all his/her followers
        db.collection("follow")
            .whereEqualTo("following", followingUID)
            .get()
            .addOnSuccessListener { result ->
                followersList.value = result.documents.mapNotNull {
                    it.toObject(Follow::class.java)
                }
            }
            .addOnFailureListener {
                Log.d(javaClass.simpleName, "fetch follower FAILED")
            }
    }

    fun dbFollow(follow: Follow) {
        follow.followID = db.collection("follow").document().id
        db.collection("follow")
            .document(follow.followID)
            .set(follow)
            .addOnSuccessListener {
                Log.d(javaClass.simpleName, "follow success")

            }
            .addOnFailureListener {
                Log.d(javaClass.simpleName, "follow failed")
            }
    }

    fun dbUnfollow(follow: Follow) {
        db.collection("follow")
            .document(follow.followID)
            .delete()
            .addOnSuccessListener {
                Log.d(javaClass.simpleName, "unfollow success")

            }
            .addOnFailureListener {
                Log.d(javaClass.simpleName, "unfollow failed")
            }
    }

    /*
     Deal with Like
     */

    fun dbFetchTrend(postsList: MutableLiveData<List<Post>>) {
        db.collection("allPosts")
            .orderBy("timeStamp", Query.Direction.DESCENDING) // descending
            .limit(100)
            .get()
            .addOnSuccessListener { result ->
                postsList.value = result.documents.mapNotNull {
                    it.toObject(Post::class.java)
                }
                var postIDList = mutableListOf<String>()
                if (postsList.value!!.size != 0) {
                    for (post in postsList.value!!) {
                        postIDList.add(post.postID)
                    }
                    var likesCountList = mutableListOf<Int>()
                    var tempLists = postIDList.chunked(10)
                    var entryCount = 0
                    db.collection("like")
                        .get()
                        .addOnSuccessListener { like ->
                            entryCount = like.documents.size
                            var likes = mutableListOf<Like>()
                            for (list in tempLists) {
                                db.collection("like")
                                    .whereIn("postID", list)
                                    .get()
                                    .addOnSuccessListener {result ->
                                        var temp = MutableLiveData<List<Like>>()
                                        temp.value = result.documents.mapNotNull {
                                            it.toObject(Like::class.java)
                                        }
                                        likes.addAll(temp.value!!)


                                        if (likes!!.size != 0 && likes!!.size == entryCount) {
                                            for (pid in postIDList) {
                                                var count = 0
                                                for (l in likes!!) {
                                                    if (l.postID == pid) {
                                                        count++
                                                    }
                                                }
                                                likesCountList.add(count)
                                            }

                                            var pairList = mutableListOf<Pair<Post, Int>>()
                                            for (i in 0 until postIDList.size) {
                                                pairList.add(Pair(postsList.value!![i], likesCountList[i]))
                                            }
                                            pairList.sortBy {
                                                it.second
                                            }
                                            pairList.reverse()
                                            var sortedPostList = mutableListOf<Post>()
                                            for (pair in pairList) {
                                                sortedPostList.add(pair.first)
                                            }
                                            postsList.value = sortedPostList
                                        }

                                    }
                                    .addOnFailureListener {

                                    }
                            }
                        }



                }



            }
            .addOnFailureListener {

            }
    }

    fun dbFetchOnePostLikes(postID: String, likes: MutableLiveData<List<Like>>, count: TextView) {
        db.collection("like")
            .whereEqualTo("postID", postID)
            .get()
            .addOnSuccessListener { result ->
                Log.d(javaClass.simpleName, "fetch one post likes success size is ${result.documents.size}")
                likes.value = result.documents.mapNotNull {
                    it.toObject(Like::class.java)
                }
                count.text = likes.value!!.size.toString()
            }
            .addOnFailureListener {
                Log.d(javaClass.simpleName, "fetch one post like FAILED")
            }
    }

    fun dbFetchUserLikedPostsLikes(userUID: String, likes: MutableLiveData<List<Like>>, count: MutableLiveData<Int>) {
        db.collection("like")
            .whereEqualTo("userUID", userUID)
            .get()
            .addOnSuccessListener { result ->
                Log.d(javaClass.simpleName, "fetch user liked post success size is ${result.documents.size}")
                likes.value = result.documents.mapNotNull {
                    it.toObject(Like::class.java)
                }
                count.value = likes.value!!.size

            }
            .addOnFailureListener {
                Log.d(javaClass.simpleName, "fetch user liked post FAILED")
            }
    }
    fun dbFetchUserLikedPosts(userUID: String, likes: MutableLiveData<List<Like>>, postsList: MutableLiveData<List<Post>>) {

        db.collection("like")
            .whereEqualTo("userUID", userUID)
            .get()
            .addOnSuccessListener { result ->
                Log.d(javaClass.simpleName, "fetch user liked post success size is ${result.documents.size}")
                likes.value = result.documents.mapNotNull {
                    it.toObject(Like::class.java)
                }
                if (likes.value!!.size != 0) {
                    var list = mutableListOf<String>()
                    for (l in likes.value!!) {
                        list.add(l.postID)
                    }
                    var temp = mutableListOf<Post>()
                    for (i in list.indices) {
                        db.collection("allPosts")
                            .whereEqualTo("postID", list[i])
                            .get()
                            .addOnSuccessListener { result ->
                               temp.addAll(result.documents.mapNotNull {
                                   it.toObject(Post::class.java)
                               })
                                if (i == list.size - 1) {
                                    postsList.value = temp
                                }
                            }
                            .addOnFailureListener {

                            }
                    }

                }


            }
            .addOnFailureListener {
                Log.d(javaClass.simpleName, "fetch user liked post FAILED")
            }

    }

    fun dbFetchUserLikedPostsAndBind (userUID: String, likes: MutableLiveData<List<Like>>, post: Post, iv: ImageView) {
        db.collection("like")
            .whereEqualTo("userUID", userUID)
            .get()
            .addOnSuccessListener { result ->
                Log.d(javaClass.simpleName, "fetch user liked post success size is ${result.documents.size}")
                likes.value = result.documents.mapNotNull {
                    it.toObject(Like::class.java)
                }
                if (likes.value!!.isNotEmpty()) {
                    for (l in likes.value!!) {
                        if (l.postID == post.postID) {
                            iv.setImageResource(R.drawable.ic_baseline_favorite_24)
                            return@addOnSuccessListener
                        }

                    }
                }
                iv.setImageResource(R.drawable.ic_baseline_favorite_border_24)
            }
            .addOnFailureListener {
                Log.d(javaClass.simpleName, "fetch user liked post FAILED")
            }
    }

    fun dbLike(like: Like) {
        like.likeID = db.collection("like").document().id
        db.collection("like")
            .document(like.likeID)
            .set(like)
            .addOnSuccessListener {
                Log.d(javaClass.simpleName, "like success")
            }
            .addOnFailureListener {
                Log.d(javaClass.simpleName, "like FAILED")
            }
    }

    fun dbUnlike(like: Like) {
        db.collection("like")
            .document(like.likeID)
            .delete()
            .addOnSuccessListener {
                Log.d(javaClass.simpleName, "unlike success")
            }
            .addOnFailureListener {
                Log.d(javaClass.simpleName, "unlike FAILED")
            }
    }

    /*
     handle comment
     */
    fun dbFetchOnePostComments(postID: String, commentsList: MutableLiveData<List<Comment>>) {
        db.collection("comment")
            .whereEqualTo("postID", postID)
            .orderBy("timeStamp")
            .get()
            .addOnSuccessListener { result ->
                Log.d(javaClass.simpleName, "db fectch one post comments success size is ${result.documents.size}")
                commentsList.value = result.documents.mapNotNull {
                    it.toObject(Comment::class.java)
                }
            }
            .addOnFailureListener {
                Log.d(javaClass.simpleName, "db fetch one post comments failed")
            }
    }

    fun dbCreateComment(comment: Comment, commentsList: MutableLiveData<List<Comment>>) {
        comment.commentID = db.collection("comment").document().id
        db.collection("comment")
            .document(comment.commentID)
            .set(comment)
            .addOnSuccessListener {
                Log.d(javaClass.simpleName, "db create comment success")
                dbFetchOnePostComments(comment.postID, commentsList)
            }
            .addOnFailureListener {
                Log.d(javaClass.simpleName, "db create comment failed")
            }
    }

    fun dbDeleteComment(comment: Comment, commentsList: MutableLiveData<List<Comment>>) {
        db.collection("comment")
            .document(comment.commentID)
            .delete()
            .addOnSuccessListener {
                Log.d(javaClass.simpleName, "db delete comment success")
                dbFetchOnePostComments(comment.postID, commentsList)
            }
            .addOnFailureListener {
                Log.d(javaClass.simpleName, "db delete comment failed")
            }

    }

    /*
     Deal with messages
     */

    fun dbFetchMessageBySenderReceiver(senderUID: String, receiverUID: String, messagesList: MutableLiveData<List<Message>>, allMessagesList: MutableLiveData<List<Message>>) {
        db.collection("message")
            .whereEqualTo("senderUID", senderUID)
            .whereEqualTo("receiverUID", receiverUID)
            .orderBy("timeStamp")
            .get()
            .addOnSuccessListener { result ->
                Log.d(javaClass.simpleName, "db fetch messages success")
                messagesList.value = result.documents.mapNotNull {
                    it.toObject(Message::class.java)
                }
                var temp = mutableListOf<Message>()
                if (allMessagesList.value != null) {
                    temp.addAll(allMessagesList.value!!)
                }

                if (messagesList.value != null) {
                    for (m in messagesList.value!!) {
                        if (!temp.contains(m)) {
                            temp.add(m)
                        }
                    }
                }/*
                temp.sortedBy {
                    it.timeStamp
                }*/
                allMessagesList.value = temp
                Log.d(javaClass.simpleName, "allMessagesList size is ${allMessagesList.value?.size}")

            }
            .addOnFailureListener {
                Log.d(javaClass.simpleName, "db fetch messages failed", it)
            }
    }

    fun dbFetchLastMessageReceived(receiverUID: String, messageReceived: MutableLiveData<List<Message>>, lastMessageReceivedList: MutableLiveData<List<Message>>) {
        db.collection("message")

            .whereEqualTo("receiverUID", receiverUID)
            .orderBy("timeStamp", Query.Direction.DESCENDING)

            .get()
            .addOnSuccessListener {result ->
                Log.d(javaClass.simpleName, "db fetch last message success")
                messageReceived.value = result.documents.mapNotNull {
                    it.toObject(Message::class.java)
                }
                var temp = mutableListOf<Message>()
                if (messageReceived.value != null) {
                    for (m in messageReceived.value!!) {
                        var add = true
                        for (mm in temp) {
                            if (mm.senderName == m.senderName) {
                                add = false
                            }
                        }
                        if (add) {
                            temp.add(m)
                        }
                    }
                }
                lastMessageReceivedList.value = temp
                Log.d(javaClass.simpleName, "lastMessageReceivedList size is ${lastMessageReceivedList.value?.size}")


            }
            .addOnFailureListener {
                Log.d(javaClass.simpleName, "fetch last message failed")
            }
    }

    fun dbCreateMessage(message: Message, messagesList: MutableLiveData<List<Message>>, allMessagesList: MutableLiveData<List<Message>>) {
        message.messageID = db.collection("message").document().id
        db.collection("message")
            .document(message.messageID)
            .set(message)
            .addOnSuccessListener {
                Log.d(javaClass.simpleName, "db create message success")
                dbFetchMessageBySenderReceiver(message.senderUID, message.receiverUID, messagesList, allMessagesList)
            }
            .addOnFailureListener {
                Log.d(javaClass.simpleName, "db create message failed")
            }
    }







}
