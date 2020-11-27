package edu.utap.sharein

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView


class ImageAdapter(private val viewModel: MainViewModel, private val deleteImage:((Int) -> Unit)? = null):
    ListAdapter<String, ImageAdapter.VH>(Diff()) {
    class Diff: DiffUtil.ItemCallback<String>() {
        override fun areItemsTheSame(oldItem: String, newItem: String): Boolean {
            return oldItem == newItem
        }
        override fun areContentsTheSame(oldItem: String, newItem: String): Boolean {
            return oldItem == newItem
        }
    }

    inner class VH(view: View): RecyclerView.ViewHolder(view) {
        private var imageIB: ImageButton = view.findViewById(R.id.imageIB)
        fun bind(pictureUUID: String, position: Int) {
            viewModel.glideFetch(pictureUUID, imageIB)
            if (deleteImage == null) {
                imageIB.isLongClickable = false
            }
            else {
                imageIB.setOnLongClickListener {
                    deleteImage.invoke(position)
                    return@setOnLongClickListener true
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.image_row, parent, false)
        return VH(itemView)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(currentList[holder.adapterPosition], holder.adapterPosition)
    }
}