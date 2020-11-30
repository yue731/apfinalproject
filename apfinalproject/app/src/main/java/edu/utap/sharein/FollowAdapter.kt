package edu.utap.sharein

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.RecyclerView
import edu.utap.sharein.model.Follow
import edu.utap.sharein.model.Post
import edu.utap.sharein.model.User

class FollowAdapter(private val viewModel: MainViewModel, private val follow: Int, private val viewMe: (Int, String) -> Unit)
    : RecyclerView.Adapter<FollowAdapter.VH>() {
    private var followingList = mutableListOf<Follow>()
    private var followerList = mutableListOf<Follow>()

    inner class VH(itemView: View): RecyclerView.ViewHolder(itemView) {
        private var followUserProfile: ImageView = itemView.findViewById(R.id.followUserProfileIV)
        private var followUserName: TextView = itemView.findViewById(R.id.followUserName)
        private var followUserContainer: ConstraintLayout = itemView.findViewById(R.id.followUserContainer)

        init {
            followUserContainer.setOnClickListener {
                if (follow == Constants.FOLLOWING) {
                    viewMe(adapterPosition, followingList[adapterPosition].following)
                }
                else {
                    viewMe(adapterPosition, followerList[adapterPosition].follower)
                }

            }
        }

        fun bind(f: Follow) {
            if (follow == Constants.FOLLOWING) {
                viewModel.fetchOwner(followUserProfile, f.following)
                viewModel.fetchUserName(f.following, followUserName)
            }
            else {
                viewModel.fetchOwner(followUserProfile, f.follower)
                viewModel.fetchUserName(f.follower, followUserName)
            }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.follow_user_list_row, parent, false)
        return VH(itemView)
    }

    override fun getItemCount(): Int {
        if (follow == Constants.FOLLOWING) {
            return followingList.size
        }
        else {
            return followerList.size
        }
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        if (follow == Constants.FOLLOWING) {
            holder.bind(followingList[holder.adapterPosition])
        }
        else {
            holder.bind(followerList[holder.adapterPosition])
        }
    }

    fun addAll(mode: Int, items: List<Follow>) {
        if (mode == Constants.FOLLOWING) {
            followingList.addAll(items)
        }
        else {
            followerList.addAll(items)
        }
    }

    fun clearAll(mode: Int) {
        if (mode == Constants.FOLLOWING) {
            followingList.clear()
        }
        else {
            followerList.clear()
        }
    }
}
