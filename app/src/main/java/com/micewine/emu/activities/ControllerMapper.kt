package com.micewine.emu.activities

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.ImageButton
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import com.micewine.emu.ControllerUtils.getGameControllerNames
import com.micewine.emu.R
import com.micewine.emu.activities.GeneralSettings.Companion.DEAD_ZONE_KEY
import com.micewine.emu.activities.GeneralSettings.Companion.MOUSE_SENSIBILITY_KEY
import com.micewine.emu.databinding.ActivityControllerMapperBinding
import com.micewine.emu.fragments.ControllerMapperFragment

class ControllerMapper : AppCompatActivity() {
    private var binding: ActivityControllerMapperBinding? = null
    private var backButton: ImageButton? = null
    private var controllerConnected: TextView? = null
    private var deadZoneSeekbar: SeekBar? = null
    private var seekBarDeadZoneValue: TextView? = null
    private var mouseSensibilitySeekBar: SeekBar? = null
    private var mouseSensibilityValue: TextView? = null

    @SuppressLint("SetTextI18n")
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

        val preferences = PreferenceManager.getDefaultSharedPreferences(this)!!

        deadZoneSeekbar = findViewById(R.id.deadZoneSeekBar)

        deadZoneSeekbar?.progress = preferences.getInt(DEAD_ZONE_KEY, 25)

        seekBarDeadZoneValue = findViewById(R.id.seekBarDeadZoneValue)

        seekBarDeadZoneValue?.text = "${deadZoneSeekbar?.progress.toString()}%"

        deadZoneSeekbar?.max = 75

        deadZoneSeekbar?.min = 25

        deadZoneSeekbar?.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            @SuppressLint("SetTextI18n")
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                seekBarDeadZoneValue?.text = "$progress%"
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                val editor = preferences.edit()

                editor.putInt(DEAD_ZONE_KEY, seekBar!!.progress)

                editor.apply()
            }
        })

        mouseSensibilitySeekBar = findViewById(R.id.mouseSensibilitySeekBar)

        mouseSensibilitySeekBar?.progress = preferences.getInt(MOUSE_SENSIBILITY_KEY, 100)

        mouseSensibilityValue = findViewById(R.id.mouseSensibilityValue)

        mouseSensibilityValue?.text = "${mouseSensibilitySeekBar?.progress.toString()}%"

        mouseSensibilitySeekBar?.max = 200

        mouseSensibilitySeekBar?.min = 50

        mouseSensibilitySeekBar?.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            @SuppressLint("SetTextI18n")
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                mouseSensibilityValue?.text = "$progress%"
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                val editor = preferences.edit()

                editor.putInt(MOUSE_SENSIBILITY_KEY, seekBar!!.progress)

                editor.apply()
            }
        })
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
        val availableButtonMappings: Array<String> = arrayOf(
            "Null", "ESC", "Left", "Right", "Up", "Down", "Enter",
            "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K",
            "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V",
            "W", "X", "Y", "Z", "'", "LCtrl", "RCtrl", "LShift",
            "RShift", "Tab", "Space", "AltLeft", "F1", "F2", "F3",
            "F4", "F5", "F6", "F7", "F8", "F9", "F10", "F11", "F12",
            "Insert", "Home", "PageUp", "Delete", "End", "PageDown",
            "0", "1")

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