package com.iosdevlog.jj

import android.graphics.Bitmap
import android.graphics.Point
import iosdevlog.com.jump.utils.Utils

/**
 * Created iosdevlog on 2018/1/5.
 */
// 找白点,也就是连跳的中心点
object BoardCenterFinder {
    //设定中心点的颜色
    internal val red = 0xfa
    internal val green = 0xfa
    internal val blue = 0xfa

    internal var scaleX = 1f

    fun findBoardCenter(bitmap: Bitmap, startCenterPoint: Point): Point? {
        val width = bitmap.width
        val centerX = 0
        val centerY = 0
        val height = bitmap.height * 2 / 3
        var h = 200
        while (h < height && h < startCenterPoint.y) {
            for (w in 50 until width) {
                val color = bitmap.getPixel(w, h)
                val newColor = Utils.intToJumpColor(color)

                if (Math.abs(newColor.red - red) <= 5 &&
                        Math.abs(newColor.green - green) <= 5 &&
                        Math.abs(newColor.blue - blue) <= 5) {

                    val endCenter = findWhiteCenter(bitmap, w, h, startCenterPoint) ?: return null
                    if (startCenterPoint.x > bitmap.width / 2) {//在右边,所以如果找到的点也在右边就丢掉
                        if (endCenter.x > startCenterPoint.x) {
                            return Point(0, -1)
                        }
                    } else if (startCenterPoint.x < bitmap.width / 2) {
                        if (endCenter.x < startCenterPoint.x) {
                            return Point(0, -1)
                        }
                    }
                    return endCenter
                }
            }
            h++
        }
        return Point((centerX * scaleX).toInt(), (centerY - 1).toInt())
    }

    internal fun findWhiteCenter(bitmap: Bitmap, x: Int, y: Int, startCenterPoint: Point): Point? {
        val minX = x
        var maxX = x
        var maxY = y
        for (w in x until bitmap.width) {
            val color = bitmap.getPixel(w, y)
            val newColor = Utils.intToJumpColor(color)

            if (Math.abs(newColor.red - red) <= 5 &&
                    Math.abs(newColor.green - green) <= 5 &&
                    Math.abs(newColor.blue - blue) <= 5) {
                maxX = x + (w - x) / 2
            } else {
                break
            }
        }

        var h = y
        while (h < startCenterPoint.y) {
            val color = bitmap.getPixel(x, h)
            val newColor = Utils.intToJumpColor(color)
            if (Math.abs(newColor.red - red) <= 5 &&
                    Math.abs(newColor.green - green) <= 5 &&
                    Math.abs(newColor.blue - blue) <= 5) {
                maxY = h
            }
            h++
        }
        val centerY = y + (maxY - y) / 2
        return if (maxY - y < 18) {
            null
        } else Point((maxX * scaleX).toInt(), centerY.toInt())
    }
}

