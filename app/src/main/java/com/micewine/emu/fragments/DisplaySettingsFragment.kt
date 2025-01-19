package com.micewine.emu.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.micewine.emu.R
import com.micewine.emu.activities.GeneralSettings.Companion.DISPLAY_RESOLUTION
import com.micewine.emu.activities.GeneralSettings.Companion.SPINNER
import com.micewine.emu.activities.GeneralSettings.Companion.SWITCH
import com.micewine.emu.activities.MainActivity.Companion.CPU_COUNTER_KEY
import com.micewine.emu.activities.MainActivity.Companion.ENABLE_DEBUG_INFO_KEY
import com.micewine.emu.activities.MainActivity.Companion.RAM_COUNTER_KEY
import com.micewine.emu.adapters.AdapterSettingsPreferences
import com.micewine.emu.adapters.AdapterSettingsPreferences.SettingsListSpinner

class DisplaySettingsFragment : Fragment() {
    private val settingsList: MutableList<SettingsListSpinner> = ArrayList()
    private var rootView: View? = null
    private var recyclerView: RecyclerView? = null
    private var layoutManager: GridLayoutManager? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        rootView = inflater.inflate(R.layout.fragment_settings_model, container, false)
        recyclerView = rootView?.findViewById(R.id.recyclerViewSettingsModel)

        layoutManager = recyclerView?.layoutManager as GridLayoutManager?
        layoutManager?.spanCount = 1

        setAdapter()

        return rootView
    }

    private fun setAdapter() {
        recyclerView?.setAdapter(AdapterSettingsPreferences(settingsList, requireActivity()))

        settingsList.clear()

        addToAdapter(R.string.display_resolution_title, R.string.display_resolution_description, arrayOf(
            "640x480", "800x600",
            "960x540", "1024x768",
            "1280x720", "1440x720",
            "1600x900", "1800x900",
            "1920x1080", "2160x1080"
            ),
            SPINNER, "1280x720", DISPLAY_RESOLUTION)
        addToAdapter(R.string.enable_ram_counter, R.string.enable_ram_counter_description, null,
            SWITCH, "true", RAM_COUNTER_KEY)
        addToAdapter(R.string.enable_cpu_counter, R.string.enable_cpu_counter_description, null,
            SWITCH, "false", CPU_COUNTER_KEY)
        addToAdapter(R.string.enable_debug_info, R.string.enable_debug_info_description, null,
            SWITCH, "true", ENABLE_DEBUG_INFO_KEY)
    }

    private fun addToAdapter(titleId: Int, descriptionId: Int, valuesArray: Array<String>?, type: Int, defaultValue: String, keyId: String) {
        settingsList.add(SettingsListSpinner(titleId, descriptionId, valuesArray, type, defaultValue, keyId))
    }
}
