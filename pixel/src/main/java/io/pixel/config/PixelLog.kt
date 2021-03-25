package io.pixel.config

import android.util.Log
import io.pixel.Pixel

/**
 * An internal logger for pixel library.
 */
internal object PixelLog {
    private var loggingEnabled = false
    private const val tag = Pixel.TAG

    fun enabled(loggingEnabled: Boolean) {
        PixelLog.loggingEnabled = loggingEnabled
    }

    fun debug(tag: String = "", message: String) {
        if (loggingEnabled)
            Log.d("${PixelLog.tag} $tag", message)
    }

    fun error(tag: String = "", message: String) {
        if (loggingEnabled)
            Log.e("${PixelLog.tag} $tag", message)
    }

    fun warn(tag: String, message: String) {
        if (loggingEnabled)
            Log.w("${PixelLog.tag} $tag", message)
    }
}