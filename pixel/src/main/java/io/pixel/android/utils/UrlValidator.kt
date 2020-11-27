package io.pixel.android.utils

import android.util.Patterns
import io.pixel.android.config.PixelLog

internal object UrlValidator {
    fun validateURL(url: String?): String? {
        if (url.isNullOrBlank()) {
            logError(message = "Null or blank URL")
            return null
        }

        if (!Patterns.WEB_URL.matcher(url).matches()) {
            logError(message = "Invalid URL")
            return null
        }

        if (!beginsWithHttp(url)) {
            logError(message = "URL must begin with http:// or https://")
            return null
        }

        return url
    }

    private fun beginsWithHttp(url: String) =
        url.startsWith("http://") || url.startsWith("https://")


    private fun logError(message: String) = PixelLog.error(message = message)
}