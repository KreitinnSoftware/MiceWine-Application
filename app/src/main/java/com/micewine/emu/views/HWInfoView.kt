package com.micewine.emu.views

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import com.micewine.emu.activities.MainActivity.Companion.enableRamCounter
import com.micewine.emu.activities.MainActivity.Companion.getMemoryInfo

class HWInfoView @JvmOverloads constructor (context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0): View(context, attrs, defStyleAttr) {
    private val paint: Paint = Paint().apply {
        textSize = 30F
        strokeWidth = 8F
    }

    private var ramInfo: String? = null

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (enableRamCounter) {
            ramCounter(canvas)
        }
    }

    private fun ramCounter(c: Canvas) {
        ramInfo = getMemoryInfo(context)

        paint.style = Paint.Style.STROKE
        paint.setColor(Color.BLACK)
        c.drawText(ramInfo!!, 20F, 40F, paint)

        paint.style = Paint.Style.FILL
        paint.setColor(Color.WHITE)
        c.drawText(ramInfo!!, 20F, 40F, paint)

        invalidate()
    }
}
