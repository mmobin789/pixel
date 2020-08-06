package io.pixel.android.utils

import android.graphics.Bitmap
import java.io.IOException
import java.net.URL


internal object DownloadUtils {

    fun getBitmapFromURL(path: String, reqWidth: Int, reqHeight: Int): Bitmap? {
        return try {
            URL(path).run {
                val iS = openStream()
                iS.r
                val bytes = iS.readBytes()
                BitmapUtils.getDecodedBitmapFromByteArray(bytes, reqWidth, reqHeight)
                    .also {
                        iS.close()
                    }

            }
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }
}