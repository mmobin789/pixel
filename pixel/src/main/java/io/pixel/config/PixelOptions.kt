package io.pixel.config

import okhttp3.OkHttpClient
import okhttp3.Request

/**
 * PixelOptions allow to customize each load.
 * @author Mobin Munir
 */
class PixelOptions private constructor() {
    private var width = 0
    private var height = 0
    private var placeHolderResource = 0
    private var imageFormat = ImageFormat.PNG
    private var okHttpClient: OkHttpClient? = null
    private var request: Request? = null

    enum class ImageFormat {
        JPEG, PNG
    }

    /**
     * @return the requested image width.
     */
    fun getRequestedImageWidth() = width

    /**
     * @return the requested image height.
     */
    fun getRequestedImageHeight() = height

    /**
     * @return the placeholder resource to display when loading images.
     */
    fun getPlaceholderResource() = placeHolderResource

    /**
     *@return the image format to apply after downloading image.
     */
    fun getImageFormat() = imageFormat

    /**
     * @return the custom request null if not provided.
     * Uses default GET request on null.
     */
    fun getRequest() = request

    class Builder {
        private val pixelOptions = PixelOptions()

        /** Sets a placeholder resource for load.
         * @param placeHolderResource The resource Id of drawable to use as a placeholder for load.
         */
        fun setPlaceholderResource(placeHolderResource: Int) = pixelOptions.let {
            it.placeHolderResource = placeHolderResource
            this
        }

        /**
         * Sets custom width and height in pixels for image to load.
         * @param width in pixels.
         * @param height in pixels.
         */
        fun setImageSize(width: Int, height: Int) = pixelOptions.let {
            it.width = width
            it.height = height
            this
        }

        /**
         * Set the downloaded image format to JPEG/PNG
         * Defaults to PNG.
         */
        fun setImageFormat(imageFormat: ImageFormat) = pixelOptions.let {
            it.imageFormat = imageFormat
            this
        }

        /**
         * This allows for full control on request for each image load.
         * A custom configured request can be supplied to load images at the cost of ignoring original request.
         *
         * Note: This means the necessary url provided for this request will effectively ignore the provided url for Pixel.load() method.
         * Default method is GET.
         */
        fun setRequest(request: Request) = pixelOptions.let {
            it.request = request
            this
        }

        /**
         * @return PixelOptions
         */
        fun build() = pixelOptions
    }
}
