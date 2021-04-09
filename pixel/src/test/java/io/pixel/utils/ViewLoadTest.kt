package io.pixel.utils

import io.pixel.loader.load.ViewLoad
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Local unit tests for view load.
 * View load represents each image load request for a view.
 * @see ViewLoad
 */
class ViewLoadTest {

    private val path = "https://www.facebook.com/"
    private val viewLoad = ViewLoad(200, 100, path)

    @Test
    fun validateViewLoadId() {
        assertEquals(2656, viewLoad.hashCode())
    }

    @Test
    fun validateViewLoadEqual() {
        assertEquals(true, viewLoad == ViewLoad(200, 100, path))
    }

    @Test
    fun validatePath() {
        assertEquals(path, viewLoad.path)
    }

    @Test
    fun validateViewWidth() {
        assertEquals(200, viewLoad.width)
    }

    @Test
    fun validateViewHeight() {
        assertEquals(100, viewLoad.height)
    }
}