package edu.utap.sharein.ui.home

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.google.firebase.auth.FirebaseUser
import edu.utap.sharein.Constants
import edu.utap.sharein.MainViewModel
import edu.utap.sharein.PostsAdapter
import edu.utap.sharein.R
import kotlinx.android.synthetic.main.fragment_home.*

class HomeFragment : Fragment() {

    private lateinit var homeViewModel: HomeViewModel
    private val viewModel: MainViewModel by activityViewModels()
    private var currentUser: FirebaseUser? = null
    private lateinit var postsAdapter: PostsAdapter

    private fun initAuth() {
        viewModel.observeFirebaseAuthLiveData().observe(viewLifecycleOwner, Observer {
            currentUser = it
        })

    }

    private fun toggleEmptyPosts() {
        if (viewModel.isPostsEmpty()) {
            empty_post_view.visibility = View.VISIBLE
        }
        else {
            empty_post_view.visibility = View.INVISIBLE
        }
    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        homeViewModel =
                ViewModelProvider(this).get(HomeViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_home, container, false)
//        val textView: TextView = root.findViewById(R.id.text_home)
//        homeViewModel.text.observe(viewLifecycleOwner, Observer {
//            textView.text = it
//        })
        setHasOptionsMenu(true)
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initAuth()


        postsRV.layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        postsAdapter = PostsAdapter(viewModel, ::viewPost, ::editDeleteAlert)

        postsRV.adapter = postsAdapter

        viewModel.observePosts().observe(viewLifecycleOwner, Observer {
            toggleEmptyPosts()
            postsAdapter.clearAll()

            postsAdapter.addAll(it)
            postsAdapter.notifyDataSetChanged()
        })

        // set initial state
        toggleEmptyPosts()





    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.main_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId) {
            R.id.follow -> {
                viewModel.updateFetchStatus(Constants.FETCH_FOLLOW)
                viewModel.resetFollowingList()
                viewModel.observeFollowing().observe(viewLifecycleOwner, Observer {
                    Log.d(javaClass.simpleName, "following list is changed size is ${viewModel.observeFollowing().value!!.size}")
                    if (it.size != 0) {
                        viewModel.fetchPosts(viewModel.observeFetchStatus().value!!, "")
                    }

                })
                viewModel.fetchFollowing(viewModel.observeUser().value!!.uid)
                true
            }
            R.id.trending -> {
                viewModel.updateFetchStatus(Constants.FETCH_TRENDING)
                viewModel.fetchPosts(viewModel.observeFetchStatus().value!!, "")
                true
            }
            R.id.nearby -> {
                true
            }
            R.id.search -> {
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun viewPost(position: Int) {
        val action = HomeFragmentDirections.actionNavigationHomeToNavigationOnePost(position, "Post")
        findNavController().navigate(action)
    }

    private fun editPost(position: Int) {
        if (permission(position)) {
            val action = HomeFragmentDirections.actionNavigationHomeToNavigationNewPost(position, "Edit")
            findNavController().navigate(action)
        }


    }

    private fun deletePost(position: Int) {
        if (permission(position)) {
            viewModel.removePostAt(position)
            val user = viewModel.observeUser().value
            if (user != null) {
                var tempList = user.postsList.toMutableList()
                tempList.remove(viewModel.getPost(position).postID)
                user.postsList = tempList
                viewModel.updateUser(user)
            }



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