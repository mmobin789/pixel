package io.pixel.android.loader.cache.memory

import android.graphics.Bitmap
import android.os.Build
import android.util.LruCache
import io.pixel.android.config.PixelLog
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


internal class BitmapMemoryCache private constructor() {

    companion object {
        /*   Get max available VM memory, exceeding this amount will throw an
           OutOfMemory exception. Stored in kilobytes as LruCache takes an
          int in its constructor.*/

        private val maxMemory = (Runtime.getRuntime().maxMemory() / 1024).toInt()

        // Use 1/8th of the available memory for this memory cache.
        private val maxCacheSize = maxMemory / 8
        private var bitmapMemoryCache: BitmapMemoryCache? = null

        fun getInstance(): BitmapMemoryCache {
            if (bitmapMemoryCache == null)
                bitmapMemoryCache = BitmapMemoryCache()

            return bitmapMemoryCache!!
        }
    }

    init {
        PixelLog.debug(javaClass.simpleName, "Max JVM = $maxMemory")
        PixelLog.debug(javaClass.simpleName, "Cache Size = $maxCacheSize")
    }


    fun setCacheSize(cacheSizeInKiloBytes: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            cache.resize(cacheSizeInKiloBytes)
            PixelLog.debug(javaClass.simpleName, "New Cache Size = $cacheSizeInKiloBytes")
        }
    }

    private val cache = object : LruCache<Int, Bitmap>(maxCacheSize) {
        override fun sizeOf(key: Int?, value: Bitmap?): Int {
            // The cache size will be measured in kilobytes rather than
            // number of items.
            return value!!.byteCount / 1024
        }
    }

    fun get(key: Int): Bitmap? = cache[key]

    fun put(key: Int, bitmap: Bitmap) {
        synchronized(cache)
        {
            if (get(key) == null)
                cache.put(key, bitmap)


        }
    }

    fun clear(key: Int): Bitmap? = cache.remove(key)

    fun clear() = GlobalScope.launch {
        cache.evictAll()
    }
}