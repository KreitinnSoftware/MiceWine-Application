package com.micewine.emu.views

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
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

    private val blackPaint: Paint = Paint().apply {
        color = Color.BLACK
    }

    private var selected: Int = 0

    private val box: BoxProperties = BoxProperties(20F, 20F, 400F, 0F)

    private fun addButton(buttonData: CustomButtonData) {
        buttonList.add(buttonData)
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

    class BoxProperties(
        var x: Float,
        var y: Float,
        var width: Float,
        var height: Float
    )
}
