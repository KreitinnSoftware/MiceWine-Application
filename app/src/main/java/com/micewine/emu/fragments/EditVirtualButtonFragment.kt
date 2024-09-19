package com.micewine.emu.fragments

import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Spinner
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
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val inflater = requireActivity().layoutInflater
        val view = inflater.inflate(R.layout.fragment_edit_virtual_button, null)
        val saveButton = view.findViewById<Button>(R.id.saveButton)
        val buttonSpinner = view.findViewById<Spinner>(R.id.buttonSpinner).apply {
            adapter = ArrayAdapter(context, android.R.layout.simple_spinner_dropdown_item, getKeyNames())
            setSelection(getKeyNames().indexOf(selectedButtonKeyName))
        }
        val analogUpKeySpinner = view.findViewById<Spinner>(R.id.analogUpKeySpinner).apply {
            adapter = ArrayAdapter(context, android.R.layout.simple_spinner_dropdown_item, getKeyNames())
            setSelection(getKeyNames().indexOf(selectedAnalogUpKeyName))
        }
        val analogDownKeySpinner = view.findViewById<Spinner>(R.id.analogDownKeySpinner).apply {
            adapter = ArrayAdapter(context, android.R.layout.simple_spinner_dropdown_item, getKeyNames())
            setSelection(getKeyNames().indexOf(selectedAnalogDownKeyName))
        }
        val analogLeftKeySpinner = view.findViewById<Spinner>(R.id.analogLeftKeySpinner).apply {
            adapter = ArrayAdapter(context, android.R.layout.simple_spinner_dropdown_item, getKeyNames())
            setSelection(getKeyNames().indexOf(selectedAnalogLeftKeyName))
        }
        val analogRightKeySpinner = view.findViewById<Spinner>(R.id.analogRightKeySpinner).apply {
            adapter = ArrayAdapter(context, android.R.layout.simple_spinner_dropdown_item, getKeyNames())
            setSelection(getKeyNames().indexOf(selectedAnalogRightKeyName))
        }

        if (lastSelectedType == ANALOG) {
            buttonSpinner.visibility = View.GONE
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
                buttonList[lastSelectedButton - 1].keyName = buttonSpinner.selectedItem.toString()
                buttonList[lastSelectedButton - 1].keyCodes = getXKeyScanCodes(buttonSpinner.selectedItem.toString())
            } else if (lastSelectedType == ANALOG && analogList.isNotEmpty()) {
                analogList[lastSelectedButton - 1].upKeyName = analogUpKeySpinner.selectedItem.toString()
                analogList[lastSelectedButton - 1].upKeyCodes = getXKeyScanCodes(analogUpKeySpinner.selectedItem.toString())

                analogList[lastSelectedButton - 1].downKeyName = analogDownKeySpinner.selectedItem.toString()
                analogList[lastSelectedButton - 1].downKeyCodes = getXKeyScanCodes(analogDownKeySpinner.selectedItem.toString())

                analogList[lastSelectedButton - 1].leftKeyName = analogLeftKeySpinner.selectedItem.toString()
                analogList[lastSelectedButton - 1].leftKeyCodes = getXKeyScanCodes(analogLeftKeySpinner.selectedItem.toString())

                analogList[lastSelectedButton - 1].rightKeyName = analogRightKeySpinner.selectedItem.toString()
                analogList[lastSelectedButton - 1].rightKeyCodes = getXKeyScanCodes(analogRightKeySpinner.selectedItem.toString())
            }

            context?.sendBroadcast(
                Intent(ACTION_INVALIDATE)
            )

            dismiss()
        }

        return dialog
    }

    companion object {
        var selectedButtonKeyName = ""
        var selectedAnalogUpKeyName = ""
        var selectedAnalogDownKeyName = ""
        var selectedAnalogLeftKeyName = ""
        var selectedAnalogRightKeyName = ""
    }
}