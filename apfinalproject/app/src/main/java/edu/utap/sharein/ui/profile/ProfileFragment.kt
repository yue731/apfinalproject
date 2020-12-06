package edu.utap.sharein.ui.profile

import android.app.Activity
import android.content.Intent
import android.graphics.*
import android.graphics.drawable.Drawable
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
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.firebase.ui.auth.IdpResponse
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import edu.utap.sharein.AuthInitActivity
import edu.utap.sharein.MainViewModel
import edu.utap.sharein.R


class ProfileFragment : Fragment() {
    companion object {
        const val rcSignIn = 17
        const val rcPickFromGallery = 37
        const val rcCropPhoto = 47
    }

    private lateinit var profileViewModel: ProfileViewModel
    private val viewModel: MainViewModel by activityViewModels()
    private lateinit var userProfilePhoto: ImageView

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        profileViewModel =
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
            viewModel.resetUser()
            val authInitIntent = Intent(activity, AuthInitActivity::class.java)
            startActivityForResult(authInitIntent, rcSignIn)
            val action = ActionOnlyNavDirections(R.id.action_navigation_profile_to_navigation_home)
            findNavController().navigate(action)

        }

        val uploadProfilePhotoButton = root.findViewById<Button>(R.id.uploadProfilePhotoBut)
        val user = viewModel.observeUser().value
        if (user != null && user.profilePhotoUUID != "") {
            viewModel.glideFetch(user.profilePhotoUUID, userProfilePhoto)
        }
        else {
            userProfilePhoto.setImageResource(R.drawable.profile)
        }

        uploadProfilePhotoButton.setOnClickListener {
            val pickFromGallery = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            pickFromGallery.type = "image/*"
            val mimeTypes = arrayOf("image/jpeg", "image/png", "image/jpg")
            pickFromGallery.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes)
            pickFromGallery.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            startActivityForResult(pickFromGallery, rcPickFromGallery)
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
        if (requestCode == rcPickFromGallery) {
            if (resultCode == Activity.RESULT_OK) {
                Log.d(javaClass.simpleName, " uri is ${data?.data.toString()}")
                cropPhoto(data?.data!!)
            }
            else {
                Log.d(javaClass.simpleName, "Pick image from gallery failed")
            }
        }
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            val result = CropImage.getActivityResult(data)

            if (resultCode == Activity.RESULT_OK && data != null) {
                bindImageToView(result.uri)
                val bitmap = MediaStore.Images.Media.getBitmap(requireContext().contentResolver, result.uri)

                viewModel.uploadProfilePhoto(bitmap)



            }
            else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Log.d(javaClass.simpleName, "Crop error ${result.error}")
            }

        }
    }

    private fun cropPhoto(uri: Uri) {
//        if (uri == null) {
//            Log.d(javaClass.simpleName, "uri doesn't exist")
//        }
//        val intent = Intent("com.android.camera.action.CROP")
//
//        intent.setDataAndType(uri, "image/*")
//        intent.putExtra("aspectX", 1)
//        intent.putExtra("aspectY", 1)
//        intent.putExtra("outputX", 150)
//        intent.putExtra("outputY", 150)
//        intent.putExtra("return-data", true)
//        intent.putExtra(MediaStore.EXTRA_OUTPUT, uri)
//        intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
//        startActivityForResult(intent, rcCropPhoto)
        CropImage.activity(uri)
                .setGuidelines(CropImageView.Guidelines.ON)
                .setAspectRatio(1, 1)
                .setCropShape(CropImageView.CropShape.RECTANGLE)
                .start(requireContext(), this)


    }

    private fun bindImageToView(uri: Uri){

        Glide.with(requireContext())
                .asBitmap()
                .load(uri)
                .into(object: CustomTarget<Bitmap>(){
                    override fun onLoadCleared(placeholder: Drawable?) {

                    }

                    override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                        userProfilePhoto.setImageBitmap(resource)

                    }

                })


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