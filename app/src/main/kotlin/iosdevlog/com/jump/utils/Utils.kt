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
import android.graphics.Bitmap.CompressFormat
import android.graphics.Bitmap
import android.util.Range
import android.graphics.Bitmap.Config;
import android.os.Environment
import java.io.*


/**
 * Created iosdevlog on 2018/1/4.
 */

object Utils {
    @SuppressLint("SdCardPath")
    val SCREENSHOT_LOCATION = "/sdcard/Download/screenshot.png"

    private var isJump = false
    private var runtime: Runtime = Runtime.getRuntime()
    private var process: Process? = null

    private var firstPoint: Point? = Point(0, 0)
    private var secondPoint: Point? = Point(0, 0)

    // 间隔 3000ms
    private var screenshotInterval = 3000
    private var screenshotPath = SCREENSHOT_LOCATION
    // 系数 k
    var resizedDistancePressTimeRatio = 1.37

    private val TAG = this.javaClass.simpleName
    private val WECHAT_JUMP_UI = "com.tencent.mm/.plugin.appbrand.ui.AppBrandUI"

    // 开始工作了，进入跳一跳界面运行，否则没有动作
    fun doWork(currentActivityName: String) {
        if (currentActivityName == WECHAT_JUMP_UI) {
            isJump = true
            playGame()
        } else {
            isJump = false
        }
    }

    // 新开线程玩游戏，截图
    private fun playGame() {
        Thread(Runnable {
            Looper.prepare()

            while (isJump) {
                screencap()

                val bitmap = BitmapFactory.decodeFile(screenshotPath)
                if (bitmap != null) {
                    jump(bitmap)
                }
            }
        }).start()
    }

    // 分析棋子起跳点 和 落脚点
    private fun jump(bitmap: Bitmap) {
        try {
            firstPoint = findStartCenter(bitmap)
            secondPoint = findBoardCenter(bitmap, firstPoint!!)

            var distance = if (secondPoint == null) 0 else distance(firstPoint!!, secondPoint!!)
            if (secondPoint == null
                    || secondPoint!!.x == 0
                    || distance < 75
                    || Math.abs(secondPoint!!.x - firstPoint!!.x) < 38) {
                secondPoint = ColorFilterFinder.findEndCenter(bitmap, firstPoint!!)
                if (secondPoint == null) {
                    screencap()
                    return
                }
            } else {
                val colorfilterCenter = ColorFilterFinder.findEndCenter(bitmap, firstPoint!!)
                if (Math.abs(secondPoint!!.x - colorfilterCenter!!.x) > 20) {
                    secondPoint = colorfilterCenter
                }
            }

            ColorFilterFinder.updateLastShapeMinMax(bitmap, firstPoint!!, secondPoint!!)
            distance = distance(firstPoint!!, secondPoint!!)
            longPress((distance * resizedDistancePressTimeRatio).toInt())
        } catch (e1: IOException) {
            e1.printStackTrace()
        }
        sleep()
    }

    fun saveBitmapAsFile(name: String, bitmap: Bitmap): Boolean {
        val saveFile = File(Environment.DIRECTORY_DOWNLOADS, name)

        var saved = false
        val os: FileOutputStream?
        try {
            Log.d("FileCache", "Saving File To Cache " + saveFile.getPath())
            os = FileOutputStream(saveFile)
            bitmap.compress(CompressFormat.PNG, 100, os)
            os.flush()
            os.close()
            saved = true
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return saved
    }

    // wait for screencap
    private fun sleep() {
        try {
            Thread.sleep(screenshotInterval.toLong())
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
        val cmd = arrayOf("screencap -p " + screenshotPath)
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
