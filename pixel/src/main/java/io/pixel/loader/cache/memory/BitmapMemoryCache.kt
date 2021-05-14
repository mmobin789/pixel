package io.pixel.loader.cache.memory

import android.graphics.Bitmap
import android.os.Build
import android.util.LruCache
import io.pixel.config.PixelLog


internal object BitmapMemoryCache {


    /*   Get max available VM memory, exceeding this amount will throw an
       OutOfMemory exception. Stored in kilobytes as LruCache takes an
      int in its constructor.*/

    private val maxMemory = (Runtime.getRuntime().maxMemory() / 1024).toInt()

    // Use 1/8th of the available memory for this memory cache.
    private val maxCacheSize = maxMemory / 8


    init {
        PixelLog.debug(javaClass.simpleName, "Max JVM = $maxMemory")
        PixelLog.debug(javaClass.simpleName, "Cache Size = ${maxCacheSize / 1024} MegaBytes")
    }


    fun setCacheSize(cacheSizeInMegaBytes: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            cache.resize(cacheSizeInMegaBytes * 1024)
            PixelLog.debug(javaClass.simpleName, "New Cache Size = $cacheSizeInMegaBytes MegaBytes")
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

    fun clear() = cache.evictAll()

}