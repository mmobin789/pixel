package io.pixel.android.loader.download

import io.pixel.android.config.PixelLog
import io.pixel.android.utils.createJsonArray
import io.pixel.android.utils.createJsonObject
import io.pixel.android.utils.getUniqueIdentifier
import io.pixel.pixel.loader.load.LoadAdapter
import kotlinx.coroutines.*
import org.json.JSONArray
import org.json.JSONObject

internal data class JsonDownload(val url: String) {

    private lateinit var downloadJob: Job

    val id = url.getUniqueIdentifier()

    override fun equals(other: Any?): Boolean {

        if (other == null || other !is JsonDownload)
            return false

        return other.id == id
    }

    override fun hashCode(): Int {
        return id
    }
/*
    fun cancel(message: String? = null) {

        if (downloadJob.isActive && !isCancelled()) {
            downloadJob.cancel(CancellationException(message))
            PixelLog.debug(javaClass.simpleName, "JSON Download for id = $id is explicitly cancelled")
        }
    }*/

    /* fun isCancelled() = downloadJob.isCancelled*/


    fun startJsonArray(
        onSuccess: (JSONArray) -> Unit,
        observeInBackground: Boolean
    ) {
        PixelLog.debug(
            javaClass.simpleName,
            "JSONArray Download no = ${hashCode()} started for $url"
        )


        downloadJob = GlobalScope.launch(Dispatchers.IO) {
            LoadAdapter.downloadString(
                url
            )?.also {
                var dispatcher: CoroutineDispatcher = Dispatchers.Main
                if (observeInBackground)
                    dispatcher = Dispatchers.IO

                withContext(dispatcher) {
                    it.createJsonArray()?.also(onSuccess)
                }
            }
        }
    }


    fun startJsonObject(
        onSuccess: (JSONObject) -> Unit,
        observeInBackground: Boolean = false
    ) {

        PixelLog.debug(
            javaClass.simpleName,
            "JSONObject Download no = ${hashCode()} started for $url"
        )


        downloadJob = GlobalScope.launch(Dispatchers.IO) {
            LoadAdapter.downloadString(
                url
            )?.also {
                var dispatcher: CoroutineDispatcher = Dispatchers.Main
                if (observeInBackground)
                    dispatcher = Dispatchers.IO

                withContext(dispatcher) {
                    it.createJsonObject()?.also(onSuccess)
                }
            }
        }
    }

}