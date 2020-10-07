package io.pixel.android.loader.load

import io.pixel.android.config.PixelLog
import io.pixel.android.config.PixelOptions
import io.pixel.android.loader.cache.disk.BitmapDiskCache
import io.pixel.android.loader.cache.memory.BitmapMemoryCache
import io.pixel.android.loader.download.Downloader.getBitmapFromURL
import io.pixel.android.loader.download.ImageDownload

internal object LoadAdapter {
    private val imageDownloads = HashMap<Int, ImageDownload>(100)

    @Synchronized
    fun addDownload(imageDownload: ImageDownload) {
        val id = imageDownload.id
        if (!imageDownloads.containsKey(id))
            imageDownloads[id] = imageDownload
        else cancelImageDownload(id)
    }


    private fun cancelImageDownload(
        id: Int,
        removeFromCache: Boolean = false
    ) {

        imageDownloads[id]?.apply {
            cancel()
            removeImageDownload(
                this.id,
                removeFromCache
            )
        }

    }


    fun loadImageFromMemory(viewLoadCode: Int) = BitmapMemoryCache.get(viewLoadCode)

    fun loadImageFromDisk(viewLoadCode: Int) = BitmapDiskCache.get(viewLoadCode)


    fun downloadImage(
        url: String,
        reqWidth: Int,
        reqHeight: Int,
        viewLoadCode: Int,
        pixelOptions: PixelOptions?
    ) =
        getBitmapFromURL(url, reqWidth, reqHeight, pixelOptions)?.also {
            PixelLog.debug(
                this@LoadAdapter.javaClass.simpleName,
                "Downloaded no = $viewLoadCode Bitmap for ${it.width}x${it.height} size in Kilobytes: ${it.byteCount / 1024}"
            )

            ViewLoad(reqWidth, reqHeight, url).run {
                BitmapMemoryCache.put(
                    hashCode(), it
                )

                BitmapDiskCache.put(
                    this, it,
                    pixelOptions?.getImageFormat() ?: PixelOptions.ImageFormat.PNG
                )
            }
        }

    private fun removeImageDownload(id: Int, removeFromCache: Boolean) {
        imageDownloads.remove(id)?.also {
            if (removeFromCache) {
                BitmapMemoryCache.clear(id)
                BitmapDiskCache.clear(id.toString())
            }
        }
    }
}