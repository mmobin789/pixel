package io.pixel.validators

import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import io.pixel.loader.cache.disk.deleteIfExists
import io.pixel.utils.validators.FileValidator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.io.FileOutputStream


@RunWith(AndroidJUnit4::class)
class FileValidatorTest {

    private lateinit var context: Context

    private val validator = FileValidator

    @Before
    fun getContext() {
        context = InstrumentationRegistry.getInstrumentation().context
    }

    @Test
    fun validPath() {
        val file = runBlocking(Dispatchers.IO) {
            val FILENAME = "hello_file"
            val string = "hello world!"
            val testFile = File(context.filesDir, FILENAME)
            val fos = FileOutputStream(testFile)
            fos.write(string.toByteArray())
            fos.close()
            testFile
        }
        val path = validator.validateFile(file)
        println(path)
        file.deleteIfExists()
        assertNotNull(path)
    }

    @Test
    fun invalidPath() {
        val file = runBlocking(Dispatchers.IO) {
            val FILENAME = "hello_file"
            val string = "hello world!"
            val testFile = File(context.filesDir, FILENAME)
            val fos = FileOutputStream(testFile)
            fos.write(string.toByteArray())
            fos.close()
            testFile
        }
        val path = validator.validateFile(File("/data/user/0/io.pixel.android.test/files/hello_fil"))
        file.deleteIfExists()
        assertNull(path)
    }


    @Test
    fun nullFile() {
        val path = validator.validateFile(null)
        assertNull(path)
    }

}
