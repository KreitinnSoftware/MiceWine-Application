package com.micewine.emu.fragments

import androidx.fragment.app.Fragment
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.micewine.emu.R
import com.micewine.emu.activities.GeneralSettings.Companion.DISPLAY_RESOLUTION_KEY
import com.micewine.emu.activities.GeneralSettings.Companion.SPINNER
import com.micewine.emu.activities.GeneralSettings.Companion.SWITCH
import com.micewine.emu.activities.MainActivity.Companion.RAM_COUNTER_KEY
import com.micewine.emu.adapters.AdapterSettingsPreferences
import com.micewine.emu.adapters.AdapterSettingsPreferences.SettingsListSpinner

class DisplaySettingsFragment : Fragment() {
    private val settingsList: MutableList<SettingsListSpinner> = ArrayList()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_settings_model, container, false)
        val recyclerView = rootView.findViewById<RecyclerView>(R.id.recyclerViewSettingsModel)
        setAdapter(recyclerView)

        val layoutManager = recyclerView.layoutManager as GridLayoutManager
        layoutManager.spanCount = 1

        return rootView
    }

    private fun setAdapter(recyclerView: RecyclerView) {
        val adapterSettingsPreferences = AdapterSettingsPreferences(settingsList, requireContext())
        recyclerView.setAdapter(adapterSettingsPreferences)

        settingsList.clear()

        addToAdapter(R.string.display_resolution_title, R.string.display_resolution_description, arrayOf(
            "640x480", "800x600",
            "960x540", "960x540",
            "1024x768", "1280x720",
            "1440x720", "1600x900",
            "1800x900", "1080x1920",
            "2160x1080"
            ),
            SPINNER, "1280x720", DISPLAY_RESOLUTION_KEY)

        addToAdapter(R.string.enableRamCounter, R.string.enableRamCounterDescription, arrayOf(""),
            SWITCH, "false", RAM_COUNTER_KEY)
    }

    private fun addToAdapter(titleId: Int, descriptionId: Int, valuesArray: Array<String>, type: Int, defaultValue: String, keyId: String) {
        settingsList.add(SettingsListSpinner(titleId, descriptionId, valuesArray, type, defaultValue, keyId))
    }
}
