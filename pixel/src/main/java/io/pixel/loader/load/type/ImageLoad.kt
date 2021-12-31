package io.pixel.loader.load.type

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.widget.ImageView
import io.pixel.config.PixelLog
import io.pixel.config.PixelOptions
import io.pixel.loader.cache.disk.BitmapDiskCache
import io.pixel.loader.load.LoadAdapter
import io.pixel.loader.load.LoadAdapter.addLoad
import io.pixel.loader.load.ViewLoad
import io.pixel.loader.load.request.FileLoadRequest
import io.pixel.loader.load.request.ImageLoadRequest
import io.pixel.loader.load.request.download.ImageDownloadRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

internal abstract class ImageLoad(
    private val viewLoad: ViewLoad,
    private val imageView: ImageView,
    private val pixelOptions: PixelOptions?,
    private val coroutineScope: CoroutineScope
) {

    private val id = viewLoad.hashCode()

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

    private suspend fun setImage(imageLoadRequest: ImageLoadRequest) {
        val bitmap = imageLoadRequest.bitmap()
        withContext(Dispatchers.Main.immediate) {
            if (addLoad(imageLoadRequest) && bitmap != null) {
                imageView.setImageBitmap(bitmap)
            } else {
                setPlaceholder()
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
                setImage(CachedImageLoadRequest(this, coroutineScope, id))
            } ?: LoadAdapter.loadImageFromDisk(id)?.apply {
                PixelLog.debug(
                    tag,
                    "Returned Disk Cached Bitmap whose size is ${byteCount / 1024} Kilobytes"
                )
                setImage(CachedImageLoadRequest(this, coroutineScope, id))
            } ?: run {
                val imageDownloadRequest =
                    ImageDownloadRequest(viewLoad, coroutineScope, pixelOptions)
                setImage(imageDownloadRequest)
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
                setImage(CachedImageLoadRequest(this, coroutineScope, id))
            } ?: LoadAdapter.loadImageFromDisk(id)?.apply {
                PixelLog.debug(
                    tag,
                    "Returned Disk Cached Bitmap whose size is ${byteCount / 1024} Kilobytes"
                )
                setImage(CachedImageLoadRequest(this, coroutineScope, id))
            } ?: run {
                val fileLoadRequest =
                    FileLoadRequest(imageView.context, viewLoad, coroutineScope, pixelOptions)
                setImage(fileLoadRequest)
            }
        }
    }

    private companion object {
        val transparentColorDrawable = ColorDrawable(Color.TRANSPARENT)
        private fun prepareBitmapCache(context: Context) = BitmapDiskCache.prepare(context)
    }
}
