package edu.utap.sharein

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.viewpager.widget.PagerAdapter
import edu.utap.sharein.model.Post

class ImageSliderAdapter(private val viewModel: MainViewModel, private val context: Context, post: Post): PagerAdapter() {

    private val images = post.pictureUUIDs
    private lateinit var layoutInflater: LayoutInflater

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view == `object`
    }

    override fun getCount(): Int {
        return images.size
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        layoutInflater = LayoutInflater.from(context)
        var view = layoutInflater.inflate(R.layout.image_slider, container, false)
        var imageIS = view.findViewById<ImageView>(R.id.imageIS)
        viewModel.glideFetch(images[position], imageIS)
        container.addView(imageIS)
        return imageIS

    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        container.removeView(`object` as ConstraintLayout)
    }




}