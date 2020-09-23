package io.pixel.android.loader

import android.widget.ImageView
import io.pixel.android.config.PixelOptions
import io.pixel.android.loader.load.ImageLoad
import io.pixel.android.loader.load.LoadRequest
import io.pixel.android.loader.load.ViewLoad
import kotlinx.coroutines.CoroutineScope

/**
 * A proxy class representing a link to load process and it's states.
 */

internal object LoaderProxy {

    private val cancelledLoadRequests = ArrayList<LoadRequest>(100)

    fun loadImage(
        imageView: ImageView,
        path: String,
        pixelOptions: PixelOptions?,
        coroutineScope: CoroutineScope
    ) {
        val viewLoad = ViewLoad(
            imageView.width,
            imageView.height,
            path
        )

        ImageLoad(viewLoad, imageView, pixelOptions, coroutineScope).also { it.start() }

    }

    fun addCancelledLoad(loadRequest: LoadRequest) {
        cancelledLoadRequests.add(loadRequest)
        cancelLoads()
    }

    private fun cancelLoads() = cancelledLoadRequests.forEachIndexed { index, loadRequest ->
        loadRequest.cancel()
        cancelledLoadRequests.removeAt(index)

    }
}