package io.pixel.android

import android.widget.ImageView
import io.pixel.android.config.PixelOptions
import io.pixel.android.loader.LoaderProxy
import io.pixel.android.loader.load.LoadRequest
import io.pixel.android.utils.ValidatorUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Pixel is a library to load and cache images.
 * @author Mobin Munir
 */
class Pixel private constructor() {

    private lateinit var loadRequest: LoadRequest
    private val uiScope = CoroutineScope(Dispatchers.Main.immediate)

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
         * Primary method to load images once the view hierarchy is rendered on the main thread.
         * If the UI thread is otherwise engaged the requests are automatically paused.
         * It is also safe to call this method from a background thread.
         * @param path The image url.
         * @param imageView The image view to load into.
         * @param pixelOptions Custom Image load request option.
         */
        @JvmStatic
        fun load(
            path: String?,
            imageView: ImageView,
            pixelOptions: PixelOptions? = null
        ): Pixel {

            return init().apply {
                ValidatorUtils.validateURL(path)?.apply path@{

                    loadRequest = LoadRequest(uiScope.launch(Dispatchers.Main.immediate) {
                        imageView.post {
                            loadImage(this@path, pixelOptions, imageView)
                        }


                    })

                }


            }
        }
    }


    private fun loadImage(
        path: String,
        pixelOptions: PixelOptions?,
        imageView: ImageView
    ) {

        LoaderProxy.loadImage(
            imageView,
            path,
            pixelOptions
        )

    }

    /**
     * A method to cancel image load request.
     * Invoking this method immediately cancels the load.
     */
    fun cancel() {
        LoaderProxy.addCancelledLoad(loadRequest)
    }
}


