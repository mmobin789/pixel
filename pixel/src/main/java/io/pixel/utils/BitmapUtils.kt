package io.pixel.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.os.Build
import java.nio.ByteBuffer

internal fun ByteArray.getDecodedBitmapFromByteArray(
    reqWidth: Int,
    reqHeight: Int
): Bitmap {

    if (reqWidth <= 0 || reqHeight <= 0) {

        return getDecodedBitmapFromByteArray()
    }

    val byteArray = this

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {

        return ImageDecoder.decodeBitmap(
            ImageDecoder.createSource(ByteBuffer.wrap(byteArray))
        ) { decoder, _, _ ->
            decoder.setTargetSize(reqWidth, reqHeight)
        }
    }

    return BitmapFactory.Options().run {
        inJustDecodeBounds = true
        BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size, this)
        // Calculate inSampleSize
        inSampleSize = calculateInSampleSize(this, reqWidth, reqHeight)
        // Decode bitmap with inSampleSize set
        inJustDecodeBounds = false

        BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size, this)
    }
}

private fun ByteArray.getDecodedBitmapFromByteArray(): Bitmap {

    val byteArray = this

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {

        return ImageDecoder.decodeBitmap(
            ImageDecoder.createSource(ByteBuffer.wrap(this))
        ) { decoder, info, _ ->

            decoder.setTargetSize(info.size.width, info.size.height)
        }
    }
    return BitmapFactory.Options().run {
        inJustDecodeBounds = true

        BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size, this)
        // Calculate inSampleSize
        inSampleSize = calculateInSampleSize(this, outWidth, outHeight)
        // Decode bitmap with inSampleSize set
        inJustDecodeBounds = false

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
