package edu.utap.sharein.ui.newpost

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import edu.utap.sharein.R

class NewPostFragment : Fragment() {

    private lateinit var newPostViewModel: NewPostViewModel

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
        return root
    }
}