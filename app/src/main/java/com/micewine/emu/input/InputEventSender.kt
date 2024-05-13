// Copyright 2016 The Chromium Authors
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.
package com.micewine.emu.input

import android.graphics.PointF
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import androidx.core.math.MathUtils
import java.nio.charset.StandardCharsets
import java.util.TreeSet

/**
 * A set of functions to send users' activities, which are represented by Android classes, to
 * remote host machine. This class uses a [InputStub] to do the real injections.
 */
class InputEventSender(injector: InputStub?) {
    val pointers = BooleanArray(10)
    private val mInjector: InputStub

    /**
     * Set of pressed keys for which we've sent TextEvent.
     */
    private val mPressedTextKeys: TreeSet<Int>
    @JvmField
    var tapToMove = false
    @JvmField
    var preferScancodes = false
    @JvmField
    var pointerCapture = false

    init {
        if (injector == null) throw NullPointerException()
        mInjector = injector
        mPressedTextKeys = TreeSet()
    }

    fun sendMouseEvent(pos: PointF?, button: Int, down: Boolean, relative: Boolean) {
        if (!buttons.contains(button)) return
        mInjector.sendMouseEvent(
            (pos?.x?.toInt() ?: 0).toFloat(),
            (pos?.y?.toInt() ?: 0).toFloat(),
            button,
            down,
            relative
        )
    }

    fun sendMouseDown(button: Int, relative: Boolean) {
        if (!buttons.contains(button)) return
        mInjector.sendMouseEvent(0f, 0f, button, true, relative)
    }

    fun sendMouseUp(button: Int, relative: Boolean) {
        if (!buttons.contains(button)) return
        mInjector.sendMouseEvent(0f, 0f, button, false, relative)
    }

    fun sendMouseClick(button: Int, relative: Boolean) {
        if (!buttons.contains(button)) return
        mInjector.sendMouseEvent(0f, 0f, button, true, relative)
        mInjector.sendMouseEvent(0f, 0f, button, false, relative)
    }

    fun sendCursorMove(x: Float, y: Float, relative: Boolean) {
        mInjector.sendMouseEvent(x, y, InputStub.BUTTON_UNDEFINED, false, relative)
    }

    fun sendMouseWheelEvent(distanceX: Float, distanceY: Float) {
        mInjector.sendMouseWheelEvent(distanceX, distanceY)
    }

    /**
     * Extracts the touch point data from a MotionEvent, converts each point into a marshallable
     * object and passes the set of points to the JNI layer to be transmitted to the remote host.
     *
     * @param event The event to send to the remote host for injection.  NOTE: This object must be
     * updated to represent the remote machine's coordinate system before calling this
     * function.
     */
    fun sendTouchEvent(event: MotionEvent, renderData: RenderData) {
        val action = event.actionMasked
        if (action == MotionEvent.ACTION_MOVE || action == MotionEvent.ACTION_HOVER_MOVE || action == MotionEvent.ACTION_HOVER_ENTER || action == MotionEvent.ACTION_HOVER_EXIT) {
            // In order to process all of the events associated with an ACTION_MOVE event, we need
            // to walk the list of historical events in order and add each event to our list, then
            // retrieve the current move event data.
            val pointerCount = event.pointerCount
            for (p in 0 until pointerCount) pointers[event.getPointerId(p)] = false
            for (p in 0 until pointerCount) {
                val x = MathUtils.clamp(
                    (event.getX(p) * renderData.scale.x).toInt(),
                    0,
                    renderData.screenWidth
                )
                val y = MathUtils.clamp(
                    (event.getY(p) * renderData.scale.y).toInt(),
                    0,
                    renderData.screenHeight
                )
                pointers[event.getPointerId(p)] = true
                mInjector.sendTouchEvent(XI_TOUCH_UPDATE, event.getPointerId(p), x, y)
            }

            // Sometimes Android does not send ACTION_POINTER_UP/ACTION_UP so some pointers are "stuck" in pressed state.
            for (p in 0..9) {
                if (!pointers[p]) mInjector.sendTouchEvent(XI_TOUCH_END, p, 0, 0)
            }
        } else {
            // For all other events, we only want to grab the current/active pointer.  The event
            // contains a list of every active pointer but passing all of of these to the host can
            // cause confusion on the remote OS side and result in broken touch gestures.
            val activePointerIndex = event.actionIndex
            val id = event.getPointerId(activePointerIndex)
            val x = MathUtils.clamp(
                (event.getX(activePointerIndex) * renderData.scale.x).toInt(),
                0,
                renderData.screenWidth
            )
            val y = MathUtils.clamp(
                (event.getY(activePointerIndex) * renderData.scale.y).toInt(),
                0,
                renderData.screenHeight
            )
            val a =
                if (action == MotionEvent.ACTION_DOWN || action == MotionEvent.ACTION_POINTER_DOWN) XI_TOUCH_BEGIN else XI_TOUCH_END
            if (a == XI_TOUCH_END) mInjector.sendTouchEvent(XI_TOUCH_UPDATE, id, x, y)
            mInjector.sendTouchEvent(a, id, x, y)
        }
    }

