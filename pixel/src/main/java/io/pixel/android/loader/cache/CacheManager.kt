package io.pixel.android.loader.cache

import android.graphics.Bitmap
import io.pixel.android.loader.cache.memory.MemoryCacheImpl
import io.pixel.android.loader.load.ViewLoad

internal object CacheManager {
    private val bitmapMemoryCache = MemoryCacheImpl.forBitmap()

    fun cacheToMemory(viewLoad: ViewLoad, bitmap: Bitmap) {
//todo
    }

    fun cacheToDisk(viewLoad: ViewLoad, bitmap: Bitmap) {
//todo
    }
}