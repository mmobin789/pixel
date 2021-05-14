package io.pixel.utils.validators

import io.pixel.config.PixelLog
import java.io.File

object FileValidator {
    /**
     * This method fixes the file path to exactly match the (android.net) URI to avoid file not found.
     * @param file file to evaluate.
     * @return if the file is valid by all means, it's path.
     */
    fun validatePath(file: File?): String? = file?.run {
        path.replaceFirst("/", "//")
    }


    private fun logError(message: String) = PixelLog.error(message = message)

}