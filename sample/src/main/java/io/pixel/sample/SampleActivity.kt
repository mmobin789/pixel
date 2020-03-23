package io.pixel.sample

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import io.pixel.android.Pixel
import io.pixel.android.config.PixelConfiguration
import io.pixel.android.config.PixelOptions
import io.pixel.sample.viewmodel.SampleViewModel
import kotlinx.android.synthetic.main.activity_sample.*
import org.json.JSONArray

class SampleActivity : AppCompatActivity(), SampleViewModel.SampleView {

    private val sampleViewModel: SampleViewModel by viewModels()
    private lateinit var rvAdapter: RVAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sample)
        btn.setOnClickListener {
            startActivity(Intent(it.context, UnsplashActivity::class.java))

        }
        loadImages()

        PixelConfiguration.clearImageCache()
        PixelConfiguration.clearDocumentCache()
        PixelConfiguration.setLoggingEnabled(true)
    }

    private fun loadImages() {

        sampleViewModel.run {
            attachView(this@SampleActivity)
            getJSONArrayLiveData().observe(this@SampleActivity, jsonArrayObserver)
            getRandomPicsLiveData().observe(this@SampleActivity, randomPicturesObserver)
            getCollections()
        }

        PixelConfiguration.setImageMemoryCacheSize(48000)
        PixelConfiguration.setJSONMemoryCacheSize(16000)

        Pixel.load(
            null,
            iv3,
            PixelOptions.Builder().setPlaceholderResource(R.drawable.ic_loading_android).build()
        )


        /*rv.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                val gridLayoutManager = recyclerView.layoutManager as LinearLayoutManager

                if (gridLayoutManager.findLastVisibleItemPosition() == rvAdapter.itemCount - 1) {

                    tvLoading.visibility = View.VISIBLE
                    sampleViewModel.getCollections()

                }


            }

            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {

            }
        })*/


    }

    override fun onPhotoCollectionReady(jsonArray: JSONArray) {
        tvLoading.visibility = View.GONE
        btn.visibility = View.VISIBLE
        rvAdapter = RVAdapter(jsonArray)
        rv.adapter = rvAdapter
        rvH.adapter = RVAdapterHorizontal(jsonArray)

    }

    override fun onRandomPicturesReady(path: Array<String>) {
        Pixel.load(path[0], iv)
        //same image loaded on iv1 and iv2 but iv1 was cancelled explicit.
        Pixel.load(path[1], iv1).cancel()

        SampleJavaLoad.load(path[1], R.drawable.ic_loading_android, iv3)

        SampleJavaLoad.load(path[0], iv2)


    }

    override fun onStop() {
        super.onStop()
        PixelConfiguration.clearCaches()
    }

}
