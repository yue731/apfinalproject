package edu.utap.sharein

import android.app.Application
import android.net.Uri
import android.os.Environment
import android.util.Log
import android.widget.ImageView
import androidx.core.content.FileProvider
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import edu.utap.sharein.glide.Glide
import edu.utap.sharein.model.Post
import java.io.File
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
    private lateinit var storage: Storage

    private lateinit var crashMe: String
    private var pictureUUID: String = ""
    private var allImages = MutableLiveData<List<String>>()


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
        }
        catch (e: IOException) {
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

    fun getPost(position: Int): Post {
        val post = postsList.value?.get(position)
        return post!!
    }
    // after we successfully modify the post, we need to re-fetch the content to update livedata
    fun updatePost(position: Int, title: String, text: String, pictureUUIDs: List<String>) {
        val post = getPost(position)
        post.title = title
        post.text = text
        post.pictureUUIDs = pictureUUIDs
        dbHelp.updatePost(post, postsList)

    }

    fun createPost(title: String, text: String, pictureUUIDs: List<String>) {
        val post = Post(
            name = firebaseAuthLiveData.value?.displayName ?: "",
            ownerUid = myUid()!!,
            title = title,
            text = text,
            pictureUUIDs = pictureUUIDs
        )
        dbHelp.createPost(post, postsList)
    }

    fun removePostAt(position: Int) {
        val post = getPost(position)
        // delete each image before deleting the post itself
        post.pictureUUIDs.forEach {
            storage.deleteImage(it)
        }
        dbHelp.removePost(post, postsList)
    }
    ////////////////////////////////////////////////////////////////////////




    ////////////////////////////////////////////////////////////////////////
    /*
     Deal with images
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
    ////////////////////////////////////////////////////////////////////////




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