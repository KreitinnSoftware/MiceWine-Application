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
import com.micewine.emu.controller.ControllerUtils.LEFT
import com.micewine.emu.controller.ControllerUtils.LEFT_DOWN
import com.micewine.emu.controller.ControllerUtils.LEFT_UP
import com.micewine.emu.controller.ControllerUtils.RIGHT
import com.micewine.emu.controller.ControllerUtils.RIGHT_DOWN
import com.micewine.emu.controller.ControllerUtils.RIGHT_UP
import com.micewine.emu.controller.ControllerUtils.UP
import com.micewine.emu.controller.ControllerUtils.DOWN
import com.micewine.emu.controller.ControllerUtils.connectedVirtualControllers
import com.micewine.emu.input.InputStub.BUTTON_UNDEFINED
import com.micewine.emu.views.VirtualKeyboardInputView.Companion.SHAPE_CIRCLE
import com.micewine.emu.views.VirtualKeyboardInputView.Companion.SHAPE_DPAD
import com.micewine.emu.views.VirtualKeyboardInputView.Companion.SHAPE_RECTANGLE
import com.micewine.emu.views.VirtualKeyboardInputView.Companion.detectClick
import okhttp3.Cookie
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
    private val dpadUp: Path = Path()
    private val dpadDown: Path = Path()
    private val dpadLeft: Path = Path()
    private val dpadRight: Path = Path()
    private val startButton: Path = Path()
    private val selectButton: Path = Path()
    private val dpadList: MutableList<VirtualXInputDPad> = mutableListOf()
    private val buttonList: MutableList<VirtualControllerButton> = mutableListOf()
    private val analogList: MutableList<VirtualXInputAnalog> = mutableListOf()

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
            analogList.forEach {
                it.x = (it.x / 100F * multiplierX)
                it.y = (it.y / 100F * multiplierY)
            }
            dpadList.forEach {
                it.x = (it.x / 100F * multiplierX)
                it.y = (it.y / 100F * multiplierY)
            }
        }
    }

    init {
        buttonList.add(
            VirtualControllerButton(
                A_BUTTON,
                2065F,
                910F,
                180F,
                0,
                false,
                SHAPE_CIRCLE,
            )
        )
        buttonList.add(
            VirtualControllerButton(
                B_BUTTON,
                2205F,
                735F,
                180F,
                0,
                false,
                SHAPE_CIRCLE,
            )
        )
        buttonList.add(
            VirtualControllerButton(
                X_BUTTON,
                1925F,
                735F,
                180F,
                0,
                false,
                SHAPE_CIRCLE,
            )
        )
        buttonList.add(
            VirtualControllerButton(
                Y_BUTTON,
                2065F,
                560F,
                180F,
                0,
                false,
                SHAPE_CIRCLE,
            )
        )
        buttonList.add(
            VirtualControllerButton(
                START_BUTTON,
                1330F,
                980F,
                130F,
                0,
                false,
                SHAPE_CIRCLE,
            )
        )
        buttonList.add(
            VirtualControllerButton(
                SELECT_BUTTON,
                1120F,
                980F,
                130F,
                0,
                false,
                SHAPE_CIRCLE,
            )
        )
        buttonList.add(
            VirtualControllerButton(
                LB_BUTTON,
                280F,
                300F,
                260F,
                0,
                false,
                SHAPE_RECTANGLE,
            )
        )
        buttonList.add(
            VirtualControllerButton(
                LT_BUTTON,
                280F,
                140F,
                260F,
                0,
                false,
                SHAPE_RECTANGLE,
            )
        )
        buttonList.add(
            VirtualControllerButton(
                RB_BUTTON,
                2065F,
                300F,
                260F,
                0,
                false,
                SHAPE_RECTANGLE,
            )
        )
        buttonList.add(
            VirtualControllerButton(
                RT_BUTTON,
                2065F,
                140F,
                260F,
                0,
                false,
                SHAPE_RECTANGLE,
            )
        )
        buttonList.add(
            VirtualControllerButton(
                LS_BUTTON,
                880F,
                980F,
                180F,
                0,
                false,
                SHAPE_CIRCLE,
            )
        )
        buttonList.add(
            VirtualControllerButton(
                RS_BUTTON,
                1560F,
                980F,
                180F,
                0,
                false,
                SHAPE_CIRCLE,
            )
        )
        analogList.add(
            VirtualXInputAnalog(
                LEFT_ANALOG,
                280F,
                840F,
                275F,
                false,
                0,
                0F,
                0F
            )
        )
        analogList.add(
            VirtualXInputAnalog(
                RIGHT_ANALOG_TOUCHPAD,
                1750F,
                480F,
                275F,
                false,
                0,
                0F,
                0F
            )
        )
        dpadList.add(
            VirtualXInputDPad(
                dpadList.count() + 1,
                640F,
                480F,
                250F,
                0,
                false,
                0F,
                0F,
                0
            )
        )

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

                drawDPad(
                    dpadUp,
                    it.dpadStatus in listOf(UP, RIGHT_UP, LEFT_UP),
                    canvas
                )

                drawDPad(
                    dpadDown,
                    it.dpadStatus in listOf(DOWN, RIGHT_DOWN, LEFT_DOWN),
                    canvas
                )

                drawDPad(
                    dpadLeft,
                    it.dpadStatus in listOf(LEFT, LEFT_DOWN, LEFT_UP),
                    canvas
                )

                drawDPad(
                    dpadRight,
                    it.dpadStatus in listOf(RIGHT, RIGHT_DOWN, RIGHT_UP),
                    canvas
                )
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (virtualXInputControllerId != -1) {
            when (event.actionMasked) {
                MotionEvent.ACTION_POINTER_DOWN, MotionEvent.ACTION_DOWN -> {
                    buttonList.forEach {
                        if (detectClick(
                                event,
                                event.actionIndex,
                                it.x,
                                it.y,
                                it.radius,
                                it.shape
                            )
                        ) {
                            it.isPressed = true
                            it.fingerId = event.actionIndex

                            handleButton(it, true)

                            return@forEach
                        }
                    }
                    analogList.forEach {
                        if (detectClick(
                                event,
                                event.actionIndex,
                                it.x,
                                it.y,
                                it.radius,
                                SHAPE_CIRCLE
                            )
                        ) {
                            val posX = event.getX(event.actionIndex) - it.x
                            val posY = event.getY(event.actionIndex) - it.y

                            it.fingerX = posX
                            it.fingerY = posY
                            it.isPressed = true
                            it.fingerId = event.actionIndex

                            it.fingerX = posX
                            it.fingerY = posY

                            when (it.id) {
                                LEFT_ANALOG -> {
                                    virtualAxis(
                                        posX / (it.radius / 4),
                                        posY / (it.radius / 4),
                                        connectedVirtualControllers[virtualXInputControllerId].lx,
                                        connectedVirtualControllers[virtualXInputControllerId].ly
                                    )
                                }

                                RIGHT_ANALOG_TOUCHPAD -> {
                                    virtualAxis(
                                        posX / (it.radius / 4),
                                        posY / (it.radius / 4),
                                        connectedVirtualControllers[virtualXInputControllerId].rx,
                                        connectedVirtualControllers[virtualXInputControllerId].ry
                                    )
                                }
                            }

                            return@forEach
                        }
                    }
                    dpadList.forEach {
                        if (detectClick(
                                event,
                                event.actionIndex,
                                it.x,
                                it.y,
                                it.radius,
                                SHAPE_DPAD
                            )
                        ) {
                            val posX = event.getX(event.actionIndex) - it.x
                            val posY = event.getY(event.actionIndex) - it.y

                            it.fingerX = posX
                            it.fingerY = posY
                            it.isPressed = true
                            it.fingerId = event.actionIndex

                            it.fingerX = posX
                            it.fingerY = posY

                            when {
                                (posX / it.radius > 0.25) && !(posY / it.radius < -0.25 || posY / it.radius > 0.25) -> it.dpadStatus =
                                    RIGHT

                                (posX / it.radius < -0.25) && !(posY / it.radius < -0.25 || posY / it.radius > 0.25) -> it.dpadStatus =
                                    LEFT

                                (posY / it.radius > 0.25) && !(posX / it.radius < -0.25 || posX / it.radius > 0.25) -> it.dpadStatus =
                                    DOWN

                                (posY / it.radius < -0.25) && !(posX / it.radius < -0.25 || posX / it.radius > 0.25) -> it.dpadStatus =
                                    UP

                                (posX / it.radius > 0.25) && (posY / it.radius > 0.25) -> it.dpadStatus =
                                    RIGHT_DOWN

                                (posX / it.radius > 0.25) && (posY / it.radius < -0.25) -> it.dpadStatus =
                                    RIGHT_UP

                                (posX / it.radius < -0.25) && (posY / it.radius > 0.25) -> it.dpadStatus =
                                    LEFT_DOWN

                                (posX / it.radius < -0.25) && (posY / it.radius < -0.25) -> it.dpadStatus =
                                    LEFT_UP

                                else -> it.dpadStatus = 0
                            }

                            connectedVirtualControllers[virtualXInputControllerId].dpadStatus = it.dpadStatus

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

                                when (it.id) {
                                    LEFT_ANALOG -> {
                                        virtualAxis(
                                            posX / (it.radius / 4),
                                            posY / (it.radius / 4),
                                            connectedVirtualControllers[virtualXInputControllerId].lx,
                                            connectedVirtualControllers[virtualXInputControllerId].ly
                                        )
                                    }

                                    RIGHT_ANALOG_TOUCHPAD -> {
                                        virtualAxis(
                                            posX / (it.radius / 4),
                                            posY / (it.radius / 4),
                                            connectedVirtualControllers[virtualXInputControllerId].rx,
                                            connectedVirtualControllers[virtualXInputControllerId].ry
                                        )
                                    }
                                }

                                isFingerPressingButton = true
                            }
                        }
                        dpadList.forEach {
                            if (it.isPressed && it.fingerId == i) {
                                val posX = event.getX(i) - it.x
                                val posY = event.getY(i) - it.y

                                it.fingerX = posX
                                it.fingerY = posY

                                isFingerPressingButton = true

                                when {
                                    (posX / it.radius > 0.25) && !(posY / it.radius < -0.25 || posY / it.radius > 0.25) -> it.dpadStatus =
                                        RIGHT

                                    (posX / it.radius < -0.25) && !(posY / it.radius < -0.25 || posY / it.radius > 0.25) -> it.dpadStatus =
                                        LEFT

                                    (posY / it.radius > 0.25) && !(posX / it.radius < -0.25 || posX / it.radius > 0.25) -> it.dpadStatus =
                                        DOWN

                                    (posY / it.radius < -0.25) && !(posX / it.radius < -0.25 || posX / it.radius > 0.25) -> it.dpadStatus =
                                        UP

                                    (posX / it.radius > 0.25) && (posY / it.radius > 0.25) -> it.dpadStatus =
                                        RIGHT_DOWN

                                    (posX / it.radius > 0.25) && (posY / it.radius < -0.25) -> it.dpadStatus =
                                        RIGHT_UP

                                    (posX / it.radius < -0.25) && (posY / it.radius > 0.25) -> it.dpadStatus =
                                        LEFT_DOWN

                                    (posX / it.radius < -0.25) && (posY / it.radius < -0.25) -> it.dpadStatus =
                                        LEFT_UP

                                    else -> it.dpadStatus = 0
                                }

                                connectedVirtualControllers[virtualXInputControllerId].dpadStatus = it.dpadStatus
                            }
                        }

                        if (!isFingerPressingButton) {
                            if (event.historySize > 0) {
                                val deltaX = event.getX(i) - event.getHistoricalX(i, 0)
                                val deltaY = event.getY(i) - event.getHistoricalY(i, 0)

                                if ((deltaX > 0.08 || deltaX < -0.08) && (deltaY > 0.08 || deltaY < -0.08)) {
                                    lorieView.sendMouseEvent(
                                        deltaX,
                                        deltaY,
                                        BUTTON_UNDEFINED,
                                        false,
                                        true
                                    )
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
                        if (detectClick(
                                event,
                                event.actionIndex,
                                it.x,
                                it.y,
                                it.radius,
                                it.shape
                            )
                        ) {
                            handleButton(it, false)
                        }
                    }
                    analogList.forEach {
                        if (it.fingerId == event.actionIndex) {
                            it.fingerId = -1
                            it.fingerX = 0F
                            it.fingerY = 0F

                            it.isPressed = false

                            when (it.id) {
                                LEFT_ANALOG -> {
                                    virtualAxis(
                                        0F,
                                        0F,
                                        connectedVirtualControllers[virtualXInputControllerId].lx,
                                        connectedVirtualControllers[virtualXInputControllerId].ly
                                    )
                                }

                                RIGHT_ANALOG_TOUCHPAD -> {
                                    virtualAxis(
                                        0F,
                                        0F,
                                        connectedVirtualControllers[virtualXInputControllerId].rx,
                                        connectedVirtualControllers[virtualXInputControllerId].ry
                                    )
                                }
                            }
                        }
                    }
                    dpadList.forEach {
                        if (it.fingerId == event.actionIndex) {
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
                    analogList.forEach {
                        it.fingerX = 0F
                        it.fingerY = 0F
                        it.isPressed = false

                        when (it.id) {
                            LEFT_ANALOG -> {
                                virtualAxis(
                                    0F,
                                    0F,
                                    connectedVirtualControllers[virtualXInputControllerId].lx,
                                    connectedVirtualControllers[virtualXInputControllerId].ly
                                )
                            }

                            RIGHT_ANALOG_TOUCHPAD -> {
                                virtualAxis(
                                    0F,
                                    0F,
                                    connectedVirtualControllers[virtualXInputControllerId].rx,
                                    connectedVirtualControllers[virtualXInputControllerId].ry
                                )
                            }
                        }
                    }
                    dpadList.forEach {
                        it.fingerX = 0F
                        it.fingerY = 0F
                        it.isPressed = false
                        it.dpadStatus = 0

                        connectedVirtualControllers[virtualXInputControllerId].dpadStatus = 0
                    }

                    invalidate()
                }
            }
        }

        return true
    }

    private fun virtualAxis(
        axisX: Float,
        axisY: Float,
        x: ByteArray,
        y: ByteArray,
    ) {
        val lxStr = ((axisX.coerceIn(-1F, 1F) + 1) / 2 * 255).toInt().toString().padStart(3, '0')
        val lyStr = ((-axisY.coerceIn(-1F, 1F) + 1) / 2 * 255).toInt().toString().padStart(3, '0')

        x[0] = lxStr[0].digitToInt().toByte()
        x[1] = lxStr[1].digitToInt().toByte()
        x[2] = lxStr[2].digitToInt().toByte()

        y[0] = lyStr[0].digitToInt().toByte()
        y[1] = lyStr[1].digitToInt().toByte()
        y[2] = lyStr[2].digitToInt().toByte()
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
        var fingerId: Int,
        var isPressed: Boolean,
        var shape: Int,
    )

    class VirtualXInputDPad(
        var id: Int,
        var x: Float,
        var y: Float,
        var radius: Float,
        var fingerId: Int,
        var isPressed: Boolean,
        var fingerX: Float,
        var fingerY: Float,
        var dpadStatus: Int
    )

    class VirtualXInputAnalog(
        var id: Int,
        var x: Float,
        var y: Float,
        var radius: Float,
        var isPressed: Boolean,
        var fingerId: Int,
        var fingerX: Float,
        var fingerY: Float
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
        const val RIGHT_ANALOG_TOUCHPAD = 12
        const val LS_BUTTON = 13
        const val RS_BUTTON = 14

        var virtualXInputControllerId = -1
    }
}
