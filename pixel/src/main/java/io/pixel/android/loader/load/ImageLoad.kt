package io.pixel.android.loader.load

import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.widget.ImageView
import io.pixel.android.config.PixelLog
import io.pixel.android.config.PixelOptions
import io.pixel.android.loader.cache.disk.BitmapDiskCache
import io.pixel.android.loader.download.ImageDownload
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*

internal class ImageLoad(
    private val viewLoad: ViewLoad,
    private val imageView: ImageView,
    private val pixelOptions: PixelOptions?,
    private val coroutineScope: CoroutineScope
) {


    private val id = viewLoad.hashCode()


    companion object {
        /** Weak Ref Map
         * Key - Image view
         * Values - id for image.
         */
        private val imageViewsMap = Collections.synchronizedMap(WeakHashMap<ImageView, Int>(100))
        private val transparentColorDrawable = ColorDrawable(Color.TRANSPARENT)

    }

    private fun imageViewReused(): Boolean {
        val id = imageViewsMap[imageView]
        if (id == null || id != this.id)
            return true
        return false
    }

    private fun setPlaceholder() = pixelOptions?.run {
        imageView.setImageResource(getPlaceholderResource())
    } ?: run {
        imageView.setImageDrawable(transparentColorDrawable)
    }

    fun start() {
        setPlaceholder()
        coroutineScope.launch(Dispatchers.IO) {
            BitmapDiskCache.prepare(imageView.context)

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


            LoadAdapter.loadImageFromMemory(id)?.apply {
                PixelLog.debug(
                    this@ImageLoad.javaClass.simpleName,
                    "Returned Memory Cached Bitmap whose size is ${byteCount / 1024} Kilobytes"
                )
                setImage(this)

            } ?: LoadAdapter.loadImageFromDisk(id)?.run {
                PixelLog.debug(
                    this@ImageLoad.javaClass.simpleName,
                    "Returned Disk Cached Bitmap whose size is ${byteCount / 1024} Kilobytes"
                )
                setImage(this)
            } ?: apply {
                val imageDownload = ImageDownload(viewLoad, coroutineScope, pixelOptions)
                imageDownload.start {
                    setImage(it)
                }
                LoadAdapter.addDownload(imageDownload)

            }

        }
    }

    private fun setImage(bitmap: Bitmap) {
        imageViewsMap[imageView] = id
        coroutineScope.launch(Dispatchers.Main.immediate) {
            if (!imageViewReused()) {
                imageView.setImageBitmap(bitmap)
                PixelLog.debug("ImageViewReused", "No")
            } else {
                PixelLog.debug("ImageViewReused", "Yes")
            }
        }

    }
}