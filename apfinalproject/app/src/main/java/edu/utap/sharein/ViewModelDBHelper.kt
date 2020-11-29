package edu.utap.sharein

import android.util.Log
import android.widget.ImageView
import androidx.lifecycle.MutableLiveData
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import edu.utap.sharein.glide.Glide
import edu.utap.sharein.model.Follow
import edu.utap.sharein.model.Post
import edu.utap.sharein.model.User

class ViewModelDBHelper(postsList: MutableLiveData<List<Post>>) {
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()


    init {
        dbFetchPostsTrending(postsList)

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
    fun dbFetchPostsTrending(postsList: MutableLiveData<List<Post>>) {
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

    fun dbFetchPostsFollow(followers: List<String>, postsList: MutableLiveData<List<Post>>) {
        db.collection("allPosts")
            .orderBy("timeStamp", Query.Direction.DESCENDING)
            .whereIn("ownerUid", followers)
            .limit(100)
            .get()
            .addOnSuccessListener {

            }
            .addOnFailureListener {

            }
    }

    fun dbFetchPostsCurrUser(uid: String, postsList: MutableLiveData<List<Post>>) {
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
                        Constants.FETCH_TRENDING -> {
                            dbFetchPostsTrending(postsList)
                        }
                        Constants.FETCH_NEARBY -> {

                        }
                        Constants.FETCH_CURR_USER_POSTS -> {
                            dbFetchPostsCurrUser(post.ownerUid, postsList)
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
                        Constants.FETCH_TRENDING -> {
                            Log.d("create new", "fetch trending")
                            dbFetchPostsTrending(postsList)
                        }
                        Constants.FETCH_NEARBY -> {

                        }
                        Constants.FETCH_CURR_USER_POSTS -> {
                            dbFetchPostsCurrUser(post.ownerUid, postsList)
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
                        Constants.FETCH_TRENDING -> {
                            dbFetchPostsTrending(postsList)
                        }
                        Constants.FETCH_NEARBY -> {

                        }
                        Constants.FETCH_CURR_USER_POSTS -> {
                            dbFetchPostsCurrUser(post.ownerUid, postsList)
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

    fun follow(follow: Follow) {
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

    fun unfollow(follow: Follow) {
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
}
