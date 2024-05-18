package com.micewine.emu.fragments

import androidx.fragment.app.Fragment
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.micewine.emu.R
import com.micewine.emu.activities.GeneralSettings.Companion.SPINNER
import com.micewine.emu.activities.GeneralSettings.Companion.SWITCH
import com.micewine.emu.adapters.AdapterSettingsPreferences
import com.micewine.emu.adapters.AdapterSettingsPreferences.SettingsListSpinner

class Box64SettingsFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_box64_settings, container, false)
        val recyclerView = rootView.findViewById<RecyclerView>(R.id.recyclerViewBox64Settings)
        setAdapter(recyclerView)

        return rootView
    }

    private fun setAdapter(recyclerView: RecyclerView) {
        val settingsList: MutableList<SettingsListSpinner> = ArrayList()
        val adapterSettingsPreferences = AdapterSettingsPreferences(settingsList, requireContext())
        recyclerView.setAdapter(adapterSettingsPreferences)
        var person: SettingsListSpinner?

        person = SettingsListSpinner(R.string.box64_bigblock_title, R.string.box64_bigblock_description, arrayOf("0", "1", "2", "3"), SPINNER, "1")
        settingsList.add(person)

        person = SettingsListSpinner(R.string.box64_strongmem_title, R.string.box64_strongmem_description, arrayOf("0", "1", "2", "3"), SPINNER, "0")
        settingsList.add(person)

        person = SettingsListSpinner(R.string.box64_x87double_title, R.string.box64_x87double_description, arrayOf(""), SWITCH, "false")
        settingsList.add(person)

        person = SettingsListSpinner(R.string.box64_fastnan_title, R.string.box64_fastnan_description, arrayOf(""), SWITCH, "true")
        settingsList.add(person)

        person = SettingsListSpinner(R.string.box64_fastround_title, R.string.box64_fastround_description, arrayOf(""), SWITCH, "true")
        settingsList.add(person)

        person = SettingsListSpinner(R.string.box64_safeflags_title, R.string.box64_safeflags_description, arrayOf("0", "1", "2"), SPINNER, "1")
        settingsList.add(person)

        person = SettingsListSpinner(R.string.box64_callret_title, R.string.box64_callret_description, arrayOf(""), SWITCH, "true")
        settingsList.add(person)
    }
}
