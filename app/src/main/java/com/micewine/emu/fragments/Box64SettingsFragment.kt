package com.micewine.emu.fragments

import androidx.fragment.app.Fragment
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.recyclerview.widget.RecyclerView
import com.micewine.emu.R
import com.micewine.emu.adapters.AdapterSettingsWithOption
import com.micewine.emu.adapters.AdapterSettingsWithOption.SettingsListWithOption

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
        val settingsList: MutableList<SettingsListWithOption> = ArrayList()
        val adapterSettingsWithOption = AdapterSettingsWithOption(settingsList, requireContext())
        recyclerView.setAdapter(adapterSettingsWithOption)
        var person: SettingsListWithOption?

        person = SettingsListWithOption(R.string.box64_bigblock_title, R.string.box64_bigblock_description, arrayOf("1", "2", "3"))
        settingsList.add(person)
    }
}
