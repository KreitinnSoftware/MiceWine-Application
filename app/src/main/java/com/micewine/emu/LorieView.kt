package com.micewine.emu

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.ContextWrapper
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Point
import android.graphics.drawable.ColorDrawable
import android.util.AttributeSet
import android.util.Log
import android.view.KeyEvent
import android.view.Surface
import android.view.SurfaceHolder
import android.view.SurfaceView
import androidx.preference.PreferenceManager
import com.micewine.emu.input.InputStub
import com.micewine.emu.systemutils.SystemMemoryInfo

@SuppressLint("WrongConstant")
class LorieView : SurfaceView, InputStub {
    private val p = Point()
    private var totalMemory = SystemMemoryInfo.getTotalRAM(context)
    private var freeMemory = SystemMemoryInfo.getFreeRAM(context)
    private var mCallback: Callback? = null
    private val mSurfaceCallback: SurfaceHolder.Callback = object : SurfaceHolder.Callback {
        override fun surfaceCreated(holder: SurfaceHolder) {
            updateRamCounter()
            holder.setFormat(PixelFormat.BGRA_8888)
        }

        override fun surfaceChanged(holder: SurfaceHolder, f: Int, width: Int, height: Int) {
            Log.d("SurfaceChangedListener", "Surface was changed: " + measuredWidth + "x" + measuredHeight)
            if (mCallback == null) return
            dimensionsFromSettings
            mCallback!!.changed(holder.surface, measuredWidth, measuredHeight, p.x, p.y)
        }

        override fun surfaceDestroyed(holder: SurfaceHolder) {
            if (mCallback != null) mCallback!!.changed(holder.surface, 0, 0, 0, 0)
        }
    }

    constructor(context: Context?) : super(context) {
        init()
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        init()
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        init()
    }

    @Suppress("unused")
    constructor(
        context: Context?,
        attrs: AttributeSet?,
        defStyleAttr: Int,
        defStyleRes: Int
    ) : super(context, attrs, defStyleAttr, defStyleRes) {
        init()
    }

    private fun init() {
        holder.addCallback(mSurfaceCallback)
    }

    fun setCallback(callback: Callback?) {
        mCallback = callback
        triggerCallback()
    }

    fun regenerate() {
        val callback = mCallback
        mCallback = null
        holder.setFormat(android.graphics.PixelFormat.RGBA_8888)
        mCallback = callback
        triggerCallback()
    }

    fun triggerCallback() {
        isFocusable = true
        setFocusableInTouchMode(true)
        requestFocus()
        background = object : ColorDrawable(Color.TRANSPARENT) {
            override fun isStateful(): Boolean {
                return true
            }

            override fun hasFocusStateSpecified(): Boolean {
                return true
            }
        }
        val r = holder.surfaceFrame
        activity.runOnUiThread {
            mSurfaceCallback.surfaceChanged(
                holder,
                PixelFormat.BGRA_8888,
                r.width(),
                r.height()
            )
        }
    }

    private val activity: Activity
        get() {
            var context = context
            while (context is ContextWrapper) {
                if (context is Activity) {
                    return context
                }
                context = context.baseContext
            }
            throw NullPointerException()
        }
    val dimensionsFromSettings: Unit
        get() {
            val preferences = PreferenceManager.getDefaultSharedPreferences(context)!!
            val width = measuredWidth
            val height = measuredHeight
            val w: Int
            val h: Int
            val resolution = preferences.getString("displayResolutionExact", "1280x720")!!
                .split("x".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            w = resolution[0].toInt()
            h = resolution[1].toInt()
            if (width < height && w > h || width > height && w < h) p[h] = w else p[w] = h
        }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val preferences = PreferenceManager.getDefaultSharedPreferences(context)
        if (preferences.getBoolean("displayStretch", false)) {
            holder.setSizeFromLayout()
            return
        }
        dimensionsFromSettings
        if (p.x <= 0 || p.y <= 0) return
        var width = measuredWidth
        var height = measuredHeight
        if (width < height && p.x > p.y || width > height && p.x < p.y) p[p.y] = p.x
        if (width > height * p.x / p.y) width = height * p.x / p.y else height = width * p.y / p.x
        holder.setFixedSize(p.x, p.y)
        setMeasuredDimension(width, height)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        ramCounter(canvas)
    }

    private fun ramCounter(c: Canvas) {
        val paint = Paint()
        paint.textSize = 30f
        paint.style = Paint.Style.STROKE
        paint.setColor(Color.BLACK)
        val ramCounterBorderWidth = 8f
        paint.strokeWidth = ramCounterBorderWidth
        c.drawText(
            ("RAM: " + (totalMemory - freeMemory) / BYTES_FOR_MEGABYTES) + "/" + totalMemory / BYTES_FOR_MEGABYTES,
            10f,
            40f,
            paint
        )
        paint.style = Paint.Style.FILL
        paint.setColor(Color.WHITE)
        c.drawText(
            ("RAM: " + (totalMemory - freeMemory) / BYTES_FOR_MEGABYTES) + "/" + totalMemory / BYTES_FOR_MEGABYTES,
            10f,
            40f,
            paint
        )
    }

    private fun updateRamCounter() {
        totalMemory = SystemMemoryInfo.getTotalRAM(context)
        freeMemory = SystemMemoryInfo.getFreeRAM(context)
        invalidate()
        handler.postDelayed({ updateRamCounter() }, 50)
    }

    override fun sendMouseWheelEvent(deltaX: Float, deltaY: Float) {
        sendMouseEvent(deltaX, deltaY, InputStub.BUTTON_SCROLL, buttonDown = false, relative = true)
    }

    override fun dispatchKeyEventPreIme(event: KeyEvent): Boolean {
        val a = activity
        return a is EmulationActivity && a.handleKey(event)
    }

    // It is used in native code
    fun setClipboardText(text: String?) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText("X11 clipboard", text))
    }

    external fun handleXEvents()
    external override fun sendMouseEvent(
        x: Float,
        y: Float,
        whichButton: Int,
        buttonDown: Boolean,
        relative: Boolean
    )

    external override fun sendTouchEvent(action: Int, id: Int, x: Int, y: Int)
    external override fun sendKeyEvent(scanCode: Int, keyCode: Int, keyDown: Boolean): Boolean
    external override fun sendTextEvent(text: ByteArray)
    external override fun sendUnicodeEvent(code: Int)
    interface Callback {
        fun changed(
            sfc: Surface?,
            surfaceWidth: Int,
            surfaceHeight: Int,
            screenWidth: Int,
            screenHeight: Int
        )
    }

    internal interface PixelFormat {
        companion object {
            const val BGRA_8888 = 5 // Stands for HAL_PIXEL_FORMAT_BGRA_8888
        }
    }

    companion object {
        var BYTES_FOR_MEGABYTES = (1024 * 1024).toLong()

        init {
            System.loadLibrary("Xlorie")
        }

        @JvmStatic
        external fun connect(fd: Int)
        @JvmStatic
        external fun startLogcat(fd: Int)
        @JvmStatic
        external fun setClipboardSyncEnabled(enabled: Boolean)
        @JvmStatic
        external fun sendWindowChange(width: Int, height: Int, framerate: Int)
    }
}
