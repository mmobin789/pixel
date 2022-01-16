package io.pixel.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import io.pixel.loader.cache.memory.BitmapMemoryCache
import java.lang.ref.SoftReference

internal fun ByteArray.getDecodedBitmapFromByteArray(
    reqWidth: Int,
    reqHeight: Int,
    bitmapSrc: BitmapSrc
): Bitmap {

    if (reqWidth <= 0 || reqHeight <= 0) {

        return getDecodedBitmapFromByteArray()
    }

    val byteArray = this

    return BitmapFactory.Options().run {
        inJustDecodeBounds = true
        BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size, this)
        // Calculate inSampleSize
        inSampleSize = calculateInSampleSize(this, reqWidth, reqHeight)
        // Decode bitmap with inSampleSize set
        inJustDecodeBounds = false

        if (bitmapSrc == BitmapSrc.File)
            addInBitmapOptions(this)
        BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size, this)
    }
}

private fun ByteArray.getDecodedBitmapFromByteArray(): Bitmap {

    val byteArray = this

    return BitmapFactory.Options().run {
        inJustDecodeBounds = true

        BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size, this)
        // Calculate inSampleSize
        inSampleSize = calculateInSampleSize(this, outWidth, outHeight)
        // Decode bitmap with inSampleSize set
        inJustDecodeBounds = false

        addInBitmapOptions(this)

        BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size, this)
    }
}

private fun calculateInSampleSize(
    options: BitmapFactory.Options,
    reqWidth: Int,
    reqHeight: Int
): Int {
    // Raw height and width of image
    val (height: Int, width: Int) = options.run { outHeight to outWidth }
    var inSampleSize = 1

    if (height > reqHeight || width > reqWidth) {
        val halfHeight: Int = height / 2
        val halfWidth: Int = width / 2

        // Calculate the largest inSampleSize value that is a power of 2 and keeps both
        // height and width larger than the requested height and width.
        while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
            inSampleSize *= 2
        }
    }

    return inSampleSize
}

private fun addInBitmapOptions(options: BitmapFactory.Options) {
    // inBitmap only works with mutable bitmaps, so force the decoder to
    // return mutable bitmaps.
    options.inMutable = true

    // Try to find a bitmap to use for inBitmap.
    getBitmapFromReusableSet(options)?.also { inBitmap ->
        // If a suitable bitmap has been found, set it as the value of
        // inBitmap.
        options.inBitmap = inBitmap
    }
}

// This method iterates through the reusable bitmaps, looking for one
// to use for inBitmap:
private fun getBitmapFromReusableSet(options: BitmapFactory.Options): Bitmap? {
    BitmapMemoryCache.reusableBitmaps.takeIf { it.isNotEmpty() }?.let { reusableBitmaps ->
        synchronized(reusableBitmaps) {
            val iterator: MutableIterator<SoftReference<Bitmap>> = reusableBitmaps.iterator()
            while (iterator.hasNext()) {
                iterator.next().get()?.let { item ->
                    if (item.isMutable) {
                        // Check to see it the item can be used for inBitmap.
                        if (canUseForInBitmap(item, options)) {
                            // Remove from reusable set so it can't be used again.
                            iterator.remove()
                            return item
                        }
                    } else {
                        // Remove from the set if the reference has been cleared.
                        iterator.remove()
                    }
                }
            }
        }
    }
    return null
}

private fun canUseForInBitmap(candidate: Bitmap, targetOptions: BitmapFactory.Options): Boolean {
    // From Android 4.4 (KitKat) onward we can re-use if the byte size of
    // the new bitmap is smaller than the reusable bitmap candidate
    // allocation byte count.
    val width: Int = targetOptions.outWidth / targetOptions.inSampleSize
    val height: Int = targetOptions.outHeight / targetOptions.inSampleSize
    val byteCount: Int = width * height * getBytesPerPixel(candidate.config)
    return byteCount <= candidate.allocationByteCount
}

/**
 * A helper function to return the byte usage per pixel of a bitmap based on its configuration.
 */
private fun getBytesPerPixel(config: Bitmap.Config): Int {
    return when (config) {
        Bitmap.Config.ARGB_8888 -> 4
        Bitmap.Config.RGB_565, Bitmap.Config.ARGB_4444 -> 2
        Bitmap.Config.ALPHA_8 -> 1
        else -> 1
    }
}

/**
 * A personal algorithm for view load hashing purpose.
 * It creates a unique ID for each cached image.
 */
internal fun String.getUniqueIdentifier(): Int {
    var sum = 0
    forEach {
        sum += it.code
    }
    return sum
}

sealed class BitmapSrc {
    object Internet : BitmapSrc()
    object File : BitmapSrc()
}
