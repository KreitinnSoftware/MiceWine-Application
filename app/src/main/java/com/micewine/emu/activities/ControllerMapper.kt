package com.micewine.emu.activities

import android.os.Bundle
import android.view.MenuItem
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import com.micewine.emu.ControllerUtils.getGameControllerNames
import com.micewine.emu.R
import com.micewine.emu.databinding.ActivityControllerMapperBinding
import com.micewine.emu.fragments.ControllerMapperFragment

class ControllerMapper : AppCompatActivity() {
    private var binding: ActivityControllerMapperBinding? = null
    private var backButton: ImageButton? = null
    private var controllerConnected: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityControllerMapperBinding.inflate(layoutInflater)
        setContentView(binding!!.root)

        fragmentLoader(ControllerMapperFragment(), true)

        findViewById<Toolbar>(R.id.controllerMapperToolbar).title = getString(R.string.controllerMapperTitle)

        controllerConnected = findViewById(R.id.controllerConnected)

        val connectedControllers = getGameControllerNames()

        if (connectedControllers.isNotEmpty()) {
            controllerConnected?.text = getString(R.string.connected_controller, connectedControllers[0])
        } else {
            controllerConnected?.text = getString(R.string.no_controllers_connected)
        }

        backButton = findViewById(R.id.backButton)

        backButton?.setOnClickListener {
            if (supportFragmentManager.backStackEntryCount > 0) {
                supportFragmentManager.popBackStack()
                findViewById<Toolbar>(R.id.controllerMapperToolbar).title = this.resources.getString(R.string.controllerMapperTitle)
            } else {
                finish()
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId

        if (id == android.R.id.home) {
            finish()
            return true
        }

        return super.onOptionsItemSelected(item)
    }

    private fun fragmentLoader(fragment: Fragment, appInit: Boolean) {
        val fragmentTransaction = supportFragmentManager.beginTransaction()

        fragmentTransaction.replace(R.id.controller_mapper_content, fragment)

        if (!appInit) {
            fragmentTransaction.addToBackStack(null)
        }

        fragmentTransaction.commit()
    }

    companion object {
        val availableButtonMappings = arrayOf("Null", "ESC", "Left", "Right", "Up", "Down", "Enter", "C", "X", "Z")

        const val BUTTON_A_KEY = "buttonA"
        const val BUTTON_B_KEY = "buttonB"
        const val BUTTON_X_KEY = "buttonX"
        const val BUTTON_Y_KEY = "buttonY"
        const val BUTTON_START_KEY = "buttonStart"
        const val BUTTON_SELECT_KEY = "buttonSelect"
        const val BUTTON_R1_KEY = "buttonR1"
        const val BUTTON_R2_KEY = "buttonR2"
        const val BUTTON_L1_KEY = "buttonL1"
        const val BUTTON_L2_KEY = "buttonL2"
        const val AXIS_X_PLUS_KEY = "axisX+"
        const val AXIS_X_MINUS_KEY = "axisX-"
        const val AXIS_Y_PLUS_KEY = "axisY+"
        const val AXIS_Y_MINUS_KEY = "axisY-"
        const val AXIS_Z_PLUS_KEY = "axisZ+"
        const val AXIS_Z_MINUS_KEY = "axisZ-"
        const val AXIS_RZ_PLUS_KEY = "axisRZ+"
        const val AXIS_RZ_MINUS_KEY = "axisRZ-"
        const val AXIS_HAT_X_PLUS_KEY = "axisHatX+"
        const val AXIS_HAT_X_MINUS_KEY = "axisHatX-"
        const val AXIS_HAT_Y_PLUS_KEY = "axisHatY+"
        const val AXIS_HAT_Y_MINUS_KEY = "axisHatY-"
    }
}