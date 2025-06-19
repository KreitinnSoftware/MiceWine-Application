package com.micewine.emu.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.micewine.emu.R
import com.micewine.emu.activities.GeneralSettingsActivity.Companion.BOX64_AVX
import com.micewine.emu.activities.GeneralSettingsActivity.Companion.BOX64_DYNAREC_ALIGNED_ATOMICS
import com.micewine.emu.activities.GeneralSettingsActivity.Companion.BOX64_DYNAREC_BIGBLOCK
import com.micewine.emu.activities.GeneralSettingsActivity.Companion.BOX64_DYNAREC_CALLRET
import com.micewine.emu.activities.GeneralSettingsActivity.Companion.BOX64_DYNAREC_DF
import com.micewine.emu.activities.GeneralSettingsActivity.Companion.BOX64_DYNAREC_DIRTY
import com.micewine.emu.activities.GeneralSettingsActivity.Companion.BOX64_DYNAREC_FASTNAN
import com.micewine.emu.activities.GeneralSettingsActivity.Companion.BOX64_DYNAREC_FASTROUND
import com.micewine.emu.activities.GeneralSettingsActivity.Companion.BOX64_DYNAREC_FORWARD
import com.micewine.emu.activities.GeneralSettingsActivity.Companion.BOX64_DYNAREC_NATIVEFLAGS
import com.micewine.emu.activities.GeneralSettingsActivity.Companion.BOX64_DYNAREC_PAUSE
import com.micewine.emu.activities.GeneralSettingsActivity.Companion.BOX64_DYNAREC_SAFEFLAGS
import com.micewine.emu.activities.GeneralSettingsActivity.Companion.BOX64_DYNAREC_STRONGMEM
import com.micewine.emu.activities.GeneralSettingsActivity.Companion.BOX64_DYNAREC_WAIT
import com.micewine.emu.activities.GeneralSettingsActivity.Companion.BOX64_DYNAREC_WEAKBARRIER
import com.micewine.emu.activities.GeneralSettingsActivity.Companion.BOX64_DYNAREC_X87DOUBLE
import com.micewine.emu.activities.GeneralSettingsActivity.Companion.BOX64_MMAP32
import com.micewine.emu.activities.GeneralSettingsActivity.Companion.BOX64_SSE42
import com.micewine.emu.activities.GeneralSettingsActivity.Companion.SPINNER
import com.micewine.emu.activities.GeneralSettingsActivity.Companion.SWITCH
import com.micewine.emu.adapters.AdapterSettingsBox64Preset

class Box64SettingsFragment : Fragment() {
    private val settingsList: MutableList<AdapterSettingsBox64Preset.Box64ListSpinner> = ArrayList()
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
        val adapterSettingsPreferences = AdapterSettingsBox64Preset(settingsList, requireActivity())

        recyclerView?.setAdapter(adapterSettingsPreferences)

        settingsList.clear()

        addToAdapter(R.string.box64_bigblock, R.string.box64_bigblock_desc, arrayOf("0", "1", "2", "3"), SPINNER, BOX64_DYNAREC_BIGBLOCK)
        addToAdapter(R.string.box64_strongmem, R.string.box64_strongmem_desc, arrayOf("0", "1", "2", "3"), SPINNER, BOX64_DYNAREC_STRONGMEM)
        addToAdapter(R.string.box64_weakbarrier, R.string.box64_weakbarrier_desc, arrayOf("0", "1", "2"), SPINNER, BOX64_DYNAREC_WEAKBARRIER)
        addToAdapter(R.string.box64_pause, R.string.box64_pause_desc, arrayOf("0", "1", "2", "3"), SPINNER, BOX64_DYNAREC_PAUSE)
        addToAdapter(R.string.box64_x87double, R.string.box64_x87double_desc, arrayOf("0", "1", "2"), SPINNER, BOX64_DYNAREC_X87DOUBLE)
        addToAdapter(R.string.box64_fastnan, R.string.box64_fastnan_desc, null, SWITCH, BOX64_DYNAREC_FASTNAN)
        addToAdapter(R.string.box64_fastround, R.string.box64_fastround_desc, null, SWITCH, BOX64_DYNAREC_FASTROUND)
        addToAdapter(R.string.box64_safeflags, R.string.box64_safeflags_desc, arrayOf("0", "1", "2"), SPINNER, BOX64_DYNAREC_SAFEFLAGS)
        addToAdapter(R.string.box64_callret, R.string.box64_callret_desc, arrayOf("0", "1", "2"), SPINNER, BOX64_DYNAREC_CALLRET)
        addToAdapter(R.string.box64_df, R.string.box64_df_desc, null, SWITCH, BOX64_DYNAREC_DF)
        addToAdapter(R.string.box64_aligned_atomics, R.string.box64_aligned_atomics_desc, null, SWITCH, BOX64_DYNAREC_ALIGNED_ATOMICS)
        addToAdapter(R.string.box64_nativeflags, R.string.box64_nativeflags_desc, null, SWITCH, BOX64_DYNAREC_NATIVEFLAGS)
        addToAdapter(R.string.box64_dynarec_wait, R.string.box64_dynarec_wait_desc, null, SWITCH, BOX64_DYNAREC_WAIT)
        addToAdapter(R.string.box64_dynarec_dirty, R.string.box64_dynarec_dirty_desc, arrayOf("0", "1", "2"), SPINNER, BOX64_DYNAREC_DIRTY)
        addToAdapter(R.string.box64_dynarec_forward, R.string.box64_dynarec_forward_desc, arrayOf("0", "128", "256", "512", "1024"), SPINNER, BOX64_DYNAREC_FORWARD)
        addToAdapter(R.string.box64_avx, R.string.box64_avx_desc, arrayOf("0", "1", "2"), SPINNER, BOX64_AVX)
        addToAdapter(R.string.box64_sse42, R.string.box64_sse42_desc, null, SWITCH, BOX64_SSE42)
        addToAdapter(R.string.box64_mmap32, R.string.box64_mmap32_desc, null, SWITCH, BOX64_MMAP32)
    }

    private fun addToAdapter(titleId: Int, descriptionId: Int, valuesArray: Array<String>?, type: Int, keyId: String) {
        settingsList.add(
            AdapterSettingsBox64Preset.Box64ListSpinner(titleId, descriptionId, valuesArray, type, keyId)
        )
    }
}
