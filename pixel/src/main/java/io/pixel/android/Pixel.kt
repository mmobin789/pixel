package io.pixel.android

import android.widget.ImageView
import io.pixel.android.config.PixelOptions
import io.pixel.android.loader.LoaderProxy
import io.pixel.android.utils.UrlValidator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Pixel is a coroutine library to load and cache images.
 * @author Mobin Munir
 */
class Pixel private constructor() {
    private val mainThreadScope = CoroutineScope(Dispatchers.Main.immediate)

    companion object {
        const val TAG = "Pixel"
        private var pixel: Pixel? = null

        private fun init(): Pixel {
            if (pixel == null)
                pixel =
                    Pixel()
            return pixel!!
        }

        /**
         * Primary method to load images from urls once the view hierarchy is rendered on the main thread.
         * If the UI thread is otherwise engaged the requests are automatically paused.
         * It is also safe to call this method from a background thread.
         * @param url The image url or null if a custom request is intended to be supplied.
         * Default is null.
         * @see PixelOptions class.
         * @param imageView to load into.
         * @param pixelOptions Custom image load options.
         * Default are null.
         */
        @JvmStatic
        fun load(
            url: String? = null,
            pixelOptions: PixelOptions? = null,
            imageView: ImageView
        ): Pixel {

            return init().apply {
                UrlValidator.validateURL(pixelOptions?.getRequest()?.url?.toString() ?: url)
                    ?.apply path@{
                        mainThreadScope.launch {
                            loadImage(this@path, pixelOptions, imageView)
                        }
                    }


            }
        }
    }


    private fun loadImage(
        path: String,
        pixelOptions: PixelOptions?,
        imageView: ImageView
    ) = LoaderProxy.loadImage(
        imageView,
        path,
        pixelOptions,
        mainThreadScope
    )


    /**
     * A method to cancel image load request.
     * Invoking this method immediately cancels the load.
     */
    /*fun cancel() {
        imageLoad.cancel()
    }*/
}


