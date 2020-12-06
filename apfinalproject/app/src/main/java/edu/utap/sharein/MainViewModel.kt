package edu.utap.sharein

import android.app.Application
import android.graphics.Bitmap
import android.net.Uri
import android.os.Environment
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.lifecycle.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import edu.utap.sharein.glide.Glide
import edu.utap.sharein.model.*
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.*

class MainViewModel(application: Application, private val state: SavedStateHandle) : AndroidViewModel(application) {
    companion object {

    }

    private val appContext = getApplication<Application>().applicationContext
    private val storageDir = getApplication<Application>().getExternalFilesDir(Environment.DIRECTORY_PICTURES)

    private var firebaseAuthLiveData = FirebaseAuthLiveData()

    private var postsList = MutableLiveData<List<Post>>()
    private val dbHelp = ViewModelDBHelper(postsList)
    private var fetchStatus = MutableLiveData<Int>()

    private var followingList = MutableLiveData<List<Follow>>()
    private var followerList = MutableLiveData<List<Follow>>()

    private var commentsList = MutableLiveData<List<Comment>>()



    private var currUserId = MutableLiveData<String>()
    private var currUser = MutableLiveData<User>()

    private var currPageUser = MutableLiveData<User>()
    // create a stack to store users
    private var userStack = MutableLiveData<Stack<User>>().apply {
        value = Stack()
    }



    private lateinit var storage: Storage


    private lateinit var crashMe: String
    private var pictureUUID: String = ""
    private var profilePhotoUUID: String = ""
    private var allImages = MutableLiveData<List<String>>()

    private var onePostLikes = MutableLiveData<List<Like>>()
    private var allPostsLikes = MutableLiveData<List<Int>>()
    private var postLikes = MutableLiveData<Int>()
    private var userLikedPosts = MutableLiveData<List<Like>>()
    private var userLikedPostsCount = MutableLiveData<Int>()

    private var messagesSentByMeList = MutableLiveData<List<Message>>()
    private var messagesReceivedByMeList = MutableLiveData<List<Message>>()
    private var allMessagesList = MutableLiveData<List<Message>>()
    private var lastMessageReceivedList = MutableLiveData<List<Message>>()
    private var messagesReceivedListPreview = MutableLiveData<List<Message>>()


    ////////////////////////////////////////////////////////////////////////
    /*
     Deal with liked posts
     */

    fun observeOnePostLikes(): LiveData<List<Like>> {
        return onePostLikes
    }
    fun observePostsLikes(): LiveData<Int> {
        return postLikes
    }
    fun observeUserLikedPosts(): LiveData<List<Like>> {
        return userLikedPosts
    }
    fun observeUserLikedPostsCount(): LiveData<Int> {
        return userLikedPostsCount
    }
    fun resetOnePostLikes() {
        onePostLikes.value = listOf()
    }
    fun resetPostsLikes() {
        postLikes.value = 0
    }
    fun resetUserLikedPosts() {
        userLikedPosts.value = listOf()
    }
    fun resetUserLikedPostsCount() {
        userLikedPostsCount.value = 0
    }
    fun fetchOnePostLikes(postID: String, count: TextView) {
        dbHelp.dbFetchOnePostLikes(postID, onePostLikes, count)
    }
    fun fetchUserLikedPostsLikes(userUID: String) {
        dbHelp.dbFetchUserLikedPostsLikes(userUID, userLikedPosts, userLikedPostsCount)
    }

    fun fetchUserLikedPostsAndBind(userUID: String, post: Post, iv: ImageView) {
        dbHelp.dbFetchUserLikedPostsAndBind(userUID, userLikedPosts, post, iv)
    }
    fun fetchUserLikedPosts(userUID: String) {
        dbHelp.dbFetchUserLikedPosts(userUID, userLikedPosts, postsList)
    }



    fun pushUser() {
        userStack.value!!.push(currPageUser.value!!)
    }

    fun popUser() {
        currPageUser.value = userStack.value!!.pop()
    }

    fun setCurrPageUser(user: User) {
        currPageUser.value = user
    }

