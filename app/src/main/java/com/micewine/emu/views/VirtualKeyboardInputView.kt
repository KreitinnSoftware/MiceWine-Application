package com.micewine.emu.views

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.micewine.emu.LorieView
import com.micewine.emu.R
import com.micewine.emu.activities.MainActivity.Companion.preferences
import com.micewine.emu.activities.PresetManagerActivity.Companion.SELECTED_VIRTUAL_CONTROLLER_PRESET
import com.micewine.emu.controller.ControllerUtils
import com.micewine.emu.controller.ControllerUtils.DOWN
import com.micewine.emu.controller.ControllerUtils.LEFT
import com.micewine.emu.controller.ControllerUtils.LEFT_DOWN
import com.micewine.emu.controller.ControllerUtils.LEFT_UP
import com.micewine.emu.controller.ControllerUtils.MOUSE
import com.micewine.emu.controller.ControllerUtils.RIGHT
import com.micewine.emu.controller.ControllerUtils.RIGHT_DOWN
import com.micewine.emu.controller.ControllerUtils.RIGHT_UP
import com.micewine.emu.controller.ControllerUtils.UP
import com.micewine.emu.controller.ControllerUtils.handleAxis
import com.micewine.emu.controller.ControllerUtils.handleKey
import com.micewine.emu.controller.XKeyCodes.ButtonMapping
import com.micewine.emu.controller.XKeyCodes.getMapping
import com.micewine.emu.fragments.VirtualControllerPresetManagerFragment.Companion.getVirtualControllerPreset
import com.micewine.emu.input.InputStub.BUTTON_LEFT
import com.micewine.emu.input.InputStub.BUTTON_MIDDLE
import com.micewine.emu.input.InputStub.BUTTON_RIGHT
import com.micewine.emu.input.InputStub.BUTTON_UNDEFINED
import kotlin.math.sqrt

