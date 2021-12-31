package io.pixel.loader

import android.widget.ImageView
import io.pixel.config.PixelOptions
import io.pixel.loader.load.ViewLoad
import io.pixel.loader.load.type.FileImageLoad
import io.pixel.loader.load.type.InternetImageLoad
import kotlinx.coroutines.CoroutineScope

/**
 * A proxy class representing a link to load process and it's states.
 */

internal object LoaderProxy {

    suspend fun loadUrl(
        imageView: ImageView,
        url: String,
        pixelOptions: PixelOptions?,
        coroutineScope: CoroutineScope

    ) = InternetImageLoad(
        ViewLoad(
            imageView.width,
            imageView.height,
            url
        ),
        imageView, pixelOptions, coroutineScope
    ).invoke()

    suspend fun loadFile(
        imageView: ImageView,
        path: String,
        pixelOptions: PixelOptions?,
        coroutineScope: CoroutineScope

    ) = FileImageLoad(
        ViewLoad(
            imageView.width,
            imageView.height,
            path
        ),
        imageView, pixelOptions, coroutineScope
    ).invoke()
}
