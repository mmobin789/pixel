package io.pixel.sample

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.keenencharles.unsplash.api.Scope
import com.keenencharles.unsplash.api.UnsplashResource
import com.keenencharles.unsplash.models.Collection
import io.pixel.config.PixelConfiguration
import io.pixel.sample.viewmodel.UnsplashViewModel
import kotlinx.android.synthetic.main.activity_sample.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.util.concurrent.TimeUnit

class UnsplashActivity : AppCompatActivity(), UnsplashViewModel.SampleView {

    private val unsplashViewModel: UnsplashViewModel by viewModels()
    private lateinit var rvUnsplashAdapter: RVUnsplashAdapter
    //private val redirectURI = "example://androidunsplash/callback"

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

 //       handleAuthCallback()

        //   Thread { PixelConfiguration.clearDiskCache(this) }.start()


    }

  /*  private fun handleAuthCallback() {
        val data = intent.data
        val code = data?.query?.replace("code=", "")
        code?.let { fetchToken(it) }
    }

    private fun fetchToken(code: String) {
        unsplashViewModel.unsplash.run {
            getToken("x3edkPc07EhbjAKYKEJJ7L4iU09x0Dao7WVIVNmrZBM", redirectURI, code, {
                setToken(it.accessToken)
                loadImages()
            }, {
                onPhotoCollectionError(it)
            })
        }
    }*/

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

    override fun onPhotoCollectionReady(collection: MutableList<Collection>) {
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
