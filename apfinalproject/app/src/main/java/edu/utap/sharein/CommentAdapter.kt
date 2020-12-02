package edu.utap.sharein

import android.text.method.ScrollingMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.recyclerview.widget.RecyclerView
import edu.utap.sharein.model.Comment
import java.text.DateFormat
import java.text.SimpleDateFormat

class CommentAdapter(private val viewModel: MainViewModel, private val deleteComment: (Int) ->Unit)
    : RecyclerView.Adapter<CommentAdapter.VH>() {
    private var commentsList = mutableListOf<Comment>()

    private val dateFormat: DateFormat =
        SimpleDateFormat("MM-dd-yyyy HH:mm:ss")

    inner class VH(itemView: View): RecyclerView.ViewHolder(itemView) {
        private var commentProfilePhotoIV = itemView.findViewById<ImageView>(R.id.commentProfilePhotoIV)
        private var commentUserNameTV = itemView.findViewById<TextView>(R.id.commentUserNameTV)
        private var commentTextTV = itemView.findViewById<TextView>(R.id.commentTextTV)
        private var commentTimeStampTV = itemView.findViewById<TextView>(R.id.commentTimeStampTV)
        private var commentContainer = itemView.findViewById<ConstraintLayout>(R.id.commentContainer)

        init {
            commentContainer.setOnLongClickListener {
                deleteComment(adapterPosition)
                true
            }
        }
        fun bind(comment: Comment) {
            viewModel.fetchOwner(commentProfilePhotoIV, comment.userUID)
            commentUserNameTV.text = comment.userName
            commentTextTV.text = comment.text
            commentTextTV.movementMethod = ScrollingMovementMethod()
            comment.timeStamp?.let {
                commentTimeStampTV.text = dateFormat.format(it.toDate())
            }

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.comment_row, parent, false)
        return VH(itemView)
    }

    override fun getItemCount(): Int {
        return commentsList.size
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(commentsList[holder.adapterPosition])
    }

    fun addAll(items: List<Comment>) {
        commentsList.addAll((items))
    }

    fun clearAll() {
        commentsList.clear()
    }
}