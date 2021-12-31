package io.pixel.internet.unsplash

import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.keenencharles.unsplash.models.Photo
import io.pixel.config.PixelConfiguration
import io.pixel.internet.R
import io.pixel.internet.unsplash.viewmodel.UnsplashViewModel
import kotlinx.android.synthetic.main.activity_sample.*
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.util.concurrent.TimeUnit

class UnsplashActivity : AppCompatActivity(), UnsplashViewModel.SampleView {

    private val unsplashViewModel: UnsplashViewModel by viewModels()
    private lateinit var rvUnsplashAdapter: RVUnsplashAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sample)
        iv.visibility = View.GONE
        iv1.visibility = View.GONE
        iv2.visibility = View.GONE
        rvH.visibility = View.GONE
        PixelConfiguration.setLoggingEnabled(true)
        PixelConfiguration.setAppVersion(2)
        PixelConfiguration.setDiskCacheSize(100)
        PixelConfiguration.setMemoryCacheSize(25)
        PixelConfiguration.setOkHttpClient(
            OkHttpClient.Builder()
                .connectTimeout(1, TimeUnit.MINUTES).readTimeout(1, TimeUnit.MINUTES)
                .addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
                .build()
        )
        loadImages()

        //   Thread { PixelConfiguration.clearDiskCache(this) }.start()


    }

    private fun loadImages() {
        unsplashViewModel.run {
           // unsplash.authorize(this@UnsplashActivity, "redirectURI", listOf(Scope.PUBLIC))
            attachView(this@UnsplashActivity)
            getCollectionsLiveData().observe(this@UnsplashActivity, collectionObserver)
            getErrorLiveData().observe(this@UnsplashActivity, errorObserver)

            getCollections()
        }

        rvUnsplashAdapter = RVUnsplashAdapter(mutableListOf())
        rv.adapter = rvUnsplashAdapter



        rv.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                val gridLayoutManager = recyclerView.layoutManager as LinearLayoutManager

                if (gridLayoutManager.findLastVisibleItemPosition() == rvUnsplashAdapter.itemCount - 1) {

                    tvLoading.visibility = View.VISIBLE
                    unsplashViewModel.getCollections()

                }


            }

            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {

            }
        })


    }

    override fun onPhotoCollectionReady(collection: MutableList<Photo>) {
        tvLoading.visibility = View.GONE
        rvUnsplashAdapter.addAll(collection)

    }

    override fun onPhotoCollectionError(error: String?) {
        tvLoading.run {
            text = error
            visibility = View.VISIBLE
        }
    }


}
