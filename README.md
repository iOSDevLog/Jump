# Jump
微信小游戏 跳一跳 Android 插件

视频：<https://weibo.com/tv/v/FD5JIDeTO?fid=1034:be8ac5577f9d183858300b1b18a0c782>

现在的微信跳一跳小游戏都是通过 PC 端破解的，于是我就写了一个只用 Android 手机就能破解的插件。

如果10秒还不跳，重新回到插件主页再回到跳一跳试试 。

![screenshot](https://upload-images.jianshu.io/upload_images/910914-3bb86889546e700b.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/700)

现在的微信跳一跳小游戏都是通过 PC 端破解的，于是我就写了一个只用 Android 手机就能破解的插件。

[【建议】应该还可以通过Accessibility直接在手机端实现 ](https://github.com/iOSDevLog/JumpJump/issues/1)

# 分析
---

kotlin, Android Studio, Accessibility, adb, root

## Accessibility 

<https://developer.android.com/guide/topics/ui/accessibility/services.html>

Accessibility 可以用来辅助操作，自动化测试等，可以参考微信抢红包插件。

我们可以监测微信，如果是*跳一跳*界面就触发插件。

查了一下文档，好像只有长按，不能自己控制时间。用 `Accessibility` 实现跳跃是不行了。

## adb

如果是连接 PC 端，可以通过 `adb` 命令截图，模拟跳跃的长按（点击拖动）操作。

```bash
input swipe <x1> <y1> <x2> <y2> [duration(ms)] (Default: touchscreen) # 模拟长按
screencap <filename> # 保存截屏到手机
```

# 开发
---

用 Android Studio 创建一个新的项目。

创建一个 `AccessibilityService` 子类。

manifests

```xml
        <service
            android:name=".JumpAccessibilityService"
            android:enabled="true"
            android:exported="true"
            android:permission="android.permission.BIND_ACCESSIBILITY_SERVICE">
            <intent-filter>
                <action android:name="android.accessibilityservice.AccessibilityService" />
            </intent-filter>
            <meta-data
                android:name="android.accessibilityservice"
                android:resource="@xml/accessibility" />
        </service>
```

xmo/accessibility.xml

```xml
<?xml version="1.0" encoding="utf-8"?>
<accessibility-service xmlns:android="http://schemas.android.com/apk/res/android"
    android:accessibilityEventTypes="typeWindowStateChanged"
    android:accessibilityFeedbackType="feedbackGeneric"
    android:accessibilityFlags="flagDefault"
    android:canRetrieveWindowContent="true"
    android:notificationTimeout="100"
    android:packageNames="com.tencent.mm" />
```

```kotlin
package iosdevlog.com.jump

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent

class JumpAccessibilityService : AccessibilityService() {
    override fun onInterrupt() {
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
    }

}
```

# 测试 adb 命令

```kotlin
    fun adbCommand(commands: Array<String>) {
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
```

## 在 AndroidTest 下面添加测试

```kotlin
@Test
fun adbCommand() {
	Utils.screencap()
	val bitmap = BitmapFactory.decodeFile(Utils.SCREENSHOT_LOCATION)
	assertNotNull(bitmap)
	assert(bitmap.width > 0)
}
```

报错 :

> Error:Gradle: failed to create directory '...'

解决方法：

> 在 *gradle.properties* 添加 `android.enableAapt2=false`
