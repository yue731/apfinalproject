package edu.utap.sharein.ui.home

import android.app.AlertDialog
import android.graphics.Color
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.TextUtils
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.*
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
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
import edu.utap.sharein.model.Post
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
            if(viewModel.observeUser().value != null) {
                toggleEmptyPosts()
                postsAdapter.clearAll()

                postsAdapter.addAll(it)
                postsAdapter.notifyDataSetChanged()
            }
        })
        viewModel.observeUser().observe(viewLifecycleOwner, Observer {
            if (it != null) {
                viewModel.setCurrPageUser(it)
                if (viewModel.observePosts().value != null) {
                    toggleEmptyPosts()
                    postsAdapter.clearAll()

                    postsAdapter.addAll(viewModel.observePosts().value!!)
                    postsAdapter.notifyDataSetChanged()
                }
            }
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
                    else {
                        viewModel.resetPosts()
                    }

                })
                viewModel.fetchFollowing(viewModel.observeUser().value!!.uid)
                true
            }
            R.id.all -> {
                viewModel.updateFetchStatus(Constants.FETCH_ALL)

                viewModel.fetchPosts(viewModel.observeFetchStatus().value!!, "")
                true
            }
            R.id.trend -> {
                viewModel.updateFetchStatus(Constants.FETCH_TREND)
                viewModel.fetchPosts(viewModel.observeFetchStatus().value!!, "")
                true
            }
            R.id.search -> {
                val searchPopUpView = LayoutInflater.from(requireContext()).inflate(R.layout.search_pop_up, null)
                val dialogBuilder = AlertDialog.Builder(requireContext())
                dialogBuilder.setCancelable(false)
                    .setView(searchPopUpView)
                val alert = dialogBuilder.create()
                alert.show()
                val searchOKBut = searchPopUpView.findViewById<Button>(R.id.searchOKBut)
                val searchCancelBut = searchPopUpView.findViewById<Button>(R.id.searchCancelBut)
                val searchET = searchPopUpView.findViewById<EditText>(R.id.searchET)
                searchET.requestFocus()
                searchOKBut.setOnClickListener {
                    if (TextUtils.isEmpty(searchET.text.toString())) {
                        Toast.makeText(activity, "Enter search content!", Toast.LENGTH_LONG).show()
                    }
                    else {
                        performSearch(searchET.text.toString())
                        alert.cancel()
                    }
                }
                searchCancelBut.setOnClickListener {
                    alert.cancel()
                }
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

    private fun performSearch(searchTerm: String) {
        var newList = mutableListOf<Post>()
        if (viewModel.observePosts().value != null) {
            for (post in viewModel.observePosts().value!!) {
                if (post.title.contains(searchTerm, ignoreCase = true) || post.text.contains(searchTerm, ignoreCase = true)) {
                    newList.add(post)
                }
            }
            viewModel.updatePostsList(newList.toList())
        }


    }



    override fun onDestroy() {
        Log.d(javaClass.simpleName, " destroy")
        super.onDestroy()
    }


}