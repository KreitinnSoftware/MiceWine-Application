package com.micewine.emu.controller;

import static com.micewine.emu.activities.EmulationActivity.handler;
import static com.micewine.emu.activities.MainActivity.enableDInput;
import static com.micewine.emu.activities.MainActivity.enableXInput;
import static com.micewine.emu.activities.PresetManagerActivity.AXIS_HAT_X_MINUS_KEY;
import static com.micewine.emu.activities.PresetManagerActivity.AXIS_HAT_X_PLUS_KEY;
import static com.micewine.emu.activities.PresetManagerActivity.AXIS_HAT_Y_MINUS_KEY;
import static com.micewine.emu.activities.PresetManagerActivity.AXIS_HAT_Y_PLUS_KEY;
import static com.micewine.emu.activities.PresetManagerActivity.AXIS_RZ_MINUS_KEY;
import static com.micewine.emu.activities.PresetManagerActivity.AXIS_RZ_PLUS_KEY;
import static com.micewine.emu.activities.PresetManagerActivity.AXIS_X_MINUS_KEY;
import static com.micewine.emu.activities.PresetManagerActivity.AXIS_X_PLUS_KEY;
import static com.micewine.emu.activities.PresetManagerActivity.AXIS_Y_MINUS_KEY;
import static com.micewine.emu.activities.PresetManagerActivity.AXIS_Y_PLUS_KEY;
import static com.micewine.emu.activities.PresetManagerActivity.AXIS_Z_MINUS_KEY;
import static com.micewine.emu.activities.PresetManagerActivity.AXIS_Z_PLUS_KEY;
import static com.micewine.emu.activities.PresetManagerActivity.BUTTON_A_KEY;
import static com.micewine.emu.activities.PresetManagerActivity.BUTTON_B_KEY;
import static com.micewine.emu.activities.PresetManagerActivity.BUTTON_L1_KEY;
import static com.micewine.emu.activities.PresetManagerActivity.BUTTON_L2_KEY;
import static com.micewine.emu.activities.PresetManagerActivity.BUTTON_R1_KEY;
import static com.micewine.emu.activities.PresetManagerActivity.BUTTON_R2_KEY;
import static com.micewine.emu.activities.PresetManagerActivity.BUTTON_SELECT_KEY;
import static com.micewine.emu.activities.PresetManagerActivity.BUTTON_START_KEY;
import static com.micewine.emu.activities.PresetManagerActivity.BUTTON_THUMBL_KEY;
import static com.micewine.emu.activities.PresetManagerActivity.BUTTON_THUMBR_KEY;
import static com.micewine.emu.activities.PresetManagerActivity.BUTTON_X_KEY;
import static com.micewine.emu.activities.PresetManagerActivity.BUTTON_Y_KEY;
import static com.micewine.emu.adapters.AdapterGame.selectedGameName;
import static com.micewine.emu.fragments.ControllerPresetManagerFragment.getControllerPreset;
import static com.micewine.emu.fragments.ControllerPresetManagerFragment.getDeadZone;
import static com.micewine.emu.fragments.ControllerPresetManagerFragment.getMouseSensibility;
import static com.micewine.emu.fragments.ShortcutsFragment.getControllerPreset;
import static com.micewine.emu.controller.XKeyCodes.ButtonMapping;
import static com.micewine.emu.fragments.ShortcutsFragment.getControllerXInput;
import static com.micewine.emu.fragments.ShortcutsFragment.getControllerXInputSwapAnalogs;
import static com.micewine.emu.input.InputStub.BUTTON_LEFT;
import static com.micewine.emu.input.InputStub.BUTTON_MIDDLE;
import static com.micewine.emu.input.InputStub.BUTTON_RIGHT;

import android.content.Context;
import android.util.Log;
import android.view.InputDevice;
import android.view.KeyEvent;
import android.view.MotionEvent;

import com.micewine.emu.LorieView;
import com.micewine.emu.input.InputStub;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import java.nio.ByteBuffer;

public class ControllerUtils {
    public static final int KEYBOARD = 0;
    public static final int MOUSE = 1;

    private static int controllerIndex = -1;

    private static LorieView lorieView;

    public static final int UP = 1;
    public static final int RIGHT_UP = 2;
    public static final int RIGHT = 3;
    public static final int RIGHT_DOWN = 4;
    public static final int DOWN = 5;
    public static final int LEFT_DOWN = 6;
    public static final int LEFT = 7;
    public static final int LEFT_UP = 8;
    public static final int SCROLL_UP = 9;
    public static final int SCROLL_DOWN = 10;

    private static float lastAxisX = 0F;
    private static float lastAxisY = 0F;
    private static float mouseSensibility = 0F;

    public static void initialize(Context context) {
        lorieView = new LorieView(context);
        handler.postDelayed(virtualMouseControllerRunnable, 16L);
    }

    private static final Runnable virtualMouseControllerRunnable = new Runnable() {
        @Override
        public void run() {
            if (Math.abs(lastAxisX) > 0.125F || Math.abs(lastAxisY) > 0.125F) {
                lorieView.sendMouseEvent(
                        lastAxisX * mouseSensibility,
                        lastAxisY * mouseSensibility,
                        InputStub.BUTTON_UNDEFINED,
                        false,
                        true
                );
            }
            handler.postDelayed(this, 16L);
        }
    };

