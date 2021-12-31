package io.pixel.config

import android.content.Context
import io.pixel.Pixel
import io.pixel.loader.cache.disk.BitmapDiskCache
import io.pixel.loader.cache.memory.BitmapMemoryCache
import io.pixel.loader.load.request.download.Downloader
import okhttp3.OkHttpClient

/**
 * Allow to set application-level configuration for pixel library.
 * @see Pixel
 * @author Mobin Munir
 */
object PixelConfiguration {

    /**
     * Allows to override default memory cache size which is 1/8th of Virtual Machine Memory.
     * @param cacheSizeInMegaBytes new cache size in MegaBytes.
     * This call requires minimum Android API Level 21 or Lollipop.
     */
    @JvmStatic
    fun setMemoryCacheSize(cacheSizeInMegaBytes: Int) =
        BitmapMemoryCache.setCacheSize(cacheSizeInMegaBytes)

    /**
     * Allows to override default memory cache size which is 250MB (MegaBytes)
     * @param cacheSizeInMegaBytes new cache size in Megabytes.
     */
    @JvmStatic
    fun setDiskCacheSize(cacheSizeInMegaBytes: Long) =
        BitmapDiskCache.setCacheSize(cacheSizeInMegaBytes)

    /**
     * Allows to set the app version for disk cache entries.
     * @param appVersion to use.
     */
    @JvmStatic
    fun setAppVersion(appVersion: Int) = BitmapDiskCache.setAppVersion(appVersion)

    /**
     * This allows for full control of OkHttp client responsible for each network request's operations per image load.
     * A custom configuration client can be supplied to use for each request.
     * @param okHttpClient to set or null to use default.
     */
    @JvmStatic
    fun setOkHttpClient(okHttpClient: OkHttpClient?) {
        Downloader.okHttpClient = okHttpClient
    }

    /**
     * clears all images for memory cache.
     */
    @JvmStatic
    fun clearMemoryCache() = BitmapMemoryCache.clear()

    /**
     * clears all images from disk cache.
     * Note: Since this is a blocking call so invoke this method from an I/O bound thread.
     */

    @JvmStatic
    fun clearDiskCache(context: Context) = BitmapDiskCache.delete(context)

    /**
     * Set logging enabled for pixel.
     * @param loggingEnabled pass true/false to enable/disable logging.
     * Default is false.
     * @see Pixel
     */
    @JvmStatic
    fun setLoggingEnabled(loggingEnabled: Boolean) = PixelLog.enabled(loggingEnabled)

}
