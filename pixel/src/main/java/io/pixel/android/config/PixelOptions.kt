package io.pixel.android.config


/**
 * PixelOptions allow to customize each load.
 * @author Mobin Munir
 */
class PixelOptions private constructor() {
    private var width = 0
    private var height = 0
    private var placeHolderResource = 0

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


    class Builder {
        private val imageLoaderOptions = PixelOptions()

        /** Sets a placeholder resource for load.
         * @param resId The resource Id of drawable to use as a placeholder for load.
         */
        fun setPlaceholderResource(resId: Int) = imageLoaderOptions.let {
            it.placeHolderResource = resId
            this
        }

        /**
         * Sets custom width and height in pixels for image to load.
         * @param width in pixels.
         * @param height in pixels.
         *
         * Note: width and height less than ImageView's width and height are ignored.
         */
        fun setImageSize(width: Int, height: Int) =
            imageLoaderOptions.let {
                it.width = width
                it.height = height
                this
            }

        /**
         * @return PixelOptions
         */
        fun build() = imageLoaderOptions
    }
}