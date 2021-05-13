package io.pixel.loader

import android.widget.ImageView
import io.pixel.config.PixelOptions
import io.pixel.loader.load.type.FileImageLoad
import io.pixel.loader.load.type.ImageLoad
import io.pixel.loader.load.type.InternetImageLoad
import io.pixel.loader.load.ViewLoad
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

    ): ImageLoad = InternetImageLoad(
        ViewLoad(
            imageView.width,
            imageView.height,
            url
        ), imageView, pixelOptions, coroutineScope
    )

    //todo working here
    fun loadFile(
        imageView: ImageView,
        path: String,
        pixelOptions: PixelOptions?,
        coroutineScope: CoroutineScope

    ): ImageLoad = FileImageLoad(
        ViewLoad(
            imageView.width,
            imageView.height,
            path
        ), imageView, pixelOptions, coroutineScope
    )
}