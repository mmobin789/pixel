package io.pixel.loader.load.request.download

import io.pixel.config.PixelLog
import io.pixel.config.PixelOptions
import io.pixel.loader.load.LoadAdapter.downloadImage
import io.pixel.loader.load.ViewLoad
import io.pixel.loader.load.request.ImageLoadRequest
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.isActive

internal class ImageDownloadRequest(
    private val viewLoad: ViewLoad,
    private val coroutineScope: CoroutineScope,
    private val pixelOptions: PixelOptions?
) : ImageLoadRequest {

    override val id = viewLoad.hashCode()

    private val TAG = javaClass.simpleName

    override fun equals(other: Any?): Boolean {
        if (other == null || other !is ImageDownloadRequest)
            return false

        return other.id == id
    }

    override fun hashCode(): Int {
        return id
    }

    override fun cancel(message: String) {
        PixelLog.error(
            TAG,
            message
        )
        coroutineScope.cancel(message)
    }

    override fun isRunning(): Boolean {
        return coroutineScope.isActive
    }

    override fun bitmap() = viewLoad.run {
        PixelLog.debug(
            TAG,
            "Image download request no = ${hashCode()} started for $path for ${width}x$height"
        )

        downloadImage(
            this,
            pixelOptions
        )
    }
}
