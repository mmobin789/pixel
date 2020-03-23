package io.pixel.android.utils

import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject


internal fun String.getUniqueIdentifier(): Int {
    var sum = 0
    map {
        sum += it.toInt()
    }
    return sum
}

internal fun String.createJsonArray(): JSONArray? = try {
    JSONArray(this)
} catch (e: JSONException) {
    e.printStackTrace()
    null
}


internal fun String.createJsonObject(): JSONObject? = try {
    JSONObject(this)
} catch (e: JSONException) {
    e.printStackTrace()
    null
}
