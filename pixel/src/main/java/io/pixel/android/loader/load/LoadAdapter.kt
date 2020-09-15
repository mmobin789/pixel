package io.pixel.android.loader.load

import android.content.Context
import android.graphics.Bitmap
import io.pixel.android.config.PixelLog
import io.pixel.android.loader.cache.disk.BitmapDiskCache
import io.pixel.android.loader.cache.memory.BitmapMemoryCache
import io.pixel.android.loader.download.ImageDownload
import io.pixel.android.utils.DownloadUtils.getBitmapFromURL

internal object LoadAdapter {
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


    fun loadImageFromMemory(viewLoadCode: Int) = BitmapMemoryCache.get(viewLoadCode)

    fun loadImageFromDisk(context: Context, viewLoadCode: Int, callback: (Bitmap?) -> Unit) =
        BitmapDiskCache.get(context, viewLoadCode, callback)


    fun downloadImage(
        url: String,
        reqWidth: Int,
        reqHeight: Int,
        viewLoadCode: Int,
        context: Context
    ) =
        getBitmapFromURL(url, reqWidth, reqHeight)?.also {
            PixelLog.debug(
                this@LoadAdapter.javaClass.simpleName,
                "Downloaded no = $viewLoadCode Bitmap for ${it.width}x${it.height} size in Kilobytes: ${it.byteCount / 1024}"
            )

            ViewLoad(reqWidth, reqHeight, url).run {
                BitmapMemoryCache.put(
                    hashCode(), it
                )
                BitmapDiskCache.put(context, this, it)
            }
        }

    private fun removeImageDownload(id: Int, removeFromCache: Boolean) {
        imageLoads.remove(id)?.also {
            if (removeFromCache)
                BitmapMemoryCache.clear(id)
        }
    }
}