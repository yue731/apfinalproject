package edu.utap.sharein

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.google.firebase.firestore.FirebaseFirestore
import edu.utap.sharein.model.Post

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
    private fun dbFetchPosts(postsList: MutableLiveData<List<Post>>) {
        db.collection("allPosts")
            .orderBy("timeStamp") // descending
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
}
