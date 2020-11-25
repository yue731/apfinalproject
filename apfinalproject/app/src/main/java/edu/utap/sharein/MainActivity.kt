package edu.utap.sharein

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.widget.Button
import android.widget.EditText
import androidx.activity.viewModels
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.navigation.ActionOnlyNavDirections
import androidx.navigation.Navigation
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import edu.utap.sharein.model.User
import edu.utap.sharein.ui.home.HomeFragmentDirections
import kotlinx.android.synthetic.main.set_user_name.*

class MainActivity : AppCompatActivity() {
    companion object {
        const val rcSignIn = 17
        const val cameraRC = 10
    }

    private val viewModel: MainViewModel by viewModels()




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)




        val navView: BottomNavigationView = findViewById(R.id.nav_view)
        Log.d(javaClass.simpleName, "navigation inflated")

        val navController = findNavController(R.id.nav_host_fragment)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        val appBarConfiguration = AppBarConfiguration(setOf(
                R.id.navigation_home, R.id.navigation_new_post, R.id.navigation_profile))
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        // take photo intent
        viewModel.setPhotoIntent(::takePhotoIntent)

        // storage initialization
        viewModel.firestoreInit(Storage())

        // user authentication
        initUserUI()
        val authInitIntent = Intent(this, AuthInitActivity::class.java)
        startActivityForResult(authInitIntent, rcSignIn)
        viewModel.observeUserId().observe(this, Observer {
            var currUser = viewModel.observeUser().value
            var currUserId = viewModel.observeUserId().value
            if (currUserId != null && currUser == null) {
                Log.d(javaClass.simpleName, "curr user cannot be found")
                viewModel.createUser()
                setUserName()
            }

        })




    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when(requestCode) {
            cameraRC -> {
                if (resultCode == RESULT_OK) {
                    viewModel.photoSuccess()
                }
                else{
                    viewModel.photoFailure()
                }
            }

        }
    }



//    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
//        // inflate the menu: add items to action bar
//        menuInflater.inflate(R.menu.main_menu, menu)
//        return true
//    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    private fun initUserUI() {
        viewModel.observeFirebaseAuthLiveData().observe(this, Observer {
            if (it == null) {
                Log.d(javaClass.simpleName, "No one is signed in")
            }
            else {
                Log.d(javaClass.simpleName, "${it.displayName} ${it.email} ${it.uid} signed in")
            }
        })
    }

    private fun takePhotoIntent() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also {
            it.putExtra(MediaStore.EXTRA_OUTPUT, viewModel.getPhotoURI())
            startActivityForResult(it, cameraRC)
        }
    }

    private fun setUserName() {
        Log.d(javaClass.simpleName, "setUserName is called")
        var setButPressed = false

        val currUserUID = viewModel.myUid()
        val currUser = viewModel.observeUser().value
        Log.d(javaClass.simpleName, "curr user to set name is ${currUser}")

        // inflate the set user name view
        val setUserNameView = LayoutInflater.from(this).inflate(R.layout.set_user_name, null)
        val setUserNameET = setUserNameView.findViewById<EditText>(R.id.setUserNameET)
        setUserNameET.hint = currUser!!.name
        setUserNameET.requestFocus()
        val dialogBuilder = AlertDialog.Builder(this)
        dialogBuilder.setCancelable(false)
                .setView(setUserNameView)
        val alert = dialogBuilder.create()
        alert.show()

        val setUserNameBut = setUserNameView.findViewById<Button>(R.id.setUserNameBut)
        setUserNameBut.setOnClickListener {

            val userName = setUserNameET.text.toString()
            // XXX update firebase auth user profile
            FirebaseAuth.getInstance().currentUser?.apply {
                val profileUpdates = UserProfileChangeRequest.Builder()
                        .setDisplayName(userName)
                        .build()
                this.updateProfile(profileUpdates)
            }


            // XXX update User: name and isUserNameSet
            currUser!!.name = userName
            currUser.isUserNameSet = true
            viewModel.updateUser(currUser)
            alert.cancel()


        }

        val cancelUserNameBut = setUserNameView.findViewById<Button>(R.id.cancelUserNameBut)
        cancelUserNameBut.setOnClickListener {
            currUser!!.isUserNameSet = true
            viewModel.updateUser(currUser)
            alert.cancel()
        }


    }

    override fun onStop() {
        Log.d(javaClass.simpleName, " mainactivity stopped")
        viewModel.resetUser()
        super.onStop()
    }

    override fun onPause() {
        Log.d(javaClass.simpleName, " mainactivity paused")
        super.onPause()
    }

    override fun onRestart() {
        Log.d(javaClass.simpleName, " mainactivity restarted")

        super.onRestart()
    }

    override fun onResume() {
        Log.d(javaClass.simpleName, "mainactivity resumed")
        viewModel.fetchPosts()

        val currUserUID =viewModel.myUid()

        Log.d(javaClass.simpleName, "curr user uid is ${currUserUID}")

        if(currUserUID == null) {
            super.onResume()
            return
        }

        Log.d(javaClass.simpleName, "curr user uid is not null")
        viewModel.fetchUser(currUserUID)


        super.onResume()

    }






}