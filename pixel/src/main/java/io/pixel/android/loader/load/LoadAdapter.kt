package io.pixel.android.loader.load

import io.pixel.android.config.PixelLog
import io.pixel.android.loader.cache.memory.BitmapMemoryCache
import io.pixel.android.loader.download.ImageDownload
import io.pixel.android.utils.DownloadUtils.getBitmapFromURL

internal object LoadAdapter {
    private val bitmapMemoryCache = BitmapMemoryCache.getInstance()
    private val imageLoads = linkedMapOf<Int, ImageDownload>()

    fun addDownload(imageDownload: ImageDownload) {
        imageLoads.put(imageDownload.id, imageDownload)?.also {
            cancelImageDownload(
                it.id
            )
        }
    }


    private fun cancelImageDownload(
        id: Int,
        removeFromCache: Boolean = false
    ): Boolean {

        val imageLoad = imageLoads[id]?.apply {
            cancel()
            removeImageDownload(
                this.id,
                removeFromCache
            )
        }

        return imageLoad == null || imageLoad.isCancelled()
    }


    fun loadImageFromCache(viewLoadCode: Int) = bitmapMemoryCache.get(viewLoadCode)


    fun downloadImage(url: String, reqWidth: Int, reqHeight: Int, viewLoadCode: Int) =
        getBitmapFromURL(url, reqWidth, reqHeight)?.also {
            PixelLog.debug(
                this@LoadAdapter.javaClass.simpleName,
                "Downloaded no = $viewLoadCode Bitmap for ${it.width}x${it.height} size in Kilobytes: ${it.byteCount / 1024}"
            )
            bitmapMemoryCache.put(
                ViewLoad(
                    reqWidth,
                    reqHeight,
                    url
                ).hashCode(), it
            )
        }

    private fun removeImageDownload(id: Int, removeFromCache: Boolean) {
        imageLoads.remove(id)?.also {
            if (removeFromCache)
                bitmapMemoryCache.clear(id)
        }
    }
}