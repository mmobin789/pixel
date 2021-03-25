package io.pixel.utils.validators

import io.pixel.config.PixelLog
import java.io.File

object FileValidator {
    fun validateFile(file: File?): String? {
       /* if (file.isNullOrBlank()) {
            logError(message = "Null or blank path")
            return null
        }*/

       /* if (!Patterns..matcher(url).matches()) {
            UrlValidator.logError(message = "Invalid URL")
            return null
        }

        if (!UrlValidator.beginsWithHttp(url)) {
            UrlValidator.logError(message = "URL must begin with http:// or https://")
            return null
        }*/

        return file?.path
    }

    private fun logError(message: String) = PixelLog.error(message = message)

}