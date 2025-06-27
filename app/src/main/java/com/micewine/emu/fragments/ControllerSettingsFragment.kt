package com.micewine.emu.fragments

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.google.android.material.button.MaterialButton
import com.google.android.material.materialswitch.MaterialSwitch
import com.micewine.emu.R
import com.micewine.emu.adapters.AdapterGame.Companion.selectedGameName
import com.micewine.emu.controller.ControllerUtils.connectedPhysicalControllers
import com.micewine.emu.controller.ControllerUtils.prepareControllersMappings
import com.micewine.emu.fragments.ControllerPresetManagerFragment.Companion.getControllerPresets
import com.micewine.emu.fragments.ShortcutsFragment.Companion.getControllerPreset
import com.micewine.emu.fragments.ShortcutsFragment.Companion.getControllerXInput
import com.micewine.emu.fragments.ShortcutsFragment.Companion.getControllerXInputSwapAnalogs
import com.micewine.emu.fragments.ShortcutsFragment.Companion.putControllerPreset
import com.micewine.emu.fragments.ShortcutsFragment.Companion.putControllerXInput
import com.micewine.emu.fragments.ShortcutsFragment.Companion.putControllerXInputSwapAnalogs

class ControllerSettingsFragment : DialogFragment() {
    private lateinit var controllersMappingTypeTexts: List<TextView>
    private lateinit var controllersMappingTypeSpinners: List<Spinner>
    private lateinit var controllersSwapAnalogsTexts: List<TextView>
    private lateinit var controllersSwapAnalogsSwitches: List<MaterialSwitch>
    private lateinit var controllersKeyboardPresetSpinners: List<Spinner>
    private lateinit var controllersKeyboardPresetTexts: List<TextView>
    private lateinit var controllersNamesTexts: List<TextView>
    private lateinit var buttonCancel: MaterialButton
    private lateinit var buttonConfirm: MaterialButton

