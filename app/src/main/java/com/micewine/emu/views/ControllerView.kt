package com.micewine.emu.views

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.drawable.VectorDrawable
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import com.micewine.emu.R
import com.micewine.emu.activities.MainActivity.Companion.getNativeResolution
import com.micewine.emu.controller.ControllerUtils.LEFT
import com.micewine.emu.controller.ControllerUtils.LEFT_DOWN
import com.micewine.emu.controller.ControllerUtils.LEFT_UP
import com.micewine.emu.controller.ControllerUtils.RIGHT
import com.micewine.emu.controller.ControllerUtils.RIGHT_DOWN
import com.micewine.emu.controller.ControllerUtils.RIGHT_UP
import com.micewine.emu.controller.ControllerUtils.UP
import com.micewine.emu.controller.ControllerUtils.DOWN
import com.micewine.emu.views.OverlayView.Companion.SHAPE_CIRCLE
import com.micewine.emu.views.OverlayView.Companion.SHAPE_RECTANGLE
import com.micewine.emu.views.XInputOverlayView.Companion.A_BUTTON
import com.micewine.emu.views.XInputOverlayView.Companion.B_BUTTON
import com.micewine.emu.views.XInputOverlayView.Companion.LB_BUTTON
import com.micewine.emu.views.XInputOverlayView.Companion.LEFT_ANALOG
import com.micewine.emu.views.XInputOverlayView.Companion.LS_BUTTON
import com.micewine.emu.views.XInputOverlayView.Companion.LT_BUTTON
import com.micewine.emu.views.XInputOverlayView.Companion.RB_BUTTON
import com.micewine.emu.views.XInputOverlayView.Companion.RIGHT_ANALOG
import com.micewine.emu.views.XInputOverlayView.Companion.RS_BUTTON
import com.micewine.emu.views.XInputOverlayView.Companion.RT_BUTTON
import com.micewine.emu.views.XInputOverlayView.Companion.SELECT_BUTTON
import com.micewine.emu.views.XInputOverlayView.Companion.START_BUTTON
import com.micewine.emu.views.XInputOverlayView.Companion.X_BUTTON
import com.micewine.emu.views.XInputOverlayView.Companion.Y_BUTTON
import com.micewine.emu.views.XInputOverlayView.VirtualXInputAnalog
import com.micewine.emu.views.XInputOverlayView.VirtualXInputButton
import com.micewine.emu.views.XInputOverlayView.VirtualXInputDPad
import kotlin.math.sqrt

class ControllerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    private val paint: Paint = Paint().apply {
        color = Color.WHITE
        textAlign = Paint.Align.CENTER
        isFakeBoldText = true
    }
    private val buttonPaint: Paint = Paint().apply {
        strokeWidth = 8F
        color = Color.WHITE
        style = Paint.Style.STROKE
    }
    private val textPaint: Paint = Paint().apply {
        color = Color.WHITE
        textAlign = Paint.Align.CENTER
        textSize = 40F
    }

    private val dpadUp: Path = Path()
    private val dpadDown: Path = Path()
    private val dpadLeft: Path = Path()
    private val dpadRight: Path = Path()
    val dpadList: MutableList<VirtualXInputDPad> = mutableListOf()
    val buttonList: MutableList<VirtualXInputButton> = mutableListOf()
    val analogList: MutableList<VirtualXInputAnalog> = mutableListOf()

    private fun getBitmapFromVectorDrawable(
        context: Context,
        drawableId: Int,
        width: Int,
        height: Int
    ): Bitmap {
        val drawable = ContextCompat.getDrawable(context, drawableId) as VectorDrawable
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        return bitmap
    }

    private fun textAsBitmap(
        text: String,
        textSize: Float,
        textColor: Int,
        width: Int,
        height: Int,
        context: Context
    ): Bitmap {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            this.textSize = textSize
            this.color = textColor
            this.textAlign = Paint.Align.CENTER
            typeface = context.resources.getFont(R.font.quicksand)
        }

        val image = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(image)

        val xPos = width / 2f
        val yPos = (height / 2f) - ((paint.descent() + paint.ascent()) / 2)

        canvas.drawText(text, xPos, yPos, paint)
        return image
    }

    private fun adjustButtons() {
        val nativeResolution = getNativeResolution(context)
        val baseResolution = "2400x1080" // My Device Resolution

        if (baseResolution != nativeResolution) {
            val nativeSplit = nativeResolution.split("x").map { it.toFloat() }
            val processedSplit = baseResolution.split("x").map { it.toFloat() }

            val multiplierX = nativeSplit[0] / processedSplit[0] * 100F
            val multiplierY = nativeSplit[1] / processedSplit[1] * 100F

            buttonList.forEach {
                it.x = (it.x / 100F * multiplierX)
                it.y = (it.y / 100F * multiplierY)
            }
            analogList.forEach {
                it.x = (it.x / 100F * multiplierX)
                it.y = (it.y / 100F * multiplierY)
            }
            dpadList.forEach {
                it.x = (it.x / 100F * multiplierX)
                it.y = (it.y / 100F * multiplierY)
            }
        }
    }

    init {
        buttonList.add(
            VirtualXInputButton(
                A_BUTTON,
                2065F,
                910F,
                180F,
                0,
                false,
                SHAPE_CIRCLE,
                textAsBitmap("A", 150F, Color.WHITE, 180, 180, context)
            )
        )
        buttonList.add(
            VirtualXInputButton(
                B_BUTTON,
                2205F,
                735F,
                180F,
                0,
                false,
                SHAPE_CIRCLE,
                textAsBitmap("B", 150F, Color.WHITE, 180, 180, context)
            )
        )
        buttonList.add(
            VirtualXInputButton(
                X_BUTTON,
                1925F,
                735F,
                180F,
                0,
                false,
                SHAPE_CIRCLE,
                textAsBitmap("X", 150F, Color.WHITE, 180, 180, context)
            )
        )
        buttonList.add(
            VirtualXInputButton(
                Y_BUTTON,
                2065F,
                560F,
                180F,
                0,
                false,
                SHAPE_CIRCLE,
                textAsBitmap("Y", 150F, Color.WHITE, 180, 180, context)
            )
        )
        buttonList.add(
            VirtualXInputButton(
                SELECT_BUTTON,
                1120F,
                980F,
                100F,
                0,
                false,
                SHAPE_CIRCLE,
                getBitmapFromVectorDrawable(context, R.drawable.select_button, 100, 100)
            )
        )
        buttonList.add(
            VirtualXInputButton(
                START_BUTTON,
                1330F,
                980F,
                100F,
                0,
                false,
                SHAPE_CIRCLE,
                getBitmapFromVectorDrawable(context, R.drawable.start_button, 100, 100)
            )
        )
        buttonList.add(
            VirtualXInputButton(
                LB_BUTTON,
                280F,
                280F,
                180F,
                0,
                false,
                SHAPE_RECTANGLE,
                textAsBitmap("LB", 80F, Color.WHITE, 180, 180, context)
            )
        )
        buttonList.add(
            VirtualXInputButton(
                LT_BUTTON,
                280F,
                140F,
                180F,
                0,
                false,
                SHAPE_RECTANGLE,
                textAsBitmap("LT", 80F, Color.WHITE, 180, 180, context)
            )
        )
        buttonList.add(
            VirtualXInputButton(
                RB_BUTTON,
                2065F,
                280F,
                180F,
                0,
                false,
                SHAPE_RECTANGLE,
                textAsBitmap("RB", 80F, Color.WHITE, 180, 180, context)
            )
        )
        buttonList.add(
            VirtualXInputButton(
                RT_BUTTON,
                2065F,
                140F,
                180F,
                0,
                false,
                SHAPE_RECTANGLE,
                textAsBitmap("RT", 80F, Color.WHITE, 180, 180, context)
            )
        )
        buttonList.add(
            VirtualXInputButton(
                LS_BUTTON,
                940F,
                980F,
                140F,
                0,
                false,
                SHAPE_CIRCLE,
                textAsBitmap("LS", 80F, Color.WHITE, 140, 140, context)
            )
        )
        buttonList.add(
            VirtualXInputButton(
                RS_BUTTON,
                1500F,
                980F,
                140F,
                0,
                false,
                SHAPE_CIRCLE,
                textAsBitmap("RS", 80F, Color.WHITE, 140, 140, context)
            )
        )
        analogList.add(
            VirtualXInputAnalog(
                LEFT_ANALOG,
                280F,
                840F,
                275F,
                false,
                0,
                0F,
                0F
            )
        )
        analogList.add(
            VirtualXInputAnalog(
                RIGHT_ANALOG,
                1750F,
                210F,
                275F,
                false,
                0,
                0F,
                0F
            )
        )
        dpadList.add(
            VirtualXInputDPad(
                dpadList.count() + 1,
                640F,
                260F,
                250F,
                0,
                false,
                0F,
                0F,
                0
            )
        )

        adjustButtons()
    }

    private fun drawDPad(path: Path, pressed: Boolean, canvas: Canvas) {
        buttonPaint.color = if (pressed) Color.GRAY else Color.WHITE
        buttonPaint.alpha = 150
        textPaint.color = buttonPaint.color

        canvas.drawPath(path, buttonPaint)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        buttonList.forEach {
            buttonPaint.color = if (it.isPressed) Color.GRAY else Color.WHITE
            buttonPaint.alpha = 150

            textPaint.color = if (it.isPressed) Color.GRAY else Color.WHITE

            paint.textSize = it.radius / 4

            when (it.shape) {
                SHAPE_CIRCLE -> {
                    canvas.drawCircle(it.x, it.y, it.radius / 2, buttonPaint)
                }
                SHAPE_RECTANGLE -> {
                    canvas.drawRect(
                        it.x - it.radius / 2,
                        it.y - it.radius / 4,
                        it.x + it.radius / 2,
                        it.y + it.radius / 4,
                        buttonPaint
                    )
                }
            }

            canvas.drawBitmap(it.bitmap, it.x - it.radius / 2, it.y - it.radius / 2, buttonPaint)
        }
        analogList.forEach {
            paint.color = if (it.isPressed) Color.GRAY else Color.WHITE
            buttonPaint.color = if (it.isPressed) Color.GRAY else Color.WHITE

            paint.alpha = 150
            buttonPaint.alpha = 150

            canvas.drawCircle(it.x, it.y, it.radius / 2, buttonPaint)

            var analogX = it.x + it.fingerX
            var analogY = it.y + it.fingerY

            val distanceSquared = (it.fingerX * it.fingerX) + (it.fingerY * it.fingerY)
            val maxRadiusSquared = (it.radius / 4) * (it.radius / 4)

            if (distanceSquared > maxRadiusSquared) {
                val scale = (it.radius / 4) / sqrt(distanceSquared)
                analogX = it.x + (it.fingerX * scale)
                analogY = it.y + (it.fingerY * scale)
            }

            canvas.drawCircle(analogX, analogY, it.radius / 4, paint)
        }
        dpadList.forEach {
            canvas.apply {
                dpadLeft.apply {
                    reset()
                    moveTo(it.x - 20, it.y)
                    lineTo(it.x - 20 - it.radius / 4, it.y - it.radius / 4)
                    lineTo(it.x - 20 - it.radius / 4 - it.radius / 2, it.y - it.radius / 4)
                    lineTo(
                        it.x - 20 - it.radius / 4 - it.radius / 2,
                        it.y - it.radius / 4 + it.radius / 2
                    )
                    lineTo(it.x - 20 - it.radius / 4, it.y - it.radius / 4 + it.radius / 2)
                    lineTo(it.x - 20, it.y)
                    close()
                }
                dpadUp.apply {
                    reset()
                    moveTo(it.x, it.y - 20)
                    lineTo(it.x - it.radius / 4, it.y - 20 - it.radius / 4)
                    lineTo(it.x - it.radius / 4, it.y - 20 - it.radius / 4 - it.radius / 2)
                    lineTo(
                        it.x - it.radius / 4 + it.radius / 2,
                        it.y - 20 - it.radius / 4 - it.radius / 2
                    )
                    lineTo(it.x - it.radius / 4 + it.radius / 2, it.y - 20 - it.radius / 4)
                    lineTo(it.x, it.y - 20)
                    close()
                }
                dpadRight.apply {
                    reset()
                    moveTo(it.x + 20, it.y)
                    lineTo(it.x + 20 + it.radius / 4, it.y - it.radius / 4)
                    lineTo(it.x + 20 + it.radius / 4 + it.radius / 2, it.y - it.radius / 4)
                    lineTo(
                        it.x + 20 + it.radius / 4 + it.radius / 2,
                        it.y - it.radius / 4 + it.radius / 2
                    )
                    lineTo(it.x + 20 + it.radius / 4, it.y - it.radius / 4 + it.radius / 2)
                    lineTo(it.x + 20, it.y)
                    close()
                }
                dpadDown.apply {
                    reset()
                    moveTo(it.x, it.y + 20)
                    lineTo(it.x - it.radius / 4, it.y + 20 + it.radius / 4)
                    lineTo(it.x - it.radius / 4, it.y + 20 + it.radius / 4 + it.radius / 2)
                    lineTo(
                        it.x - it.radius / 4 + it.radius / 2,
                        it.y + 20 + it.radius / 4 + it.radius / 2
                    )
                    lineTo(it.x - it.radius / 4 + it.radius / 2, it.y + 20 + it.radius / 4)
                    lineTo(it.x, it.y + 20)
                    close()
                }
                drawDPad(
                    dpadUp,
                    it.dpadStatus in listOf(UP, RIGHT_UP, LEFT_UP),
                    canvas
                )
                drawDPad(
                    dpadDown,
                    it.dpadStatus in listOf(DOWN, RIGHT_DOWN, LEFT_DOWN),
                    canvas
                )
                drawDPad(
                    dpadLeft,
                    it.dpadStatus in listOf(LEFT, LEFT_DOWN, LEFT_UP),
                    canvas
                )
                drawDPad(
                    dpadRight,
                    it.dpadStatus in listOf(RIGHT, RIGHT_DOWN, RIGHT_UP),
                    canvas
                )
            }
        }
    }
}
