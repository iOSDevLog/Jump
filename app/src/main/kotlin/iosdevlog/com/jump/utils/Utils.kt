package iosdevlog.com.jump.utils

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.support.v4.app.ActivityCompat.requestPermissions
import android.support.v4.app.ActivityCompat.startActivities
import android.support.v4.content.ContextCompat
import java.io.DataOutputStream

/**
 * Created by e on 2018/1/4.
 */

object Utils {
    @SuppressLint("SdCardPath")
    val SCREENSHOT_LOCATION = "/sdcard/Download/screenshot.png"

    private var runtime: Runtime = Runtime.getRuntime()
    private var process: Process? = null

    private fun adbCommand(commands: Array<String>) {
        try {
            process = runtime.exec("su")
            val os = DataOutputStream(process?.outputStream)

            os.let {
                for (command in commands) {
                    os.write(command.toByteArray())
                    os.writeBytes("\n")
                    os.flush()
                }
                os.writeBytes("exit\n")
                os.flush()
                process?.waitFor()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // ms
    fun longPress(timeInteval: Int) {
        val cmd = arrayOf("input touchscreen swipe 170 187 170 187 " + timeInteval)
        adbCommand(cmd)
    }

    fun screencap() {
        val cmd = arrayOf("screencap -p " + SCREENSHOT_LOCATION)
        adbCommand(cmd)
    }
}