    private val mappingTypes = listOf("MiceWine Controller", "Keyboard/Mouse")
    private val controllerProfilesNames: List<String> = getControllerPresets().map { it.name }
    private val receiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                ACTION_UPDATE_CONTROLLERS_STATUS -> {
                    requireActivity().runOnUiThread {
                        updateControllersStatus()
                    }
                }
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun updateControllersStatus() {
        controllersNamesTexts.forEach { it.visibility = View.VISIBLE }
        controllersMappingTypeTexts.forEach { it.visibility = View.VISIBLE }
        controllersMappingTypeSpinners.forEach { it.visibility = View.VISIBLE }
        controllersKeyboardPresetSpinners.forEach { it.visibility = View.VISIBLE }
        controllersKeyboardPresetTexts.forEach { it.visibility = View.VISIBLE }
        controllersSwapAnalogsTexts.forEach { it.visibility = View.VISIBLE }
        controllersSwapAnalogsSwitches.forEach { it.visibility = View.VISIBLE }

        for (i in connectedPhysicalControllers.size..3) {
            controllersNamesTexts[i].visibility = View.GONE
            controllersMappingTypeTexts[i].visibility = View.GONE
            controllersMappingTypeSpinners[i].visibility = View.GONE
            controllersKeyboardPresetSpinners[i].visibility = View.GONE
            controllersKeyboardPresetTexts[i].visibility = View.GONE
            controllersSwapAnalogsTexts[i].visibility = View.GONE
            controllersSwapAnalogsSwitches[i].visibility = View.GONE
        }

        if (connectedPhysicalControllers.size == 0) {
            controllersMappingTypeTexts[0].visibility = View.VISIBLE
            controllersMappingTypeTexts[0].text = requireContext().getString(R.string.no_controllers_connected)
        }

        controllersNamesTexts.forEachIndexed { index, it ->
            if (index < connectedPhysicalControllers.size) {
                it.text = "$index: ${connectedPhysicalControllers[index].name}"
            }
        }

        controllersMappingTypeSpinners.forEachIndexed { index, it ->
            it.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, mappingTypes)
            it.setSelection(
                if (getControllerXInput(selectedGameName, index)) 0 else 1
            )
            it.onItemSelectedListener = object : OnItemSelectedListener {
                override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                    val isXInput = p2 == 0
                    controllersKeyboardPresetSpinners[index].visibility = if (isXInput) View.GONE else View.VISIBLE
                    controllersKeyboardPresetTexts[index].visibility = if (isXInput) View.GONE else View.VISIBLE

                    controllersSwapAnalogsSwitches[index].visibility = if (isXInput) View.VISIBLE else View.GONE
                    controllersSwapAnalogsTexts[index].visibility = if (isXInput) View.VISIBLE else View.GONE

                    putControllerXInput(selectedGameName, isXInput, index)
                }

                override fun onNothingSelected(p0: AdapterView<*>?) {}
            }
        }
        controllersSwapAnalogsSwitches.forEachIndexed { index, sw ->
            sw.isChecked = getControllerXInputSwapAnalogs(selectedGameName, index)
            sw.setOnClickListener {
                putControllerXInputSwapAnalogs(selectedGameName, sw.isChecked, index)
            }
        }
        controllersKeyboardPresetSpinners.forEachIndexed { index, it ->
            it.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, controllerProfilesNames)
            it.setSelection(controllerProfilesNames.indexOf(getControllerPreset(selectedGameName, index)))
            it.onItemSelectedListener = object : OnItemSelectedListener {
                override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                    putControllerPreset(selectedGameName, controllerProfilesNames[p2], index)
                }

                override fun onNothingSelected(p0: AdapterView<*>?) {
                }
            }
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val view = requireActivity().layoutInflater.inflate(R.layout.fragment_controller_settings, null)

        buttonConfirm = view.findViewById(R.id.buttonContinue)
        buttonCancel = view.findViewById(R.id.buttonCancel)

        controllersMappingTypeTexts = listOf(
            view.findViewById(R.id.controller0MappingTypeText),
            view.findViewById(R.id.controller1MappingTypeText),
            view.findViewById(R.id.controller2MappingTypeText),
            view.findViewById(R.id.controller3MappingTypeText)
        )
        controllersMappingTypeSpinners = listOf(
            view.findViewById(R.id.controller0MappingTypeSpinner),
            view.findViewById(R.id.controller1MappingTypeSpinner),
            view.findViewById(R.id.controller2MappingTypeSpinner),
            view.findViewById(R.id.controller3MappingTypeSpinner)
        )
        controllersSwapAnalogsTexts = listOf(
            view.findViewById(R.id.controller0SwapAnalogsText),
            view.findViewById(R.id.controller1SwapAnalogsText),
            view.findViewById(R.id.controller2SwapAnalogsText),
            view.findViewById(R.id.controller3SwapAnalogsText)
        )
        controllersSwapAnalogsSwitches = listOf(
            view.findViewById(R.id.controller0SwapAnalogsSwitch),
            view.findViewById(R.id.controller1SwapAnalogsSwitch),
            view.findViewById(R.id.controller2SwapAnalogsSwitch),
            view.findViewById(R.id.controller3SwapAnalogsSwitch)
        )
        controllersKeyboardPresetSpinners = listOf(
            view.findViewById(R.id.controller0KeyboardPresetSpinner),
            view.findViewById(R.id.controller1KeyboardPresetSpinner),
            view.findViewById(R.id.controller2KeyboardPresetSpinner),
            view.findViewById(R.id.controller3KeyboardPresetSpinner)
        )
        controllersKeyboardPresetTexts = listOf(
            view.findViewById(R.id.controller0KeyboardPresetText),
            view.findViewById(R.id.controller1KeyboardPresetText),
            view.findViewById(R.id.controller2KeyboardPresetText),
            view.findViewById(R.id.controller3KeyboardPresetText)
        )
        controllersNamesTexts = listOf(
            view.findViewById(R.id.controller0Name),
            view.findViewById(R.id.controller1Name),
            view.findViewById(R.id.controller2Name),
            view.findViewById(R.id.controller3Name)
        )

        updateControllersStatus()

        buttonCancel.setOnClickListener {
            dismiss()
        }
        buttonConfirm.setOnClickListener {
            dismiss()
        }

        requireActivity().registerReceiver(receiver, object : IntentFilter() {
            init {
                addAction(ACTION_UPDATE_CONTROLLERS_STATUS)
            }
        })

        return AlertDialog.Builder(requireActivity(), R.style.CustomAlertDialog).setView(view).create()
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        requireActivity().unregisterReceiver(receiver)
        prepareControllersMappings()
    }

    companion object {
        const val ACTION_UPDATE_CONTROLLERS_STATUS = "com.micewine.emu.ACTION_UPDATE_CONTROLLERS_STATUS"
    }
}