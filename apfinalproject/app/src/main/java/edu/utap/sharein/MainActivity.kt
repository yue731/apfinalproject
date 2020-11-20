package edu.utap.sharein

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.Menu
import androidx.activity.viewModels
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController

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
}