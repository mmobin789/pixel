package io.pixel.loader.load.type

import android.widget.ImageView
import io.pixel.config.PixelOptions
import io.pixel.loader.load.ViewLoad
import kotlinx.coroutines.CoroutineScope

internal class InternetImageLoad(
    viewLoad: ViewLoad,
    imageView: ImageView,
    pixelOptions: PixelOptions?,
    coroutineScope: CoroutineScope
) : ImageLoad(viewLoad, imageView, pixelOptions, coroutineScope) {

    override fun start() {
        loadFromInternet()
    }


}