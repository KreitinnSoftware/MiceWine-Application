package com.micewine.emu.fragments

import android.app.AlertDialog
import android.app.Dialog
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import com.micewine.emu.R
import com.micewine.emu.activities.MainActivity.Companion.setSharedVars
import com.micewine.emu.activities.MainActivity.Companion.setupDone
import com.micewine.emu.core.RatPackageManager.listRatPackages
import com.micewine.emu.core.RatPackageManager.listRatPackagesId
import com.micewine.emu.fragments.Box64PresetManagerFragment.Companion.addBox64Preset
import com.micewine.emu.fragments.ControllerPresetManagerFragment.Companion.addControllerPreset
import com.micewine.emu.fragments.SetupFragment.Companion.dialogTitleText
import com.micewine.emu.fragments.SetupFragment.Companion.progressBarIsIndeterminate
import com.micewine.emu.fragments.VirtualControllerPresetManagerFragment.Companion.addVirtualControllerPreset
import com.micewine.emu.fragments.WinePrefixManagerFragment.Companion.createWinePrefix
import com.micewine.emu.fragments.WinePrefixManagerFragment.Companion.putSelectedWinePrefix
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CreatePresetFragment(private val presetType: Int) : DialogFragment() {
    var preferences: SharedPreferences? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val inflater = requireActivity().layoutInflater
        val view = inflater.inflate(R.layout.fragment_create_preset, null)

        val editTextNewName = view.findViewById<EditText>(R.id.editTextNewName)
        val buttonContinue = view.findViewById<Button>(R.id.buttonContinue)
        val buttonCancel = view.findViewById<Button>(R.id.buttonCancel)
        val wineVersionText = view.findViewById<TextView>(R.id.wineVersionText)
        val wineVersionSpinner = view.findViewById<Spinner>(R.id.wineVersionSpinner)
        val wineVersions: MutableList<String> = mutableListOf()
        val wineVersionsId: MutableList<String> = mutableListOf()

        val dialog = AlertDialog.Builder(requireContext(), R.style.CustomAlertDialog).setView(view).create()

        when (presetType) {
            WINEPREFIX_PRESET -> {
                wineVersionText.visibility = View.VISIBLE
                wineVersionSpinner.visibility = View.VISIBLE

                listRatPackages("Wine-").forEach {
                    wineVersions.add(it.name!!)
                }
                listRatPackagesId("Wine-").forEach {
                    wineVersionsId.add(it)
                }

                wineVersionSpinner.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, wineVersions)
            }
            CONTROLLER_PRESET, VIRTUAL_CONTROLLER_PRESET, BOX64_PRESET -> {
                wineVersionText.visibility = View.GONE
                wineVersionSpinner.visibility = View.GONE
            }
        }

        buttonContinue.setOnClickListener {
            val newName = editTextNewName.text.toString()
            if (newName == "") {
                dismiss()
            }

            when (presetType) {
                WINEPREFIX_PRESET -> {
                    putSelectedWinePrefix(newName)
                    setSharedVars(requireActivity())

                    lifecycleScope.launch {
                        setupDone = false

                        SetupFragment().show(requireActivity().supportFragmentManager, "")

                        dialogTitleText = getString(R.string.creating_wine_prefix)
                        progressBarIsIndeterminate = true

                        withContext(Dispatchers.IO) {
                            createWinePrefix(newName, wineVersionsId[wineVersionSpinner.selectedItemId.toInt()])

                            setupDone = true
                        }
                    }
                }
                CONTROLLER_PRESET -> {
                    addControllerPreset(requireContext(), newName)
                }
                VIRTUAL_CONTROLLER_PRESET -> {
                    addVirtualControllerPreset(requireContext(), newName)
                }
                BOX64_PRESET -> {
                    addBox64Preset(requireContext(), newName)
                }
            }

            dismiss()
        }

        buttonCancel.setOnClickListener {
            dismiss()
        }

        return dialog
    }

    companion object {
        const val WINEPREFIX_PRESET = 1
        const val CONTROLLER_PRESET = 2
        const val VIRTUAL_CONTROLLER_PRESET = 3
        const val BOX64_PRESET = 4
    }
}