    private static ButtonMapping detectKey(String presetName, String key) {
        String preset = (presetName == null || presetName.equals("--") ? "default" : presetName);
        ButtonMapping controllerPreset = getControllerPreset(preset, key);

        if (controllerPreset == null) {
            return new ButtonMapping();
        }

        return switch (controllerPreset.name) {
            case "M_Left" -> new ButtonMapping("M_Left", BUTTON_LEFT, BUTTON_LEFT, MOUSE);
            case "M_Middle" -> new ButtonMapping("M_Middle", BUTTON_MIDDLE, BUTTON_MIDDLE, MOUSE);
            case "M_Right" -> new ButtonMapping("M_Right", BUTTON_RIGHT, BUTTON_RIGHT, MOUSE);
            case "M_WheelUp" -> new ButtonMapping("M_WheelUp", SCROLL_UP, SCROLL_UP, MOUSE);
            case "M_WheelDown" -> new ButtonMapping("M_WheelDown", SCROLL_DOWN, SCROLL_DOWN, MOUSE);
            case "Mouse" -> new ButtonMapping("Mouse", MOUSE, MOUSE, MOUSE);
            default -> controllerPreset;
        };
    }

    public static void prepareControllersMappings() {
        for (int i = 0; i < connectedPhysicalControllers.size(); i++) {
            PhysicalController physicalController = connectedPhysicalControllers.get(i);
            String presetName = getControllerPreset(selectedGameName, i);
            boolean controllerIsXInput = getControllerXInput(selectedGameName, i);

            if (controllerIsXInput) {
                physicalController.mappingType = MAPPING_TYPE_XINPUT;
                physicalController.swapAnalogs = getControllerXInputSwapAnalogs(selectedGameName, i);

                if (physicalController.virtualControllerID == -1) {
                    physicalController.virtualControllerID = connectController();
                }
            } else {
                physicalController.mappingType = MAPPING_TYPE_KEYBOARD_MOUSE;

                physicalController.keyboardMapping.aButton = detectKey(presetName, BUTTON_A_KEY);
                physicalController.keyboardMapping.bButton = detectKey(presetName, BUTTON_B_KEY);
                physicalController.keyboardMapping.xButton = detectKey(presetName, BUTTON_X_KEY);
                physicalController.keyboardMapping.yButton = detectKey(presetName, BUTTON_Y_KEY);

                physicalController.keyboardMapping.rbButton = detectKey(presetName, BUTTON_R1_KEY);
                physicalController.keyboardMapping.rtButton = detectKey(presetName, BUTTON_R2_KEY);

                physicalController.keyboardMapping.lbButton = detectKey(presetName, BUTTON_L1_KEY);
                physicalController.keyboardMapping.ltButton = detectKey(presetName, BUTTON_L2_KEY);

                physicalController.keyboardMapping.lsButton = detectKey(presetName, BUTTON_THUMBL_KEY);
                physicalController.keyboardMapping.rsButton = detectKey(presetName, BUTTON_THUMBR_KEY);

                physicalController.keyboardMapping.startButton = detectKey(presetName, BUTTON_START_KEY);
                physicalController.keyboardMapping.selectButton = detectKey(presetName, BUTTON_SELECT_KEY);

                physicalController.keyboardMapping.leftAnalog.up = detectKey(presetName, AXIS_Y_MINUS_KEY);
                physicalController.keyboardMapping.leftAnalog.down = detectKey(presetName, AXIS_Y_PLUS_KEY);
                physicalController.keyboardMapping.leftAnalog.left = detectKey(presetName, AXIS_X_MINUS_KEY);
                physicalController.keyboardMapping.leftAnalog.right = detectKey(presetName, AXIS_X_PLUS_KEY);

                physicalController.keyboardMapping.leftAnalog.isMouseMapping = Stream.of(
                        detectKey(presetName, AXIS_Y_MINUS_KEY),
                        detectKey(presetName, AXIS_Y_PLUS_KEY),
                        detectKey(presetName, AXIS_X_MINUS_KEY),
                        detectKey(presetName, AXIS_X_PLUS_KEY)
                ).anyMatch(key -> key.type == MOUSE);

                physicalController.keyboardMapping.rightAnalog.up = detectKey(presetName, AXIS_Z_MINUS_KEY);
                physicalController.keyboardMapping.rightAnalog.down = detectKey(presetName, AXIS_Z_PLUS_KEY);
                physicalController.keyboardMapping.rightAnalog.left = detectKey(presetName, AXIS_RZ_MINUS_KEY);
                physicalController.keyboardMapping.rightAnalog.right = detectKey(presetName, AXIS_RZ_PLUS_KEY);

                physicalController.keyboardMapping.rightAnalog.isMouseMapping = Stream.of(
                        detectKey(presetName, AXIS_Z_MINUS_KEY),
                        detectKey(presetName, AXIS_Z_PLUS_KEY),
                        detectKey(presetName, AXIS_RZ_MINUS_KEY),
                        detectKey(presetName, AXIS_RZ_PLUS_KEY)
                ).anyMatch(key -> key.type == MOUSE);

                physicalController.keyboardMapping.dpad.up = detectKey(presetName, AXIS_Z_MINUS_KEY);
                physicalController.keyboardMapping.dpad.down = detectKey(presetName, AXIS_Z_PLUS_KEY);
                physicalController.keyboardMapping.dpad.left = detectKey(presetName, AXIS_RZ_MINUS_KEY);
                physicalController.keyboardMapping.dpad.right = detectKey(presetName, AXIS_RZ_PLUS_KEY);

                physicalController.keyboardMapping.dpad.isMouseMapping = Stream.of(
                        detectKey(presetName, AXIS_HAT_Y_MINUS_KEY),
                        detectKey(presetName, AXIS_HAT_Y_PLUS_KEY),
                        detectKey(presetName, AXIS_HAT_X_MINUS_KEY),
                        detectKey(presetName, AXIS_HAT_X_PLUS_KEY)
                ).anyMatch(key -> key.type == MOUSE);

                physicalController.deadZone = getDeadZone(presetName) / 100F;
                physicalController.mouseSensibility = getMouseSensibility(presetName) / 100F;
            }
        }
    }

