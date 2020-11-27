package edu.utap.sharein

import android.util.Log
import android.view.View
import android.widget.ImageView
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import edu.utap.sharein.glide.Glide
import edu.utap.sharein.model.Post
import edu.utap.sharein.model.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ViewModelDBHelper(postsList: MutableLiveData<List<Post>>) {
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()


    init {
        dbFetchPosts(postsList)

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
    fun dbFetchPosts(postsList: MutableLiveData<List<Post>>) {
        db.collection("allPosts")
                .orderBy("timeStamp", Query.Direction.DESCENDING) // descending
                .limit(100)
                .get()
                .addOnSuccessListener { result ->
                    Log.d(javaClass.simpleName, "allPosts fetch ${result.documents.size}")
                    postsList.postValue(result.documents.mapNotNull {
                        it.toObject(Post::class.java)
                    })
                }
                .addOnFailureListener {
                    Log.d(javaClass.simpleName, "allPosts fetch FAILED", it)
                }
    }

    

    /*
     After successfully modify db, re-fetch the contents to update live data
     Thus dbFetchPosts is called here
     */
    fun updatePost(post: Post, postsList: MutableLiveData<List<Post>>) {
        val pictureUUIDs = post.pictureUUIDs
        db.collection("allPosts")
                .document(post.postID)
                .set(post)
                .addOnSuccessListener {
                    Log.d(javaClass.simpleName, "Post update success \"${ellipsizeString(post.text)}\" len ${pictureUUIDs.size} id ${post.postID}")
                    dbFetchPosts(postsList)
                }
                .addOnFailureListener {
                    Log.d(javaClass.simpleName, "Post update FAILED \"${ellipsizeString(post.text)}\"")
                    Log.w(javaClass.simpleName, "Error ", it)
                }
    }

    /*
     After creating the post, need to re-fetch the contents
     */
    fun createPost(post: Post, postsList: MutableLiveData<List<Post>>) {
        // cloud firestore generates the postID
        post.postID = db.collection("allPosts").document().id
        db.collection("allPosts")

                .document(post.postID)
                .set(post)
                .addOnSuccessListener {
                    Log.d(javaClass.simpleName, "Post create success \"${ellipsizeString(post.text)}\" id ${post.postID}")
                    dbFetchPosts(postsList)
                }
                .addOnFailureListener {
                    Log.d(javaClass.simpleName, "Post create FAILED \"${ellipsizeString(post.text)}\"")
                    Log.w(javaClass.simpleName, "Error ", it)
                }
    }

    /*
     After deleting the post, need to re-fetch the contents
     */
    fun removePost(post: Post, postsList: MutableLiveData<List<Post>>) {
        db.collection("allPosts").document(post.postID).delete()
                .addOnSuccessListener {
                    Log.d(javaClass.simpleName, "Post delete success \"${ellipsizeString(post.text)}\" id ${post.postID}")
                    dbFetchPosts(postsList)
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

    fun dbFetchOwner(imageView: ImageView, uid: String, storage: Storage){
        // fetch post owner and bind profile photo too image view
        db.collection("allUsers")
            .document(uid)
            .get()
            .addOnSuccessListener { result ->
                val u: User? = result.toObject(User::class.java)

                if (u != null) {
                    Glide.fetch(storage.uuid2StorageReference(u.profilePhotoUUID), imageView)
                }


            }
            .addOnFailureListener {

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
}
