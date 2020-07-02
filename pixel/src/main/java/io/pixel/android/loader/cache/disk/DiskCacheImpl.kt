package io.pixel.android.loader.cache.disk

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

internal object DiskCacheImpl {
    private val mIOScope = CoroutineScope(Dispatchers.IO)

    @Volatile
    private var mDiskLRUCache: DiskLRUCache? = null


    fun getInstance(context: Context, appVersion: Int = 1) {
        if (mDiskLRUCache == null) {
            mIOScope.launch {
                synchronized(this@DiskCacheImpl) {
                    //todo working here
                    mDiskLRUCache =
                        DiskLRUCache.open(context.cacheDir, appVersion, 1, 50 * 1024 * 1024)
                }
            }
        }


    }
}