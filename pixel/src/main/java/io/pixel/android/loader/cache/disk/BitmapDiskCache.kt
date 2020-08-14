package io.pixel.android.loader.cache.disk

import android.content.Context
import android.graphics.Bitmap
import android.os.Environment
import android.os.Environment.isExternalStorageRemovable
import io.pixel.android.loader.load.ViewLoad
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import okio.Buffer
import java.io.File
import java.io.IOException

internal class BitmapDiskCache(private val context: Context) {


    private var mDiskLRUCache: DiskLRUCache? = null

    init {
        GlobalScope.launch(Dispatchers.IO) {
            buildInstance()
        }
    }

    @Synchronized
    private fun buildInstance(appVersion: Int = 1) {
        try {

            mDiskLRUCache =
                DiskLRUCache.open(getDiskCacheDir(), appVersion, 1, 50 * 1024 * 1024) // 50mb

        } catch (e: IOException) {
            e.printStackTrace()
        }

    }


    fun put(viewLoad: ViewLoad, bitmap: Bitmap) {
        GlobalScope.launch(Dispatchers.IO) {
            try {
                val key = viewLoad.toString()
                // todo working here
                if (mDiskLRUCache != null && mDiskLRUCache?.get(key) == null) {
                    val editor = mDiskLRUCache?.edit(key)
                    val mutableBytes = Buffer()
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, mutableBytes.outputStream())
                    editor?.newSink(0)?.write(mutableBytes, mutableBytes.size)
                    editor?.commit()
                }

            } catch (e: IOException) {
                e.printStackTrace()

            }
        }


    }

    // Creates a unique subdirectory of the designated app cache directory. Tries to use external
    // but if not mounted, falls back on internal storage.
    private fun getDiskCacheDir(): File {
        // Check if media is mounted or storage is built-in, if so, try and use external cache dir
        // otherwise use internal cache dir
        val cachePath =
            if (Environment.MEDIA_MOUNTED == Environment.getExternalStorageState() ||
                !isExternalStorageRemovable()
            ) context.externalCacheDir!!.path else context.cacheDir.path
        return File(cachePath + File.separator + "Bitmap Cache")
    }


}