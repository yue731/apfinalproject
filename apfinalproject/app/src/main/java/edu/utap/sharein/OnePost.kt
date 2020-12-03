package edu.utap.sharein

import android.app.AlertDialog
import android.content.res.AssetFileDescriptor
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.media.MediaPlayer
import android.os.Bundle
import android.text.TextUtils
import android.text.method.ScrollingMovementMethod
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class OnePost: Fragment(R.layout.one_post_view) {

    private val viewModel: MainViewModel by activityViewModels()
    private val args: OnePostArgs by navArgs()
    private var position = -1
    private lateinit var commentAdapter: CommentAdapter
    private lateinit var player: MediaPlayer

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

        var locationTV: TextView = view.findViewById(R.id.locationTVInPostView)
        // XXX to write music
        var likeIcon: ImageView = view.findViewById(R.id.onePostLikeIcon)
        var likeCount: TextView = view.findViewById(R.id.onePostLikeCount)
        var comment: ImageView = view.findViewById(R.id.onePostComment)
        var commentsRV: RecyclerView = view.findViewById(R.id.commentsRV)
        var addFriendIV: ImageView = view.findViewById(R.id.addFriendIV)




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
            viewModel.pushUser()
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
                Log.d(javaClass.simpleName, "following is true? ")
                if (isFollowing) {
                    addFriendIV.setImageResource(R.drawable.ic_baseline_check_24)
                    addFriendIV.setOnClickListener {
//                        addFriendIV.setImageResource(R.drawable.ic_baseline_person_add_24)
                        viewModel.unfollow(viewModel.observeUser().value!!, meUID, postOwnerUID)
                        viewModel.fetchFollowing(meUID)
                    }
                }
                else {
                    addFriendIV.setImageResource(R.drawable.ic_baseline_person_add_24)
                    addFriendIV.setOnClickListener {
//                        addFriendIV.setImageResource(R.drawable.ic_baseline_check_24)
                        viewModel.follow(meUID, postOwnerUID)
                        viewModel.fetchFollowing(meUID)
                    }
                }
            })
            viewModel.fetchFollowing(meUID)
        }

        // XXX refine like
        viewModel.resetOnePostLikes()
        viewModel.resetUserLikedPosts()
        viewModel.observeOnePostLikes().observe(viewLifecycleOwner, Observer { onePostLikes ->
            viewModel.observeUserLikedPosts().observe(viewLifecycleOwner, Observer { userLikedPosts ->
                var isLiked = viewModel.isLiked(post.postID)
                Log.d(javaClass.simpleName, "this post is liked ${isLiked}")
                if (isLiked) {
                    likeIcon.setImageResource(R.drawable.ic_baseline_favorite_24)
                    likeIcon.setOnClickListener {
                        Log.d(javaClass.simpleName, "post is unliked")
//                        likeIcon.setImageResource(R.drawable.ic_baseline_favorite_border_24)
                        viewModel.unlike(post.postID)
                        likeCount.text = (onePostLikes.size - 1).toString()
                        viewModel.fetchOnePostLikes(post.postID, likeCount)
                        viewModel.fetchUserLikedPostsLikes(viewModel.observeUser().value!!.uid)
                    }
                }
                else {
                    likeIcon.setImageResource((R.drawable.ic_baseline_favorite_border_24))
                    likeIcon.setOnClickListener {
                        Log.d(javaClass.simpleName, "post is liked")
//                        likeIcon.setImageResource(R.drawable.ic_baseline_favorite_24)
                        viewModel.like(post.postID)
                        likeCount.text = (onePostLikes.size + 1).toString()
                        viewModel.fetchOnePostLikes(post.postID, likeCount)
                        viewModel.fetchUserLikedPostsLikes(viewModel.observeUser().value!!.uid)
                    }
                }
            })

        })
        viewModel.fetchOnePostLikes(post.postID, likeCount)
        viewModel.fetchUserLikedPostsLikes(viewModel.observeUser().value!!.uid)

        // play background music
        val musicRawID = post.musicRawID
        if(musicRawID != -1) {
            player = MediaPlayer.create(requireContext(), musicRawID)
            player.start()
            player.setOnCompletionListener {
                it.reset()
                val fd: AssetFileDescriptor = resources.openRawResourceFd(musicRawID)
                it.setDataSource(fd.fileDescriptor, fd.startOffset, fd.length)
                it.prepare()
                it.start()
            }
        }





        // XXX bind location

        // deal with comment
        commentsRV.layoutManager = LinearLayoutManager(context)
        commentAdapter = CommentAdapter(viewModel, ::deleteComment)
        commentsRV.adapter = commentAdapter

        viewModel.resetOnePostComments()

        viewModel.observeOnePostComments().observe(viewLifecycleOwner, Observer {
            commentAdapter.clearAll()
            commentAdapter.addAll(it)
            commentAdapter.notifyDataSetChanged()
        })
        viewModel.fetchOnePostComments(post.postID)

        comment.setOnClickListener {
            val commentPopUpView = LayoutInflater.from(requireContext()).inflate(R.layout.comment_pop_up, null)
            val dialogueBuilderCommentPopUp = AlertDialog.Builder(requireContext())
            dialogueBuilderCommentPopUp.setCancelable(false)
                .setView(commentPopUpView)
            val alertComment = dialogueBuilderCommentPopUp.create()
            alertComment.show()
            val submitBut = commentPopUpView.findViewById<Button>(R.id.submitCommentBut)
            val canncelSubmitBut = commentPopUpView.findViewById<Button>(R.id.cancelSubmitCommentBut)
            val commentET = commentPopUpView.findViewById<EditText>(R.id.commentET)
            commentET.requestFocus()

            submitBut.setOnClickListener {
                if (TextUtils.isEmpty(commentET.text.toString())) {
                    Toast.makeText(activity, "Enter comment!", Toast.LENGTH_LONG).show()
                }
                else {
                    viewModel.createComment(post.postID, commentET.text.toString())
                    alertComment.cancel()
                }

            }
            canncelSubmitBut.setOnClickListener{
                alertComment.cancel()
            }

        }





    }

    private fun deleteComment(position: Int) {

        if (permission(position)) {
            val deleteCommentView = LayoutInflater.from(requireContext()).inflate(R.layout.delete_comment_alert, null)
            val dialogBuilder = AlertDialog.Builder(requireContext())
            dialogBuilder.setCancelable(false)
                .setView(deleteCommentView)
            val alert = dialogBuilder.create()
            alert.show()
            val deleteCommentBut = deleteCommentView.findViewById<Button>(R.id.deleteCommentBut)
            val cancelDeleteCommentBut = deleteCommentView.findViewById<Button>(R.id.cancelDeleteCommentBut)
            deleteCommentBut.setOnClickListener {
                viewModel.deleteCommentAt(position)
                alert.cancel()
            }
            cancelDeleteCommentBut.setOnClickListener {
                alert.cancel()
            }

        }
    }

    private fun permission(position: Int): Boolean {
        val comment = viewModel.getComment(position)
        return comment.userUID == viewModel.observeUser().value!!.uid
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
        if (player != null) {
            player.release()
        }
        super.onPause()
    }

    override fun onDestroy() {
        Log.d(javaClass.simpleName, "on destroy")
        super.onDestroy()
    }




}