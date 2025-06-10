package com.micewine.emu.controller

import android.content.Context
import android.util.Log
import android.view.InputDevice
import android.view.InputDevice.MotionRange
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
import android.view.MotionEvent.AXIS_LTRIGGER
import android.view.MotionEvent.AXIS_RTRIGGER
import android.view.MotionEvent.AXIS_RZ
import android.view.MotionEvent.AXIS_X
import android.view.MotionEvent.AXIS_Y
import android.view.MotionEvent.AXIS_Z
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.micewine.emu.LorieView
import com.micewine.emu.activities.EmulationActivity.Companion.sharedLogs
import com.micewine.emu.activities.PresetManagerActivity.Companion.AXIS_HAT_X_MINUS_KEY
import com.micewine.emu.activities.PresetManagerActivity.Companion.AXIS_HAT_X_PLUS_KEY
import com.micewine.emu.activities.PresetManagerActivity.Companion.AXIS_HAT_Y_MINUS_KEY
import com.micewine.emu.activities.PresetManagerActivity.Companion.AXIS_HAT_Y_PLUS_KEY
import com.micewine.emu.activities.PresetManagerActivity.Companion.AXIS_RZ_MINUS_KEY
import com.micewine.emu.activities.PresetManagerActivity.Companion.AXIS_RZ_PLUS_KEY
import com.micewine.emu.activities.PresetManagerActivity.Companion.AXIS_X_MINUS_KEY
import com.micewine.emu.activities.PresetManagerActivity.Companion.AXIS_X_PLUS_KEY
import com.micewine.emu.activities.PresetManagerActivity.Companion.AXIS_Y_MINUS_KEY
import com.micewine.emu.activities.PresetManagerActivity.Companion.AXIS_Y_PLUS_KEY
import com.micewine.emu.activities.PresetManagerActivity.Companion.AXIS_Z_MINUS_KEY
import com.micewine.emu.activities.PresetManagerActivity.Companion.AXIS_Z_PLUS_KEY
import com.micewine.emu.activities.PresetManagerActivity.Companion.BUTTON_A_KEY
import com.micewine.emu.activities.PresetManagerActivity.Companion.BUTTON_B_KEY
import com.micewine.emu.activities.PresetManagerActivity.Companion.BUTTON_L1_KEY
import com.micewine.emu.activities.PresetManagerActivity.Companion.BUTTON_L2_KEY
import com.micewine.emu.activities.PresetManagerActivity.Companion.BUTTON_R1_KEY
import com.micewine.emu.activities.PresetManagerActivity.Companion.BUTTON_R2_KEY
import com.micewine.emu.activities.PresetManagerActivity.Companion.BUTTON_SELECT_KEY
import com.micewine.emu.activities.PresetManagerActivity.Companion.BUTTON_START_KEY
import com.micewine.emu.activities.PresetManagerActivity.Companion.BUTTON_THUMBL_KEY
import com.micewine.emu.activities.PresetManagerActivity.Companion.BUTTON_THUMBR_KEY
import com.micewine.emu.activities.PresetManagerActivity.Companion.BUTTON_X_KEY
import com.micewine.emu.activities.PresetManagerActivity.Companion.BUTTON_Y_KEY
import com.micewine.emu.adapters.AdapterGame.Companion.selectedGameName
import com.micewine.emu.controller.XKeyCodes.ButtonMapping
import com.micewine.emu.fragments.ControllerPresetManagerFragment.Companion.getDeadZone
import com.micewine.emu.fragments.ControllerPresetManagerFragment.Companion.getControllerPreset
import com.micewine.emu.fragments.ControllerPresetManagerFragment.Companion.getMouseSensibility
import com.micewine.emu.fragments.ShortcutsFragment.Companion.getControllerPreset
import com.micewine.emu.fragments.ShortcutsFragment.Companion.getControllerXInput
import com.micewine.emu.fragments.ShortcutsFragment.Companion.getControllerXInputSwapAnalogs
import com.micewine.emu.input.InputStub.BUTTON_LEFT
import com.micewine.emu.input.InputStub.BUTTON_MIDDLE
import com.micewine.emu.input.InputStub.BUTTON_RIGHT
import com.micewine.emu.input.InputStub.BUTTON_UNDEFINED
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.nio.ByteBuffer
import kotlin.math.absoluteValue

object ControllerUtils {
    const val KEYBOARD = 0
    const val MOUSE = 1

    private var virtualMouseMovingState: Int? = null
    private var axisXVelocity: Float = 0F
    private var axisYVelocity: Float = 0F
    private var controllerIndex = 0
    private lateinit var lorieView: LorieView
    const val UP = 1
    const val RIGHT_UP = 2
    const val RIGHT = 3
    const val RIGHT_DOWN = 4
    const val DOWN = 5
    const val LEFT_DOWN = 6
    const val LEFT = 7
    const val LEFT_UP = 8
    const val SCROLL_UP = 9
    const val SCROLL_DOWN = 10

