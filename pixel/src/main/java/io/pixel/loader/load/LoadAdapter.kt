package io.pixel.loader.load

import io.pixel.config.PixelLog
import io.pixel.config.PixelOptions
import io.pixel.loader.cache.disk.BitmapDiskCache
import io.pixel.loader.cache.memory.BitmapMemoryCache
import io.pixel.loader.load.request.LoadRequest
import io.pixel.loader.load.request.download.Downloader.getBitmapFromURL

internal object LoadAdapter {
    private val imageLoads = HashMap<Int, LoadRequest>(100)

    @Synchronized
    fun addLoad(loadRequest: LoadRequest) {
        val id = loadRequest.id
        if (!imageLoads.containsKey(id))
            imageLoads[id] = loadRequest
        else cancelImageDownload(id)
    }


    private fun cancelImageDownload(
        id: Int,
        removeFromCache: Boolean = false
    ) {

        imageLoads[id]?.apply {
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

    fun loadImageFromFile(
        url: String,
        reqWidth: Int,
        reqHeight: Int,
        viewLoadCode: Int,
        pixelOptions: PixelOptions?
    ) {
     //todo
    }

    private fun removeImageDownload(id: Int, removeFromCache: Boolean) {
        imageLoads.remove(id)?.also {
            if (removeFromCache) {
                BitmapMemoryCache.clear(id)
                BitmapDiskCache.clear(id.toString())
            }
        }
    }
}