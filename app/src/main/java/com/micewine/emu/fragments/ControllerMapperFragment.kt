package com.micewine.emu.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
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
import com.micewine.emu.adapters.AdapterSettingsController
import com.micewine.emu.adapters.AdapterSettingsController.SettingsController

class ControllerMapperFragment : Fragment() {
    private val settingsList: MutableList<SettingsController> = ArrayList()
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

        setAdapter()

        layoutManager = recyclerView?.layoutManager as GridLayoutManager?
        layoutManager?.spanCount = 2

        return rootView
    }

    private fun setAdapter() {
        recyclerView?.setAdapter(AdapterSettingsController(settingsList, requireContext()))

        settingsList.clear()

        addToAdapter(R.drawable.a_button, BUTTON_A_KEY)
        addToAdapter(R.drawable.x_button, BUTTON_X_KEY)
        addToAdapter(R.drawable.y_button, BUTTON_Y_KEY)
        addToAdapter(R.drawable.b_button, BUTTON_B_KEY)
        addToAdapter(R.drawable.rb_button, BUTTON_R1_KEY)
        addToAdapter(R.drawable.rt_button, BUTTON_R2_KEY)
        addToAdapter(R.drawable.lb_button, BUTTON_L1_KEY)
        addToAdapter(R.drawable.lt_button, BUTTON_L2_KEY)
        addToAdapter(R.drawable.start_button, BUTTON_START_KEY)
        addToAdapter(R.drawable.select_button, BUTTON_SELECT_KEY)
        addToAdapter(R.drawable.l_right, AXIS_X_PLUS_KEY)
        addToAdapter(R.drawable.l_left, AXIS_X_MINUS_KEY)
        addToAdapter(R.drawable.l_down, AXIS_Y_PLUS_KEY)
        addToAdapter(R.drawable.l_up, AXIS_Y_MINUS_KEY)
        addToAdapter(R.drawable.r_right, AXIS_Z_PLUS_KEY)
        addToAdapter(R.drawable.r_left, AXIS_Z_MINUS_KEY)
        addToAdapter(R.drawable.r_down, AXIS_RZ_PLUS_KEY)
        addToAdapter(R.drawable.r_up, AXIS_RZ_MINUS_KEY)
        addToAdapter(R.drawable.dpad_right, AXIS_HAT_X_PLUS_KEY)
        addToAdapter(R.drawable.dpad_left, AXIS_HAT_X_MINUS_KEY)
        addToAdapter(R.drawable.dpad_down, AXIS_HAT_Y_PLUS_KEY)
        addToAdapter(R.drawable.dpad_up, AXIS_HAT_Y_MINUS_KEY)
    }

    private fun addToAdapter(buttonIconId: Int, keyId: String) {
        settingsList.add(SettingsController(buttonIconId, keyId))
    }
}
