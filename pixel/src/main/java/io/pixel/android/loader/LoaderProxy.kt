package io.pixel.android.loader

import android.widget.ImageView
import io.pixel.android.config.PixelLog
import io.pixel.android.config.PixelOptions
import io.pixel.android.loader.download.JsonDownload
import io.pixel.android.loader.load.ImageLoad
import io.pixel.android.loader.load.LoadAdapter
import io.pixel.android.loader.load.LoadRequest
import io.pixel.android.loader.load.ViewLoad
import io.pixel.android.utils.createJsonArray
import io.pixel.android.utils.createJsonObject
import org.json.JSONArray
import org.json.JSONObject

/**
 * A proxy class representing a link to load process and it's states.
 */

internal object LoaderProxy {

    private val cancelledLoadRequests = ArrayList<LoadRequest>(300)


    fun loadJsonArray(
        path: String,
        observeInBackground: Boolean,
        onSuccess: (JSONArray) -> Unit

    ) = LoadAdapter.loadStringFromCache(path)?.also {
        PixelLog.debug(
            this@LoaderProxy.javaClass.simpleName,
            "Returned for path $path Cached JSONArray $it"
        )
        it.createJsonArray()?.also(onSuccess)
    }
        ?: run {
            val jsonDownload = JsonDownload(path)
            addDownload(jsonDownload)
            jsonDownload.startJsonArray(onSuccess, observeInBackground)
        }


    fun loadJsonObject(
        path: String,
        observeInBackground: Boolean = false,
        onSuccess: (JSONObject) -> Unit

    ) = LoadAdapter.loadStringFromCache(path)?.also {
        PixelLog.debug(
            this@LoaderProxy.javaClass.simpleName,
            "Returned for path $path Cached JSONObject $it"
        )
        it.createJsonObject()?.also(onSuccess)
    } ?: run {
        val jsonDownload = JsonDownload(path)
        addDownload(jsonDownload)
        jsonDownload.startJsonObject(onSuccess, observeInBackground)
    }


    fun loadImage(
        imageView: ImageView,
        path: String,
        pixelOptions: PixelOptions?

    ) {


        val viewLoad = ViewLoad(
            imageView.width,
            imageView.height,
            path
        )

        ImageLoad(viewLoad, imageView, pixelOptions).also { it.start() }


    }


    fun addCancelledLoad(loadRequest: LoadRequest) {
        cancelledLoadRequests.add(loadRequest)
        cancelLoads()
    }


    private fun cancelLoads() = cancelledLoadRequests.forEachIndexed { index, loadRequest ->
        loadRequest.cancel()
        cancelledLoadRequests.removeAt(index)

    }

    private fun addDownload(jsonDownload: JsonDownload) = LoadAdapter.addDownload(jsonDownload)


}