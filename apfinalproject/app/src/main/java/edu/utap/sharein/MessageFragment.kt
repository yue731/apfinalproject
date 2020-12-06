package edu.utap.sharein

import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import edu.utap.sharein.model.Message
import edu.utap.sharein.model.User

class MessageFragment: Fragment() {
    private val viewModel: MainViewModel by activityViewModels()
    private lateinit var messageAdapter: MessageAdapter
    private val args: MessageFragmentArgs by navArgs()
    private var user = MutableLiveData<User>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_message, container, false)
        setHasOptionsMenu(true)
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val messageName = view.findViewById<TextView>(R.id.messageName)
        val messageRV = view.findViewById<RecyclerView>(R.id.messageRV)
        val clearMessage = view.findViewById<ImageButton>(R.id.clearMessage)
        val composeMessageET = view.findViewById<EditText>(R.id.composeMessageET)
        val composeSendIB = view.findViewById<ImageButton>(R.id.composeSendIB)

        messageRV.layoutManager = LinearLayoutManager(requireContext())
        messageAdapter = MessageAdapter(viewModel)
        messageRV.adapter = messageAdapter


        val receiverUID = args.receiverUID
        Log.d(javaClass.simpleName, "receiver UID is ${receiverUID}")
        viewModel.resetAllMessages()
        viewModel.resetSentMessages()
        viewModel.resetReceivedMessages()
        user.value = null
        user.observe(viewLifecycleOwner, Observer {
            if (user.value != null) {
                Log.d(javaClass.simpleName, "user is ${user.value!!.name}")
                messageName.text = it.name
            }

            composeSendIB.setOnClickListener {
                Log.d(javaClass.simpleName, "send is clicked")
                if (composeMessageET.text.isEmpty()) {
                    Toast.makeText(activity, "Enter message!", Toast.LENGTH_LONG).show()
                }
                else {
                    viewModel.createMessage(receiverUID, user.value!!.name, composeMessageET.text.toString())
                    composeMessageET.text.clear()
                    (activity as MainActivity?)?.hideKeyboard()
                }
            }


        })
        viewModel.observeAllMessages().observe(viewLifecycleOwner, Observer {
            Log.d(javaClass.simpleName, "all message size is ${it.size}")
            var temp = mutableListOf<Message>()
            temp.addAll((it))
            temp.sortBy {
                it.timeStamp
            }
            messageAdapter.clearAll()
            messageAdapter.addAll(temp)
            messageAdapter.notifyDataSetChanged()
        })
        //viewModel.fetchReceivedMessages(receiverUID)
        viewModel.fetchSentMessages(receiverUID)
        viewModel.fetchReceivedMessages(receiverUID)
        viewModel.fetchOtherUser(receiverUID, user)




        clearMessage.setOnClickListener {
            composeMessageET.text.clear()
        }
        composeMessageET.requestFocus()




    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.message_menu_top, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId) {
            R.id.messageExit -> {
                findNavController().popBackStack()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

}