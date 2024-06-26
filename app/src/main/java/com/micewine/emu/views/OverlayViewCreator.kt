package com.micewine.emu.views

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import androidx.preference.PreferenceManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.micewine.emu.controller.XKeyCodes.getXKeyScanCodes
import kotlin.math.ceil

class OverlayViewCreator @JvmOverloads constructor (context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0): View(context, attrs, defStyleAttr) {
    private val buttonList = mutableListOf<CustomButtonData>()
    private val paint: Paint = Paint().apply {
        color = Color.WHITE
        textAlign = Paint.Align.CENTER
        isFakeBoldText = true
    }

    private val boxPaint: Paint = Paint().apply {
        color = Color.BLACK
        alpha = 128
    }

    private val whiteBoxPaint: Paint = Paint().apply {
        color = Color.WHITE
        alpha = 128
    }

    private val blackPaint: Paint = Paint().apply {
        color = Color.BLACK
    }

    private var selected: Int = 0

    private val box: Box = Box(20F, 20F, 400F, 0F)

    private val addButtonButton = Box(box.x + 10F, box.y + 10F, box.width - 20F, 60F)

    private fun addButton(buttonData: CustomButtonData) {
        buttonList.add(buttonData)

        saveOnPreferences(buttonData)
    }

    fun loadFromPreferences() {
        val preferences: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)!!

        val gson = Gson()

        val json = preferences.getString("overlaySettings", "")

        val listType = object : TypeToken<MutableList<CustomButtonData>>() {}.type

        val processed: MutableList<CustomButtonData> = gson.fromJson(json, listType) ?: mutableListOf()

        processed.forEach {
            addButton(it)
        }
    }

    private fun saveOnPreferences(button: CustomButtonData) {
        val preferences: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)!!

        val gson = Gson()

        val editor = preferences.edit()

        val json = preferences.getString("overlaySettings", "")

        val listType = object : TypeToken<MutableList<CustomButtonData>>() {}.type

        val processed: MutableList<CustomButtonData> = gson.fromJson(json, listType) ?: mutableListOf()

        processed.add(button)

        val newJson = gson.toJson(processed)

        editor.putString("overlaySettings", newJson)
        editor.apply()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        box.height = height.toFloat() - 40F

        buttonList.forEach {
            canvas.drawCircle(it.x, it.y, it.width / 2, paint)

            canvas.drawCircle(it.x, it.y, it.width / 2 - 10, blackPaint)

            paint.textSize = it.width / 4
            canvas.drawText(it.text, it.x, it.y + 10, paint)
        }

        canvas.drawRect(box.x, box.y, box.x + box.width, box.y + box.height, boxPaint)

        canvas.drawRect(addButtonButton.x, addButtonButton.y, addButtonButton.x + addButtonButton.width, addButtonButton.y + addButtonButton.height, whiteBoxPaint)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
         when (event.actionMasked) {
             MotionEvent.ACTION_POINTER_DOWN, MotionEvent.ACTION_DOWN -> {
                 buttonList.forEach {
                     if ((event.getX(event.actionIndex) >= it.x - it.width / 2 && event.getX(event.actionIndex) <= (it.x + (it.width / 2))) &&
                         (event.getY(event.actionIndex) >= it.y - it.width / 2 && event.getY(event.actionIndex) <= (it.y + (it.width / 2)))) {

                         return true
                     }
                 }

                 if ((event.getX(event.actionIndex) >= box.x && event.getX(event.actionIndex) <= box.x + box.width) &&
                     (event.getY(event.actionIndex) >= box.y && event.getX(event.actionIndex) <= box.y + box.height)) {

                     return true
                 }

                 addButton(CustomButtonData(buttonList.count() + 1, "Right", event.x, event.y, 150F, getXKeyScanCodes("Right")))

                 invalidate()
             }

             MotionEvent.ACTION_MOVE -> {
                 buttonList.forEach { it ->
                     if ((event.getX(event.actionIndex) >= it.x - it.width / 2 && event.getX(event.actionIndex) <= (it.x + (it.width / 2))) &&
                         (event.getY(event.actionIndex) >= it.y - it.width / 2 && event.getY(event.actionIndex) <= (it.y + (it.width / 2)))) {

                         if (selected == 0) {
                             selected = it.id
                         }

                         buttonList[buttonList.indexOfFirst {
                             it.id == selected
                         }].apply {
                             x = ceil(event.getX(event.actionIndex) / 20) * 20
                             y = ceil(event.getY(event.actionIndex) / 20) * 20
                         }

                         invalidate()
                     }
                 }

                 if ((event.getX(event.actionIndex) >= box.x && event.getX(event.actionIndex) <= box.x + box.width) &&
                     (event.getY(event.actionIndex) >= box.y && event.getX(event.actionIndex) <= box.y + box.height)) {

                     box.x = event.getX(event.actionIndex) - box.width / 2
                 }

                 return true
             }

             MotionEvent.ACTION_POINTER_UP, MotionEvent.ACTION_UP -> {
                 selected = 0
             }
         }

        return true
    }

    companion object {
        private const val PLACE = 1
        private const val MOVE = 2
    }

    class CustomButtonData(
        val id: Int,
        val text: String,
        var x: Float,
        var y: Float,
        val width: Float,
        val keyCodes: MutableList<Int>
    )

    class Box(
        var x: Float,
        var y: Float,
        var width: Float,
        var height: Float
    )

    class AddButton(
        var x: Float,
        var y: Float,
        var width: Float,
        var height: Float
    )
}
