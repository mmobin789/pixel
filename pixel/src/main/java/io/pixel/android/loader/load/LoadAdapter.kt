package io.pixel.pixel.loader.load

import io.pixel.android.config.PixelLog
import io.pixel.android.loader.cache.memory.MemoryCacheImpl
import io.pixel.android.loader.download.ImageDownload
import io.pixel.android.loader.download.JsonDownload
import io.pixel.android.utils.DownloadUtils.getBitmapFromURL
import io.pixel.android.utils.DownloadUtils.getJSONStringFromURL


internal object LoadAdapter {
    private val bitmapMemoryCache = MemoryCacheImpl.forBitmap()
    private val documentMemoryCache = MemoryCacheImpl.forDocument()
    private val imageLoads = linkedMapOf<Int, ImageDownload>()
    private val jsonLoads = linkedMapOf<Int, JsonDownload>()

    fun addDownload(imageDownload: ImageDownload) {
        imageLoads.put(imageDownload.id, imageDownload)?.also {
            cancelImageDownload(
                it.id
            )
        }
    }

    fun addDownload(jsonDownload: JsonDownload) {
        jsonLoads[jsonDownload.id] = jsonDownload
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


    fun loadStringFromCache(path: String) = documentMemoryCache.get(path)


    fun downloadString(url: String) = getJSONStringFromURL(url)?.also {
        PixelLog.debug(this@LoadAdapter.javaClass.simpleName, "Downloaded JSON schema $it")
        documentMemoryCache.put(url, it)
    }

    private fun removeImageDownload(id: Int, removeFromCache: Boolean) {
        imageLoads.remove(id)?.also {
            if (removeFromCache)
                bitmapMemoryCache.clear(id)
        }
    }


}