    private static List<PhysicalController> getConnectedControllers() {
        int[] devicesIds = InputDevice.getDeviceIds();
        List<PhysicalController> devices = new ArrayList<>();

        for (int id : devicesIds) {
            InputDevice device = InputDevice.getDevice(id);

            if (device == null) continue;
            if (((device.getSources() & InputDevice.SOURCE_GAMEPAD) == InputDevice.SOURCE_GAMEPAD)
                    || ((device.getSources() & InputDevice.SOURCE_JOYSTICK) == InputDevice.SOURCE_JOYSTICK)) {
                if (!device.getName().contains("uinput")) {
                    devices.add(new PhysicalController(device.getName(), id));
                }
            }
        }

        return devices;
    }

    public static void handleKey(boolean pressed, ButtonMapping mapping) {
        switch (mapping.type) {
            case KEYBOARD -> lorieView.sendKeyEvent(mapping.scanCode, mapping.keyCode, pressed);
            case MOUSE -> {
                switch (mapping.scanCode) {
                    case SCROLL_UP: {
                        if (pressed) lorieView.sendMouseWheelEvent(0F, -10F);
                    }
                    case SCROLL_DOWN: {
                        if (pressed) lorieView.sendMouseWheelEvent(0F, 10F);
                    }
                    default: {
                        lorieView.sendMouseEvent(0F, 0F, mapping.scanCode, pressed, true);
                    }
                }
            }
        }
    }

