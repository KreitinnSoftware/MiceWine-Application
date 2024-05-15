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

class SettingsFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_settings, container, false)
        val recyclerView = rootView.findViewById<RecyclerView>(R.id.recyclerViewSettings)
        setAdapter(recyclerView)

        return rootView
    }

    private fun setAdapter(recyclerView: RecyclerView) {
        val settingsList: MutableList<SettingsList> = ArrayList()
        val adapterSettings = AdapterSettings(settingsList, requireContext())
        recyclerView.setAdapter(adapterSettings)
        var person: AdapterSettings.SettingsList?

        person = SettingsList(R.string.settings_title, R.string.settings_description, R.drawable.ic_settings_outline)
        settingsList.add(person)

        person = SettingsList(R.string.log_title, R.string.log_description, R.drawable.ic_log)
        settingsList.add(person)

        person = SettingsList(R.string.about_title, R.string.about_description, R.drawable.ic_info_outline)
        settingsList.add(person)
    }
}
