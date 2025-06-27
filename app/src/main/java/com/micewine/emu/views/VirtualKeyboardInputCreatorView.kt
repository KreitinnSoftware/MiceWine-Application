package com.micewine.emu.views

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.drawable.VectorDrawable
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.core.content.ContextCompat
import com.micewine.emu.R
import com.micewine.emu.activities.MainActivity.Companion.getNativeResolution
import com.micewine.emu.activities.VirtualControllerOverlayMapper.Companion.ACTION_EDIT_VIRTUAL_BUTTON
import com.micewine.emu.adapters.AdapterPreset.Companion.clickedPresetName
import com.micewine.emu.fragments.EditVirtualButtonFragment.Companion.selectedAnalogDownKeyName
import com.micewine.emu.fragments.EditVirtualButtonFragment.Companion.selectedAnalogLeftKeyName
import com.micewine.emu.fragments.EditVirtualButtonFragment.Companion.selectedAnalogRightKeyName
import com.micewine.emu.fragments.EditVirtualButtonFragment.Companion.selectedAnalogUpKeyName
import com.micewine.emu.fragments.EditVirtualButtonFragment.Companion.selectedButtonKeyName
import com.micewine.emu.fragments.EditVirtualButtonFragment.Companion.selectedButtonRadius
import com.micewine.emu.fragments.EditVirtualButtonFragment.Companion.selectedButtonShape
import com.micewine.emu.fragments.VirtualControllerPresetManagerFragment.Companion.getVirtualControllerPreset
import com.micewine.emu.fragments.VirtualControllerPresetManagerFragment.Companion.putVirtualControllerPreset
import com.micewine.emu.views.VirtualKeyboardInputView.Companion.SHAPE_CIRCLE
import com.micewine.emu.views.VirtualKeyboardInputView.Companion.SHAPE_RECTANGLE
import com.micewine.emu.views.VirtualKeyboardInputView.Companion.SHAPE_SQUARE
import com.micewine.emu.views.VirtualKeyboardInputView.Companion.analogList
import com.micewine.emu.views.VirtualKeyboardInputView.Companion.buttonList
import com.micewine.emu.views.VirtualKeyboardInputView.Companion.detectClick
import com.micewine.emu.views.VirtualKeyboardInputView.Companion.dpadList
import kotlin.math.roundToInt

class VirtualKeyboardInputCreatorView @JvmOverloads constructor (context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0): View(context, attrs, defStyleAttr) {
    private val paint = Paint().apply {
        strokeWidth = 16F
        color = Color.WHITE
        style = Paint.Style.STROKE
    }
    private val textPaint: Paint = Paint().apply {
        color = Color.WHITE
        textAlign = Paint.Align.CENTER
        textSize = 120F
        typeface = context.resources.getFont(R.font.quicksand)
    }

    private val editButton: CircleButton = CircleButton(0F, 0F, 150F)
    private val removeButton: CircleButton = CircleButton(0F, 0F, 150F)

    private var editIcon: Bitmap = getBitmapFromVectorDrawable(context, R.drawable.ic_edit, (editButton.radius / 2).toInt(), (editButton.radius / 2).toInt())
    private var removeIcon: Bitmap = getBitmapFromVectorDrawable(context, R.drawable.ic_delete, (removeButton.radius / 2).toInt(), (removeButton.radius / 2).toInt())

    private fun getBitmapFromVectorDrawable(context: Context, drawableId: Int, width: Int, height: Int): Bitmap {
        val drawable = ContextCompat.getDrawable(context, drawableId) as VectorDrawable
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        return bitmap
    }

    private val gridPaint = Paint().apply {
        color = Color.BLACK
        strokeWidth = 10F
        isAntiAlias = true
        alpha = 200
    }

    private var selectedButton = 0
    private var selectedVAxis = 0
    private var selectedDPad = 0
    private val dpadUp: Path = Path()
    private val dpadDown: Path = Path()
    private val dpadLeft: Path = Path()
    private val dpadRight: Path = Path()

