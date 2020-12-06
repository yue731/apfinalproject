package edu.utap.sharein

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.view.menu.ActionMenuItemView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import edu.utap.sharein.model.Message
import java.text.DateFormat
import java.text.SimpleDateFormat

class MessagePreviewAdapter(private val viewModel: MainViewModel, private val viewMessage: (String, Int) -> Unit)
    : RecyclerView.Adapter<MessagePreviewAdapter.VH>() {

    private var lastMessagesReceivedList = mutableListOf<Message>()
    companion object {
        private val dateFormat: DateFormat =
            SimpleDateFormat("MM-dd-yyyy HH:mm:ss")
    }

    inner class VH(itemView: View): RecyclerView.ViewHolder(itemView) {
        private var messagePreviewProfilePhoto = itemView.findViewById<ImageView>(R.id.messagePreviewProfilePhoto)
        private var messagePreviewName = itemView.findViewById<TextView>(R.id.messagePreviewName)
        private var messagePreviewMessage = itemView.findViewById<TextView>(R.id.messagePreviewMessage)
        private var messagePreviewTime = itemView.findViewById<TextView>(R.id.messagePreviewTime)
        private var messagePreviewContainer = itemView.findViewById<ConstraintLayout>(R.id.messagePreviewContainer)

        init {
            messagePreviewContainer.setOnClickListener {
                viewMessage(lastMessagesReceivedList[adapterPosition].senderUID, adapterPosition)
            }
        }

        fun bind(m: Message) {
            viewModel.fetchOwner(messagePreviewProfilePhoto, m.senderUID)
            messagePreviewName.text = m.senderName
            messagePreviewMessage.text = m.messageText
            messagePreviewTime.text = dateFormat.format(m.timeStamp!!.toDate())
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.message_preview_row, parent, false)
        return VH(itemView)
    }

    override fun getItemCount(): Int {
        return lastMessagesReceivedList.size
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        Log.d(javaClass.simpleName, "on bind is called")
        holder.bind(lastMessagesReceivedList[holder.adapterPosition])
    }

    fun addAll(items: List<Message>) {
        lastMessagesReceivedList.addAll(items)
    }

    fun clearAll() {
        lastMessagesReceivedList.clear()
    }
}