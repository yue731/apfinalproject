package edu.utap.sharein

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.SavedStateHandle
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class MainViewModel(application: Application, private val state: SavedStateHandle) : AndroidViewModel(application) {
    companion object {

    }

    private var firebaseAuthLiveData = FirebaseAuthLiveData()


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