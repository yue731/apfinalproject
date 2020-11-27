package edu.utap.sharein

import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.view.View
import android.widget.ImageView
import android.widget.TableLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.navArgs
import androidx.viewpager.widget.ViewPager
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.viewpagerindicator.CirclePageIndicator

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
        var photoVP: ViewPager2 = view.findViewById(R.id.photoVP) // XXX to do a image swipe

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
        viewModel.fetchOwner(profilePhotoIV, post.ownerUid) // fetch post owner and bind profile photo too image view

        userNameTV.text = post.name

        // XXX bind photos swipe
        val imageSiderRVAdapter = ImageSliderRVAdapter(viewModel, post)
        photoVP.adapter = imageSiderRVAdapter

        val indicator = view.findViewById<TabLayout>(R.id.indicator)
        TabLayoutMediator(indicator, photoVP) { tab, position ->

        }.attach()











        titleTV.text = post.title
        postTV.text = post.text
        postTV.movementMethod = ScrollingMovementMethod()
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