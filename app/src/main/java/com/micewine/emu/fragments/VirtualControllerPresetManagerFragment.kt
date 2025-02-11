package com.micewine.emu.fragments

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.os.Bundle
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
import com.micewine.emu.activities.ControllerMapperActivity.Companion.SELECTED_VIRTUAL_CONTROLLER_PRESET_KEY
import com.micewine.emu.adapters.AdapterPreset
import com.micewine.emu.adapters.AdapterPreset.Companion.VIRTUAL_CONTROLLER
import com.micewine.emu.adapters.AdapterPreset.Companion.selectedPresetId
import com.micewine.emu.fragments.ControllerPresetManagerFragment.Companion.ACTION_ADD_CONTROLLER_PRESET
import com.micewine.emu.fragments.ControllerPresetManagerFragment.Companion.ACTION_DELETE_CONTROLLER_PRESET
import com.micewine.emu.fragments.ControllerPresetManagerFragment.Companion.ACTION_EDIT_CONTROLLER_PRESET
import com.micewine.emu.views.OverlayView

class VirtualControllerPresetManagerFragment : Fragment() {
    private var rootView: View? = null
    private var recyclerView: RecyclerView? = null
    private val receiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                ACTION_DELETE_CONTROLLER_PRESET -> {
                    val index = intent.getIntExtra("index", -1)

                    recyclerView?.adapter?.notifyItemRemoved(index)

                    if (index == selectedPresetId) {
                        preferences?.edit {
                            putString(SELECTED_VIRTUAL_CONTROLLER_PRESET_KEY, presetListNames.first().titleSettings)
                            apply()
                        }
                        recyclerView?.adapter?.notifyItemChanged(0)
                    }
                }
                ACTION_ADD_CONTROLLER_PRESET -> {
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
                addAction(ACTION_EDIT_CONTROLLER_PRESET)
                addAction(ACTION_ADD_CONTROLLER_PRESET)
                addAction(ACTION_DELETE_CONTROLLER_PRESET)
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

        presetList = getVirtualControllerPresets()
        presetList?.forEach {
            addToAdapter(it.name, VIRTUAL_CONTROLLER, true)
        }
    }

    private fun addToAdapter(titleSettings: String, type: Int, userPreset: Boolean) {
        presetListNames.add(AdapterPreset.Item(titleSettings, type, userPreset))
    }

    companion object {
        private val presetListNames: MutableList<AdapterPreset.Item> = mutableListOf()
        private var presetList: MutableList<VirtualControllerPreset>? = null
        private var preferences: SharedPreferences? = null

        private val gson = Gson()

        fun initialize(context: Context) {
            preferences = PreferenceManager.getDefaultSharedPreferences(context)
            presetList = getVirtualControllerPresets()
        }

        fun getMapping(name: String): VirtualControllerPreset? {
            val index = presetList?.indexOfFirst { it.name == name } ?: return null

            if (index == -1) return null

            return presetList!![index]
        }

        fun putMapping(name: String, buttonList: MutableList<OverlayView.VirtualButton>, analogList: MutableList<OverlayView.VirtualAnalog>) {
            val index = presetList?.indexOfFirst { it.name == name } ?: return

            if (index == -1) return

            presetList!![index].buttons.clear()
            presetList!![index].analogs.clear()

            buttonList.forEach {
                presetList!![index].buttons.add(it)
            }
            analogList.forEach {
                presetList!![index].analogs.add(it)
            }

            saveControllerPresets()
        }

        fun addVirtualControllerPreset(context: Context, name: String) {
            if (presetListNames.firstOrNull { it.titleSettings == name } != null) {
                Toast.makeText(context, context.getString(R.string.executable_already_added), Toast.LENGTH_LONG).show()
                return
            }

            val defaultPreset = VirtualControllerPreset(name, mutableListOf(), mutableListOf())

            presetList?.add(defaultPreset)
            presetListNames.add(
                AdapterPreset.Item(name, VIRTUAL_CONTROLLER, true)
            )

            context.sendBroadcast(
                Intent(ACTION_ADD_CONTROLLER_PRESET)
            )

            saveControllerPresets()
        }

        fun deleteVirtualControllerPreset(context: Context, name: String) {
            val index = presetList?.indexOfFirst { it.name == name }

            presetList?.removeAt(index!!)
            presetListNames.removeAt(index!!)

            val intent = Intent(ACTION_DELETE_CONTROLLER_PRESET).apply {
                putExtra("index", index)
            }

            context.sendBroadcast(intent)

            saveControllerPresets()
        }

        private fun saveControllerPresets() {
            preferences?.edit {
                putString("virtualControllerPresetList", gson.toJson(presetList))
                apply()
            }
        }

        fun getVirtualControllerPresets(): MutableList<VirtualControllerPreset> {
            val json = preferences?.getString("virtualControllerPresetList", "")
            val listType = object : TypeToken<MutableList<VirtualControllerPreset>>() {}.type

            return gson.fromJson(json, listType) ?: mutableListOf(
                VirtualControllerPreset("default", mutableListOf(), mutableListOf())
            )
        }

        data class VirtualControllerPreset(
            var name: String,
            var analogs: MutableList<OverlayView.VirtualAnalog>,
            var buttons: MutableList<OverlayView.VirtualButton>
        )
    }
}
