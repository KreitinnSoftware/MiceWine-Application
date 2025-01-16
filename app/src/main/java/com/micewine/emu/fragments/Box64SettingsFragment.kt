package com.micewine.emu.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.micewine.emu.R
import com.micewine.emu.activities.GeneralSettings.Companion.BOX64_AVX
import com.micewine.emu.activities.GeneralSettings.Companion.BOX64_AVX_DEFAULT_VALUE
import com.micewine.emu.activities.GeneralSettings.Companion.BOX64_DYNAREC_ALIGNED_ATOMICS
import com.micewine.emu.activities.GeneralSettings.Companion.BOX64_DYNAREC_ALIGNED_ATOMICS_DEFAULT_VALUE
import com.micewine.emu.activities.GeneralSettings.Companion.BOX64_DYNAREC_BIGBLOCK
import com.micewine.emu.activities.GeneralSettings.Companion.BOX64_DYNAREC_BIGBLOCK_DEFAULT_VALUE
import com.micewine.emu.activities.GeneralSettings.Companion.BOX64_DYNAREC_BLEEDING_EDGE
import com.micewine.emu.activities.GeneralSettings.Companion.BOX64_DYNAREC_BLEEDING_EDGE_DEFAULT_VALUE
import com.micewine.emu.activities.GeneralSettings.Companion.BOX64_DYNAREC_CALLRET
import com.micewine.emu.activities.GeneralSettings.Companion.BOX64_DYNAREC_CALLRET_DEFAULT_VALUE
import com.micewine.emu.activities.GeneralSettings.Companion.BOX64_DYNAREC_DIRTY
import com.micewine.emu.activities.GeneralSettings.Companion.BOX64_DYNAREC_DIRTY_DEFAULT_VALUE
import com.micewine.emu.activities.GeneralSettings.Companion.BOX64_DYNAREC_FASTNAN
import com.micewine.emu.activities.GeneralSettings.Companion.BOX64_DYNAREC_FASTNAN_DEFAULT_VALUE
import com.micewine.emu.activities.GeneralSettings.Companion.BOX64_DYNAREC_FASTROUND
import com.micewine.emu.activities.GeneralSettings.Companion.BOX64_DYNAREC_FASTROUND_DEFAULT_VALUE
import com.micewine.emu.activities.GeneralSettings.Companion.BOX64_DYNAREC_NATIVEFLAGS
import com.micewine.emu.activities.GeneralSettings.Companion.BOX64_DYNAREC_NATIVEFLAGS_DEFAULT_VALUE
import com.micewine.emu.activities.GeneralSettings.Companion.BOX64_DYNAREC_PAUSE
import com.micewine.emu.activities.GeneralSettings.Companion.BOX64_DYNAREC_PAUSE_DEFAULT_VALUE
import com.micewine.emu.activities.GeneralSettings.Companion.BOX64_DYNAREC_SAFEFLAGS
import com.micewine.emu.activities.GeneralSettings.Companion.BOX64_DYNAREC_SAFEFLAGS_DEFAULT_VALUE
import com.micewine.emu.activities.GeneralSettings.Companion.BOX64_DYNAREC_STRONGMEM
import com.micewine.emu.activities.GeneralSettings.Companion.BOX64_DYNAREC_STRONGMEM_DEFAULT_VALUE
import com.micewine.emu.activities.GeneralSettings.Companion.BOX64_DYNAREC_WAIT
import com.micewine.emu.activities.GeneralSettings.Companion.BOX64_DYNAREC_WAIT_DEFAULT_VALUE
import com.micewine.emu.activities.GeneralSettings.Companion.BOX64_DYNAREC_WEAKBARRIER
import com.micewine.emu.activities.GeneralSettings.Companion.BOX64_DYNAREC_WEAKBARRIER_DEFAULT_VALUE
import com.micewine.emu.activities.GeneralSettings.Companion.BOX64_DYNAREC_X87DOUBLE
import com.micewine.emu.activities.GeneralSettings.Companion.BOX64_DYNAREC_X87DOUBLE_DEFAULT_VALUE
import com.micewine.emu.activities.GeneralSettings.Companion.BOX64_LOG
import com.micewine.emu.activities.GeneralSettings.Companion.BOX64_LOG_DEFAULT_VALUE
import com.micewine.emu.activities.GeneralSettings.Companion.BOX64_NOSIGILL
import com.micewine.emu.activities.GeneralSettings.Companion.BOX64_NOSIGILL_DEFAULT_VALUE
import com.micewine.emu.activities.GeneralSettings.Companion.BOX64_NOSIGSEGV
import com.micewine.emu.activities.GeneralSettings.Companion.BOX64_NOSIGSEGV_DEFAULT_VALUE
import com.micewine.emu.activities.GeneralSettings.Companion.BOX64_SHOWBT
import com.micewine.emu.activities.GeneralSettings.Companion.BOX64_SHOWBT_DEFAULT_VALUE
import com.micewine.emu.activities.GeneralSettings.Companion.BOX64_SHOWSEGV
import com.micewine.emu.activities.GeneralSettings.Companion.BOX64_SHOWSEGV_DEFAULT_VALUE
import com.micewine.emu.activities.GeneralSettings.Companion.SPINNER
import com.micewine.emu.activities.GeneralSettings.Companion.SWITCH
import com.micewine.emu.adapters.AdapterSettingsPreferences
import com.micewine.emu.adapters.AdapterSettingsPreferences.SettingsListSpinner

