package com.micewine.emu.utils

import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.util.Log

object SamsungDexUtils {
    private val TAG = SamsungDexUtils::class.java.getSimpleName()
    fun available(): Boolean {
        try {
            val semWindowManager = Class.forName("com.samsung.android.view.SemWindowManager")
            semWindowManager.getMethod("getInstance")
            semWindowManager.getDeclaredMethod(
                "requestMetaKeyEvent",
                ComponentName::class.java,
                Boolean::class.javaPrimitiveType
            )
            return true
        } catch (ignored: Exception) {
        }
        return false
    }

    fun dexMetaKeyCapture(activity: Activity, enable: Boolean) {
        try {
            val semWindowManager = Class.forName("com.samsung.android.view.SemWindowManager")
            val getInstanceMethod = semWindowManager.getMethod("getInstance")
            val manager = getInstanceMethod.invoke(null)
            val requestMetaKeyEvent = semWindowManager.getDeclaredMethod(
                "requestMetaKeyEvent",
                ComponentName::class.java,
                Boolean::class.javaPrimitiveType
            )
            requestMetaKeyEvent.invoke(manager, activity.componentName, enable)
            Log.d(TAG, "com.samsung.android.view.SemWindowManager.requestMetaKeyEvent: success")
        } catch (it: Exception) {
            Log.d(
                TAG,
                "Could not call com.samsung.android.view.SemWindowManager.requestMetaKeyEvent "
            )
            Log.d(TAG, it.javaClass.getCanonicalName() + ": " + it.message)
        }
    }

    @JvmStatic
    fun checkDeXEnabled(ctx: Context): Boolean {
        val config = ctx.resources.configuration
        try {
            val c: Class<*> = config.javaClass
            return (c.getField("SEM_DESKTOP_MODE_ENABLED").getInt(c)
                    == c.getField("semDesktopModeEnabled").getInt(config))
        } catch (ignored: NoSuchFieldException) {
        } catch (ignored: IllegalArgumentException) {
        } catch (ignored: IllegalAccessException) {
        }
        return false
    }
}
