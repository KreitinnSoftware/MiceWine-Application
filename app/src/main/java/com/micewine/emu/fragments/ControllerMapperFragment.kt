package com.micewine.emu.fragments

import androidx.fragment.app.Fragment
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.micewine.emu.R
import com.micewine.emu.activities.ControllerMapper.Companion.AXIS_HAT_X_MINUS_KEY
import com.micewine.emu.activities.ControllerMapper.Companion.AXIS_HAT_X_PLUS_KEY
import com.micewine.emu.activities.ControllerMapper.Companion.AXIS_HAT_Y_MINUS_KEY
import com.micewine.emu.activities.ControllerMapper.Companion.AXIS_HAT_Y_PLUS_KEY
import com.micewine.emu.activities.ControllerMapper.Companion.AXIS_RZ_MINUS_KEY
import com.micewine.emu.activities.ControllerMapper.Companion.AXIS_RZ_PLUS_KEY
import com.micewine.emu.activities.ControllerMapper.Companion.AXIS_X_MINUS_KEY
import com.micewine.emu.activities.ControllerMapper.Companion.AXIS_X_PLUS_KEY
import com.micewine.emu.activities.ControllerMapper.Companion.AXIS_Y_MINUS_KEY
import com.micewine.emu.activities.ControllerMapper.Companion.AXIS_Y_PLUS_KEY
import com.micewine.emu.activities.ControllerMapper.Companion.AXIS_Z_MINUS_KEY
import com.micewine.emu.activities.ControllerMapper.Companion.AXIS_Z_PLUS_KEY
import com.micewine.emu.activities.ControllerMapper.Companion.BUTTON_A_KEY
import com.micewine.emu.activities.ControllerMapper.Companion.BUTTON_B_KEY
import com.micewine.emu.activities.ControllerMapper.Companion.BUTTON_L1_KEY
import com.micewine.emu.activities.ControllerMapper.Companion.BUTTON_L2_KEY
import com.micewine.emu.activities.ControllerMapper.Companion.BUTTON_R1_KEY
import com.micewine.emu.activities.ControllerMapper.Companion.BUTTON_R2_KEY
import com.micewine.emu.activities.ControllerMapper.Companion.BUTTON_SELECT_KEY
import com.micewine.emu.activities.ControllerMapper.Companion.BUTTON_START_KEY
import com.micewine.emu.activities.ControllerMapper.Companion.BUTTON_X_KEY
import com.micewine.emu.activities.ControllerMapper.Companion.BUTTON_Y_KEY
import com.micewine.emu.activities.ControllerMapper.Companion.availableButtonMappings
import com.micewine.emu.activities.GeneralSettings.Companion.BOX64_DYNAREC_BIGBLOCK_KEY
import com.micewine.emu.activities.GeneralSettings.Companion.BOX64_DYNAREC_CALLRET_KEY
import com.micewine.emu.activities.GeneralSettings.Companion.BOX64_DYNAREC_FASTNAN_KEY
import com.micewine.emu.activities.GeneralSettings.Companion.BOX64_DYNAREC_FASTROUND_KEY
import com.micewine.emu.activities.GeneralSettings.Companion.BOX64_DYNAREC_SAFEFLAGS_KEY
import com.micewine.emu.activities.GeneralSettings.Companion.BOX64_DYNAREC_STRONGMEM_KEY
import com.micewine.emu.activities.GeneralSettings.Companion.BOX64_DYNAREC_X87DOUBLE_KEY
import com.micewine.emu.activities.GeneralSettings.Companion.SPINNER
import com.micewine.emu.activities.GeneralSettings.Companion.SWITCH
import com.micewine.emu.adapters.AdapterSettingsController
import com.micewine.emu.adapters.AdapterSettingsController.SettingsController
import com.micewine.emu.adapters.AdapterSettingsPreferences
import com.micewine.emu.adapters.AdapterSettingsPreferences.SettingsListSpinner

class ControllerMapperFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_settings_model, container, false)
        val recyclerView = rootView.findViewById<RecyclerView>(R.id.recyclerViewSettingsModel)
        setAdapter(recyclerView)

        return rootView
    }

    private fun setAdapter(recyclerView: RecyclerView) {
        val settingsList: MutableList<SettingsController> = ArrayList()
        val adapterSettingsController = AdapterSettingsController(settingsList, requireContext())
        recyclerView.setAdapter(adapterSettingsController)
        var person: SettingsController?

        person = SettingsController("A", availableButtonMappings, BUTTON_A_KEY)
        settingsList.add(person)

        person = SettingsController("X", availableButtonMappings, BUTTON_X_KEY)
        settingsList.add(person)

        person = SettingsController("Y", availableButtonMappings, BUTTON_Y_KEY)
        settingsList.add(person)

        person = SettingsController("B", availableButtonMappings, BUTTON_B_KEY)
        settingsList.add(person)

        person = SettingsController("RB", availableButtonMappings, BUTTON_R1_KEY)
        settingsList.add(person)

        person = SettingsController("RT", availableButtonMappings, BUTTON_R2_KEY)
        settingsList.add(person)

        person = SettingsController("LB", availableButtonMappings, BUTTON_L1_KEY)
        settingsList.add(person)

        person = SettingsController("LT", availableButtonMappings, BUTTON_L2_KEY)
        settingsList.add(person)

        person = SettingsController("Start", availableButtonMappings, BUTTON_START_KEY)
        settingsList.add(person)

        person = SettingsController("Select", availableButtonMappings, BUTTON_SELECT_KEY)
        settingsList.add(person)

        person = SettingsController("AxisX+", availableButtonMappings, AXIS_X_PLUS_KEY)
        settingsList.add(person)

        person = SettingsController("AxisX-", availableButtonMappings, AXIS_X_MINUS_KEY)
        settingsList.add(person)

        person = SettingsController("AxisY+", availableButtonMappings, AXIS_Y_PLUS_KEY)
        settingsList.add(person)

        person = SettingsController("AxisY-", availableButtonMappings, AXIS_Y_MINUS_KEY)
        settingsList.add(person)

        person = SettingsController("AxisZ+", availableButtonMappings, AXIS_Z_PLUS_KEY)
        settingsList.add(person)

        person = SettingsController("AxisZ-", availableButtonMappings, AXIS_Z_MINUS_KEY)
        settingsList.add(person)

        person = SettingsController("AxisRZ+", availableButtonMappings, AXIS_RZ_PLUS_KEY)
        settingsList.add(person)

        person = SettingsController("AxisRZ-", availableButtonMappings, AXIS_RZ_MINUS_KEY)
        settingsList.add(person)

        person = SettingsController("AxisHatX+", availableButtonMappings, AXIS_HAT_X_PLUS_KEY)
        settingsList.add(person)

        person = SettingsController("AxisHatX-", availableButtonMappings, AXIS_HAT_X_MINUS_KEY)
        settingsList.add(person)

        person = SettingsController("AxisHatY+", availableButtonMappings, AXIS_HAT_Y_PLUS_KEY)
        settingsList.add(person)

        person = SettingsController("AxisHatY-", availableButtonMappings, AXIS_HAT_Y_MINUS_KEY)
        settingsList.add(person)
    }
}
