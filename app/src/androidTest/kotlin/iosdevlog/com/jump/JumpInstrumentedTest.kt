package iosdevlog.com.jump

import android.graphics.BitmapFactory
import android.os.Environment
import android.support.test.InstrumentationRegistry
import android.support.test.runner.AndroidJUnit4
import iosdevlog.com.jump.utils.Utils

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.*
import org.junit.BeforeClass
import java.io.File


/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class JumpInstrumentedTest {
    companion object {
        @BeforeClass
        @JvmStatic
        fun setup() {

        }
    }

    @Test
    fun useAppContext() {
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getTargetContext()
        assertEquals("iosdevlog.com.jump", appContext.packageName)
    }

    @Test
    fun screencapExist() {
        Utils.screencap()
        val file = File(Utils.SCREENSHOT_LOCATION)

        assertNotNull(file)
        assert(file.exists())
    }

    @Test
    fun jump() {
        // open WeChat Jump First, 1 ms
        Utils.longPress(1)
    }
}
