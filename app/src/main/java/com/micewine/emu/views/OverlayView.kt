package com.micewine.emu.views

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.VectorDrawable
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.graphics.drawable.DrawableCompat
import androidx.preference.PreferenceManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.micewine.emu.LorieView

class OverlayView @JvmOverloads constructor (context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0): View(context, attrs, defStyleAttr) {
    private val buttonList = mutableListOf<CustomButtonData>()
    private val paint: Paint = Paint().apply {
        color = Color.WHITE
        textAlign = Paint.Align.CENTER
        isFakeBoldText = true
    }

    private val blackPaint: Paint = Paint().apply {
        color = Color.BLACK
    }

    private var lorieView: LorieView = LorieView(context)

    private fun addButton(buttonData: CustomButtonData) {
        buttonList.add(buttonData)
        invalidate()
    }

    fun loadFromPreferences(preferences: SharedPreferences) {
        val gson = Gson()

        val json = preferences.getString("overlaySettings", "")

        val listType = object : TypeToken<MutableList<CustomButtonData>>() {}.type

        val processed: MutableList<CustomButtonData> = gson.fromJson(json, listType) ?: mutableListOf()

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
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_POINTER_DOWN, MotionEvent.ACTION_DOWN -> {
                buttonList.forEach {
                    if ((event.getX(event.actionIndex) >= it.x - it.width / 2 && event.getX(event.actionIndex) <= (it.x + (it.width / 2))) &&
                        (event.getY(event.actionIndex) >= it.y - it.width / 2 && event.getY(event.actionIndex) <= (it.y + (it.width / 2)))) {
                        handleButton(it, true)
                    }
                }
            }

            MotionEvent.ACTION_POINTER_UP -> {
                buttonList.forEach {
                    if ((event.getX(event.actionIndex) >= it.x - it.width / 2 && event.getX(event.actionIndex) <= (it.x + (it.width / 2))) &&
                        (event.getY(event.actionIndex) >= it.y - it.width / 2 && event.getY(event.actionIndex) <= (it.y + (it.width / 2)))) {
                        handleButton(it, false)
                    }
                }
            }

            MotionEvent.ACTION_UP -> {
                buttonList.forEach {
                    handleButton(it, false)
                }
            }
        }

        return true
    }

    private fun handleButton(button: CustomButtonData, pressed: Boolean) {
        lorieView.sendKeyEvent(button.keyCodes[0], button.keyCodes[1], pressed)
    }

    class CustomButtonData(
            val id: Int,
            val text: String,
            val x: Float,
            val y: Float,
            val width: Float,
            val keyCodes: MutableList<Int>
    )
}
