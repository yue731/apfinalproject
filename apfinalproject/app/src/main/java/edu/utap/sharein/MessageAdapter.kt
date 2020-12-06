package edu.utap.sharein

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import edu.utap.sharein.model.Message
import java.text.DateFormat
import java.text.SimpleDateFormat

class MessageAdapter (private val viewModel: MainViewModel) : RecyclerView.Adapter<MessageAdapter.VH>() {
    private var messagesList = mutableListOf<Message>()
    companion object {
        private val dateFormat: DateFormat =
            SimpleDateFormat("MM-dd-yyyy HH:mm:ss")
    }



    inner class VH(itemView: View): RecyclerView.ViewHolder(itemView) {
        private var senderNameTV = itemView.findViewById<TextView>(R.id.senderNameTV)
        private var senderTimeTV = itemView.findViewById<TextView>(R.id.senderTimeTV)
        private var senderTextTV = itemView.findViewById<TextView>(R.id.senderTextTV)
        private var senderTextCV = itemView.findViewById<CardView>(R.id.senderTextCV)
        private var receiverNameTV = itemView.findViewById<TextView>(R.id.receiverNameTV)
        private var receiverTimeTV = itemView.findViewById<TextView>(R.id.receiverTimeTV)
        private var receiverTextTV = itemView.findViewById<TextView>(R.id.receiverTextTV)
        private var receiverTextCV = itemView.findViewById<CardView>(R.id.receiverTextCV)

        private fun goneElements(nameTV: TextView, timeTV: TextView, textTV: TextView, textCV: CardView) {
            nameTV.visibility = View.GONE
            timeTV.visibility = View.GONE
            textTV.visibility = View.GONE
            textCV.visibility = View.GONE
        }

        private fun visibleElements(nameTV: TextView, timeTV: TextView, textTV: TextView, textCV: CardView) {
            nameTV.visibility = View.VISIBLE
            timeTV.visibility = View.VISIBLE
            textTV.visibility = View.VISIBLE
            textCV.visibility = View.VISIBLE

        }

        private fun bindElements(item: Message, nameTV: TextView, timeTV: TextView, textTV: TextView) {
            nameTV.text = item.senderName
            item.timeStamp?.let {
                timeTV.text = dateFormat.format(it.toDate())
            }
            textTV.text = item.messageText

        }

        fun bind(item: Message?) {
            if (item == null) return
            if (viewModel.observeUser().value!!.uid == item.senderUID) {
                goneElements(receiverNameTV, receiverTimeTV, receiverTextTV, receiverTextCV)
                visibleElements(senderNameTV, senderTimeTV, senderTextTV, senderTextCV)
                bindElements(item, senderNameTV, senderTimeTV, senderTextTV)
            }
            else {
                goneElements(senderNameTV, senderTimeTV, senderTextTV, senderTextCV)
                visibleElements(receiverNameTV, receiverTimeTV, receiverTextTV, receiverTextCV)
                bindElements(item, receiverNameTV, receiverTimeTV, receiverTextTV)
            }
            Log.d(javaClass.simpleName, "bind send message ${item.senderUID}")
        }
    }



    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.message_row, parent, false)
        return VH(itemView)
    }

    override fun getItemCount(): Int {
        return messagesList.size
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        Log.d(javaClass.simpleName, "on bind is called")
        holder.bind(messagesList[holder.adapterPosition])
    }

    fun addAll(items: List<Message>) {
        messagesList.addAll(items)
    }

    fun clearAll() {
        messagesList.clear()
    }
}