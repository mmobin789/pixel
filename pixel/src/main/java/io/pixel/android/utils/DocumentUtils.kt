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