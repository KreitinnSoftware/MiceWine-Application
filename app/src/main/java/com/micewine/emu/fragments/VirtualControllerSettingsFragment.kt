package com.micewine.emu.fragments

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.google.android.material.button.MaterialButton
import com.micewine.emu.R
import com.micewine.emu.adapters.AdapterGame.Companion.selectedGameName
import com.micewine.emu.controller.ControllerUtils.prepareControllersMappings
import com.micewine.emu.fragments.ShortcutsFragment.Companion.getSelectedVirtualControllerPreset
import com.micewine.emu.fragments.ShortcutsFragment.Companion.getVirtualControllerXInput
import com.micewine.emu.fragments.ShortcutsFragment.Companion.putSelectedVirtualControllerPreset
import com.micewine.emu.fragments.ShortcutsFragment.Companion.putVirtualControllerXInput
import com.micewine.emu.fragments.VirtualControllerPresetManagerFragment.Companion.getVirtualControllerPresets

class VirtualControllerSettingsFragment : DialogFragment() {
    private lateinit var controllerMappingTypeSpinner: Spinner
    private lateinit var controllerKeyboardPresetSpinner: Spinner
    private lateinit var controllerKeyboardPresetText: TextView
    private lateinit var buttonCancel: MaterialButton
    private lateinit var buttonConfirm: MaterialButton

    private val mappingTypes = listOf("MiceWine Controller", "Keyboard/Mouse")
    private val virtualControllerProfilesNames: List<String> = getVirtualControllerPresets().map { it.name }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val view = requireActivity().layoutInflater.inflate(R.layout.fragment_virtual_controller_settings, null)

        buttonConfirm = view.findViewById(R.id.buttonContinue)
        buttonCancel = view.findViewById(R.id.buttonCancel)

        controllerKeyboardPresetText = view.findViewById(R.id.controllerKeyboardPresetText)
        controllerMappingTypeSpinner = view.findViewById(R.id.controllerMappingTypeSpinner)
        controllerMappingTypeSpinner.let {
            it.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, mappingTypes)
            it.setSelection(
                if (getVirtualControllerXInput(selectedGameName)) 0 else 1
            )
            it.onItemSelectedListener = object : OnItemSelectedListener {
                override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                    val isXInput = p2 == 0
                    controllerKeyboardPresetSpinner.visibility = if (isXInput) View.GONE else View.VISIBLE
                    controllerKeyboardPresetText.visibility = if (isXInput) View.GONE else View.VISIBLE

                    putVirtualControllerXInput(selectedGameName, isXInput)
                }

                override fun onNothingSelected(p0: AdapterView<*>?) {}
            }
        }

        controllerKeyboardPresetSpinner = view.findViewById(R.id.controllerKeyboardPresetSpinner)
        controllerKeyboardPresetSpinner.let {
            it.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, virtualControllerProfilesNames)
            it.setSelection(virtualControllerProfilesNames.indexOf(getSelectedVirtualControllerPreset(selectedGameName)))
            it.onItemSelectedListener = object : OnItemSelectedListener {
                override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                    putSelectedVirtualControllerPreset(selectedGameName, virtualControllerProfilesNames[p2])
                }

                override fun onNothingSelected(p0: AdapterView<*>?) {
                }
            }
        }

        buttonCancel.setOnClickListener {
            dismiss()
        }
        buttonConfirm.setOnClickListener {
            dismiss()
        }

        return AlertDialog.Builder(requireActivity(), R.style.CustomAlertDialog).setView(view).create()
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        parentFragmentManager.setFragmentResult("invalidateControllerType", Bundle())
        prepareControllersMappings()
    }
}