    public static void updateButtonsState(KeyEvent e) {
        boolean pressed = (e.getAction() == KeyEvent.ACTION_DOWN);

        PhysicalController pController = null;
        for (PhysicalController c : connectedPhysicalControllers) {
            if (c.id == e.getDeviceId()) {
                pController = c;
                break;
            }
        }

        if (pController == null) return;

        switch (e.getKeyCode()) {
            case KeyEvent.KEYCODE_BUTTON_Y -> {
                pController.state.yPressed = pressed;

                switch (pController.mappingType) {
                    case MAPPING_TYPE_XINPUT: {
                        int index = pController.virtualControllerID;
                        if (index != -1) {
                            connectedVirtualControllers[index].state.yPressed = pressed;
                        }
                    }
                    case MAPPING_TYPE_KEYBOARD_MOUSE: {
                        handleKey(pressed, pController.keyboardMapping.yButton);
                    }
                }
            }
            case KeyEvent.KEYCODE_BUTTON_A -> {
                pController.state.aPressed = pressed;

                switch (pController.mappingType) {
                    case MAPPING_TYPE_XINPUT: {
                        int index = pController.virtualControllerID;
                        if (index != -1) {
                            connectedVirtualControllers[index].state.aPressed = pressed;
                        }
                    }
                    case MAPPING_TYPE_KEYBOARD_MOUSE: {
                        handleKey(pressed, pController.keyboardMapping.aButton);
                    }
                }
            }
            case KeyEvent.KEYCODE_BUTTON_B -> {
                pController.state.bPressed = pressed;

                switch (pController.mappingType) {
                    case MAPPING_TYPE_XINPUT: {
                        int index = pController.virtualControllerID;
                        if (index != -1) {
                            connectedVirtualControllers[index].state.bPressed = pressed;
                        }
                    }
                    case MAPPING_TYPE_KEYBOARD_MOUSE: {
                        handleKey(pressed, pController.keyboardMapping.bButton);
                    }
                }
            }
            case KeyEvent.KEYCODE_BUTTON_X -> {
                pController.state.xPressed = pressed;

                switch (pController.mappingType) {
                    case MAPPING_TYPE_XINPUT: {
                        int index = pController.virtualControllerID;
                        if (index != -1) {
                            connectedVirtualControllers[index].state.xPressed = pressed;
                        }
                    }
                    case MAPPING_TYPE_KEYBOARD_MOUSE: {
                        handleKey(pressed, pController.keyboardMapping.xButton);
                    }
                }
            }
            case KeyEvent.KEYCODE_BUTTON_START -> {
                pController.state.startPressed = pressed;

                switch (pController.mappingType) {
                    case MAPPING_TYPE_XINPUT: {
                        int index = pController.virtualControllerID;
                        if (index != -1) {
                            connectedVirtualControllers[index].state.startPressed = pressed;
                        }
                    }
                    case MAPPING_TYPE_KEYBOARD_MOUSE: {
                        handleKey(pressed, pController.keyboardMapping.startButton);
                    }
                }
            }
            case KeyEvent.KEYCODE_BUTTON_SELECT -> {
                pController.state.selectPressed = pressed;

                switch (pController.mappingType) {
                    case MAPPING_TYPE_XINPUT: {
                        int index = pController.virtualControllerID;
                        if (index != -1) {
                            connectedVirtualControllers[index].state.selectPressed = pressed;
                        }
                    }
                    case MAPPING_TYPE_KEYBOARD_MOUSE: {
                        handleKey(pressed, pController.keyboardMapping.selectButton);
                    }
                }
            }
            case KeyEvent.KEYCODE_BUTTON_R1 -> {
                pController.state.rbPressed = pressed;

                switch (pController.mappingType) {
                    case MAPPING_TYPE_XINPUT: {
                        int index = pController.virtualControllerID;
                        if (index != -1) {
                            connectedVirtualControllers[index].state.rbPressed = pressed;
                        }
                    }
                    case MAPPING_TYPE_KEYBOARD_MOUSE: {
                        handleKey(pressed, pController.keyboardMapping.rbButton);
                    }
                }
            }
            case KeyEvent.KEYCODE_BUTTON_L1 -> {
                pController.state.lbPressed = pressed;

                switch (pController.mappingType) {
                    case MAPPING_TYPE_XINPUT: {
                        int index = pController.virtualControllerID;
                        if (index != -1) {
                            connectedVirtualControllers[index].state.lbPressed = pressed;
                        }
                    }
                    case MAPPING_TYPE_KEYBOARD_MOUSE: {
                        handleKey(pressed, pController.keyboardMapping.lbButton);
                    }
                }
            }
            case KeyEvent.KEYCODE_BUTTON_R2 -> {
                if (pController.supportAxisTrigger) return;

                pController.state.rt = pressed ? 1F : 0F;

                switch (pController.mappingType) {
                    case MAPPING_TYPE_XINPUT: {
                        int index = pController.virtualControllerID;
                        if (index != -1) {
                            connectedVirtualControllers[index].state.rt = pressed ? 1F : 0F;
                        }
                    }
                    case MAPPING_TYPE_KEYBOARD_MOUSE: {
                        handleKey(pressed, pController.keyboardMapping.rtButton);
                    }
                }
            }
            case KeyEvent.KEYCODE_BUTTON_L2 -> {
                if (pController.supportAxisTrigger) return;

                pController.state.lt = pressed ? 1F : 0F;

                switch (pController.mappingType) {
                    case MAPPING_TYPE_XINPUT: {
                        int index = pController.virtualControllerID;
                        if (index != -1) {
                            connectedVirtualControllers[index].state.lt = pressed ? 1F : 0F;
                        }
                    }
                    case MAPPING_TYPE_KEYBOARD_MOUSE: {
                        handleKey(pressed, pController.keyboardMapping.ltButton);
                    }
                }
            }
            case KeyEvent.KEYCODE_BUTTON_THUMBR -> {
                pController.state.rsPressed = pressed;

                switch (pController.mappingType) {
                    case MAPPING_TYPE_XINPUT: {
                        int index = pController.virtualControllerID;
                        if (index != -1) {
                            connectedVirtualControllers[index].state.rsPressed = pressed;
                        }
                    }
                    case MAPPING_TYPE_KEYBOARD_MOUSE: {
                        handleKey(pressed, pController.keyboardMapping.rsButton);
                    }
                }
            }
            case KeyEvent.KEYCODE_BUTTON_THUMBL -> {
                pController.state.lsPressed = pressed;

                switch (pController.mappingType) {
                    case MAPPING_TYPE_XINPUT: {
                        int index = pController.virtualControllerID;
                        if (index != -1) {
                            connectedVirtualControllers[index].state.lsPressed = pressed;
                        }
                    }
                    case MAPPING_TYPE_KEYBOARD_MOUSE: {
                        handleKey(pressed, pController.keyboardMapping.lsButton);
                    }
                }
            }
        }
    }

