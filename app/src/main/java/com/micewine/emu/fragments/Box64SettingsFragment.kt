package com.micewine.emu.fragments

import androidx.fragment.app.Fragment
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.recyclerview.widget.RecyclerView
import com.micewine.emu.R
import com.micewine.emu.adapters.AdapterSettingsSpinner
import com.micewine.emu.adapters.AdapterSettingsSpinner.SettingsListSpinner

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
        val adapterSettingsSpinner = AdapterSettingsSpinner(settingsList, requireContext())
        recyclerView.setAdapter(adapterSettingsSpinner)
        var person: SettingsListSpinner?

        person = SettingsListSpinner(R.string.box64_bigblock_title, R.string.box64_bigblock_description, arrayOf("0", "1", "2", "3"))
        settingsList.add(person)

        person = SettingsListSpinner(R.string.box64_strongmem_title, R.string.box64_strongmem_description, arrayOf("0", "1", "2", "3"))
        settingsList.add(person)
    }
}