    fun getCurrPageUser(): User {
        return currPageUser.value!!
    }

    fun like(postID: String) {
        val like = Like (
            userUID = currUser.value!!.uid,
            postID = postID
        )
        dbHelp.dbLike(like)
    }
    fun unlike(postID: String) {
        var like: Like? = null
        for (l in userLikedPosts.value!!) {
            if (l.postID == postID && l.userUID == currUser.value!!.uid) {
                like = l
            }
        }
        if (like != null) {
            dbHelp.dbUnlike(like)
        }
    }
    fun isLiked(postID: String): Boolean {
        if (userLikedPosts == null || userLikedPosts.value == null) return false
        for (l in userLikedPosts.value!!) {
            if (l.postID == postID) return true
        }
        return false
    }





    ////////////////////////////////////////////////////////////////////////



    ////////////////////////////////////////////////////////////////////////
    /*
     Camera stuff
     */
    private fun noPhoto() {
        Log.d(javaClass.simpleName, "Function must be initialized to something that can start the camera intent")
        crashMe.plus(" ")
    }

    private var takePhotoIntent: () -> Unit = ::noPhoto

    private fun defaultPhoto(@Suppress("UNUSED_PARAMETER") path: String) {
        Log.d(javaClass.simpleName, "Function must be initialized to photo callback")
        crashMe.plus(" ")
    }

    private var photoSuccess: (path: String) -> Unit = ::defaultPhoto

    fun setPhotoIntent(_takePhotoIntent: () -> Unit) {
        takePhotoIntent = _takePhotoIntent
    }

    fun takePhoto(_photoSuccess: (String) -> Unit) {
        photoSuccess = _photoSuccess
        takePhotoIntent()
    }

    // create a file for the photo, remember it, and create a uri
    fun getPhotoURI(): Uri {
        pictureUUID = UUID.randomUUID().toString()
        var photoUri: Uri? = null
        try {
            val localPhotoFile = File(storageDir, "${pictureUUID}.jpg")
            Log.d(javaClass.simpleName, "photo path ${localPhotoFile.absolutePath}")
            photoUri = FileProvider.getUriForFile(
                    appContext,
                    "edu.utap.sharein",
                    localPhotoFile
            )
        } catch (e: IOException) {
            Log.d(javaClass.simpleName, "Cannot create file", e)
        }
        return photoUri!!
    }

    fun photoSuccess() {
        val localPhotoFile = File(storageDir, "${pictureUUID}.jpg")
        storage.uploadImage(localPhotoFile, pictureUUID) {
            photoSuccess(pictureUUID)
            photoSuccess = ::defaultPhoto
            pictureUUID = ""
        }
    }

    fun photoFailure() {
        pictureUUID = ""
    }

    ////////////////////////////////////////////////////////////////////////


    ////////////////////////////////////////////////////////////////////////
    /*
     Firebase authentication section
     */
    fun observeFirebaseAuthLiveData(): LiveData<FirebaseUser?> {
        return firebaseAuthLiveData
    }


    fun myUid(): String? {
        return firebaseAuthLiveData.value?.uid
    }

    fun signOut() {
        FirebaseAuth.getInstance().signOut()
    }

    fun getUserName(): String? {
        return firebaseAuthLiveData.value?.displayName
    }

    fun getEmail(): String? {
        return firebaseAuthLiveData.value?.email
    }
    ////////////////////////////////////////////////////////////////////////


    ////////////////////////////////////////////////////////////////////////
    /*
     Post and db interaction
     */
    fun observePosts(): LiveData<List<Post>> {
        return postsList
    }

    fun isPostsEmpty(): Boolean {
        return postsList.value.isNullOrEmpty()
    }