    public static int getAxisStatus(float axisX, float axisY, float deadZone) {
        boolean axisXNeutral = (axisX < deadZone && axisX > -deadZone);
        boolean axisYNeutral = (axisY < deadZone && axisY > -deadZone);

        if (axisX > deadZone && axisY < -deadZone) {
            return RIGHT_UP;
        } else if (axisX > deadZone && axisYNeutral) {
            return RIGHT;
        } else if (axisX > deadZone && axisY > deadZone) {
            return RIGHT_DOWN;
        } else if (axisY > deadZone && axisXNeutral) {
            return DOWN;
        } else if (axisY < -deadZone && axisXNeutral) {
            return UP;
        } else if (axisX < -deadZone && axisY > deadZone) {
            return LEFT_DOWN;
        } else if (axisX < -deadZone && axisYNeutral) {
            return LEFT;
        } else if (axisX < -deadZone && axisY < -deadZone) {
            return LEFT_UP;
        }

        return 0;
    }

    public static void handleAxis(float axisX, float axisY, Analog analog, float deadZone) {
        if (analog.isMouseMapping) {
            lastAxisX = axisX;
            lastAxisY = axisY;
            mouseSensibility = getMouseSensibility(getControllerPreset(selectedGameName, controllerIndex)) / 6F;
            return;
        }

        int status = getAxisStatus(axisX, axisY, deadZone);

        switch (status) {
            case LEFT -> {
                lorieView.sendKeyEvent(analog.right.scanCode, analog.right.keyCode, false);
                lorieView.sendKeyEvent(analog.left.scanCode, analog.left.keyCode, true);
                lorieView.sendKeyEvent(analog.up.scanCode, analog.up.keyCode, false);
                lorieView.sendKeyEvent(analog.down.scanCode, analog.down.keyCode, false);
            }
            case RIGHT -> {
                lorieView.sendKeyEvent(analog.right.scanCode, analog.right.keyCode, true);
                lorieView.sendKeyEvent(analog.left.scanCode, analog.left.keyCode, false);
                lorieView.sendKeyEvent(analog.up.scanCode, analog.up.keyCode, false);
                lorieView.sendKeyEvent(analog.down.scanCode, analog.down.keyCode, false);
            }
            case UP -> {
                lorieView.sendKeyEvent(analog.right.scanCode, analog.right.keyCode, false);
                lorieView.sendKeyEvent(analog.left.scanCode, analog.left.keyCode, false);
                lorieView.sendKeyEvent(analog.up.scanCode, analog.up.keyCode, true);
                lorieView.sendKeyEvent(analog.down.scanCode, analog.down.keyCode, false);
            }
            case DOWN -> {
                lorieView.sendKeyEvent(analog.right.scanCode, analog.right.keyCode, false);
                lorieView.sendKeyEvent(analog.left.scanCode, analog.left.keyCode, false);
                lorieView.sendKeyEvent(analog.up.scanCode, analog.up.keyCode, false);
                lorieView.sendKeyEvent(analog.down.scanCode, analog.down.keyCode, true);
            }
            case LEFT_UP -> {
                lorieView.sendKeyEvent(analog.right.scanCode, analog.right.keyCode, false);
                lorieView.sendKeyEvent(analog.left.scanCode, analog.left.keyCode, true);
                lorieView.sendKeyEvent(analog.up.scanCode, analog.up.keyCode, true);
                lorieView.sendKeyEvent(analog.down.scanCode, analog.down.keyCode, false);
            }
            case LEFT_DOWN -> {
                lorieView.sendKeyEvent(analog.right.scanCode, analog.right.keyCode, false);
                lorieView.sendKeyEvent(analog.left.scanCode, analog.left.keyCode, true);
                lorieView.sendKeyEvent(analog.up.scanCode, analog.up.keyCode, false);
                lorieView.sendKeyEvent(analog.down.scanCode, analog.down.keyCode, true);
            }
            case RIGHT_UP -> {
                lorieView.sendKeyEvent(analog.right.scanCode, analog.right.keyCode, true);
                lorieView.sendKeyEvent(analog.left.scanCode, analog.left.keyCode, false);
                lorieView.sendKeyEvent(analog.up.scanCode, analog.up.keyCode, true);
                lorieView.sendKeyEvent(analog.down.scanCode, analog.down.keyCode, false);
            }
            case RIGHT_DOWN -> {
                lorieView.sendKeyEvent(analog.right.scanCode, analog.right.keyCode, true);
                lorieView.sendKeyEvent(analog.left.scanCode, analog.left.keyCode, false);
                lorieView.sendKeyEvent(analog.up.scanCode, analog.up.keyCode, false);
                lorieView.sendKeyEvent(analog.down.scanCode, analog.down.keyCode, true);
            }
            default -> {
                lorieView.sendKeyEvent(analog.right.scanCode, analog.right.keyCode, false);
                lorieView.sendKeyEvent(analog.left.scanCode, analog.left.keyCode, false);
                lorieView.sendKeyEvent(analog.up.scanCode, analog.up.keyCode, false);
                lorieView.sendKeyEvent(analog.down.scanCode, analog.down.keyCode, false);
            }
        }
    }

