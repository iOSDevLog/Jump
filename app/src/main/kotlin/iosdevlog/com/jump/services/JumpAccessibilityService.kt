package iosdevlog.com.jump.services

import android.accessibilityservice.AccessibilityService
import android.content.ComponentName
import android.content.pm.PackageManager
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import iosdevlog.com.jump.utils.Utils

/**
 * Created iosdevlog on 2018/1/5.
 */
class JumpAccessibilityService : AccessibilityService() {
    private val TAG = this.javaClass.simpleName

    override fun onInterrupt() {
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        try {
            val componentName = ComponentName(
                    event?.packageName.toString(),
                    event?.className.toString()
            )

            packageManager.getActivityInfo(componentName, 0)
            val currentActivityName = componentName.flattenToShortString()
            Utils.doWork(currentActivityName)
        } catch (e: PackageManager.NameNotFoundException) {
            Log.e(TAG, "onAccessibilityEvent:" + e.toString())
        }
    }

}