    fun fetchPosts(status: Int, uid: String?) {
        when(status) {
            Constants.FETCH_FOLLOW -> {
                if (followingList.value != null) {
                    var list = mutableListOf<String>()
                    for (f in followingList.value!!) {
                        list.add(f.following)
                    }
                    Log.d(javaClass.simpleName, "list size is ${list.size}")
                    dbHelp.dbFetchPostsFollow(list, postsList)
                }

            }
            Constants.FETCH_ALL -> {
                dbHelp.dbFetchPostsAll(postsList)
            }
            Constants.FETCH_TREND -> {
                dbHelp.dbFetchTrend(postsList)


            }
            Constants.FETCH_CURR_USER_POSTS -> {
                if (currUser.value != null) {
                    dbHelp.dbFetchPostsUser(currUser.value!!.uid, postsList)

                }

            }
            Constants.FETCH_LIKED -> {
                fetchUserLikedPosts(currUser.value!!.uid)
            }
            Constants.FETCH_OTHER_USER -> {
                dbHelp.dbFetchPostsUser(uid!!, postsList)
            }
        }

    }

    fun getPost(position: Int): Post {
        val post = postsList.value?.get(position)
        return post!!
    }

    fun resetPosts() {
        postsList.value = listOf()
    }
    fun updatePostsList(list: List<Post>) {
        postsList.value = list
    }

    // after we successfully modify the post, we need to re-fetch the content to update livedata
    fun updatePost(position: Int, title: String, text: String, pictureUUIDs: List<String>, musicRawID: Int, address: String) {
        val post = getPost(position)
        post.title = title
        post.text = text
        post.pictureUUIDs = pictureUUIDs
        post.musicRawID = musicRawID
        post.address = address
        dbHelp.updatePost(fetchStatus.value!!, post, postsList)

    }

    fun createPost(title: String, text: String, pictureUUIDs: List<String>, musicRawID: Int, address: String): String {
        // create post and return postID
        val post = Post(
                name = currUser.value?.name!!,
                ownerUid = myUid()!!,
                title = title,
                text = text,
                pictureUUIDs = pictureUUIDs,
                musicRawID = musicRawID,
                address = address

        )
        dbHelp.createPost(fetchStatus.value!!, post, postsList)
//        var longerList = postsList.value?.toMutableList()
//        longerList?.add(post)
//        postsList.value = longerList
        return post.postID
    }

    fun removePostAt(position: Int) {
        val post = getPost(position)
        // delete each image before deleting the post itself
        post.pictureUUIDs.forEach {
            storage.deleteImage(it)
        }
        dbHelp.removePost(fetchStatus.value!!, post, postsList)

    }


    fun initFetchStatus() {
        fetchStatus.value = Constants.FETCH_ALL
    }
    fun observeFetchStatus(): LiveData<Int> {
        return fetchStatus
    }

    fun updateFetchStatus(status: Int) {
        fetchStatus.value = status
    }
    ////////////////////////////////////////////////////////////////////////


    ////////////////////////////////////////////////////////////////////////
    /*
     User and db interaction
     */

    fun observeUser(): LiveData<User> {
        return currUser
    }

    fun observeUserId(): LiveData<String> {
        return currUserId
    }





    fun fetchUser(uid: String) {

        dbHelp.dbFetchUser(currUser, currUserId,  uid)

        Log.d(javaClass.simpleName, "currUser value is ${currUser.value}")
    }

    fun fetchOtherUser(uid: String, user: MutableLiveData<User>) {
        var otherUserUID = MutableLiveData<String>()
        dbHelp.dbFetchUser(user, otherUserUID, uid)
    }

    fun fetchUserName(uid: String, view: TextView) {
        dbHelp.dbFetchUserName(uid, view)
    }

    fun fetchOwner(imageView: ImageView, uid: String) {
        // fetch post owner and bind profile photo to image view
        dbHelp.dbFetchOwner(imageView, uid, storage)
    }


    fun createUser() {
        Log.d(javaClass.simpleName, "createUser is called")



        val user = User(
                name = getEmail()?.substringBefore("@") ?: "",
                email = getEmail() ?: "",
                uid = FirebaseAuth.getInstance().currentUser?.uid!!

                )
        Log.d(javaClass.simpleName, "user uid ${user.uid}")
        currUser.value = user
        Log.d(javaClass.simpleName, "after create user currUser is ${currUser.value}")
        dbHelp.createUser(user)


    }

