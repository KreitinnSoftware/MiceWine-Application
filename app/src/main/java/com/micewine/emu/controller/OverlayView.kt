package com.micewine.emu.controller

import android.annotation.SuppressLint
import android.content.Context
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
import com.micewine.emu.LorieView
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

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_POINTER_DOWN, MotionEvent.ACTION_DOWN -> {
                buttonList.forEach {
                    if (event.getX(event.actionIndex) >= it.x && event.getX(event.actionIndex) <= (it.x + it.radius)
                            && event.getY(event.actionIndex) >= it.y && event.getY(event.actionIndex) <= (it.y + it.radius)
                            ) {
                        handleButton(it, true)
                    }
                }
            }

            MotionEvent.ACTION_POINTER_UP -> {
                buttonList.forEach {
                    if (event.getX(event.actionIndex) >= it.x && event.getX(event.actionIndex) <= (it.x + it.radius)
                            && event.getY(event.actionIndex) >= it.y && event.getY(event.actionIndex) <= (it.y + it.radius)
                            ) {
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

    class CustomButtonData(
            val id: Int,
            val imageResource: Int,
            val x: Float,
            val y: Float,
            val radius: Float,
            val keyCodes: MutableList<Int>
    )
}
