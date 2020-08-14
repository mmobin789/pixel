package io.pixel.android.utils

import android.graphics.Bitmap
import java.io.IOException
import java.net.URL


internal object DownloadUtils {

    fun getBitmapFromURL(path: String, reqWidth: Int, reqHeight: Int): Bitmap? {
        return try {
            URL(path).run {
                val iS = openStream()
                BitmapUtils.getDecodedBitmapFromByteArray(iS.readBytes(), reqWidth, reqHeight)
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