package edu.utap.sharein.ui.newpost


import android.os.Bundle
import android.text.TextUtils
import android.text.method.ScrollingMovementMethod
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import edu.utap.sharein.*
import kotlinx.android.synthetic.main.fragment_new_post.*

class NewPostFragment : Fragment() {

    private lateinit var newPostViewModel: NewPostViewModel
    private lateinit var imageAdapter: ImageAdapter
    private lateinit var pictureUUIDs: List<String>
    private lateinit var musicUUID: String
    private lateinit var profilePhotoUUID: String
    private var position = -1
    private val viewModel: MainViewModel by activityViewModels()
    private val args: NewPostFragmentArgs by navArgs()



    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        newPostViewModel =
                ViewModelProvider(this).get(NewPostViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_new_post, container, false)
//        val textView: TextView = root.findViewById(R.id.text_dashboard)
//        dashboardViewModel.text.observe(viewLifecycleOwner, Observer {
//            textView.text = it
//        })
        setHasOptionsMenu(true)
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        position = args.position
        if (position == -1) {
            // new post
            pictureUUIDs = listOf()
            // XXX update music info
            musicUUID = ""
            profilePhotoUUID = viewModel.observeUser().value?.profilePhotoUUID ?: ""
        }
        else {
            // edit post
            val post = viewModel.getPost(position)
            enterTitleET.text.insert(0, post.title)
            enterPostET.text.insert(0, post.text)
            enterPostET.movementMethod = ScrollingMovementMethod()
            pictureUUIDs = post.pictureUUIDs
            // XXX update music info
            musicUUID = ""
            profilePhotoUUID = viewModel.observeUser().value?.profilePhotoUUID ?: ""

        }
        enterTitleET.requestFocus()

        photosRV.layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        imageAdapter = ImageAdapter(viewModel) {
            // long click to remove image
            val shorterList = pictureUUIDs.toMutableList()
            val removedPictureUUID: String = shorterList.removeAt(it)
            viewModel.deleteImage(removedPictureUUID)
            pictureUUIDs = shorterList
            imageAdapter.submitList(pictureUUIDs)

        }
        photosRV.adapter = imageAdapter
        imageAdapter.submitList(pictureUUIDs)




    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.new_post_menu, menu)


    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.postBut -> {
                // XXX post it
                if (TextUtils.isEmpty(enterTitleET.text.toString())) {
                    Toast.makeText(activity, "Enter title!", Toast.LENGTH_LONG).show()
                }
                else {
                    if (position == -1) {
                        // new post
                        val postID = viewModel.createPost(enterTitleET.text.toString(), enterPostET.text.toString(), pictureUUIDs, musicUUID)
                        Log.d(javaClass.simpleName, "fetch status is ${viewModel.observeFetchStatus().value} ")
                        val user = viewModel.observeUser().value
                        if (user != null) {
                            Log.d(javaClass.simpleName, "when post, user is not null")
                            var tempList = user.postsList.toMutableList()
                            tempList.add(postID)
                            user.postsList = tempList
                            Log.d(javaClass.simpleName, "the postslist is ${user.postsList}")
                            viewModel.updateUser(user)
                        }

                    }
                    else {
                        // edit post
                        viewModel.updatePost(position, enterTitleET.text.toString(), enterPostET.text.toString(), pictureUUIDs, musicUUID)

                    }
                    (activity as MainActivity?)?.hideKeyboard()
                    val status = viewModel.observeFetchStatus().value
                    if (status == Constants.FETCH_FOLLOW || status == Constants.FETCH_TRENDING || status == Constants.FETCH_NEARBY) {
                        val action = NewPostFragmentDirections.actionNavigationNewPostToNavigationHome()
                        findNavController().navigate(action)
                    }
                    else {
                        val action = NewPostFragmentDirections.actionNavigationNewPostToNavigationMe(-1, "Me", viewModel.observeUser().value!!.uid)
                        findNavController().navigate(action)
                    }
                }


                true
            }
            R.id.cameraBut -> {
                viewModel.takePhoto(::pictureSuccess)
                true
            }
            R.id.cancelBut -> {
                (activity as MainActivity?)?.hideKeyboard()
                val status = viewModel.observeFetchStatus().value
                if (status == Constants.FETCH_FOLLOW || status == Constants.FETCH_TRENDING || status == Constants.FETCH_NEARBY) {
                    val action = NewPostFragmentDirections.actionNavigationNewPostToNavigationHome()
                    findNavController().navigate(action)
                }
                else {
                    val action = NewPostFragmentDirections.actionNavigationNewPostToNavigationMe(-1, "Me", viewModel.observeUser().value!!.uid)
                    findNavController().navigate(action)
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun pictureSuccess(pictureUUID: String) {
        pictureUUIDs.toMutableList().apply {
            add(pictureUUID)
            Log.d(javaClass.simpleName, "photo added $pictureUUID len ${this.size}")
            pictureUUIDs = this
            imageAdapter.submitList(pictureUUIDs)
        }
    }


}