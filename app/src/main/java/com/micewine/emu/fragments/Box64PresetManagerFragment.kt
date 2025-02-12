package com.micewine.emu.fragments

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.edit
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.micewine.emu.R
import com.micewine.emu.activities.GeneralSettingsActivity.Companion.BOX64_AVX
import com.micewine.emu.activities.GeneralSettingsActivity.Companion.BOX64_DYNAREC_ALIGNED_ATOMICS
import com.micewine.emu.activities.GeneralSettingsActivity.Companion.BOX64_DYNAREC_BIGBLOCK
import com.micewine.emu.activities.GeneralSettingsActivity.Companion.BOX64_DYNAREC_CALLRET
import com.micewine.emu.activities.GeneralSettingsActivity.Companion.BOX64_DYNAREC_DIRTY
import com.micewine.emu.activities.GeneralSettingsActivity.Companion.BOX64_DYNAREC_FASTNAN
import com.micewine.emu.activities.GeneralSettingsActivity.Companion.BOX64_DYNAREC_FASTROUND
import com.micewine.emu.activities.GeneralSettingsActivity.Companion.BOX64_DYNAREC_FORWARD
import com.micewine.emu.activities.GeneralSettingsActivity.Companion.BOX64_DYNAREC_NATIVEFLAGS
import com.micewine.emu.activities.GeneralSettingsActivity.Companion.BOX64_DYNAREC_PAUSE
import com.micewine.emu.activities.GeneralSettingsActivity.Companion.BOX64_DYNAREC_SAFEFLAGS
import com.micewine.emu.activities.GeneralSettingsActivity.Companion.BOX64_DYNAREC_STRONGMEM
import com.micewine.emu.activities.GeneralSettingsActivity.Companion.BOX64_DYNAREC_WAIT
import com.micewine.emu.activities.GeneralSettingsActivity.Companion.BOX64_DYNAREC_WEAKBARRIER
import com.micewine.emu.activities.GeneralSettingsActivity.Companion.BOX64_DYNAREC_X87DOUBLE
import com.micewine.emu.activities.GeneralSettingsActivity.Companion.BOX64_MMAP32
import com.micewine.emu.activities.GeneralSettingsActivity.Companion.BOX64_SSE42
import com.micewine.emu.activities.PresetManagerActivity.Companion.SELECTED_BOX64_PRESET_KEY
import com.micewine.emu.adapters.AdapterPreset
import com.micewine.emu.adapters.AdapterPreset.Companion.selectedPresetId
import com.micewine.emu.fragments.CreatePresetFragment.Companion.BOX64_PRESET
import java.util.Collections

class Box64PresetManagerFragment : Fragment() {
    private var rootView: View? = null
    private var recyclerView: RecyclerView? = null
    private val receiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                ACTION_DELETE_BOX64_PRESET -> {
                    val index = intent.getIntExtra("index", -1)

                    recyclerView?.adapter?.notifyItemRemoved(index)

                    if (index == selectedPresetId) {
                        preferences?.edit {
                            putString(SELECTED_BOX64_PRESET_KEY, presetListNames.first().titleSettings)
                            apply()
                        }
                        recyclerView?.adapter?.notifyItemChanged(0)
                    }
                }
                ACTION_ADD_BOX64_PRESET -> {
                    recyclerView?.adapter?.notifyItemInserted(presetListNames.size)
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        rootView = inflater.inflate(R.layout.fragment_general_settings, container, false)
        recyclerView = rootView?.findViewById(R.id.recyclerViewGeneralSettings)

        initialize(requireContext())
        setAdapter()

        requireActivity().registerReceiver(receiver, object : IntentFilter() {
            init {
                addAction(ACTION_ADD_BOX64_PRESET)
                addAction(ACTION_DELETE_BOX64_PRESET)
            }
        })

        return rootView
    }

    override fun onDestroy() {
        super.onDestroy()
        requireActivity().unregisterReceiver(receiver)
    }

    private fun setAdapter() {
        recyclerView?.setAdapter(AdapterPreset(presetListNames, requireContext(), requireActivity().supportFragmentManager))

        presetListNames.clear()

        presetList = getBox64Presets()
        presetList?.forEach {
            addToAdapter(it[0], BOX64_PRESET, true)
        }
    }

    private fun addToAdapter(titleSettings: String, type: Int, userPreset: Boolean) {
        presetListNames.add(AdapterPreset.Item(titleSettings, type, userPreset))
    }

