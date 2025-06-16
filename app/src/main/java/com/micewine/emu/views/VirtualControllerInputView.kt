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
import com.micewine.emu.activities.MainActivity.Companion.getNativeResolution
import com.micewine.emu.controller.ControllerUtils.DOWN
import com.micewine.emu.controller.ControllerUtils.LEFT
import com.micewine.emu.controller.ControllerUtils.LEFT_DOWN
import com.micewine.emu.controller.ControllerUtils.LEFT_UP
import com.micewine.emu.controller.ControllerUtils.RIGHT
import com.micewine.emu.controller.ControllerUtils.RIGHT_DOWN
import com.micewine.emu.controller.ControllerUtils.RIGHT_UP
import com.micewine.emu.controller.ControllerUtils.UP
import com.micewine.emu.controller.ControllerUtils.axisToByteArray
import com.micewine.emu.controller.ControllerUtils.connectedVirtualControllers
import com.micewine.emu.controller.ControllerUtils.normalizeAxisValue
import com.micewine.emu.input.InputStub.BUTTON_UNDEFINED
import com.micewine.emu.views.VirtualKeyboardInputView.Companion.SHAPE_CIRCLE
import com.micewine.emu.views.VirtualKeyboardInputView.Companion.SHAPE_DPAD
import com.micewine.emu.views.VirtualKeyboardInputView.Companion.SHAPE_RECTANGLE
import com.micewine.emu.views.VirtualKeyboardInputView.Companion.SHAPE_SQUARE
import com.micewine.emu.views.VirtualKeyboardInputView.Companion.detectClick
import kotlin.math.sqrt