    /**
     * Converts the [KeyEvent] into low-level events and sends them to the host as either
     * key-events or text-events. This contains some logic for handling some special keys, and
     * avoids sending a key-up event for a key that was previously injected as a text-event.
     */
    @Suppress("DEPRECATION")
    fun sendKeyEvent(v: View, e: KeyEvent): Boolean {
        val keyCode = e.keyCode
        val pressed = e.action == KeyEvent.ACTION_DOWN

        // Events received from software keyboards generate TextEvent in two
        // cases:
        //   1. This is an ACTION_MULTIPLE event.
        //   2. Ctrl, Alt and Meta are not pressed.
        // This ensures that on-screen keyboard always injects input that
        // correspond to what user sees on the screen, while physical keyboard
        // acts as if it is connected to the remote host.
        if (e.action == KeyEvent.ACTION_MULTIPLE) {
            if (e.characters != null) mInjector.sendTextEvent(
                e.characters.toByteArray(
                    StandardCharsets.UTF_8
                )
            ) else if (e.unicodeChar != 0) mInjector.sendTextEvent(
                e.unicodeChar.toChar().toString().toByteArray(StandardCharsets.UTF_8)
            )
            return true
        }
        val noModifiers =
            !e.isAltPressed && !e.isCtrlPressed && !e.isMetaPressed || e.metaState and KeyEvent.META_ALT_RIGHT_ON != 0 && (e.characters != null || e.unicodeChar != 0) // For layouts with AltGr
        // For Enter getUnicodeChar() returns 10 (line feed), but we still
        // want to send it as KeyEvent.
        val unicode: Char = if (keyCode != KeyEvent.KEYCODE_ENTER) e.unicodeChar.toChar() else 0.toChar()
        val scancode = if (preferScancodes || !noModifiers) e.scanCode else 0
        if (!preferScancodes) {
            if (pressed && unicode.code != 0 && noModifiers) {
                mPressedTextKeys.add(keyCode)
                if (e.metaState and KeyEvent.META_ALT_RIGHT_ON != 0) mInjector.sendKeyEvent(
                    0,
                    KeyEvent.KEYCODE_ALT_RIGHT,
                    false
                ) // For layouts with AltGr
                mInjector.sendTextEvent(unicode.toString().toByteArray(StandardCharsets.UTF_8))
                if (e.metaState and KeyEvent.META_ALT_RIGHT_ON != 0) mInjector.sendKeyEvent(
                    0,
                    KeyEvent.KEYCODE_ALT_RIGHT,
                    true
                ) // For layouts with AltGr
                return true
            }
            if (!pressed && mPressedTextKeys.contains(keyCode)) {
                mPressedTextKeys.remove(keyCode)
                return true
            }
        }

        // KEYCODE_AT, KEYCODE_POUND, KEYCODE_STAR and KEYCODE_PLUS are
        // deprecated, but they still need to be here for older devices and
        // third-party keyboards that may still generate these events. See
        // https://source.android.com/devices/input/keyboard-devices.html#legacy-unsupported-keys
        val chars = arrayOf(
            charArrayOf(KeyEvent.KEYCODE_AT.toChar(), '@', KeyEvent.KEYCODE_2.toChar()),
            charArrayOf(
                KeyEvent.KEYCODE_POUND.toChar(), '#', KeyEvent.KEYCODE_3.toChar()
            ),
            charArrayOf(
                KeyEvent.KEYCODE_STAR.toChar(), '*', KeyEvent.KEYCODE_8.toChar()
            ),
            charArrayOf(
                KeyEvent.KEYCODE_PLUS.toChar(), '+', KeyEvent.KEYCODE_EQUALS.toChar()
            )
        )
        for (i in chars) {
            if (e.keyCode != i[0].code) continue
            if (e.characters != null && i[1].toString().contentEquals(e.characters)
                || e.unicodeChar == i[1].code
            ) {
                mInjector.sendKeyEvent(0, KeyEvent.KEYCODE_SHIFT_LEFT, pressed)
                mInjector.sendKeyEvent(0, i[2].code, pressed)
                return true
            }
        }

        // Ignoring Android's autorepeat.
        if (e.repeatCount > 0) return true
        if (pointerCapture && keyCode == KeyEvent.KEYCODE_ESCAPE && !pressed) v.releasePointerCapture()

        // We try to send all other key codes to the host directly.
        return mInjector.sendKeyEvent(scancode, keyCode, pressed)
    }

    companion object {
        private const val XI_TOUCH_BEGIN = 18
        private const val XI_TOUCH_UPDATE = 19
        private const val XI_TOUCH_END = 20
        private val buttons = listOf(
            InputStub.BUTTON_UNDEFINED,
            InputStub.BUTTON_LEFT,
            InputStub.BUTTON_MIDDLE,
            InputStub.BUTTON_RIGHT
        )
    }
}
