package io.pixel.loader.load.request.download

import kotlinx.coroutines.Job
import java.lang.ref.WeakReference

class ImageLoadInfo(downloadJob: Job) {

    private val downloadJob = WeakReference(downloadJob)

    fun getImageLoadJob() = downloadJob.get()
}