    private fun loadFromPreferences() {
        val mapping = getVirtualControllerPreset(clickedPresetName)

        buttonList.clear()
        analogList.clear()
        dpadList.clear()

        mapping?.buttons?.forEach {
            buttonList.add(it)
        }
        mapping?.analogs?.forEach {
            analogList.add(it)
        }
        mapping?.dpads?.forEach {
            dpadList.add(it)
        }

        reorderButtonsAnalogsIDs()
    }

    fun saveOnPreferences() {
        putVirtualControllerPreset(clickedPresetName, getNativeResolution(context), buttonList, analogList, dpadList)
    }

    init {
        loadFromPreferences()
    }

    private fun drawDPad(path: Path, pressed: Boolean, canvas: Canvas) {
        if (pressed) {
            paint.style = Paint.Style.FILL_AND_STROKE
            textPaint.color = Color.BLACK
        } else {
            paint.style = Paint.Style.STROKE
            textPaint.color = Color.WHITE
        }
        paint.alpha = 200
        textPaint.alpha = 200

        canvas.drawPath(path, paint)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        for (i in 0..width) {
            if (i % GRID_SIZE == 0) {
                canvas.drawLine(i.toFloat(), 0F, i.toFloat(), height.toFloat(), gridPaint)
            }
        }

        for (i in 0..height) {
            if (i % GRID_SIZE == 0) {
                canvas.drawLine(0F, i.toFloat(), width.toFloat(), i.toFloat(), gridPaint)
            }
        }

        buttonList.forEach {
            if (lastSelectedButton == it.id && lastSelectedType == BUTTON) {
                paint.style = Paint.Style.FILL_AND_STROKE
                textPaint.color = Color.BLACK
            } else {
                paint.style = Paint.Style.STROKE
                textPaint.color = Color.WHITE
            }
            paint.color = Color.WHITE
            paint.alpha = 200
            paint.strokeWidth = 16F
            textPaint.alpha = 200

            paint.textSize = it.radius / 4

            when (it.shape) {
                SHAPE_CIRCLE -> {
                    canvas.drawCircle(it.x, it.y, it.radius / 2, paint)
                }
                SHAPE_RECTANGLE -> {
                    canvas.drawRoundRect(
                        it.x - it.radius / 2,
                        it.y - it.radius / 4,
                        it.x + it.radius / 2,
                        it.y + it.radius / 4,
                        32F,
                        32F,
                        paint
                    )
                }
                SHAPE_SQUARE -> {
                    canvas.drawRoundRect(
                        it.x - it.radius / 2,
                        it.y - it.radius / 2,
                        it.x + it.radius / 2,
                        it.y + it.radius / 2,
                        32F,
                        32F,
                        paint
                    )
                }
            }

            textPaint.textSize = it.radius / 4
            val offset = (textPaint.fontMetrics.ascent + textPaint.fontMetrics.descent) / 2
            canvas.drawText(it.keyName, it.x, it.y - offset - 4, textPaint)
        }
        analogList.forEach {
            paint.style = if (lastSelectedButton == it.id && lastSelectedType == ANALOG) Paint.Style.FILL_AND_STROKE else Paint.Style.STROKE
            paint.color = Color.WHITE
            paint.alpha = 200
            canvas.drawCircle(it.x, it.y, it.radius / 2, paint)

            paint.style = Paint.Style.FILL
            paint.color = if (lastSelectedButton == it.id && lastSelectedType == ANALOG) Color.BLACK else Color.WHITE
            paint.alpha = 200
            canvas.drawCircle(it.x, it.y, it.radius / 4, paint)
        }

        if (lastSelectedButton > 0) {
            editButton.x = width / 2F + editButton.radius / 2F
            editButton.y = 20F + editButton.radius / 2F

            removeButton.x = editButton.x - removeButton.radius
            removeButton.y = 20F + removeButton.radius / 2F

            canvas.apply {
                paint.color = Color.BLACK
                paint.alpha = 200
                drawCircle(editButton.x, editButton.y, editButton.radius / 2, paint)
                drawCircle(removeButton.x, removeButton.y, removeButton.radius / 2, paint)
                paint.color = Color.WHITE
                paint.alpha = 200
                drawBitmap(editIcon, editButton.x - editButton.radius / 4, editButton.y - editButton.radius / 4, paint)
                drawBitmap(removeIcon, removeButton.x - removeButton.radius / 4, removeButton.y - removeButton.radius / 4, paint)
            }
        }
        dpadList.forEach {
            canvas.apply {
                dpadLeft.apply {
                    reset()
                    moveTo(it.x - 20, it.y)
                    lineTo(it.x - 20 - it.radius / 4, it.y - it.radius / 4)
                    lineTo(it.x - 20 - it.radius / 4 - it.radius / 2, it.y - it.radius / 4)
                    lineTo(it.x - 20 - it.radius / 4 - it.radius / 2, it.y - it.radius / 4 + it.radius / 2)
                    lineTo(it.x - 20 - it.radius / 4, it.y - it.radius / 4 + it.radius / 2)
                    lineTo(it.x - 20, it.y)
                    close()
                }
                dpadUp.apply {
                    reset()
                    moveTo(it.x, it.y - 20)
                    lineTo(it.x - it.radius / 4, it.y - 20 - it.radius / 4)
                    lineTo(it.x - it.radius / 4, it.y - 20 - it.radius / 4 - it.radius / 2)
                    lineTo(it.x - it.radius / 4 + it.radius / 2, it.y - 20 - it.radius / 4 - it.radius / 2)
                    lineTo(it.x - it.radius / 4 + it.radius / 2, it.y - 20 - it.radius / 4)
                    lineTo(it.x, it.y - 20)
                    close()
                }
                dpadRight.apply {
                    reset()
                    moveTo(it.x + 20, it.y)
                    lineTo(it.x + 20 + it.radius / 4, it.y - it.radius / 4)
                    lineTo(it.x + 20 + it.radius / 4 + it.radius / 2, it.y - it.radius / 4)
                    lineTo(it.x + 20 + it.radius / 4 + it.radius / 2, it.y - it.radius / 4 + it.radius / 2)
                    lineTo(it.x + 20 + it.radius / 4, it.y - it.radius / 4 + it.radius / 2)
                    lineTo(it.x + 20, it.y)
                    close()
                }
                dpadDown.apply {
                    reset()
                    moveTo(it.x, it.y + 20)
                    lineTo(it.x - it.radius / 4, it.y + 20 + it.radius / 4)
                    lineTo(it.x - it.radius / 4, it.y + 20 + it.radius / 4 + it.radius / 2)
                    lineTo(it.x - it.radius / 4 + it.radius / 2, it.y + 20 + it.radius / 4 + it.radius / 2)
                    lineTo(it.x - it.radius / 4 + it.radius / 2, it.y + 20 + it.radius / 4)
                    lineTo(it.x, it.y + 20)
                    close()
                }

                val pressed = (lastSelectedButton == it.id && lastSelectedType == DPAD)

                fun adjustTextSize(
                    text: String,
                    maxWidth: Float,
                    paint: Paint,
                ) {
                    paint.textSize = it.radius / 6F
                    while (paint.measureText(text) > maxWidth - (maxWidth / 5F)) {
                        paint.textSize--
                    }
                }


                drawDPad(dpadUp, pressed, canvas)
                adjustTextSize(it.upKeyName, it.radius / 2F, textPaint)
                drawText(it.upKeyName, it.x, it.y - (textPaint.fontMetrics.ascent + textPaint.fontMetrics.descent) / 2F - it.radius / 2F, textPaint)

                drawDPad(dpadDown, pressed, canvas)
                adjustTextSize(it.downKeyName, it.radius / 2F, textPaint)
                drawText(it.downKeyName, it.x, it.y - (textPaint.fontMetrics.ascent + textPaint.fontMetrics.descent) / 2F + it.radius / 2F, textPaint)

                drawDPad(dpadLeft, pressed, canvas)
                adjustTextSize(it.leftKeyName, it.radius / 2F, textPaint)
                drawText(it.leftKeyName, it.x - it.radius / 2F, it.y - (textPaint.fontMetrics.ascent + textPaint.fontMetrics.descent) / 2F, textPaint)

                drawDPad(dpadRight, pressed, canvas)
                adjustTextSize(it.rightKeyName, it.radius / 2F, textPaint)
                drawText(it.rightKeyName, it.x + it.radius / 2F, it.y - (textPaint.fontMetrics.ascent + textPaint.fontMetrics.descent) / 2F, textPaint)
            }
        }
    }

