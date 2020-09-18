package io.pixel.android.config

import io.pixel.android.Pixel
import io.pixel.android.loader.cache.disk.BitmapDiskCache
import io.pixel.android.loader.cache.memory.BitmapMemoryCache

/**
 * Allow to set application-level configuration for pixel library.
 * @see Pixel
 * @author Mobin Munir
 */
object PixelConfiguration {

    /**
     * Allows to override default memory cache size which is 1/8th of Virtual Machine Memory.
     * @param cacheSizeInMegaBytes new cache size in Kilobytes.
     * This call requires minimum Android API Level 21 or Lollipop.
     */
    @JvmStatic
    fun setMemoryCacheSize(cacheSizeInMegaBytes: Int) =
        BitmapMemoryCache.setCacheSize(cacheSizeInMegaBytes)

    /**
     * Allows to override default memory cache size which is 250MB (MegaBytes)
     * @param cacheSizeInMegaBytes new cache size in Megabytes.
     * This call requires minimum Android API Level 21 or Lollipop.
     */
    @JvmStatic
    fun setDiskCacheSize(cacheSizeInMegaBytes: Long) =
        BitmapDiskCache.setCacheSize(cacheSizeInMegaBytes)

    @JvmStatic
    fun setAppVersion(appVersion: Int) = BitmapDiskCache.setAppVersion(appVersion)

    /**
     * clears all images for memory cache.
     */
    @JvmStatic
    fun clearMemoryCache() = BitmapMemoryCache.clear()

    @JvmStatic
    fun clearDiskCache() = BitmapDiskCache.delete()

    /**
     * Set logging enabled for pixel.
     * @param loggingEnabled pass true/false to enable/disable logging.
     * Default is false.
     * @see Pixel
     */
    @JvmStatic
    fun setLoggingEnabled(loggingEnabled: Boolean) = PixelLog.enabled(loggingEnabled)

}