class Box64SettingsFragment : Fragment() {
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
        val adapterSettingsPreferences = AdapterSettingsPreferences(settingsList, requireActivity())

        recyclerView?.setAdapter(adapterSettingsPreferences)

        settingsList.clear()

        addToAdapter(R.string.box64_bigblock_title, R.string.box64_bigblock_description, arrayOf("0", "1", "2", "3"), SPINNER, BOX64_DYNAREC_BIGBLOCK_DEFAULT_VALUE, BOX64_DYNAREC_BIGBLOCK)
        addToAdapter(R.string.box64_strongmem_title, R.string.box64_strongmem_description, arrayOf("0", "1", "2", "3"), SPINNER, BOX64_DYNAREC_STRONGMEM_DEFAULT_VALUE, BOX64_DYNAREC_STRONGMEM)
        addToAdapter(R.string.box64_weakbarrier_title, R.string.box64_weakbarrier_description, arrayOf("0", "1", "2"), SPINNER, BOX64_DYNAREC_WEAKBARRIER_DEFAULT_VALUE, BOX64_DYNAREC_WEAKBARRIER)
        addToAdapter(R.string.box64_pause_title, R.string.box64_pause_description, arrayOf("0", "1", "2", "3"), SPINNER, BOX64_DYNAREC_PAUSE_DEFAULT_VALUE, BOX64_DYNAREC_PAUSE)
        addToAdapter(R.string.box64_x87double_title, R.string.box64_x87double_description, null, SWITCH, BOX64_DYNAREC_X87DOUBLE_DEFAULT_VALUE, BOX64_DYNAREC_X87DOUBLE)
        addToAdapter(R.string.box64_fastnan_title, R.string.box64_fastnan_description, null, SWITCH, BOX64_DYNAREC_FASTNAN_DEFAULT_VALUE, BOX64_DYNAREC_FASTNAN)
        addToAdapter(R.string.box64_fastround_title, R.string.box64_fastround_description, null, SWITCH, BOX64_DYNAREC_FASTROUND_DEFAULT_VALUE, BOX64_DYNAREC_FASTROUND)
        addToAdapter(R.string.box64_safeflags_title, R.string.box64_safeflags_description, arrayOf("0", "1", "2"), SPINNER, BOX64_DYNAREC_SAFEFLAGS_DEFAULT_VALUE, BOX64_DYNAREC_SAFEFLAGS)
        addToAdapter(R.string.box64_callret_title, R.string.box64_callret_description, null, SWITCH, BOX64_DYNAREC_CALLRET_DEFAULT_VALUE, BOX64_DYNAREC_CALLRET)
        addToAdapter(R.string.box64_aligned_atomics_title, R.string.box64_aligned_atomics_description, null, SWITCH, BOX64_DYNAREC_ALIGNED_ATOMICS_DEFAULT_VALUE, BOX64_DYNAREC_ALIGNED_ATOMICS)
        addToAdapter(R.string.box64_nativeflags_title, R.string.box64_nativeflags_description, null, SWITCH, BOX64_DYNAREC_NATIVEFLAGS_DEFAULT_VALUE, BOX64_DYNAREC_NATIVEFLAGS)
        addToAdapter(R.string.box64_bleeding_edge_title, R.string.box64_bleeding_edge_description, null, SWITCH, BOX64_DYNAREC_BLEEDING_EDGE_DEFAULT_VALUE, BOX64_DYNAREC_BLEEDING_EDGE)
        addToAdapter(R.string.box64_dynarec_wait_title, R.string.box64_dynarec_wait_description, null, SWITCH, BOX64_DYNAREC_WAIT_DEFAULT_VALUE, BOX64_DYNAREC_WAIT)
        addToAdapter(R.string.box64_dynarec_dirty_title, R.string.box64_dynarec_dirty_description, null, SWITCH, BOX64_DYNAREC_DIRTY_DEFAULT_VALUE, BOX64_DYNAREC_DIRTY)
        addToAdapter(R.string.box64_log_title, R.string.box64_log_description, arrayOf("0", "1"), SPINNER, BOX64_LOG_DEFAULT_VALUE, BOX64_LOG)
        addToAdapter(R.string.box64_avx_title, R.string.box64_avx_description, arrayOf("0", "1", "2"), SPINNER, BOX64_AVX_DEFAULT_VALUE, BOX64_AVX)

        // Debugging Options

        addToAdapter(R.string.box64_show_segv_title, R.string.box64_show_segv_description, null, SWITCH, BOX64_SHOWSEGV_DEFAULT_VALUE, BOX64_SHOWSEGV)
        addToAdapter(R.string.box64_no_sigsegv_title, R.string.box64_no_sigsegv_description, null, SWITCH, BOX64_NOSIGSEGV_DEFAULT_VALUE, BOX64_NOSIGSEGV)
        addToAdapter(R.string.box64_show_bt_title, R.string.box64_show_bt_description, null, SWITCH, BOX64_SHOWBT_DEFAULT_VALUE, BOX64_SHOWBT)
        addToAdapter(R.string.box64_no_sigill_title, R.string.box64_no_sigill_description, null, SWITCH, BOX64_NOSIGILL_DEFAULT_VALUE, BOX64_NOSIGILL)
    }

    private fun addToAdapter(titleId: Int, descriptionId: Int, valuesArray: Array<String>?, type: Int, defaultValue: Any, keyId: String) {
        settingsList.add(SettingsListSpinner(titleId, descriptionId, valuesArray, type, "$defaultValue", keyId))
    }
}
