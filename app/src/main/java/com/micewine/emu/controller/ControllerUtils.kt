package com.micewine.emu.controller

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
import android.view.KeyEvent.KEYCODE_BUTTON_THUMBL
import android.view.KeyEvent.KEYCODE_BUTTON_THUMBR
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
import com.micewine.emu.LorieView
import com.micewine.emu.activities.ControllerMapperActivity.Companion.AXIS_HAT_X_MINUS_KEY
import com.micewine.emu.activities.ControllerMapperActivity.Companion.AXIS_HAT_X_PLUS_KEY
import com.micewine.emu.activities.ControllerMapperActivity.Companion.AXIS_HAT_Y_MINUS_KEY
import com.micewine.emu.activities.ControllerMapperActivity.Companion.AXIS_HAT_Y_PLUS_KEY
import com.micewine.emu.activities.ControllerMapperActivity.Companion.AXIS_RZ_MINUS_KEY
import com.micewine.emu.activities.ControllerMapperActivity.Companion.AXIS_RZ_PLUS_KEY
import com.micewine.emu.activities.ControllerMapperActivity.Companion.AXIS_X_MINUS_KEY
import com.micewine.emu.activities.ControllerMapperActivity.Companion.AXIS_X_PLUS_KEY
import com.micewine.emu.activities.ControllerMapperActivity.Companion.AXIS_Y_MINUS_KEY
import com.micewine.emu.activities.ControllerMapperActivity.Companion.AXIS_Y_PLUS_KEY
import com.micewine.emu.activities.ControllerMapperActivity.Companion.AXIS_Z_MINUS_KEY
import com.micewine.emu.activities.ControllerMapperActivity.Companion.AXIS_Z_PLUS_KEY
import com.micewine.emu.activities.ControllerMapperActivity.Companion.BUTTON_A_KEY
import com.micewine.emu.activities.ControllerMapperActivity.Companion.BUTTON_B_KEY
import com.micewine.emu.activities.ControllerMapperActivity.Companion.BUTTON_L1_KEY
import com.micewine.emu.activities.ControllerMapperActivity.Companion.BUTTON_L2_KEY
import com.micewine.emu.activities.ControllerMapperActivity.Companion.BUTTON_R1_KEY
import com.micewine.emu.activities.ControllerMapperActivity.Companion.BUTTON_R2_KEY
import com.micewine.emu.activities.ControllerMapperActivity.Companion.BUTTON_SELECT_KEY
import com.micewine.emu.activities.ControllerMapperActivity.Companion.BUTTON_START_KEY
import com.micewine.emu.activities.ControllerMapperActivity.Companion.BUTTON_THUMBL_KEY
import com.micewine.emu.activities.ControllerMapperActivity.Companion.BUTTON_THUMBR_KEY
import com.micewine.emu.activities.ControllerMapperActivity.Companion.BUTTON_X_KEY
import com.micewine.emu.activities.ControllerMapperActivity.Companion.BUTTON_Y_KEY
import com.micewine.emu.activities.ControllerMapperActivity.Companion.SELECTED_CONTROLLER_PRESET_KEY
import com.micewine.emu.controller.XKeyCodes.getXKeyScanCodes
import com.micewine.emu.fragments.ControllerPresetManagerFragment.Companion.getDeadZone
import com.micewine.emu.fragments.ControllerPresetManagerFragment.Companion.getMapping
import com.micewine.emu.fragments.ControllerPresetManagerFragment.Companion.getMouseSensibility
import com.micewine.emu.input.InputStub.BUTTON_LEFT
import com.micewine.emu.input.InputStub.BUTTON_MIDDLE
import com.micewine.emu.input.InputStub.BUTTON_RIGHT
import com.micewine.emu.input.InputStub.BUTTON_UNDEFINED
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.absoluteValue

object ControllerUtils {
    const val KEYBOARD = 0
    const val MOUSE = 1

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
    private lateinit var buttonThumbR_mapping: List<Int>
    private lateinit var buttonThumbL_mapping: List<Int>

    private var deadZone: Float = 0F
    private var moveVMouse: Int? = null
    private var mouseSensibility: Float = 0F
    private var axisXVelocity: Float = 0F
    private var axisYVelocity: Float = 0F

    private const val LEFT = 1
    private const val RIGHT = 2
    private const val UP = 3
    private const val DOWN = 4
    private const val LEFT_UP = 5
    private const val LEFT_DOWN = 6
    private const val RIGHT_UP = 7
    private const val RIGHT_DOWN = 8

