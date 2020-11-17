package edu.utap.sharein.ui.notifications

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.ActionOnlyNavDirections
import androidx.navigation.fragment.findNavController
import com.firebase.ui.auth.IdpResponse
import edu.utap.sharein.AuthInitActivity
import edu.utap.sharein.MainActivity
import edu.utap.sharein.MainViewModel
import edu.utap.sharein.R
import edu.utap.sharein.ui.home.HomeFragment

class ProfileFragment : Fragment() {
    companion object {
        const val rcSignIn = 17
    }

    private lateinit var notificationsViewModel: ProfileViewModel
    private val viewModel: MainViewModel by activityViewModels()

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        notificationsViewModel =
                ViewModelProvider(this).get(ProfileViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_profile, container, false)
//        val textView: TextView = root.findViewById(R.id.text_notifications)
//        notificationsViewModel.text.observe(viewLifecycleOwner, Observer {
//            textView.text = it
//        })
        /*
         click on the sign out button in profile fragment to sign out
         */
        val signOutButton = root.findViewById<Button>(R.id.signOutBut)
        signOutButton.setOnClickListener {
            viewModel.signOut()
            val authInitIntent = Intent(activity, AuthInitActivity::class.java)
            startActivityForResult(authInitIntent, rcSignIn)
            val action = ActionOnlyNavDirections(R.id.action_navigation_profile_to_navigation_home)
            findNavController().navigate(action)

        }

        return root
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == AuthInitActivity.rcSignIn) {
            val response = IdpResponse.fromResultIntent(data)

            if (resultCode == Activity.RESULT_OK) {
               val action = ActionOnlyNavDirections(R.id.action_navigation_profile_to_navigation_home)
                findNavController().navigate(action)
            }
        }
    }
}