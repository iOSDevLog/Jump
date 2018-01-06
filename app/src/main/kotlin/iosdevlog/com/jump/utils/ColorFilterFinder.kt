package com.iosdevlog.jj

import android.graphics.Bitmap
import android.graphics.Point
import android.support.constraint.solver.widgets.Rectangle
import android.util.Log
import iosdevlog.com.jump.entities.JumpColor
import iosdevlog.com.jump.utils.Utils

/**
 * Created iosdevlog on 2018/1/5.
 */

// 直接根据色差来定位下一个中心点
object ColorFilterFinder {
    private var TAG = ColorFilterFinder.javaClass.simpleName
    private var bgColor: JumpColor = JumpColor(255, 0, 0, 255)

    private var startCenterPoint: Point = Point(0, 0)

    private var lastShapeMinMax = 150

    fun findEndCenter(bitmap: Bitmap, startCenterPoint: Point): Point? {
        ColorFilterFinder.startCenterPoint = startCenterPoint
        val color = bitmap.getPixel(540, 700)
        bgColor = Utils.intToJumpColor(color)

        val tmpStartCenterPoint: Point
        val tmpEndCenterPoint: Point

        // 排除小人所在的位置的整个柱状区域检测,为了排除某些特定情况的干扰.
        val rect = Rectangle()
        rect.setBounds((startCenterPoint.x - lastShapeMinMax / 2), 0, lastShapeMinMax, startCenterPoint.y)

        val lastColor = bgColor
        for (y in 600 until startCenterPoint.y) {
            for (x in 10 until bitmap.width) {
                if (rect.contains(x, y)) {
                    continue
                }
                val newColor = Utils.intToJumpColor(bitmap.getPixel(x, y))
                if ((Math.abs(newColor.red - lastColor.red)
                        + Math.abs(newColor.blue - lastColor.blue)
                        + Math.abs(newColor.green - lastColor.green)) >= 20 || (Math.abs(newColor.red - lastColor.red) >= 15
                        || Math.abs(newColor.blue - lastColor.blue) >= 15
                        || Math.abs(newColor.green - lastColor.green) >= 15)) {
                    Log.e(TAG, "y = " + y + " x = " + x)
                    tmpStartCenterPoint = findStartCenterPoint(bitmap, x, y)
                    Log.e(TAG, tmpStartCenterPoint.toString())
                    tmpEndCenterPoint = findEndCenterPoint(bitmap, tmpStartCenterPoint)
                    return Point(tmpStartCenterPoint.x, (tmpEndCenterPoint.y + tmpStartCenterPoint.y) / 2)
                }
            }
        }
        return null
    }

    // 查找新方块/圆的有效结束最低位置
    private fun findEndCenterPoint(bitmap: Bitmap, tmpStartCenterPoint: Point): Point {
        val startColor = Utils.intToJumpColor(bitmap.getPixel(tmpStartCenterPoint.x, tmpStartCenterPoint.y))
        val centX = tmpStartCenterPoint.x
        var centY = tmpStartCenterPoint.y
        var i = tmpStartCenterPoint.y
        while (i < bitmap.height && i < startCenterPoint.y - 10) {
            // -2是为了避开正方体的右边墙壁的影响
            val newColor = Utils.intToJumpColor(bitmap.getPixel(tmpStartCenterPoint.x, i))
            if (Math.abs(newColor.red - startColor.red) <= 8
                    && Math.abs(newColor.green - startColor.green) <= 8
                    && Math.abs(newColor.blue - startColor.blue) <= 8) {
                centY = i
            }
            i++
        }
        if (centY - tmpStartCenterPoint.y < 40) {
            centY = centY + 40
        }
        if (centY - tmpStartCenterPoint.y > 230) {
            centY = tmpStartCenterPoint.y + 230
        }
        return Point(centX, centY)
    }

    // 查找下一个方块的最高点的中点
    private fun findStartCenterPoint(bitmap: Bitmap, x: Int, y: Int): Point {
        val lastColor = Utils.intToJumpColor(bitmap.getPixel(x - 1, y))
        var centX = x
        for (i in x until bitmap.width) {
            val newColor = Utils.intToJumpColor(bitmap.getPixel(i, y))
            if ((Math.abs(newColor.red - lastColor.red) + Math.abs(newColor.blue - lastColor.blue)
                    + Math.abs(newColor.green - lastColor.green)) >= 20 || (Math.abs(newColor.red - lastColor.red) >= 15
                    || Math.abs(newColor.blue - lastColor.blue) >= 15
                    || Math.abs(newColor.green - lastColor.green) >= 15)) {
                centX = x + (i - x) / 2
            } else {
                break
            }
        }
        return Point(centX, y)
    }

    private fun like(a: JumpColor, b: JumpColor): Boolean {
        return !((Math.abs(a.red - b.red) + Math.abs(a.blue - b.blue)
                + Math.abs(a.green - b.green)) >= 20 || (Math.abs(a.red - b.red) >= 15 || Math.abs(a.blue - b.blue) >= 15
                || Math.abs(a.green - b.green) >= 15))
    }

    fun updateLastShapeMinMax(bitmap: Bitmap, first: Point, second: Point) {
        if (first.x < second.y) {
            for (x in second.x until bitmap.width) {
                val newColor = Utils.intToJumpColor(bitmap.getPixel(x, second.y))
                if (like(newColor, bgColor)) {
                    lastShapeMinMax = Math.max((x - second.x) * 1.5, 150.0).toInt()
                    break
                }
            }
        } else {
            for (x in second.x downTo 10) {
                val newColor = Utils.intToJumpColor(bitmap.getPixel(x, second.y))
                if (like(newColor, bgColor)) {
                    lastShapeMinMax = Math.max((second.x - x) * 1.5, 150.0).toInt()
                    break
                }
            }
        }
    }
}