    private fun detectKey(context: Context, key: String): List<Int> {
        val preferences = PreferenceManager.getDefaultSharedPreferences(context)!!
        val mapping =  getMapping(preferences.getString(SELECTED_CONTROLLER_PRESET_KEY, "default")!!, key)
        val keyList: List<Int>

        Log.v("AAAAA", mapping[0])

        when (mapping[0]) {
            "M_Left" -> {
                keyList = listOf(BUTTON_LEFT, BUTTON_LEFT, MOUSE)
            }

            "M_Middle" -> {
                keyList = listOf(BUTTON_MIDDLE, BUTTON_MIDDLE, MOUSE)
            }

            "M_Right" -> {
                keyList = listOf(BUTTON_RIGHT, BUTTON_RIGHT, MOUSE)
            }

            "Mouse" -> {
                keyList = listOf(MOUSE, MOUSE, MOUSE)
            }

            else -> {
                keyList = getXKeyScanCodes(mapping[0])
            }
        }

        return keyList
    }

    fun prepareButtonsAxisValues(context: Context) {
        val preferences = PreferenceManager.getDefaultSharedPreferences(context)!!

        buttonA_mapping = detectKey(context, BUTTON_A_KEY)
        buttonX_mapping = detectKey(context, BUTTON_X_KEY)
        buttonB_mapping = detectKey(context, BUTTON_B_KEY)
        buttonY_mapping = detectKey(context, BUTTON_Y_KEY)

        buttonR1_mapping = detectKey(context, BUTTON_R1_KEY)
        buttonR2_mapping = detectKey(context, BUTTON_R2_KEY)

        buttonL1_mapping = detectKey(context, BUTTON_L1_KEY)
        buttonL2_mapping = detectKey(context, BUTTON_L2_KEY)

        buttonThumbL_mapping = detectKey(context, BUTTON_THUMBL_KEY)
        buttonThumbR_mapping = detectKey(context, BUTTON_THUMBR_KEY)

        buttonStart_mapping = detectKey(context, BUTTON_START_KEY)
        buttonSelect_mapping = detectKey(context, BUTTON_SELECT_KEY)

        axisX_plus_mapping = detectKey(context, AXIS_X_PLUS_KEY)
        axisX_minus_mapping = detectKey(context, AXIS_X_MINUS_KEY)

        axisY_plus_mapping = detectKey(context, AXIS_Y_PLUS_KEY)
        axisY_minus_mapping = detectKey(context, AXIS_Y_MINUS_KEY)

        axisZ_plus_mapping = detectKey(context, AXIS_Z_PLUS_KEY)
        axisZ_minus_mapping = detectKey(context, AXIS_Z_MINUS_KEY)

        axisRZ_plus_mapping = detectKey(context, AXIS_RZ_PLUS_KEY)
        axisRZ_minus_mapping = detectKey(context, AXIS_RZ_MINUS_KEY)

        axisHatX_plus_mapping = detectKey(context, AXIS_HAT_X_PLUS_KEY)
        axisHatX_minus_mapping = detectKey(context, AXIS_HAT_X_MINUS_KEY)

        axisHatY_plus_mapping = detectKey(context, AXIS_HAT_Y_PLUS_KEY)
        axisHatY_minus_mapping = detectKey(context, AXIS_HAT_Y_MINUS_KEY)

        deadZone = getDeadZone(preferences.getString(SELECTED_CONTROLLER_PRESET_KEY, "default")!!).toFloat() / 100
        mouseSensibility = getMouseSensibility(preferences.getString(SELECTED_CONTROLLER_PRESET_KEY, "default")!!).toFloat() / 100
    }

