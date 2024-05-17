package com.micewine.emu.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.micewine.emu.R
import com.micewine.emu.adapters.AdapterSettingsBoolean
import com.micewine.emu.adapters.AdapterSettingsBoolean.SettingsListBoolean
import com.micewine.emu.adapters.AdapterSettingsSpinner
import com.micewine.emu.adapters.AdapterSettingsSpinner.SettingsListWithOption

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
        val settingsBooleanList: MutableList<SettingsListBoolean> = ArrayList()



        val adapterSettingsBoolean = AdapterSettingsBoolean(settingsBooleanList, requireContext())
        recyclerView.setAdapter(adapterSettingsBoolean)

        var person: SettingsListWithOption?
        val personBoolean: SettingsListBoolean?


        personBoolean = SettingsListBoolean(R.string.box64_strongmem_title, R.string.box64_strongmem_description)
        settingsBooleanList.add(personBoolean)
    }
}
