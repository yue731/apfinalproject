package edu.utap.sharein

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.PersistableBundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest

class AuthInitActivity: AppCompatActivity() {
    companion object {
        const val rcSignIn = 17
        val providers = arrayListOf(
            AuthUI.IdpConfig.EmailBuilder().build()
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(javaClass.simpleName, "auth created")
        if (FirebaseAuth.getInstance().currentUser == null) {
            startActivityForResult(
                AuthUI.getInstance()
                    .createSignInIntentBuilder()
                    .setIsSmartLockEnabled(false)
                    .setAvailableProviders(providers)
                    .build(),
                rcSignIn
            )

        }
        else {
            Log.d(javaClass.simpleName, "user ${FirebaseAuth.getInstance().currentUser?.displayName} email ${FirebaseAuth.getInstance().currentUser?.email}")
            setDefaultDisplayNameByEmail()
            finish()
        }
    }

    /*
     Set the default user display name by email
     */
    private fun setDefaultDisplayNameByEmail() {
        Log.d(javaClass.simpleName, "set name by address called")
        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            Log.d(javaClass.simpleName, "setDefaultDisplayNameByEmail current user null")

        }
        else if (user.displayName == null || user.displayName!!.isEmpty()) {
            user.apply {
                val displayName = this.email?.substringBefore("@")
                val profileUpdates = UserProfileChangeRequest.Builder()
                    .setDisplayName(displayName)
                    .build()

                if (displayName != null && displayName.isNotEmpty()) {
                    this.updateProfile(profileUpdates)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                Log.d(javaClass.simpleName, "User profile updated")
                            }
                            finish()
                        }

                }
                else {
                    Log.d(javaClass.simpleName, "displayname set to $displayName")
                }
            }
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.d(javaClass.simpleName, "activity result $resultCode")
        if (resultCode == rcSignIn) {
            val response = IdpResponse.fromResultIntent(data)



            if (resultCode == Activity.RESULT_OK) {
                setDefaultDisplayNameByEmail()
                finish()
            }
        }
    }

    override fun onResume() {
        Log.d(javaClass.simpleName, "auth resumed")

        super.onResume()
        finish()
    }

    override fun onDestroy() {
        Log.d(javaClass.simpleName, "auth destroyed")
        super.onDestroy()
    }

    override fun onPause() {
        Log.d(javaClass.simpleName, "auth paused")
        super.onPause()
    }

    override fun onStop() {
        Log.d(javaClass.simpleName, "auth stopped")
        super.onStop()
    }
}