package edu.utap.sharein.ui.profile

import android.R.attr.data
import android.app.Activity
import android.content.Intent
import android.graphics.*
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.ActionOnlyNavDirections
import androidx.navigation.fragment.findNavController
import com.firebase.ui.auth.IdpResponse
import edu.utap.sharein.AuthInitActivity
import edu.utap.sharein.MainViewModel
import edu.utap.sharein.R


class ProfileFragment : Fragment() {
    companion object {
        const val rcSignIn = 17
        const val rcUploadProfilePhoto = 37
        const val rcCropPhoto = 47
    }

    private lateinit var notificationsViewModel: ProfileViewModel
    private val viewModel: MainViewModel by activityViewModels()
    private lateinit var userProfilePhoto: ImageView

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

        userProfilePhoto = root.findViewById<ImageView>(R.id.userProfilePhoto)
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

        val uploadProfilePhotoButton = root.findViewById<Button>(R.id.uploadProfilePhotoBut)
        uploadProfilePhotoButton.setOnClickListener {
            val uploadProfilePhotoIntent = Intent(Intent.ACTION_PICK)
            uploadProfilePhotoIntent.setType("image/*")
            startActivityForResult(uploadProfilePhotoIntent, rcUploadProfilePhoto)
        }



        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val userName = view.findViewById<TextView>(R.id.userName)
        viewModel.observeUser().observe(viewLifecycleOwner, Observer {
            if (it != null) {
                userName.text = it.name
            }

        })

        val userEmail = view.findViewById<TextView>(R.id.userEmail)
        userEmail.text = viewModel.getEmail()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == AuthInitActivity.rcSignIn) {
            val response = IdpResponse.fromResultIntent(data)

            if (resultCode == Activity.RESULT_OK) {
               val action = ActionOnlyNavDirections(R.id.action_navigation_profile_to_navigation_home)
                findNavController().navigate(action)
            }
        }
        if (requestCode == rcUploadProfilePhoto) {
            if (resultCode == Activity.RESULT_OK) {
                Log.d(javaClass.simpleName, " uri is ${data?.data.toString()}")
                cropPhoto(data?.data)
            }
        }
        if (requestCode == rcCropPhoto) {
            if (data != null) {
                val photo: Bitmap = bindImageToView(data)!!
                viewModel.uploadProfilePhoto(photo)

            }
        }
    }

    private fun cropPhoto(uri: Uri?) {
        if (uri == null) {
            Log.d(javaClass.simpleName, "uri doesn't exist")
        }
        val intent = Intent("com.android.camera.action.CROP")

        intent.setDataAndType(uri, "image/*")
        intent.putExtra("aspectX", 1)
        intent.putExtra("aspectY", 1)
        intent.putExtra("outputX", 150)
        intent.putExtra("outputY", 150)
        intent.putExtra("return-data", true)
        intent.putExtra(MediaStore.EXTRA_OUTPUT, uri)
        intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
        startActivityForResult(intent, rcCropPhoto)


    }

    private fun bindImageToView(data: Intent): Bitmap? {
        var photo: Bitmap? = null
        val extras = data.extras
        if (extras != null) {
            photo = extras.getParcelable("data")!!
            photo = getRoundBitmap(photo)
            userProfilePhoto.setImageBitmap(photo)

        }
        return photo
    }

    // https://stackoverflow.com/questions/12944275/crop-image-as-circle-in-android

    private fun getRoundBitmap(bitmap: Bitmap): Bitmap {
        val width = bitmap.width
        val height = bitmap.height

        val output = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)
        val paintColor = Paint()
        paintColor.flags = Paint.ANTI_ALIAS_FLAG
        val rectF = RectF(Rect(0, 0, width, height))
        canvas.drawRoundRect(rectF, (width / 2).toFloat(), (height / 2).toFloat(), paintColor)

        val paintImage = Paint()
        paintImage.setXfermode(PorterDuffXfermode(PorterDuff.Mode.SRC_ATOP))
        canvas.drawBitmap(bitmap, 0.toFloat(), 0.toFloat(), paintImage)

        return output

    }
}