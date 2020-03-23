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

    fun getJSONStringFromURL(path: String): String? {
        val url = URL(path)
        val connection = url.openConnection() as HttpURLConnection
        return connection.run {
            var bufferedReader: BufferedReader? = null
            try {
                connect()
                bufferedReader =
                    BufferedReader(InputStreamReader(inputStream, Charset.defaultCharset()))
                val stringBuffer = StringBuffer(500)
                var line: String?

                while (bufferedReader.readText().also { line = it }.isNotEmpty()) {
                    stringBuffer.append(line).append("\n")
                }

                if (stringBuffer.isBlank())
                    return null


                stringBuffer.toString()

            } catch (e: IOException) {
                e.printStackTrace()
                null
            } finally {
                bufferedReader?.close()
                disconnect()
            }


        }

    }
}