package com.micewine.emu.overlay

object XKeyCodes {
    private val scanCodes = mapOf(
        "Up" to 103, "Down" to 108, "Left" to 105, "Right" to 106,
        "ESC" to 1, "Enter" to 28, "A" to 30, "B" to 48, "C" to 46,
        "D" to 32, "E" to 18, "F" to 33, "G" to 34, "H" to 35,
        "I" to 23, "J" to 36, "K" to 37, "L" to 38, "M" to 50,
        "N" to 49, "O" to 24, "P" to 25, "Q" to 16, "R" to 19,
        "S" to 31, "T" to 20, "U" to 22, "V" to 47, "W" to 17,
        "X" to 45, "Y" to 21, "Z" to 44, "'" to 40, "LCtrl" to 29,
        "RCtrl" to 97, "LShift" to 42, "RShift" to 54, "Tab" to 15,
        "Space" to 57, "AltLeft" to 56, "F1" to 59, "F2" to 60,
        "F3" to 61, "F4" to 62, "F5" to 63, "F6" to 64, "F7" to 65,
        "F8" to 66, "F9" to 67, "F10" to 68, "F11" to 87, "F12" to 88,
        "Insert" to 110, "Home" to 102, "PageUp" to 104, "Delete" to 111,
        "End" to 107, "PageDown" to 109, "0" to 82, "1" to 79
    )

    private val keyCodes = mapOf(
        "Up" to 19, "Down" to 20, "Left" to 21, "Right" to 22,
        "ESC" to 111, "Enter" to 66, "A" to 29, "B" to 30, "C" to 31,
        "D" to 32, "E" to 33, "F" to 34, "G" to 35, "H" to 36,
        "I" to 37, "J" to 38, "K" to 39, "L" to 40, "M" to 41,
        "N" to 42, "O" to 43, "P" to 44, "Q" to 45, "R" to 46,
        "S" to 47, "T" to 48, "U" to 49, "V" to 50, "W" to 51,
        "X" to 52, "Y" to 53, "Z" to 54, "'" to 75, "LCtrl" to 113,
        "RCtrl" to 114, "LShift" to 59, "RShift" to 60, "Tab" to 61,
        "Space" to 62, "AltLeft" to 57, "F1" to 131, "F2" to 132,
        "F3" to 133, "F4" to 134, "F5" to 135, "F6" to 136, "F7" to 137,
        "F8" to 138, "F9" to 139, "F10" to 140, "F11" to 141, "F12" to 142,
        "Insert" to 124, "Home" to 122, "PageUp" to 92, "Delete" to 112,
        "End" to 123, "PageDown" to 93, "0" to 144, "1" to 145
    )

    fun getXKeyScanCodes(key: String): MutableList<Int> {
        val scanCode = scanCodes[key] ?: 0
        val keyCode = keyCodes[key] ?: 0
        return mutableListOf(scanCode, keyCode, 0)
    }
}
