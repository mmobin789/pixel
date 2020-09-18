package io.pixel.android.loader.download

import android.graphics.Bitmap
import io.pixel.android.config.PixelLog
import io.pixel.android.loader.load.LoadAdapter
import io.pixel.android.loader.load.ViewLoad
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

internal class ImageDownload(
    private val viewLoad: ViewLoad,
    private val coroutineScope: CoroutineScope
) {

    val id = viewLoad.hashCode()

    private val TAG = javaClass.simpleName

    private var downloadJob: Job? = null

    override fun equals(other: Any?): Boolean {
        if (other == null || other !is ImageDownload)
            return false

        return other.id == id
    }

    override fun hashCode(): Int {
        return id
    }

    fun cancel(message: String = "Download Cancelled for $id") {
        if (downloadJob?.isActive == true) {
            PixelLog.error(
                TAG,
                message
            )
            downloadJob?.cancel()
        }
    }

    /* fun isCancelled(): Boolean {
         if (downloadJob == null)
             return true

         return downloadJob?.isCancelled!!
     }*/


    fun start(callback: (Bitmap) -> Unit) {
        downloadJob = coroutineScope.launch(Dispatchers.IO) {
            viewLoad.run {
                PixelLog.debug(
                    TAG,
                    "Download no = ${hashCode()} started for $path for ${width}x${height}"
                )

                LoadAdapter.downloadImage(
                    path,
                    width,
                    height,
                    hashCode()
                )?.also {
                    callback(it)

                }
            }
        }
    }
}