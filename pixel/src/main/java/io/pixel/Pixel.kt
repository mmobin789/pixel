package io.pixel

import android.widget.ImageView
import io.pixel.config.PixelOptions
import io.pixel.loader.LoaderProxy
import io.pixel.utils.validators.FileValidator
import io.pixel.utils.validators.UrlValidator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

/**
 * Pixel is a coroutine library to load and cache images.
 * Ideally, you shouldn't need multiple instances of this artifact but just in case you do.
 * @author Mobin Munir
 */
class Pixel  {
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
                    ?.apply url@{
                        mainThreadScope.launch {
                            LoaderProxy.loadUrl(imageView, this@url, pixelOptions, mainThreadScope).start()
                        }
                    }


            }
        }

        /**
         * Primary method to load images from files in device storage once the view hierarchy is rendered on the main thread.
         * If the UI thread is otherwise engaged the requests are automatically paused.
         * It is also safe to call this method from a background thread.
         * @param file The image file to load.
         * Default is null.
         * @see PixelOptions class.
         * @param imageView to load into.
         * @param pixelOptions Custom image load options.
         * Default are null.
         */
        @JvmStatic
        fun load(
            file: File?,
            pixelOptions: PixelOptions? = null,
            imageView: ImageView
        ): Pixel {

            return init().apply {
                FileValidator.validatePath(file)
                    ?.apply path@{
                        mainThreadScope.launch {
                            LoaderProxy.loadFile(
                                imageView,
                               this@path,
                                pixelOptions,
                                mainThreadScope
                            ).start()
                        }
                    }


            }
        }
    }


    /**
     * A method to cancel image load request.
     * Invoking this method immediately cancels the load.
     */
    /*fun cancel() {
        imageLoad.cancel()
    }*/
}


