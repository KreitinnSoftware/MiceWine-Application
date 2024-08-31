package com.micewine.emu.views

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import com.micewine.emu.activities.MainActivity.Companion.d3dxRenderer
import com.micewine.emu.activities.MainActivity.Companion.enableCpuCounter
import com.micewine.emu.activities.MainActivity.Companion.enableDebugInfo
import com.micewine.emu.activities.MainActivity.Companion.enableRamCounter
import com.micewine.emu.activities.MainActivity.Companion.memoryStats
import com.micewine.emu.activities.MainActivity.Companion.miceWineVersion
import com.micewine.emu.activities.MainActivity.Companion.selectedDXVK
import com.micewine.emu.activities.MainActivity.Companion.selectedWineD3D
import com.micewine.emu.activities.MainActivity.Companion.totalCpuUsage

class HWInfoView @JvmOverloads constructor (context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0): View(context, attrs, defStyleAttr) {
    private val paint: Paint = Paint().apply {
        textSize = 30F
        strokeWidth = 8F
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (enableRamCounter) {
            drawText("RAM: $memoryStats", 20F, 40F, canvas)
        }

        if (enableCpuCounter) {
            drawText("CPU: $totalCpuUsage", 20F, 80F, canvas)
        }

        if (enableDebugInfo) {
            onScreenInfo(canvas)
        }

        invalidate()
    }

    private fun onScreenInfo(c: Canvas) {
        drawText(miceWineVersion, c.width - paint.measureText(miceWineVersion) - 20F, 40F, c)

        if (d3dxRenderer == "DXVK") {
            drawText(selectedDXVK!!, c.width - paint.measureText(selectedDXVK) - 20F, 80F, c)
        } else if (d3dxRenderer == "WineD3D") {
            drawText(selectedWineD3D!!, c.width - paint.measureText(selectedWineD3D) - 20F, 80F, c)
        }
    }

    private fun drawText(text: String, x: Float, y: Float, c: Canvas) {
        paint.style = Paint.Style.STROKE
        paint.color = Color.BLACK
        c.drawText(text, x, y, paint)

        paint.style = Paint.Style.FILL
        paint.color = Color.WHITE
        c.drawText(text, x, y, paint)
    }
}
