package edu.utap.sharein

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.replace
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class FollowFragment: Fragment(R.layout.fragment_follow) {
    private val viewModel: MainViewModel by activityViewModels()
    private lateinit var followAdapter: FollowAdapter
    private val args: FollowFragmentArgs by navArgs()
    private lateinit var followRV: RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_follow, container, false)

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        followRV = view.findViewById<RecyclerView>(R.id.followRV)
//        val itemDecor = DividerItemDecoration(followRV.context, LinearLayoutManager.VERTICAL)
//        itemDecor.setDrawable(ContextCompat.getDrawable(followRV.context, (R.drawable.divider))!!)
//        followRV.addItemDecoration(itemDecor)
        followAdapter = FollowAdapter(viewModel, args.mode, ::viewMe)
        followRV.layoutManager = LinearLayoutManager(context)
        followRV.adapter = followAdapter
        if (args.mode == Constants.FOLLOWING) {
            viewModel.observeFollowing().observe(viewLifecycleOwner, Observer {
                followAdapter.clearAll(args.mode)
                followAdapter.addAll(args.mode, it)
                followAdapter.notifyDataSetChanged()
            })
        }
        else {
            viewModel.observeFollower().observe(viewLifecycleOwner, Observer {
                followAdapter.clearAll(args.mode)
                followAdapter.addAll(args.mode, it)
                followAdapter.notifyDataSetChanged()
            })
        }



    }

    private fun viewMe(position: Int, uid: String) {


        val action = FollowFragmentDirections.actionNavigationFollowToNavigationMe(position, "", uid)
        viewModel.pushUser()

        findNavController().navigate(action)

    }

    override fun onResume() {
        Log.d(javaClass.simpleName, "resume")



        super.onResume()
    }

    override fun onStop() {
        Log.d(javaClass.simpleName, "stop")
        super.onStop()
    }

    override fun onPause() {
        Log.d(javaClass.simpleName, "pause")


        super.onPause()
    }

    override fun onDestroy() {
        Log.d(javaClass.simpleName, "destroy")
        super.onDestroy()
    }

    override fun onDestroyView() {
        Log.d(javaClass.simpleName, "destroy view")

        super.onDestroyView()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        Log.d(javaClass.simpleName, "on activity created")
        super.onActivityCreated(savedInstanceState)
    }


}