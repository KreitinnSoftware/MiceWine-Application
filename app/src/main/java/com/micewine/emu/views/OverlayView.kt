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
            canvas.drawCircle(it.x, it.y, it.radius / 2, buttonPaint)
            paint.textSize = it.radius / 4
            canvas.drawText(it.keyName, it.x, it.y + 10, textPaint)
        }

        analogList.forEach {
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
                        handleButton(it, true)
                    }
                }

                analogList.forEach {
                    if (detectClick(event, it.x, it.y, it.radius)) {
                        var posX = event.getX(event.actionIndex) - it.x
                        var posY = event.getY(event.actionIndex) - it.y
                        val maxVAxisPos = it.radius / 4

                        if (posX > maxVAxisPos) {
                            posX = maxVAxisPos
                        } else if (posX < -maxVAxisPos) {
                            posX = -maxVAxisPos
                        }

                        if (posY > maxVAxisPos) {
                            posY = maxVAxisPos
                        } else if (posY < -maxVAxisPos) {
                            posY = -maxVAxisPos
                        }

                        it.fingerX = posX
                        it.fingerY = posY

                        val axisX = posX / maxVAxisPos
                        val axisY = posY / maxVAxisPos

                        it.isPressed = true

                        virtualAxis(
                            axisX,
                            axisY,
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

            MotionEvent.ACTION_MOVE -> {
                analogList.forEach {
                    if (it.isPressed) {
                        var posX = event.getX(event.actionIndex) - it.x
                        var posY = event.getY(event.actionIndex) - it.y
                        val maxVAxisPos = it.radius / 4

                        if (posX > maxVAxisPos) {
                            posX = maxVAxisPos
                        } else if (posX < -maxVAxisPos) {
                            posX = -maxVAxisPos
                        }

                        if (posY > maxVAxisPos) {
                            posY = maxVAxisPos
                        } else if (posY < -maxVAxisPos) {
                            posY = -maxVAxisPos
                        }

                        it.fingerX = posX
                        it.fingerY = posY

                        val axisX = posX / maxVAxisPos
                        val axisY = posY / maxVAxisPos

                        virtualAxis(
                            axisX,
                            axisY,
                            it.upKeyCodes!!,
                            it.downKeyCodes!!,
                            it.leftKeyCodes!!,
                            it.rightKeyCodes!!,
                            0.30F
                        )
                    } else {
                        if (detectClick(event, it.x, it.y, it.radius)) {
                            it.isPressed = true
                        }
                    }
                }

                invalidate()
            }

            MotionEvent.ACTION_POINTER_UP -> {
                buttonList.forEach {
                    if ((event.getX(event.actionIndex) >= it.x - it.radius / 2 && event.getX(event.actionIndex) <= (it.x + (it.radius / 2))) &&
                        (event.getY(event.actionIndex) >= it.y - it.radius / 2 && event.getY(event.actionIndex) <= (it.y + (it.radius / 2)))
                    ) {
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
                        0.30F
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
        lorieView.sendKeyEvent(button.keyCodes!![0], button.keyCodes!![1], pressed)
    }

    class VirtualButton(
        val id: Int,
        var x: Float,
        var y: Float,
        var radius: Float,
        var keyName: String,
        var keyCodes: List<Int>?
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
        var isPressed: Boolean
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
