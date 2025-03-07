package com.micewine.emu.fragments

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.Spinner
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.micewine.emu.R
import com.micewine.emu.activities.VirtualControllerOverlayMapper.Companion.ACTION_INVALIDATE
import com.micewine.emu.controller.XKeyCodes.getKeyNames
import com.micewine.emu.controller.XKeyCodes.getXKeyScanCodes
import com.micewine.emu.views.OverlayView.Companion.analogList
import com.micewine.emu.views.OverlayView.Companion.buttonList
import com.micewine.emu.views.OverlayViewCreator.Companion.ANALOG
import com.micewine.emu.views.OverlayViewCreator.Companion.BUTTON
import com.micewine.emu.views.OverlayViewCreator.Companion.lastSelectedButton
import com.micewine.emu.views.OverlayViewCreator.Companion.lastSelectedType

class EditVirtualButtonFragment : DialogFragment() {

    private var selectedButtonKeyName: String = ""
    private var selectedAnalogUpKeyName: String = ""
    private var selectedAnalogDownKeyName: String = ""
    private var selectedAnalogLeftKeyName: String = ""
    private var selectedAnalogRightKeyName: String = ""
    private var selectedButtonRadius: Int = 180
    private var selectedButtonShape: String = "Circular" // Novo parâmetro para o formato

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            selectedButtonKeyName = it.getString(ARG_BUTTON_KEY, "")
            selectedAnalogUpKeyName = it.getString(ARG_ANALOG_UP_KEY, "")
            selectedAnalogDownKeyName = it.getString(ARG_ANALOG_DOWN_KEY, "")
            selectedAnalogLeftKeyName = it.getString(ARG_ANALOG_LEFT_KEY, "")
            selectedAnalogRightKeyName = it.getString(ARG_ANALOG_RIGHT_KEY, "")
            selectedButtonRadius = it.getInt(ARG_BUTTON_RADIUS, 100)
            selectedButtonShape = it.getString(ARG_BUTTON_SHAPE, "Circular")
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val inflater = requireActivity().layoutInflater
        val view = inflater.inflate(R.layout.fragment_edit_virtual_button, null)