    public static void updateAxisState(MotionEvent event) {
        controllerIndex = -1;
        for (int i = 0; i < connectedPhysicalControllers.size(); i++) {
            if (connectedPhysicalControllers.get(i).id == event.getDeviceId()) {
                controllerIndex = i;
                break;
            }
        }

        if (controllerIndex == -1) return;

        PhysicalController pController = connectedPhysicalControllers.get(controllerIndex);

        pController.state.lx = event.getAxisValue(MotionEvent.AXIS_X);
        pController.state.ly = event.getAxisValue(MotionEvent.AXIS_Y);
        pController.state.rx = event.getAxisValue(MotionEvent.AXIS_Z);
        pController.state.ry = event.getAxisValue(MotionEvent.AXIS_RZ);

        if (pController.state.lx < -200F) {
            pController.state.lx += 270F;
        }

        pController.state.dpadX = event.getAxisValue(MotionEvent.AXIS_HAT_X);
        pController.state.dpadY = event.getAxisValue(MotionEvent.AXIS_HAT_Y);

        if (pController.supportAxisTrigger) {
            pController.state.lt = event.getAxisValue(MotionEvent.AXIS_LTRIGGER);
            pController.state.rt = event.getAxisValue(MotionEvent.AXIS_RTRIGGER);
        }

        switch (pController.mappingType) {
            case MAPPING_TYPE_XINPUT: {
                int index = pController.virtualControllerID;
                if (index != -1) {
                    if (pController.swapAnalogs) {
                        connectedVirtualControllers[index].state.lx = pController.state.rx;
                        connectedVirtualControllers[index].state.ly = pController.state.ry;
                        connectedVirtualControllers[index].state.rx = pController.state.lx;
                        connectedVirtualControllers[index].state.ry = pController.state.ly;
                    } else {
                        connectedVirtualControllers[index].state.lx = pController.state.lx;
                        connectedVirtualControllers[index].state.ly = pController.state.ly;
                        connectedVirtualControllers[index].state.rx = pController.state.rx;
                        connectedVirtualControllers[index].state.ry = pController.state.ry;
                    }

                    if (pController.supportAxisTrigger) {
                        connectedVirtualControllers[index].state.lt = pController.state.lt;
                        connectedVirtualControllers[index].state.rt = pController.state.rt;
                    }

                    connectedVirtualControllers[index].state.dpadX = pController.state.dpadX;
                    connectedVirtualControllers[index].state.dpadY = pController.state.dpadY;
                }
            }
            case MAPPING_TYPE_KEYBOARD_MOUSE: {
                handleAxis(pController.state.lx, pController.state.ly, pController.keyboardMapping.leftAnalog, pController.deadZone);
                handleAxis(pController.state.rx, pController.state.ry, pController.keyboardMapping.rightAnalog, pController.deadZone);
                handleAxis(pController.state.dpadX, pController.state.dpadY, pController.keyboardMapping.dpad, pController.deadZone);

                if (pController.supportAxisTrigger) {
                    handleKey(pController.state.lt > pController.deadZone, pController.keyboardMapping.ltButton);
                    handleKey(pController.state.rt > pController.deadZone, pController.keyboardMapping.rtButton);
                }
            }
        }
    }

    public static List<PhysicalController> connectedPhysicalControllers = getConnectedControllers();

    private static final int MAPPING_TYPE_KEYBOARD_MOUSE = 0;
    private static final int MAPPING_TYPE_XINPUT = 1;

    public static class PhysicalController {
        String name;
        public int id;
        public int mappingType = MAPPING_TYPE_KEYBOARD_MOUSE;
        public int virtualControllerID;
        boolean supportAxisTrigger;
        public boolean swapAnalogs = false;
        public float deadZone = 0.25F;
        public float mouseSensibility = 1F;
        public KeyboardMapping keyboardMapping = new KeyboardMapping();
        public ControllerState state = new ControllerState();

        public PhysicalController(String name, int id) {
            this.name = name;
            this.id = id;

            InputDevice device = InputDevice.getDevice(id);

            if (device != null) {
                boolean supportsLeftTrigger = device.getMotionRange(MotionEvent.AXIS_LTRIGGER) != null;
                boolean supportsRightTrigger = device.getMotionRange(MotionEvent.AXIS_RTRIGGER) != null;

                this.supportAxisTrigger = (supportsLeftTrigger && supportsRightTrigger);
            }
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }

    public static class ControllerState {
        public boolean aPressed = false;
        public boolean bPressed = false;
        public boolean xPressed = false;
        public boolean yPressed = false;
        public boolean startPressed = false;
        public boolean selectPressed = false;
        public boolean rbPressed = false;
        public boolean lbPressed = false;
        public boolean lsPressed = false;
        public boolean rsPressed = false;
        public float dpadX = 0F;
        public float dpadY = 0F;
        public float lx = 0F;
        public float ly = 0F;
        public float rx = 0F;
        public float ry = 0F;
        public float lt = 0F;
        public float rt = 0F;
    }

    public static class KeyboardMapping {
        public Analog leftAnalog = new Analog();
        public Analog rightAnalog = new Analog();
        public Analog dpad = new Analog();
        public ButtonMapping aButton = new ButtonMapping();
        public ButtonMapping bButton = new ButtonMapping();
        public ButtonMapping xButton = new ButtonMapping();
        public ButtonMapping yButton = new ButtonMapping();
        public ButtonMapping startButton = new ButtonMapping();
        public ButtonMapping selectButton = new ButtonMapping();
        public ButtonMapping lbButton = new ButtonMapping();
        public ButtonMapping rbButton = new ButtonMapping();
        public ButtonMapping ltButton = new ButtonMapping();
        public ButtonMapping rtButton = new ButtonMapping();
        public ButtonMapping lsButton = new ButtonMapping();
        public ButtonMapping rsButton = new ButtonMapping();
    }

