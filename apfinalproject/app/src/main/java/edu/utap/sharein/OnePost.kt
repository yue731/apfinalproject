package edu.utap.sharein

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.navArgs

class OnePost: Fragment(R.layout.one_post_view) {

    private val viewModel: MainViewModel by activityViewModels()
    private val args: OnePostArgs by navArgs()
    private var position = -1

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        displayPost(view)




    }

    private fun displayPost(view: View) {
        var profilePhotoIV: ImageView = view.findViewById(R.id.profilePhotoIV)
        var userNameTV: TextView = view.findViewById(R.id.userNameTV)
        var photoIV: ImageView = view.findViewById(R.id.photoIV) // XXX to do a image swipe
        var titleTV: TextView = view.findViewById(R.id.titleTV)
        var postTV: TextView = view.findViewById(R.id.postTV)
        var tagTV: TextView = view.findViewById(R.id.tagTV)
        var locationTV: TextView = view.findViewById(R.id.locationTVInPostView)
        // XXX to write music
        var likeIcon: ImageView = view.findViewById(R.id.onePostLikeIcon)
        var likeCount: TextView = view.findViewById(R.id.onePostLikeCount)
        var comment: ImageView = view.findViewById(R.id.onePostComment)
        var likeClicked: Boolean = false


        position = args.position
        val post = viewModel.getPost(position)
        // XXX to bind profile photo
        if (post.ownerProfilePhotoUUID != null) {
            viewModel.glideFetch(post.ownerProfilePhotoUUID, profilePhotoIV)
        }

        userNameTV.text = post.name
        // XXX bind photos swipe
        titleTV.text = post.title
        postTV.text = post.text
        // XXX bind tag
        // XXX bind location
        // XXX refine like
        likeIcon.setOnClickListener {
            if (!likeClicked) {
                likeIcon.setImageResource(R.drawable.ic_baseline_favorite_24)
                likeClicked = true
                post.likes += 1
            }
            else {
                likeIcon.setImageResource(R.drawable.ic_baseline_favorite_border_24)
                likeClicked = false
                post.likes -= 1
            }
        }






    }




}