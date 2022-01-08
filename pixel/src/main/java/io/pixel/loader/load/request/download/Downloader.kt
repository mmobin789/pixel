package io.pixel.loader.load.request.download

import android.graphics.Bitmap
import io.pixel.config.PixelLog
import io.pixel.config.PixelOptions
import io.pixel.utils.getDecodedBitmapFromByteArray
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException

internal object Downloader {

    var okHttpClient: OkHttpClient? = null

    fun getBitmapFromURL(
        url: String,
        reqWidth: Int,
        reqHeight: Int,
        pixelOptions: PixelOptions?
    ): Bitmap? {

        val okHttpClient = okHttpClient ?: OkHttpClient()
        val request = pixelOptions?.getRequest() ?: Request.Builder().url(url).build()
        PixelLog.debug(message = "$request")
        var response: Response? = null
        try {
            response = okHttpClient.newCall(request).execute()
            val body = response.body
            return body?.run {
                val bytes = byteStream().readBytes()
                bytes.getDecodedBitmapFromByteArray(reqWidth, reqHeight)
            }
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            response?.close()
        }

        return null
    }
}