    public static class Analog {
        public boolean isMouseMapping;
        public ButtonMapping up;
        public ButtonMapping down;
        public ButtonMapping left;
        public ButtonMapping right;

        public Analog() {
            this.isMouseMapping = false;
            this.up = new ButtonMapping();
            this.down = new ButtonMapping();
            this.left = new ButtonMapping();
            this.right = new ButtonMapping();
        }

        public Analog(boolean isMouseMapping, ButtonMapping up, ButtonMapping down, ButtonMapping left, ButtonMapping right) {
            this.isMouseMapping = isMouseMapping;
            this.up = up;
            this.down = down;
            this.left = left;
            this.right = right;
        }
    }

    public static class VirtualController {
        public boolean connected = false;
        public ControllerState state = new ControllerState();
    }

    public static VirtualController[] connectedVirtualControllers = {
            new VirtualController(), new VirtualController(), new VirtualController(), new VirtualController(),
    };

    public static int connectController() {
        for (int i = 0; i < connectedVirtualControllers.length; i++) {
            if (!connectedVirtualControllers[i].connected) {
                Log.d("ControllerDebug", "Connected Controller on Port " + i);
                connectedVirtualControllers[i].connected = true;
                return i;
            }
        }

        return -1;
    }

    public static void disconnectController(int index) {
        if (index != -1) {
            Log.d("ControllerDebug", "Disconnected Controller on Port " + index);
            connectedVirtualControllers[index].connected = false;
        }
    }

    private static final int CLIENT_PORT = 7941;
    private static final int BUFFER_SIZE = 44;
    private static final int GET_CONNECTION = 1;
    private static final int GET_GAMEPAD_STATE = 2;
    private static final int GET_GAMEPAD_STATE_DINPUT = 3;

    private static boolean inputServerRunning = false;

