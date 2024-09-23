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
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.micewine.emu.LorieView
import com.micewine.emu.controller.ControllerUtils.handleAxis
import com.micewine.emu.controller.XKeyCodes.getXKeyScanCodes

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
        strokeWidth = 10F
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
    private val gson = Gson()

    init {
        loadFromPreferences()
    }

    private fun loadFromPreferences() {
        val buttonJson = preferences.getString("overlayButtons", "")
        val axisJson = preferences.getString("overlayAxis", "")

        val virtualButtonListType = object : TypeToken<MutableList<VirtualButton>>() {}.type
        val virtualAxisListType = object : TypeToken<MutableList<VirtualAnalog>>() {}.type

        val currentButtons: MutableList<VirtualButton> = gson.fromJson(buttonJson, virtualButtonListType) ?: mutableListOf()
        val currentVAxis: MutableList<VirtualAnalog> = gson.fromJson(axisJson, virtualAxisListType) ?: mutableListOf()

        currentButtons.forEach {
            it.keyCodes = getXKeyScanCodes(it.keyName)

            buttonList.add(it)
        }

        currentVAxis.forEach {
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
            textPaint.color = if (it.isPressed) Color.GRAY else Color.WHITE

            canvas.drawCircle(it.x, it.y, it.radius / 2, buttonPaint)
            paint.textSize = it.radius / 4
            canvas.drawText(it.keyName, it.x, it.y + 10, textPaint)
        }

        analogList.forEach {
            paint.color = if (it.isPressed) Color.GRAY else Color.WHITE
            buttonPaint.color = if (it.isPressed) Color.GRAY else Color.WHITE

            canvas.drawCircle(it.x, it.y, it.radius / 2, buttonPaint)
            canvas.drawCircle(it.x + it.fingerX, it.y + it.fingerY, it.radius / 4 - 10, paint)
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_POINTER_DOWN, MotionEvent.ACTION_DOWN -> {
                buttonList.forEach {
                    if (detectClick(event, it.x, it.y, it.radius)) {
                        it.isPressed = true
                        handleButton(it, true)
                    }
                }

                analogList.forEach {
                    if (detectClick(event, it.x, it.y, it.radius)) {
                        val posX = (event.getX(event.actionIndex) - it.x).coerceIn(-it.radius / 4, it.radius / 4)
                        val posY = (event.getY(event.actionIndex) - it.y).coerceIn(-it.radius / 4, it.radius / 4)

                        it.fingerX = posX
                        it.fingerY = posY
                        it.isPressed = true

                        val axisX = posX / (it.radius / 4)
                        val axisY = posY / (it.radius / 4)

                        virtualAxis(
                            axisX,
                            axisY,
                            it.upKeyCodes!!,
                            it.downKeyCodes!!,
                            it.leftKeyCodes!!,
                            it.rightKeyCodes!!,
                            it.deadZone
                        )
                    }
                }

                invalidate()
            }

            MotionEvent.ACTION_MOVE -> {
                buttonList.forEach {
                    val clicked = detectClick(event, it.x, it.y, it.radius)

                    it.isPressed = clicked
                    handleButton(it, clicked)
                }

                analogList.forEach {
                    if (it.isPressed) {
                        val posX = (event.getX(event.actionIndex) - it.x).coerceIn(-it.radius / 4, it.radius / 4)
                        val posY = (event.getY(event.actionIndex) - it.y).coerceIn(-it.radius / 4, it.radius / 4)

                        it.fingerX = posX
                        it.fingerY = posY

                        val axisX = posX / (it.radius / 4)
                        val axisY = posY / (it.radius / 4)

                        virtualAxis(
                            axisX,
                            axisY,
                            it.upKeyCodes!!,
                            it.downKeyCodes!!,
                            it.leftKeyCodes!!,
                            it.rightKeyCodes!!,
                            it.deadZone
                        )
                    }
                }

                invalidate()
            }

            MotionEvent.ACTION_POINTER_UP -> {
                buttonList.forEach {
                    if (detectClick(event, it.x, it.y, it.radius)) {
                        handleButton(it, false)
                    }
                }

                analogList.forEach {
                    if ((event.getX(event.actionIndex) >= it.x - it.radius / 2 && event.getX(event.actionIndex) <= (it.x + (it.radius / 2))) &&
                        (event.getY(event.actionIndex) >= it.y - it.radius / 2 && event.getY(event.actionIndex) <= (it.y + (it.radius / 2)))
                    ) {
                        it.fingerX = 0F
                        it.fingerY = 0F

                        it.isPressed = false

                        virtualAxis(
                            0F,
                            0F,
                            it.upKeyCodes!!,
                            it.downKeyCodes!!,
                            it.leftKeyCodes!!,
                            it.rightKeyCodes!!,
                            0.30F
                        )
                    }
                }

                invalidate()
            }

            MotionEvent.ACTION_UP -> {
                buttonList.forEach {
                    handleButton(it, false)
                }

                analogList.forEach {
                    it.fingerX = 0F
                    it.fingerY = 0F
                    it.isPressed = false

                    virtualAxis(
                        0F,
                        0F,
                        it.upKeyCodes!!,
                        it.downKeyCodes!!,
                        it.leftKeyCodes!!,
                        it.rightKeyCodes!!,
                        it.deadZone
                    )
                }

                invalidate()
            }
        }

        return true
    }

    private fun virtualAxis(
        axisX: Float,
        axisY: Float,
        upKeyCodes: List<Int>,
        downKeyCodes: List<Int>,
        leftKeyCodes: List<Int>,
        rightKeyCodes: List<Int>,
        deadZone: Float
    ) {
        val axisXNeutral = axisX < deadZone && axisX > -deadZone
        val axisYNeutral = axisY < deadZone && axisY > -deadZone

        handleAxis(
            lorieView,
            axisX,
            axisY,
            axisXNeutral,
            axisYNeutral,
            rightKeyCodes,
            leftKeyCodes,
            downKeyCodes,
            upKeyCodes,
            deadZone
        )
    }

    private fun handleButton(button: VirtualButton, pressed: Boolean) {
        button.isPressed = pressed

        lorieView.sendKeyEvent(button.keyCodes!![0], button.keyCodes!![1], pressed)
    }

    class VirtualButton(
        val id: Int,
        var x: Float,
        var y: Float,
        var radius: Float,
        var keyName: String,
        var keyCodes: List<Int>?,
        var isPressed: Boolean
    )

    class VirtualAnalog(
        val id: Int,
        var x: Float,
        var y: Float,
        var fingerX: Float,
        var fingerY: Float,
        val radius: Float,
        var upKeyName: String,
        var upKeyCodes: List<Int>?,
        var downKeyName: String,
        var downKeyCodes: List<Int>?,
        var leftKeyName: String,
        var leftKeyCodes: List<Int>?,
        var rightKeyName: String,
        var rightKeyCodes: List<Int>?,
        var isPressed: Boolean,
        var deadZone: Float
    )

    companion object {
        val buttonList = mutableListOf<VirtualButton>()
        val analogList = mutableListOf<VirtualAnalog>()

        fun detectClick(event: MotionEvent, x: Float, y: Float, radius: Float): Boolean {
            return (event.getX(event.actionIndex) >= x - radius / 2 && event.getX(event.actionIndex) <= (x + (radius / 2))) &&
                    (event.getY(event.actionIndex) >= y - radius / 2 && event.getY(event.actionIndex) <= (y + (radius / 2)))
        }
    }
}
