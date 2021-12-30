package io.pixel.loader.load.request

import kotlinx.coroutines.Job

internal interface LoadRequest {
    val id: Int
    fun getRequest(): Job
    fun cancel(message: String = "Load Cancelled for $id")
    fun start()
}