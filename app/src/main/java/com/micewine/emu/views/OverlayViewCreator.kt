package com.micewine.emu.views

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.VectorDrawable
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.core.content.ContextCompat
import com.micewine.emu.R
import com.micewine.emu.activities.VirtualControllerOverlayMapper.Companion.ACTION_EDIT_VIRTUAL_BUTTON
import com.micewine.emu.adapters.AdapterPreset.Companion.clickedPresetName
import com.micewine.emu.fragments.VirtualControllerPresetManagerFragment.Companion.getMapping
import com.micewine.emu.fragments.VirtualControllerPresetManagerFragment.Companion.putMapping
import com.micewine.emu.views.OverlayView.Companion.analogList
import com.micewine.emu.views.OverlayView.Companion.buttonList
import com.micewine.emu.views.OverlayView.Companion.detectClick

class OverlayViewCreator @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val editButton: CircleButton = CircleButton(0F, 0F, 130F)
    private val removeButton: CircleButton = CircleButton(0F, 0F, 130F)

    private var editIcon: Bitmap = getBitmapFromVectorDrawable(
        context, R.drawable.ic_edit,
        (editButton.radius / 2).toInt(), (editButton.radius / 2).toInt()
    )
    private var removeIcon: Bitmap = getBitmapFromVectorDrawable(
        context, R.drawable.ic_delete,
        (removeButton.radius / 2).toInt(), (removeButton.radius / 2).toInt()
    )

    private fun getBitmapFromVectorDrawable(context: Context, drawableId: Int, width: Int, height: Int): Bitmap {
        val drawable = ContextCompat.getDrawable(context, drawableId) as VectorDrawable
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        return bitmap
    }

    private val paint = Paint().apply {
        color = Color.BLACK
        alpha = 200
    }

    private val whitePaint = Paint().apply {
        color = Color.WHITE
    }

    private val buttonPaint: Paint = Paint().apply {
        strokeWidth = 8F
        color = Color.RED
        style = Paint.Style.STROKE
    }

    private val textPaint: Paint = Paint().apply {
        color = Color.RED
        textAlign = Paint.Align.CENTER
        textSize = 50F
    }

    private var selectedButton = 0
    private var selectedVAxis = 0

    private fun loadFromPreferences() {
        val mapping = getMapping(clickedPresetName)

        buttonList.clear()
        analogList.clear()

        mapping?.buttons?.forEach {
            buttonList.add(it)
        }

        mapping?.analogs?.forEach {
            analogList.add(it)
        }

        reorderButtonsAnalogsIDs()
    }

    fun saveOnPreferences() {
        putMapping(clickedPresetName, buttonList, analogList)
    }

    init {
        loadFromPreferences()
    }

    private fun drawText(text: String, x: Float, y: Float, c: Canvas) {
        textPaint.style = Paint.Style.FILL
        textPaint.color = Color.WHITE
        c.drawText(text, x, y, textPaint)
    }

    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // Desenha os botões virtuais de acordo com seu formato
        buttonList.forEach { button ->
            buttonPaint.color = if (lastSelectedButton == button.id && lastSelectedType == BUTTON) Color.GRAY else Color.WHITE
            textPaint.color = buttonPaint.color
            buttonPaint.alpha = 220

            when (button.shape) {
                "Quadrado" -> {
                    val halfSide = button.radius / 2
                    canvas.drawRect(
                        button.x - halfSide,
                        button.y - halfSide,
                        button.x + halfSide,
                        button.y + halfSide,
                        buttonPaint
                    )
                }
                "Retangular" -> {
                    val halfWidth = button.radius / 2
                    val halfHeight = button.radius / 4
                    canvas.drawRect(
                        button.x - halfWidth,
                        button.y - halfHeight,
                        button.x + halfWidth,
                        button.y + halfHeight,
                        buttonPaint
                    )
                }
                else -> { // Padrão: Circular
                    canvas.drawCircle(button.x, button.y, button.radius / 2, buttonPaint)
                }
            }
            // Desenha o texto centralizado
            canvas.drawText(button.keyName, button.x, button.y + (button.radius / 8), textPaint)
        }

        // Desenha os analógicos (sempre circulares)
        analogList.forEach {
            buttonPaint.color = if (lastSelectedButton == it.id && lastSelectedType == ANALOG) Color.GRAY else Color.WHITE
            whitePaint.color = buttonPaint.color
            whitePaint.alpha = 220
            buttonPaint.alpha = 220
            canvas.drawCircle(it.x, it.y, it.radius / 2, buttonPaint)
            canvas.drawCircle(it.x, it.y, it.radius / 4, whitePaint)
        }

        // Se houver um botão selecionado, desenha os ícones de editar e remover
        if (lastSelectedButton > 0) {
            // Calcula o centro horizontal da tela e define a margem superior
            val centerX = width / 2F
            val topMargin = 20F + editButton.radius / 2
            val spacing = 20F

            // Posiciona o botão de editar à direita do centro
            editButton.x = centerX + spacing / 2 + editButton.radius / 2
            editButton.y = topMargin

            // Posiciona o botão de remover à esquerda do centro
            removeButton.x = centerX - spacing / 2 - removeButton.radius / 2
            removeButton.y = topMargin

            canvas.drawCircle(editButton.x, editButton.y, editButton.radius / 2, paint)
            canvas.drawCircle(removeButton.x, removeButton.y, removeButton.radius / 2, paint)
            canvas.drawBitmap(editIcon, editButton.x - editButton.radius / 4, editButton.y - editButton.radius / 4, whitePaint)
            canvas.drawBitmap(removeIcon, removeButton.x - removeButton.radius / 4, removeButton.y - removeButton.radius / 4, whitePaint)
        }
    }

    fun addButton(buttonData: OverlayView.VirtualButton) {
        buttonList.add(buttonData)
        invalidate()
    }

    fun addAnalog(buttonData: OverlayView.VirtualAnalog) {
        analogList.add(buttonData)
        invalidate()
    }

    private fun reorderButtonsAnalogsIDs() {
        buttonList.forEachIndexed { i, button ->
            button.id = i + 1
        }
        analogList.forEachIndexed { i, analog ->
            analog.id = i + 1
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_POINTER_DOWN -> {
                if (!detectClick(event, event.actionIndex, editButton.x, editButton.y, editButton.radius) &&
                    !detectClick(event, event.actionIndex, removeButton.x, removeButton.y, removeButton.radius)
                ) {
                    lastSelectedButton = 0
                }
                buttonList.forEach {
                    if (detectClick(event, event.actionIndex, it.x, it.y, it.radius)) {
                        if (selectedButton == 0) {
                            selectedButton = it.id
                            lastSelectedType = BUTTON
                            lastSelectedButton = it.id
                        }
                    }
                }
                analogList.forEach {
                    if (detectClick(event, event.actionIndex, it.x, it.y, it.radius)) {
                        if (selectedVAxis == 0) {
                            selectedVAxis = it.id
                            lastSelectedType = ANALOG
                            lastSelectedButton = it.id
                        }
                    }
                }
                invalidate()
            }
            MotionEvent.ACTION_MOVE -> {
                buttonList.forEach {
                    if (detectClick(event, event.actionIndex, it.x, it.y, it.radius)) {
                        if (selectedButton > 0) {
                            buttonList[buttonList.indexOfFirst { i -> i.id == selectedButton }].apply {
                                x = event.getX(event.actionIndex)
                                y = event.getY(event.actionIndex)
                            }
                        }
                    }
                }
                analogList.forEach {
                    if (detectClick(event, event.actionIndex, it.x, it.y, it.radius)) {
                        if (selectedVAxis > 0) {
                            analogList[analogList.indexOfFirst { i -> i.id == selectedVAxis }].apply {
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
                    if (detectClick(event, event.actionIndex, it.x, it.y, it.radius)) {
                        if (selectedButton == it.id) {
                            selectedButton = 0
                        }
                    }
                }
                analogList.forEach {
                    if (detectClick(event, event.actionIndex, it.x, it.y, it.radius)) {
                        if (selectedVAxis == it.id) {
                            selectedVAxis = 0
                        }
                    }
                }
                if (detectClick(event, event.actionIndex, editButton.x, editButton.y, editButton.radius) && lastSelectedButton > 0) {
                    val intent = Intent(ACTION_EDIT_VIRTUAL_BUTTON)
                    if (buttonList.isNotEmpty() && lastSelectedType == BUTTON) {
                        val buttonItem = buttonList[lastSelectedButton - 1]
                        intent.putExtra("buttonKey", buttonItem.keyName)
                        intent.putExtra("buttonRadius", buttonItem.radius.toInt())
                        intent.putExtra("buttonShape", buttonItem.shape)
                        intent.putExtra("analogUpKey", "")
                        intent.putExtra("analogDownKey", "")
                        intent.putExtra("analogLeftKey", "")
                        intent.putExtra("analogRightKey", "")
                    } else if (analogList.isNotEmpty() && lastSelectedType == ANALOG) {
                        val analogItem = analogList[lastSelectedButton - 1]
                        intent.putExtra("analogUpKey", analogItem.upKeyName)
                        intent.putExtra("analogDownKey", analogItem.downKeyName)
                        intent.putExtra("analogLeftKey", analogItem.leftKeyName)
                        intent.putExtra("analogRightKey", analogItem.rightKeyName)
                        intent.putExtra("buttonRadius", analogItem.radius.toInt())
                        intent.putExtra("buttonKey", "")
                        intent.putExtra("buttonShape", "Circular")
                    }
                    context.sendBroadcast(intent)
                }
                if (detectClick(event, event.actionIndex, removeButton.x, removeButton.y, removeButton.radius) && lastSelectedButton > 0) {
                    if (buttonList.isNotEmpty() && lastSelectedType == BUTTON) {
                        buttonList.removeAt(lastSelectedButton - 1)
                    }
                    if (analogList.isNotEmpty() && lastSelectedType == ANALOG) {
                        analogList.removeAt(lastSelectedButton - 1)
                    }
                    lastSelectedButton = 0
                    reorderButtonsAnalogsIDs()
                    invalidate()
                }
            }
        }
        return true
    }

    class CircleButton(var x: Float, var y: Float, var radius: Float)

    companion object {
        const val BUTTON = 0
        const val ANALOG = 1

        var lastSelectedButton = 0
        var lastSelectedType = BUTTON
    }
}
s