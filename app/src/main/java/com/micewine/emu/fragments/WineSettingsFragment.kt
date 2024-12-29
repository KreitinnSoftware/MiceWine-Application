package com.micewine.emu.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.micewine.emu.R
import com.micewine.emu.activities.GeneralSettings.Companion.ENABLE_SERVICES
import com.micewine.emu.activities.GeneralSettings.Companion.ENABLE_SERVICES_DEFAULT_VALUE
import com.micewine.emu.activities.GeneralSettings.Companion.SPINNER
import com.micewine.emu.activities.GeneralSettings.Companion.SWITCH
import com.micewine.emu.activities.GeneralSettings.Companion.WINE_ESYNC
import com.micewine.emu.activities.GeneralSettings.Companion.WINE_ESYNC_DEFAULT_VALUE
import com.micewine.emu.activities.GeneralSettings.Companion.WINE_LOG_LEVEL
import com.micewine.emu.activities.GeneralSettings.Companion.WINE_LOG_LEVEL_DEFAULT_VALUE
import com.micewine.emu.adapters.AdapterSettingsPreferences
import com.micewine.emu.adapters.AdapterSettingsPreferences.SettingsListSpinner

class WineSettingsFragment : Fragment() {
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

        addToAdapter(R.string.wine_esync_title, R.string.null_description, null, SWITCH, WINE_ESYNC_DEFAULT_VALUE, WINE_ESYNC)
        addToAdapter(R.string.enable_wine_services_title, R.string.null_description, null, SWITCH, ENABLE_SERVICES_DEFAULT_VALUE, ENABLE_SERVICES)
        addToAdapter(R.string.wine_log_level_title, R.string.null_description, arrayOf("minimal", "default"), SPINNER, WINE_LOG_LEVEL_DEFAULT_VALUE, WINE_LOG_LEVEL)
    }

    private fun addToAdapter(titleId: Int, descriptionId: Int, valuesArray: Array<String>?, type: Int, defaultValue: Any, keyId: String) {
        settingsList.add(SettingsListSpinner(titleId, descriptionId, valuesArray, type, "$defaultValue", keyId))
    }
}
