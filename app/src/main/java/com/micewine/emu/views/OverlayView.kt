package com.micewine.emu.views

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.preference.PreferenceManager
import com.micewine.emu.LorieView
import com.micewine.emu.activities.PresetManagerActivity.Companion.SELECTED_VIRTUAL_CONTROLLER_PRESET_KEY
import com.micewine.emu.controller.ControllerUtils.KEYBOARD
import com.micewine.emu.controller.ControllerUtils.MOUSE
import com.micewine.emu.controller.ControllerUtils.handleAxis
import com.micewine.emu.controller.XKeyCodes.getXKeyScanCodes
import com.micewine.emu.fragments.VirtualControllerPresetManagerFragment.Companion.getMapping
import com.micewine.emu.input.InputStub.BUTTON_LEFT
import com.micewine.emu.input.InputStub.BUTTON_MIDDLE
import com.micewine.emu.input.InputStub.BUTTON_RIGHT
import com.micewine.emu.input.InputStub.BUTTON_UNDEFINED
import kotlin.math.sqrt

class OverlayView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    private val paint: Paint = Paint().apply {
        color = Color.WHITE
        textAlign = Paint.Align.CENTER
        isFakeBoldText = true
    }

    private val buttonPaint: Paint = Paint().apply {
        strokeWidth = 8F
        color = Color.WHITE
        style = Paint.Style.STROKE
    }

    private val textPaint: Paint = Paint().apply {
        color = Color.WHITE
        textAlign = Paint.Align.CENTER
        textSize = 40F
    }

    private var lorieView: LorieView = LorieView(context)
    private val preferences = PreferenceManager.getDefaultSharedPreferences(context)

    fun loadPreset(name: String?) {
        var mapping = getMapping(name ?: preferences.getString(SELECTED_VIRTUAL_CONTROLLER_PRESET_KEY, "default")!!)

        if (name == "--") mapping = getMapping(preferences.getString(SELECTED_VIRTUAL_CONTROLLER_PRESET_KEY, "default")!!)

        buttonList.clear()
        analogList.clear()

        mapping?.buttons?.forEach {
            when (it.keyName) {
                "M_Left" -> {
                    it.keyCodes = listOf(BUTTON_LEFT, BUTTON_LEFT, MOUSE)
                }

                "M_Middle" -> {
                    it.keyCodes = listOf(BUTTON_MIDDLE, BUTTON_MIDDLE, MOUSE)
                }

                "M_Right" -> {
                    it.keyCodes = listOf(BUTTON_RIGHT, BUTTON_RIGHT, MOUSE)
                }

                "Mouse" -> {
                    it.keyCodes = listOf(MOUSE, MOUSE, MOUSE)
                }

                else -> {
                    it.keyCodes = getXKeyScanCodes(it.keyName)
                }
            }

            buttonList.add(it)
        }

        mapping?.analogs?.forEach {
            it.upKeyCodes = getXKeyScanCodes(it.upKeyName)
            it.downKeyCodes = getXKeyScanCodes(it.downKeyName)
            it.leftKeyCodes = getXKeyScanCodes(it.leftKeyName)
            it.rightKeyCodes = getXKeyScanCodes(it.rightKeyName)

            analogList.add(it)
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        buttonList.forEach {
            buttonPaint.color = if (it.isPressed) Color.GRAY else Color.WHITE
            buttonPaint.alpha = 150

            textPaint.color = if (it.isPressed) Color.GRAY else Color.WHITE

            canvas.drawCircle(it.x, it.y, it.radius / 2, buttonPaint)
            paint.textSize = it.radius / 4
            canvas.drawText(it.keyName, it.x, it.y + 10, textPaint)
        }

        analogList.forEach {
            paint.color = if (it.isPressed) Color.GRAY else Color.WHITE
            buttonPaint.color = if (it.isPressed) Color.GRAY else Color.WHITE

            paint.alpha = 150
            buttonPaint.alpha = 150

            canvas.drawCircle(it.x, it.y, it.radius / 2, buttonPaint)

            var analogX = it.x + it.fingerX
            var analogY = it.y + it.fingerY

            val distanceSquared = (it.fingerX * it.fingerX) + (it.fingerY * it.fingerY)
            val maxRadiusSquared = (it.radius / 4) * (it.radius / 4)

            if (distanceSquared > maxRadiusSquared) {
                val scale = (it.radius / 4) / sqrt(distanceSquared)
                analogX = it.x + (it.fingerX * scale)
                analogY = it.y + (it.fingerY * scale)
            }

            canvas.drawCircle(analogX, analogY, it.radius / 4, paint)
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_POINTER_DOWN, MotionEvent.ACTION_DOWN -> {
                buttonList.forEach {
                    if (detectClick(event, event.actionIndex, it.x, it.y, it.radius)) {
                        it.isPressed = true
                        it.fingerId = event.actionIndex

                        handleButton(it, true)

                        return@forEach
                    }
                }

                analogList.forEach {
                    if (detectClick(event, event.actionIndex, it.x, it.y, it.radius)) {
                        val posX = event.getX(event.actionIndex) - it.x
                        val posY = event.getY(event.actionIndex) - it.y

                        it.fingerX = posX
                        it.fingerY = posY
                        it.isPressed = true
                        it.fingerId = event.actionIndex

                        it.fingerX = posX
                        it.fingerY = posY

                        virtualAxis(posX / (it.radius / 4), posY / (it.radius / 4), it.upKeyCodes!!, it.downKeyCodes!!, it.leftKeyCodes!!, it.rightKeyCodes!!, it.deadZone)

                        return@forEach
                    }
                }

                invalidate()
            }

            MotionEvent.ACTION_MOVE -> {
                for (i in 0 until event.pointerCount) {
                    var isFingerPressingButton = false

                    buttonList.forEach {
                        if (it.fingerId == i) {
                            it.isPressed = true
                            handleButton(it, true)

                            isFingerPressingButton = true
                        }
                    }

                    analogList.forEach {
                        if (it.isPressed && it.fingerId == i) {
                            val posX = event.getX(i) - it.x
                            val posY = event.getY(i) - it.y

                            it.fingerX = posX
                            it.fingerY = posY

                            virtualAxis(posX / (it.radius / 4), posY / (it.radius / 4), it.upKeyCodes!!, it.downKeyCodes!!, it.leftKeyCodes!!, it.rightKeyCodes!!, it.deadZone)

                            isFingerPressingButton = true
                        }
                    }

                    if (!isFingerPressingButton) {
                        if (event.historySize > 0) {
                            val deltaX = event.getX(i) - event.getHistoricalX(i, 0)
                            val deltaY = event.getY(i) - event.getHistoricalY(i, 0)

                            if ((deltaX > 0.08 || deltaX < -0.08) && (deltaY > 0.08 || deltaY < -0.08)) {
                                lorieView.sendMouseEvent(deltaX, deltaY, BUTTON_UNDEFINED, false, true)
                            }
                        }
                    }
                }

                invalidate()
            }

            MotionEvent.ACTION_POINTER_UP -> {
                buttonList.forEach {
                    if (it.fingerId == event.actionIndex) {
                        it.fingerId = -1
                    }
                    if (detectClick(event, event.actionIndex, it.x, it.y, it.radius)) {
                        handleButton(it, false)
                    }
                }

                analogList.forEach {
                    if (it.fingerId == event.actionIndex) {
                        it.fingerId = -1
                        it.fingerX = 0F
                        it.fingerY = 0F

                        it.isPressed = false

                        virtualAxis(0F, 0F, it.upKeyCodes!!, it.downKeyCodes!!, it.leftKeyCodes!!, it.rightKeyCodes!!, it.deadZone)
                    }
                }

                invalidate()
            }

            MotionEvent.ACTION_UP -> {
                buttonList.forEach {
                    it.fingerId = -1
                    handleButton(it, false)
                }

                analogList.forEach {
                    it.fingerX = 0F
                    it.fingerY = 0F
                    it.isPressed = false

                    virtualAxis(0F, 0F, it.upKeyCodes!!, it.downKeyCodes!!, it.leftKeyCodes!!, it.rightKeyCodes!!, it.deadZone)
                }

                invalidate()
            }
        }

        return true
    }

    private fun virtualAxis(axisX: Float, axisY: Float, upKeyCodes: List<Int>, downKeyCodes: List<Int>, leftKeyCodes: List<Int>, rightKeyCodes: List<Int>, deadZone: Float) {
        handleAxis(lorieView, axisX, axisY, axisX < deadZone && axisX > -deadZone, axisY < deadZone && axisY > -deadZone, rightKeyCodes, leftKeyCodes, downKeyCodes, upKeyCodes, deadZone)
    }

    private fun handleButton(button: VirtualButton, pressed: Boolean) {
        button.isPressed = pressed

        when (button.keyCodes!![2]) {
            KEYBOARD -> lorieView.sendKeyEvent(button.keyCodes!![0], button.keyCodes!![1], pressed)
            MOUSE -> lorieView.sendMouseEvent(0F, 0F, button.keyCodes!![0], pressed, true)
        }
    }

    class VirtualButton(
        var id: Int,
        var x: Float,
        var y: Float,
        var radius: Float,
        var keyName: String,
        var keyCodes: List<Int>?,
        var fingerId: Int,
        var isPressed: Boolean
    )

    class VirtualAnalog(
        var id: Int,
        var x: Float,
        var y: Float,
        var fingerX: Float,
        var fingerY: Float,
        var radius: Float,
        var upKeyName: String,
        var upKeyCodes: List<Int>?,
        var downKeyName: String,
        var downKeyCodes: List<Int>?,
        var leftKeyName: String,
        var leftKeyCodes: List<Int>?,
        var rightKeyName: String,
        var rightKeyCodes: List<Int>?,
        var isPressed: Boolean,
        var fingerId: Int,
        var deadZone: Float
    )

    companion object {
        val buttonList = mutableListOf<VirtualButton>()
        val analogList = mutableListOf<VirtualAnalog>()

        fun detectClick(event: MotionEvent, index: Int, x: Float, y: Float, radius: Float): Boolean {
            return (event.getX(index) >= x - radius / 2 && event.getX(index) <= (x + (radius / 2))) &&
                    (event.getY(index) >= y - radius / 2 && event.getY(index) <= (y + (radius / 2)))
        }
    }
}
