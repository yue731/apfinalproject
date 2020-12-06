package edu.utap.sharein

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import androidx.activity.viewModels
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest

class MainActivity : AppCompatActivity() {
    companion object {
        const val rcSignIn = 17
        const val cameraRC = 10

    }

    private val viewModel: MainViewModel by viewModels()


    // An Android nightmare
    // https://stackoverflow.com/questions/1109022/close-hide-the-android-soft-keyboard
    // https://stackoverflow.com/questions/7789514/how-to-get-activitys-windowtoken-without-view
    fun hideKeyboard() {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(window.decorView.rootView.windowToken, 0);
    }





    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)




        val navView: BottomNavigationView = findViewById(R.id.nav_view)
        Log.d(javaClass.simpleName, "navigation inflated")



        val navController = findNavController(R.id.nav_host_fragment)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        val appBarConfiguration = AppBarConfiguration(setOf(
                R.id.navigation_home, R.id.navigation_new_post, R.id.navigation_message_preview, R.id.navigation_me))
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        navController.addOnDestinationChangedListener { controller, destination, arguments ->
            when (destination.id) {
                R.id.navigation_home -> {
                    viewModel.updateFetchStatus(Constants.FETCH_ALL)
                    viewModel.fetchPosts(viewModel.observeFetchStatus().value!!, "")
                }
                R.id.navigation_me -> {
                    viewModel.updateFetchStatus(Constants.FETCH_CURR_USER_POSTS)
                    viewModel.fetchPosts(viewModel.observeFetchStatus().value!!, "")
                }
            }
        }

        // take photo intent
        viewModel.setPhotoIntent(::takePhotoIntent)

        // storage initialization
        viewModel.firestoreInit(Storage())


        viewModel.initFetchStatus()

        // user authentication
        initUserUI()
        val authInitIntent = Intent(this, AuthInitActivity::class.java)
        startActivityForResult(authInitIntent, rcSignIn)
        viewModel.observeUserId().observe(this, Observer {
            var currUser = viewModel.observeUser().value
            var currUserId = viewModel.observeUserId().value //  id is set onResume if logged in already
            if (currUserId != null && currUser == null) {
                Log.d(javaClass.simpleName, "curr user cannot be found")
                viewModel.createUser()
                setUserName()

            }

        })

        viewModel.observeUser().observe(this, Observer {
            Log.d(javaClass.simpleName, "curr user is ${viewModel.observeUser().value}")
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
        viewModel.updateFetchStatus(Constants.FETCH_ALL)
        viewModel.fetchPosts(viewModel.observeFetchStatus().value!!, "")

        val currUserUID =viewModel.myUid()

        Log.d(javaClass.simpleName, "curr user uid is ${currUserUID}")

        if(currUserUID == null) {
            // not logged in yet
            super.onResume()
            return
        }

        Log.d(javaClass.simpleName, "curr user uid is not null")
        viewModel.fetchUser(currUserUID)




        super.onResume()

    }






}