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
import com.google.gson.reflect.TypeToken
import com.micewine.emu.R
import com.micewine.emu.activities.GeneralSettingsActivity.Companion.BOX64_AVX_DEFAULT_VALUE
import com.micewine.emu.activities.GeneralSettingsActivity.Companion.BOX64_DYNAREC_ALIGNED_ATOMICS_DEFAULT_VALUE
import com.micewine.emu.activities.GeneralSettingsActivity.Companion.BOX64_DYNAREC_BIGBLOCK_DEFAULT_VALUE
import com.micewine.emu.activities.GeneralSettingsActivity.Companion.BOX64_DYNAREC_CALLRET_DEFAULT_VALUE
import com.micewine.emu.activities.GeneralSettingsActivity.Companion.BOX64_DYNAREC_DF_DEFAULT_VALUE
import com.micewine.emu.activities.GeneralSettingsActivity.Companion.BOX64_DYNAREC_DIRTY_DEFAULT_VALUE
import com.micewine.emu.activities.GeneralSettingsActivity.Companion.BOX64_DYNAREC_FASTNAN_DEFAULT_VALUE
import com.micewine.emu.activities.GeneralSettingsActivity.Companion.BOX64_DYNAREC_FASTROUND_DEFAULT_VALUE
import com.micewine.emu.activities.GeneralSettingsActivity.Companion.BOX64_DYNAREC_FORWARD_DEFAULT_VALUE
import com.micewine.emu.activities.GeneralSettingsActivity.Companion.BOX64_DYNAREC_NATIVEFLAGS_DEFAULT_VALUE
import com.micewine.emu.activities.GeneralSettingsActivity.Companion.BOX64_DYNAREC_PAUSE_DEFAULT_VALUE
import com.micewine.emu.activities.GeneralSettingsActivity.Companion.BOX64_DYNAREC_SAFEFLAGS_DEFAULT_VALUE
import com.micewine.emu.activities.GeneralSettingsActivity.Companion.BOX64_DYNAREC_STRONGMEM_DEFAULT_VALUE
import com.micewine.emu.activities.GeneralSettingsActivity.Companion.BOX64_DYNAREC_WAIT_DEFAULT_VALUE
import com.micewine.emu.activities.GeneralSettingsActivity.Companion.BOX64_DYNAREC_WEAKBARRIER_DEFAULT_VALUE
import com.micewine.emu.activities.GeneralSettingsActivity.Companion.BOX64_DYNAREC_X87DOUBLE_DEFAULT_VALUE
import com.micewine.emu.activities.GeneralSettingsActivity.Companion.BOX64_MMAP32_DEFAULT_VALUE
import com.micewine.emu.activities.GeneralSettingsActivity.Companion.BOX64_SSE42_DEFAULT_VALUE
import com.micewine.emu.activities.MainActivity.Companion.gson
import com.micewine.emu.activities.MainActivity.Companion.preferences
import com.micewine.emu.activities.MainActivity.Companion.strBoolToNum
import com.micewine.emu.activities.PresetManagerActivity.Companion.SELECTED_BOX64_PRESET
import com.micewine.emu.adapters.AdapterPreset
import com.micewine.emu.adapters.AdapterPreset.Companion.selectedPresetId
import com.micewine.emu.fragments.CreatePresetFragment.Companion.BOX64_PRESET
import java.io.File
import java.lang.reflect.Type

class Box64PresetManagerFragment : Fragment() {
    private var rootView: View? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        rootView = inflater.inflate(R.layout.fragment_general_settings, container, false)
        recyclerView = rootView?.findViewById(R.id.recyclerViewGeneralSettings)

        initialize()
        setAdapter()

