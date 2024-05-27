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
import com.micewine.emu.models.GameList

class SettingsFragment : Fragment() {
    private val settingsList: MutableList<SettingsList> = ArrayList()

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
        val adapterSettings = AdapterSettings(settingsList, requireContext())
        recyclerView.setAdapter(adapterSettings)

        settingsList.clear()

        addToAdapter(R.string.settingsTitle, R.string.settings_description, R.drawable.ic_settings_outline)

        addToAdapter(R.string.logTitle, R.string.log_description, R.drawable.ic_log)

        addToAdapter(R.string.aboutTitle, R.string.about_description, R.drawable.ic_info_outline)

        addToAdapter(R.string.controllerMapperTitle, R.string.controllerMapperDescription, R.drawable.ic_info_outline)
    }

    private fun addToAdapter(titleId: Int, descriptionId: Int, icon: Int) {
        settingsList.add(SettingsList(titleId, descriptionId, icon))
    }
}
