package io.pixel.loader.load.request

import android.graphics.Bitmap

internal interface ImageLoadRequest {
    val id: Int
    fun cancel(message: String = "Load Cancelled for $id")
    fun bitmap(): Bitmap?
}
