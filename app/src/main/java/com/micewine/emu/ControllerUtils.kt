package com.micewine.emu

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import android.view.InputDevice
import android.view.KeyEvent
import android.view.KeyEvent.KEYCODE_BUTTON_A
import android.view.KeyEvent.KEYCODE_BUTTON_B
import android.view.KeyEvent.KEYCODE_BUTTON_L1
import android.view.KeyEvent.KEYCODE_BUTTON_L2
import android.view.KeyEvent.KEYCODE_BUTTON_R1
import android.view.KeyEvent.KEYCODE_BUTTON_R2
import android.view.KeyEvent.KEYCODE_BUTTON_SELECT
import android.view.KeyEvent.KEYCODE_BUTTON_START
import android.view.KeyEvent.KEYCODE_BUTTON_X
import android.view.KeyEvent.KEYCODE_BUTTON_Y
import android.view.MotionEvent
import android.view.MotionEvent.AXIS_HAT_X
import android.view.MotionEvent.AXIS_HAT_Y
import android.view.MotionEvent.AXIS_RZ
import android.view.MotionEvent.AXIS_X
import android.view.MotionEvent.AXIS_Y
import android.view.MotionEvent.AXIS_Z
import androidx.preference.PreferenceManager
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
import com.micewine.emu.activities.GeneralSettings.Companion.DEAD_ZONE_KEY
import com.micewine.emu.input.InputStub.BUTTON_LEFT
import com.micewine.emu.input.InputStub.BUTTON_MIDDLE
import com.micewine.emu.input.InputStub.BUTTON_RIGHT
import com.micewine.emu.overlay.XKeyCodes.getXKeyScanCodes

object ControllerUtils {
    private const val KEYBOARD = 0

    private const val MOUSE = 1

    private lateinit var axisX_plus_mapping: List<Int>

    private lateinit var axisY_plus_mapping: List<Int>

    private lateinit var axisX_minus_mapping: List<Int>

    private lateinit var axisY_minus_mapping: List<Int>

    private lateinit var axisZ_plus_mapping: List<Int>

    private lateinit var axisRZ_plus_mapping: List<Int>

    private lateinit var axisZ_minus_mapping: List<Int>

    private lateinit var axisRZ_minus_mapping: List<Int>

    private lateinit var buttonA_mapping: List<Int>

    private lateinit var buttonB_mapping: List<Int>

    private lateinit var buttonX_mapping: List<Int>

    private lateinit var buttonY_mapping: List<Int>

    private lateinit var buttonStart_mapping: List<Int>

    private lateinit var buttonSelect_mapping: List<Int>

    private lateinit var buttonR1_mapping: List<Int>

    private lateinit var buttonL1_mapping: List<Int>

    private lateinit var buttonR2_mapping: List<Int>

    private lateinit var buttonL2_mapping: List<Int>

    private lateinit var axisHatX_plus_mapping: List<Int>

    private lateinit var axisHatY_plus_mapping: List<Int>

    private lateinit var axisHatX_minus_mapping: List<Int>

    private lateinit var axisHatY_minus_mapping: List<Int>

    private var deadZone: Float = 0F

    private fun detectKey(preferences: SharedPreferences, key: String): MutableList<Int> {
        val list = getXKeyScanCodes(preferences.getString(key, "Null")!!)

        if (preferences.getString("${key}_mappingType", "Keyboard") == "Keyboard") {
            list[2] = KEYBOARD
        } else {
            if (preferences.getString(key, "Null") == "Left") {
                list[1] = BUTTON_LEFT
            } else if (preferences.getString(key, "Null") == "Right") {
                list[1] = BUTTON_RIGHT
            } else if (preferences.getString(key, "Null") == "Middle") {
                list[1] = BUTTON_MIDDLE
            }

            list[2] = MOUSE
        }

        return list
    }

