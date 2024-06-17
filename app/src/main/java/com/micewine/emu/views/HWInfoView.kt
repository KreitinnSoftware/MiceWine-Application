package com.micewine.emu.views

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import com.micewine.emu.activities.MainActivity.Companion.enableCpuCounter
import com.micewine.emu.activities.MainActivity.Companion.enableRamCounter
import com.micewine.emu.activities.MainActivity.Companion.memoryStats
import com.micewine.emu.activities.MainActivity.Companion.totalCpuUsage

class HWInfoView @JvmOverloads constructor (context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0): View(context, attrs, defStyleAttr) {
    private val paint: Paint = Paint().apply {
        textSize = 30F
        strokeWidth = 8F
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (enableRamCounter) {
            ramCounter(canvas)
        }

        if (enableCpuCounter) {
            cpuUsageCounter(canvas)
        }
    }

    private fun ramCounter(c: Canvas) {
        paint.style = Paint.Style.STROKE
        paint.setColor(Color.BLACK)
        c.drawText("RAM: $memoryStats", 20F, 40F, paint)

        paint.style = Paint.Style.FILL
        paint.setColor(Color.WHITE)
        c.drawText("RAM: $memoryStats", 20F, 40F, paint)

        invalidate()
    }

    private fun cpuUsageCounter(c: Canvas) {
        paint.style = Paint.Style.STROKE
        paint.setColor(Color.BLACK)
        c.drawText("CPU: $totalCpuUsage", 20F, 80F, paint)

        paint.style = Paint.Style.FILL
        paint.setColor(Color.WHITE)
        c.drawText("CPU: $totalCpuUsage", 20F, 80F, paint)

        invalidate()
    }
}
