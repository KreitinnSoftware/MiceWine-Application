package com.micewine.emu.views

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
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
    private val buttonList = mutableListOf<VirtualButton>()
    private val analogList = mutableListOf<VirtualAnalog>()
    private val paint: Paint = Paint().apply {
        color = Color.WHITE
        textAlign = Paint.Align.CENTER
        isFakeBoldText = true
    }

    private val blackPaint: Paint = Paint().apply {
        color = Color.BLACK
    }

    private var lorieView: LorieView = LorieView(context)

    private fun addAnalog(analogData: VirtualAnalog) {
        analogList.add(analogData)
        invalidate()
    }

    init {
        addAnalog(
            VirtualAnalog(0, 200F, 200F, 0F, 0F, 250F, getXKeyScanCodes("Up"), getXKeyScanCodes("Down"), getXKeyScanCodes("Left"), getXKeyScanCodes("Right"))
        )
    }

    private fun addButton(buttonData: VirtualButton) {
        buttonList.add(buttonData)
        invalidate()
    }

    fun loadFromPreferences(preferences: SharedPreferences) {
        val gson = Gson()

        val json = preferences.getString("overlaySettings", "")

        val listType = object : TypeToken<MutableList<VirtualButton>>() {}.type

        val processed: MutableList<VirtualButton> =
            gson.fromJson(json, listType) ?: mutableListOf()

        processed.forEach {
            addButton(it)
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        buttonList.forEach {
            canvas.drawCircle(it.x, it.y, it.width / 2, paint)

            canvas.drawCircle(it.x, it.y, it.width / 2 - 10, blackPaint)

            paint.textSize = it.width / 4
            canvas.drawText(it.text, it.x, it.y + 10, paint)
        }

        analogList.forEach {
            canvas.drawCircle(it.x, it.y, it.width / 2, paint)

            canvas.drawCircle(it.x, it.y, it.width / 2 - 10, blackPaint)

            canvas.drawCircle(it.x + it.fingerX, it.y + it.fingerY, it.width / 4 - 10, paint)
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_POINTER_DOWN, MotionEvent.ACTION_DOWN -> {
                buttonList.forEach {
                    if ((event.getX(event.actionIndex) >= it.x - it.width / 2 && event.getX(event.actionIndex) <= (it.x + (it.width / 2))) &&
                        (event.getY(event.actionIndex) >= it.y - it.width / 2 && event.getY(event.actionIndex) <= (it.y + (it.width / 2)))
                    ) {
                        handleButton(it, true)
                    }
                }

                analogList.forEach {
                    if ((event.getX(event.actionIndex) >= it.x - it.width / 2 && event.getX(event.actionIndex) <= (it.x + (it.width / 2))) &&
                        (event.getY(event.actionIndex) >= it.y - it.width / 2 && event.getY(event.actionIndex) <= (it.y + (it.width / 2)))
                    ) {
                        var posX = event.getX(event.actionIndex) - it.x
                        var posY = event.getY(event.actionIndex) - it.y
                        val maxVAxisPos = it.width / 4

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

                        invalidate()
                    }
                }
            }

            MotionEvent.ACTION_MOVE -> {
                analogList.forEach {
                    var posX = event.getX(event.actionIndex) - it.x
                    var posY = event.getY(event.actionIndex) - it.y
                    val maxVAxisPos = it.width / 4

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

                    virtualAxis(axisX, axisY, it.upKeyCodes, it.downKeyCodes, it.leftKeyCodes, it.rightKeyCodes)

                    invalidate()
                }
            }

            MotionEvent.ACTION_POINTER_UP -> {
                buttonList.forEach {
                    if ((event.getX(event.actionIndex) >= it.x - it.width / 2 && event.getX(event.actionIndex) <= (it.x + (it.width / 2))) &&
                        (event.getY(event.actionIndex) >= it.y - it.width / 2 && event.getY(event.actionIndex) <= (it.y + (it.width / 2)))
                    ) {
                        handleButton(it, false)
                    }
                }

                analogList.forEach {
                    if ((event.getX(event.actionIndex) >= it.x - it.width / 2 && event.getX(event.actionIndex) <= (it.x + (it.width / 2))) &&
                        (event.getY(event.actionIndex) >= it.y - it.width / 2 && event.getY(event.actionIndex) <= (it.y + (it.width / 2)))
                    ) {
                        it.fingerX = 0F
                        it.fingerY = 0F

                        virtualAxis(0F, 0F, it.upKeyCodes, it.downKeyCodes, it.leftKeyCodes, it.rightKeyCodes)

                        invalidate()
                    }
                }
            }

            MotionEvent.ACTION_UP -> {
                buttonList.forEach {
                    handleButton(it, false)
                }

                analogList.forEach {
                    it.fingerX = 0F
                    it.fingerY = 0F

                    virtualAxis(0F, 0F, it.upKeyCodes, it.downKeyCodes, it.leftKeyCodes, it.rightKeyCodes)
                }

                invalidate()
            }
        }

        return true
    }

    private fun virtualAxis(axisX: Float, axisY: Float, upKeyCodes: List<Int>, downKeyCodes: List<Int>, leftKeyCodes: List<Int>, rightKeyCodes: List<Int>) {
        val axisXNeutral = axisX < 0.25F && axisX > -0.25F
        val axisYNeutral = axisY < 0.25F && axisY > -0.25F

        handleAxis(lorieView, axisX, axisY, axisXNeutral, axisYNeutral, rightKeyCodes, leftKeyCodes, downKeyCodes, upKeyCodes, 0.25F)
    }

    private fun handleButton(button: VirtualButton, pressed: Boolean) {
        lorieView.sendKeyEvent(button.keyCodes[0], button.keyCodes[1], pressed)
    }

    class VirtualButton(
        val id: Int,
        val text: String,
        val x: Float,
        val y: Float,
        val width: Float,
        val keyCodes: List<Int>
    )

    class VirtualAnalog(
        val id: Int,
        val x: Float,
        val y: Float,
        var fingerX: Float,
        var fingerY: Float,
        val width: Float,
        val upKeyCodes: List<Int>,
        val downKeyCodes: List<Int>,
        val leftKeyCodes: List<Int>,
        val rightKeyCodes: List<Int>
    )
}
