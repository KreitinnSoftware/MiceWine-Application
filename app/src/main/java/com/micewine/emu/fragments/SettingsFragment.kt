package com.micewine.emu.fragments

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.micewine.emu.R
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

        addToAdapter(R.string.settings_title, R.string.settings_description,
            R.drawable.setting_icon_242766
        )
        addToAdapter(R.string.controller_mapper_title, R.string.controller_mapper_description, R.drawable.ic_joystick)
        addToAdapter(R.string.virtual_controller_mapper_title, R.string.controller_virtual_mapper_description, R.drawable.ic_joystick)

        if (Build.SUPPORTED_ABIS[0] != "x86_64") {
            addToAdapter(R.string.box64_preset_manager_title, R.string.box64_preset_manager_description, R.drawable.ic_box64)
            addToAdapter(R.string.box64_manager_title, R.string.box64_manager_description, R.drawable.ic_box64)
        }

        addToAdapter(R.string.driver_manager_title, R.string.driver_manager_description,
            R.drawable.gpu
        )
        addToAdapter(R.string.wine_manager_title, R.string.wine_manager_description, R.drawable.ic_wine)
    }

    private fun addToAdapter(titleId: Int, descriptionId: Int, icon: Int) {
        settingsList.add(SettingsList(context?.getString(titleId)!!, context?.getString(descriptionId)!!, icon))
    }
}
