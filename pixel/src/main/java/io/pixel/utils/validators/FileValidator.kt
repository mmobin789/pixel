package io.pixel.utils.validators

import io.pixel.config.PixelLog
import okio.IOException
import java.io.File

object FileValidator {
    /**
     * This method validates the file by evaluating its path and the state of file itself.
     * @param file file to evaluate.
     * @return if the file is valid by all means, it's path.
     */
    fun validateFile(file: File?): String? = file?.run {

        if (!exists()) {
            logError(message = "Image file doesn't exist with name of $name.$extension")
            return null
        }

        if (!canRead()) {
            logError(message = "Path isn't readable for image file $name")
            return null
        }

        try {
            if (canonicalPath.isBlank()) {
                logError(message = "Path isn't valid for image file $name")
                return null
            }
        } catch (e: IOException) {
            e.printStackTrace()
            return null
        }


        path
    }


    private fun logError(message: String) = PixelLog.error(message = message)

}