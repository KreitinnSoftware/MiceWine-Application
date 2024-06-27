package com.micewine.emu.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.micewine.emu.R
import com.micewine.emu.activities.GeneralSettings.Companion.BOX64_DYNAREC_ALIGNED_ATOMICS_KEY
import com.micewine.emu.activities.GeneralSettings.Companion.BOX64_DYNAREC_BIGBLOCK_KEY
import com.micewine.emu.activities.GeneralSettings.Companion.BOX64_DYNAREC_BLEEDING_EDGE_KEY
import com.micewine.emu.activities.GeneralSettings.Companion.BOX64_DYNAREC_CALLRET_KEY
import com.micewine.emu.activities.GeneralSettings.Companion.BOX64_DYNAREC_FASTNAN_KEY
import com.micewine.emu.activities.GeneralSettings.Companion.BOX64_DYNAREC_FASTROUND_KEY
import com.micewine.emu.activities.GeneralSettings.Companion.BOX64_DYNAREC_SAFEFLAGS_KEY
import com.micewine.emu.activities.GeneralSettings.Companion.BOX64_DYNAREC_STRONGMEM_KEY
import com.micewine.emu.activities.GeneralSettings.Companion.BOX64_DYNAREC_WAIT_KEY
import com.micewine.emu.activities.GeneralSettings.Companion.BOX64_DYNAREC_X87DOUBLE_KEY
import com.micewine.emu.activities.GeneralSettings.Companion.SPINNER
import com.micewine.emu.activities.GeneralSettings.Companion.SWITCH
import com.micewine.emu.adapters.AdapterSettingsPreferences
import com.micewine.emu.adapters.AdapterSettingsPreferences.SettingsListSpinner

class Box64SettingsFragment : Fragment() {
    private val settingsList: MutableList<SettingsListSpinner> = ArrayList()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_settings_model, container, false)
        val recyclerView = rootView.findViewById<RecyclerView>(R.id.recyclerViewSettingsModel)
        setAdapter(recyclerView)

        val layoutManager = recyclerView.layoutManager as GridLayoutManager
        layoutManager.spanCount = 1

        return rootView
    }

    private fun setAdapter(recyclerView: RecyclerView) {
        val adapterSettingsPreferences = AdapterSettingsPreferences(settingsList, requireContext())
        recyclerView.setAdapter(adapterSettingsPreferences)

        settingsList.clear()

        addToAdapter(R.string.box64_bigblock_title, R.string.box64_bigblock_description, arrayOf("0", "1", "2", "3"), SPINNER, "1", BOX64_DYNAREC_BIGBLOCK_KEY)

        addToAdapter(R.string.box64_strongmem_title, R.string.box64_strongmem_description, arrayOf("0", "1", "2", "3"), SPINNER, "0", BOX64_DYNAREC_STRONGMEM_KEY)

        addToAdapter(R.string.box64_x87double_title, R.string.box64_x87double_description, arrayOf(""), SWITCH, "false", BOX64_DYNAREC_X87DOUBLE_KEY)

        addToAdapter(R.string.box64_fastnan_title, R.string.box64_fastnan_description, arrayOf(""), SWITCH, "true", BOX64_DYNAREC_FASTNAN_KEY)

        addToAdapter(R.string.box64_fastround_title, R.string.box64_fastround_description, arrayOf(""), SWITCH, "true", BOX64_DYNAREC_FASTROUND_KEY)

        addToAdapter(R.string.box64_safeflags_title, R.string.box64_safeflags_description, arrayOf("0", "1", "2"), SPINNER, "1", BOX64_DYNAREC_SAFEFLAGS_KEY)

        addToAdapter(R.string.box64_callret_title, R.string.box64_callret_description, arrayOf(""), SWITCH, "true", BOX64_DYNAREC_CALLRET_KEY)

        addToAdapter(R.string.box64_aligned_atomics_title, R.string.box64_aligned_atomics_description, arrayOf(""), SWITCH, "false", BOX64_DYNAREC_ALIGNED_ATOMICS_KEY)

        addToAdapter(R.string.box64_bleeding_edge_title, R.string.box64_bleeding_edge_description, arrayOf(""), SWITCH, "true", BOX64_DYNAREC_BLEEDING_EDGE_KEY)

        addToAdapter(R.string.box64_dynarec_wait_title, R.string.box64_dynarec_wait_description, arrayOf(""), SWITCH, "true", BOX64_DYNAREC_WAIT_KEY)
    }

    private fun addToAdapter(titleId: Int, descriptionId: Int, valuesArray: Array<String>, type: Int, defaultValue: String, keyId: String) {
        settingsList.add(SettingsListSpinner(titleId, descriptionId, valuesArray, type, defaultValue, keyId))
    }
}