class VirtualControllerInputView @JvmOverloads constructor(
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
    private val startButton: Path = Path()
    private val selectButton: Path = Path()
    private val buttonList: MutableList<VirtualControllerButton> = mutableListOf()
    private val dpad: VirtualXInputDPad
    private val leftAnalog: VirtualXInputAnalog
    private val rightTouchPad: VirtualXInputTouchPad

    private fun adjustButtons() {
        val nativeResolution = getNativeResolution(context)
        val baseResolution = "2400x1080" // My Device Resolution

        if (baseResolution != nativeResolution) {
            val nativeSplit = nativeResolution.split("x").map { it.toFloat() }
            val processedSplit = baseResolution.split("x").map { it.toFloat() }

            val multiplierX = nativeSplit[0] / processedSplit[0] * 100F
            val multiplierY = nativeSplit[1] / processedSplit[1] * 100F

            buttonList.forEach {
                it.x = (it.x / 100F * multiplierX)
                it.y = (it.y / 100F * multiplierY)
            }
            leftAnalog.let {
                it.x = (it.x / 100F * multiplierX)
                it.y = (it.y / 100F * multiplierY)
            }
            dpad.let {
                it.x = (it.x / 100F * multiplierX)
                it.y = (it.y / 100F * multiplierY)
            }
        }
    }

    private fun addButton(id: Int, x: Float, y: Float, radius: Float, shape: Int) {
        buttonList.add(
            VirtualControllerButton(id, x, y, radius, shape)
        )
    }

    init {
        addButton(A_BUTTON, 2065F, 910F, 180F, SHAPE_CIRCLE)
        addButton(B_BUTTON, 2205F, 735F, 180F, SHAPE_CIRCLE)
        addButton(X_BUTTON, 1925F, 735F, 180F, SHAPE_CIRCLE)
        addButton(Y_BUTTON, 2065F, 560F, 180F, SHAPE_CIRCLE)
        addButton(START_BUTTON, 1330F, 980F, 130F, SHAPE_CIRCLE)
        addButton(SELECT_BUTTON, 1120F, 980F, 130F, SHAPE_CIRCLE)
        addButton(LB_BUTTON, 280F, 300F, 260F, SHAPE_RECTANGLE)
        addButton(LT_BUTTON, 280F, 140F, 260F, SHAPE_RECTANGLE)
        addButton(RB_BUTTON, 2065F, 300F, 260F, SHAPE_RECTANGLE)
        addButton(RT_BUTTON, 2065F, 140F, 260F, SHAPE_RECTANGLE)
        addButton(LS_BUTTON, 880F, 980F, 180F, SHAPE_CIRCLE)
        addButton(RS_BUTTON, 1560F, 980F, 180F, SHAPE_CIRCLE)

        leftAnalog = VirtualXInputAnalog(LEFT_ANALOG, 280F, 840F, 275F, false, 0, 0F, 0F)
        dpad = VirtualXInputDPad(0, 640F, 480F, 250F)
        rightTouchPad = VirtualXInputTouchPad(0, 1750F, 480F, 275F)

        adjustButtons()
    }

    private fun getButtonName(id: Int): String {
        return when (id) {
            A_BUTTON -> "A"
            B_BUTTON -> "B"
            X_BUTTON -> "X"
            Y_BUTTON -> "Y"
            RB_BUTTON -> "RB"
            LB_BUTTON -> "LB"
            RT_BUTTON -> "RT"
            LT_BUTTON -> "LT"
            RS_BUTTON -> "RS"
            LS_BUTTON -> "LS"
            else -> ""
        }
    }

    private fun drawDPad(path: Path, pressed: Boolean, canvas: Canvas) {
        if (pressed) {
            paint.style = Paint.Style.FILL_AND_STROKE
        } else {
            paint.style = Paint.Style.STROKE
        }
        paint.alpha = 200

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

            val offset = (textPaint.fontMetrics.ascent + textPaint.fontMetrics.descent) / 2

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
            }

            when (it.id) {
                START_BUTTON -> {
                    paint.strokeWidth = 12F
                    startButton.apply {
                        reset()
                        moveTo(it.x - it.radius / 3, it.y - it.radius / 8)
                        lineTo(it.x - it.radius / 3 + it.radius - it.radius / 3, it.y - it.radius / 8)
                        moveTo(it.x - it.radius / 3, it.y)
                        lineTo(it.x - it.radius / 3 + it.radius - it.radius / 3, it.y)
                        moveTo(it.x - it.radius / 3, it.y + it.radius / 8)
                        lineTo(it.x - it.radius / 3 + it.radius - it.radius / 3, it.y + it.radius / 8)
                        close()
                    }
                    paint.color = if (it.isPressed) Color.BLACK else Color.WHITE
                    paint.alpha = 200

                    canvas.drawPath(startButton, paint)
                }
                SELECT_BUTTON -> {
                    paint.strokeWidth = 12F
                    selectButton.apply {
                        reset()
                        moveTo(it.x - it.radius / 4 + 4F, it.y - it.radius / 4 + 40F)
                        lineTo(it.x - it.radius / 4 + 4F, it.y - it.radius / 4)
                        lineTo(it.x - it.radius / 4 + 4F + 40F, it.y - it.radius / 4)
                        lineTo(it.x - it.radius / 4 + 4F + 40F, it.y - it.radius / 4 + 20F)
                        lineTo(it.x - it.radius / 4 + 4F + 40F, it.y - it.radius / 4)
                        lineTo(it.x - it.radius / 4 + 4F, it.y - it.radius / 4)
                        close()
                        moveTo(it.x - it.radius / 4 + 20F, it.y - it.radius / 4 + 30F)
                        lineTo(it.x - it.radius / 4 + 60F, it.y - it.radius / 4 + 30F)
                        lineTo(it.x - it.radius / 4 + 60F, it.y - it.radius / 4 + 70F)
                        lineTo(it.x - it.radius / 4 + 20F, it.y - it.radius / 4 + 70F)
                        lineTo(it.x - it.radius / 4 + 20F, it.y - it.radius / 4 + 24F)
                        lineTo(it.x - it.radius / 4 + 20F, it.y - it.radius / 4 + 70F)
                        lineTo(it.x - it.radius / 4 + 60F, it.y - it.radius / 4 + 70F)
                        lineTo(it.x - it.radius / 4 + 60F, it.y - it.radius / 4 + 30F)
                        close()
                    }

                    paint.color = if (it.isPressed) Color.BLACK else Color.WHITE
                    paint.alpha = 200

                    canvas.drawPath(selectButton, paint)
                }
                else -> {
                    canvas.drawText(getButtonName(it.id), it.x, it.y - offset - 4, textPaint)
                }
            }
        }
        leftAnalog.let {
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
        rightTouchPad.let {
            paint.style = Paint.Style.STROKE
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
        dpad.let {
            canvas.apply {
                dpadLeft.apply {
                    reset()
                    moveTo(it.x - 20, it.y)
                    lineTo(it.x - 20 - it.radius / 4, it.y - it.radius / 4)
                    lineTo(it.x - 20 - it.radius / 4 - it.radius / 2, it.y - it.radius / 4)
                    lineTo(
                        it.x - 20 - it.radius / 4 - it.radius / 2,
                        it.y - it.radius / 4 + it.radius / 2
                    )
                    lineTo(it.x - 20 - it.radius / 4, it.y - it.radius / 4 + it.radius / 2)
                    lineTo(it.x - 20, it.y)
                    close()
                }
                dpadUp.apply {
                    reset()
                    moveTo(it.x, it.y - 20)
                    lineTo(it.x - it.radius / 4, it.y - 20 - it.radius / 4)
                    lineTo(it.x - it.radius / 4, it.y - 20 - it.radius / 4 - it.radius / 2)
                    lineTo(
                        it.x - it.radius / 4 + it.radius / 2,
                        it.y - 20 - it.radius / 4 - it.radius / 2
                    )
                    lineTo(it.x - it.radius / 4 + it.radius / 2, it.y - 20 - it.radius / 4)
                    lineTo(it.x, it.y - 20)
                    close()
                }
                dpadRight.apply {
                    reset()
                    moveTo(it.x + 20, it.y)
                    lineTo(it.x + 20 + it.radius / 4, it.y - it.radius / 4)
                    lineTo(it.x + 20 + it.radius / 4 + it.radius / 2, it.y - it.radius / 4)
                    lineTo(
                        it.x + 20 + it.radius / 4 + it.radius / 2,
                        it.y - it.radius / 4 + it.radius / 2
                    )
                    lineTo(it.x + 20 + it.radius / 4, it.y - it.radius / 4 + it.radius / 2)
                    lineTo(it.x + 20, it.y)
                    close()
                }
                dpadDown.apply {
                    reset()
                    moveTo(it.x, it.y + 20)
                    lineTo(it.x - it.radius / 4, it.y + 20 + it.radius / 4)
                    lineTo(it.x - it.radius / 4, it.y + 20 + it.radius / 4 + it.radius / 2)
                    lineTo(
                        it.x - it.radius / 4 + it.radius / 2,
                        it.y + 20 + it.radius / 4 + it.radius / 2
                    )
                    lineTo(it.x - it.radius / 4 + it.radius / 2, it.y + 20 + it.radius / 4)
                    lineTo(it.x, it.y + 20)
                    close()
                }

                drawDPad(dpadUp, it.dpadStatus in listOf(UP, RIGHT_UP, LEFT_UP), canvas)
                drawDPad(dpadDown, it.dpadStatus in listOf(DOWN, RIGHT_DOWN, LEFT_DOWN), canvas)
                drawDPad(dpadLeft, it.dpadStatus in listOf(LEFT, LEFT_DOWN, LEFT_UP), canvas)
                drawDPad(dpadRight, it.dpadStatus in listOf(RIGHT, RIGHT_DOWN, RIGHT_UP), canvas)
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (virtualXInputControllerId == -1) return true

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
                leftAnalog.let {
                    if (detectClick(event, event.actionIndex, it.x, it.y, it.radius, SHAPE_CIRCLE)) {
                        val posX = event.getX(event.actionIndex) - it.x
                        val posY = event.getY(event.actionIndex) - it.y

                        it.fingerX = posX
                        it.fingerY = posY
                        it.isPressed = true
                        it.fingerId = event.getPointerId(event.actionIndex)

                        it.fingerX = posX
                        it.fingerY = posY

                        axisToByteArray(connectedVirtualControllers[virtualXInputControllerId].lx, normalizeAxisValue(posX / (it.radius / 4)))
                        axisToByteArray(connectedVirtualControllers[virtualXInputControllerId].ly, normalizeAxisValue(-posY / (it.radius / 4)))

                        return@let
                    }
                }
                rightTouchPad.let {
                    if (detectClick(event, event.actionIndex, it.x, it.y, it.radius, SHAPE_SQUARE)) {
                        it.isPressed = true
                        it.fingerId = event.getPointerId(event.actionIndex)
                    }
                }
                dpad.let {
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

                            else -> it.dpadStatus = 0
                        }

                        connectedVirtualControllers[virtualXInputControllerId].dpadStatus = it.dpadStatus

                        return@let
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

                            isFingerPressingButton = true

                            handleButton(it, true)
                        }
                    }
                    leftAnalog.let {
                        if (it.isPressed && it.fingerId == event.getPointerId(i)) {
                            val posX = event.getX(i) - it.x
                            val posY = event.getY(i) - it.y

                            it.fingerX = posX
                            it.fingerY = posY

                            isFingerPressingButton = true

                            axisToByteArray(connectedVirtualControllers[virtualXInputControllerId].lx, normalizeAxisValue(posX / (it.radius / 4)))
                            axisToByteArray(connectedVirtualControllers[virtualXInputControllerId].ly, normalizeAxisValue(-posY / (it.radius / 4)))
                        }
                    }
                    rightTouchPad.let {
                        if (it.isPressed && it.fingerId == event.getPointerId(i)) {
                            if (event.historySize > 0) {
                                val dx = event.getX(i) - event.getHistoricalX(i, event.historySize - 1)
                                val dy = event.getY(i) - event.getHistoricalY(i, event.historySize - 1)

                                isFingerPressingButton = true

                                axisToByteArray(connectedVirtualControllers[virtualXInputControllerId].rx, ((dx + 0.5F) * 255F).toInt())
                                axisToByteArray(connectedVirtualControllers[virtualXInputControllerId].ry, ((-dy + 0.5F) * 255F).toInt())
                            }
                        }
                    }
                    dpad.let {
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

                                else -> it.dpadStatus = 0
                            }

                            connectedVirtualControllers[virtualXInputControllerId].dpadStatus = it.dpadStatus
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
                leftAnalog.let {
                    if (it.fingerId == event.getPointerId(event.actionIndex)) {
                        it.fingerId = -1
                        it.fingerX = 0F
                        it.fingerY = 0F

                        it.isPressed = false

                        axisToByteArray(connectedVirtualControllers[virtualXInputControllerId].lx, 127)
                        axisToByteArray(connectedVirtualControllers[virtualXInputControllerId].ly, 127)
                    }
                }
                rightTouchPad.let {
                    if (it.fingerId == event.getPointerId(event.actionIndex)) {
                        it.fingerId = -1
                        it.isPressed = false

                        axisToByteArray(connectedVirtualControllers[virtualXInputControllerId].rx, 127)
                        axisToByteArray(connectedVirtualControllers[virtualXInputControllerId].ry, 127)
                    }
                }
                dpad.let {
                    if (it.fingerId == event.getPointerId(event.actionIndex)) {
                        it.fingerId = -1
                        it.fingerX = 0F
                        it.fingerY = 0F

                        it.isPressed = false
                        it.dpadStatus = 0

                        connectedVirtualControllers[virtualXInputControllerId].dpadStatus = 0
                    }
                }

                invalidate()
            }
            MotionEvent.ACTION_UP -> {
                buttonList.forEach {
                    it.fingerId = -1

                    handleButton(it, false)
                }
                leftAnalog.let {
                    it.fingerX = 0F
                    it.fingerY = 0F
                    it.isPressed = false

                    axisToByteArray(connectedVirtualControllers[virtualXInputControllerId].lx, 127)
                    axisToByteArray(connectedVirtualControllers[virtualXInputControllerId].ly, 127)
                }
                rightTouchPad.let {
                    it.fingerId = -1
                    it.isPressed = false

                    axisToByteArray(connectedVirtualControllers[virtualXInputControllerId].rx, 127)
                    axisToByteArray(connectedVirtualControllers[virtualXInputControllerId].ry, 127)
                }
                dpad.let {
                    it.fingerX = 0F
                    it.fingerY = 0F
                    it.isPressed = false
                    it.dpadStatus = 0

                    connectedVirtualControllers[virtualXInputControllerId].dpadStatus = 0
                }

                invalidate()
            }
        }

        return true
    }

    private fun handleButton(button: VirtualControllerButton, pressed: Boolean) {
        button.isPressed = pressed

        when (button.id) {
            A_BUTTON -> {
                connectedVirtualControllers[virtualXInputControllerId].aPressed = pressed
            }
            B_BUTTON -> {
                connectedVirtualControllers[virtualXInputControllerId].bPressed = pressed
            }
            X_BUTTON -> {
                connectedVirtualControllers[virtualXInputControllerId].xPressed = pressed
            }
            Y_BUTTON -> {
                connectedVirtualControllers[virtualXInputControllerId].yPressed = pressed
            }
            START_BUTTON -> {
                connectedVirtualControllers[virtualXInputControllerId].startPressed = pressed
            }
            SELECT_BUTTON -> {
                connectedVirtualControllers[virtualXInputControllerId].selectPressed = pressed
            }
            LB_BUTTON -> {
                connectedVirtualControllers[virtualXInputControllerId].lbPressed = pressed
            }
            LT_BUTTON -> {
                connectedVirtualControllers[virtualXInputControllerId].lt[0] = if (pressed) 2 else 0
                connectedVirtualControllers[virtualXInputControllerId].lt[1] = if (pressed) 5 else 0
                connectedVirtualControllers[virtualXInputControllerId].lt[2] = if (pressed) 5 else 0
            }
            RB_BUTTON -> {
                connectedVirtualControllers[virtualXInputControllerId].rbPressed = pressed
            }
            RT_BUTTON -> {
                connectedVirtualControllers[virtualXInputControllerId].rt[0] = if (pressed) 2 else 0
                connectedVirtualControllers[virtualXInputControllerId].rt[1] = if (pressed) 5 else 0
                connectedVirtualControllers[virtualXInputControllerId].rt[2] = if (pressed) 5 else 0
            }
            LS_BUTTON -> {
                connectedVirtualControllers[virtualXInputControllerId].lsPressed = pressed
            }
            RS_BUTTON -> {
                connectedVirtualControllers[virtualXInputControllerId].rsPressed = pressed
            }
        }
    }

    class VirtualControllerButton(
        var id: Int,
        var x: Float,
        var y: Float,
        var radius: Float,
        var shape: Int,
        var fingerId: Int = 0,
        var isPressed: Boolean = false
    )

    class VirtualXInputDPad(
        var id: Int,
        var x: Float,
        var y: Float,
        var radius: Float,
        var fingerId: Int = 0,
        var isPressed: Boolean = false,
        var fingerX: Float = 0F,
        var fingerY: Float = 0F,
        var dpadStatus: Int = 0
    )

    class VirtualXInputAnalog(
        var id: Int,
        var x: Float,
        var y: Float,
        var radius: Float,
        var isPressed: Boolean = false,
        var fingerId: Int = 0,
        var fingerX: Float = 0F,
        var fingerY: Float = 0F
    )

    class VirtualXInputTouchPad(
        var id: Int,
        var x: Float,
        var y: Float,
        var radius: Float,
        var isPressed: Boolean = false,
        var fingerId: Int = 0,
    )

    companion object {
        const val A_BUTTON = 1
        const val B_BUTTON = 2
        const val X_BUTTON = 3
        const val Y_BUTTON = 4
        const val START_BUTTON = 5
        const val SELECT_BUTTON = 6
        const val LB_BUTTON = 7
        const val LT_BUTTON = 8
        const val RB_BUTTON = 9
        const val RT_BUTTON = 10
        const val LEFT_ANALOG = 11
        const val LS_BUTTON = 12
        const val RS_BUTTON = 13

        var virtualXInputControllerId = -1
    }
}
