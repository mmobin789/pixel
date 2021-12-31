package io.pixel.loader.load.request.download

import io.pixel.config.PixelLog
import io.pixel.config.PixelOptions
import io.pixel.loader.load.LoadAdapter
import io.pixel.loader.load.ViewLoad
import io.pixel.loader.load.request.ImageLoadRequest
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.job

internal class ImageDownloadRequest(
    private val viewLoad: ViewLoad,
    coroutineScope: CoroutineScope,
    private val pixelOptions: PixelOptions?
) : ImageLoadRequest {

    override val id = viewLoad.hashCode()

    private val downloadJob = coroutineScope.coroutineContext.job

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
        downloadJob.cancel(CancellationException(message))
    }

    override fun isRunning(): Boolean {
        return downloadJob.isActive
    }

    override fun bitmap() = viewLoad.run {
        PixelLog.debug(
            TAG,
            "Image download request no = ${hashCode()} started for $path for ${width}x$height"
        )

        LoadAdapter.downloadImage(
            this,
            pixelOptions
        )
    }
}
