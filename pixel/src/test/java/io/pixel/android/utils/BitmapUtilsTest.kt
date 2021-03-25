package io.pixel.android.utils

import io.pixel.utils.getUniqueIdentifier
import org.junit.Assert.assertTrue
import org.junit.Test


/**
 * Local unit test for Id generator.
 */

class BitmapUtilsTest {

    @Test
    fun validateUniqueIdGenerator() {
        val s = "Tu en fera' no priente de te comer`"
        val id = s.getUniqueIdentifier()
        assertTrue(id > 0)

    }

}