    fun addButton(buttonData: VirtualKeyboardInputView.VirtualButton) {
        buttonList.add(buttonData)
        invalidate()
    }

    fun addAnalog(buttonData: VirtualKeyboardInputView.VirtualAnalog) {
        analogList.add(buttonData)
        invalidate()
    }

    fun addDPad(buttonData: VirtualKeyboardInputView.VirtualDPad) {
        dpadList.add(buttonData)
        invalidate()
    }

    private fun reorderButtonsAnalogsIDs() {
        buttonList.forEachIndexed { i, button ->
            button.id = i + 1
        }
        analogList.forEachIndexed { i, analog ->
            analog.id = i + 1
        }
        dpadList.forEachIndexed { i, virtualDPad ->
            virtualDPad.id = i + 1
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
         when (event.actionMasked) {
             MotionEvent.ACTION_DOWN, MotionEvent.ACTION_POINTER_DOWN -> {
                 if (!detectClick(event, event.actionIndex, editButton.x, editButton.y, editButton.radius, SHAPE_CIRCLE) && !detectClick(event, event.actionIndex, removeButton.x, removeButton.y, removeButton.radius, SHAPE_CIRCLE)) {
                     lastSelectedButton = 0
                 }

                 buttonList.forEach {
                     if (detectClick(event, event.actionIndex, it.x, it.y, it.radius, it.shape)) {
                         if (selectedButton == 0) {
                             selectedButton = it.id
                             lastSelectedType = BUTTON
                             lastSelectedButton = it.id
                         }
                     }
                 }
                 analogList.forEach {
                     if (detectClick(event, event.actionIndex, it.x, it.y, it.radius, SHAPE_SQUARE)) {
                         if (selectedVAxis == 0) {
                             selectedVAxis = it.id
                             lastSelectedType = ANALOG
                             lastSelectedButton = it.id
                         }
                     }
                 }
                 dpadList.forEach {
                     if (detectClick(event, event.actionIndex, it.x, it.y, it.radius, SHAPE_SQUARE)) {
                         if (selectedDPad == 0) {
                             selectedDPad = it.id
                             lastSelectedType = DPAD
                             lastSelectedButton = it.id
                         }
                     }
                 }

                 invalidate()
             }

             MotionEvent.ACTION_MOVE -> {
                 buttonList.forEach {
                     if (detectClick(event, event.actionIndex, it.x, it.y, it.radius, it.shape)) {
                         if (selectedButton > 0) {
                             buttonList[buttonList.indexOfFirst { i ->
                                 i.id == selectedButton
                             }].apply {
                                 x = (event.getX(event.actionIndex) / GRID_SIZE).roundToInt() * GRID_SIZE.toFloat()
                                 y = (event.getY(event.actionIndex) / GRID_SIZE).roundToInt() * GRID_SIZE.toFloat()
                             }
                         }
                     }
                 }
                 analogList.forEach {
                     if (detectClick(event, event.actionIndex, it.x, it.y, it.radius, SHAPE_CIRCLE)) {
                         if (selectedVAxis > 0) {
                             analogList[analogList.indexOfFirst { i ->
                                 i.id == selectedVAxis
                             }].apply {
                                 x = (event.getX(event.actionIndex) / GRID_SIZE).roundToInt() * GRID_SIZE.toFloat()
                                 y = (event.getY(event.actionIndex) / GRID_SIZE).roundToInt() * GRID_SIZE.toFloat()
                             }
                         }
                     }
                 }
                 dpadList.forEach {
                     if (detectClick(event, event.actionIndex, it.x, it.y, it.radius, SHAPE_SQUARE)) {
                         if (selectedDPad > 0) {
                             dpadList[dpadList.indexOfFirst { i ->
                                 i.id == selectedDPad
                             }].apply {
                                 x = (event.getX(event.actionIndex) / GRID_SIZE).roundToInt() * GRID_SIZE.toFloat()
                                 y = (event.getY(event.actionIndex) / GRID_SIZE).roundToInt() * GRID_SIZE.toFloat()
                             }
                         }
                     }
                 }

                 invalidate()
             }

             MotionEvent.ACTION_POINTER_UP, MotionEvent.ACTION_UP -> {
                 buttonList.forEach {
                     if (detectClick(event, event.actionIndex, it.x, it.y, it.radius, it.shape)) {
                         if (selectedButton == it.id) {
                             selectedButton = 0
                         }
                     }
                 }
                 analogList.forEach {
                     if (detectClick(event, event.actionIndex, it.x, it.y, it.radius, SHAPE_CIRCLE)) {
                         if (selectedVAxis == it.id) {
                             selectedVAxis = 0
                         }
                     }
                 }
                 dpadList.forEach {
                     if (detectClick(event, event.actionIndex, it.x, it.y, it.radius, SHAPE_SQUARE)) {
                         if (selectedDPad == it.id) {
                             selectedDPad = 0
                         }
                     }
                 }

                 if (detectClick(event, event.actionIndex, editButton.x, editButton.y, editButton.radius, SHAPE_CIRCLE) && lastSelectedButton > 0) {
                     if (buttonList.isNotEmpty() && lastSelectedType == BUTTON) {
                         selectedButtonKeyName = buttonList[lastSelectedButton - 1].keyName
                         selectedButtonRadius = buttonList[lastSelectedButton - 1].radius.toInt()
                         selectedButtonShape = buttonList[lastSelectedButton - 1].shape
                     }

                     if (analogList.isNotEmpty() && lastSelectedType == ANALOG) {
                         selectedAnalogUpKeyName = analogList[lastSelectedButton - 1].upKeyName
                         selectedAnalogDownKeyName = analogList[lastSelectedButton - 1].downKeyName
                         selectedAnalogLeftKeyName = analogList[lastSelectedButton - 1].leftKeyName
                         selectedAnalogRightKeyName = analogList[lastSelectedButton - 1].rightKeyName
                         selectedButtonRadius = analogList[lastSelectedButton - 1].radius.toInt()
                     }

                     if (dpadList.isNotEmpty() && lastSelectedType == DPAD) {
                         selectedAnalogUpKeyName = dpadList[lastSelectedButton - 1].upKeyName
                         selectedAnalogDownKeyName = dpadList[lastSelectedButton - 1].downKeyName
                         selectedAnalogLeftKeyName = dpadList[lastSelectedButton - 1].leftKeyName
                         selectedAnalogRightKeyName = dpadList[lastSelectedButton - 1].rightKeyName
                         selectedButtonRadius = dpadList[lastSelectedButton - 1].radius.toInt()
                     }

                     context.sendBroadcast(
                         Intent(ACTION_EDIT_VIRTUAL_BUTTON)
                     )
                 }

                 if (detectClick(event, event.actionIndex, removeButton.x, removeButton.y, removeButton.radius, SHAPE_CIRCLE) && lastSelectedButton > 0) {
                     if (buttonList.isNotEmpty() && lastSelectedType == BUTTON) {
                         buttonList.removeAt(lastSelectedButton - 1)
                     }

                     if (analogList.isNotEmpty() && lastSelectedType == ANALOG) {
                         analogList.removeAt(lastSelectedButton - 1)
                     }

                     if (dpadList.isNotEmpty() && lastSelectedType == DPAD) {
                         dpadList.removeAt(lastSelectedButton - 1)
                     }

                     lastSelectedButton = 0

                     reorderButtonsAnalogsIDs()
                     invalidate()
                 }
             }
         }

        return true
    }

    class CircleButton(
        var x: Float,
        var y: Float,
        var radius: Float,
    )

    companion object {
        const val BUTTON = 0
        const val ANALOG = 1
        const val DPAD = 2
        const val GRID_SIZE = 30

        var lastSelectedButton = 0
        var lastSelectedType = BUTTON
    }
}
