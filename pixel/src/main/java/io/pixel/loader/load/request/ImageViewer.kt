package io.pixel.loader.load.request

import android.graphics.Bitmap
import android.widget.ImageView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

internal object ImageViewer {

    suspend fun setCachedImage(bitmap: Bitmap, imageView: ImageView) =
        withContext(Dispatchers.Main.immediate) {
            imageView.setImageBitmap(bitmap)
        }

    suspend fun setLoadedImage(imageLoadRequest: ImageLoadRequest, imageView: ImageView) {
        val bitmap = imageLoadRequest.bitmap()

        withContext(Dispatchers.Main.immediate) {
            imageView.setImageBitmap(bitmap)
        }
    }
}
