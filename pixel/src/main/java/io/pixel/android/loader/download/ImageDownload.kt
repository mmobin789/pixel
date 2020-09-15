package io.pixel.android.loader.download

import android.content.Context
import android.graphics.Bitmap
import io.pixel.android.config.PixelLog
import io.pixel.android.loader.load.LoadAdapter
import io.pixel.android.loader.load.ViewLoad
import kotlinx.coroutines.*
import java.util.concurrent.CancellationException

internal class ImageDownload(private val viewLoad: ViewLoad) {

    val id = viewLoad.hashCode()

    private val ioScope = CoroutineScope(Dispatchers.IO)

    override fun equals(other: Any?): Boolean {
        if (other == null || other !is ImageDownload)
            return false

        return other.id == id
    }

    override fun hashCode(): Int {
        return id
    }

    fun cancel(message: String = "Download Cancelled for $id") {
        if (ioScope.isActive) {
            ioScope.cancel(CancellationException(message))
            PixelLog.error(
                javaClass.simpleName,
                "Image Download for id = $id is explicitly cancelled"
            )
        }
    }

    fun isCancelled() = !ioScope.isActive

    fun start(callback: (Bitmap) -> Unit, context: Context) {
        ioScope.launch(Dispatchers.IO) {
            viewLoad.run {
                PixelLog.debug(
                    this@ImageDownload.javaClass.simpleName,
                    "Download no = ${hashCode()} started for $path for ${width}x${height}"
                )

                LoadAdapter.downloadImage(
                    path,
                    width,
                    height,
                    hashCode(),
                    context
                )?.also {
                    withContext(Dispatchers.Main) {
                        callback(it)
                    }
                }
            }
        }
    }
}