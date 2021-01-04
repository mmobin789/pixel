package io.pixel.android.loader

import android.widget.ImageView
import io.pixel.android.config.PixelOptions
import io.pixel.android.loader.load.ImageLoad
import io.pixel.android.loader.load.ViewLoad
import kotlinx.coroutines.CoroutineScope

/**
 * A proxy class representing a link to load process and it's states.
 */

internal object LoaderProxy {

    fun loadUrl(
        imageView: ImageView,
        url: String,
        pixelOptions: PixelOptions?,
        coroutineScope: CoroutineScope

    ) = ImageLoad(
        ViewLoad(
            imageView.width,
            imageView.height,
            url
        ), imageView, pixelOptions, coroutineScope
    ).also { it.start() }

    //todo working here
    fun loadFile(
        imageView: ImageView,
        path: String,
        pixelOptions: PixelOptions?,
        coroutineScope: CoroutineScope

    ) = ImageLoad(
        ViewLoad(
            imageView.width,
            imageView.height,
            path
        ), imageView, pixelOptions, coroutineScope
    ).also { it.start() }
}