package edu.utap.sharein

import android.app.AlertDialog
import android.os.Bundle
import android.view.*
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import edu.utap.sharein.ui.home.HomeFragmentDirections


class MeFragment : Fragment() {
    private val viewModel: MainViewModel by activityViewModels()
    private lateinit var postsAdapter: PostsAdapter

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

        val user = viewModel.observeUser().value
        if (user != null && user.profilePhotoUUID != "") {
            viewModel.glideFetch(user.profilePhotoUUID, meProfilePhotoIV)
        }
        meUserName.text = user?.name
        // XXX wrtie about following and on click listener to list of following
        // XXX write about follower and on click listener to list of follower

        meToProfileBut.setOnClickListener {
            val action = MeFragmentDirections.actionNavigationMeToNavigationProfile()
            findNavController().navigate(action)
        }

        meRV.layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        postsAdapter = PostsAdapter(viewModel, ::viewPost, ::editDeleteAlert)
        meRV.adapter = postsAdapter
        viewModel.observePosts().observe(viewLifecycleOwner, Observer {
            postsAdapter.clearAll()

            postsAdapter.addAll(it)
            postsAdapter.notifyDataSetChanged()
        })

    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.me_menu_top, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.mePosts -> {
                viewModel.updateFetchStatus(Constants.FETCH_CURR_USER_POSTS)
                viewModel.fetchPosts(viewModel.observeFetchStatus().value!!)
                true
            }
            R.id.meLiked -> {
                true
            }
            else -> super.onOptionsItemSelected(item)
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
}