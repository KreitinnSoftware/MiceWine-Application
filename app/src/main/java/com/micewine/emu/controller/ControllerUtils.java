package com.micewine.emu.controller;

import static com.micewine.emu.activities.EmulationActivity.handler;
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
import android.view.InputDevice;
import android.view.KeyEvent;
import android.view.MotionEvent;

import com.micewine.emu.LorieView;
import com.micewine.emu.input.InputStub;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import dalvik.annotation.optimization.FastNative;

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
                    case SCROLL_UP -> {
                        if (pressed) lorieView.sendMouseWheelEvent(0F, -100F);
                    }
                    case SCROLL_DOWN -> {
                        if (pressed) lorieView.sendMouseWheelEvent(0F, 100F);
                    }
                    default -> lorieView.sendMouseEvent(0F, 0F, mapping.scanCode, pressed, true);
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

        byte buttonsStateA = 0;
        byte buttonsStateB = 0;

        switch (e.getKeyCode()) {
            case KeyEvent.KEYCODE_BUTTON_Y -> {
                pController.state.yPressed = pressed;

                if (pController.mappingType == MAPPING_TYPE_KEYBOARD_MOUSE) {
                    handleKey(pressed, pController.keyboardMapping.yButton);
                }
            }
            case KeyEvent.KEYCODE_BUTTON_A -> {
                pController.state.aPressed = pressed;

                if (pController.mappingType == MAPPING_TYPE_KEYBOARD_MOUSE) {
                    handleKey(pressed, pController.keyboardMapping.aButton);
                }
            }
            case KeyEvent.KEYCODE_BUTTON_B -> {
                pController.state.bPressed = pressed;

                if (pController.mappingType == MAPPING_TYPE_KEYBOARD_MOUSE) {
                    handleKey(pressed, pController.keyboardMapping.bButton);
                }
            }
            case KeyEvent.KEYCODE_BUTTON_X -> {
                pController.state.xPressed = pressed;

                if (pController.mappingType == MAPPING_TYPE_KEYBOARD_MOUSE) {
                    handleKey(pressed, pController.keyboardMapping.xButton);
                }
            }
            case KeyEvent.KEYCODE_BUTTON_START -> {
                pController.state.startPressed = pressed;

                if (pController.mappingType == MAPPING_TYPE_KEYBOARD_MOUSE) {
                    handleKey(pressed, pController.keyboardMapping.startButton);
                }
            }
            case KeyEvent.KEYCODE_BUTTON_SELECT -> {
                pController.state.selectPressed = pressed;

                if (pController.mappingType == MAPPING_TYPE_KEYBOARD_MOUSE) {
                    handleKey(pressed, pController.keyboardMapping.selectButton);
                }
            }
            case KeyEvent.KEYCODE_BUTTON_R1 -> {
                pController.state.rbPressed = pressed;

                if (pController.mappingType == MAPPING_TYPE_KEYBOARD_MOUSE) {
                    handleKey(pressed, pController.keyboardMapping.rbButton);
                }
            }
            case KeyEvent.KEYCODE_BUTTON_L1 -> {
                pController.state.lbPressed = pressed;

                if (pController.mappingType == MAPPING_TYPE_KEYBOARD_MOUSE) {
                    handleKey(pressed, pController.keyboardMapping.lbButton);
                }
            }
            case KeyEvent.KEYCODE_BUTTON_R2 -> {
                if (pController.supportAxisTrigger) return;

                pController.state.rt = pressed ? 1F : 0F;

                if (pController.mappingType == MAPPING_TYPE_KEYBOARD_MOUSE) {
                    handleKey(pressed, pController.keyboardMapping.rtButton);
                }
            }
            case KeyEvent.KEYCODE_BUTTON_L2 -> {
                if (pController.supportAxisTrigger) return;

                pController.state.lt = pressed ? 1F : 0F;

                if (pController.mappingType == MAPPING_TYPE_KEYBOARD_MOUSE) {
                    handleKey(pressed, pController.keyboardMapping.ltButton);
                }
            }
            case KeyEvent.KEYCODE_BUTTON_THUMBR -> {
                pController.state.rsPressed = pressed;

                if (pController.mappingType == MAPPING_TYPE_KEYBOARD_MOUSE) {
                    handleKey(pressed, pController.keyboardMapping.rsButton);
                }
            }
            case KeyEvent.KEYCODE_BUTTON_THUMBL -> {
                pController.state.lsPressed = pressed;

                if (pController.mappingType == MAPPING_TYPE_KEYBOARD_MOUSE) {
                    handleKey(pressed, pController.keyboardMapping.lsButton);
                }
            }
        }

        if (pController.mappingType == MAPPING_TYPE_XINPUT) {
            buttonsStateA |= (byte) (pController.state.aPressed ? A_BUTTON : 0);
            buttonsStateA |= (byte) (pController.state.bPressed ? B_BUTTON : 0);
            buttonsStateA |= (byte) (pController.state.xPressed ? X_BUTTON : 0);
            buttonsStateA |= (byte) (pController.state.yPressed ? Y_BUTTON : 0);
            buttonsStateA |= (byte) (pController.state.lbPressed ? LB_BUTTON : 0);
            buttonsStateA |= (byte) (pController.state.rbPressed ? RB_BUTTON : 0);
            buttonsStateA |= (byte) (pController.state.lsPressed ? LS_BUTTON : 0);
            buttonsStateA |= (byte) (pController.state.rsPressed ? RS_BUTTON : 0);
            buttonsStateB |= (byte) (pController.state.startPressed ? START_BUTTON : 0);
            buttonsStateB |= (byte) (pController.state.selectPressed ? SELECT_BUTTON : 0);

            updateButtonsStateNative(pController.virtualControllerID, buttonsStateA, buttonsStateB);
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
                        updateAxisStateNative(index, pController.state.rx, pController.state.ry, pController.state.lx, pController.state.ly, pController.state.lt, pController.state.rt, (byte) getAxisStatus(pController.state.dpadX, pController.state.dpadY, 0.25F));
                    } else {
                        updateAxisStateNative(index, pController.state.lx, pController.state.ly, pController.state.rx, pController.state.ry, pController.state.lt, pController.state.rt, (byte) getAxisStatus(pController.state.dpadX, pController.state.dpadY, 0.25F));
                    }
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

    public final static int A_BUTTON = 0x01;
    public final static int B_BUTTON = 0x02;
    public final static int X_BUTTON = 0x04;
    public final static int Y_BUTTON = 0x08;
    public final static int RB_BUTTON = 0x10;
    public final static int LB_BUTTON = 0x20;
    public final static int LS_BUTTON = 0x40;
    public final static int RS_BUTTON = 0x80;
    public final static int START_BUTTON = 0x01;
    public final static int SELECT_BUTTON = 0x02;

    static {
        System.loadLibrary("micewine");
    }

    @FastNative
    public static native int connectController();
    @FastNative
    public static native void disconnectController(int index);
    @FastNative
    public static native void updateAxisStateNative(int index, float lx, float ly, float rx, float ry, float lt, float rt, byte dpadStatus);
    @FastNative
    public static native void updateButtonsStateNative(int index, int buttons, int buttonsB);
    @FastNative
    public static native void startInputServer();
    @FastNative
    public static native void stopInputServer();
}