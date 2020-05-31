package io.pixel.android.utils

import android.graphics.Bitmap
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.Charset


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