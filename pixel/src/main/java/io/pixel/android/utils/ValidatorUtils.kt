package io.pixel.android.utils

import android.util.Patterns
import io.pixel.android.config.PixelLog

internal object ValidatorUtils {
    fun validateURL(url: String?, debugging: Boolean = true): String? {
        if (url.isNullOrBlank()) {
            if (debugging)
                PixelLog.error(message = "Null or blank URL")
            return null
        }
        if (!Patterns.WEB_URL.matcher(url).matches()) {
            if (debugging)
                PixelLog.error(message = "Invalid URL")
            return null
        }
        return url
    }
}