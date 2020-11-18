package edu.utap.sharein.glide

import android.content.Context
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.Registry
import com.bumptech.glide.annotation.GlideModule
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.module.AppGlideModule
import com.bumptech.glide.request.RequestOptions
import com.firebase.ui.storage.images.FirebaseImageLoader
import com.google.firebase.storage.StorageReference
import edu.utap.sharein.R
import java.io.InputStream

@GlideModule
class AppGlideModule: AppGlideModule() {
    override fun registerComponents(context: Context, glide: Glide, registry: Registry) {
        // register FireBaseImageLoader to handle StorageReference
        registry.append(
            StorageReference::class.java, InputStream::class.java,
            FirebaseImageLoader.Factory()
        )
    }
}

object Glide {
    private val width = 500
    private val height = 500
    private var glideOptions = RequestOptions()
        .fitCenter()
        .transform(RoundedCorners(20))

    fun fetch(storageReference: StorageReference, imageView: ImageView) {
        GlideApp.with(imageView.context)
            .asBitmap()
            .load(storageReference)
            .apply(glideOptions)
            .error(R.color.colorAccent)
            .override(width, height)
            .into(imageView)

    }

}