package io.pixel.android.loader.cache.disk

import java.io.Closeable
import java.io.File
import java.io.IOException

fun Closeable.closeQuietly() {
    try {
        close()
    } catch (rethrown: RuntimeException) {
        throw rethrown
    } catch (ignored: Exception) {
    }
}

@Throws(IOException::class)
fun File.deleteIfExists() {
    if (exists() && !delete()) {
        throw IOException()
    }
}

@Throws(IOException::class)
fun File.renameTo(to: File, deleteDestination: Boolean) {
    if (deleteDestination) {
        to.deleteIfExists()
    }
    if (!renameTo(to)) {
        throw IOException()
    }
}

/**
 * Deletes the contents of `dir`. Throws an IOException if any file
 * could not be deleted, or if `dir` is not a readable directory.
 */
@Throws(IOException::class)
fun File.deleteDirectory() {
    val files = listFiles() ?: throw IOException("Not a readable directory: $this")
    files.forEach { file ->
        if (file.isDirectory) {
            file.deleteDirectory()
        }
        if (!file.delete()) {
            throw IOException("Failed to delete file: $file")
        }
    }
}