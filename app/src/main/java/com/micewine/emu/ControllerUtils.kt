package com.micewine.emu

import android.content.Context
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
import com.micewine.emu.activities.ControllerMapper.Companion.availableButtonMappings
import com.micewine.emu.overlay.XKeyCodes.getXKeyScanCodes

object ControllerUtils {
    private const val DEAD_ZONE = 0.25

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

    fun prepareButtonsAxisValues(context: Context) {
        val preferences = PreferenceManager.getDefaultSharedPreferences(context)!!

        buttonA_mapping = getXKeyScanCodes(preferences.getString(BUTTON_A_KEY, "Null")!!)
        buttonX_mapping = getXKeyScanCodes(preferences.getString(BUTTON_X_KEY, "Null")!!)
        buttonB_mapping = getXKeyScanCodes(preferences.getString(BUTTON_B_KEY, "Null")!!)
        buttonY_mapping = getXKeyScanCodes(preferences.getString(BUTTON_Y_KEY, "Null")!!)

        buttonR1_mapping = getXKeyScanCodes(preferences.getString(BUTTON_R1_KEY, "Null")!!)
        buttonR2_mapping = getXKeyScanCodes(preferences.getString(BUTTON_R2_KEY, "Null")!!)

        buttonL1_mapping = getXKeyScanCodes(preferences.getString(BUTTON_L1_KEY, "Null")!!)
        buttonL2_mapping = getXKeyScanCodes(preferences.getString(BUTTON_L2_KEY, "Null")!!)

        buttonStart_mapping = getXKeyScanCodes(preferences.getString(BUTTON_START_KEY, "Null")!!)
        buttonSelect_mapping = getXKeyScanCodes(preferences.getString(BUTTON_SELECT_KEY, "Null")!!)

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

    fun checkControllerButtons(lorieView: LorieView, e: KeyEvent): Boolean {
        val pressed = e.action == KeyEvent.ACTION_DOWN

        return when (e.keyCode) {
            KEYCODE_BUTTON_Y -> {
                lorieView.sendKeyEvent(buttonY_mapping[0], buttonY_mapping[1], pressed)

                true
            }

            KEYCODE_BUTTON_A -> {
                lorieView.sendKeyEvent(buttonA_mapping[0], buttonA_mapping[1], pressed)

                true
            }

            KEYCODE_BUTTON_B -> {
                lorieView.sendKeyEvent(buttonB_mapping[0], buttonB_mapping[1], pressed)

                true
            }

            KEYCODE_BUTTON_X -> {
                lorieView.sendKeyEvent(buttonX_mapping[0], buttonX_mapping[1], pressed)

                true
            }

            KEYCODE_BUTTON_START -> {
                lorieView.sendKeyEvent(buttonStart_mapping[0], buttonStart_mapping[1], pressed)

                true
            }

            KEYCODE_BUTTON_SELECT -> {
                lorieView.sendKeyEvent(buttonSelect_mapping[0], buttonSelect_mapping[1], pressed)

                true
            }

            KEYCODE_BUTTON_R1 -> {
                lorieView.sendKeyEvent(buttonR1_mapping[0], buttonR1_mapping[1], pressed)

                true
            }

            KEYCODE_BUTTON_R2 -> {
                lorieView.sendKeyEvent(buttonR2_mapping[0], buttonR2_mapping[1], pressed)

                true
            }

            KEYCODE_BUTTON_L1 -> {
                lorieView.sendKeyEvent(buttonL1_mapping[0], buttonL1_mapping[1], pressed)

                true
            }

            KEYCODE_BUTTON_L2 -> {
                lorieView.sendKeyEvent(buttonL2_mapping[0], buttonL2_mapping[1], pressed)

                true
            }

            else -> false
        }
    }

    fun checkControllerAxis(lorieView: LorieView, event: MotionEvent): Boolean {
        val axisX = event.getAxisValue(AXIS_X)
        val axisY = event.getAxisValue(AXIS_Y)

        val axisZ = event.getAxisValue(AXIS_Z)
        val axisRZ = event.getAxisValue(AXIS_RZ)

        val axisHatX = event.getAxisValue(AXIS_HAT_X)
        val axisHatY = event.getAxisValue(AXIS_HAT_Y)

        return when {
            axisX > DEAD_ZONE -> {
                lorieView.sendKeyEvent(axisX_plus_mapping[0], axisX_plus_mapping[1], true)
                lorieView.sendKeyEvent(axisX_minus_mapping[0], axisX_minus_mapping[1], false)

                true
            }

            axisX < -DEAD_ZONE -> {
                lorieView.sendKeyEvent(axisX_minus_mapping[0], axisX_minus_mapping[1], true)
                lorieView.sendKeyEvent(axisX_plus_mapping[0], axisX_plus_mapping[1], false)

                true
            }

            axisY > DEAD_ZONE -> {
                lorieView.sendKeyEvent(axisY_plus_mapping[0], axisY_plus_mapping[1], true)
                lorieView.sendKeyEvent(axisY_minus_mapping[0], axisY_minus_mapping[1], false)

                true
            }

            axisY < -DEAD_ZONE -> {
                lorieView.sendKeyEvent(axisY_minus_mapping[0], axisY_minus_mapping[1], true)
                lorieView.sendKeyEvent(axisY_plus_mapping[0], axisY_plus_mapping[1], false)

                true
            }

            //

            axisZ > DEAD_ZONE -> {
                lorieView.sendKeyEvent(axisZ_plus_mapping[0], axisZ_plus_mapping[1], true)
                lorieView.sendKeyEvent(axisZ_minus_mapping[0], axisZ_minus_mapping[1], false)

                true
            }

            axisZ < -DEAD_ZONE -> {
                lorieView.sendKeyEvent(axisZ_minus_mapping[0], axisZ_minus_mapping[1], true)
                lorieView.sendKeyEvent(axisZ_plus_mapping[0], axisZ_plus_mapping[1], false)

                true
            }

            axisRZ > DEAD_ZONE -> {
                lorieView.sendKeyEvent(axisRZ_plus_mapping[0], axisRZ_plus_mapping[1], true)
                lorieView.sendKeyEvent(axisRZ_minus_mapping[0], axisRZ_minus_mapping[1], false)

                true
            }

            axisRZ < -DEAD_ZONE -> {
                lorieView.sendKeyEvent(axisRZ_minus_mapping[0], axisRZ_minus_mapping[1], true)
                lorieView.sendKeyEvent(axisRZ_plus_mapping[0], axisRZ_plus_mapping[1], false)

                true
            }

            //

            axisHatX > DEAD_ZONE -> {
                lorieView.sendKeyEvent(axisHatX_plus_mapping[0], axisHatX_plus_mapping[1], true)
                lorieView.sendKeyEvent(axisHatX_minus_mapping[0], axisHatX_minus_mapping[1], false)

                true
            }

            axisHatX < -DEAD_ZONE -> {
                lorieView.sendKeyEvent(axisHatX_minus_mapping[0], axisHatX_minus_mapping[1], true)
                lorieView.sendKeyEvent(axisHatX_plus_mapping[0], axisHatX_plus_mapping[1], false)

                true
            }

            axisHatY > DEAD_ZONE -> {
                lorieView.sendKeyEvent(axisHatY_plus_mapping[0], axisHatY_plus_mapping[1], true)
                lorieView.sendKeyEvent(axisHatY_minus_mapping[0], axisHatY_minus_mapping[1], false)

                true
            }

            axisHatY < -DEAD_ZONE -> {
                lorieView.sendKeyEvent(axisHatY_minus_mapping[0], axisHatY_minus_mapping[1], true)
                lorieView.sendKeyEvent(axisHatY_plus_mapping[0], axisHatY_plus_mapping[1], false)

                true
            }

            else -> {
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

                false
            }
        }
    }
}