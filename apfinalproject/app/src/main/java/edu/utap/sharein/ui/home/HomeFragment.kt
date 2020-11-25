package edu.utap.sharein.ui.home

import android.os.Bundle
import android.view.*
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.google.firebase.auth.FirebaseUser
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

    private fun toggleEmptyNotes() {
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
        postsAdapter = PostsAdapter(viewModel) {
            //  click on the post view the post
            val action = HomeFragmentDirections.actionNavigationHomeToNavigationOnePost(it, "Post")
            findNavController().navigate(action)

        }

        postsRV.adapter = postsAdapter

        viewModel.observePosts().observe(viewLifecycleOwner, Observer {
            toggleEmptyNotes()
            postsAdapter.submitList(it)
        })

        // set initial state
        toggleEmptyNotes()





    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.main_menu, menu)
    }


}