        return rootView
    }

    private fun setAdapter() {
        recyclerView?.setAdapter(
            AdapterPreset(presetListAdapters, requireContext(), requireActivity().supportFragmentManager)
        )

        presetListAdapters.clear()
        presetList.forEach {
            addToAdapter(it.name, BOX64_PRESET, true)
        }
    }

    private fun addToAdapter(titleSettings: String, type: Int, userPreset: Boolean) {
        presetListAdapters.add(
            AdapterPreset.Item(titleSettings, type, userPreset)
        )
    }

    companion object {
        private var recyclerView: RecyclerView? = null
        private var presetListAdapters: MutableList<AdapterPreset.Item> = mutableListOf()
        private var presetList: MutableList<Box64Preset> = mutableListOf()
        private val listTypeV1: Type = object : TypeToken<MutableList<String>>() {}.type
        private val listTypeV2: Type = object : TypeToken<Box64Preset>() {}.type

        fun initialize() {
            presetList = getBox64Presets()
        }

        fun addBox64Preset(context: Context, name: String) {
            if (presetListAdapters.firstOrNull { it.titleSettings == name } != null) {
                Toast.makeText(context, context.getString(R.string.executable_already_added), Toast.LENGTH_LONG).show()
                return
            }

            val defaultPreset = Box64Preset(name)

            presetList.add(defaultPreset)
            presetListAdapters.add(
                AdapterPreset.Item(name, BOX64_PRESET, true)
            )

            recyclerView?.adapter?.notifyItemInserted(presetList.size)

            saveBox64Preset()
        }

        fun deleteBox64Preset(name: String): Boolean {
            if (presetList.size == 1) return false

            val index = presetList.indexOfFirst { it.name == name }

            presetList.removeAt(index)
            presetListAdapters.removeAt(index)

            recyclerView?.adapter?.notifyItemRemoved(index)

            if (index == selectedPresetId) {
                preferences?.edit {
                    putString(SELECTED_BOX64_PRESET, presetList.first().name)
                    apply()
                }
                recyclerView?.adapter?.notifyItemChanged(0)
            }

            saveBox64Preset()

            return true
        }

        fun renameBox64Preset(name: String, newName: String) {
            val index = presetList.indexOfFirst { it.name == name }

            presetList[index].name = newName
            presetListAdapters[index].titleSettings = newName

            recyclerView?.adapter?.notifyItemChanged(index)

            saveBox64Preset()
        }

        fun putBox64BigBlock(name: String, value: Int) {
            val index = presetList.indexOfFirst { it.name == name }
            if (index == -1) return

            presetList[index].box64BigBlock = value

            saveBox64Preset()
        }

        fun getBox64BigBlock(name: String): Int {
            val index = presetList.indexOfFirst { it.name == name }
            if (index == -1) return 2

            return presetList[index].box64BigBlock
        }

        fun putBox64StrongMem(name: String, value: Int) {
            val index = presetList.indexOfFirst { it.name == name }
            if (index == -1) return

            presetList[index].box64StrongMem = value

            saveBox64Preset()
        }

        fun getBox64StrongMem(name: String): Int {
            val index = presetList.indexOfFirst { it.name == name }
            if (index == -1) return 1

            return presetList[index].box64StrongMem
        }

        fun putBox64WeakBarrier(name: String, value: Int) {
            val index = presetList.indexOfFirst { it.name == name }
            if (index == -1) return

            presetList[index].box64WeakBarrier = value

            saveBox64Preset()
        }

        fun getBox64WeakBarrier(name: String): Int {
            val index = presetList.indexOfFirst { it.name == name }
            if (index == -1) return 2

            return presetList[index].box64WeakBarrier
        }

        fun putBox64Pause(name: String, value: Int) {
            val index = presetList.indexOfFirst { it.name == name }
            if (index == -1) return

            presetList[index].box64Pause = value

            saveBox64Preset()
        }

        fun getBox64Pause(name: String): Int {
            val index = presetList.indexOfFirst { it.name == name }
            if (index == -1) return 2

            return presetList[index].box64Pause
        }

        fun putBox64X87Double(name: String, value: Int) {
            val index = presetList.indexOfFirst { it.name == name }
            if (index == -1) return

            presetList[index].box64X87Double = value

            saveBox64Preset()
        }

        fun getBox64X87Double(name: String): Int {
            val index = presetList.indexOfFirst { it.name == name }
            if (index == -1) return 2

            return presetList[index].box64X87Double
        }

        fun putBox64FastNan(name: String, value: Boolean) {
            val index = presetList.indexOfFirst { it.name == name }
            if (index == -1) return

            presetList[index].box64FastNan = boolToInt(value)

            saveBox64Preset()
        }

        fun getBox64FastNan(name: String): Boolean {
            val index = presetList.indexOfFirst { it.name == name }
            if (index == -1) return true

            return intToBool(presetList[index].box64FastNan)
        }

        fun putBox64FastRound(name: String, value: Boolean) {
            val index = presetList.indexOfFirst { it.name == name }
            if (index == -1) return

            presetList[index].box64FastRound = boolToInt(value)

            saveBox64Preset()
        }

        fun getBox64FastRound(name: String): Boolean {
            val index = presetList.indexOfFirst { it.name == name }
            if (index == -1) return true

            return intToBool(presetList[index].box64FastRound)
        }

        fun putBox64SafeFlags(name: String, value: Int) {
            val index = presetList.indexOfFirst { it.name == name }
            if (index == -1) return

            presetList[index].box64SafeFlags = value

            saveBox64Preset()
        }

        fun getBox64SafeFlags(name: String): Int {
            val index = presetList.indexOfFirst { it.name == name }
            if (index == -1) return 1

            return presetList[index].box64SafeFlags
        }

        fun putBox64CallRet(name: String, value: Int) {
            val index = presetList.indexOfFirst { it.name == name }
            if (index == -1) return

            presetList[index].box64CallRet = value

            saveBox64Preset()
        }

        fun getBox64CallRet(name: String): Int {
            val index = presetList.indexOfFirst { it.name == name }
            if (index == -1) return 0

            return presetList[index].box64CallRet
        }

        fun putBox64AlignedAtomics(name: String, value: Boolean) {
            val index = presetList.indexOfFirst { it.name == name }
            if (index == -1) return

            presetList[index].box64AlignedAtomics = boolToInt(value)

            saveBox64Preset()
        }

        fun getBox64AlignedAtomics(name: String): Boolean {
            val index = presetList.indexOfFirst { it.name == name }
            if (index == -1) return true

            return intToBool(presetList[index].box64AlignedAtomics)
        }

        fun putBox64NativeFlags(name: String, value: Boolean) {
            val index = presetList.indexOfFirst { it.name == name }
            if (index == -1) return

            presetList[index].box64NativeFlags = boolToInt(value)

            saveBox64Preset()
        }

        fun getBox64NativeFlags(name: String): Boolean {
            val index = presetList.indexOfFirst { it.name == name }
            if (index == -1) return true

            return intToBool(presetList[index].box64NativeFlags)
        }

        fun putBox64Wait(name: String, value: Boolean) {
            val index = presetList.indexOfFirst { it.name == name }
            if (index == -1) return

            presetList[index].box64Wait = boolToInt(value)

            saveBox64Preset()
        }

        fun getBox64Wait(name: String): Boolean {
            val index = presetList.indexOfFirst { it.name == name }
            if (index == -1) return true

            return intToBool(presetList[index].box64Wait)
        }

        fun putBox64Dirty(name: String, value: Int) {
            val index = presetList.indexOfFirst { it.name == name }
            if (index == -1) return

            presetList[index].box64Dirty = value

            saveBox64Preset()
        }

        fun getBox64Dirty(name: String): Int {
            val index = presetList.indexOfFirst { it.name == name }
            if (index == -1) return 0

            return presetList[index].box64Dirty
        }

        fun putBox64Forward(name: String, value: Int) {
            val index = presetList.indexOfFirst { it.name == name }
            if (index == -1) return

            presetList[index].box64Forward = value

            saveBox64Preset()
        }

        fun getBox64Forward(name: String): Int {
            val index = presetList.indexOfFirst { it.name == name }
            if (index == -1) return 128

            return presetList[index].box64Forward
        }

        fun putBox64DF(name: String, value: Boolean) {
            val index = presetList.indexOfFirst { it.name == name }
            if (index == -1) return

            presetList[index].box64DF = boolToInt(value)

            saveBox64Preset()
        }

        fun getBox64DF(name: String): Boolean {
            val index = presetList.indexOfFirst { it.name == name }
            if (index == -1) return true

            return intToBool(presetList[index].box64DF)
        }

        fun putBox64Avx(name: String, value: Int) {
            val index = presetList.indexOfFirst { it.name == name }
            if (index == -1) return

            presetList[index].box64Avx = value

            saveBox64Preset()
        }

        fun getBox64Avx(name: String): Int {
            val index = presetList.indexOfFirst { it.name == name }
            if (index == -1) return 2

            return presetList[index].box64Avx
        }

        fun putBox64Sse2(name: String, value: Boolean) {
            val index = presetList.indexOfFirst { it.name == name }
            if (index == -1) return

            presetList[index].box64Sse42 = boolToInt(value)

            saveBox64Preset()
        }

        fun getBox64Sse2(name: String): Boolean {
            val index = presetList.indexOfFirst { it.name == name }
            if (index == -1) return true

            return intToBool(presetList[index].box64Sse42)
        }

        fun putBox64MMap32(name: String, value: Boolean) {
            val index = presetList.indexOfFirst { it.name == name }
            if (index == -1) return

            presetList[index].box64MMap32 = boolToInt(value)

            saveBox64Preset()
        }

        fun getBox64MMap32(name: String): Boolean {
            val index = presetList.indexOfFirst { it.name == name }
            if (index == -1) return true

            return intToBool(presetList[index].box64MMap32)
        }

        private fun v1PresetToV2(v1Preset: MutableList<String>): Box64Preset {
            return Box64Preset(
                v1Preset[0],
                strBoolToNum(v1Preset[1].toBoolean()),
                v1Preset[2].toInt(),
                strBoolToNum(v1Preset[3].toBoolean()),
                v1Preset[4].toInt(),
                v1Preset[5].toInt(),
                v1Preset[6].toInt(),
                v1Preset[7].toInt(),
                strBoolToNum(v1Preset[8].toBoolean()),
                strBoolToNum(v1Preset[9].toBoolean()),
                strBoolToNum(v1Preset[10].toBoolean()),
                v1Preset[11].toInt(),
                strBoolToNum(v1Preset[12].toBoolean()),
                strBoolToNum(v1Preset[13].toBoolean()),
                strBoolToNum(v1Preset[14].toBoolean()),
                strBoolToNum(v1Preset[15].toBoolean()),
                strBoolToNum(v1Preset[16].toBoolean()),
                v1Preset[17].toInt()
            )
        }

        fun importBox64Preset(file: File): Boolean {
            val lines = file.readLines()
            if (lines.size < 2) return false

            val type = lines[0]
            val json = lines[1]

            val preset: Box64Preset = when (type) {
                "box64Preset" -> v1PresetToV2(gson.fromJson(json, listTypeV1))
                "box64PresetV2" -> gson.fromJson(json, listTypeV2)
                else -> return false
            }

            var presetName = preset.name
            var count = 1
            while (presetList.any { it.name == presetName }) {
                presetName = "${preset.name}-${count++}"
            }

            preset.name = presetName

            presetList.add(preset)
            presetListAdapters.add(
                AdapterPreset.Item(preset.name, BOX64_PRESET, true)
            )

            recyclerView?.post {
                recyclerView?.adapter?.notifyItemInserted(presetList.size)
            }

            saveBox64Preset()

            return true
        }

        fun exportBox64Preset(name: String, file: File) {
            val index = presetList.indexOfFirst { it.name == name }

            file.writeText("box64PresetV2\n" + gson.toJson(presetList[index]))
        }

        private fun saveBox64Preset() {
            preferences?.edit {
                putString("box64PresetList", gson.toJson(presetList))
                apply()
            }
        }

        fun getBox64Presets(): MutableList<Box64Preset> {
            val json = preferences?.getString("box64PresetList", "")
            val listType = object : TypeToken<MutableList<Box64Preset>>() {}.type
            val presetList = gson.fromJson<MutableList<Box64Preset>>(json, listType)

            return presetList ?: mutableListOf(
                Box64Preset("default")
            )
        }

        private fun intToBool(int: Int): Boolean = (int == 1)
        private fun boolToInt(bool: Boolean): Int = if (bool) 1 else 0

        data class Box64Preset(
            var name: String,
            var box64MMap32: Int = BOX64_MMAP32_DEFAULT_VALUE,
            var box64Avx: Int = BOX64_AVX_DEFAULT_VALUE,
            var box64Sse42: Int = BOX64_SSE42_DEFAULT_VALUE,
            var box64BigBlock: Int = BOX64_DYNAREC_BIGBLOCK_DEFAULT_VALUE,
            var box64StrongMem: Int = BOX64_DYNAREC_STRONGMEM_DEFAULT_VALUE,
            var box64WeakBarrier: Int = BOX64_DYNAREC_WEAKBARRIER_DEFAULT_VALUE,
            var box64Pause: Int = BOX64_DYNAREC_PAUSE_DEFAULT_VALUE,
            var box64X87Double: Int = BOX64_DYNAREC_X87DOUBLE_DEFAULT_VALUE,
            var box64FastNan: Int = BOX64_DYNAREC_FASTNAN_DEFAULT_VALUE,
            var box64FastRound: Int = BOX64_DYNAREC_FASTROUND_DEFAULT_VALUE,
            var box64SafeFlags: Int = BOX64_DYNAREC_SAFEFLAGS_DEFAULT_VALUE,
            var box64CallRet: Int = BOX64_DYNAREC_CALLRET_DEFAULT_VALUE,
            var box64AlignedAtomics: Int = BOX64_DYNAREC_ALIGNED_ATOMICS_DEFAULT_VALUE,
            var box64NativeFlags: Int = BOX64_DYNAREC_NATIVEFLAGS_DEFAULT_VALUE,
            var box64Wait: Int = BOX64_DYNAREC_WAIT_DEFAULT_VALUE,
            var box64Dirty: Int = BOX64_DYNAREC_DIRTY_DEFAULT_VALUE,
            var box64Forward: Int = BOX64_DYNAREC_FORWARD_DEFAULT_VALUE,
            var box64DF: Int = BOX64_DYNAREC_DF_DEFAULT_VALUE
        )
    }
}
