package com.micewine.emu.fragments

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.edit
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.reflect.TypeToken
import com.micewine.emu.R
import com.micewine.emu.activities.MainActivity.Companion.getNativeResolution
import com.micewine.emu.activities.MainActivity.Companion.gson
import com.micewine.emu.activities.MainActivity.Companion.preferences
import com.micewine.emu.activities.PresetManagerActivity.Companion.SELECTED_VIRTUAL_CONTROLLER_PRESET
import com.micewine.emu.adapters.AdapterPreset
import com.micewine.emu.adapters.AdapterPreset.Companion.selectedPresetId
import com.micewine.emu.fragments.CreatePresetFragment.Companion.VIRTUAL_CONTROLLER_PRESET
import com.micewine.emu.views.VirtualKeyboardInputCreatorView.Companion.GRID_SIZE
import com.micewine.emu.views.VirtualKeyboardInputView
import java.io.File
import kotlin.math.roundToInt

class VirtualControllerPresetManagerFragment(private val editShortcut: Boolean) : Fragment() {
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
            AdapterPreset(presetListAdapters, requireContext(), requireActivity().supportFragmentManager)
        )

        presetListAdapters.clear()
        presetList.forEach {
            addToAdapter(it.name, VIRTUAL_CONTROLLER_PRESET, true)
        }
    }

    private fun addToAdapter(titleSettings: String, type: Int, userPreset: Boolean) {
        presetListAdapters.add(
            AdapterPreset.Item(titleSettings, type, userPreset, editShortcut)
        )
    }

    companion object {
        private var recyclerView: RecyclerView? = null
        private val presetListAdapters: MutableList<AdapterPreset.Item> = mutableListOf()
        private var presetList: MutableList<VirtualControllerPreset> = mutableListOf()
        private var editShortcut: Boolean = false
        private val listType = object : TypeToken<VirtualControllerPreset>() {}.type

        fun initialize(editable: Boolean = false) {
            presetList = getVirtualControllerPresets()
            editShortcut = editable
        }

        fun getVirtualControllerPreset(name: String): VirtualControllerPreset? {
            val index = presetList.indexOfFirst { it.name == name }
            if (index == -1) return null

            return presetList[index]
        }

        fun putVirtualControllerPreset(name: String, resolution: String, buttonList: MutableList<VirtualKeyboardInputView.VirtualButton>, analogList: MutableList<VirtualKeyboardInputView.VirtualAnalog>, dpadList: MutableList<VirtualKeyboardInputView.VirtualDPad>) {
            val index = presetList.indexOfFirst { it.name == name }
            if (index == -1) return

            presetList[index] = VirtualControllerPreset(name, resolution, mutableListOf(), mutableListOf(), mutableListOf())

            buttonList.forEach {
                presetList[index].buttons.add(it)
            }
            analogList.forEach {
                presetList[index].analogs.add(it)
            }
            dpadList.forEach {
                presetList[index].dpads.add(it)
            }

            saveVirtualControllerPresets()
        }

        fun addVirtualControllerPreset(context: Context, name: String) {
            if (presetListAdapters.firstOrNull { it.titleSettings == name } != null) {
                Toast.makeText(context, context.getString(R.string.executable_already_added), Toast.LENGTH_LONG).show()
                return
            }

            val defaultPreset = VirtualControllerPreset(name, "", mutableListOf(), mutableListOf(), mutableListOf())

            presetList.add(defaultPreset)
            presetListAdapters.add(
                AdapterPreset.Item(name, VIRTUAL_CONTROLLER_PRESET, true, editShortcut)
            )

            recyclerView?.adapter?.notifyItemInserted(presetListAdapters.size)

            saveVirtualControllerPresets()
        }

        fun deleteVirtualControllerPreset(name: String) {
            val index = presetList.indexOfFirst { it.name == name }

            presetList.removeAt(index)
            presetListAdapters.removeAt(index)

            recyclerView?.adapter?.notifyItemRemoved(index)

            if (index == selectedPresetId) {
                preferences?.edit {
                    putString(SELECTED_VIRTUAL_CONTROLLER_PRESET, presetListAdapters.firstOrNull()?.titleSettings)
                    apply()
                }
                recyclerView?.adapter?.notifyItemChanged(0)
            }

            saveVirtualControllerPresets()
        }

        fun renameVirtualControllerPreset(name: String, newName: String) {
            val index = presetList.indexOfFirst { it.name == name }

            presetList[index].name = newName
            presetListAdapters[index].titleSettings = newName

            recyclerView?.adapter?.notifyItemChanged(index)

            saveVirtualControllerPresets()
        }

        fun importVirtualControllerPreset(context: Context, file: File): Boolean {
            val lines = file.readLines()
            if (lines.size < 2) return false

            val type = lines[0]
            if (type != "virtualControllerPreset") return false

            val json = lines[1]
            val preset = gson.fromJson<VirtualControllerPreset>(json, listType)
            val canAutoAdjust = json.contains("resolution")

            var presetName = preset.name
            var count = 1

            while (presetList.any { it.name == presetName }) {
                presetName = "${preset.name}-${count++}"
            }

            preset.name = presetName

            if (canAutoAdjust) {
                val nativeResolution = getNativeResolution(context)

                if (preset.resolution != nativeResolution) {
                    val nativeSplit = nativeResolution.split("x").map { it.toFloat() }
                    val processedSplit = preset.resolution.split("x").map { it.toFloat() }

                    val multiplierX = nativeSplit[0] / processedSplit[0] * 100F
                    val multiplierY = nativeSplit[1] / processedSplit[1] * 100F

                    preset.buttons.forEach {
                        it.x = (it.x / 100F * multiplierX / GRID_SIZE).roundToInt() * GRID_SIZE.toFloat()
                        it.y = (it.y / 100F * multiplierY / GRID_SIZE).roundToInt() * GRID_SIZE.toFloat()
                    }
                    preset.analogs.forEach {
                        it.x = (it.x / 100F * multiplierX / GRID_SIZE).roundToInt() * GRID_SIZE.toFloat()
                        it.y = (it.y / 100F * multiplierY / GRID_SIZE).roundToInt() * GRID_SIZE.toFloat()
                    }
                    preset.dpads.forEach {
                        it.x = (it.x / 100F * multiplierX / GRID_SIZE).roundToInt() * GRID_SIZE.toFloat()
                        it.y = (it.y / 100F * multiplierY / GRID_SIZE).roundToInt() * GRID_SIZE.toFloat()
                    }
                }
            }

            presetList.add(preset)
            presetListAdapters.add(
                AdapterPreset.Item(preset.name, VIRTUAL_CONTROLLER_PRESET, true)
            )

            recyclerView?.post {
                recyclerView?.adapter?.notifyItemInserted(presetListAdapters.size)
            }

            saveVirtualControllerPresets()

            return true
        }

        fun exportVirtualControllerPreset(name: String, file: File) {
            val index = presetList.indexOfFirst { it.name == name }

            file.writeText("virtualControllerPreset\n" + gson.toJson(presetList[index]))
        }

        private fun saveVirtualControllerPresets() {
            preferences?.edit {
                putString("virtualControllerPresetList", gson.toJson(presetList))
                apply()
            }
        }

        fun getVirtualControllerPresets(): MutableList<VirtualControllerPreset> {
            val json = preferences?.getString("virtualControllerPresetList", "")
            val listType = object : TypeToken<MutableList<VirtualControllerPreset>>() {}.type

            return gson.fromJson(json, listType) ?: mutableListOf()
        }

        data class VirtualControllerPreset(
            var name: String,
            var resolution: String,
            var analogs: MutableList<VirtualKeyboardInputView.VirtualAnalog>,
            var buttons: MutableList<VirtualKeyboardInputView.VirtualButton>,
            var dpads: MutableList<VirtualKeyboardInputView.VirtualDPad>
        )
    }
}
