package io.pixel.loader.load

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import io.pixel.config.PixelLog
import io.pixel.config.PixelOptions
import io.pixel.loader.cache.disk.BitmapDiskCache
import io.pixel.loader.cache.memory.BitmapMemoryCache
import io.pixel.loader.load.request.ImageLoadRequest
import io.pixel.loader.load.request.download.Downloader.getBitmapFromURL
import io.pixel.utils.BitmapSrc
import io.pixel.utils.getDecodedBitmapFromByteArray
import java.io.FileNotFoundException
import java.io.IOException
import java.io.InputStream
import java.util.Collections

internal object LoadAdapter {
    private val imageLoads = Collections.synchronizedMap(hashMapOf<Int, ImageLoadRequest>())

    fun addLoad(imageLoadRequest: ImageLoadRequest) {
        val id = imageLoadRequest.id

        if (cancelImageDownload(id) || imageLoads[id] == null) {
            imageLoads[id] = imageLoadRequest
        }
    }

    private fun cancelImageDownload(
        id: Int
    ): Boolean {
        val loadRequest = imageLoads[id]

        return loadRequest?.let {
            if (it.isRunning()) {
                BitmapMemoryCache.clear(it.id)
                BitmapDiskCache.clear(it.id.toString())
                imageLoads.remove(id)?.cancel()
                true
            } else false
        } ?: false
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
                "Internet downloaded no = ${viewLoad.hashCode()} Bitmap for ${it.width}x${it.height} size in Kilobytes: ${it.byteCount / 1024}"
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
                val bitmap = bytes.getDecodedBitmapFromByteArray(reqWidth, reqHeight, BitmapSrc.File)
                updateCaches(bitmap, viewLoad, pixelOptions)
                PixelLog.debug(
                    this@LoadAdapter.javaClass.simpleName,
                    "File downloaded no = ${viewLoad.hashCode()} Bitmap for ${reqWidth}x$reqHeight size in Kilobytes: ${bitmap.byteCount / 1024}"
                )
                bitmap
            }
        } catch (e: FileNotFoundException) {
            PixelLog.error(tag = "LoadAdapter", e.stackTraceToString())
            null
        } catch (e: IOException) {
            PixelLog.error(tag = "LoadAdapter", e.stackTraceToString())
            null
        } catch (e: OutOfMemoryError) { // this will be only thrown if image view size is dynamic and no image has been set to it forcing library to download original images pure of any scaling.
            PixelLog.error(tag = "LoadAdapter", e.stackTraceToString())
            null
        } finally {
            fileIS?.close()
        }
    }

    private fun updateCaches(
        bitmap: Bitmap,
        viewLoad: ViewLoad,
        pixelOptions: PixelOptions?
    ) = viewLoad.run {

        BitmapMemoryCache.put(
            hashCode(), bitmap
        )
        BitmapDiskCache.put(
            this, bitmap,
            pixelOptions?.getImageFormat() ?: PixelOptions.ImageFormat.PNG
        )
    }
}
