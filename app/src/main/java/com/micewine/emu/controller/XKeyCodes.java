package com.micewine.emu.controller;

import static com.micewine.emu.controller.ControllerUtils.KEYBOARD;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class XKeyCodes {
    private record ScanKeyCode(String name, int scanCode, int keyCode) {}

    private static final ScanKeyCode[] scanKeyCodes = {
            new ScanKeyCode("Up", 103, 19),
            new ScanKeyCode("Down", 108, 20),
            new ScanKeyCode("Left", 105, 21),
            new ScanKeyCode("Right", 106, 22),
            new ScanKeyCode("ESC", 1, 111),
            new ScanKeyCode("Enter", 28, 66),
            new ScanKeyCode("Space", 57, 62),
            new ScanKeyCode("A", 30, 29),
            new ScanKeyCode("B", 48, 30),
            new ScanKeyCode("C", 46, 31),
            new ScanKeyCode("D", 32, 32),
            new ScanKeyCode("E", 18, 33),
            new ScanKeyCode("F", 33, 34),
            new ScanKeyCode("G", 34, 35),
            new ScanKeyCode("H", 35, 36),
            new ScanKeyCode("I", 23, 37),
            new ScanKeyCode("J", 36, 38),
            new ScanKeyCode("K", 37, 39),
            new ScanKeyCode("L", 38, 40),
            new ScanKeyCode("M", 50, 41),
            new ScanKeyCode("N", 49, 42),
            new ScanKeyCode("O", 24, 43),
            new ScanKeyCode("P", 25, 44),
            new ScanKeyCode("Q", 16, 45),
            new ScanKeyCode("R", 19, 46),
            new ScanKeyCode("S", 31, 47),
            new ScanKeyCode("T", 20, 48),
            new ScanKeyCode("U", 22, 49),
            new ScanKeyCode("V", 47, 50),
            new ScanKeyCode("W", 17, 51),
            new ScanKeyCode("X", 45, 52),
            new ScanKeyCode("Y", 21, 53),
            new ScanKeyCode("Z", 44, 54),
            new ScanKeyCode("'", 40, 75),
            new ScanKeyCode("LCtrl", 29, 113),
            new ScanKeyCode("RCtrl", 97, 114),
            new ScanKeyCode("LShift", 42, 59),
            new ScanKeyCode("RShift", 54, 60),
            new ScanKeyCode("Tab", 15, 61),
            new ScanKeyCode("AltLeft", 56, 57),
            new ScanKeyCode("F1", 59, 131),
            new ScanKeyCode("F2", 60, 132),
            new ScanKeyCode("F3", 61, 133),
            new ScanKeyCode("F4", 62, 134),
            new ScanKeyCode("F5", 63, 135),
            new ScanKeyCode("F6", 64, 136),
            new ScanKeyCode("F7", 65, 137),
            new ScanKeyCode("F8", 66, 138),
            new ScanKeyCode("F9", 67, 139),
            new ScanKeyCode("F10", 68, 140),
            new ScanKeyCode("F11", 87, 141),
            new ScanKeyCode("F12", 88, 142),
            new ScanKeyCode("Insert", 110, 124),
            new ScanKeyCode("Home", 102, 122),
            new ScanKeyCode("PageUp", 104, 92),
            new ScanKeyCode("Delete", 111, 112),
            new ScanKeyCode("End", 107, 123),
            new ScanKeyCode("PageDown", 109, 93),
            new ScanKeyCode("BackSpace", 14, 67),
            new ScanKeyCode("0", 11, 7),
            new ScanKeyCode("1", 2, 8),
            new ScanKeyCode("2", 3, 9),
            new ScanKeyCode("3", 4, 10),
            new ScanKeyCode("4", 5, 11),
            new ScanKeyCode("5", 6, 12),
            new ScanKeyCode("6", 7, 13),
            new ScanKeyCode("7", 8, 14),
            new ScanKeyCode("8", 9, 15),
            new ScanKeyCode("9", 10, 16),
    };

    public static List<String> getKeyNames(boolean getMouseButtons) {
        final ArrayList<String> keyNames = new ArrayList<>();

        keyNames.add("--");

        if (getMouseButtons) {
            keyNames.add("M_Left");
            keyNames.add("M_Middle");
            keyNames.add("M_Right");
            keyNames.add("M_WheelUp");
            keyNames.add("M_WheelDown");
        } else {
            keyNames.add("Mouse");
        }

        for (ScanKeyCode scanKeyCode : scanKeyCodes) {
            keyNames.add(scanKeyCode.name);
        }

        return keyNames;
    }

    public static ButtonMapping getMapping(String key) {
        ScanKeyCode mapping = null;

        for (ScanKeyCode scanKeyCode : scanKeyCodes) {
            if (scanKeyCode.name.equals(key)) {
                mapping = scanKeyCode;
            }
        }

        if (mapping == null) {
            return new ButtonMapping(key);
        }

        return new ButtonMapping(key, mapping.scanCode, mapping.keyCode, KEYBOARD);
    }

    public static class ButtonMapping {
        String name;
        int scanCode;
        int keyCode;
        int type;

        public ButtonMapping() {
            this.name = "";
            this.scanCode = -1;
            this.keyCode = -1;
            this.type = -1;
        }

        public ButtonMapping(String name) {
            this.name = name;
            this.scanCode = -1;
            this.keyCode = -1;
            this.type = -1;
        }

        public ButtonMapping(String name, int scanCode, int keyCode, int type) {
            this.name = name;
            this.scanCode = scanCode;
            this.keyCode = keyCode;
            this.type = type;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }
}
