package com.micewine.emu.fragments

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

        addToAdapter(R.string.settings_title, R.string.settings_description, R.drawable.ic_settings_outline)
        addToAdapter(R.string.controller_mapper_title, R.string.controller_mapper_description, R.drawable.ic_joystick)
        addToAdapter(R.string.virtual_controller_mapper_title, R.string.controller_virtual_mapper_description, R.drawable.ic_joystick)
        addToAdapter(R.string.driver_manager_title, R.string.driver_manager_description, R.drawable.ic_gpu)
    }

    private fun addToAdapter(titleId: Int, descriptionId: Int, icon: Int) {
        settingsList.add(SettingsList(context?.getString(titleId)!!, context?.getString(descriptionId)!!, icon))
    }
}