    // Need to update user when:
    // 1. user updates its user name (one-time)
    // 2. user creates a new post
    // 3. user likes a post
    // 4. user follow/unfollow other user
    // 5. user updates profile photo
    fun updateUser(user: User) {
        Log.d(javaClass.simpleName, "updateUser is called")
        currUser.value = user
        dbHelp.updateUser(user)

    }

    fun resetUser() {
        currUser.value = null
        currUserId.value = null
        val list: List<Post> = listOf()
        postsList.value = list
        profilePhotoUUID = ""
        currPageUser.value = null
    }


    ////////////////////////////////////////////////////////////////////////


    ////////////////////////////////////////////////////////////////////////
    /*
     Deal with follow
     */

    fun observeFollowing(): LiveData<List<Follow>> {
        return followingList
    }
    fun observeFollower(): LiveData<List<Follow>> {
        return followerList
    }

    fun resetFollowingList() {
        followingList.value = listOf()
    }

    fun resetFollowerList() {
        followerList.value = listOf()
    }

    fun fetchAllFollow(followList: MutableLiveData<List<Follow>>) {
        dbHelp.dbFetchAllFollow(followList)
    }

    fun fetchFollowing(followerUID: String) {
        dbHelp.dbFetchFollowing(followerUID, followingList)
        Log.d(javaClass.simpleName, "fetch following size is ${followingList.value?.size}")
    }
    fun fetchFollower(followingUID: String) {
        dbHelp.dbFetchFollower(followingUID, followerList)
    }
    fun follow(followerUID: String, followingUID: String) {
        val follow = Follow (
            follower = followerUID,
            following = followingUID
        )
        dbHelp.dbFollow(follow)
    }
    fun unfollow(user: User, followerUID: String, followingUID: String) {


        var follow: Follow? = null
        if (user.uid == currUser.value!!.uid) {
            for (f in followingList.value!!) {

                if (f.follower == followerUID && f.following == followingUID) {
                    follow = f
                }
            }
        }
        else {
            for (f in followerList.value!!) {

                if (f.follower == followerUID && f.following == followingUID) {
                    follow = f
                }
            }
        }


        if (follow != null) {
            dbHelp.dbUnfollow(follow)
        }
    }
    fun isFollowing(followingUID: String): Boolean {
        // return true if follower is following following


        Log.d(javaClass.simpleName, "list is ${followingList.value}")
        if (followingList == null || followingList.value == null) return false
        for (f in followingList.value!!) {
            if (f.following == followingUID) return true
        }
        return false
    }

    fun isFollower(followerUID: String): Boolean {
        if (followerList == null || followerList.value == null) return false
        for (f in followerList.value!!) {
            if (f.follower == followerUID) return true
        }
        return false
    }




    ////////////////////////////////////////////////////////////////////////
    /*
     Deal with images XXX might not be required
     */
    private fun imageListReturns(pictureUUIDs: List<String>) {
        allImages.value = pictureUUIDs
    }

    fun refreshAllImages() {
        storage.listAllImages(::imageListReturns)
    }

    fun observeAllImages(): LiveData<List<String>> {
        return allImages
    }

    fun deleteImage(pictureUUID: String) {
        storage.deleteImage(pictureUUID)
    }

    /*
     we have the bitmap of the profile photo
     we need to:
     1. save bitmap to a file
     2. generate a uuid
     3. upload to storage
     4. also need to update user for the change in profilephoto uuid
     */
    fun uploadProfilePhoto(bitmap: Bitmap) {
        // if previously user has uploaded a profile photo, need to delete
        if (currUser.value != null && currUser.value!!.profilePhotoUUID != "") {
            storage.deleteImage(currUser.value!!.profilePhotoUUID)

        }

        profilePhotoUUID = UUID.randomUUID().toString()
        val localPhotoFile = File(storageDir, "${profilePhotoUUID}.jpg")
        try {
            val out = FileOutputStream(localPhotoFile)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)
            out.flush()
            out.close()
        }
        catch (e: IOException) {
            Log.d(javaClass.simpleName, "Cannot create a file from the bitmap given")
        }

