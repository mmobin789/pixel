package io.pixel.loader.cache.disk

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Environment
import android.os.Environment.isExternalStorageRemovable
import io.pixel.config.PixelLog
import io.pixel.config.PixelOptions
import io.pixel.loader.load.ViewLoad
import okio.Buffer
import okio.buffer
import java.io.File
import java.io.IOException

/**
 * An application of DiskLRUCache specific to bitmaps.
 */
internal object BitmapDiskCache {

    private var mDiskLRUCache: DiskLRUCache? = null

    private val TAG = javaClass.simpleName

    private var appVersion = 1

    private var cacheSizeInMB = 250L

    @Synchronized
    fun prepare(context: Context) {

        if (mDiskLRUCache == null)
            try {
                mDiskLRUCache = // 50mb
                    DiskLRUCache.open(
                        getDiskCacheDir(context),
                        appVersion,
                        1,
                        cacheSizeInMB * 1024 * 1024
                    ) // 250mb

                PixelLog.debug(TAG, "Disk Cache initialized successfully.")
                PixelLog.debug(TAG, "Cache Size = $cacheSizeInMB MegaBytes")
                PixelLog.debug(TAG, "App Version = $appVersion")
            } catch (e: IOException) {
                e.printStackTrace()
            }
    }

    fun setCacheSize(cacheSizeInMB: Long) {
        BitmapDiskCache.cacheSizeInMB = cacheSizeInMB
    }

    fun setAppVersion(appVersion: Int) {
        BitmapDiskCache.appVersion = appVersion
    }

    fun put(viewLoad: ViewLoad, bitmap: Bitmap, imageFormat: PixelOptions.ImageFormat) {
        val key = viewLoad.toString()
        val editor = mDiskLRUCache?.edit(key)
        try {

            if (mDiskLRUCache?.get(key) == null) {
                PixelLog.debug("$TAG ImageFormat", "${imageFormat.name} for ${viewLoad.hashCode()}")
                val buffer = Buffer()
                if (bitmap.compress(
                        if (imageFormat == PixelOptions.ImageFormat.PNG) Bitmap.CompressFormat.PNG else Bitmap.CompressFormat.JPEG,
                        100,
                        buffer.outputStream()
                    )
                ) {
                    mDiskLRUCache?.flush()
                    editor?.newSink(0)?.write(buffer, buffer.size)
                    editor?.commit()
                    buffer.close()
                    PixelLog.debug(TAG, "$key disk cached.")
                } else editor?.abort()
            }
        } catch (e: IOException) {
            e.printStackTrace()
            editor?.abort()
        }
    }

    fun get(viewLoadCode: Int): Bitmap? {
        val snapshot = mDiskLRUCache?.get(viewLoadCode.toString())
        val buffer = snapshot?.getSource(0)
            ?.buffer()
        val bitmap = BitmapFactory.decodeStream(buffer?.inputStream())
        buffer?.close()
        return bitmap
    }

    fun clear(key: String) = mDiskLRUCache?.remove(key)

    fun delete(context: Context) {
        try {
            prepare(context)
            mDiskLRUCache?.delete()
            PixelLog.debug(TAG, "Disk Cache Deleted Successfully.")
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    // Creates a unique subdirectory of the designated app cache directory. Tries to use external
// but if not mounted, falls back on internal storage.
    private fun getDiskCacheDir(context: Context): File {
        // Check if media is mounted or storage is built-in, if so, try and use external cache dir
        // otherwise use internal cache dir
        val cachePath =
            if (Environment.MEDIA_MOUNTED == Environment.getExternalStorageState() ||
                !isExternalStorageRemovable()
            ) {
                context.externalCacheDir?.run {
                    PixelLog.debug(TAG, "External Cache directory available.")
                    path
                } ?: run {
                    PixelLog.warn(TAG, "External Cache directory not available.")
                    context.cacheDir.path
                }
            } else context.cacheDir.path
        return File(cachePath + File.separator + "${context.packageName} Image Cache")
    }
}
