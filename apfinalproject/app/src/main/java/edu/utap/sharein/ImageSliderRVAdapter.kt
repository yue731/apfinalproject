package edu.utap.sharein

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import edu.utap.sharein.model.Post

class ImageSliderRVAdapter(private val viewModel: MainViewModel, private val post: Post):
        RecyclerView.Adapter<ImageSliderRVAdapter.VH>() {

    inner class VH(itemView: View): RecyclerView.ViewHolder(itemView) {
        private var imageIS = itemView.findViewById<ImageView>(R.id.imageIS)
        fun bind(position: Int) {
            val images = post.pictureUUIDs
            viewModel.glideFetch(images[position], imageIS)

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.image_slider, parent, false)
        return VH(itemView)
    }

    override fun getItemCount(): Int {
        return post.pictureUUIDs.size
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
       holder.bind(holder.adapterPosition)
    }


}