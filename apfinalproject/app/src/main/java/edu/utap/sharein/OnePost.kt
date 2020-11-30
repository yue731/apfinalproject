package edu.utap.sharein

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class OnePost: Fragment(R.layout.one_post_view) {

    private val viewModel: MainViewModel by activityViewModels()
    private val args: OnePostArgs by navArgs()
    private var position = -1

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        displayPost(view)
        Log.d(javaClass.simpleName, "on view created")




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
        var addFriendIV: ImageView = view.findViewById(R.id.addFriendIV)
        var likeClicked: Boolean = false



        position = args.position
        val post = viewModel.getPost(position)
        //  to bind profile photo
        viewModel.fetchOwner(profilePhotoIV, post.ownerUid) // fetch post owner and bind profile photo to image view
        profilePhotoIV.setOnClickListener {
            if (post.ownerUid == viewModel.observeUser().value!!.uid) {
                val action = OnePostDirections.actionNavigationOnePostToNavigationMe(-1, "Me", viewModel.observeUser().value!!.uid)
                findNavController().navigate(action)
            }
            else {
                val action = OnePostDirections.actionNavigationOnePostToNavigationMe(position, "", post.ownerUid)
                findNavController().navigate(action)
            }

        }
        userNameTV.text = post.name

        //  bind photos swipe
        val imageSiderRVAdapter = ImageSliderRVAdapter(viewModel, post)
        photoVP.adapter = imageSiderRVAdapter

        val indicator = view.findViewById<TabLayout>(R.id.indicator)
        TabLayoutMediator(indicator, photoVP) { tab, position ->

        }.attach()




        titleTV.text = post.title
        postTV.text = post.text
        postTV.movementMethod = ScrollingMovementMethod()

        // handle follow
        val meUID = viewModel.observeUser().value!!.uid
        val postOwnerUID = post.ownerUid


        if (meUID == postOwnerUID) {
            addFriendIV.setImageDrawable(ColorDrawable(Color.TRANSPARENT))
            addFriendIV.isClickable = false
        }
        else {
            viewModel.resetFollowingList()
            viewModel.observeFollowing().observe(viewLifecycleOwner, Observer {
                var isFollowing = viewModel.isFollowing(postOwnerUID)
                if (isFollowing) {
                    addFriendIV.setImageResource(R.drawable.ic_baseline_check_24)
                    addFriendIV.setOnClickListener {
                        addFriendIV.setImageResource(R.drawable.ic_baseline_person_add_24)
                        viewModel.unfollow(viewModel.observeUser().value!!, meUID, postOwnerUID)
                    }
                }
                else {
                    addFriendIV.setImageResource(R.drawable.ic_baseline_person_add_24)
                    addFriendIV.setOnClickListener {
                        addFriendIV.setImageResource(R.drawable.ic_baseline_check_24)
                        viewModel.follow(meUID, postOwnerUID)
                    }
                }
            })
            viewModel.fetchFollowing(meUID)
        }



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

    override fun onResume() {
        Log.d(javaClass.simpleName, "on resume")
        super.onResume()
    }

    override fun onStop() {
        Log.d(javaClass.simpleName, "on stop")
        super.onStop()
    }

    override fun onPause() {
        Log.d(javaClass.simpleName, "on pause")
        super.onPause()
    }

    override fun onDestroy() {
        Log.d(javaClass.simpleName, "on destroy")
        super.onDestroy()
    }




}