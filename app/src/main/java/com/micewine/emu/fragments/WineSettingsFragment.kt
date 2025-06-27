package com.micewine.emu.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.micewine.emu.R
import com.micewine.emu.activities.GeneralSettingsActivity.Companion.PA_SINK
import com.micewine.emu.activities.GeneralSettingsActivity.Companion.PA_SINK_DEFAULT_VALUE
import com.micewine.emu.activities.GeneralSettingsActivity.Companion.SEEKBAR
import com.micewine.emu.activities.GeneralSettingsActivity.Companion.SPINNER
import com.micewine.emu.activities.GeneralSettingsActivity.Companion.WINE_DPI
import com.micewine.emu.activities.GeneralSettingsActivity.Companion.WINE_DPI_DEFAULT_VALUE
import com.micewine.emu.activities.MainActivity.Companion.paSink
import com.micewine.emu.activities.MainActivity.Companion.usrDir
import com.micewine.emu.adapters.AdapterSettingsPreferences
import com.micewine.emu.adapters.AdapterSettingsPreferences.SettingsListSpinner
import java.io.File

class WineSettingsFragment : Fragment() {
    private val settingsList: MutableList<SettingsListSpinner> = ArrayList()
    private var rootView: View? = null
    private var recyclerView: RecyclerView? = null
    private var layoutManager: GridLayoutManager? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        rootView = inflater.inflate(R.layout.fragment_settings_model, container, false)
        recyclerView = rootView?.findViewById(R.id.recyclerViewSettingsModel)

        layoutManager = recyclerView?.layoutManager as GridLayoutManager?
        layoutManager?.spanCount = 1

        setAdapter()

        return rootView
    }

    private fun setAdapter() {
        recyclerView?.setAdapter(AdapterSettingsPreferences(settingsList, requireActivity()))

        settingsList.clear()

        addToAdapter(R.string.wine_dpi, R.string.null_desc, arrayOf(96, 480), SEEKBAR, WINE_DPI_DEFAULT_VALUE, WINE_DPI)
    }

    private fun addToAdapter(titleId: Int, descriptionId: Int, seekBarMaxMinValues: Array<Int>, type: Int, defaultValue: Any, keyId: String) {
        settingsList.add(SettingsListSpinner(titleId, descriptionId, null, seekBarMaxMinValues, type, "$defaultValue", keyId))
    }
}
