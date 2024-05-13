// Copyright 2013 The Chromium Authors
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.
package com.micewine.emu.input

import android.graphics.PointF

/**
 * This class stores UI configuration that will be used when rendering the remote desktop.
 */
class RenderData {
    /**
     * Specifies the position, in image coordinates, at which the cursor image will be drawn.
     * This will normally be at the location of the most recently injected motion event.
     */
    private val mCursorPosition = PointF()
    @JvmField
    var scale = PointF()
    @JvmField
    var screenWidth = 0
    @JvmField
    var screenHeight = 0
    @JvmField
    var imageWidth = 0
    @JvmField
    var imageHeight = 0
    val cursorPosition: PointF
        /**
         * Returns the position of the rendered cursor.
         *
         * @return A point representing the current position.
         */
        get() = PointF(mCursorPosition.x, mCursorPosition.y)

    /**
     * Sets the position of the cursor which is used for rendering.
     *
     * @param newX The new value of the x coordinate.
     * @param newY The new value of the y coordinate
     * @return True if the cursor position has changed.
     */
    fun setCursorPosition(newX: Float, newY: Float): Boolean {
        var cursorMoved = false
        if (newX != mCursorPosition.x) {
            mCursorPosition.x = newX
            cursorMoved = true
        }
        if (newY != mCursorPosition.y) {
            mCursorPosition.y = newY
            cursorMoved = true
        }
        return cursorMoved
    }
}
