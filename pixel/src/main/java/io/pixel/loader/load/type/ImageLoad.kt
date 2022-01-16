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
import io.pixel.loader.load.LoadAdapter.addLoad
import io.pixel.loader.load.ViewLoad
import io.pixel.loader.load.request.FileLoadRequest
import io.pixel.loader.load.request.ImageLoadRequest
import io.pixel.loader.load.request.ImageViewer.setCachedImage
import io.pixel.loader.load.request.ImageViewer.setLoadedImage
import io.pixel.loader.load.request.download.ImageDownloadRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext

internal abstract class ImageLoad(
    private val viewLoad: ViewLoad,
    private val imageView: ImageView,
    private val pixelOptions: PixelOptions?,
    private val coroutineScope: CoroutineScope
) {

    private val loadId = viewLoad.hashCode()

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

    protected suspend fun loadFromInternet() {

        setPlaceholder()

        withContext(Dispatchers.IO) {
            val tag = "InternetImageLoad"
            prepareBitmapCache(context = imageView.context)

            setImageSize()

            LoadAdapter.loadImageFromMemory(loadId)?.apply {
                PixelLog.debug(
                    tag,
                    "Returned Memory Cached Bitmap whose size is ${byteCount / 1024} Kilobytes"
                )
                setCachedImage(this, imageView)
                addLoad(object : ImageLoadRequest {
                    override val id: Int
                        get() = loadId

                    override fun bitmap(): Bitmap {
                        return this@apply
                    }

                    override fun isRunning(): Boolean {
                        return isActive
                    }

                    override fun cancel(message: String) {
                        this@withContext.cancel(message)
                    }
                })
            } ?: LoadAdapter.loadImageFromDisk(loadId)?.apply {
                PixelLog.debug(
                    tag,
                    "Returned Disk Cached Bitmap whose size is ${byteCount / 1024} Kilobytes"
                )
                setCachedImage(this, imageView)
                addLoad(object : ImageLoadRequest {
                    override val id: Int
                        get() = loadId

                    override fun bitmap(): Bitmap {
                        return this@apply
                    }

                    override fun isRunning(): Boolean {
                        return isActive
                    }

                    override fun cancel(message: String) {
                        this@withContext.cancel(message)
                    }
                })
            } ?: run {
                val imageDownloadRequest =
                    ImageDownloadRequest(viewLoad, coroutineScope, pixelOptions)

                addLoad(imageDownloadRequest)
                setLoadedImage(imageDownloadRequest, imageView)
            }
        }
    }

    protected suspend fun loadFromFile() {

        setPlaceholder()

        withContext(Dispatchers.IO) {
            val tag = "FileImageLoad"
            setImageSize()

            val inMemoryBitmap = LoadAdapter.loadImageFromMemory(loadId)

            if (inMemoryBitmap != null) {
                PixelLog.debug(
                    tag,
                    "Returned Memory Cached Bitmap whose size is ${inMemoryBitmap.byteCount / 1024} Kilobytes"
                )

                setCachedImage(inMemoryBitmap, imageView)

                addLoad(object : ImageLoadRequest {
                    override val id: Int
                        get() = loadId

                    override fun bitmap(): Bitmap {
                        return inMemoryBitmap
                    }

                    override fun isRunning(): Boolean {
                        return isActive
                    }

                    override fun cancel(message: String) {
                        this@withContext.cancel(message)
                    }
                })
            } else {
                pixelOptions?.run {

                    val context = imageView.context
                    val fileLoadRequest = FileLoadRequest(
                        context,
                        viewLoad,
                        coroutineScope,
                        this
                    )
                    addLoad(fileLoadRequest)
                    setLoadedImage(fileLoadRequest, imageView)
                }
            }
        }
    }

    private companion object {
        val transparentColorDrawable = ColorDrawable(Color.TRANSPARENT)
        fun prepareBitmapCache(context: Context) = BitmapDiskCache.prepare(context)
    }
}
