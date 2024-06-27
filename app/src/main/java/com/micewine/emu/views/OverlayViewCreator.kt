package com.micewine.emu.views

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.widget.Spinner
import androidx.preference.PreferenceManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class OverlayViewCreator @JvmOverloads constructor (context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0): View(context, attrs, defStyleAttr) {
    private val box: Box = Box(20F, 20F, 450F, 140F)

    val buttonList = mutableListOf<VirtualButton>()

    val analogList = mutableListOf<VirtualAnalog>()

    private val paint = Paint().apply {
        color = Color.BLACK
        alpha = 200
    }

    private val whitePaint = Paint().apply {
        color = Color.WHITE
    }

    private val buttonPaint: Paint = Paint().apply {
        strokeWidth = 10F
        color = Color.WHITE
        style = Paint.Style.STROKE
    }

    private val textPaint: Paint = Paint().apply {
        color = Color.WHITE
        textAlign = Paint.Align.CENTER
        isFakeBoldText = true
    }

    private var selectedButton = 0

    private var selectedVAxis = 0

    private val preferences = PreferenceManager.getDefaultSharedPreferences(context)

    private val gson = Gson()

    var spinner: Spinner? = null

    private fun loadFromPreferences() {
        val buttonJson = preferences.getString("overlayButtons", "")
        val axisJson = preferences.getString("overlayAxis", "")

        val virtualButtonListType = object : TypeToken<MutableList<VirtualButton>>() {}.type
        val virtualAxisListType = object : TypeToken<MutableList<VirtualAnalog>>() {}.type

        val currentButtons: MutableList<VirtualButton> = gson.fromJson(buttonJson, virtualButtonListType) ?: mutableListOf()
        val currentVAxis: MutableList<VirtualAnalog> = gson.fromJson(axisJson, virtualAxisListType) ?: mutableListOf()

        currentButtons.forEach {
            buttonList.add(it)
        }

        currentVAxis.forEach {
            analogList.add(it)
        }
    }

    fun saveOnPreferences() {
        val editor = preferences.edit()

        val buttonJson = gson.toJson(buttonList)
        val axisJson = gson.toJson(analogList)

        editor.putString("overlayButtons", buttonJson)
        editor.putString("overlayAxis", axisJson)
        editor.apply()
    }

    init {
        loadFromPreferences()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        canvas.drawRoundRect(box.x, box.y, box.x + box.width, box.height, 50F, 50F, paint)

        buttonList.forEach {
            canvas.drawCircle(it.x, it.y, it.width / 2, buttonPaint)

            paint.textSize = it.width / 4
            canvas.drawText(it.text, it.x, it.y + 10, textPaint)
        }

        analogList.forEach {
            canvas.drawCircle(it.x, it.y, it.width / 2, buttonPaint)

            canvas.drawCircle(it.x, it.y, it.width / 4 - 10, whitePaint)
        }
    }

    fun addButton(buttonData: VirtualButton) {
        buttonList.add(buttonData)
        invalidate()
    }

    fun addAnalog(buttonData: VirtualAnalog) {
        analogList.add(buttonData)
        invalidate()
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
         when (event.actionMasked) {
             MotionEvent.ACTION_DOWN, MotionEvent.ACTION_POINTER_DOWN -> {
                 buttonList.forEach {
                     if ((event.getX(event.actionIndex) >= it.x - it.width / 2 && event.getX(event.actionIndex) <= (it.x + (it.width / 2))) &&
                         (event.getY(event.actionIndex) >= it.y - it.width / 2 && event.getY(event.actionIndex) <= (it.y + (it.width / 2)))) {

                         if (selectedButton == 0) {
                             selectedButton = it.id
                         }
                     }
                 }

                 analogList.forEach {
                     if ((event.getX(event.actionIndex) >= it.x - it.width / 2 && event.getX(event.actionIndex) <= (it.x + (it.width / 2))) &&
                         (event.getY(event.actionIndex) >= it.y - it.width / 2 && event.getY(event.actionIndex) <= (it.y + (it.width / 2)))) {

                         if (selectedVAxis == 0) {
                             selectedVAxis = it.id
                         }
                     }
                 }

                 invalidate()
             }

             MotionEvent.ACTION_MOVE -> {
                 if ((event.getX(event.actionIndex) >= box.x && event.getX(event.actionIndex) <= box.x + box.width) &&
                     (event.getY(event.actionIndex) >= box.y && event.getY(event.actionIndex) <= box.y + box.height)) {
                     box.x = event.getX(event.actionIndex) - box.width / 2
                 }

                 buttonList.forEach {
                     if ((event.getX(event.actionIndex) >= it.x - it.width / 2 && event.getX(event.actionIndex) <= (it.x + (it.width / 2))) &&
                         (event.getY(event.actionIndex) >= it.y - it.width / 2 && event.getY(event.actionIndex) <= (it.y + (it.width / 2)))) {

                         if (selectedButton > 0) {
                             buttonList[buttonList.indexOfFirst { i ->
                                 i.id == selectedButton
                             }].apply {
                                 x = event.getX(event.actionIndex)
                                 y = event.getY(event.actionIndex)
                             }
                         }
                     }
                 }

                 analogList.forEach {
                     if ((event.getX(event.actionIndex) >= it.x - it.width / 2 && event.getX(event.actionIndex) <= (it.x + (it.width / 2))) &&
                         (event.getY(event.actionIndex) >= it.y - it.width / 2 && event.getY(event.actionIndex) <= (it.y + (it.width / 2)))) {

                         if (selectedVAxis > 0) {
                             analogList[analogList.indexOfFirst { i ->
                                 i.id == selectedVAxis
                             }].apply {
                                 x = event.getX(event.actionIndex)
                                 y = event.getY(event.actionIndex)
                             }
                         }
                     }
                 }

                 invalidate()
             }

             MotionEvent.ACTION_POINTER_UP, MotionEvent.ACTION_UP -> {
                 buttonList.forEach {
                     if ((event.getX(event.actionIndex) >= it.x - it.width / 2 && event.getX(event.actionIndex) <= (it.x + (it.width / 2))) &&
                         (event.getY(event.actionIndex) >= it.y - it.width / 2 && event.getY(event.actionIndex) <= (it.y + (it.width / 2)))
                     ) {
                         if (selectedButton == it.id) {
                             selectedButton = 0
                         }
                     }
                 }

                 analogList.forEach {
                     if ((event.getX(event.actionIndex) >= it.x - it.width / 2 && event.getX(event.actionIndex) <= (it.x + (it.width / 2))) &&
                         (event.getY(event.actionIndex) >= it.y - it.width / 2 && event.getY(event.actionIndex) <= (it.y + (it.width / 2)))
                     ) {
                         if (selectedVAxis == it.id) {
                             selectedVAxis = 0
                         }
                     }
                 }
             }
         }

        return true
    }

    class Box(
        var x: Float,
        var y: Float,
        var width: Float,
        var height: Float
    )

    class VirtualButton(
        val id: Int,
        val text: String,
        var x: Float,
        var y: Float,
        var width: Float,
        val keyCodes: List<Int>
    )

    class VirtualAnalog(
        val id: Int,
        var x: Float,
        var y: Float,
        var fingerX: Float,
        var fingerY: Float,
        var width: Float,
        val upKeyCodes: List<Int>,
        val downKeyCodes: List<Int>,
        val leftKeyCodes: List<Int>,
        val rightKeyCodes: List<Int>,
        var isPressed: Boolean
    )
}