        storage.uploadImage(localPhotoFile, profilePhotoUUID) {
            Toast.makeText(appContext, "Profile Photo Upload Success!", Toast.LENGTH_LONG).show()
        }
        val user = currUser.value
        if (user != null) {
            user?.profilePhotoUUID = profilePhotoUUID
            updateUser(user)

        }
        else {
            Log.d(javaClass.simpleName, "user is null when uploading profile photo")
        }


    }
    ////////////////////////////////////////////////////////////////////////

    /*
     Handle comment
     */

    fun observeOnePostComments(): LiveData<List<Comment>> {
        return commentsList
    }
    fun resetOnePostComments() {
        commentsList.value = listOf()
    }
    fun fetchOnePostComments(postID: String) {
        dbHelp.dbFetchOnePostComments(postID, commentsList)
    }
    fun getComment(position: Int): Comment {
        val comment = commentsList.value?.get(position)
        return comment!!
    }
    fun createComment(postID: String, text: String) {
        val comment = Comment (
            userUID = currUser.value!!.uid,
            userName = currUser.value!!.name,
            postID = postID,
            text = text

        )
        dbHelp.dbCreateComment(comment, commentsList)
    }

    fun deleteCommentAt(position: Int) {
        val comment = getComment(position)
        dbHelp.dbDeleteComment(comment, commentsList)
    }

    /*
     Deal with messages
     */

    fun observeSentMessages(): LiveData<List<Message>> {
        return messagesSentByMeList
    }
    fun observeReceivedMessages(): LiveData<List<Message>> {
        return messagesReceivedByMeList
    }

    fun observeAllMessages(): LiveData<List<Message>> {
        return allMessagesList
    }

    fun observeLastMessagesReceivedList(): LiveData<List<Message>> {
        return lastMessageReceivedList
    }



    fun resetSentMessages() {
        messagesSentByMeList.value = listOf()
    }
    fun resetReceivedMessages() {
        messagesReceivedByMeList.value = listOf()
    }
    fun resetAllMessages() {
        allMessagesList.value = listOf()
    }

    fun resetLastMessagesReceivedList() {
        lastMessageReceivedList.value = listOf()
    }
    fun resetMessagesReceivedListPreview() {
        messagesReceivedListPreview.value = listOf()
    }

    fun sortAllMessages() {
        var temp = allMessagesList.value
        temp?.sortedBy {
            it.timeStamp
        }
        allMessagesList.value = temp
    }


    fun fetchSentMessages(receiverUID: String) {
        dbHelp.dbFetchMessageBySenderReceiver(currUser.value!!.uid, receiverUID, messagesSentByMeList, allMessagesList)
       // sortAllMessages()
    }
    fun fetchReceivedMessages(senderUID: String) {
        dbHelp.dbFetchMessageBySenderReceiver(senderUID, currUser.value!!.uid, messagesReceivedByMeList, allMessagesList)
      //  sortAllMessages()
    }

    fun fetchLastReceivedMessages() {
        dbHelp.dbFetchLastMessageReceived(currUser.value!!.uid, messagesReceivedListPreview, lastMessageReceivedList)
    }

    fun createMessage(receiverUID: String, receiverName: String, messageText: String) {
        val message = Message(
            senderUID = currUser.value!!.uid,
            senderName = currUser.value!!.name,
            receiverUID = receiverUID,
            receiverName = receiverName,
            messageText = messageText
        )
        dbHelp.dbCreateMessage(message, messagesSentByMeList, allMessagesList)
    }





    ////////////////////////////////////////////////////////////////////////
    /*
     Glide fetch
     */

    fun glideFetch(pictureUUID: String, imageView: ImageView) {
        Glide.fetch(storage.uuid2StorageReference(pictureUUID), imageView)
    }

    ////////////////////////////////////////////////////////////////////////


    ////////////////////////////////////////////////////////////////////////
    /*
     Initialize storage
     */
    fun firestoreInit(storage: Storage) {
        this.storage = storage
    }
    ////////////////////////////////////////////////////////////////////////


}