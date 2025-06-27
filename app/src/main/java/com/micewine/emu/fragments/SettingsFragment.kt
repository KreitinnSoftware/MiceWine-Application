package com.micewine.emu.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.micewine.emu.R
import com.micewine.emu.activities.MainActivity.Companion.deviceArch
import com.micewine.emu.adapters.AdapterSettings
import com.micewine.emu.adapters.AdapterSettings.SettingsList

class SettingsFragment : Fragment() {
    private val settingsList: MutableList<SettingsList> = ArrayList()
    private var rootView: View? = null
    private var recyclerView: RecyclerView? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        rootView = inflater.inflate(R.layout.fragment_settings, container, false)
        recyclerView = rootView?.findViewById(R.id.recyclerViewSettings)

        setAdapter()

        return rootView
    }

    private fun setAdapter() {
        recyclerView?.setAdapter(AdapterSettings(settingsList, requireContext()))

        settingsList.clear()

        addToAdapter(R.string.general_settings, R.string.settings_desc, R.drawable.ic_settings_outline)
        addToAdapter(R.string.controller_mapper_title, R.string.controller_mapper_desc, R.drawable.ic_joystick)
        addToAdapter(R.string.virtual_controller_mapper_title, R.string.controller_virtual_mapper_desc, R.drawable.ic_joystick)

        if (deviceArch != "x86_64") {
            addToAdapter(R.string.box64_preset_manager_title, R.string.box64_preset_manager_desc, R.drawable.ic_box64)
        }

        addToAdapter(R.string.wine_prefix_manager_title, R.string.wine_prefix_manager_desc, R.drawable.ic_wine)
        addToAdapter(R.string.rat_manager_title, R.string.rat_manager_desc, R.drawable.ic_rat_package_grayscale)
        addToAdapter(R.string.rat_downloader_title, R.string.rat_downloader_desc, R.drawable.ic_download)
        addToAdapter(R.string.controller_view_title, R.string.controller_view_desc, R.drawable.ic_joystick)
    }

    private fun addToAdapter(titleId: Int, descriptionId: Int, icon: Int) {
        settingsList.add(
            SettingsList(getString(titleId), getString(descriptionId), icon)
        )
    }
}