    public static void startInputServer() {
        if (inputServerRunning) return;

        Log.d("ControllerDebug", "Input Server Initialized.");

        inputServerRunning = true;

        try (DatagramSocket serverSocket = new DatagramSocket(CLIENT_PORT)) {
            while (inputServerRunning) {
                byte[] buffer = new byte[BUFFER_SIZE];
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

                serverSocket.receive(packet);

                Thread.sleep(2);

                ByteBuffer receivedBuffer = ByteBuffer.wrap(buffer);
                switch (receivedBuffer.get(0) & 0xFF) {
                    case GET_CONNECTION -> {
                        DatagramPacket responsePacket = new DatagramPacket(buffer, buffer.length, packet.getAddress(), packet.getPort());
                        serverSocket.send(responsePacket);
                    }
                    case GET_GAMEPAD_STATE -> {
                        for (int i = 0; i < connectedVirtualControllers.length; i++) {
                            buffer[i * 11] = (byte) GET_GAMEPAD_STATE;
                            buffer[1 + (i * 11)] = (byte) (((connectedVirtualControllers[i].connected || i == 0) && enableXInput) ? 1 : 0);

                            int buttonsState1 = 0;
                            
                            buttonsState1 |= (connectedVirtualControllers[i].state.aPressed ? A_BUTTON : 0);
                            buttonsState1 |= (connectedVirtualControllers[i].state.bPressed ? B_BUTTON : 0);
                            buttonsState1 |= (connectedVirtualControllers[i].state.xPressed ? X_BUTTON : 0);
                            buttonsState1 |= (connectedVirtualControllers[i].state.yPressed ? Y_BUTTON : 0);
                            buttonsState1 |= (connectedVirtualControllers[i].state.lbPressed ? LB_BUTTON : 0);
                            buttonsState1 |= (connectedVirtualControllers[i].state.rbPressed ? RB_BUTTON : 0);
                            buttonsState1 |= (connectedVirtualControllers[i].state.lsPressed ? LS_BUTTON : 0);
                            buttonsState1 |= (connectedVirtualControllers[i].state.rsPressed ? RS_BUTTON : 0);

                            int buttonsState2 = 0;

                            buttonsState2 |= (connectedVirtualControllers[i].state.startPressed ? START_BUTTON : 0);
                            buttonsState2 |= (connectedVirtualControllers[i].state.selectPressed ? SELECT_BUTTON : 0);
                            
                            buffer[2 + (i * 11)] = (byte) buttonsState1;
                            buffer[3 + (i * 11)] = (byte) buttonsState2;
                            buffer[4 + (i * 11)] = (byte) getAxisStatus(connectedVirtualControllers[i].state.dpadX, connectedVirtualControllers[i].state.dpadY, 0.25F);

                            int lx = (int) ((connectedVirtualControllers[i].state.lx + 1F) * 127.5F);
                            int ly = (int) ((-connectedVirtualControllers[i].state.ly + 1F) * 127.5F);
                            int rx = (int) ((connectedVirtualControllers[i].state.rx + 1F) * 127.5F);
                            int ry = (int) ((-connectedVirtualControllers[i].state.ry + 1F) * 127.5F);
                            int lt = (int) ((connectedVirtualControllers[i].state.lt * 2F) * 127.5F);
                            int rt = (int) ((connectedVirtualControllers[i].state.rt * 2F) * 127.5F);

                            buffer[5 + (i * 11)] = (byte) lx;
                            buffer[6 + (i * 11)] = (byte) ly;
                            buffer[7 + (i * 11)] = (byte) rx;
                            buffer[8 + (i * 11)] = (byte) ry;
                            buffer[9 + (i * 11)] = (byte) lt;
                            buffer[10 + (i * 11)] = (byte) rt;
                        }

                        DatagramPacket responsePacket = new DatagramPacket(buffer, buffer.length, packet.getAddress(), packet.getPort());
                        serverSocket.send(responsePacket);
                    }
                    case GET_GAMEPAD_STATE_DINPUT -> {
                        for (int i = 0; i < connectedVirtualControllers.length; i++) {
                            buffer[i * 11] = (byte) GET_GAMEPAD_STATE_DINPUT;
                            buffer[1 + (i * 11)] = (byte) (((connectedVirtualControllers[i].connected || i == 0) && enableDInput) ? 1 : 0);

                            int buttonsState1 = 0;

                            buttonsState1 |= (connectedVirtualControllers[i].state.aPressed ? A_BUTTON : 0);
                            buttonsState1 |= (connectedVirtualControllers[i].state.bPressed ? B_BUTTON : 0);
                            buttonsState1 |= (connectedVirtualControllers[i].state.xPressed ? X_BUTTON : 0);
                            buttonsState1 |= (connectedVirtualControllers[i].state.yPressed ? Y_BUTTON : 0);
                            buttonsState1 |= (connectedVirtualControllers[i].state.lbPressed ? LB_BUTTON : 0);
                            buttonsState1 |= (connectedVirtualControllers[i].state.rbPressed ? RB_BUTTON : 0);
                            buttonsState1 |= (connectedVirtualControllers[i].state.lsPressed ? LS_BUTTON : 0);
                            buttonsState1 |= (connectedVirtualControllers[i].state.rsPressed ? RS_BUTTON : 0);

                            int buttonsState2 = 0;

                            buttonsState2 |= (connectedVirtualControllers[i].state.startPressed ? START_BUTTON : 0);
                            buttonsState2 |= (connectedVirtualControllers[i].state.selectPressed ? SELECT_BUTTON : 0);

                            buffer[2 + (i * 11)] = (byte) buttonsState1;
                            buffer[3 + (i * 11)] = (byte) buttonsState2;
                            buffer[4 + (i * 11)] = (byte) getAxisStatus(connectedVirtualControllers[i].state.dpadX, connectedVirtualControllers[i].state.dpadY, 0.25F);

                            int lx = (int) ((connectedVirtualControllers[i].state.lx + 1F) * 127.5F);
                            int ly = (int) ((-connectedVirtualControllers[i].state.ly + 1F) * 127.5F);
                            int rx = (int) ((connectedVirtualControllers[i].state.rx + 1F) * 127.5F);
                            int ry = (int) ((-connectedVirtualControllers[i].state.ry + 1F) * 127.5F);
                            int lt = (int) ((connectedVirtualControllers[i].state.lt * 2F) * 127.5F);
                            int rt = (int) ((connectedVirtualControllers[i].state.rt * 2F) * 127.5F);

                            buffer[5 + (i * 11)] = (byte) lx;
                            buffer[6 + (i * 11)] = (byte) ly;
                            buffer[7 + (i * 11)] = (byte) rx;
                            buffer[8 + (i * 11)] = (byte) ry;
                            buffer[9 + (i * 11)] = (byte) lt;
                            buffer[10 + (i * 11)] = (byte) rt;
                        }

                        DatagramPacket responsePacket = new DatagramPacket(buffer, buffer.length, packet.getAddress(), packet.getPort());
                        serverSocket.send(responsePacket);
                    }
                }
            }
        } catch (SocketException e) {
            Log.e("ControllerDebug", "Failed to create socket. " + e.getMessage());
        } catch (IOException e) {
            Log.e("ControllerDebug", "IOException: " + e.getMessage());
        } catch (InterruptedException e) {
            Log.e("ControllerDebug", "InterruptedException: " + e.getMessage());
        }
    }

    public static void destroyInputServer() {
        inputServerRunning = false;
    }

    private final static int A_BUTTON = 0x01;
    private final static int B_BUTTON = 0x02;
    private final static int X_BUTTON = 0x04;
    private final static int Y_BUTTON = 0x08;
    private final static int RB_BUTTON = 0x10;
    private final static int LB_BUTTON = 0x20;
    private final static int LS_BUTTON = 0x40;
    private final static int RS_BUTTON = 0x80;
    private final static int START_BUTTON = 0x01;
    private final static int SELECT_BUTTON = 0x02;
}