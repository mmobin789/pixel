package io.pixel.pixel.loader.load

import io.pixel.android.utils.getUniqueIdentifier

internal data class ViewLoad(var width: Int, var height: Int, val path: String) {


    private val id = path.getUniqueIdentifier()

    override fun equals(other: Any?): Boolean {

        if (other == null || other !is ViewLoad)
            return false

        return other.hashCode() == hashCode()
    }

    /**
     * Creates a unique id for each view load.
     * @return id of view load.
     * @author mobin munir
     */
    override fun hashCode(): Int {
        return width + height + id
    }


}