    private fun getGameControllerIds(): List<Int> {
        val gameControllerDeviceIds = mutableListOf<Int>()
        val deviceIds = InputDevice.getDeviceIds()
        deviceIds.forEach { deviceId ->
            InputDevice.getDevice(deviceId)?.apply {
                if (sources and InputDevice.SOURCE_GAMEPAD == InputDevice.SOURCE_GAMEPAD
                    || sources and InputDevice.SOURCE_JOYSTICK == InputDevice.SOURCE_JOYSTICK) {
                    gameControllerDeviceIds.takeIf { !it.contains(deviceId) }?.add(deviceId)
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
            KEYBOARD -> lorieView.sendKeyEvent(mapping[0], mapping[1], pressed)
            MOUSE -> lorieView.sendMouseEvent(0F, 0F, mapping[0], pressed, true)
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

            KEYCODE_BUTTON_THUMBR -> {
                handleKey(lorieView, pressed, buttonThumbR_mapping)

                true
            }

            KEYCODE_BUTTON_THUMBL -> {
                handleKey(lorieView, pressed, buttonThumbL_mapping)

                true
            }

            else -> false
        }
    }

    suspend fun controllerMouseEmulation(lorieView: LorieView) {
        withContext(Dispatchers.IO) {
            while (true) {
                when (moveVMouse) {
                    LEFT -> {
                        lorieView.sendMouseEvent(
                            -10F * (axisXVelocity * mouseSensibility),
                            0F,
                            BUTTON_UNDEFINED,
                            false,
                            true
                        )
                    }

                    RIGHT -> {
                        lorieView.sendMouseEvent(
                            10F * (axisXVelocity * mouseSensibility),
                            0F,
                            BUTTON_UNDEFINED,
                            false,
                            true
                        )
                    }

                    UP -> {
                        lorieView.sendMouseEvent(
                            0F,
                            -10F * (axisYVelocity * mouseSensibility),
                            BUTTON_UNDEFINED,
                            false,
                            true
                        )
                    }

                    DOWN -> {
                        lorieView.sendMouseEvent(
                            0F,
                            10F * (axisYVelocity * mouseSensibility),
                            BUTTON_UNDEFINED,
                            false,
                            true
                        )
                    }

                    LEFT_UP -> {
                        lorieView.sendMouseEvent(
                            -10F * (axisXVelocity * mouseSensibility),
                            -10F * (axisYVelocity * mouseSensibility),
                            BUTTON_UNDEFINED,
                            false,
                            true
                        )
                    }

                    LEFT_DOWN -> {
                        lorieView.sendMouseEvent(
                            -10F * (axisXVelocity * mouseSensibility),
                            10F * (axisYVelocity * mouseSensibility),
                            BUTTON_UNDEFINED,
                            false,
                            true
                        )
                    }

                    RIGHT_UP -> {
                        lorieView.sendMouseEvent(
                            10F * (axisXVelocity * mouseSensibility),
                            -10F * (axisYVelocity * mouseSensibility),
                            BUTTON_UNDEFINED,
                            false,
                            true
                        )
                    }

                    RIGHT_DOWN -> {
                        lorieView.sendMouseEvent(
                            10F * (axisXVelocity * mouseSensibility),
                            10F * (axisYVelocity * mouseSensibility),
                            BUTTON_UNDEFINED,
                            false,
                            true
                        )
                    }
                }

                Thread.sleep(16)
            }
        }
    }

    private fun checkMouse(axisX: Float, axisY: Float, orientation: Int) {
        moveVMouse = orientation

        axisXVelocity = axisX.absoluteValue
        axisYVelocity = axisY.absoluteValue
    }

    fun handleAxis(lorieView: LorieView, axisX: Float, axisY: Float, axisXNeutral: Boolean, axisYNeutral: Boolean, axisXPlusMapping: List<Int>, axisXMinusMapping: List<Int>, axisYPlusMapping: List<Int>, axisYMinusMapping: List<Int>, deadZone: Float): Boolean {
        return when {
            // Left
            axisX < -deadZone && axisYNeutral -> {
                if (axisXMinusMapping[2] == KEYBOARD) {
                    lorieView.sendKeyEvent(axisXPlusMapping[0], axisXPlusMapping[1], false)
                    lorieView.sendKeyEvent(axisXMinusMapping[0], axisXMinusMapping[1], true)

                    lorieView.sendKeyEvent(axisYPlusMapping[0], axisYPlusMapping[1], false)
                    lorieView.sendKeyEvent(axisYMinusMapping[0], axisYMinusMapping[1], false)
                } else {
                    checkMouse(axisX, axisY, LEFT)
                }

                true
            }

            // Right
            axisX > deadZone && axisYNeutral -> {
                if (axisXPlusMapping[2] == KEYBOARD) {
                    lorieView.sendKeyEvent(axisXPlusMapping[0], axisXPlusMapping[1], true)
                    lorieView.sendKeyEvent(axisXMinusMapping[0], axisXMinusMapping[1], false)

                    lorieView.sendKeyEvent(axisYPlusMapping[0], axisYPlusMapping[1], false)
                    lorieView.sendKeyEvent(axisYMinusMapping[0], axisYMinusMapping[1], false)
                } else {
                    checkMouse(axisX, axisY, RIGHT)
                }

                true
            }

            // Up
            axisY < -deadZone && axisXNeutral -> {
                if (axisYMinusMapping[2] == KEYBOARD) {
                    lorieView.sendKeyEvent(axisXPlusMapping[0], axisXPlusMapping[1], false)
                    lorieView.sendKeyEvent(axisXMinusMapping[0], axisXMinusMapping[1], false)

                    lorieView.sendKeyEvent(axisYPlusMapping[0], axisYPlusMapping[1], false)
                    lorieView.sendKeyEvent(axisYMinusMapping[0], axisYMinusMapping[1], true)
                } else {
                    checkMouse(axisX, axisY, UP)
                }

                true
            }

            // Down
            axisY > deadZone && axisXNeutral -> {
                if (axisYPlusMapping[2] == KEYBOARD) {
                    lorieView.sendKeyEvent(axisXPlusMapping[0], axisXPlusMapping[1], false)
                    lorieView.sendKeyEvent(axisXMinusMapping[0], axisXMinusMapping[1], false)

                    lorieView.sendKeyEvent(axisYPlusMapping[0], axisYPlusMapping[1], true)
                    lorieView.sendKeyEvent(axisYMinusMapping[0], axisYMinusMapping[1], false)
                } else {
                    checkMouse(axisX, axisY, DOWN)
                }

                true
            }

            // Left/Up
            axisX < -deadZone && axisY < -deadZone -> {
                if (axisXPlusMapping[2] == KEYBOARD && axisYMinusMapping[2] == KEYBOARD) {
                    lorieView.sendKeyEvent(axisXPlusMapping[0], axisXPlusMapping[1], false)
                    lorieView.sendKeyEvent(axisXMinusMapping[0], axisXMinusMapping[1], true)

                    lorieView.sendKeyEvent(axisYPlusMapping[0], axisYPlusMapping[1], false)
                    lorieView.sendKeyEvent(axisYMinusMapping[0], axisYMinusMapping[1], true)
                } else {
                    checkMouse(axisX, axisY, LEFT_UP)
                }

                true
            }

            // Left/Down
            axisX < -deadZone && axisY > deadZone -> {
                if (axisXPlusMapping[2] == KEYBOARD && axisYMinusMapping[2] == KEYBOARD) {
                    lorieView.sendKeyEvent(axisXPlusMapping[0], axisXPlusMapping[1], false)
                    lorieView.sendKeyEvent(axisXMinusMapping[0], axisXMinusMapping[1], true)

                    lorieView.sendKeyEvent(axisYPlusMapping[0], axisYPlusMapping[1], true)
                    lorieView.sendKeyEvent(axisYMinusMapping[0], axisYMinusMapping[1], false)
                } else {
                    checkMouse(axisX, axisY, LEFT_DOWN)
                }

                true
            }

            // Right/Up
            axisX > deadZone && axisY < -deadZone -> {
                if (axisXPlusMapping[2] == KEYBOARD && axisYMinusMapping[2] == KEYBOARD) {
                    lorieView.sendKeyEvent(axisXPlusMapping[0], axisXPlusMapping[1], true)
                    lorieView.sendKeyEvent(axisXMinusMapping[0], axisXMinusMapping[1], false)

                    lorieView.sendKeyEvent(axisYPlusMapping[0], axisYPlusMapping[1], false)
                    lorieView.sendKeyEvent(axisYMinusMapping[0], axisYMinusMapping[1], true)
                } else {
                    checkMouse(axisX, axisY, RIGHT_UP)
                }

                true
            }

            // Right/Down
            axisX > deadZone && axisY > deadZone -> {
                if (axisXPlusMapping[2] == KEYBOARD && axisYMinusMapping[2] == KEYBOARD) {
                    lorieView.sendKeyEvent(axisXPlusMapping[0], axisXPlusMapping[1], true)
                    lorieView.sendKeyEvent(axisXMinusMapping[0], axisXMinusMapping[1], false)

                    lorieView.sendKeyEvent(axisYPlusMapping[0], axisYPlusMapping[1], true)
                    lorieView.sendKeyEvent(axisYMinusMapping[0], axisYMinusMapping[1], false)
                } else {
                    checkMouse(axisX, axisY, RIGHT_DOWN)
                }

                true
            }
            else -> {
                if (axisXPlusMapping[2] == KEYBOARD &&
                    axisXMinusMapping[2] == KEYBOARD &&
                    axisYPlusMapping[2] == KEYBOARD &&
                    axisYMinusMapping[2] == KEYBOARD) {

                    lorieView.sendKeyEvent(axisXPlusMapping[0], axisXPlusMapping[1], false)
                    lorieView.sendKeyEvent(axisXMinusMapping[0], axisXMinusMapping[1], false)
                    lorieView.sendKeyEvent(axisYPlusMapping[0], axisYPlusMapping[1], false)
                    lorieView.sendKeyEvent(axisYMinusMapping[0], axisYMinusMapping[1], false)
                } else {
                    moveVMouse = null
                }

                false
            }
        }
    }

    fun checkControllerAxis(lorieView: LorieView, event: MotionEvent) {
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

        handleAxis(lorieView, axisX, axisY, axisXNeutral, axisYNeutral, axisX_plus_mapping, axisX_minus_mapping, axisY_plus_mapping, axisY_minus_mapping, deadZone)
        handleAxis(lorieView, axisZ, axisRZ, axisZNeutral, axisRZNeutral, axisZ_plus_mapping, axisZ_minus_mapping, axisRZ_plus_mapping, axisRZ_minus_mapping, deadZone)
        handleAxis(lorieView, axisHatX, axisHatY, axisHatXNeutral, axisHatYNeutral, axisHatX_plus_mapping, axisHatX_minus_mapping, axisHatY_plus_mapping, axisHatY_minus_mapping, deadZone)    }
}