    fun initialize(context: Context) {
        lorieView = LorieView(context)
    }

    suspend fun controllerMouseEmulation() {
        withContext(Dispatchers.IO) {
            while (true) {
                val mouseSensibility = getMouseSensibility(getControllerPreset(selectedGameName, controllerIndex)).toFloat() / 100

                when (virtualMouseMovingState) {
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

                delay(20)
            }
        }
    }

    private fun detectKey(presetName: String?, key: String): ButtonMapping {
        val preset = if (presetName == "--") "default" else presetName ?: "default"
        val controllerPreset = getControllerPreset(preset, key)

        return when (controllerPreset?.name) {
            "M_Left" -> ButtonMapping("M_Left", BUTTON_LEFT, BUTTON_LEFT, MOUSE)
            "M_Middle" -> ButtonMapping("M_Middle", BUTTON_MIDDLE, BUTTON_MIDDLE, MOUSE)
            "M_Right" -> ButtonMapping("M_Right", BUTTON_RIGHT, BUTTON_RIGHT, MOUSE)
            "M_WheelUp" -> ButtonMapping("M_WheelUp", SCROLL_UP, SCROLL_UP, MOUSE)
            "M_WheelDown" -> ButtonMapping("M_WheelDown", SCROLL_DOWN, SCROLL_DOWN, MOUSE)
            "Mouse" -> ButtonMapping("Mouse", MOUSE, MOUSE, MOUSE)
            else -> getControllerPreset(preset, key) ?: ButtonMapping()
        }
    }

    fun prepareControllersMappings() {
        connectedPhysicalControllers.forEachIndexed { index, it ->
            val presetName = getControllerPreset(selectedGameName, index)

            if (getControllerXInput(selectedGameName, index)) {
                it.mappingType = MAPPING_TYPE_XINPUT
                it.swapAnalogs = getControllerXInputSwapAnalogs(selectedGameName, index)

                if (it.virtualControllerID == -1) it.virtualControllerID = connectController()
            } else {
                it.mappingType = MAPPING_TYPE_KEYBOARD_MOUSE

                it.keyboardMapping.aButton = detectKey(presetName, BUTTON_A_KEY)
                it.keyboardMapping.bButton = detectKey(presetName, BUTTON_B_KEY)
                it.keyboardMapping.xButton = detectKey(presetName, BUTTON_X_KEY)
                it.keyboardMapping.yButton = detectKey(presetName, BUTTON_Y_KEY)

                it.keyboardMapping.rbButton = detectKey(presetName, BUTTON_R1_KEY)
                it.keyboardMapping.rtButton = detectKey(presetName, BUTTON_R2_KEY)

                it.keyboardMapping.lbButton = detectKey(presetName, BUTTON_L1_KEY)
                it.keyboardMapping.ltButton = detectKey(presetName, BUTTON_L2_KEY)

                it.keyboardMapping.lsButton = detectKey(presetName, BUTTON_THUMBL_KEY)
                it.keyboardMapping.rsButton = detectKey(presetName, BUTTON_THUMBR_KEY)

                it.keyboardMapping.startButton = detectKey(presetName, BUTTON_START_KEY)
                it.keyboardMapping.selectButton = detectKey(presetName, BUTTON_SELECT_KEY)

                it.keyboardMapping.leftAnalog.up = detectKey(presetName, AXIS_Y_MINUS_KEY)
                it.keyboardMapping.leftAnalog.down = detectKey(presetName, AXIS_Y_PLUS_KEY)
                it.keyboardMapping.leftAnalog.left = detectKey(presetName, AXIS_X_MINUS_KEY)
                it.keyboardMapping.leftAnalog.right = detectKey(presetName, AXIS_X_PLUS_KEY)

                it.keyboardMapping.leftAnalog.isMouseMapping = listOf(
                    detectKey(presetName, AXIS_Y_MINUS_KEY),
                    detectKey(presetName, AXIS_Y_PLUS_KEY),
                    detectKey(presetName, AXIS_X_MINUS_KEY),
                    detectKey(presetName, AXIS_X_PLUS_KEY)
                ).any { it.type == MOUSE }

                it.keyboardMapping.rightAnalog.up = detectKey(presetName, AXIS_Z_MINUS_KEY)
                it.keyboardMapping.rightAnalog.down = detectKey(presetName, AXIS_Z_PLUS_KEY)
                it.keyboardMapping.rightAnalog.left = detectKey(presetName, AXIS_RZ_MINUS_KEY)
                it.keyboardMapping.rightAnalog.right = detectKey(presetName, AXIS_RZ_PLUS_KEY)

                it.keyboardMapping.rightAnalog.isMouseMapping = listOf(
                    detectKey(presetName, AXIS_Z_MINUS_KEY),
                    detectKey(presetName, AXIS_Z_PLUS_KEY),
                    detectKey(presetName, AXIS_RZ_MINUS_KEY),
                    detectKey(presetName, AXIS_RZ_PLUS_KEY)
                ).any { it.type == MOUSE }

                it.keyboardMapping.dPad.up = detectKey(presetName, AXIS_HAT_Y_MINUS_KEY)
                it.keyboardMapping.dPad.down = detectKey(presetName, AXIS_HAT_Y_PLUS_KEY)
                it.keyboardMapping.dPad.left = detectKey(presetName, AXIS_HAT_X_MINUS_KEY)
                it.keyboardMapping.dPad.right = detectKey(presetName, AXIS_HAT_X_PLUS_KEY)

                it.keyboardMapping.dPad.isMouseMapping = listOf(
                    detectKey(presetName, AXIS_HAT_Y_MINUS_KEY),
                    detectKey(presetName, AXIS_HAT_Y_PLUS_KEY),
                    detectKey(presetName, AXIS_HAT_X_MINUS_KEY),
                    detectKey(presetName, AXIS_HAT_X_PLUS_KEY)
                ).any { it.type == MOUSE }

                it.deadZone = getDeadZone(presetName).toFloat() / 100F
                it.mouseSensibility = getMouseSensibility(presetName).toFloat() / 100F
            }
        }
    }

    fun getConnectedControllers(): MutableList<PhysicalController> {
        val deviceIds = InputDevice.getDeviceIds()
        val devices = mutableListOf<PhysicalController>()

        deviceIds.forEach { deviceId ->
            InputDevice.getDevice(deviceId)?.let { device ->
                if (((device.sources and InputDevice.SOURCE_GAMEPAD == InputDevice.SOURCE_GAMEPAD) ||
                    (device.sources and InputDevice.SOURCE_JOYSTICK == InputDevice.SOURCE_JOYSTICK)) &&
                    (!device.name.contains("uinput"))
                ) {
                    devices.add(
                        PhysicalController(
                            device.name,
                            deviceId,
                            -1,
                            -1,
                            device.motionRanges.any { it.axis == AXIS_LTRIGGER } && device.motionRanges.any { it.axis == AXIS_RTRIGGER }
                        )
                    )
                }
            }
        }

        return devices
    }

    fun handleKey(pressed: Boolean, mapping: ButtonMapping) {
        when (mapping.type) {
            KEYBOARD -> lorieView.sendKeyEvent(mapping.scanCode, mapping.keyCode, pressed)
            MOUSE -> {
                when (mapping.scanCode) {
                    SCROLL_UP -> {
                        if (pressed) lorieView.sendMouseWheelEvent(0F, -10F)
                    }
                    SCROLL_DOWN -> {
                        if (pressed) lorieView.sendMouseWheelEvent(0F, 10F)
                    }
                    else -> {
                        lorieView.sendMouseEvent(0F, 0F, mapping.scanCode, pressed, true)
                    }
                }
            }
        }
    }

    fun updateButtonsState(e: KeyEvent): Boolean {
        val pressed = e.action == KeyEvent.ACTION_DOWN
        val pController = connectedPhysicalControllers.firstOrNull { it.id == e.deviceId } ?: return false

        when (e.keyCode) {
            KEYCODE_BUTTON_Y -> {
                pController.state.yPressed = pressed

                when (pController.mappingType) {
                    MAPPING_TYPE_XINPUT -> {
                        val index = pController.virtualControllerID
                        if (index != -1) {
                            connectedVirtualControllers[index].yPressed = pressed
                        }
                    }
                    MAPPING_TYPE_KEYBOARD_MOUSE -> {
                        val mapping = pController.keyboardMapping.yButton
                        handleKey(pressed, mapping)
                    }
                }
            }
            KEYCODE_BUTTON_A -> {
                pController.state.aPressed = pressed

                when (pController.mappingType) {
                    MAPPING_TYPE_XINPUT -> {
                        val index = pController.virtualControllerID
                        if (index != -1) {
                            connectedVirtualControllers[index].aPressed = pressed
                        }
                    }
                    MAPPING_TYPE_KEYBOARD_MOUSE -> {
                        val mapping = pController.keyboardMapping.aButton
                        handleKey(pressed, mapping)
                    }
                }
            }
            KEYCODE_BUTTON_B -> {
                pController.state.bPressed = pressed

                when (pController.mappingType) {
                    MAPPING_TYPE_XINPUT -> {
                        val index = pController.virtualControllerID
                        if (index != -1) {
                            connectedVirtualControllers[index].bPressed = pressed
                        }
                    }
                    MAPPING_TYPE_KEYBOARD_MOUSE -> {
                        val mapping = pController.keyboardMapping.bButton
                        handleKey(pressed, mapping)
                    }
                }
            }
            KEYCODE_BUTTON_X -> {
                pController.state.xPressed = pressed

                when (pController.mappingType) {
                    MAPPING_TYPE_XINPUT -> {
                        val index = pController.virtualControllerID
                        if (index != -1) {
                            connectedVirtualControllers[index].xPressed = pressed
                        }
                    }
                    MAPPING_TYPE_KEYBOARD_MOUSE -> {
                        val mapping = pController.keyboardMapping.xButton
                        handleKey(pressed, mapping)
                    }
                }
            }
            KEYCODE_BUTTON_START -> {
                pController.state.startPressed = pressed

                when (pController.mappingType) {
                    MAPPING_TYPE_XINPUT -> {
                        val index = pController.virtualControllerID
                        if (index != -1) {
                            connectedVirtualControllers[index].startPressed = pressed
                        }
                    }
                    MAPPING_TYPE_KEYBOARD_MOUSE -> {
                        val mapping = pController.keyboardMapping.startButton
                        handleKey(pressed, mapping)
                    }
                }
            }
            KEYCODE_BUTTON_SELECT -> {
                pController.state.selectPressed = pressed

                when (pController.mappingType) {
                    MAPPING_TYPE_XINPUT -> {
                        val index = pController.virtualControllerID
                        if (index != -1) {
                            connectedVirtualControllers[index].selectPressed = pressed
                        }
                    }
                    MAPPING_TYPE_KEYBOARD_MOUSE -> {
                        val mapping = pController.keyboardMapping.selectButton
                        handleKey(pressed, mapping)
                    }
                }
            }
            KEYCODE_BUTTON_R1 -> {
                pController.state.rbPressed = pressed

                when (pController.mappingType) {
                    MAPPING_TYPE_XINPUT -> {
                        val index = pController.virtualControllerID
                        if (index != -1) {
                            connectedVirtualControllers[index].rbPressed = pressed
                        }
                    }
                    MAPPING_TYPE_KEYBOARD_MOUSE -> {
                        val mapping = pController.keyboardMapping.rbButton
                        handleKey(pressed, mapping)
                    }
                }
            }
            KEYCODE_BUTTON_L1 -> {
                pController.state.lbPressed = pressed

                when (pController.mappingType) {
                    MAPPING_TYPE_XINPUT -> {
                        val index = pController.virtualControllerID
                        if (index != -1) {
                            connectedVirtualControllers[index].lbPressed = pressed
                        }
                    }
                    MAPPING_TYPE_KEYBOARD_MOUSE -> {
                        val mapping = pController.keyboardMapping.lbButton
                        handleKey(pressed, mapping)
                    }
                }
            }
            KEYCODE_BUTTON_R2 -> {
                if (pController.supportAxisTrigger) return true

                pController.state.rt = if (pressed) 1F else 0F

                when (pController.mappingType) {
                    MAPPING_TYPE_XINPUT -> {
                        val index = pController.virtualControllerID
                        if (index != -1) {
                            connectedVirtualControllers[index].rt[0] = if (pressed) 2 else 0
                            connectedVirtualControllers[index].rt[1] = if (pressed) 5 else 0
                            connectedVirtualControllers[index].rt[2] = if (pressed) 5 else 0
                        }
                    }
                    MAPPING_TYPE_KEYBOARD_MOUSE -> {
                        val mapping = pController.keyboardMapping.rtButton
                        handleKey(pressed, mapping)
                    }
                }
            }
            KEYCODE_BUTTON_L2 -> {
                if (pController.supportAxisTrigger) return true

                pController.state.lt = if (pressed) 1F else 0F

                when (pController.mappingType) {
                    MAPPING_TYPE_XINPUT -> {
                        val index = pController.virtualControllerID
                        if (index != -1) {
                            connectedVirtualControllers[index].lt[0] = if (pressed) 2 else 0
                            connectedVirtualControllers[index].lt[1] = if (pressed) 5 else 0
                            connectedVirtualControllers[index].lt[2] = if (pressed) 5 else 0
                        }
                    }
                    MAPPING_TYPE_KEYBOARD_MOUSE -> {
                        val mapping = pController.keyboardMapping.ltButton
                        handleKey(pressed, mapping)
                    }
                }
            }
            KEYCODE_BUTTON_THUMBR -> {
                pController.state.rsPressed = pressed

                when (pController.mappingType) {
                    MAPPING_TYPE_XINPUT -> {
                        val index = pController.virtualControllerID
                        if (index != -1) {
                            connectedVirtualControllers[index].rsPressed = pressed
                        }
                    }
                    MAPPING_TYPE_KEYBOARD_MOUSE -> {
                        val mapping = pController.keyboardMapping.rsButton
                        handleKey(pressed, mapping)
                    }
                }
            }
            KEYCODE_BUTTON_THUMBL -> {
                pController.state.lsPressed = pressed

                when (pController.mappingType) {
                    MAPPING_TYPE_XINPUT -> {
                        val index = pController.virtualControllerID
                        if (index != -1) {
                            connectedVirtualControllers[index].lsPressed = pressed
                        }
                    }
                    MAPPING_TYPE_KEYBOARD_MOUSE -> {
                        val mapping = pController.keyboardMapping.lsButton
                        handleKey(pressed, mapping)
                    }
                }
            }
        }

        return true
    }

    private fun setVirtualMouseState(axisX: Float, axisY: Float, state: Int) {
        if (axisX.absoluteValue > 0.25F) axisXVelocity = axisX.absoluteValue
        if (axisY.absoluteValue > 0.25F) axisYVelocity = axisY.absoluteValue

        virtualMouseMovingState = state
    }

    fun getAxisStatus(axisX: Float, axisY: Float, deadZone: Float): Int {
        val axisXNeutral = axisX < deadZone && axisX > -deadZone
        val axisYNeutral = axisY < deadZone && axisY > -deadZone

        return when {
            axisX > deadZone && axisY < -deadZone -> RIGHT_UP
            axisX > deadZone && axisYNeutral -> RIGHT
            axisX > deadZone && axisY > deadZone -> RIGHT_DOWN
            axisY > deadZone && axisXNeutral -> DOWN
            axisY < -deadZone && axisXNeutral -> UP
            axisX < -deadZone && axisY > deadZone -> LEFT_DOWN
            axisX < -deadZone && axisYNeutral -> LEFT
            axisX < -deadZone && axisY < -deadZone -> LEFT_UP
            else -> 0
        }
    }

    fun handleAxis(axisX: Float, axisY: Float, analog: Analog, deadZone: Float) {
        val status = getAxisStatus(axisX, axisY, deadZone)
        when (status) {
            LEFT -> {
                if (analog.isMouseMapping) {
                    setVirtualMouseState(axisX, axisY, LEFT)
                } else {
                    lorieView.sendKeyEvent(analog.right.scanCode, analog.right.keyCode, false)
                    lorieView.sendKeyEvent(analog.left.scanCode, analog.left.keyCode, true)
                    lorieView.sendKeyEvent(analog.up.scanCode, analog.up.keyCode, false)
                    lorieView.sendKeyEvent(analog.down.scanCode, analog.down.keyCode, false)
                }
            }
            RIGHT -> {
                if (analog.isMouseMapping) {
                    setVirtualMouseState(axisX, axisY, RIGHT)
                } else {
                    lorieView.sendKeyEvent(analog.right.scanCode, analog.right.keyCode, true)
                    lorieView.sendKeyEvent(analog.left.scanCode, analog.left.keyCode, false)
                    lorieView.sendKeyEvent(analog.up.scanCode, analog.up.keyCode, false)
                    lorieView.sendKeyEvent(analog.down.scanCode, analog.down.keyCode, false)
                }
            }
            UP -> {
                if (analog.isMouseMapping) {
                    setVirtualMouseState(axisX, axisY, UP)
                } else {
                    lorieView.sendKeyEvent(analog.right.scanCode, analog.right.keyCode, false)
                    lorieView.sendKeyEvent(analog.left.scanCode, analog.left.keyCode, false)
                    lorieView.sendKeyEvent(analog.up.scanCode, analog.up.keyCode, true)
                    lorieView.sendKeyEvent(analog.down.scanCode, analog.down.keyCode, false)
                }
            }
            DOWN -> {
                if (analog.isMouseMapping) {
                    setVirtualMouseState(axisX, axisY, DOWN)
                } else {
                    lorieView.sendKeyEvent(analog.right.scanCode, analog.right.keyCode, false)
                    lorieView.sendKeyEvent(analog.left.scanCode, analog.left.keyCode, false)
                    lorieView.sendKeyEvent(analog.up.scanCode, analog.up.keyCode, false)
                    lorieView.sendKeyEvent(analog.down.scanCode, analog.down.keyCode, true)
                }
            }
            LEFT_UP -> {
                if (analog.isMouseMapping) {
                    setVirtualMouseState(axisX, axisY, LEFT_UP)
                } else {
                    lorieView.sendKeyEvent(analog.right.scanCode, analog.right.keyCode, false)
                    lorieView.sendKeyEvent(analog.left.scanCode, analog.left.keyCode, true)
                    lorieView.sendKeyEvent(analog.up.scanCode, analog.up.keyCode, true)
                    lorieView.sendKeyEvent(analog.down.scanCode, analog.down.keyCode, false)
                }
            }
            LEFT_DOWN -> {
                if (analog.isMouseMapping) {
                    setVirtualMouseState(axisX, axisY, LEFT_DOWN)
                } else {
                    lorieView.sendKeyEvent(analog.right.scanCode, analog.right.keyCode, false)
                    lorieView.sendKeyEvent(analog.left.scanCode, analog.left.keyCode, true)
                    lorieView.sendKeyEvent(analog.up.scanCode, analog.up.keyCode, false)
                    lorieView.sendKeyEvent(analog.down.scanCode, analog.down.keyCode, true)
                }
            }
            RIGHT_UP -> {
                if (analog.isMouseMapping) {
                    setVirtualMouseState(axisX, axisY, RIGHT_UP)
                } else {
                    lorieView.sendKeyEvent(analog.right.scanCode, analog.right.keyCode, true)
                    lorieView.sendKeyEvent(analog.left.scanCode, analog.left.keyCode, false)
                    lorieView.sendKeyEvent(analog.up.scanCode, analog.up.keyCode, true)
                    lorieView.sendKeyEvent(analog.down.scanCode, analog.down.keyCode, false)
                }
            }
            RIGHT_DOWN -> {
                if (analog.isMouseMapping) {
                    setVirtualMouseState(axisX, axisY, RIGHT_DOWN)
                } else {
                    lorieView.sendKeyEvent(analog.right.scanCode, analog.right.keyCode, true)
                    lorieView.sendKeyEvent(analog.left.scanCode, analog.left.keyCode, false)
                    lorieView.sendKeyEvent(analog.up.scanCode, analog.up.keyCode, false)
                    lorieView.sendKeyEvent(analog.down.scanCode, analog.down.keyCode, true)
                }
            }
            else -> {
                if (analog.isMouseMapping) {
                    virtualMouseMovingState = null
                } else {
                    lorieView.sendKeyEvent(analog.right.scanCode, analog.right.keyCode, false)
                    lorieView.sendKeyEvent(analog.left.scanCode, analog.left.keyCode, false)
                    lorieView.sendKeyEvent(analog.up.scanCode, analog.up.keyCode, false)
                    lorieView.sendKeyEvent(analog.down.scanCode, analog.down.keyCode, false)
                }
            }
        }
    }

    fun updateAxisState(event: MotionEvent) {
        controllerIndex = connectedPhysicalControllers.indexOfFirst { it.id == event.deviceId }
        if (controllerIndex == -1) return

        val pController = connectedPhysicalControllers[controllerIndex]

        pController.state.lx = event.getAxisValue(AXIS_X)
        if (pController.state.lx < -200F) {
            pController.state.lx += 270F
        }
        pController.state.ly = event.getAxisValue(AXIS_Y)
        pController.state.rx = event.getAxisValue(AXIS_Z)
        pController.state.ry = event.getAxisValue(AXIS_RZ)
        pController.state.dpadX = event.getAxisValue(AXIS_HAT_X)
        pController.state.dpadY = event.getAxisValue(AXIS_HAT_Y)

        if (pController.supportAxisTrigger) {
            pController.state.lt = event.getAxisValue(AXIS_LTRIGGER)
            pController.state.rt = event.getAxisValue(AXIS_RTRIGGER)
        }

        when (pController.mappingType) {
            MAPPING_TYPE_XINPUT -> {
                val index = pController.virtualControllerID
                if (index != -1) {
                    if (pController.swapAnalogs) {
                        axisToByteArray(connectedVirtualControllers[index].lx, normalizeAxisValue(pController.state.rx))
                        axisToByteArray(connectedVirtualControllers[index].ly, normalizeAxisValue(-pController.state.ry))
                        axisToByteArray(connectedVirtualControllers[index].rx, normalizeAxisValue(pController.state.lx))
                        axisToByteArray(connectedVirtualControllers[index].ry, normalizeAxisValue(-pController.state.ly))
                    } else {
                        axisToByteArray(connectedVirtualControllers[index].lx, normalizeAxisValue(pController.state.lx))
                        axisToByteArray(connectedVirtualControllers[index].ly, normalizeAxisValue(-pController.state.ly))
                        axisToByteArray(connectedVirtualControllers[index].rx, normalizeAxisValue(pController.state.rx))
                        axisToByteArray(connectedVirtualControllers[index].ry, normalizeAxisValue(-pController.state.ry))
                    }

                    if (pController.supportAxisTrigger) {
                        axisToByteArray(connectedVirtualControllers[index].lt, normalizeAxisValue(pController.state.lt, true))
                        axisToByteArray(connectedVirtualControllers[index].rt, normalizeAxisValue(pController.state.rt, true))
                    }

                    connectedVirtualControllers[index].dpadStatus = getAxisStatus(pController.state.dpadX, pController.state.dpadY, 0.25F)
                }
            }
            MAPPING_TYPE_KEYBOARD_MOUSE -> {
                handleAxis(pController.state.lx, pController.state.ly, pController.keyboardMapping.leftAnalog, pController.deadZone)
                handleAxis(pController.state.rx, pController.state.ry, pController.keyboardMapping.rightAnalog, pController.deadZone)
                handleAxis(pController.state.dpadX, pController.state.dpadY, pController.keyboardMapping.dPad, pController.deadZone)

                if (pController.supportAxisTrigger) {
                    handleKey(pController.state.lt > pController.deadZone, pController.keyboardMapping.ltButton)
                    handleKey(pController.state.rt > pController.deadZone, pController.keyboardMapping.rtButton)
                }
            }
        }
    }

    private fun normalizeAxisValue(value: Float, half: Boolean = false): Int {
        return if (half) {
            (value * 255).toInt().coerceIn(0, 255)
        } else {
            ((value + 1) / 2 * 255).toInt().coerceIn(0, 255)
        }
    }

    private fun axisToByteArray(byteArray: ByteArray, value: Int) {
        val str = value.coerceIn(0, 255).toString().padStart(3, '0')
        byteArray[0] = str[0].digitToInt().toByte()
        byteArray[1] = str[1].digitToInt().toByte()
        byteArray[2] = str[2].digitToInt().toByte()
    }

    val connectedPhysicalControllers: MutableList<PhysicalController> = getConnectedControllers()

    private const val MAPPING_TYPE_KEYBOARD_MOUSE = 0
    private const val MAPPING_TYPE_XINPUT = 1

    class PhysicalController(
        var name: String,
        var id: Int,
        var mappingType: Int = MAPPING_TYPE_KEYBOARD_MOUSE,
        var virtualControllerID: Int = -1,
        var supportAxisTrigger: Boolean = false,
        var flatZone: Float = 0F,
        var swapAnalogs: Boolean = false,
        var deadZone: Float = 0.25F,
        var mouseSensibility: Float = 1F,
        var keyboardMapping: KeyboardMapping = KeyboardMapping(),
        var state: ControllerState = ControllerState(),
    )

    class ControllerState(
        var aPressed: Boolean = false,
        var bPressed: Boolean = false,
        var xPressed: Boolean = false,
        var yPressed: Boolean = false,
        var startPressed: Boolean = false,
        var selectPressed: Boolean = false,
        var rbPressed: Boolean = false,
        var lbPressed: Boolean = false,
        var lsPressed: Boolean = false,
        var rsPressed: Boolean = false,
        var dpadX: Float = 0F,
        var dpadY: Float = 0F,
        var lx: Float = 0F,
        var ly: Float = 0F,
        var rx: Float = 0F,
        var ry: Float = 0F,
        var lt: Float = 0F,
        var rt: Float = 0F,
    )

    class Axis(
        var value: Float,
        var range: MotionRange,
        var flatZone: Float
    )

    class KeyboardMapping(
        var leftAnalog: Analog = Analog(),
        var rightAnalog: Analog = Analog(),
        var dPad: Analog = Analog(),
        var aButton: ButtonMapping = ButtonMapping(),
        var bButton: ButtonMapping = ButtonMapping(),
        var xButton: ButtonMapping = ButtonMapping(),
        var yButton: ButtonMapping = ButtonMapping(),
        var startButton: ButtonMapping = ButtonMapping(),
        var selectButton: ButtonMapping = ButtonMapping(),
        var lbButton: ButtonMapping = ButtonMapping(),
        var rbButton: ButtonMapping = ButtonMapping(),
        var ltButton: ButtonMapping = ButtonMapping(),
        var rtButton: ButtonMapping = ButtonMapping(),
        var lsButton: ButtonMapping = ButtonMapping(),
        var rsButton: ButtonMapping = ButtonMapping(),
    )

    class Analog(
        var isMouseMapping: Boolean = false,
        var up: ButtonMapping = ButtonMapping(),
        var down: ButtonMapping = ButtonMapping(),
        var left: ButtonMapping = ButtonMapping(),
        var right: ButtonMapping = ButtonMapping(),
    )

    class VirtualController(
        var connected: Boolean = false,
        var aPressed: Boolean = false,
        var bPressed: Boolean = false,
        var xPressed: Boolean = false,
        var yPressed: Boolean = false,
        var startPressed: Boolean = false,
        var selectPressed: Boolean = false,
        var rbPressed: Boolean = false,
        var lbPressed: Boolean = false,
        var lsPressed: Boolean = false,
        var rsPressed: Boolean = false,
        var dpadStatus: Int = 0,
        var lx: ByteArray = byteArrayOf(1, 2, 7),
        var ly: ByteArray = byteArrayOf(1, 2, 7),
        var rx: ByteArray = byteArrayOf(1, 2, 7),
        var ry: ByteArray = byteArrayOf(1, 2, 7),
        var lt: ByteArray = byteArrayOf(0, 0, 0),
        var rt: ByteArray = byteArrayOf(0, 0, 0)
    )

    val connectedVirtualControllers: List<VirtualController> = listOf(VirtualController(), VirtualController(), VirtualController(), VirtualController())

    fun connectController(): Int {
        connectedVirtualControllers.forEachIndexed { index, it ->
            if (!it.connected) {
                Log.d("ControllerDebug", "Connected Controller on Port $index")
                it.connected = true
                return index
            }
        }

        return -1
    }

    fun disconnectController(index: Int) {
        if (index != -1) {
            Log.d("ControllerDebug", "Disconnected Controller on Port $index")
            connectedVirtualControllers[index].connected = false
        }
    }

    private const val CLIENT_PORT = 7941
    private const val BUFFER_SIZE = 128
    private const val GET_CONNECTION = 1
    private const val GET_GAMEPAD_STATE = 2

    private var inputServerRunning = false
    private val serverSocket = DatagramSocket(CLIENT_PORT)

    suspend fun startInputServer() {
        if (inputServerRunning) return

        Log.d("ControllerDebug", "Input Server Initialized.")

        inputServerRunning = true

        withContext(Dispatchers.IO) {
            while (inputServerRunning) {
                val buffer = ByteArray(BUFFER_SIZE)
                val packet = DatagramPacket(buffer, buffer.size)

                serverSocket.receive(packet)

                val receivedData = ByteBuffer.wrap(buffer).get().toInt()
                when (receivedData) {
                    GET_CONNECTION -> {
                        val responsePacket = DatagramPacket(buffer, buffer.size, packet.address, packet.port)
                        serverSocket.send(responsePacket)
                    }
                    GET_GAMEPAD_STATE -> {
                        connectedVirtualControllers.forEachIndexed { index, virtualController ->
                            buffer[0 + (index * 32)] = GET_GAMEPAD_STATE.toByte()
                            buffer[1 + (index * 32)] = if (virtualController.connected) 1 else 0
                            buffer[2 + (index * 32)] = if (virtualController.aPressed) 1 else 0
                            buffer[3 + (index * 32)] = if (virtualController.bPressed) 1 else 0
                            buffer[4 + (index * 32)] = if (virtualController.xPressed) 1 else 0
                            buffer[5 + (index * 32)] = if (virtualController.yPressed) 1 else 0
                            buffer[6 + (index * 32)] = if (virtualController.lbPressed) 1 else 0
                            buffer[7 + (index * 32)] = if (virtualController.rbPressed) 1 else 0
                            buffer[8 + (index * 32)] = if (virtualController.selectPressed) 1 else 0
                            buffer[9 + (index * 32)] = if (virtualController.startPressed) 1 else 0
                            buffer[10 + (index * 32)] = if (virtualController.lsPressed) 1 else 0
                            buffer[11 + (index * 32)] = if (virtualController.rsPressed) 1 else 0
                            buffer[12 + (index * 32)] = 0
                            buffer[13 + (index * 32)] = virtualController.dpadStatus.toByte()
                            buffer[14 + (index * 32)] = virtualController.lx[0]
                            buffer[15 + (index * 32)] = virtualController.lx[1]
                            buffer[16 + (index * 32)] = virtualController.lx[2]
                            buffer[17 + (index * 32)] = virtualController.ly[0]
                            buffer[18 + (index * 32)] = virtualController.ly[1]
                            buffer[19 + (index * 32)] = virtualController.ly[2]
                            buffer[20 + (index * 32)] = virtualController.rx[0]
                            buffer[21 + (index * 32)] = virtualController.rx[1]
                            buffer[22 + (index * 32)] = virtualController.rx[2]
                            buffer[23 + (index * 32)] = virtualController.ry[0]
                            buffer[24 + (index * 32)] = virtualController.ry[1]
                            buffer[25 + (index * 32)] = virtualController.ry[2]
                            buffer[26 + (index * 32)] = virtualController.lt[0]
                            buffer[27 + (index * 32)] = virtualController.lt[1]
                            buffer[28 + (index * 32)] = virtualController.lt[2]
                            buffer[29 + (index * 32)] = virtualController.rt[0]
                            buffer[30 + (index * 32)] = virtualController.rt[1]
                            buffer[31 + (index * 32)] = virtualController.rt[2]
                        }

                        val responsePacket = DatagramPacket(buffer, buffer.size, packet.address, packet.port)
                        serverSocket.send(responsePacket)
                    }
                }
            }
        }
    }

    fun destroyInputServer() {
        inputServerRunning = false
    }
}
