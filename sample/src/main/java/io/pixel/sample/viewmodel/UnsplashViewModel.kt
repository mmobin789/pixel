package io.pixel.sample.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import com.keenencharles.unsplash.Unsplash
import com.keenencharles.unsplash.models.Collection

class UnsplashViewModel : ViewModel() {

     val unsplash = Unsplash("LKKIM-pzVTHV1XabCIik1V4LHf-Zn0x7t7lzv8-Q9J4","x3edkPc07EhbjAKYKEJJ7L4iU09x0Dao7WVIVNmrZBM")

    private val collectionsLiveData = MutableLiveData<MutableList<Collection>>()
    private val errorLiveData = MutableLiveData<String?>()
    private var loading = false
    private var collectionsPageNo = 1

    private lateinit var sampleView: SampleView

    val collectionObserver = Observer<MutableList<Collection>> {
        collectionsPageNo++
        sampleView.onPhotoCollectionReady(it)
    }

    val errorObserver = Observer<String?> {
        sampleView.onPhotoCollectionError(it)
    }


    fun getCollectionsLiveData() = collectionsLiveData as LiveData<MutableList<Collection>>

    fun getErrorLiveData() = errorLiveData as LiveData<String?>


    fun attachView(sampleView: SampleView) {
        this.sampleView = sampleView
    }


    fun getCollections(
        perPageItems: Int = 20
    ) {
        if (loading)
            return

        loading = true

        unsplash.collections.get(
            collectionsPageNo,
            perPageItems,
            {
                loading = false
                collectionsLiveData.postValue(it.toMutableList())


            },
            {
                errorLiveData.postValue(it)
            })



}

interface SampleView {
    fun onPhotoCollectionReady(collection: MutableList<Collection>)
    fun onPhotoCollectionError(error: String?)
}
}