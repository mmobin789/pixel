package io.pixel.android.url


import androidx.test.ext.junit.runners.AndroidJUnit4
import io.pixel.android.utils.validators.UrlValidator
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class Url {

    @Test
    fun validURLHttp() {
        Assert.assertNotNull(UrlValidator.validateURL("http://google.com"))
    }

    @Test
    fun validURLHttps() {
        Assert.assertNotNull(UrlValidator.validateURL("https://google.com"))
    }

    @Test
    fun invalidURL() {
        val invalids = arrayOf("google.co", "g.com", "https//google.c", "http://google",""," ","https:/null.co/","https:/null.co",null)
        Assert.assertTrue(invalids.none {
            UrlValidator.validateURL(it) != null
        })
    }
}