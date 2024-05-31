package com.micewine.emu

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.VectorDrawable
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.graphics.drawable.DrawableCompat

data class CustomButtonData(
    val id: Int,
    val imageResource: Int,
    val x: Float,
    val y: Float,
    val radius: Float,
    val keyCodes: MutableList<Int>
)

class OverlayView @JvmOverloads constructor (context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0): View(context, attrs, defStyleAttr) {
    private val buttonList = mutableListOf<CustomButtonData>()
    private val paint: Paint = Paint()
    private lateinit var bitmap: MutableList<Bitmap>
    private var lorieView: LorieView = LorieView(context)

    fun addButton(buttonData: CustomButtonData) {
        buttonList.add(buttonData)
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        paint.color = Color.WHITE

        if (!::bitmap.isInitialized) {
            bitmap = mutableListOf()
            buttonList.forEach {
                bitmap.add(getBitmap(it))
            }
        }

        buttonList.forEachIndexed { index, button ->
            canvas.drawBitmap(bitmap[index], button.x, button.y, paint)
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                buttonList.forEach {
                    if (event.x >= it.x && event.x <= (it.x + it.radius)
                        && event.y >= it.y && event.y <= (it.y + it.radius)
                    ) {
                        handleButton(it, true)

                        return true
                    }
                }
            }

            MotionEvent.ACTION_UP -> {
                buttonList.forEach {
                    if (event.x >= it.x && event.x <= (it.x + it.radius)
                        && event.y >= it.y && event.y <= (it.y + it.radius)
                    ) {
                        handleButton(it, false)

                        return true
                    }
                }
            }

            MotionEvent.ACTION_MOVE -> {
                buttonList.forEach {
                    if (event.x >= it.x && event.x <= (it.x + it.radius)
                        && event.y >= it.y && event.y <= (it.y + it.radius)
                    ) {
                        handleButton(it, true)

                        return true
                    } else {
                        handleButton(it, false)
                    }
                }
            }
        }
        performClick()

        return super.onTouchEvent(event)
    }

    override fun performClick(): Boolean {
        super.performClick()

        return true
    }

    private fun handleButton(button: CustomButtonData, pressed: Boolean) {
        lorieView.sendKeyEvent(button.keyCodes[0], button.keyCodes[1], pressed)
    }

    private fun getBitmap(button: CustomButtonData): Bitmap {
        val vectorDrawable: VectorDrawable = DrawableCompat.unwrap(AppCompatResources.getDrawable(context, button.imageResource)!!) as VectorDrawable

        val bitmap = Bitmap.createBitmap(
            button.radius.toInt(),
            button.radius.toInt(),
            Bitmap.Config.ARGB_8888
        )

        val canvas = Canvas(bitmap)

        vectorDrawable.setBounds(0, 0, canvas.width, canvas.height)
        vectorDrawable.draw(canvas)

        return bitmap
    }
}
