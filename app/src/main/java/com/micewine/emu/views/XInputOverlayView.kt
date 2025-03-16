package com.micewine.emu.views

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.drawable.VectorDrawable
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.core.content.ContextCompat
import com.micewine.emu.LorieView
import com.micewine.emu.R
import com.micewine.emu.controller.ControllerUtils.GamePadServer.Companion.DPAD_DOWN
import com.micewine.emu.controller.ControllerUtils.GamePadServer.Companion.DPAD_LEFT
import com.micewine.emu.controller.ControllerUtils.GamePadServer.Companion.DPAD_LEFT_DOWN
import com.micewine.emu.controller.ControllerUtils.GamePadServer.Companion.DPAD_LEFT_UP
import com.micewine.emu.controller.ControllerUtils.GamePadServer.Companion.DPAD_RIGHT
import com.micewine.emu.controller.ControllerUtils.GamePadServer.Companion.DPAD_RIGHT_DOWN
import com.micewine.emu.controller.ControllerUtils.GamePadServer.Companion.DPAD_RIGHT_UP
import com.micewine.emu.controller.ControllerUtils.GamePadServer.Companion.DPAD_UP
import com.micewine.emu.input.InputStub.BUTTON_UNDEFINED
import com.micewine.emu.views.OverlayView.Companion.SHAPE_CIRCLE
import com.micewine.emu.views.OverlayView.Companion.SHAPE_RECTANGLE
import com.micewine.emu.views.OverlayView.Companion.SHAPE_SQUARE
import com.micewine.emu.views.OverlayView.Companion.detectClick
import kotlin.math.sqrt

