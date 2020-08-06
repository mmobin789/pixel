package io.pixel.android.loader.cache.disk

import android.content.Context
import io.pixel.android.loader.load.ViewLoad
import okio.IOException

internal object DiskCacheImpl {

    @Volatile
    private lateinit var mDiskLRUCache: DiskLRUCache


    @Synchronized
    private fun buildInstance(context: Context, appVersion: Int = 1) {

        try {
            if (!::mDiskLRUCache.isInitialized) {
                mDiskLRUCache = DiskLRUCache.open(context.cacheDir, appVersion, 1, 50 * 1024 * 1024)
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }

    }

    fun put(context: Context, viewLoad: ViewLoad, byteArray: ByteArray) {

        buildInstance(context)
        try {
            // todo working here
            val editor = mDiskLRUCache.edit(viewLoad.toString())
            // editor?.newSink()
        } catch (e: Exception) {

        }


    }


}