    fun prepareButtonsAxisValues(context: Context) {
        val preferences = PreferenceManager.getDefaultSharedPreferences(context)!!

        buttonA_mapping = detectKey(preferences, BUTTON_A_KEY)
        buttonX_mapping = detectKey(preferences, BUTTON_X_KEY)
        buttonB_mapping = detectKey(preferences, BUTTON_B_KEY)
        buttonY_mapping = detectKey(preferences, BUTTON_Y_KEY)

        buttonR1_mapping = detectKey(preferences, BUTTON_R1_KEY)
        buttonR2_mapping = detectKey(preferences, BUTTON_R2_KEY)

        buttonL1_mapping = detectKey(preferences, BUTTON_L1_KEY)
        buttonL2_mapping = detectKey(preferences, BUTTON_L2_KEY)

        buttonStart_mapping = detectKey(preferences, BUTTON_START_KEY)
        buttonSelect_mapping = detectKey(preferences, BUTTON_SELECT_KEY)

        axisX_plus_mapping = getXKeyScanCodes(preferences.getString(AXIS_X_PLUS_KEY, "Null")!!)
        axisX_minus_mapping = getXKeyScanCodes(preferences.getString(AXIS_X_MINUS_KEY, "Null")!!)

        axisY_plus_mapping = getXKeyScanCodes(preferences.getString(AXIS_Y_PLUS_KEY, "Null")!!)
        axisY_minus_mapping = getXKeyScanCodes(preferences.getString(AXIS_Y_MINUS_KEY, "Null")!!)

        axisZ_plus_mapping = getXKeyScanCodes(preferences.getString(AXIS_Z_PLUS_KEY, "Null")!!)
        axisZ_minus_mapping = getXKeyScanCodes(preferences.getString(AXIS_Z_MINUS_KEY, "Null")!!)

        axisRZ_plus_mapping = getXKeyScanCodes(preferences.getString(AXIS_RZ_PLUS_KEY, "Null")!!)
        axisRZ_minus_mapping = getXKeyScanCodes(preferences.getString(AXIS_RZ_MINUS_KEY, "Null")!!)

        axisHatX_plus_mapping = getXKeyScanCodes(preferences.getString(AXIS_HAT_X_PLUS_KEY, "Null")!!)
        axisHatX_minus_mapping = getXKeyScanCodes(preferences.getString(AXIS_HAT_X_MINUS_KEY, "Null")!!)

        axisHatY_plus_mapping = getXKeyScanCodes(preferences.getString(AXIS_HAT_Y_PLUS_KEY, "Null")!!)
        axisHatY_minus_mapping = getXKeyScanCodes(preferences.getString(AXIS_HAT_Y_MINUS_KEY, "Null")!!)

        deadZone = (preferences.getInt(DEAD_ZONE_KEY, 25)).toFloat() / 100

        Log.v("Info", "$deadZone")
    }

    private fun getGameControllerIds(): List<Int> {
        val gameControllerDeviceIds = mutableListOf<Int>()
        val deviceIds = InputDevice.getDeviceIds()
        deviceIds.forEach { deviceId ->
            InputDevice.getDevice(deviceId)?.apply {

                if (sources and InputDevice.SOURCE_GAMEPAD == InputDevice.SOURCE_GAMEPAD
                    || sources and InputDevice.SOURCE_JOYSTICK == InputDevice.SOURCE_JOYSTICK) {
                    gameControllerDeviceIds
                        .takeIf { !it.contains(deviceId) }
                        ?.add(deviceId)
                }
            }
        }
        return gameControllerDeviceIds
    }

    fun getGameControllerNames(): List<String> {
        val deviceIds = getGameControllerIds()
        val deviceNames = mutableListOf<String>()

        for (id in deviceIds) {
            val inputDevice = InputDevice.getDevice(id)

            deviceNames.add(inputDevice?.name.toString())
        }

        return deviceNames
    }

    private fun handleKey(lorieView: LorieView, pressed: Boolean, mapping: List<Int>) {
        when (mapping[2]) {
            KEYBOARD -> {
                lorieView.sendKeyEvent(mapping[0], mapping[1], pressed)
            }

            MOUSE -> {
                lorieView.sendMouseEvent(0F, 0F, mapping[1], pressed, true)
            }
        }
    }

    fun checkControllerButtons(lorieView: LorieView, e: KeyEvent): Boolean {
        val pressed = e.action == KeyEvent.ACTION_DOWN

        return when (e.keyCode) {
            KEYCODE_BUTTON_Y -> {
                handleKey(lorieView, pressed, buttonY_mapping)

                true
            }

            KEYCODE_BUTTON_A -> {
                handleKey(lorieView, pressed, buttonA_mapping)

                true
            }

            KEYCODE_BUTTON_B -> {
                handleKey(lorieView, pressed, buttonB_mapping)

                true
            }

            KEYCODE_BUTTON_X -> {
                handleKey(lorieView, pressed, buttonX_mapping)

                true
            }

            KEYCODE_BUTTON_START -> {
                handleKey(lorieView, pressed, buttonStart_mapping)

                true
            }

            KEYCODE_BUTTON_SELECT -> {
                handleKey(lorieView, pressed, buttonSelect_mapping)

                true
            }

            KEYCODE_BUTTON_R1 -> {
                handleKey(lorieView, pressed, buttonR1_mapping)

                true
            }

            KEYCODE_BUTTON_R2 -> {
                handleKey(lorieView, pressed, buttonR2_mapping)

                true
            }

            KEYCODE_BUTTON_L1 -> {
                handleKey(lorieView, pressed, buttonL1_mapping)

                true
            }

            KEYCODE_BUTTON_L2 -> {
                handleKey(lorieView, pressed, buttonL2_mapping)

                true
            }

            else -> false
        }
    }

