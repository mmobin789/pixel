package io.pixel.android.utils

import org.junit.Assert.assertEquals
import org.junit.Test


/**
 * Local unit test for Id generator.
 * See more in DocumentUtils.kt file.
 */

class DocumentUtilsTest {

    @Test
    fun validateUniqueIdGenerator() {
        val s = "Tu en fera' no priente de te comer`"
        val id = s.getUniqueIdentifier()
        assertEquals(true, id > 0)

    }

}
