package io.pixel.android.utils

import android.graphics.Bitmap
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException


internal object DownloadUtils {

    fun getBitmapFromURL(url: String, reqWidth: Int, reqHeight: Int): Bitmap? {
        try {
            val response = createHTTPClient().newCall(Request.Builder().url(url).build()).execute()
            if (response.isSuccessful) {
                return response.body?.run {
                    val bytes = byteStream().readBytes()
                    val bitmap = if (reqWidth > 0 && reqHeight > 0)
                        bytes.getDecodedBitmapFromByteArray(reqWidth, reqHeight)
                    else bytes.getDecodedBitmapFromByteArray()
                    close()
                    bitmap

                }
            }

        } catch (e: IOException) {
            e.printStackTrace()
        }

        return null
    }

    private fun createHTTPClient() = OkHttpClient.Builder().build()
}