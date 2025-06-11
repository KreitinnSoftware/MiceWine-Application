package com.micewine.emu.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.edit
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.micewine.emu.R
import com.micewine.emu.activities.MainActivity.Companion.preferences
import com.micewine.emu.activities.PresetManagerActivity.Companion.AXIS_HAT_X_MINUS_KEY
import com.micewine.emu.activities.PresetManagerActivity.Companion.AXIS_HAT_X_PLUS_KEY
import com.micewine.emu.activities.PresetManagerActivity.Companion.AXIS_HAT_Y_MINUS_KEY
import com.micewine.emu.activities.PresetManagerActivity.Companion.AXIS_HAT_Y_PLUS_KEY
import com.micewine.emu.activities.PresetManagerActivity.Companion.AXIS_RZ_MINUS_KEY
import com.micewine.emu.activities.PresetManagerActivity.Companion.AXIS_RZ_PLUS_KEY
import com.micewine.emu.activities.PresetManagerActivity.Companion.AXIS_X_MINUS_KEY
import com.micewine.emu.activities.PresetManagerActivity.Companion.AXIS_X_PLUS_KEY
import com.micewine.emu.activities.PresetManagerActivity.Companion.AXIS_Y_MINUS_KEY
import com.micewine.emu.activities.PresetManagerActivity.Companion.AXIS_Y_PLUS_KEY
import com.micewine.emu.activities.PresetManagerActivity.Companion.AXIS_Z_MINUS_KEY
import com.micewine.emu.activities.PresetManagerActivity.Companion.AXIS_Z_PLUS_KEY
import com.micewine.emu.activities.PresetManagerActivity.Companion.BUTTON_A_KEY
import com.micewine.emu.activities.PresetManagerActivity.Companion.BUTTON_B_KEY
import com.micewine.emu.activities.PresetManagerActivity.Companion.BUTTON_L1_KEY
import com.micewine.emu.activities.PresetManagerActivity.Companion.BUTTON_L2_KEY
import com.micewine.emu.activities.PresetManagerActivity.Companion.BUTTON_R1_KEY
import com.micewine.emu.activities.PresetManagerActivity.Companion.BUTTON_R2_KEY
import com.micewine.emu.activities.PresetManagerActivity.Companion.BUTTON_SELECT_KEY
import com.micewine.emu.activities.PresetManagerActivity.Companion.BUTTON_START_KEY
import com.micewine.emu.activities.PresetManagerActivity.Companion.BUTTON_THUMBL_KEY
import com.micewine.emu.activities.PresetManagerActivity.Companion.BUTTON_THUMBR_KEY
import com.micewine.emu.activities.PresetManagerActivity.Companion.BUTTON_X_KEY
import com.micewine.emu.activities.PresetManagerActivity.Companion.BUTTON_Y_KEY
import com.micewine.emu.activities.PresetManagerActivity.Companion.SELECTED_CONTROLLER_PRESET
import com.micewine.emu.adapters.AdapterPreset
import com.micewine.emu.adapters.AdapterPreset.Companion.selectedPresetId
import com.micewine.emu.controller.XKeyCodes.ButtonMapping
import com.micewine.emu.fragments.CreatePresetFragment.Companion.CONTROLLER_PRESET
import java.io.File

class ControllerPresetManagerFragment(private val editShortcut: Boolean) : Fragment() {
    private var rootView: View? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        rootView = inflater.inflate(R.layout.fragment_general_settings, container, false)
        recyclerView = rootView?.findViewById(R.id.recyclerViewGeneralSettings)

        initialize(editShortcut)
        setAdapter()

