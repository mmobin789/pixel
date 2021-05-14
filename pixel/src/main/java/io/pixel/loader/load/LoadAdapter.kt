package io.pixel.loader.load

import android.content.Context
import android.content.UriMatcher
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import android.util.Patterns
import io.pixel.config.PixelLog
import io.pixel.config.PixelOptions
import io.pixel.loader.cache.disk.BitmapDiskCache
import io.pixel.loader.cache.memory.BitmapMemoryCache
import io.pixel.loader.load.request.LoadRequest
import io.pixel.loader.load.request.download.Downloader.getBitmapFromURL
import io.pixel.utils.getDecodedBitmapFromByteArray
import java.io.*
import java.net.URI

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
        viewLoad: ViewLoad,
        pixelOptions: PixelOptions?
    ): Bitmap? {

        return getBitmapFromURL(
            viewLoad.path,
            viewLoad.width,
            viewLoad.height,
            pixelOptions
        )?.also {
            PixelLog.debug(
                this@LoadAdapter.javaClass.simpleName,
                "Downloaded no = ${viewLoad.hashCode()} Bitmap for ${it.width}x${it.height} size in Kilobytes: ${it.byteCount / 1024}"
            )

            updateCaches(it, viewLoad, pixelOptions)
        }
    }

    fun loadImageFromFile(
        context: Context,
        viewLoad: ViewLoad,
        pixelOptions: PixelOptions?
    ): Bitmap? {
        var fileIS: InputStream? = null
        return try {
            fileIS = context.contentResolver.openInputStream(Uri.parse(viewLoad.path))
            fileIS?.run {
                val bytes = readBytes()
                val reqWidth = viewLoad.width
                val reqHeight = viewLoad.height
                val bitmap = if (reqWidth > 0 && reqHeight > 0) {
                    bytes.getDecodedBitmapFromByteArray(reqWidth, reqHeight)
                } else {
                    bytes.getDecodedBitmapFromByteArray()
                }
                updateCaches(bitmap, viewLoad, pixelOptions, true)
                bitmap
            }

        }
        catch (e:FileNotFoundException) {
            PixelLog.error(tag = "LoadAdapter", e.stackTraceToString())
            null
        }
        catch (e: IOException) {
            PixelLog.error(tag = "LoadAdapter", e.stackTraceToString())
            null
        } finally {
            fileIS?.close()
        }

    }

    private fun updateCaches(
        bitmap: Bitmap,
        viewLoad: ViewLoad,
        pixelOptions: PixelOptions?,
        inMemoryOnly: Boolean = false
    ) = viewLoad.run {
        BitmapMemoryCache.put(
            hashCode(), bitmap
        )

        if (!inMemoryOnly) {
            BitmapDiskCache.put(
                this, bitmap,
                pixelOptions?.getImageFormat() ?: PixelOptions.ImageFormat.PNG
            )
        }
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