package edu.utap.sharein

import android.os.Bundle
import android.view.*
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager


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
//        postsAdapter = PostsAdapter(viewModel) {
//            val action =
//        }

    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.me_menu_top, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.mePosts -> {
                true
            }
            R.id.meLiked -> {
                true
            }
            else -> super.onOptionsItemSelected(item)
        }

    }
}