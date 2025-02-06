package com.micewine.emu.fragments

import android.app.Activity
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.micewine.emu.R
import com.micewine.emu.activities.GeneralSettingsActivity.Companion.DISPLAY_MODE
import com.micewine.emu.activities.GeneralSettingsActivity.Companion.DISPLAY_MODE_DEFAULT_VALUE
import com.micewine.emu.activities.GeneralSettingsActivity.Companion.DISPLAY_RESOLUTION
import com.micewine.emu.activities.GeneralSettingsActivity.Companion.DISPLAY_RESOLUTION_DEFAULT_VALUE
import com.micewine.emu.activities.GeneralSettingsActivity.Companion.SPINNER
import com.micewine.emu.activities.GeneralSettingsActivity.Companion.SWITCH
import com.micewine.emu.activities.MainActivity.Companion.CPU_COUNTER
import com.micewine.emu.activities.MainActivity.Companion.CPU_COUNTER_DEFAULT_VALUE
import com.micewine.emu.activities.MainActivity.Companion.ENABLE_DEBUG_INFO
import com.micewine.emu.activities.MainActivity.Companion.ENABLE_DEBUG_INFO_DEFAULT_VALUE
import com.micewine.emu.activities.MainActivity.Companion.RAM_COUNTER
import com.micewine.emu.activities.MainActivity.Companion.RAM_COUNTER_DEFAULT_VALUE
import com.micewine.emu.adapters.AdapterSettingsPreferences
import com.micewine.emu.adapters.AdapterSettingsPreferences.SettingsListSpinner

class DisplaySettingsFragment : Fragment() {
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
        recyclerView?.setAdapter(AdapterSettingsPreferences(settingsList, requireActivity(), recyclerView!!))

        settingsList.clear()

        addToAdapter(R.string.display_mode_title, R.string.display_mode_description, arrayOf(
            "16:9", "4:3", "Native"
            ),
            SPINNER, DISPLAY_MODE_DEFAULT_VALUE, DISPLAY_MODE)
        addToAdapter(R.string.display_resolution_title, R.string.display_resolution_description, null,
            SPINNER, DISPLAY_RESOLUTION_DEFAULT_VALUE, DISPLAY_RESOLUTION)
        addToAdapter(R.string.enable_ram_counter, R.string.enable_ram_counter_description, null,
            SWITCH, RAM_COUNTER_DEFAULT_VALUE, RAM_COUNTER)
        addToAdapter(R.string.enable_cpu_counter, R.string.enable_cpu_counter_description, null,
            SWITCH, CPU_COUNTER_DEFAULT_VALUE, CPU_COUNTER)
        addToAdapter(R.string.enable_debug_info, R.string.enable_debug_info_description, null,
            SWITCH, ENABLE_DEBUG_INFO_DEFAULT_VALUE, ENABLE_DEBUG_INFO)
    }

    private fun addToAdapter(titleId: Int, descriptionId: Int, valuesArray: Array<String>?, type: Int, defaultValue: Any, keyId: String) {
        settingsList.add(SettingsListSpinner(titleId, descriptionId, valuesArray, null, type, defaultValue.toString(), keyId))
    }

    companion object {
        val resolutions16_9 = arrayOf(
            "640x360", "854x480",
            "960x540", "1280x720",
            "1366x768", "1600x900",
            "1920x1080", "2560x1440",
            "3840x2160"
        )

        val resolutions4_3 = arrayOf(
            "640x480", "800x600",
            "1024x768", "1280x960",
            "1400x1050", "1600x1200"
        )

        @Suppress("DEPRECATION")
        private fun getNativeResolution(activity: Activity): String {
            val displayMetrics = DisplayMetrics()
            activity.windowManager.defaultDisplay.getRealMetrics(displayMetrics)
            return "${displayMetrics.widthPixels}x${displayMetrics.heightPixels}"
        }

        private fun getPercentOfResolution(original: String, percent: Int): String {
            val resolution = original.split("x")
            val width = resolution[0].toInt() * percent / 100
            val height = resolution[1].toInt() * percent / 100

            return "${width}x${height}"
        }

        fun getNativeResolutions(activity: Activity): List<String> {
            val parsedResolutions = mutableListOf<String>()
            val nativeResolution = getNativeResolution(activity)

            parsedResolutions.add(nativeResolution)
            parsedResolutions.add(getPercentOfResolution(nativeResolution, 90))
            parsedResolutions.add(getPercentOfResolution(nativeResolution, 80))
            parsedResolutions.add(getPercentOfResolution(nativeResolution, 70))
            parsedResolutions.add(getPercentOfResolution(nativeResolution, 60))
            parsedResolutions.add(getPercentOfResolution(nativeResolution, 50))
            parsedResolutions.add(getPercentOfResolution(nativeResolution, 40))
            parsedResolutions.add(getPercentOfResolution(nativeResolution, 30))

            return parsedResolutions
        }
    }
}
