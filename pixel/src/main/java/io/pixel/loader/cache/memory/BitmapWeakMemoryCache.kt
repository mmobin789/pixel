package io.pixel.loader.cache.memory

import android.graphics.Bitmap
import java.util.Collections
import java.util.WeakHashMap

/**
 * A weak memory cache backed by a weak hashmap to back the LRU cache.
 */
internal object BitmapWeakMemoryCache {

    private val cache = Collections.synchronizedMap(WeakHashMap<Int, Bitmap>())

    fun get(key: Int): Bitmap? = cache[key]

    fun put(key: Int, bitmap: Bitmap) {
        if (get(key) == null)
            cache[key] = bitmap
    }

    //  fun clear(key: Int): Bitmap? = cache.remove(key)

    fun clear() = cache.clear()
}
