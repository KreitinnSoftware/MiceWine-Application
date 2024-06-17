package com.micewine.emu.fragments

import androidx.fragment.app.Fragment
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.micewine.emu.R
import com.micewine.emu.activities.GeneralSettings.Companion.SELECTED_D3DX_RENDERER_KEY
import com.micewine.emu.activities.GeneralSettings.Companion.SELECTED_DRIVER_KEY
import com.micewine.emu.activities.GeneralSettings.Companion.SELECTED_DXVK_HUD_PRESET_KEY
import com.micewine.emu.activities.GeneralSettings.Companion.SELECTED_DXVK_KEY
import com.micewine.emu.activities.GeneralSettings.Companion.SELECTED_VIRGL_PROFILE_KEY
import com.micewine.emu.activities.GeneralSettings.Companion.SELECTED_WINED3D_KEY
import com.micewine.emu.activities.GeneralSettings.Companion.SPINNER
import com.micewine.emu.adapters.AdapterSettingsPreferences
import com.micewine.emu.adapters.AdapterSettingsPreferences.SettingsListSpinner

class DriversSettingsFragment : Fragment() {
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

        addToAdapter(R.string.select_driver_title, R.string.null_description, arrayOf(
            "Turnip/Zink", "Android/Zink",
            "VirGL"),
            SPINNER, "Turnip/Zink", SELECTED_DRIVER_KEY)

        addToAdapter(R.string.select_d3dx_title, R.string.null_description, arrayOf(
            "DXVK", "WineD3D"),
            SPINNER, "DXVK", SELECTED_D3DX_RENDERER_KEY)

        addToAdapter(R.string.select_dxvk_title, R.string.null_description, arrayOf(
            "DXVK-1.10.3-async", "DXVK-2.0-async",
            "DXVK-2.1", "DXVK-2.2", "DXVK-2.3",
            "DXVK-2.3.1", "DXVK-DEV-571948c",
            "DXVK-DEV-eb80695", "DXVK-GPLASYNC-2.3.1"),
            SPINNER, "DXVK-1.10.3-async", SELECTED_DXVK_KEY)

        addToAdapter(R.string.select_dxvk_hud_preset_title, R.string.null_description, arrayOf(
            "Off", "FPS", "GPU Load", "FPS/GPU Load", "FPS/GPU Load/Dev Info"),
            SPINNER, "FPS/GPU Load", SELECTED_DXVK_HUD_PRESET_KEY)

        addToAdapter(R.string.select_wined3d_title, R.string.null_description, arrayOf(
            "WineD3D-3.17", "WineD3D-7.18",
            "WineD3D-8.2", "WineD3D-9.0"),
            SPINNER, "WineD3D-9.0", SELECTED_WINED3D_KEY)

        addToAdapter(R.string.select_virgl_profile_title, R.string.null_description, arrayOf(
            "GL 2.1", "GL 3.3"),
            SPINNER, "GL 3.3", SELECTED_VIRGL_PROFILE_KEY)
    }

    private fun addToAdapter(titleId: Int, descriptionId: Int, valuesArray: Array<String>, type: Int, defaultValue: String, keyId: String) {
        settingsList.add(SettingsListSpinner(titleId, descriptionId, valuesArray, type, defaultValue, keyId))
    }
}
