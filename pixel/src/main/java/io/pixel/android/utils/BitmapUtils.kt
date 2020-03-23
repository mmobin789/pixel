package io.pixel.android.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.os.Build
import java.nio.ByteBuffer


internal object BitmapUtils {

    fun getDecodedBitmapFromByteArray(
        byteArray: ByteArray,
        reqWidth: Int,
        reqHeight: Int
    ): Bitmap? {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {

            return ImageDecoder.decodeBitmap(
                ImageDecoder.createSource(ByteBuffer.wrap(byteArray))
            ) { decoder, info, _ ->
                decoder.setTargetSampleSize(
                    calculateInSampleSize(info.size.width, info.size.height, reqWidth, reqHeight)
                )


            }
        }



        return decodeBitmapFromByteArray(byteArray, reqWidth, reqHeight)
    }

    private fun decodeBitmapFromByteArray(
        byteArray: ByteArray,
        reqWidth: Int,
        reqHeight: Int
    ) =
        BitmapFactory.Options().run {
            inJustDecodeBounds = true
            BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size, this)
            // Calculate inSampleSize
            inSampleSize = calculateInSampleSize(this, reqWidth, reqHeight)
            // Decode bitmap with inSampleSize set
            inJustDecodeBounds = false

            BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size, this)
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

    private fun calculateInSampleSize(
        width: Int, height: Int,
        reqWidth: Int, reqHeight: Int
    ): Int {

        // Raw height and width of image in method params.

        var inSampleSize = 1
        if (height > reqHeight || width > reqWidth) {
            val halfHeight = height / 2
            val halfWidth = width / 2
            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
// height and width larger than the requested height and width.
            while (halfHeight / inSampleSize >= reqHeight
                && halfWidth / inSampleSize >= reqWidth
            ) {
                inSampleSize *= 2
            }
        }
        return inSampleSize
    }
}