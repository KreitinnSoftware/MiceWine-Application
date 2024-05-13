// Copyright 2016 The Chromium Authors
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.
package com.micewine.emu.input

/**
 * A set of functions to send client users' activities to remote host machine. This interface
 * represents low level functions without relationships with Android system. Consumers can use
 * [InputEventSender] to avoid conversions between Android classes and JNI types. The
 * implementations of this interface are not required to be thread-safe. All these functions should
 * be called from Android UI thread.
 */
interface InputStub {
    /**
     * Sends a mouse event.
     */
    fun sendMouseEvent(x: Float, y: Float, whichButton: Int, buttonDown: Boolean, relative: Boolean)

    /**
     * Sends a mouse wheel event.
     */
    fun sendMouseWheelEvent(deltaX: Float, deltaY: Float)

    /**
     * Sends a key event, and returns false if both scanCode and keyCode are not able to be
     * converted to a known usb key code. Nothing will be sent to remote host, if this function
     * returns false.
     */
    fun sendKeyEvent(scanCode: Int, keyCode: Int, keyDown: Boolean): Boolean

    /**
     * Sends a string literal. This function is useful to handle outputs from Android input
     * methods.
     */
    fun sendTextEvent(utf8Bytes: ByteArray)
    fun sendUnicodeEvent(code: Int)

    /**z
     * Sends an event, not flushing connection.
     */
    fun sendTouchEvent(action: Int, pointerId: Int, x: Int, y: Int)

    companion object {
        // These constants must match those in the generated struct protocol::MouseEvent_MouseButton.
        const val BUTTON_UNDEFINED = 0
        const val BUTTON_LEFT = 1
        const val BUTTON_MIDDLE = 2
        const val BUTTON_RIGHT = 3
        const val BUTTON_SCROLL = 4
    }
}