        val saveButton = view.findViewById<Button>(R.id.saveButton)
        val cancelButton = view.findViewById<Button>(R.id.cancelButton)
        val radiusSeekbarValue = view.findViewById<TextView>(R.id.radiusSeekbarValue).apply {
            text = "$selectedButtonRadius%"
        }
        val radiusSeekbar = view.findViewById<SeekBar>(R.id.radiusSeekbar).apply {
            max = 400
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                min = 180
            }
            progress = selectedButtonRadius
            setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                @SuppressLint("SetTextI18n")
                override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                    radiusSeekbarValue.text = "$progress%"
                }
                override fun onStartTrackingTouch(seekBar: SeekBar?) {}
                override fun onStopTrackingTouch(seekBar: SeekBar?) {}
            })
        }

        val allKeyNames = getKeyNames(true)

        val buttonSpinner = view.findViewById<Spinner>(R.id.buttonSpinner).apply {
            adapter = ArrayAdapter(context, android.R.layout.simple_spinner_dropdown_item, allKeyNames)
            setSelection(allKeyNames.indexOf(selectedButtonKeyName))
        }
        val analogUpKeySpinner = view.findViewById<Spinner>(R.id.analogUpKeySpinner).apply {
            adapter = ArrayAdapter(context, android.R.layout.simple_spinner_dropdown_item, allKeyNames)
            setSelection(allKeyNames.indexOf(selectedAnalogUpKeyName))
        }
        val analogDownKeySpinner = view.findViewById<Spinner>(R.id.analogDownKeySpinner).apply {
            adapter = ArrayAdapter(context, android.R.layout.simple_spinner_dropdown_item, allKeyNames)
            setSelection(allKeyNames.indexOf(selectedAnalogDownKeyName))
        }
        val analogLeftKeySpinner = view.findViewById<Spinner>(R.id.analogLeftKeySpinner).apply {
            adapter = ArrayAdapter(context, android.R.layout.simple_spinner_dropdown_item, allKeyNames)
            setSelection(allKeyNames.indexOf(selectedAnalogLeftKeyName))
        }
        val analogRightKeySpinner = view.findViewById<Spinner>(R.id.analogRightKeySpinner).apply {
            adapter = ArrayAdapter(context, android.R.layout.simple_spinner_dropdown_item, allKeyNames)
            setSelection(allKeyNames.indexOf(selectedAnalogRightKeyName))
        }

        // Nova spinner para seleção do formato (disponível apenas para BOTÃO)
        val shapeSpinner = view.findViewById<Spinner>(R.id.shapeSpinner).apply {
            adapter = ArrayAdapter(
                context,
                android.R.layout.simple_spinner_dropdown_item,
                arrayOf("Circular", "Quadrado", "Retangular")
            )
            setSelection(when (selectedButtonShape) {
                "Quadrado" -> 1
                "Retangular" -> 2
                else -> 0
            })
        }

        // Ajusta a visibilidade conforme o tipo de controle
        if (lastSelectedType == ANALOG) {
            buttonSpinner.visibility = View.GONE
            shapeSpinner.visibility = View.GONE
        } else if (lastSelectedType == BUTTON) {
            view.findViewById<LinearLayout>(R.id.layoutAnalogUp).visibility = View.GONE
            view.findViewById<LinearLayout>(R.id.layoutAnalogDown).visibility = View.GONE
            view.findViewById<LinearLayout>(R.id.layoutAnalogLeft).visibility = View.GONE
            view.findViewById<LinearLayout>(R.id.layoutAnalogRight).visibility = View.GONE
        }

        val dialog = AlertDialog.Builder(requireContext(), R.style.CustomAlertDialog)
            .setView(view)
            .create()

        saveButton.setOnClickListener {
            if (lastSelectedType == BUTTON && buttonList.isNotEmpty()) {
                if (lastSelectedButton > 0 && lastSelectedButton <= buttonList.size) {
                    val buttonItem = buttonList[lastSelectedButton - 1]
                    buttonItem.keyName = buttonSpinner.selectedItem.toString()
                    buttonItem.keyCodes = getXKeyScanCodes(buttonSpinner.selectedItem.toString())
                    buttonItem.radius = radiusSeekbar.progress.toFloat()
                    // Atualiza o formato do botão conforme seleção
                    buttonItem.shape = shapeSpinner.selectedItem.toString()
                }
            } else if (lastSelectedType == ANALOG && analogList.isNotEmpty()) {
                if (lastSelectedButton > 0 && lastSelectedButton <= analogList.size) {
                    val analogItem = analogList[lastSelectedButton - 1]
                    analogItem.upKeyName = analogUpKeySpinner.selectedItem.toString()
                    analogItem.upKeyCodes = getXKeyScanCodes(analogUpKeySpinner.selectedItem.toString())
                    analogItem.downKeyName = analogDownKeySpinner.selectedItem.toString()
                    analogItem.downKeyCodes = getXKeyScanCodes(analogDownKeySpinner.selectedItem.toString())
                    analogItem.leftKeyName = analogLeftKeySpinner.selectedItem.toString()
                    analogItem.leftKeyCodes = getXKeyScanCodes(analogLeftKeySpinner.selectedItem.toString())
                    analogItem.rightKeyName = analogRightKeySpinner.selectedItem.toString()
                    analogItem.rightKeyCodes = getXKeyScanCodes(analogRightKeySpinner.selectedItem.toString())
                    analogItem.radius = radiusSeekbar.progress.toFloat()
                }
            }

            context?.sendBroadcast(Intent(ACTION_INVALIDATE))
            dismiss()
        }

        cancelButton.setOnClickListener {
            dialog.cancel()
            dismiss()
        }

        return dialog
    }

    companion object {
        private const val ARG_BUTTON_KEY = "buttonKey"
        private const val ARG_ANALOG_UP_KEY = "analogUpKey"
        private const val ARG_ANALOG_DOWN_KEY = "analogDownKey"
        private const val ARG_ANALOG_LEFT_KEY = "analogLeftKey"
        private const val ARG_ANALOG_RIGHT_KEY = "analogRightKey"
        private const val ARG_BUTTON_RADIUS = "buttonRadius"
        private const val ARG_BUTTON_SHAPE = "buttonShape" // Novo argumento para o formato

        fun newInstance(
            buttonKey: String,
            analogUpKey: String,
            analogDownKey: String,
            analogLeftKey: String,
            analogRightKey: String,
            buttonRadius: Int,
            buttonShape: String // Novo parâmetro
        ): EditVirtualButtonFragment {
            return EditVirtualButtonFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_BUTTON_KEY, buttonKey)
                    putString(ARG_ANALOG_UP_KEY, analogUpKey)
                    putString(ARG_ANALOG_DOWN_KEY, analogDownKey)
                    putString(ARG_ANALOG_LEFT_KEY, analogLeftKey)
                    putString(ARG_ANALOG_RIGHT_KEY, analogRightKey)
                    putInt(ARG_BUTTON_RADIUS, buttonRadius)
                    putString(ARG_BUTTON_SHAPE, buttonShape)
                }
            }
        }
    }
}
