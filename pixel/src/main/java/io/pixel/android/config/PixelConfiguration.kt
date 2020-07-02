package io.pixel.android.config

import io.pixel.android.Pixel
import io.pixel.android.loader.cache.memory.MemoryCacheImpl

/**
 * Allow to set global configuration for pixel library.
 * @see Pixel
 * @author Mobin Munir
 */
object PixelConfiguration {
    /**
     * Allows to override default memory cache size for Images which is 1/8th of Virtual Machine Memory.
     * @param cacheSizeInKiloBytes new cache size in Kilobytes.
     * This call requires minimum Android API Level 21 or Lollipop.
     */
    @JvmStatic
    fun setImageMemoryCacheSize(cacheSizeInKiloBytes: Int) =
        MemoryCacheImpl.forBitmap().setCacheSize(cacheSizeInKiloBytes)

    /**
     * clears all images for memory cache.
     */
    @JvmStatic
    fun clearImageCache() = MemoryCacheImpl.forBitmap().clear()

    /**
     * Clears all memory caches used by library.
     * @see Pixel
     */
    @JvmStatic
    fun clearCaches() {
        clearImageCache()
    }

    /**
     * Set logging enabled for pixel.
     * @param loggingEnabled pass true/false to enable/disable logging.
     * Default is false.
     * @see Pixel
     */
    @JvmStatic
    fun setLoggingEnabled(loggingEnabled: Boolean) = PixelLog.enabled(loggingEnabled)

}
