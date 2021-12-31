package io.pixel.utils

import io.pixel.loader.load.ViewLoad
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

/**
 * Local unit tests for view load.
 * View load represents each image load request for a view.
 * @see ViewLoad
 */
class ViewLoadTest {

    private val path = "https://www.facebook.com/"
    private val randomWidth = Random.nextInt()
    private val randomHeight = Random.nextInt()
    private val viewLoad = ViewLoad(randomWidth, randomHeight, path)

    @Test
    fun validateViewLoadId() {
        assertEquals(randomWidth +randomHeight + viewLoad.path.getUniqueIdentifier() , viewLoad.hashCode())
    }

    @Test
    fun validateViewLoadEqual() {
        assertTrue(viewLoad == ViewLoad(randomWidth, randomHeight, path))
    }

    @Test
    fun validatePath() {
        assertEquals(path, viewLoad.path)
    }

    @Test
    fun validateViewWidth() {
        assertEquals(randomWidth, viewLoad.width)
    }

    @Test
    fun validateViewHeight() {
        assertEquals(randomHeight, viewLoad.height)
    }
}