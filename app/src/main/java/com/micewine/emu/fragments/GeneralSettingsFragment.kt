package com.micewine.emu.fragments

import androidx.fragment.app.Fragment
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.micewine.emu.R
import com.micewine.emu.adapters.AdapterSettings
import com.micewine.emu.adapters.AdapterSettings.SettingsList

class GeneralSettingsFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_general_settings, container, false)
        val recyclerView = rootView.findViewById<RecyclerView>(R.id.recyclerViewGeneralSettings)
        setAdapter(recyclerView)

        return rootView
    }

    private fun setAdapter(recyclerView: RecyclerView) {
        val settingsList: MutableList<SettingsList> = ArrayList()
        val adapterSettings = AdapterSettings(settingsList, requireContext())
        recyclerView.setAdapter(adapterSettings)
        var person: SettingsList?

        person = SettingsList(R.string.box64_settings_title, R.string.box64_settings_description, R.drawable.ic_box64_white)
        settingsList.add(person)

        person = SettingsList(R.string.wine_settings_title, R.string.wine_settings_description, R.drawable.ic_wine_white)
        settingsList.add(person)

        person = SettingsList(R.string.display_settings_title, R.string.display_settings_description, R.drawable.ic_display_white)
        settingsList.add(person)

        person = SettingsList(R.string.driver_settings_title, R.string.driver_settings_description, R.drawable.ic_gpu_white)
        settingsList.add(person)
    }
}
