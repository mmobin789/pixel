package io.pixel.sample

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.random.Random

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class SampleActivityTest {

    @Rule
    @JvmField
    val mActivityTestRule = ActivityTestRule(SampleActivity::class.java)

    @Test
    fun testImageLoadInRecyclerView() {
        val randomIndex = Random.nextInt(10)
        onView(withId(R.id.rv)).perform(
            RecyclerViewActions.scrollToPosition<RVAdapter.VH>(randomIndex)
        )

    }

    @Test
    fun testImageLoadCancelInRecyclerView() {
        val randomIndex = Random.nextInt(10)

        onView(withId(R.id.rv)).perform(
            RecyclerViewActions.actionOnItemAtPosition<RVAdapter.VH>(
                randomIndex,
                click()
            )
        )
    }
}