class VirtualKeyboardInputView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    private val paint = Paint().apply {
        strokeWidth = 16F
        color = Color.WHITE
        style = Paint.Style.STROKE
    }
    private val textPaint: Paint = Paint().apply {
        color = Color.WHITE
        textAlign = Paint.Align.CENTER
        textSize = 120F
        typeface = context.resources.getFont(R.font.quicksand)
    }

    private var lorieView: LorieView = LorieView(context)
    private var isFingerPressingButton = false
    private val dpadUp: Path = Path()
    private val dpadDown: Path = Path()
    private val dpadLeft: Path = Path()
    private val dpadRight: Path = Path()

    fun loadPreset(name: String?) {
        val globalPreset = preferences?.getString(SELECTED_VIRTUAL_CONTROLLER_PRESET, "default") ?: "default"
        val presetName = if (name == "--") { globalPreset } else { name ?: globalPreset }
        val preset = getVirtualControllerPreset(presetName) ?: return

        buttonList.clear()
        analogList.clear()
        dpadList.clear()

        preset.buttons.forEach {
            when (it.keyName) {
                "M_Left" -> it.buttonMapping = ButtonMapping(it.keyName, BUTTON_LEFT, BUTTON_LEFT, MOUSE)
                "M_Middle" -> it.buttonMapping = ButtonMapping(it.keyName, BUTTON_MIDDLE, BUTTON_MIDDLE, MOUSE)
                "M_Right" -> it.buttonMapping = ButtonMapping(it.keyName, BUTTON_RIGHT, BUTTON_RIGHT, MOUSE)
                "Mouse" -> it.buttonMapping = ButtonMapping(it.keyName, MOUSE, MOUSE, MOUSE)
                else -> it.buttonMapping = getMapping(it.keyName)
            }

            buttonList.add(it)
        }
        preset.analogs.forEach {
            it.upKeyCodes = getMapping(it.upKeyName)
            it.downKeyCodes = getMapping(it.downKeyName)
            it.leftKeyCodes = getMapping(it.leftKeyName)
            it.rightKeyCodes = getMapping(it.rightKeyName)

            analogList.add(it)
        }
        preset.dpads.forEach {
            it.upKeyCodes = getMapping(it.upKeyName)
            it.downKeyCodes = getMapping(it.downKeyName)
            it.leftKeyCodes = getMapping(it.leftKeyName)
            it.rightKeyCodes = getMapping(it.rightKeyName)

            dpadList.add(it)
        }
    }

    private fun drawDPad(path: Path, pressed: Boolean, canvas: Canvas) {
        if (pressed) {
            paint.style = Paint.Style.FILL_AND_STROKE
            textPaint.color = Color.BLACK
        } else {
            paint.style = Paint.Style.STROKE
            textPaint.color = Color.WHITE
        }
        paint.alpha = 200
        textPaint.alpha = 200

        canvas.drawPath(path, paint)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        buttonList.forEach {
            if (it.isPressed) {
                paint.style = Paint.Style.FILL_AND_STROKE
                textPaint.color = Color.BLACK
            } else {
                paint.style = Paint.Style.STROKE
                textPaint.color = Color.WHITE
            }
            paint.color = Color.WHITE
            paint.alpha = 200
            paint.strokeWidth = 16F
            textPaint.alpha = 200

            paint.textSize = it.radius / 4

            when (it.shape) {
                SHAPE_CIRCLE -> {
                    canvas.drawCircle(it.x, it.y, it.radius / 2, paint)
                }
                SHAPE_RECTANGLE -> {
                    canvas.drawRoundRect(
                        it.x - it.radius / 2,
                        it.y - it.radius / 4,
                        it.x + it.radius / 2,
                        it.y + it.radius / 4,
                        32F,
                        32F,
                        paint
                    )
                }
		        SHAPE_SQUARE -> {
                    canvas.drawRoundRect(
                        it.x - it.radius / 2,
                        it.y - it.radius / 2,
                        it.x + it.radius / 2,
                        it.y + it.radius / 2,
                        32F,
                        32F,
                        paint
                    )
                }
            }

            textPaint.textSize = it.radius / 4
            val offset = (textPaint.fontMetrics.ascent + textPaint.fontMetrics.descent) / 2
            canvas.drawText(it.keyName, it.x, it.y - offset - 4, textPaint)
        }
        analogList.forEach {
            var analogX = it.x + it.fingerX
            var analogY = it.y + it.fingerY

            val distSquared = (it.fingerX * it.fingerX) + (it.fingerY * it.fingerY)
            val maxDist = (it.radius / 4) * (it.radius / 4)

            if (distSquared > maxDist) {
                val scale = (it.radius / 4) / sqrt(distSquared)
                analogX = it.x + (it.fingerX * scale)
                analogY = it.y + (it.fingerY * scale)
            }

            paint.color = Color.WHITE
            paint.alpha = 200

            paint.style = Paint.Style.STROKE
            canvas.drawCircle(it.x, it.y, it.radius / 2, paint)

            paint.style = Paint.Style.FILL
            canvas.drawCircle(analogX, analogY, it.radius / 4, paint)
        }
        dpadList.forEach {
            canvas.apply {
                dpadLeft.apply {
                    reset()
                    moveTo(it.x - 20, it.y)
                    lineTo(it.x - 20 - it.radius / 4, it.y - it.radius / 4)
                    lineTo(it.x - 20 - it.radius / 4 - it.radius / 2, it.y - it.radius / 4)
                    lineTo(it.x - 20 - it.radius / 4 - it.radius / 2, it.y - it.radius / 4 + it.radius / 2)
                    lineTo(it.x - 20 - it.radius / 4, it.y - it.radius / 4 + it.radius / 2)
                    lineTo(it.x - 20, it.y)
                    close()
                }

                dpadUp.apply {
                    reset()
                    moveTo(it.x, it.y - 20)
                    lineTo(it.x - it.radius / 4, it.y - 20 - it.radius / 4)
                    lineTo(it.x - it.radius / 4, it.y - 20 - it.radius / 4 - it.radius / 2)
                    lineTo(it.x - it.radius / 4 + it.radius / 2, it.y - 20 - it.radius / 4 - it.radius / 2)
                    lineTo(it.x - it.radius / 4 + it.radius / 2, it.y - 20 - it.radius / 4)
                    lineTo(it.x, it.y - 20)
                    close()
                }

                dpadRight.apply {
                    reset()
                    moveTo(it.x + 20, it.y)
                    lineTo(it.x + 20 + it.radius / 4, it.y - it.radius / 4)
                    lineTo(it.x + 20 + it.radius / 4 + it.radius / 2, it.y - it.radius / 4)
                    lineTo(it.x + 20 + it.radius / 4 + it.radius / 2, it.y - it.radius / 4 + it.radius / 2)
                    lineTo(it.x + 20 + it.radius / 4, it.y - it.radius / 4 + it.radius / 2)
                    lineTo(it.x + 20, it.y)
                    close()
                }

                dpadDown.apply {
                    reset()
                    moveTo(it.x, it.y + 20)
                    lineTo(it.x - it.radius / 4, it.y + 20 + it.radius / 4)
                    lineTo(it.x - it.radius / 4, it.y + 20 + it.radius / 4 + it.radius / 2)
                    lineTo(it.x - it.radius / 4 + it.radius / 2, it.y + 20 + it.radius / 4 + it.radius / 2)
                    lineTo(it.x - it.radius / 4 + it.radius / 2, it.y + 20 + it.radius / 4)
                    lineTo(it.x, it.y + 20)
                    close()
                }

                drawDPad(dpadUp, it.dpadStatus in listOf(UP, RIGHT_UP, LEFT_UP), canvas)
                drawText(it.upKeyName, it.x, it.y - it.radius / 2, textPaint)

                drawDPad(dpadDown, it.dpadStatus in listOf(DOWN, RIGHT_DOWN, LEFT_DOWN), canvas)
                drawText(it.downKeyName, it.x, it.y + it.radius / 2 + 26, textPaint)

                drawDPad(dpadLeft, it.dpadStatus in listOf(LEFT, LEFT_DOWN, LEFT_UP), canvas)
                drawText(it.leftKeyName, it.x - it.radius / 2 - 20, it.y + 16, textPaint)

                drawDPad(dpadRight, it.dpadStatus in listOf(RIGHT, RIGHT_DOWN, RIGHT_UP), canvas)
                drawText(it.rightKeyName, it.x + it.radius / 2 + 20, it.y + 16, textPaint)
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_POINTER_DOWN, MotionEvent.ACTION_DOWN -> {
                buttonList.forEach {
                    if (detectClick(event, event.actionIndex, it.x, it.y, it.radius, it.shape)) {
                        it.isPressed = true
                        it.fingerId = event.getPointerId(event.actionIndex)

                        handleButton(it, true)

                        return@forEach
                    }
                }
                analogList.forEach {
                    if (detectClick(event, event.actionIndex, it.x, it.y, it.radius, SHAPE_CIRCLE)) {
                        val posX = event.getX(event.actionIndex) - it.x
                        val posY = event.getY(event.actionIndex) - it.y

                        it.fingerX = posX
                        it.fingerY = posY
                        it.isPressed = true
                        it.fingerId = event.getPointerId(event.actionIndex)

                        it.fingerX = posX
                        it.fingerY = posY

                        virtualAxis(posX / (it.radius / 4), posY / (it.radius / 4), it.upKeyCodes, it.downKeyCodes, it.leftKeyCodes, it.rightKeyCodes, it.deadZone)

                        return@forEach
                    }
                }
                dpadList.forEach {
                    if (detectClick(event, event.actionIndex, it.x, it.y, it.radius, SHAPE_DPAD)) {
                        val posX = event.getX(event.actionIndex) - it.x
                        val posY = event.getY(event.actionIndex) - it.y

                        it.fingerX = posX
                        it.fingerY = posY
                        it.isPressed = true
                        it.fingerId = event.getPointerId(event.actionIndex)

                        it.fingerX = posX
                        it.fingerY = posY

                        when {
                            (posX / it.radius > 0.25) && !(posY / it.radius < -0.25 || posY / it.radius > 0.25) -> it.dpadStatus = RIGHT
                            (posX / it.radius < -0.25) && !(posY / it.radius < -0.25 || posY / it.radius > 0.25) -> it.dpadStatus = LEFT
                            (posY / it.radius > 0.25) && !(posX / it.radius < -0.25 || posX / it.radius > 0.25) -> it.dpadStatus = DOWN
                            (posY / it.radius < -0.25) && !(posX / it.radius < -0.25 || posX / it.radius > 0.25) -> it.dpadStatus = UP
                            (posX / it.radius > 0.25) && (posY / it.radius > 0.25) -> it.dpadStatus = RIGHT_DOWN
                            (posX / it.radius > 0.25) && (posY / it.radius < -0.25) -> it.dpadStatus = RIGHT_UP
                            (posX / it.radius < -0.25) && (posY / it.radius > 0.25) -> it.dpadStatus = LEFT_DOWN
                            (posX / it.radius < -0.25) && (posY / it.radius < -0.25) -> it.dpadStatus = LEFT_UP

                            else -> it.dpadStatus = -1
                        }

                        virtualAxis(posX / it.radius, posY / it.radius, it.upKeyCodes, it.downKeyCodes, it.leftKeyCodes, it.rightKeyCodes, 0.25F)

                        return@forEach
                    }
                }

                invalidate()
            }
            MotionEvent.ACTION_MOVE -> {
                for (i in 0 until event.pointerCount) {
                    isFingerPressingButton = false

                    buttonList.forEach {
                        if (it.fingerId == event.getPointerId(i)) {
                            it.isPressed = true
                            handleButton(it, true)

                            isFingerPressingButton = true
                        }
                    }
                    analogList.forEach {
                        if (it.isPressed && it.fingerId == event.getPointerId(i)) {
                            val posX = event.getX(i) - it.x
                            val posY = event.getY(i) - it.y

                            it.fingerX = posX
                            it.fingerY = posY

                            virtualAxis(posX / (it.radius / 4), posY / (it.radius / 4), it.upKeyCodes, it.downKeyCodes, it.leftKeyCodes, it.rightKeyCodes, it.deadZone)

                            isFingerPressingButton = true
                        }
                    }
                    dpadList.forEach {
                        if (it.isPressed && it.fingerId == event.getPointerId(i)) {
                            val posX = event.getX(i) - it.x
                            val posY = event.getY(i) - it.y

                            it.fingerX = posX
                            it.fingerY = posY

                            isFingerPressingButton = true

                            when {
                                (posX / it.radius > 0.25) && !(posY / it.radius < -0.25 || posY / it.radius > 0.25) -> it.dpadStatus = RIGHT
                                (posX / it.radius < -0.25) && !(posY / it.radius < -0.25 || posY / it.radius > 0.25) -> it.dpadStatus = LEFT
                                (posY / it.radius > 0.25) && !(posX / it.radius < -0.25 || posX / it.radius > 0.25) -> it.dpadStatus = DOWN
                                (posY / it.radius < -0.25) && !(posX / it.radius < -0.25 || posX / it.radius > 0.25) -> it.dpadStatus = UP
                                (posX / it.radius > 0.25) && (posY / it.radius > 0.25) -> it.dpadStatus = RIGHT_DOWN
                                (posX / it.radius > 0.25) && (posY / it.radius < -0.25) -> it.dpadStatus = RIGHT_UP
                                (posX / it.radius < -0.25) && (posY / it.radius > 0.25) -> it.dpadStatus = LEFT_DOWN
                                (posX / it.radius < -0.25) && (posY / it.radius < -0.25) -> it.dpadStatus = LEFT_UP

                                else -> it.dpadStatus = -1
                            }

                            virtualAxis(posX / it.radius, posY / it.radius, it.upKeyCodes, it.downKeyCodes, it.leftKeyCodes, it.rightKeyCodes, 0.25F)
                        }
                    }

                    if (!isFingerPressingButton && event.historySize > 0) {
                        val deltaX = event.getX(i) - event.getHistoricalX(i, 0)
                        val deltaY = event.getY(i) - event.getHistoricalY(i, 0)

                        if ((deltaX > 0.08 || deltaX < -0.08) && (deltaY > 0.08 || deltaY < -0.08)) {
                            lorieView.sendMouseEvent(deltaX, deltaY, BUTTON_UNDEFINED, false, true)
                        }
                    }
                }

                invalidate()
            }
            MotionEvent.ACTION_POINTER_UP -> {
                buttonList.forEach {
                    if (it.fingerId == event.getPointerId(event.actionIndex)) {
                        it.fingerId = -1
                        handleButton(it, false)
                    }
                }
                analogList.forEach {
                    if (it.fingerId == event.getPointerId(event.actionIndex)) {
                        it.fingerId = -1
                        it.fingerX = 0F
                        it.fingerY = 0F

                        it.isPressed = false

                        virtualAxis(0F, 0F, it.upKeyCodes, it.downKeyCodes, it.leftKeyCodes, it.rightKeyCodes, it.deadZone)
                    }
                }
                dpadList.forEach {
                    if (it.fingerId == event.getPointerId(event.actionIndex)) {
                        it.fingerId = -1
                        it.fingerX = 0F
                        it.fingerY = 0F

                        it.isPressed = false
                        it.dpadStatus = -1

                        virtualAxis(0F, 0F, it.upKeyCodes, it.downKeyCodes, it.leftKeyCodes, it.rightKeyCodes, 0.25F)
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

                    virtualAxis(0F, 0F, it.upKeyCodes, it.downKeyCodes, it.leftKeyCodes, it.rightKeyCodes, it.deadZone)
                }
                dpadList.forEach {
                    it.fingerX = 0F
                    it.fingerY = 0F
                    it.isPressed = false
                    it.dpadStatus = -1

                    virtualAxis(0F, 0F, it.upKeyCodes, it.downKeyCodes, it.leftKeyCodes, it.rightKeyCodes, 0.25F)
                }

                invalidate()
            }
        }

        return true
    }

    private fun virtualAxis(axisX: Float, axisY: Float, upMapping: ButtonMapping, downMapping: ButtonMapping, leftMapping: ButtonMapping, rightMapping: ButtonMapping, deadZone: Float) {
        handleAxis(axisX, axisY, ControllerUtils.Analog(false, upMapping, downMapping, leftMapping, rightMapping), deadZone)
    }

    private fun handleButton(button: VirtualButton, pressed: Boolean) {
        button.isPressed = pressed

        handleKey(pressed, button.buttonMapping)
    }

    class VirtualButton(
        var id: Int,
        var x: Float,
        var y: Float,
        var radius: Float,
        var keyName: String,
        var buttonMapping: ButtonMapping,
        var fingerId: Int,
        var isPressed: Boolean,
        var shape: Int
    )

    class VirtualDPad(
        var id: Int,
        var x: Float,
        var y: Float,
        var radius: Float,
        var upKeyName: String,
        var upKeyCodes: ButtonMapping,
        var downKeyName: String,
        var downKeyCodes: ButtonMapping,
        var leftKeyName: String,
        var leftKeyCodes: ButtonMapping,
        var rightKeyName: String,
        var rightKeyCodes: ButtonMapping,
        var fingerId: Int,
        var isPressed: Boolean,
        var fingerX: Float,
        var fingerY: Float,
        var dpadStatus: Int
    )

    class VirtualAnalog(
        var id: Int,
        var x: Float,
        var y: Float,
        var fingerX: Float,
        var fingerY: Float,
        var radius: Float,
        var upKeyName: String,
        var upKeyCodes: ButtonMapping,
        var downKeyName: String,
        var downKeyCodes: ButtonMapping,
        var leftKeyName: String,
        var leftKeyCodes: ButtonMapping,
        var rightKeyName: String,
        var rightKeyCodes: ButtonMapping,
        var isPressed: Boolean,
        var fingerId: Int,
        var deadZone: Float
    )

    companion object {
        const val SHAPE_CIRCLE = 0
        const val SHAPE_SQUARE = 1
        const val SHAPE_RECTANGLE = 2
        const val SHAPE_DPAD = 3

        val buttonList = mutableListOf<VirtualButton>()
        val analogList = mutableListOf<VirtualAnalog>()
        val dpadList = mutableListOf<VirtualDPad>()

        fun detectClick(event: MotionEvent, index: Int, x: Float, y: Float, radius: Float, shape: Int): Boolean {
            return when (shape) {
                SHAPE_RECTANGLE -> {
                    (event.getX(index) >= x - radius / 2 && event.getX(index) <= (x + (radius / 2))) &&
                            (event.getY(index) >= y - radius / 4 && event.getY(index) <= (y + (radius / 4)))
                }
                SHAPE_DPAD -> {
                    (event.getX(index) >= x - radius - 20 && event.getX(index) <= (x + (radius - 20))) &&
                            (event.getY(index) >= y - radius - 20 && event.getY(index) <= (y + (radius - 20)))
                }
                else -> (event.getX(index) >= x - radius / 2 && event.getX(index) <= (x + (radius / 2))) &&
                        (event.getY(index) >= y - radius / 2 && event.getY(index) <= (y + (radius / 2)))
            }
        }
    }
}