    fun checkControllerAxis(lorieView: LorieView, event: MotionEvent): Boolean {
        val axisX = event.getAxisValue(AXIS_X)
        val axisY = event.getAxisValue(AXIS_Y)
        val axisXNeutral = axisX < deadZone && axisX > -deadZone
        val axisYNeutral = axisY < deadZone && axisY > -deadZone

        val axisZ = event.getAxisValue(AXIS_Z)
        val axisRZ = event.getAxisValue(AXIS_RZ)
        val axisZNeutral = axisZ < deadZone && axisZ > -deadZone
        val axisRZNeutral = axisRZ < deadZone && axisRZ > -deadZone

        val axisHatX = event.getAxisValue(AXIS_HAT_X)
        val axisHatY = event.getAxisValue(AXIS_HAT_Y)
        val axisHatXNeutral = axisHatX < deadZone && axisHatX > -deadZone
        val axisHatYNeutral = axisHatY < deadZone && axisHatY > -deadZone

        when {
            // Right
            axisX > deadZone && axisYNeutral -> {
                lorieView.sendKeyEvent(axisX_plus_mapping[0], axisX_plus_mapping[1], true)
                lorieView.sendKeyEvent(axisX_minus_mapping[0], axisX_minus_mapping[1], false)

                lorieView.sendKeyEvent(axisY_plus_mapping[0], axisY_plus_mapping[1], false)
                lorieView.sendKeyEvent(axisY_minus_mapping[0], axisY_minus_mapping[1], false)

                return true
            }

            // Left
            axisX < -deadZone && axisYNeutral -> {
                lorieView.sendKeyEvent(axisX_minus_mapping[0], axisX_minus_mapping[1], true)
                lorieView.sendKeyEvent(axisX_plus_mapping[0], axisX_plus_mapping[1], false)

                lorieView.sendKeyEvent(axisY_plus_mapping[0], axisY_plus_mapping[1], false)
                lorieView.sendKeyEvent(axisY_minus_mapping[0], axisY_minus_mapping[1], false)

                return true
            }

            // Down
            axisY > deadZone && axisXNeutral -> {
                lorieView.sendKeyEvent(axisY_plus_mapping[0], axisY_plus_mapping[1], true)
                lorieView.sendKeyEvent(axisY_minus_mapping[0], axisY_minus_mapping[1], false)

                lorieView.sendKeyEvent(axisX_minus_mapping[0], axisX_minus_mapping[1], false)
                lorieView.sendKeyEvent(axisX_plus_mapping[0], axisX_plus_mapping[1], false)

                return true
            }

            // Up
            axisY < -deadZone && axisXNeutral -> {
                lorieView.sendKeyEvent(axisY_minus_mapping[0], axisY_minus_mapping[1], true)
                lorieView.sendKeyEvent(axisY_plus_mapping[0], axisY_plus_mapping[1], false)

                lorieView.sendKeyEvent(axisX_minus_mapping[0], axisX_minus_mapping[1], false)
                lorieView.sendKeyEvent(axisX_plus_mapping[0], axisX_plus_mapping[1], false)

                return true
            }

            // Right/Down
            axisX > deadZone && axisY > deadZone -> {
                lorieView.sendKeyEvent(axisX_plus_mapping[0], axisX_plus_mapping[1], true)
                lorieView.sendKeyEvent(axisX_minus_mapping[0], axisX_minus_mapping[1], false)

                lorieView.sendKeyEvent(axisY_plus_mapping[0], axisY_plus_mapping[1], true)
                lorieView.sendKeyEvent(axisY_minus_mapping[0], axisY_minus_mapping[1], false)

                return true
            }

            // Right/Up
            axisX > deadZone && axisY < -deadZone -> {
                lorieView.sendKeyEvent(axisX_plus_mapping[0], axisX_plus_mapping[1], true)
                lorieView.sendKeyEvent(axisX_minus_mapping[0], axisX_minus_mapping[1], false)

                lorieView.sendKeyEvent(axisY_plus_mapping[0], axisY_plus_mapping[1], false)
                lorieView.sendKeyEvent(axisY_minus_mapping[0], axisY_minus_mapping[1], true)

                return true
            }

            // Left/Up
            axisX < -deadZone && axisY < -deadZone -> {
                lorieView.sendKeyEvent(axisX_plus_mapping[0], axisX_plus_mapping[1], false)
                lorieView.sendKeyEvent(axisX_minus_mapping[0], axisX_minus_mapping[1], true)

                lorieView.sendKeyEvent(axisY_plus_mapping[0], axisY_plus_mapping[1], false)
                lorieView.sendKeyEvent(axisY_minus_mapping[0], axisY_minus_mapping[1], true)

                return true
            }

            // Left/Down
            axisX < -deadZone && axisY > deadZone -> {
                lorieView.sendKeyEvent(axisX_plus_mapping[0], axisX_plus_mapping[1], false)
                lorieView.sendKeyEvent(axisX_minus_mapping[0], axisX_minus_mapping[1], true)

                lorieView.sendKeyEvent(axisY_plus_mapping[0], axisY_plus_mapping[1], true)
                lorieView.sendKeyEvent(axisY_minus_mapping[0], axisY_minus_mapping[1], false)

                return true
            }

            else -> {
                lorieView.sendKeyEvent(axisX_plus_mapping[0], axisX_plus_mapping[1], false)
                lorieView.sendKeyEvent(axisX_minus_mapping[0], axisX_minus_mapping[1], false)
                lorieView.sendKeyEvent(axisY_plus_mapping[0], axisY_plus_mapping[1], false)
                lorieView.sendKeyEvent(axisY_minus_mapping[0], axisY_minus_mapping[1], false)
            }
        }

        when {
            // Right
            axisZ > deadZone && axisRZNeutral -> {
                lorieView.sendKeyEvent(axisZ_plus_mapping[0], axisZ_plus_mapping[1], true)
                lorieView.sendKeyEvent(axisZ_minus_mapping[0], axisZ_minus_mapping[1], false)

                lorieView.sendKeyEvent(axisRZ_minus_mapping[0], axisRZ_minus_mapping[1], false)
                lorieView.sendKeyEvent(axisRZ_plus_mapping[0], axisRZ_plus_mapping[1], false)

                return true
            }

            // Left
            axisZ < -deadZone && axisRZNeutral -> {
                lorieView.sendKeyEvent(axisZ_minus_mapping[0], axisZ_minus_mapping[1], true)
                lorieView.sendKeyEvent(axisZ_plus_mapping[0], axisZ_plus_mapping[1], false)

                lorieView.sendKeyEvent(axisRZ_minus_mapping[0], axisRZ_minus_mapping[1], false)
                lorieView.sendKeyEvent(axisRZ_plus_mapping[0], axisRZ_plus_mapping[1], false)

                return true
            }

            // Down
            axisRZ > deadZone && axisZNeutral -> {
                lorieView.sendKeyEvent(axisRZ_plus_mapping[0], axisRZ_plus_mapping[1], true)
                lorieView.sendKeyEvent(axisRZ_minus_mapping[0], axisRZ_minus_mapping[1], false)

                lorieView.sendKeyEvent(axisZ_plus_mapping[0], axisZ_plus_mapping[1], false)
                lorieView.sendKeyEvent(axisZ_minus_mapping[0], axisZ_minus_mapping[1], false)

                return true
            }

            // Up
            axisRZ < -deadZone && axisZNeutral -> {
                lorieView.sendKeyEvent(axisRZ_minus_mapping[0], axisRZ_minus_mapping[1], true)
                lorieView.sendKeyEvent(axisRZ_plus_mapping[0], axisRZ_plus_mapping[1], false)

                lorieView.sendKeyEvent(axisZ_plus_mapping[0], axisZ_plus_mapping[1], false)
                lorieView.sendKeyEvent(axisZ_minus_mapping[0], axisZ_minus_mapping[1], false)

                return true
            }

            // Right/Down
            axisZ > deadZone && axisRZ > deadZone -> {
                lorieView.sendKeyEvent(axisZ_plus_mapping[0], axisZ_plus_mapping[1], true)
                lorieView.sendKeyEvent(axisZ_minus_mapping[0], axisZ_minus_mapping[1], false)

                lorieView.sendKeyEvent(axisRZ_plus_mapping[0], axisRZ_plus_mapping[1], true)
                lorieView.sendKeyEvent(axisRZ_minus_mapping[0], axisRZ_minus_mapping[1], false)

                return true
            }

            // Right/Up
            axisZ > deadZone && axisRZ < -deadZone -> {
                lorieView.sendKeyEvent(axisZ_plus_mapping[0], axisZ_plus_mapping[1], true)
                lorieView.sendKeyEvent(axisZ_minus_mapping[0], axisZ_minus_mapping[1], false)

                lorieView.sendKeyEvent(axisRZ_minus_mapping[0], axisRZ_minus_mapping[1], true)
                lorieView.sendKeyEvent(axisRZ_plus_mapping[0], axisRZ_plus_mapping[1], false)

                return true
            }

            // Left/Up
            axisZ < -deadZone && axisRZ < -deadZone -> {
                lorieView.sendKeyEvent(axisZ_minus_mapping[0], axisZ_minus_mapping[1], true)
                lorieView.sendKeyEvent(axisZ_plus_mapping[0], axisZ_plus_mapping[1], false)

                lorieView.sendKeyEvent(axisRZ_minus_mapping[0], axisRZ_minus_mapping[1], true)
                lorieView.sendKeyEvent(axisRZ_plus_mapping[0], axisRZ_plus_mapping[1], false)

                return true
            }

            // Left/Down
            axisZ < -deadZone && axisRZ > deadZone -> {
                lorieView.sendKeyEvent(axisX_plus_mapping[0], axisX_plus_mapping[1], false)
                lorieView.sendKeyEvent(axisX_minus_mapping[0], axisX_minus_mapping[1], true)

                lorieView.sendKeyEvent(axisRZ_plus_mapping[0], axisRZ_plus_mapping[1], true)
                lorieView.sendKeyEvent(axisRZ_minus_mapping[0], axisRZ_minus_mapping[1], false)

                return true
            }

            else -> {
                lorieView.sendKeyEvent(axisX_plus_mapping[0], axisX_plus_mapping[1], false)
                lorieView.sendKeyEvent(axisX_minus_mapping[0], axisX_minus_mapping[1], false)
                lorieView.sendKeyEvent(axisRZ_plus_mapping[0], axisRZ_plus_mapping[1], false)
                lorieView.sendKeyEvent(axisRZ_minus_mapping[0], axisRZ_minus_mapping[1], false)
            }
        }

        when {
            // Right
            axisHatX > deadZone && axisHatYNeutral -> {
                lorieView.sendKeyEvent(axisHatX_plus_mapping[0], axisHatX_plus_mapping[1], true)
                lorieView.sendKeyEvent(axisHatX_minus_mapping[0], axisHatX_minus_mapping[1], false)

                lorieView.sendKeyEvent(axisHatY_plus_mapping[0], axisHatY_plus_mapping[1], false)
                lorieView.sendKeyEvent(axisHatY_minus_mapping[0], axisHatY_minus_mapping[1], false)

                return true
            }

            // Left
            axisHatX < -deadZone && axisHatYNeutral -> {
                lorieView.sendKeyEvent(axisHatX_minus_mapping[0], axisHatX_minus_mapping[1], true)
                lorieView.sendKeyEvent(axisHatX_plus_mapping[0], axisHatX_plus_mapping[1], false)

                lorieView.sendKeyEvent(axisHatY_plus_mapping[0], axisHatY_plus_mapping[1], false)
                lorieView.sendKeyEvent(axisHatY_minus_mapping[0], axisHatY_minus_mapping[1], false)

                return true
            }

            // Down
            axisHatY > deadZone && axisHatXNeutral -> {
                lorieView.sendKeyEvent(axisHatY_plus_mapping[0], axisHatY_plus_mapping[1], true)
                lorieView.sendKeyEvent(axisHatY_minus_mapping[0], axisHatY_minus_mapping[1], false)

                lorieView.sendKeyEvent(axisHatX_plus_mapping[0], axisHatX_plus_mapping[1], false)
                lorieView.sendKeyEvent(axisHatX_minus_mapping[0], axisHatX_minus_mapping[1], false)

                return true
            }

            // Up
            axisHatY < -deadZone && axisHatXNeutral -> {
                lorieView.sendKeyEvent(axisHatY_minus_mapping[0], axisHatY_minus_mapping[1], true)
                lorieView.sendKeyEvent(axisHatY_plus_mapping[0], axisHatY_plus_mapping[1], false)

                lorieView.sendKeyEvent(axisHatX_plus_mapping[0], axisHatX_plus_mapping[1], false)
                lorieView.sendKeyEvent(axisHatX_minus_mapping[0], axisHatX_minus_mapping[1], false)

                return true
            }

            // Right/Down
            axisHatX > deadZone && axisHatY > deadZone -> {
                lorieView.sendKeyEvent(axisHatX_plus_mapping[0], axisHatX_plus_mapping[1], true)
                lorieView.sendKeyEvent(axisHatX_minus_mapping[0], axisHatX_minus_mapping[1], false)

                lorieView.sendKeyEvent(axisHatY_plus_mapping[0], axisHatY_plus_mapping[1], true)
                lorieView.sendKeyEvent(axisHatY_minus_mapping[0], axisHatY_minus_mapping[1], false)

                return true
            }

            // Right/Up
            axisHatX > deadZone && axisHatY < -deadZone -> {
                lorieView.sendKeyEvent(axisHatX_plus_mapping[0], axisHatX_plus_mapping[1], true)
                lorieView.sendKeyEvent(axisHatX_minus_mapping[0], axisHatX_minus_mapping[1], false)

                lorieView.sendKeyEvent(axisHatY_minus_mapping[0], axisHatY_minus_mapping[1], true)
                lorieView.sendKeyEvent(axisHatY_plus_mapping[0], axisHatY_plus_mapping[1], false)

                return true
            }

            // Left/Up
            axisHatX < -deadZone && axisHatY < -deadZone -> {
                lorieView.sendKeyEvent(axisHatX_minus_mapping[0], axisHatX_minus_mapping[1], true)
                lorieView.sendKeyEvent(axisHatX_plus_mapping[0], axisHatX_plus_mapping[1], false)

                lorieView.sendKeyEvent(axisHatY_minus_mapping[0], axisHatY_minus_mapping[1], true)
                lorieView.sendKeyEvent(axisHatY_plus_mapping[0], axisHatY_plus_mapping[1], false)

                return true
            }

            // Left/Down
            axisHatX < -deadZone && axisHatY > deadZone -> {
                lorieView.sendKeyEvent(axisHatX_minus_mapping[0], axisHatX_minus_mapping[1], true)
                lorieView.sendKeyEvent(axisHatX_plus_mapping[0], axisHatX_plus_mapping[1], false)

                lorieView.sendKeyEvent(axisHatY_plus_mapping[0], axisHatY_plus_mapping[1], true)
                lorieView.sendKeyEvent(axisHatY_minus_mapping[0], axisHatY_minus_mapping[1], false)

                return true
            }

            else -> {
                lorieView.sendKeyEvent(axisHatX_minus_mapping[0], axisHatX_minus_mapping[1], false)
                lorieView.sendKeyEvent(axisHatX_plus_mapping[0], axisHatX_plus_mapping[1], false)
                lorieView.sendKeyEvent(axisHatY_plus_mapping[0], axisHatY_plus_mapping[1], false)
                lorieView.sendKeyEvent(axisHatY_minus_mapping[0], axisHatY_minus_mapping[1], false)
            }
        }

        lorieView.sendKeyEvent(axisX_plus_mapping[0], axisX_plus_mapping[1], false)
        lorieView.sendKeyEvent(axisX_minus_mapping[0], axisX_minus_mapping[1], false)
        lorieView.sendKeyEvent(axisY_plus_mapping[0], axisY_plus_mapping[1], false)
        lorieView.sendKeyEvent(axisY_minus_mapping[0], axisY_minus_mapping[1], false)
        //
        lorieView.sendKeyEvent(axisZ_plus_mapping[0], axisZ_plus_mapping[1], false)
        lorieView.sendKeyEvent(axisZ_minus_mapping[0], axisZ_minus_mapping[1], false)
        lorieView.sendKeyEvent(axisRZ_plus_mapping[0], axisRZ_plus_mapping[1], false)
        lorieView.sendKeyEvent(axisRZ_minus_mapping[0], axisRZ_minus_mapping[1], false)
        //
        lorieView.sendKeyEvent(axisHatX_minus_mapping[0], axisHatX_minus_mapping[1], false)
        lorieView.sendKeyEvent(axisHatX_plus_mapping[0], axisHatX_plus_mapping[1], false)
        lorieView.sendKeyEvent(axisHatY_minus_mapping[0], axisHatY_minus_mapping[1], false)
        lorieView.sendKeyEvent(axisHatY_plus_mapping[0], axisHatY_plus_mapping[1], false)

        return false
    }
}