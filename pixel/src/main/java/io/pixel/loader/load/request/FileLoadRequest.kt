package io.pixel.loader.load.request

import android.content.Context
import android.graphics.Bitmap
import io.pixel.config.PixelLog
import io.pixel.config.PixelOptions
import io.pixel.loader.load.LoadAdapter
import io.pixel.loader.load.ViewLoad
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

internal class FileLoadRequest(
    private val context:Context,
    private val viewLoad: ViewLoad,
    private val coroutineScope: CoroutineScope,
    private val pixelOptions: PixelOptions?,
    private val callback: (Bitmap) -> Unit
) : LoadRequest {

    override val id = viewLoad.hashCode()

    private var downloadJob: Job? = null

    override fun getLoadJob() = coroutineScope.launch(Dispatchers.IO) {
        viewLoad.run {
            PixelLog.debug(
                TAG,
                "Download no = ${hashCode()} started for $path for ${width}x${height}"
            )

            LoadAdapter.loadImageFromFile(
                context,
                this,
                pixelOptions
            )?.also(callback)
        }
    }

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
        if (downloadJob?.isActive == true) {
            PixelLog.error(
                TAG,
                message
            )
            downloadJob?.cancel()
        }
    }


    override fun start() {
        downloadJob = getLoadJob()
    }
}