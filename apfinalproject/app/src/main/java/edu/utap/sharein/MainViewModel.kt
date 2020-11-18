package edu.utap.sharein

import android.app.Application
import android.os.Environment
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import edu.utap.sharein.model.Post

class MainViewModel(application: Application, private val state: SavedStateHandle) : AndroidViewModel(application) {
    companion object {

    }

    private val appContext = getApplication<Application>().applicationContext
    private val storageDir = getApplication<Application>().getExternalFilesDir(Environment.DIRECTORY_PICTURES)

    private var firebaseAuthLiveData = FirebaseAuthLiveData()
    private var postsList = MutableLiveData<List<Post>>()
    private val dbHelp = ViewModelDBHelper(postsList)
    private lateinit var storage: Storage



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


}