package io.pixel.android.loader.cache.disk

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Environment
import android.os.Environment.isExternalStorageRemovable
import io.pixel.android.config.PixelLog
import io.pixel.android.loader.load.ViewLoad
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okio.Buffer
import okio.buffer
import java.io.BufferedOutputStream
import java.io.File
import java.io.IOException

/**
 * An application of DiskLRUCache specific to bitmaps.
 */
internal object BitmapDiskCache {


    private var mDiskLRUCache: DiskLRUCache? = null

    private val TAG = javaClass.simpleName

    private val ioScope = CoroutineScope(Dispatchers.IO)

    private var appVersion = 1

    @Synchronized
    private fun buildInstance(context: Context) {
        if (mDiskLRUCache == null)
            try {
                mDiskLRUCache = // 50mb
                    DiskLRUCache.open(
                        getDiskCacheDir(context),
                        appVersion,
                        1,
                        250 * 1024 * 1024
                    ) // 50mb

                PixelLog.debug(TAG, "Disk Cache initialized successfully.")


            } catch (e: IOException) {
                e.printStackTrace()
            }

    }

    @Synchronized
    fun setCacheSize(context: Context, cacheSizeInMB: Long) {
        ioScope.launch {
            buildInstance(context)
            mDiskLRUCache?.setMaxSize(cacheSizeInMB)
        }
    }


    fun setAppVersion(appVersion: Int) {
        this.appVersion = appVersion
    }

    @Synchronized
    fun put(context: Context, viewLoad: ViewLoad, bitmap: Bitmap) {
        buildInstance(context)
        val key = viewLoad.toString()
        val editor = mDiskLRUCache?.edit(key)
        try {

            if (mDiskLRUCache?.get(key) == null) {
                val buffer = Buffer()
                val bos = BufferedOutputStream(buffer.outputStream())
                if (bitmap.compress(
                        Bitmap.CompressFormat.PNG,
                        100,
                        bos
                    )
                ) {
                    mDiskLRUCache?.flush()
                    editor?.newSink(0)?.write(buffer, buffer.size)
                    editor?.commit()
                    bos.close()
                    buffer.close()
                    PixelLog.debug(TAG, "$key disk cached.")

                } else editor?.abort()


            }

        } catch (e: IOException) {
            e.printStackTrace()
            editor?.abort()
        }


    }

    @Synchronized
    fun get(context: Context, viewLoadCode: Int, callback: (Bitmap?) -> Unit) {
        ioScope.launch(Dispatchers.IO) {
            buildInstance(context)
            val snapshot = mDiskLRUCache?.get(viewLoadCode.toString())
            val buffer = snapshot?.getSource(0)
                ?.buffer()
            val bitmap = BitmapFactory.decodeStream(buffer?.inputStream())
            buffer?.close()
            withContext(Dispatchers.Main.immediate)
            {
                callback(bitmap)
            }

        }
    }


    fun delete() {
        mDiskLRUCache?.delete()
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
        return File(cachePath + File.separator + "Bitmap Cache")
    }


}