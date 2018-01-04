package iosdevlog.com.jump.utils

import android.annotation.SuppressLint
import android.graphics.BitmapFactory
import android.graphics.Point
import android.os.Looper
import android.util.Log
import com.iosdevlog.jj.BoardCenterFinder.findBoardCenter
import com.iosdevlog.jj.ChessCenterFinder.findStartCenter
import com.iosdevlog.jj.ColorFilterFinder
import iosdevlog.com.jump.entities.JumpColor
import java.io.DataOutputStream
import java.io.IOException

/**
 * Created iosdevlog on 2018/1/4.
 */

object Utils {
    @SuppressLint("SdCardPath")
    val SCREENSHOT_LOCATION = "/sdcard/Download/screenshot.png"

    private var isJump = false
    private var runtime: Runtime = Runtime.getRuntime()
    private var process: Process? = null

    var firstPoint: Point? = Point(0, 0)
    var secondPoint: Point? = Point(0, 0)

    // 间隔 3000ms
    var screenshotInterval = 3000
    var screenshotPath = SCREENSHOT_LOCATION
    // 系数 k
    var resizedDistancePressTimeRatio = 1.37

    private val TAG = this.javaClass.simpleName
    private val WECHAT_JUMP_UI = "com.tencent.mm/.plugin.appbrand.ui.AppBrandUI"

    fun doWork(currentActivityName: String) {
        if (currentActivityName == WECHAT_JUMP_UI) {
            isJump = true
            playGame()
        } else {
            isJump = false
        }
    }

    private fun playGame() {
        Thread(Runnable {
            Looper.prepare()

            while (isJump) {
                screencap()

                try {
                    val bitmap = BitmapFactory.decodeFile(screenshotPath)
                    firstPoint = findStartCenter(bitmap)
                    secondPoint = findBoardCenter(bitmap, firstPoint!!)

                    var distance = if (secondPoint == null) 0 else distance(firstPoint!!, secondPoint!!)
                    if (secondPoint == null
                            || secondPoint!!.x == 0
                            || distance < 75
                            ||  Math.abs(secondPoint!!.x - firstPoint!!.x) < 38) {
                        secondPoint = ColorFilterFinder.findEndCenter(bitmap, firstPoint!!)
                        if (secondPoint == null) {
                            screencap()
                            continue
                        }
                    } else {
                        val colorfilterCenter = ColorFilterFinder.findEndCenter(bitmap, firstPoint!!)
                        if (Math.abs(secondPoint!!.x - colorfilterCenter!!.x) > 20) {
                            secondPoint = colorfilterCenter
                        }
                    }
                    Log.e(TAG, "firstPoint = [x=" + firstPoint!!.x + ",y=" + firstPoint!!.y
                            + "] , secondPoint = [x=" + secondPoint!!.x + ",y=" + secondPoint!!.y + "]")
                    ColorFilterFinder.updateLastShapeMinMax(bitmap, firstPoint!!, secondPoint!!)
                    distance = distance(firstPoint!!, secondPoint!!)
                    longPress((distance * resizedDistancePressTimeRatio).toInt())
                    sleep()
                } catch (e1: IOException) {
                    e1.printStackTrace()
                }
            }
        }).start()
    }

    private fun sleep() {
        try {
            Thread.sleep(screenshotInterval.toLong())// wait for screencap
        } catch (e1: InterruptedException) {
            e1.printStackTrace()
        }
    }

    // Runtime
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

    fun distance(a: Point, b: Point): Int {
        return Math.sqrt((a.x.toDouble() - b.x.toDouble()) * (a.x.toDouble() - b.x.toDouble()) + (a.y.toDouble() - b.y.toDouble()) * (a.y.toDouble() - b.y.toDouble())).toInt()
    }

    fun intToJumpColor(color: Int): JumpColor {
        val r = (color shr 16 and 0xff)
        val g = (color shr 8 and 0xff)
        val b = (color and 0xff)
        val a = (color shr 24 and 0xff)

        return JumpColor(r, g, b, a)
    }
}
