package com.micewine.emu.activities

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ImageButton
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.micewine.emu.R
import com.micewine.emu.activities.GeneralSettings.Companion.DEAD_ZONE
import com.micewine.emu.activities.GeneralSettings.Companion.MOUSE_SENSIBILITY
import com.micewine.emu.controller.ControllerUtils.getGameControllerNames
import com.micewine.emu.databinding.ActivityControllerMapperBinding
import com.micewine.emu.fragments.ControllerMapperFragment
import com.micewine.emu.fragments.CreateControllerPresetFragment
import java.util.Collections

class ControllerMapper : AppCompatActivity() {
    private var binding: ActivityControllerMapperBinding? = null
    private var backButton: ImageButton? = null
    private var controllerConnected: TextView? = null
    private var deadZoneSeekbar: SeekBar? = null
    private var seekBarDeadZoneValue: TextView? = null
    private var mouseSensibilitySeekBar: SeekBar? = null
    private var mouseSensibilityValue: TextView? = null
    private var selectedControllerPresetSpinner: Spinner? = null
    private var addNewPresetButton: ImageButton? = null
    private var deletePresetButton: ImageButton? = null
    private var preferences: SharedPreferences? = null
    private val receiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (ACTION_UPDATE_CONTROLLER_MAPPER == intent.action) {
                val name = intent.getStringExtra("name")

                selectedControllerPresetSpinner?.adapter = ArrayAdapter(context, android.R.layout.simple_spinner_dropdown_item, getControllerPresetsName(context))
                selectedControllerPresetSpinner?.setSelection(getControllerPresetsName(context).indexOf(name))

                fragmentLoader(ControllerMapperFragment())
            }
        }
    }

    @SuppressLint("SetTextI18n", "UnspecifiedRegisterReceiverFlag")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        preferences = PreferenceManager.getDefaultSharedPreferences(this)!!

        binding = ActivityControllerMapperBinding.inflate(layoutInflater)
        setContentView(binding!!.root)

        fragmentLoader(ControllerMapperFragment())

        findViewById<Toolbar>(R.id.controllerMapperToolbar).title = getString(R.string.controller_mapper_title)

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
                findViewById<Toolbar>(R.id.controllerMapperToolbar).title = this.resources.getString(R.string.controller_mapper_title)
            } else {
                finish()
            }
        }

        deadZoneSeekbar = findViewById(R.id.deadZoneSeekBar)
        deadZoneSeekbar?.max = 75
        deadZoneSeekbar?.min = 25
        deadZoneSeekbar?.progress = getDeadZone(this, preferences!!.getString(SELECTED_CONTROLLER_PRESET_KEY, "default")!!)
        seekBarDeadZoneValue = findViewById(R.id.seekBarDeadZoneValue)
        seekBarDeadZoneValue?.text = "${deadZoneSeekbar?.progress.toString()}%"

        deadZoneSeekbar?.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            @SuppressLint("SetTextI18n")
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                seekBarDeadZoneValue?.text = "$progress%"
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                putDeadZone(applicationContext, preferences!!.getString(SELECTED_CONTROLLER_PRESET_KEY, "default")!!, seekBar!!.progress)
            }
        })

        mouseSensibilitySeekBar = findViewById(R.id.mouseSensibilitySeekBar)
        mouseSensibilitySeekBar?.max = 350
        mouseSensibilitySeekBar?.min = 25
        mouseSensibilitySeekBar?.progress = getMouseSensibility(this, preferences!!.getString(SELECTED_CONTROLLER_PRESET_KEY, "default")!!)
        mouseSensibilityValue = findViewById(R.id.mouseSensibilityValue)
        mouseSensibilityValue?.text = "${mouseSensibilitySeekBar?.progress.toString()}%"
        mouseSensibilitySeekBar?.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            @SuppressLint("SetTextI18n")
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                mouseSensibilityValue?.text = "$progress%"
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                putMouseSensibility(applicationContext, preferences!!.getString(SELECTED_CONTROLLER_PRESET_KEY, "default")!!, seekBar!!.progress)
            }
        })

        selectedControllerPresetSpinner = findViewById(R.id.selectedControllerPresetSpinner)
        selectedControllerPresetSpinner?.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, getControllerPresetsName(this))
        selectedControllerPresetSpinner?.setSelection(getControllerPresetsName(this).indexOf(preferences!!.getString(SELECTED_CONTROLLER_PRESET_KEY, "default")))
        selectedControllerPresetSpinner?.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    preferences!!.edit().apply {
                        putString(SELECTED_CONTROLLER_PRESET_KEY, parent?.selectedItem.toString())
                        apply()
                    }
                    fragmentLoader(ControllerMapperFragment())
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                }
            }

        addNewPresetButton = findViewById(R.id.addNewPreset)
        addNewPresetButton?.setOnClickListener {
            CreateControllerPresetFragment().show(supportFragmentManager, "")
        }

        deletePresetButton = findViewById(R.id.deletePreset)
        deletePresetButton?.setOnClickListener {
            deleteControllerPreset(this, preferences?.getString(SELECTED_CONTROLLER_PRESET_KEY, "default")!!)
        }

        registerReceiver(receiver, object : IntentFilter(ACTION_UPDATE_CONTROLLER_MAPPER) {})
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }

        return super.onOptionsItemSelected(item)
    }

    private fun fragmentLoader(fragment: Fragment) {
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.controller_mapper_content, fragment)
            commit()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(receiver)
    }

    companion object {
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
        const val BUTTON_THUMBL_KEY = "thumbLKey"
        const val BUTTON_THUMBR_KEY = "thumbRKey"
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

        const val SELECTED_CONTROLLER_PRESET_KEY = "selectedControllerPreset"
        const val ACTION_UPDATE_CONTROLLER_MAPPER = "com.micewine.emu.ACTION_UPDATE_CONTROLLER_MAPPER"

        private val gson = Gson()

        private val mappingMap = mapOf(
            BUTTON_A_KEY to 1,
            BUTTON_B_KEY to 2,
            BUTTON_X_KEY to 3,
            BUTTON_Y_KEY to 4,
            BUTTON_START_KEY to 5,
            BUTTON_SELECT_KEY to 6,
            BUTTON_R1_KEY to 7,
            BUTTON_R2_KEY to 8,
            BUTTON_L1_KEY to 9,
            BUTTON_L2_KEY to 10,
            BUTTON_THUMBL_KEY to 11,
            BUTTON_THUMBR_KEY to 12,
            AXIS_X_PLUS_KEY to 13,
            AXIS_X_MINUS_KEY to 14,
            AXIS_Y_PLUS_KEY to 15,
            AXIS_Y_MINUS_KEY to 16,
            AXIS_Z_PLUS_KEY to 17,
            AXIS_Z_MINUS_KEY to 18,
            AXIS_RZ_PLUS_KEY to 19,
            AXIS_RZ_MINUS_KEY to 20,
            AXIS_HAT_X_PLUS_KEY to 21,
            AXIS_HAT_X_MINUS_KEY to 22,
            AXIS_HAT_Y_PLUS_KEY to 23,
            AXIS_HAT_Y_MINUS_KEY to 24,
            DEAD_ZONE to 25,
            MOUSE_SENSIBILITY to 26
        )

        fun putDeadZone(context: Context, name: String, value: Int) {
            val currentList = loadControllerPresets(context)
            var index = currentList.indexOfFirst { it[0] == name }

            if (index == -1) {
                currentList[0][0] = name

                index = 0
            }

            currentList[index][mappingMap[DEAD_ZONE]!!] = "$value"

            val json = gson.toJson(currentList)

            PreferenceManager.getDefaultSharedPreferences(context).apply {
                edit().apply {
                    putString("controllerPresetList", json)
                    apply()
                }
            }
        }

        fun putMouseSensibility(context: Context, name: String, value: Int) {
            val currentList = loadControllerPresets(context)
            var index = currentList.indexOfFirst { it[0] == name }

            if (index == -1) {
                currentList[0][0] = name

                index = 0
            }

            currentList[index][mappingMap[MOUSE_SENSIBILITY]!!] = "$value"

            val json = gson.toJson(currentList)

            PreferenceManager.getDefaultSharedPreferences(context).apply {
                edit().apply {
                    putString("controllerPresetList", json)
                    apply()
                }
            }
        }

        fun getDeadZone(context: Context, name: String): Int {
            val currentList = loadControllerPresets(context)
            val index = currentList.indexOfFirst { it[0] == name }

            if (index == -1) {
                return 25
            }

            return currentList[index][mappingMap[DEAD_ZONE]!!].toInt()
        }

        fun getMouseSensibility(context: Context, name: String): Int {
            val currentList = loadControllerPresets(context)
            val index = currentList.indexOfFirst { it[0] == name }

            if (index == -1) {
                return 100
            }

            return currentList[index][mappingMap[MOUSE_SENSIBILITY]!!].toInt()
        }

        fun getMapping(context: Context, name: String, key: String): List<String> {
            val currentList = loadControllerPresets(context)
            val index = currentList.indexOfFirst { it[0] == name }

            if (index == -1) {
                return listOf("", "")
            }

            return currentList[index][mappingMap[key]!!].split(":")
        }

        fun deleteControllerPreset(context: Context, name: String) {
            val currentList = loadControllerPresets(context)

            if (currentList.count() == 1) {
                Toast.makeText(context,
                    context.getString(R.string.remove_last_preset_error), Toast.LENGTH_SHORT).show()

                return
            }

            currentList.removeIf { it[0] == name }

            val json = gson.toJson(currentList)

            PreferenceManager.getDefaultSharedPreferences(context).apply {
                edit().apply {
                    putString("controllerPresetList", json)
                    apply()
                }
            }

            val intent = Intent(ACTION_UPDATE_CONTROLLER_MAPPER)
            intent.putExtra("name", "default")
            context.sendBroadcast(intent)
        }

        fun addControllerPreset(context: Context, name: String) {
            val preferences = PreferenceManager.getDefaultSharedPreferences(context)
            val currentList = loadControllerPresets(context)
            val defaultList = ArrayList(Collections.nCopies(25, ":")).apply {
                this[0] = name
                this[mappingMap[DEAD_ZONE]!!] = "25"
                this[mappingMap[MOUSE_SENSIBILITY]!!] = "100"
            }

            currentList.add(defaultList)

            val json = gson.toJson(currentList)

            preferences.edit().apply {
                putString("controllerPresetList", json)
                putString(SELECTED_CONTROLLER_PRESET_KEY, name)
                apply()
            }

            context.apply {
                sendBroadcast(
                    Intent(ACTION_UPDATE_CONTROLLER_MAPPER).apply {
                        putExtra("name", name)
                    }
                )
            }
        }

        fun editControllerPreset(context: Context, name: String, key: String, selectedItem: String) {
            val preferences = PreferenceManager.getDefaultSharedPreferences(context)
            val currentList = loadControllerPresets(context)

            var index = currentList.indexOfFirst { it[0] == name }

            if (index == -1) {
                currentList[0][0] = name
                index = 0
            }

            currentList[index][mappingMap[key]!!] = selectedItem

            val json = gson.toJson(currentList)

            preferences.edit().apply {
                putString("controllerPresetList", json)
                apply()
            }
        }

        private fun loadControllerPresets(context: Context): MutableList<MutableList<String>> {
            val preferences = PreferenceManager.getDefaultSharedPreferences(context)
            val json = preferences.getString("controllerPresetList", "")
            val listType = object : TypeToken<MutableList<List<String>>>() {}.type

            return gson.fromJson(json, listType) ?: mutableListOf(ArrayList(Collections.nCopies(mappingMap.size + 1, ":")).apply {
                this[0] = "default"
                this[mappingMap[DEAD_ZONE]!!] = "25"
                this[mappingMap[MOUSE_SENSIBILITY]!!] = "100"
            })
        }

        fun getControllerPresetsName(context: Context): List<String> {
            val currentList = loadControllerPresets(context)
            val presetsName: MutableList<String> = mutableListOf()

            currentList.forEach {
                presetsName.add(it[0])
            }

            return presetsName
        }
    }
}