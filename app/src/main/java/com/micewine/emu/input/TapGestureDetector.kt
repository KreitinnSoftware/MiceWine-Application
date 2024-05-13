// Copyright 2013 The Chromium Authors
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.
package com.micewine.emu.input

import android.content.Context
import android.graphics.PointF
import android.os.Handler
import android.os.Message
import android.util.SparseArray
import android.view.MotionEvent
import android.view.ViewConfiguration
import java.lang.ref.WeakReference
import kotlin.math.max

/**
 * This class detects multi-finger tap and long-press events. This is provided since the stock
 * Android gesture-detectors only detect taps/long-presses made with one finger.
 */
class TapGestureDetector(
    context: Context?,
    /**
     * The listener to which notifications are sent.
     */
    private val mListener: OnTapListener
) {
    /**
     * Handler used for posting tasks to be executed in the future.
     */
    private val mHandler: Handler

    /**
     * Stores the location of each down MotionEvent (by pointer ID), for detecting motion of any
     * pointer beyond the TouchSlop region.
     */
    private val mInitialPositions = SparseArray<PointF>()

    /**
     * Threshold squared-distance, in pixels, to use for motion-detection. If a finger moves less
     * than this distance, the gesture is still eligible to be a tap event.
     */
    private val mTouchSlopSquare: Int

    /**
     * The maximum number of fingers seen in the gesture.
     */
    private var mPointerCount = 0

    /**
     * The coordinates of the first finger down seen in the gesture.
     */
    private var mInitialPoint: PointF? = null

    /**
     * Set to true whenever motion is detected in the gesture, or a long-touch is triggered.
     */
    private var mTapCancelled = false

    init {
        mHandler = EventHandler(this)
        val config = ViewConfiguration.get(context!!)
        val touchSlop = config.scaledTouchSlop
        mTouchSlopSquare = touchSlop * touchSlop
    }

    /**
     * Analyzes the touch event to determine whether to notify the listener.
     */
    fun onTouchEvent(event: MotionEvent) {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                reset()
                trackDownEvent(event)

                // Cause a long-press notification to be triggered after the timeout.
                mHandler.sendEmptyMessageDelayed(
                    0,
                    ViewConfiguration.getLongPressTimeout().toLong()
                )
                mPointerCount = 1
            }

            MotionEvent.ACTION_POINTER_DOWN -> {
                trackDownEvent(event)
                mPointerCount = max(mPointerCount.toDouble(), event.pointerCount.toDouble())
                    .toInt()
            }

            MotionEvent.ACTION_MOVE -> if (!mTapCancelled) {
                if (trackMoveEvent(event)) {
                    cancelLongTouchNotification()
                    mTapCancelled = true
                }
            }

            MotionEvent.ACTION_UP -> {
                cancelLongTouchNotification()
                if (!mTapCancelled) mListener.onTap(
                    mPointerCount,
                    mInitialPoint!!.x,
                    mInitialPoint!!.y
                )
                mInitialPoint = null
            }

            MotionEvent.ACTION_POINTER_UP -> {
                cancelLongTouchNotification()
                trackUpEvent(event)
            }

            MotionEvent.ACTION_CANCEL -> cancelLongTouchNotification()
            else -> {}
        }
    }

    /**
     * Stores the location of the ACTION_DOWN or ACTION_POINTER_DOWN event.
     */
    private fun trackDownEvent(event: MotionEvent) {
        var pointerIndex = 0
        if (event.actionMasked == MotionEvent.ACTION_POINTER_DOWN) {
            pointerIndex = event.actionIndex
        }
        val pointerId = event.getPointerId(pointerIndex)
        val eventPosition = PointF(event.getX(pointerIndex), event.getY(pointerIndex))
        mInitialPositions.put(pointerId, eventPosition)
        if (mInitialPoint == null) {
            mInitialPoint = eventPosition
        }
    }

    /**
     * Removes the ACTION_UP or ACTION_POINTER_UP event from the stored list.
     */
    private fun trackUpEvent(event: MotionEvent) {
        var pointerIndex = 0
        if (event.actionMasked == MotionEvent.ACTION_POINTER_UP) {
            pointerIndex = event.actionIndex
        }
        val pointerId = event.getPointerId(pointerIndex)
        mInitialPositions.remove(pointerId)
    }

    /**
     * Processes an ACTION_MOVE event and returns whether a pointer moved beyond the TouchSlop
     * threshold.
     *
     * @return True if motion was detected.
     */
    private fun trackMoveEvent(event: MotionEvent): Boolean {
        val pointerCount = event.pointerCount
        for (i in 0 until pointerCount) {
            val pointerId = event.getPointerId(i)
            val currentX = event.getX(i)
            val currentY = event.getY(i)
            val downPoint = mInitialPositions[pointerId]
            if (downPoint == null) {
                // There was no corresponding DOWN event, so add it. This is an inconsistency
                // which shouldn't normally occur.
                mInitialPositions.put(pointerId, PointF(currentX, currentY))
                continue
            }
            val deltaX = currentX - downPoint.x
            val deltaY = currentY - downPoint.y
            if (deltaX * deltaX + deltaY * deltaY > mTouchSlopSquare) {
                return true
            }
        }
        return false
    }

    /**
     * Cleans up any stored data for the gesture.
     */
    private fun reset() {
        cancelLongTouchNotification()
        mPointerCount = 0
        mInitialPositions.clear()
        mTapCancelled = false
    }

    /**
     * Cancels any pending long-touch notifications from the message-queue.
     */
    private fun cancelLongTouchNotification() {
        mHandler.removeMessages(0)
    }

    /**
     * The listener for receiving notifications of tap gestures.
     */
    interface OnTapListener {
        /**
         * Notified when a tap event occurs.
         *
         * @param pointerCount The number of fingers that were tapped.
         * @param x            The x coordinate of the initial finger tapped.
         * @param y            The y coordinate of the initial finger tapped.
         */
        fun onTap(pointerCount: Int, x: Float, y: Float)

        /**
         * Notified when a long-touch event occurs.
         *
         * @param pointerCount The number of fingers held down.
         * @param x            The x coordinate of the initial finger tapped.
         * @param y            The y coordinate of the initial finger tapped.
         */
        fun onLongPress(pointerCount: Int, x: Float, y: Float)
    }

    /**
     * @noinspection NullableProblems
     */
    // This static inner class holds a WeakReference to the outer object, to avoid triggering the
    // lint HandlerLeak warning.
    @Suppress("deprecation")
    private class EventHandler(detector: TapGestureDetector) : Handler() {
        private val mDetector: WeakReference<TapGestureDetector>

        init {
            mDetector = WeakReference(detector)
        }

        override fun handleMessage(message: Message) {
            val detector = mDetector.get()
            if (detector != null) {
                detector.mTapCancelled = true
                detector.mListener.onLongPress(
                    detector.mPointerCount,
                    detector.mInitialPoint!!.x,
                    detector.mInitialPoint!!.y
                )
                detector.mInitialPoint = null
            }
        }
    }
}
