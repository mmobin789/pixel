package io.pixel.sample.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import io.pixel.android.Pixel
import org.json.JSONArray

class SampleViewModel : ViewModel() {

    private val jsonArrayLiveData = MutableLiveData<JSONArray>()
    private val randomPicsLiveData = MutableLiveData<Array<String>>()
    private var collectionsPageNo = 1

    private lateinit var sampleView: SampleView

    val jsonArrayObserver = Observer<JSONArray> {
        collectionsPageNo++
        sampleView.onPhotoCollectionReady(it)
    }

    val randomPicturesObserver = Observer<Array<String>> {
        sampleView.onRandomPicturesReady(it)
    }


    fun getJSONArrayLiveData() = jsonArrayLiveData as LiveData<JSONArray>
    fun getRandomPicsLiveData() = randomPicsLiveData as LiveData<Array<String>>


    fun attachView(sampleView: SampleView) {
        this.sampleView = sampleView
    }


    fun getCollections() {
        Pixel.loadJsonObject("https://jsonplaceholder.typicode.com/todos/1") {

        }

        Pixel.loadJsonArray("https://pastebin.com/raw/wgkJgazE") {
            jsonArrayLiveData.postValue(it)
            randomPicsLiveData.postValue(
                arrayOf(
                    it.getJSONObject(0).getJSONObject("urls").optString("full"),
                    it.getJSONObject(5).getJSONObject("urls").optString("full"),
                    it.getJSONObject(5).getJSONObject("urls").optString("full")
                )
            )
        }


    }

    interface SampleView {
        fun onPhotoCollectionReady(jsonArray: JSONArray)
        fun onRandomPicturesReady(path: Array<String>)
    }
}