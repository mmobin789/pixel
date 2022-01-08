package io.pixel.loader.load.type

import android.graphics.Bitmap
import io.pixel.loader.load.request.ImageLoadRequest
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.job

internal class CachedImageLoadRequest(
    private val bitmap: Bitmap,
    coroutineScope: CoroutineScope,
    override val id: Int
) :
    ImageLoadRequest {

    private val loadJob = coroutineScope.coroutineContext.job

    override fun cancel(message: String) = loadJob.cancel(CancellationException(message))

    override fun isRunning(): Boolean {
        return loadJob.isActive
    }

    override fun bitmap(): Bitmap {
        return bitmap
    }
}