        return rootView
    }

    private fun setAdapter() {
        recyclerView?.setAdapter(
            AdapterPreset(presetListNames, requireContext(), requireActivity().supportFragmentManager)
        )

        presetListNames.clear()
        presetList.forEach {
            addToAdapter(it.name, CONTROLLER_PRESET, true)
        }
    }

    private fun addToAdapter(titleSettings: String, type: Int, userPreset: Boolean) {
        presetListNames.add(
            AdapterPreset.Item(titleSettings, type, userPreset, editShortcut)
        )
    }

    companion object {
        private var recyclerView: RecyclerView? = null
        private val presetListNames: MutableList<AdapterPreset.Item> = mutableListOf()
        private var presetList: MutableList<ControllerPreset> = mutableListOf()
        private var editShortcut: Boolean = false

        private val gson = Gson()

        fun initialize(boolean: Boolean = false) {
            presetList = getControllerPresets()
            editShortcut = boolean
        }

        fun getMouseSensibility(name: String): Int {
            val index = presetList.indexOfFirst { it.name == name }
            if (index == -1) {
                return 100
            }

            return presetList[index].mouseSensibility
        }

        fun putMouseSensibility(name: String, value: Int) {
            val index = presetList.indexOfFirst { it.name == name }
            if (index == -1) {
                return
            }

            presetList[index].mouseSensibility = value

            saveControllerPresets()
        }

        fun getDeadZone(name: String): Int {
            val index = presetList.indexOfFirst { it.name == name }
            if (index == -1) {
                return 25
            }

            return presetList[index].deadZone
        }

        fun putDeadZone(name: String, value: Int) {
            val index = presetList.indexOfFirst { it.name == name }
            if (index == -1) {
                return
            }

            presetList[index].deadZone = value

            saveControllerPresets()
        }

        fun getControllerPreset(name: String, key: String): ButtonMapping? {
            val index = presetList.indexOfFirst { it.name == name }
            if (index == -1) {
                return null
            }

            return when (key) {
                BUTTON_A_KEY -> presetList[index].aButton
                BUTTON_B_KEY -> presetList[index].bButton
                BUTTON_X_KEY -> presetList[index].xButton
                BUTTON_Y_KEY -> presetList[index].yButton
                BUTTON_START_KEY -> presetList[index].startButton
                BUTTON_SELECT_KEY -> presetList[index].selectButton
                BUTTON_R1_KEY -> presetList[index].rbButton
                BUTTON_R2_KEY -> presetList[index].rtButton
                BUTTON_L1_KEY -> presetList[index].lbButton
                BUTTON_L2_KEY -> presetList[index].ltButton
                BUTTON_THUMBL_KEY -> presetList[index].thumbLButton
                BUTTON_THUMBR_KEY -> presetList[index].thumbRButton
                AXIS_X_PLUS_KEY -> presetList[index].axisXPlus
                AXIS_X_MINUS_KEY -> presetList[index].axisXMinus
                AXIS_Y_PLUS_KEY -> presetList[index].axisYPlus
                AXIS_Y_MINUS_KEY -> presetList[index].axisYMinus
                AXIS_Z_PLUS_KEY -> presetList[index].axisZPlus
                AXIS_Z_MINUS_KEY -> presetList[index].axisZMinus
                AXIS_RZ_PLUS_KEY -> presetList[index].axisRZPlus
                AXIS_RZ_MINUS_KEY -> presetList[index].axisRZMinus
                AXIS_HAT_X_PLUS_KEY -> presetList[index].axisHatXPlus
                AXIS_HAT_X_MINUS_KEY -> presetList[index].axisHatXMinus
                AXIS_HAT_Y_PLUS_KEY -> presetList[index].axisHatYPlus
                AXIS_HAT_Y_MINUS_KEY -> presetList[index].axisHatYMinus
                else -> null
            }
        }

        fun editControllerPreset(name: String, key: String, buttonMapping: ButtonMapping) {
            val index = presetList.indexOfFirst { it.name == name }
            if (index == -1) return

            when (key) {
                BUTTON_A_KEY -> presetList[index].aButton = buttonMapping
                BUTTON_B_KEY -> presetList[index].bButton = buttonMapping
                BUTTON_X_KEY -> presetList[index].xButton = buttonMapping
                BUTTON_Y_KEY -> presetList[index].yButton = buttonMapping
                BUTTON_START_KEY -> presetList[index].startButton = buttonMapping
                BUTTON_SELECT_KEY -> presetList[index].selectButton = buttonMapping
                BUTTON_R1_KEY -> presetList[index].rbButton = buttonMapping
                BUTTON_R2_KEY -> presetList[index].rtButton = buttonMapping
                BUTTON_L1_KEY -> presetList[index].lbButton = buttonMapping
                BUTTON_L2_KEY -> presetList[index].ltButton = buttonMapping
                BUTTON_THUMBL_KEY -> presetList[index].thumbLButton = buttonMapping
                BUTTON_THUMBR_KEY -> presetList[index].thumbRButton = buttonMapping
                AXIS_X_PLUS_KEY -> presetList[index].axisXPlus = buttonMapping
                AXIS_X_MINUS_KEY -> presetList[index].axisXMinus = buttonMapping
                AXIS_Y_PLUS_KEY -> presetList[index].axisYPlus = buttonMapping
                AXIS_Y_MINUS_KEY -> presetList[index].axisYMinus = buttonMapping
                AXIS_Z_PLUS_KEY -> presetList[index].axisZPlus = buttonMapping
                AXIS_Z_MINUS_KEY -> presetList[index].axisZMinus = buttonMapping
                AXIS_RZ_PLUS_KEY -> presetList[index].axisRZPlus = buttonMapping
                AXIS_RZ_MINUS_KEY -> presetList[index].axisRZMinus = buttonMapping
                AXIS_HAT_X_PLUS_KEY -> presetList[index].axisHatXPlus = buttonMapping
                AXIS_HAT_X_MINUS_KEY -> presetList[index].axisHatXMinus = buttonMapping
                AXIS_HAT_Y_PLUS_KEY -> presetList[index].axisHatYPlus = buttonMapping
                AXIS_HAT_Y_MINUS_KEY -> presetList[index].axisHatYMinus = buttonMapping
            }

            saveControllerPresets()
        }

        fun addControllerPreset(context: Context, name: String) {
            if (presetListNames.firstOrNull { it.titleSettings == name } != null) {
                Toast.makeText(context, context.getString(R.string.executable_already_added), Toast.LENGTH_LONG).show()
                return
            }

            presetList.add(
                ControllerPreset(name)
            )
            presetListNames.add(
                AdapterPreset.Item(name, CONTROLLER_PRESET, true, editShortcut)
            )

            recyclerView?.post {
                recyclerView?.adapter?.notifyItemInserted(presetListNames.size)
            }

            saveControllerPresets()
        }

        fun deleteControllerPreset(name: String) {
            val index = presetList.indexOfFirst { it.name == name }

            presetList.removeAt(index)
            presetListNames.removeAt(index)

            recyclerView?.adapter?.notifyItemRemoved(index)

            if (index == selectedPresetId) {
                preferences?.edit {
                    putString(SELECTED_CONTROLLER_PRESET, presetListNames.first().titleSettings)
                    apply()
                }
                recyclerView?.adapter?.notifyItemChanged(0)
            }

            saveControllerPresets()
        }

        fun renameControllerPreset(name: String, newName: String) {
            val index = presetList.indexOfFirst { it.name == name }

            presetList[index].name = newName
            presetListNames[index].titleSettings = newName

            recyclerView?.post {
                recyclerView?.adapter?.notifyItemChanged(index)
            }

            saveControllerPresets()
        }

        fun importControllerPreset(path: String) {
            val json = File(path).readLines()
            val listType = object : TypeToken<ControllerPreset>() {}.type
            if (json.size < 2 || json[0] != "controllerPreset") {
                recyclerView?.post {
                    Toast.makeText(recyclerView?.context, recyclerView?.context?.getString(R.string.invalid_controller_preset_file), Toast.LENGTH_LONG).show()
                }
                return
            }

            val importedPreset = gson.fromJson<ControllerPreset>(json[1], listType)

            presetList.add(importedPreset)
            presetListNames.add(
                AdapterPreset.Item(importedPreset.name, CONTROLLER_PRESET, true)
            )
            
            recyclerView?.post { 
                recyclerView?.adapter?.notifyItemInserted(presetListNames.size)
            }

            saveControllerPresets()
        }

        fun exportControllerPreset(name: String, path: String) {
            val index = presetList.indexOfFirst { it.name == name }
            val file = File(path)

            file.writeText("controllerPreset\n" + gson.toJson(presetList[index]))
        }

        private fun saveControllerPresets() {
            preferences?.edit {
                putString("controllerPresetList", gson.toJson(presetList))  
                apply()
            }
        }

        fun getControllerPresets(): MutableList<ControllerPreset> {
            val json = preferences?.getString("controllerPresetList", "")
            val listType = object : TypeToken<MutableList<ControllerPreset>>() {}.type
            val controllerPresetList = gson.fromJson<MutableList<ControllerPreset>>(json, listType)

            return controllerPresetList ?: mutableListOf()
        }

        data class ControllerPreset(
            var name: String,
            var aButton: ButtonMapping = ButtonMapping(),
            var bButton: ButtonMapping = ButtonMapping(),
            var xButton: ButtonMapping = ButtonMapping(),
            var yButton: ButtonMapping = ButtonMapping(),
            var startButton: ButtonMapping = ButtonMapping(),
            var selectButton: ButtonMapping = ButtonMapping(),
            var rbButton: ButtonMapping = ButtonMapping(),
            var rtButton: ButtonMapping = ButtonMapping(),
            var lbButton: ButtonMapping = ButtonMapping(),
            var ltButton: ButtonMapping = ButtonMapping(),
            var thumbLButton: ButtonMapping = ButtonMapping(),
            var thumbRButton: ButtonMapping = ButtonMapping(),
            var axisXPlus: ButtonMapping = ButtonMapping(),
            var axisXMinus: ButtonMapping = ButtonMapping(),
            var axisYPlus: ButtonMapping = ButtonMapping(),
            var axisYMinus: ButtonMapping = ButtonMapping(),
            var axisZPlus: ButtonMapping = ButtonMapping(),
            var axisZMinus: ButtonMapping = ButtonMapping(),
            var axisRZPlus: ButtonMapping = ButtonMapping(),
            var axisRZMinus: ButtonMapping = ButtonMapping(),
            var axisHatXPlus: ButtonMapping = ButtonMapping(),
            var axisHatXMinus: ButtonMapping = ButtonMapping(),
            var axisHatYPlus: ButtonMapping = ButtonMapping(),
            var axisHatYMinus: ButtonMapping = ButtonMapping(),
            var deadZone: Int = 25,
            var mouseSensibility: Int = 100
        )
    }
}
