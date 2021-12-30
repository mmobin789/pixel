package io.pixel.loader.load.type

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.widget.ImageView
import io.pixel.config.PixelLog
import io.pixel.config.PixelOptions
import io.pixel.loader.cache.disk.BitmapDiskCache
import io.pixel.loader.load.LoadAdapter
import io.pixel.loader.load.ViewLoad
import io.pixel.loader.load.request.FileLoadRequest
import io.pixel.loader.load.request.download.ImageDownloadRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.LinkedHashMap
import java.util.Collections
import io.pixel.loader.load.LoadAdapter.addLoad

internal abstract class ImageLoad(
    private val viewLoad: ViewLoad,
    private val imageView: ImageView,
    private val pixelOptions: PixelOptions?,
    private val coroutineScope: CoroutineScope
) {

    abstract suspend fun start()

    private val id = viewLoad.hashCode()

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

    private fun setImageSize() = pixelOptions?.run {
        val sampleWidth = getRequestedImageWidth()
        val sampleHeight = getRequestedImageHeight()

        if (sampleWidth > 0 && sampleHeight > 0) {

            PixelLog.debug(
                this@ImageLoad.javaClass.simpleName,
                "Sample Bitmap load from ${viewLoad.width}x${viewLoad.height} to ${sampleWidth}x$sampleHeight"
            )

            viewLoad.width = sampleWidth
            viewLoad.height = sampleHeight
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

    protected suspend fun loadFromInternet() {

            setPlaceholder()

        withContext(Dispatchers.IO) {
            val tag = "InternetImageLoad"
            prepareBitmapCache(context = imageView.context)

            setImageSize()

            LoadAdapter.loadImageFromMemory(id)?.apply {
                PixelLog.debug(
                    tag,
                    "Returned Memory Cached Bitmap whose size is ${byteCount / 1024} Kilobytes"
                )
                setImage(this)
            } ?: LoadAdapter.loadImageFromDisk(id)?.apply {
                PixelLog.debug(
                    tag,
                    "Returned Disk Cached Bitmap whose size is ${byteCount / 1024} Kilobytes"
                )
                setImage(this)
            } ?: run {
                val imageDownloadRequest = ImageDownloadRequest(viewLoad, coroutineScope, pixelOptions) { setImage(it) }
                imageDownloadRequest.start()
               addLoad(imageDownloadRequest)
            }
        }
    }

    protected suspend fun loadFromFile() {

            setPlaceholder()

        withContext(Dispatchers.IO) {
            val tag = "FileImageLoad"
            setImageSize()

            LoadAdapter.loadImageFromMemory(id)?.apply {
                PixelLog.debug(
                    tag,
                    "Returned Memory Cached Bitmap whose size is ${byteCount / 1024} Kilobytes"
                )
                setImage(this)
            } ?: LoadAdapter.loadImageFromDisk(id)?.apply {
                PixelLog.debug(
                    tag,
                    "Returned Disk Cached Bitmap whose size is ${byteCount / 1024} Kilobytes"
                )
                setImage(this)
            } ?: run {

                val fileLoadRequest =
                    FileLoadRequest(imageView.context, viewLoad, coroutineScope, pixelOptions) {
                        setImage(it)
                    }
                fileLoadRequest.start()
                addLoad(fileLoadRequest)
            }
        }
    }

   private companion object {
        /** Weak Ref Map
         * Key - Image view
         * Values - id for image.
         */
        val imageViewsMap = Collections.synchronizedMap(LinkedHashMap<ImageView,Int>(0,0.75f))
        val transparentColorDrawable = ColorDrawable(Color.TRANSPARENT)

        private fun prepareBitmapCache(context: Context) = BitmapDiskCache.prepare(context)
    }
}