    companion object {
        private val presetListNames: MutableList<AdapterPreset.Item> = mutableListOf()
        private var presetList: MutableList<MutableList<String>>? = null
        private var preferences: SharedPreferences? = null

        const val ACTION_DELETE_BOX64_PRESET = "com.micewine.emu.ACTION_DELETE_BOX64_PRESET"
        const val ACTION_ADD_BOX64_PRESET = "com.micewine.emu.ACTION_ADD_BOX64_PRESET"

        private val gson = Gson()

        private val mappingMap = mapOf(
            BOX64_MMAP32 to 1,
            BOX64_AVX to 2,
            BOX64_SSE42 to 3,
            BOX64_DYNAREC_BIGBLOCK to 4,
            BOX64_DYNAREC_STRONGMEM to 5,
            BOX64_DYNAREC_WEAKBARRIER to 6,
            BOX64_DYNAREC_PAUSE to 7,
            BOX64_DYNAREC_X87DOUBLE to 8,
            BOX64_DYNAREC_FASTNAN to 9,
            BOX64_DYNAREC_FASTROUND to 10,
            BOX64_DYNAREC_SAFEFLAGS to 11,
            BOX64_DYNAREC_CALLRET to 12,
            BOX64_DYNAREC_ALIGNED_ATOMICS to 13,
            BOX64_DYNAREC_NATIVEFLAGS to 14,
            BOX64_DYNAREC_WAIT to 15,
            BOX64_DYNAREC_DIRTY to 16,
            BOX64_DYNAREC_FORWARD to 17,
        )

        fun initialize(context: Context) {
            preferences = PreferenceManager.getDefaultSharedPreferences(context)
            presetList = getBox64Presets()
        }

        fun getBox64Mapping(name: String, key: String): List<String> {
            val index = presetList?.indexOfFirst { it[0] == name } ?: return listOf()

            if (index == -1) {
                return listOf()
            }

            return presetList!![index][mappingMap[key]!!].split(":")
        }

        fun editBox64Mapping(name: String, key: String, value: String) {
            var index = presetList?.indexOfFirst { it[0] == name } ?: return

            if (index == -1) {
                presetList!![0][0] = name
                index = 0
            }

            presetList!![index][mappingMap[key]!!] = value

            saveBox64Preset()
        }

        fun addBox64Preset(context: Context, name: String) {
            if (presetListNames.firstOrNull { it.titleSettings == name } != null) {
                Toast.makeText(context, context.getString(R.string.executable_already_added), Toast.LENGTH_LONG).show()
                return
            }

            val defaultPreset = ArrayList(Collections.nCopies(mappingMap.size + 1, ":")).apply {
                this[0] = name
                this[mappingMap[BOX64_MMAP32]!!] = "true"
                this[mappingMap[BOX64_AVX]!!] = "2"
                this[mappingMap[BOX64_SSE42]!!] = "true"
                this[mappingMap[BOX64_DYNAREC_BIGBLOCK]!!] = "2"
                this[mappingMap[BOX64_DYNAREC_STRONGMEM]!!] = "1"
                this[mappingMap[BOX64_DYNAREC_WEAKBARRIER]!!] = "2"
                this[mappingMap[BOX64_DYNAREC_PAUSE]!!] = "0"
                this[mappingMap[BOX64_DYNAREC_X87DOUBLE]!!] = "0"
                this[mappingMap[BOX64_DYNAREC_FASTNAN]!!] = "1"
                this[mappingMap[BOX64_DYNAREC_FASTROUND]!!] = "1"
                this[mappingMap[BOX64_DYNAREC_SAFEFLAGS]!!] = "1"
                this[mappingMap[BOX64_DYNAREC_CALLRET]!!] = "true"
                this[mappingMap[BOX64_DYNAREC_ALIGNED_ATOMICS]!!] = "false"
                this[mappingMap[BOX64_DYNAREC_NATIVEFLAGS]!!] = "true"
                this[mappingMap[BOX64_DYNAREC_WAIT]!!] = "true"
                this[mappingMap[BOX64_DYNAREC_DIRTY]!!] = "false"
                this[mappingMap[BOX64_DYNAREC_FORWARD]!!] = "128"
            }

            presetList?.add(defaultPreset)
            presetListNames.add(
                AdapterPreset.Item(name, BOX64_PRESET, true)
            )

            context.sendBroadcast(
                Intent(ACTION_ADD_BOX64_PRESET)
            )

            saveBox64Preset()
        }

        fun deleteBox64Preset(context: Context, name: String) {
            val index = presetList?.indexOfFirst { it[0] == name }

            presetList?.removeAt(index!!)
            presetListNames.removeAt(index!!)

            val intent = Intent(ACTION_DELETE_BOX64_PRESET).apply {
                putExtra("index", index)
            }

            context.sendBroadcast(intent)

            saveBox64Preset()
        }

        private fun saveBox64Preset() {
            preferences?.edit {
                putString("box64PresetList", gson.toJson(presetList))
                apply()
            }
        }

        fun getBox64Presets(): MutableList<MutableList<String>> {
            val json = preferences?.getString("box64PresetList", "")
            val listType = object : TypeToken<MutableList<MutableList<String>>>() {}.type

            return gson.fromJson(json, listType) ?: mutableListOf(
                ArrayList(Collections.nCopies(mappingMap.size + 1, ":")).apply {
                    this[0] = "default"
                    this[mappingMap[BOX64_MMAP32]!!] = "true"
                    this[mappingMap[BOX64_AVX]!!] = "2"
                    this[mappingMap[BOX64_SSE42]!!] = "true"
                    this[mappingMap[BOX64_DYNAREC_BIGBLOCK]!!] = "2"
                    this[mappingMap[BOX64_DYNAREC_STRONGMEM]!!] = "1"
                    this[mappingMap[BOX64_DYNAREC_WEAKBARRIER]!!] = "2"
                    this[mappingMap[BOX64_DYNAREC_PAUSE]!!] = "0"
                    this[mappingMap[BOX64_DYNAREC_X87DOUBLE]!!] = "0"
                    this[mappingMap[BOX64_DYNAREC_FASTNAN]!!] = "1"
                    this[mappingMap[BOX64_DYNAREC_FASTROUND]!!] = "1"
                    this[mappingMap[BOX64_DYNAREC_SAFEFLAGS]!!] = "1"
                    this[mappingMap[BOX64_DYNAREC_CALLRET]!!] = "true"
                    this[mappingMap[BOX64_DYNAREC_ALIGNED_ATOMICS]!!] = "false"
                    this[mappingMap[BOX64_DYNAREC_NATIVEFLAGS]!!] = "true"
                    this[mappingMap[BOX64_DYNAREC_WAIT]!!] = "true"
                    this[mappingMap[BOX64_DYNAREC_DIRTY]!!] = "false"
                    this[mappingMap[BOX64_DYNAREC_FORWARD]!!] = "128"
                }
            )
        }
    }
}
