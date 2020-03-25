package io.pixel.android

import android.widget.ImageView
import io.pixel.android.config.PixelLog
import io.pixel.android.config.PixelOptions
import io.pixel.android.loader.LoaderProxy
import io.pixel.android.loader.load.LoadRequest
import io.pixel.android.utils.ValidatorUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject

/**
 * Pixel is a library to load and cache images.
 * Optional features include loading JSON Object and Arrays from open urls.
 * @author Mobin Munir
 */
class Pixel private constructor() {
    private lateinit var loadRequest: LoadRequest

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
                    loadRequest = LoadRequest(
                        GlobalScope.launch(Dispatchers.Main) {
                            imageView.post {
                                loadImage(this@path, pixelOptions, imageView)
                            }
                        })

                } ?: pixelOptions?.run {
                    if (getPlaceholderResource() != 0)
                        imageView.setImageResource(getPlaceholderResource())
                }


            }
        }

        /**
         * Primary method to load JSON Object async.
         * It is also safe to call this method from a background thread.
         * @param path The url.
         * @param resultInBackground passing true will invoke the callback in a background thread. (Default is false)
         * @param callback a callback for JSONObject.
         */
        @JvmStatic
        fun loadJsonObject(
            path: String?,
            resultInBackground: Boolean = false,
            callback: ((JSONObject) -> Unit)

        ) {

            ValidatorUtils.validateURL(path)?.apply {

                LoaderProxy.loadJsonObject(this, resultInBackground) {
                    PixelLog.debug(TAG, it.toString())
                    callback(it)
                }


            }
        }

        /**
         * Primary method to load JSON Array async.
         * It is also safe to call this method from a background thread.
         * @param path The url.
         * @param resultInBackground passing true will invoke the callback in a background thread. (Default is false)
         * @param callback a callback for JSONArray.
         */
        @JvmStatic
        fun loadJsonArray(
            path: String?,
            resultInBackground: Boolean = false,
            callback: ((JSONArray) -> Unit)
        ) {

            ValidatorUtils.validateURL(path)?.apply {
                LoaderProxy.loadJsonArray(this, resultInBackground) {
                    PixelLog.debug(TAG, it.toString())
                    callback(it)


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
