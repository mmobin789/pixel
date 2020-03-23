package io.pixel.android.loader.cache.memory

internal object MemoryCacheImpl {
    fun forBitmap() = BitmapMemoryCache.getInstance()
    fun forDocument() = DocumentMemoryCache.getInstance()
}
