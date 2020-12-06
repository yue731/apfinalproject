package edu.utap.sharein.ui.messagepreview

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import edu.utap.sharein.MainViewModel
import edu.utap.sharein.MessagePreviewAdapter
import edu.utap.sharein.R
import edu.utap.sharein.ViewModelDBHelper

class MessagePreviewFragment: Fragment() {

    private lateinit var messagePreviewViewModel: MessagePreviewViewModel
    private val viewModel: MainViewModel by activityViewModels()
    private lateinit var messagePreviewRV: RecyclerView
    private lateinit var messagePreviewAdapter: MessagePreviewAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        messagePreviewViewModel = ViewModelProvider(this).get(MessagePreviewViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_message_preview, container, false)
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        messagePreviewRV = view.findViewById(R.id.messagePreviewRV)
        messagePreviewAdapter = MessagePreviewAdapter(viewModel, ::viewMessage)
        messagePreviewRV.layoutManager = LinearLayoutManager(context)
        messagePreviewRV.adapter = messagePreviewAdapter
        viewModel.resetLastMessagesReceivedList()
        viewModel.resetMessagesReceivedListPreview()
        viewModel.observeLastMessagesReceivedList().observe(viewLifecycleOwner, Observer {
            messagePreviewAdapter.clearAll()
            messagePreviewAdapter.addAll(it)
            messagePreviewAdapter.notifyDataSetChanged()
        })

        viewModel.fetchLastReceivedMessages()

    }

    private fun viewMessage(senderUID: String, position: Int) {
        val action = MessagePreviewFragmentDirections.actionNavigationMessagePreviewToNavigationMessage(senderUID, position)
        viewModel.sortAllMessages()
        findNavController().navigate(action)
    }

}