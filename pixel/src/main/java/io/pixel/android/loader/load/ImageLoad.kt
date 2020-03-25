package io.pixel.android.loader.load

import android.graphics.Bitmap
import android.widget.ImageView
import io.pixel.android.config.PixelLog
import io.pixel.android.config.PixelOptions
import io.pixel.android.loader.download.ImageDownload
import java.util.*

internal data class ImageLoad(
    private val viewLoad: ViewLoad,
    private val imageView: ImageView,
    private val pixelOptions: PixelOptions?
) {

    val id = viewLoad.hashCode()

    init {
        imageViewsMap[imageView] = id
    }


    companion object {
        private val imageViewsMap = Collections.synchronizedMap(WeakHashMap<ImageView, Int>())

    }

    private fun imageViewReused(): Boolean {
        val id = imageViewsMap[imageView]
        if (id == null || id != this.id)
            return true
        return false
    }

    fun start() {

        pixelOptions?.apply {
            val sampleWidth = getRequestedImageWidth()
            val sampleHeight = getRequestedImageHeight()

            if (sampleWidth > 0 && sampleHeight > 0) {

                PixelLog.debug(
                    this@ImageLoad.javaClass.simpleName,
                    "Sample Bitmap load from ${viewLoad.width}x${viewLoad.height} to ${sampleWidth}x${sampleHeight}"
                )

                viewLoad.width = sampleWidth
                viewLoad.height = sampleHeight
            }
        }

        LoadAdapter.loadImageFromCache(viewLoad.hashCode())?.apply {
            PixelLog.debug(
                this@ImageLoad.javaClass.simpleName,
                "Returned Cached Bitmap whose size is ${byteCount / 1024} Kilobytes"
            )
            setImage(this)

        } ?: apply {
            val imageDownload = ImageDownload(viewLoad)
            LoadAdapter.addDownload(imageDownload)

            imageDownload.onReady {
                setImage(it)
            }

            pixelOptions?.also {
                imageView.setImageResource(it.getPlaceholderResource())
            }
        }
    }


    private fun setImage(bitmap: Bitmap) {

        if (imageViewReused())
            return

        imageView.setImageBitmap(bitmap)
    }
}