package edu.utap.sharein

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import edu.utap.sharein.model.User


class MeFragment : Fragment() {
    private val viewModel: MainViewModel by activityViewModels()
    private lateinit var postsAdapter: PostsAdapter
    private val args: MeFragmentArgs by navArgs()
    private var user = MutableLiveData<User>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_me, container, false)
        setHasOptionsMenu(true)

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)



        val meProfilePhotoIV = view.findViewById<ImageView>(R.id.meProfilePhotoIV)
        val meUserName = view.findViewById<TextView>(R.id.meUserName)
        val meFollowing = view.findViewById<TextView>(R.id.meFollowing)
        val meFollower = view.findViewById<TextView>(R.id.meFollower)
        val meToProfileBut = view.findViewById<Button>(R.id.meToProfileBut)
        val meRV = view.findViewById<RecyclerView>(R.id.meRV)
        val meFollowIcon = view.findViewById<ImageView>(R.id.meFollowIcon)
        val mePrivateMessage = view.findViewById<ImageView>(R.id.mePrivateMessage)

        user.observe(viewLifecycleOwner, Observer {
            meUserName.text = user.value!!.name
            // XXX wrtie about following and on click listener to list of following
            viewModel.setCurrPageUser(user.value!!)

            viewModel.resetFollowingList()
            viewModel.fetchFollowing(user.value!!.uid)

            // XXX write about follower and on click listener to list of follower
            viewModel.resetFollowerList()
            viewModel.fetchFollower(user.value!!.uid)

            if (user.value != null && user.value!!.profilePhotoUUID != "") {
                viewModel.glideFetch(user.value!!.profilePhotoUUID, meProfilePhotoIV)
            }

            // handle follow
            val currUserUID = viewModel.observeUser().value!!.uid
            val userUID = user.value!!.uid
            if (args.position != -1) {
                viewModel.resetFollowerList()
                viewModel.observeFollower().observe(viewLifecycleOwner, Observer {
                    var isFollower = viewModel.isFollower(currUserUID)
                    if (isFollower) {
                        meFollowIcon.setImageResource(R.drawable.ic_baseline_check_24)
                        meFollowIcon.setOnClickListener {
                           // meFollowIcon.setImageResource(R.drawable.ic_baseline_person_add_24)
                            viewModel.unfollow(user.value!!, currUserUID, userUID)
                            viewModel.fetchFollower(userUID)
                        }
                    }
                    else {
                        meFollowIcon.setImageResource(R.drawable.ic_baseline_person_add_24)
                        meFollowIcon.setOnClickListener {
                           // meFollowIcon.setImageResource(R.drawable.ic_baseline_check_24)
                            viewModel.follow(currUserUID, userUID)
                            viewModel.fetchFollower(userUID)
                        }
                    }
                })
                viewModel.fetchFollower(userUID)
            }
            if (it.uid == viewModel.observeUser().value!!.uid) {
                meFollowIcon.isClickable = false
                meFollowIcon.visibility = View.INVISIBLE
                mePrivateMessage.isClickable = false
                mePrivateMessage.visibility = View.INVISIBLE
            }
            else {
                mePrivateMessage.setOnClickListener {
                    val action = MeFragmentDirections.actionNavigationMeToNavigationMessage(user.value!!.uid, -1)
                    findNavController().navigate(action)
                }
            }

            // handle rv bind
            meRV.layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
            postsAdapter = PostsAdapter(viewModel, ::viewPost, ::editDeleteAlert)
            meRV.adapter = postsAdapter

            viewModel.observePosts().observe(viewLifecycleOwner, Observer {
                postsAdapter.clearAll()

                postsAdapter.addAll(it)
                postsAdapter.notifyDataSetChanged()
            })
            if (args.position != -1) {
                viewModel.updateFetchStatus(Constants.FETCH_OTHER_USER)

                viewModel.fetchPosts(viewModel.observeFetchStatus().value!!, args.uid)
            }

        })

        if (args.position != -1) {
            viewModel.fetchOtherUser(args.uid, user)
        }
        else {
            user.value = viewModel.observeUser().value
        }

        viewModel.observeFollowing().observe(viewLifecycleOwner, Observer {
            if (viewModel.observeFollowing().value == null) {
                meFollowing.text = "0"
            }
            else {
                meFollowing.text = viewModel.observeFollowing().value!!.size.toString()
            }
        })

        meFollowing.setOnClickListener {
            val action = MeFragmentDirections.actionNavigationMeToNavigationFollow(Constants.FOLLOWING)
            findNavController().navigate(action)
        }

        viewModel.observeFollower().observe(viewLifecycleOwner, Observer {
            if (viewModel.observeFollower().value == null) {
                meFollower.text = "0"
            }
            else {
                meFollower.text = viewModel.observeFollower().value!!.size.toString()
            }
        })


        meFollower.setOnClickListener {
            val action = MeFragmentDirections.actionNavigationMeToNavigationFollow(Constants.FOLLOWER)
            findNavController().navigate(action)
        }



        if (args.position == -1) {
            meFollowIcon.isClickable = false
            meFollowIcon.visibility = View.INVISIBLE
            mePrivateMessage.isClickable = false
            mePrivateMessage.visibility = View.INVISIBLE
            meToProfileBut.setOnClickListener {
                val action = MeFragmentDirections.actionNavigationMeToNavigationProfile()
                findNavController().navigate(action)
            }
        }
        else {

            meToProfileBut.isClickable = false
            meToProfileBut.visibility = View.INVISIBLE


        }



    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        if (args.position == -1) {
            inflater.inflate(R.menu.me_menu_top, menu)
        }
        else {
            inflater.inflate(R.menu.me_other_menu_top, menu)
        }

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (args.position == -1) {
            return when (item.itemId) {
                R.id.mePosts -> {
                    viewModel.updateFetchStatus(Constants.FETCH_CURR_USER_POSTS)
                    viewModel.fetchPosts(viewModel.observeFetchStatus().value!!, "")
                    true
                }
                R.id.meLiked -> {
                    Log.d(javaClass.simpleName, "liked is clicked")
                    viewModel.updateFetchStatus(Constants.FETCH_LIKED)
                    viewModel.resetUserLikedPosts()
                    viewModel.resetUserLikedPostsCount()
                    viewModel.observeUserLikedPostsCount().observe(viewLifecycleOwner, Observer {
                        if (it!= 0) {
                            viewModel.fetchPosts(viewModel.observeFetchStatus().value!!, "")
                            Log.d(javaClass.simpleName, "user liked posts size is ${it}")
                        }
                        else {
                            viewModel.resetPosts()
                        }
                    })
                    viewModel.fetchUserLikedPostsLikes(viewModel.observeUser().value!!.uid)

                    true
                }
                else -> super.onOptionsItemSelected(item)
        }

        }
        else {
            return when (item.itemId) {
                R.id.meOtherBack -> {
                    findNavController().popBackStack()

                    viewModel.popUser()
                    Log.d(javaClass.simpleName, "curr user is ${viewModel.getCurrPageUser().name}")
                    viewModel.fetchFollowing(viewModel.getCurrPageUser().uid)
                    viewModel.fetchFollower(viewModel.getCurrPageUser().uid)

                    true
                }
                else -> super.onOptionsItemSelected(item)
            }

        }

    }

    private fun viewPost(position: Int) {
        val action = MeFragmentDirections.actionNavigationMeToNavigationOnePost(position, "Post")
        findNavController().navigate(action)
    }

    private fun editPost(position: Int) {
        if (permission(position)) {
            val action = MeFragmentDirections.actionNavigationMeToNavigationNewPost(position, "Edit")
            findNavController().navigate(action)
        }


    }

    private fun deletePost(position: Int) {
        if (permission(position)) {
            val user = viewModel.observeUser().value
            if (user != null) {
                var tempList = user.postsList.toMutableList()
                tempList.remove(viewModel.getPost(position).postID)
                user.postsList = tempList
                viewModel.updateUser(user)
            }
            viewModel.removePostAt(position)


        }
    }

    private fun editDeleteAlert(position: Int) {
        if (permission(position)) {
            val editOrDeleteLayout = LayoutInflater.from(requireContext()).inflate(R.layout.edit_or_delete_alert, null)
            val dialogBuilder = AlertDialog.Builder(requireContext())
            dialogBuilder.setCancelable(false)
                .setView(editOrDeleteLayout)
            val alert = dialogBuilder.create()
            alert.show()
            val editBut = editOrDeleteLayout.findViewById<Button>(R.id.editBut)
            val deleteBut = editOrDeleteLayout.findViewById<Button>(R.id.deleteBut)
            val cancelEditDeleteBut = editOrDeleteLayout.findViewById<Button>(R.id.cancelEditDeleteBut)

            editBut.setOnClickListener {
                editPost(position)
                alert.cancel()
            }
            deleteBut.setOnClickListener {
                deletePost(position)
                alert.cancel()
            }
            cancelEditDeleteBut.setOnClickListener {
                alert.cancel()
            }
        }
    }

    private fun permission(position: Int): Boolean {
        val post = viewModel.getPost(position)
        val postOwner = post.ownerUid
        val currUser = viewModel.observeUser().value
        return (currUser != null && postOwner == currUser.uid)
    }

    override fun onDestroy() {
        Log.d(javaClass.simpleName, "destroy")
        super.onDestroy()
    }
}