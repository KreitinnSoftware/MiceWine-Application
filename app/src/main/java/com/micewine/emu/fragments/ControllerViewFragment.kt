package com.micewine.emu.fragments

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.micewine.emu.R
import com.micewine.emu.adapters.AdapterControllerView
import com.micewine.emu.controller.ControllerUtils.connectedPhysicalControllers
import kotlin.math.sqrt

class ControllerViewFragment : Fragment() {
    private val controllerViewList: MutableList<AdapterControllerView.ControllerViewList> = ArrayList()
    private var rootView: View? = null
    private var recyclerView: RecyclerView? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        rootView = inflater.inflate(R.layout.fragment_controller_view, container, false)
        recyclerView = rootView?.findViewById(R.id.recyclerViewControllerView)

        setAdapter()

        return rootView
    }

    private fun setAdapter() {
        recyclerView?.setAdapter(AdapterControllerView(controllerViewList, requireContext()))

        controllerViewList.clear()

        connectedPhysicalControllers.forEach {
            addToAdapter(it.name, it.id)
        }
    }

    private fun addToAdapter(controllerName: String, controllerId: Int) {
        controllerViewList.add(
            AdapterControllerView.ControllerViewList(controllerName, controllerId)
        )
    }

    @SuppressLint("NotifyDataSetChanged")
    fun invalidateControllerView() {
        recyclerView?.adapter?.notifyDataSetChanged()
    }

    companion object {
        private fun getClampedAnalogPosition(x: Float, y: Float, lx: Float, ly: Float, radius: Float): Pair<Float, Float> {
            var dx = lx * radius
            var dy = ly * radius

            val distSquared = dx * dx + dy * dy
            val maxDist = radius * radius

            if (distSquared > maxDist) {
                val scale = radius / sqrt(distSquared)
                dx *= scale
                dy *= scale
            }

            return Pair(x + dx, y + dy)
        }

        fun getControllerBitmap(width: Int, height: Int, controllerId: Int, context: Context): Bitmap {
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val pController = connectedPhysicalControllers.firstOrNull { it.id == controllerId } ?: return bitmap
            val canvas = Canvas(bitmap)
            val paint = Paint().apply {
                strokeWidth = 8F
                color = Color.WHITE
                style = Paint.Style.STROKE
            }
            val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                textSize = 40F
                color = Color.WHITE
                textAlign = Paint.Align.CENTER
                typeface = context.resources.getFont(R.font.quicksand)
            }

            fun drawAnalog(cx: Float, cy: Float, lx: Float, ly: Float, isPressed: Boolean) {
                val analogPos = getClampedAnalogPosition(cx, cy, lx, ly, 30F)

                paint.style = if (isPressed) Paint.Style.FILL else Paint.Style.STROKE
                paint.color = Color.WHITE
                canvas.drawCircle(cx, cy, 60F, paint)

                paint.style = Paint.Style.FILL
                paint.color = if (isPressed) Color.BLACK else Color.WHITE
                canvas.drawCircle(analogPos.first, analogPos.second, 30F, paint)
            }

            drawAnalog(80F, 320F, pController.state.lx, pController.state.ly, pController.state.lsPressed)
            drawAnalog(500F, 190F, pController.state.rx, pController.state.ry, pController.state.rsPressed)

            fun drawButton(cx: Float, cy: Float, buttonName: String, isPressed: Boolean) {
                if (isPressed) {
                    paint.style = Paint.Style.FILL_AND_STROKE
                    textPaint.color = Color.BLACK
                } else {
                    paint.style = Paint.Style.STROKE
                    textPaint.color = Color.WHITE
                }
                paint.color = Color.WHITE
                canvas.drawCircle(cx, cy, 30F, paint)
                canvas.drawText(buttonName, cx, cy + 14F, textPaint)
            }

            fun drawRoundRectButton(cx: Float, cy: Float, buttonName: String, isPressed: Boolean) {
                if (isPressed) {
                    paint.style = Paint.Style.FILL_AND_STROKE
                    textPaint.color = Color.BLACK
                } else {
                    paint.style = Paint.Style.STROKE
                    textPaint.color = Color.WHITE
                }
                paint.color = Color.WHITE
                canvas.drawRoundRect(
                    cx - 30F,
                    cy - 15F,
                    cx + 60F,
                    cy + 30F,
                    12F,
                    12F,
                    paint
                )
                canvas.drawText(buttonName, cx + 15F, cy + 22F, textPaint)
            }

            drawButton(600F, 240F, "Y", pController.state.yPressed)
            drawButton(540F, 300F, "X", pController.state.xPressed)
            drawButton(660F, 300F, "B", pController.state.bPressed)
            drawButton(600F, 360F, "A", pController.state.aPressed)

            drawRoundRectButton(585F, 100F, "RB", pController.state.rbPressed)
            drawRoundRectButton(65F, 100F, "LB", pController.state.lbPressed)
            drawRoundRectButton(585F, 40F, "RT", (pController.state.rt > 0.2F))
            drawRoundRectButton(65F, 40F, "LT", (pController.state.lt > 0.2F))

            fun drawDPad(path: Path, isPressed: Boolean) {
                paint.style = if (isPressed) Paint.Style.FILL_AND_STROKE else Paint.Style.STROKE
                canvas.drawPath(path, paint)
            }

            val x = 190F
            val y = 190F
            val radius = 80F

            val dpadLeft = Path().apply {
                reset()
                moveTo(x - 10, y)
                lineTo(x - 10 - radius / 4, y - radius / 4)
                lineTo(x - 10 - radius / 4 - radius / 2, y - radius / 4)
                lineTo(x - 10 - radius / 4 - radius / 2, y - radius / 4 + radius / 2)
                lineTo(x - 10 - radius / 4, y - radius / 4 + radius / 2)
                lineTo(x - 10, y)
                close()
            }
            val dpadDown = Path().apply {
                reset()
                moveTo(x, y + 10)
                lineTo(x - radius / 4, y + 10 + radius / 4)
                lineTo(x - radius / 4, y + 10 + radius / 4 + radius / 2)
                lineTo(x - radius / 4 + radius / 2, y + 10 + radius / 4 + radius / 2)
                lineTo(x - radius / 4 + radius / 2, y + 10 + radius / 4)
                lineTo(x, y + 10)
                close()
            }
            val dpadRight = Path().apply {
                reset()
                moveTo(x + 10, y)
                lineTo(x + 10 + radius / 4, y - radius / 4)
                lineTo(x + 10 + radius / 4 + radius / 2, y - radius / 4)
                lineTo(x + 10 + radius / 4 + radius / 2, y - radius / 4 + radius / 2)
                lineTo(x + 10 + radius / 4, y - radius / 4 + radius / 2)
                lineTo(x + 10, y)
                close()
            }
            val dpadUp = Path().apply {
                reset()
                moveTo(x, y - 10)
                lineTo(x - radius / 4, y - 10 - radius / 4)
                lineTo(x - radius / 4, y - 10 - radius / 4 - radius / 2)
                lineTo(x - radius / 4 + radius / 2, y - 10 - radius / 4 - radius / 2)
                lineTo(x - radius / 4 + radius / 2, y - 10 - radius / 4)
                lineTo(x, y - 10)
                close()
            }

            drawDPad(dpadLeft, (pController.state.dpadX < -0.2F))
            drawDPad(dpadRight, (pController.state.dpadX > 0.2F))
            drawDPad(dpadUp, (pController.state.dpadY < -0.2F))
            drawDPad(dpadDown, (pController.state.dpadY > 0.2F))

            val startButton = Path().apply {
                reset()
                moveTo(380F, 350F)
                lineTo(420F, 350F)
                moveTo(380F, 360F)
                lineTo(420F, 360F)
                moveTo(380F, 370F)
                lineTo(420F, 370F)
                close()
            }
            paint.style = if (pController.state.startPressed) Paint.Style.FILL_AND_STROKE else Paint.Style.STROKE
            canvas.drawCircle(400F, 360F, 30F, paint)
            paint.strokeWidth = 6F
            paint.color = if (pController.state.startPressed) Color.BLACK else Color.WHITE
            canvas.drawPath(startButton, paint)

            val selectButton = Path().apply {
                reset()
                moveTo(295F, 377F)
                lineTo(315F, 377F)
                lineTo(315F, 357F)
                lineTo(295F, 357F)
                lineTo(295F, 380F)
                lineTo(295F, 357F)
                lineTo(315F, 357F)
                lineTo(315F, 377F)
                close()
                moveTo(305F, 352F)
                lineTo(305F, 342F)
                lineTo(285F, 342F)
                lineTo(285F, 362F)
                lineTo(285F, 342F)
                lineTo(305F, 342F)
                close()
            }
            paint.style = if (pController.state.selectPressed) Paint.Style.FILL_AND_STROKE else Paint.Style.STROKE
            paint.strokeWidth = 8F
            paint.color = Color.WHITE
            canvas.drawCircle(300F, 360F, 30F, paint)
            paint.strokeWidth = 6F
            paint.color = if (pController.state.selectPressed) Color.BLACK else Color.WHITE
            canvas.drawPath(selectButton, paint)

            return bitmap
        }
    }
}
