package io.pixel.android.loader.download

import android.graphics.Bitmap
import io.pixel.android.config.PixelLog
import io.pixel.android.loader.load.LoadAdapter
import io.pixel.android.loader.load.ViewLoad
import kotlinx.coroutines.*
import java.util.concurrent.CancellationException

internal data class ImageDownload(private val viewLoad: ViewLoad) {

    private var downloadJob: Job? = null

    val id = viewLoad.hashCode()


    override fun equals(other: Any?): Boolean {

        if (other == null || other !is ImageDownload)
            return false

        return other.id == id
    }

    override fun hashCode(): Int {
        return id
    }

    fun cancel(message: String? = null) {
        if (downloadJob?.isActive == true) {
            downloadJob?.cancel(CancellationException(message))
            PixelLog.error(
                javaClass.simpleName,
                "Image Download for id = $id is explicitly cancelled"
            )
        }
    }

    fun isCancelled() = downloadJob?.isCancelled == true

    /*fun isActive() = downloadJob?.isActive == true*/


    fun onReady(callback: (Bitmap) -> Unit) {
        downloadJob = GlobalScope.launch(Dispatchers.IO) {
            viewLoad.run {
                PixelLog.debug(
                    this@ImageDownload.javaClass.simpleName,
                    "Download no = ${hashCode()} started for $path for ${width}x${height}"
                )


                LoadAdapter.downloadImage(
                    path,
                    width,
                    height,
                    hashCode()

                )?.also {
                    withContext(Dispatchers.Main) {
                        callback(it)

                    }
                }
            }
        }
    }

}