class XInputOverlayView @JvmOverloads constructor(
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
    private val dpadUp: Path = Path()
    private val dpadDown: Path = Path()
    private val dpadLeft: Path = Path()
    private val dpadRight: Path = Path()
    private val dpadList: MutableList<VirtualXInputDPad> = mutableListOf()
    private val buttonList: MutableList<VirtualXInputButton> = mutableListOf()
    private val analogList: MutableList<VirtualXInputAnalog> = mutableListOf()

    private val aButtonIcon = getBitmapFromVectorDrawable(context, R.drawable.a_button, 180, 180)
    private val bButtonIcon = getBitmapFromVectorDrawable(context, R.drawable.a_button, 180, 180)
    private val xButtonIcon = getBitmapFromVectorDrawable(context, R.drawable.a_button, 180, 180)
    private val yButtonIcon = getBitmapFromVectorDrawable(context, R.drawable.a_button, 180, 180)

    private fun getBitmapFromVectorDrawable(context: Context, drawableId: Int, width: Int, height: Int): Bitmap {
        val drawable = ContextCompat.getDrawable(context, drawableId) as VectorDrawable
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        return bitmap
    }

    init {
        buttonList.add(
            VirtualXInputButton(
                A_BUTTON, width - 150F, 100F, 180F, 0, false, SHAPE_CIRCLE
            )
        )
        buttonList.add(
            VirtualXInputButton(
                B_BUTTON, 200F, 100F, 180F, 0, false, SHAPE_CIRCLE
            )
        )
        buttonList.add(
            VirtualXInputButton(
                X_BUTTON, 100F, 100F, 180F, 0, false, SHAPE_CIRCLE
            )
        )
        buttonList.add(
            VirtualXInputButton(
                Y_BUTTON, 200F, 100F, 180F, 0, false, SHAPE_CIRCLE
            )
        )
    }

    private fun drawDPad(path: Path, pressed: Boolean, canvas: Canvas) {
        buttonPaint.color = if (pressed) Color.GRAY else Color.WHITE
        buttonPaint.alpha = 150
        textPaint.color = buttonPaint.color

        canvas.drawPath(path, buttonPaint)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        buttonList.forEach {
            buttonPaint.color = if (it.isPressed) Color.GRAY else Color.WHITE
            buttonPaint.alpha = 150

            textPaint.color = if (it.isPressed) Color.GRAY else Color.WHITE

            paint.textSize = it.radius / 4

            when (it.shape) {
                SHAPE_CIRCLE -> {
                    canvas.drawCircle(it.x, it.y, it.radius / 2, buttonPaint)
                }
                SHAPE_SQUARE -> {
                    canvas.drawRect(
                        it.x - it.radius / 2,
                        it.y - it.radius / 2,
                        it.x + it.radius / 2,
                        it.y + it.radius / 2,
                        buttonPaint
                    )
                }
                SHAPE_RECTANGLE -> {
                    canvas.drawRect(
                        it.x - it.radius / 2,
                        it.y - it.radius / 4,
                        it.x + it.radius / 2,
                        it.y + it.radius / 4,
                        buttonPaint
                    )
                }
            }

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

                drawDPad(dpadUp, it.dpadStatus in listOf(DPAD_UP, DPAD_RIGHT_UP, DPAD_LEFT_UP), canvas)

                drawDPad(dpadDown, it.dpadStatus in listOf(DPAD_DOWN, DPAD_RIGHT_DOWN, DPAD_LEFT_DOWN), canvas)

                drawDPad(dpadLeft, it.dpadStatus in listOf(DPAD_LEFT, DPAD_LEFT_DOWN, DPAD_LEFT_UP), canvas)

                drawDPad(dpadRight, it.dpadStatus in listOf(DPAD_RIGHT, DPAD_RIGHT_DOWN, DPAD_RIGHT_UP), canvas)
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
                        it.fingerId = event.actionIndex


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
                        it.fingerId = event.actionIndex

                        it.fingerX = posX
                        it.fingerY = posY


                        return@forEach
                    }
                }
                dpadList.forEach {
                    if (detectClick(event, event.actionIndex, it.x, it.y, it.radius, 3)) {
                        val posX = event.getX(event.actionIndex) - it.x
                        val posY = event.getY(event.actionIndex) - it.y

                        it.fingerX = posX
                        it.fingerY = posY
                        it.isPressed = true
                        it.fingerId = event.actionIndex

                        it.fingerX = posX
                        it.fingerY = posY

                        when {
                            (posX / it.radius > 0.25) && !(posY / it.radius < -0.25 || posY / it.radius > 0.25) -> it.dpadStatus = DPAD_RIGHT
                            (posX / it.radius < -0.25) && !(posY / it.radius < -0.25 || posY / it.radius > 0.25) -> it.dpadStatus = DPAD_LEFT
                            (posY / it.radius > 0.25) && !(posX / it.radius < -0.25 || posX / it.radius > 0.25) -> it.dpadStatus = DPAD_DOWN
                            (posY / it.radius < -0.25) && !(posX / it.radius < -0.25 || posX / it.radius > 0.25) -> it.dpadStatus = DPAD_UP
                            (posX / it.radius > 0.25) && (posY / it.radius > 0.25) -> it.dpadStatus = DPAD_RIGHT_DOWN
                            (posX / it.radius > 0.25) && (posY / it.radius < -0.25) -> it.dpadStatus = DPAD_RIGHT_UP
                            (posX / it.radius < -0.25) && (posY / it.radius > 0.25) -> it.dpadStatus = DPAD_LEFT_DOWN
                            (posX / it.radius < -0.25) && (posY / it.radius < -0.25) -> it.dpadStatus = DPAD_LEFT_UP

                            else -> it.dpadStatus = -1
                        }


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

                            isFingerPressingButton = true
                        }
                    }
                    analogList.forEach {
                        if (it.isPressed && it.fingerId == i) {
                            val posX = event.getX(i) - it.x
                            val posY = event.getY(i) - it.y

                            it.fingerX = posX
                            it.fingerY = posY

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
                                (posX / it.radius > 0.25) && !(posY / it.radius < -0.25 || posY / it.radius > 0.25) -> it.dpadStatus = DPAD_RIGHT
                                (posX / it.radius < -0.25) && !(posY / it.radius < -0.25 || posY / it.radius > 0.25) -> it.dpadStatus = DPAD_LEFT
                                (posY / it.radius > 0.25) && !(posX / it.radius < -0.25 || posX / it.radius > 0.25) -> it.dpadStatus = DPAD_DOWN
                                (posY / it.radius < -0.25) && !(posX / it.radius < -0.25 || posX / it.radius > 0.25) -> it.dpadStatus = DPAD_UP
                                (posX / it.radius > 0.25) && (posY / it.radius > 0.25) -> it.dpadStatus = DPAD_RIGHT_DOWN
                                (posX / it.radius > 0.25) && (posY / it.radius < -0.25) -> it.dpadStatus = DPAD_RIGHT_UP
                                (posX / it.radius < -0.25) && (posY / it.radius > 0.25) -> it.dpadStatus = DPAD_LEFT_DOWN
                                (posX / it.radius < -0.25) && (posY / it.radius < -0.25) -> it.dpadStatus = DPAD_LEFT_UP

                                else -> it.dpadStatus = -1
                            }

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
                    if (detectClick(event, event.actionIndex, it.x, it.y, it.radius, it.shape)) {
                    }
                }
                analogList.forEach {
                    if (it.fingerId == event.actionIndex) {
                        it.fingerId = -1
                        it.fingerX = 0F
                        it.fingerY = 0F

                        it.isPressed = false
                    }
                }
                dpadList.forEach {
                    if (it.fingerId == event.actionIndex) {
                        it.fingerId = -1
                        it.fingerX = 0F
                        it.fingerY = 0F

                        it.isPressed = false
                        it.dpadStatus = -1
                    }
                }

                invalidate()
            }

            MotionEvent.ACTION_UP -> {
                buttonList.forEach {
                    it.fingerId = -1
                }
                analogList.forEach {
                    it.fingerX = 0F
                    it.fingerY = 0F
                    it.isPressed = false

                }
                dpadList.forEach {
                    it.fingerX = 0F
                    it.fingerY = 0F
                    it.isPressed = false
                    it.dpadStatus = -1

                }

                invalidate()
            }
        }

        return true
    }

    class VirtualXInputButton(
        var id: Int,
        var x: Float,
        var y: Float,
        var radius: Float,
        var fingerId: Int,
        var isPressed: Boolean,
        var shape: Int
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
        var type: Int,
        var isPressed: Boolean,
        var fingerId: Int,
        var fingerX: Float,
        var fingerY: Float,
        var deadZone: Float
    )

    companion object {
        const val A_BUTTON = 0
        const val B_BUTTON = 1
        const val X_BUTTON = 2
        const val Y_BUTTON = 3
    }
}
