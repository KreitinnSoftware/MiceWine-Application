package com.micewine.emu.utils

import android.accessibilityservice.AccessibilityService
import android.util.Log
import android.view.KeyEvent
import android.view.accessibility.AccessibilityEvent
import com.micewine.emu.activities.EmulationActivity.Companion.instance

class KeyInterceptor : AccessibilityService() {
    var pressedKeys = LinkedHashSet<Int>()

    init {
        self = this
    }

    public override fun onKeyEvent(event: KeyEvent): Boolean {
        val ret = false
        val instance = instance
        if (event.action == KeyEvent.ACTION_UP) pressedKeys.remove(event.keyCode)
        Log.d(
            "KeyInterceptor",
            (if (event.unicodeChar != 0) event.unicodeChar.toChar() else "").toString() + " " + (if (event.characters != null) event.characters else "") + " " + " not " + "intercepted event " + event
        )
        return ret
    }

    override fun onAccessibilityEvent(e: AccessibilityEvent) {
        // Disable self if it is automatically started on device boot or when activity finishes.
        instance
        if (instance.isFinishing) {
            Log.d("KeyInterceptor", "finishing")
            shutdown()
        }
    }

    override fun onInterrupt() {}

    companion object {
        private var self: KeyInterceptor? = null
        fun shutdown() {
            if (self != null) {
                self!!.disableSelf()
                self!!.pressedKeys.clear()
                self = null
            }
        }
    }
}
