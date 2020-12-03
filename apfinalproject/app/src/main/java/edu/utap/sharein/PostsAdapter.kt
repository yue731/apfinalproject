package edu.utap.sharein

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout

import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import edu.utap.sharein.model.Post
import java.text.DateFormat
import java.text.SimpleDateFormat

class PostsAdapter(private val viewModel: MainViewModel, private val viewPost: (Int) -> Unit, private val editDeleteAlert: (Int) -> Unit)
    : ListAdapter<Post, PostsAdapter.VH>(Diff()) {

    private var postsList = mutableListOf<Post>()



    class Diff: DiffUtil.ItemCallback<Post>() {
        override fun areItemsTheSame(oldItem: Post, newItem: Post): Boolean {
           return oldItem.postID == newItem.postID
        }

        override fun areContentsTheSame(oldItem: Post, newItem: Post): Boolean {
            return oldItem.postID == newItem.postID
                    && oldItem.name == newItem.name
                    && oldItem.ownerUid == newItem.ownerUid
                    && oldItem.title == newItem.title
                    && oldItem.text == newItem.text
                    && oldItem.pictureUUIDs == newItem.pictureUUIDs
                    && oldItem.timeStamp == newItem.timeStamp
                    && oldItem.musicRawID == newItem.musicRawID
                    && oldItem.address == newItem.address

        }

    }

    private val dateFormat: DateFormat =
            SimpleDateFormat("MM-dd-yyyy")

    inner class VH(view: View): RecyclerView.ViewHolder(view) {

        private var pic1IV: ImageView = view.findViewById(R.id.pic1IV)
        private var timestamp: TextView = view.findViewById(R.id.timestamp)
        private var title: TextView = view.findViewById(R.id.postTitleTV)
        private var userPhotoIVSmall: ImageView = view.findViewById(R.id.userPhotoIVSmall)
        private var userNameSmall: TextView = view.findViewById(R.id.userNameSmall)
        private var likesCount: TextView = view.findViewById(R.id.likesCount)
        private var likeIcon: ImageView = view.findViewById(R.id.likeIcon)
        private var postRowContainer: ConstraintLayout = view.findViewById(R.id.postRowContainer)






        private fun bindPic1(imageList: List<String>) {
            if (imageList.isNotEmpty()) {
                viewModel.glideFetch(imageList[0], pic1IV)
            }
            else {
                pic1IV.setImageDrawable(ColorDrawable(Color.TRANSPARENT))
            }
        }
        init {
            postRowContainer.setOnClickListener {
                viewPost(adapterPosition)
            }
            postRowContainer.setOnLongClickListener {
                // edit or delete alert

                editDeleteAlert(adapterPosition)


                true
            }
        }
        fun bind(post: Post) {
            Log.d(javaClass.simpleName, "trying to bind post for ${post.ownerUid}")
            bindPic1(post.pictureUUIDs)
            post.timeStamp?.let {
                timestamp.text = dateFormat.format(it.toDate())
            }
            title.text = post.title
            // XXX to write user profile photo
            val ownerUID = post.ownerUid
            viewModel.fetchOwner(userPhotoIVSmall, ownerUID) // fetch post owner and bind profile photo to image view



            userNameSmall.text = post.name
            // XXX bind likes

            viewModel.fetchOnePostLikes(post.postID, likesCount)
            viewModel.fetchUserLikedPostsAndBind(viewModel.observeUser().value!!.uid, post, likeIcon)








        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.post_list_row, parent, false)

        return VH(itemView)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(postsList[holder.adapterPosition])
    }

    override fun getItemCount(): Int {
        return postsList.size
    }

    fun addAll(items: List<Post>) {
        postsList.addAll(items)
    }

    fun clearAll() {
        postsList.clear()
    }





}