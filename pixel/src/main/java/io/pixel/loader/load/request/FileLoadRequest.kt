package io.pixel.loader.load.request

import android.content.Context
import io.pixel.config.PixelLog
import io.pixel.config.PixelOptions
import io.pixel.loader.load.LoadAdapter.loadImageFromFile
import io.pixel.loader.load.ViewLoad
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.job

internal class FileLoadRequest(
    private val context: Context,
    private val viewLoad: ViewLoad,
    coroutineScope: CoroutineScope,
    private val pixelOptions: PixelOptions?
) : ImageLoadRequest {

    override val id = viewLoad.hashCode()

    private val loadJob = coroutineScope.coroutineContext.job

    private val TAG = javaClass.simpleName

    override fun equals(other: Any?): Boolean {
        if (other == null || other !is FileLoadRequest)
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
        loadJob.cancel(CancellationException(message))
    }

    override fun isRunning(): Boolean {
        return loadJob.isActive
    }

    override fun bitmap() = viewLoad.run {
        PixelLog.debug(
            TAG,
            "File load request no = ${hashCode()} started for $path for ${width}x$height"
        )

        loadImageFromFile(
            context,
            this,
            pixelOptions
        )
    }
}
