package io.pixel.android.loader.cache.memory

import android.os.Build
import android.util.LruCache
import io.pixel.android.config.PixelLog
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

internal class DocumentMemoryCache private constructor() {

    companion object {
        /*   Get max available VM memory, exceeding this amount will throw an
           OutOfMemory exception. Stored in kilobytes as LruCache takes an
           int in its constructor.*/

        private val maxMemory = (Runtime.getRuntime().maxMemory() / 1024).toInt()

        // Use 1/8th of the available memory for this memory cache.
        private val maxCacheSize = maxMemory / 8
        private var documentMemoryCache: DocumentMemoryCache? = null
        fun getInstance(): DocumentMemoryCache {
            if (documentMemoryCache == null)
                documentMemoryCache = DocumentMemoryCache()
            return documentMemoryCache!!
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


    private val cache = object : LruCache<String, String>(maxCacheSize) {
        override fun sizeOf(key: String?, value: String?): Int {
            return value!!.toByteArray().size / 1024
        }
    }

    /* fun isCached(key: String) = cache[key] != null*/

    fun get(key: String): String? = cache[key]

    fun put(key: String, value: String): String? {
        synchronized(cache)
        {
            if (cache[key] == null)
                return cache.put(key, value)

        }

        return null
    }

    /*  fun clear(key: String): String? = cache.remove(key)*/

    fun clear() = GlobalScope.launch {
        cache.evictAll()
    }

}