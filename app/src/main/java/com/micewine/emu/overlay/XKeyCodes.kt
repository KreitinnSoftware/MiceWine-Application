package com.micewine.emu.overlay

object XKeyCodes {
    private val xScanCodes = mapOf(
        "Up" to 103, "Down" to 108, "Left" to 105, "Right" to 106,
        "ESC" to 1, "Enter" to 28, "A" to 30, "B" to 48, "C" to 46,
        "D" to 32, "E" to 18, "F" to 33, "G" to 34, "H" to 35,
        "I" to 23, "J" to 36, "K" to 37, "L" to 38, "M" to 50,
        "N" to 49, "O" to 24, "P" to 25, "Q" to 16, "R" to 19,
        "S" to 47, "T" to 20, "U" to 22, "V" to 47, "W" to 17,
        "X" to 45, "Y" to 21, "Z" to 44, "'" to 40
    )

    private val xKeyCodes = mapOf(
        "Up" to 19, "Down" to 20, "Left" to 21, "Right" to 22,
        "ESC" to 111, "Enter" to 66, "A" to 29, "B" to 30, "C" to 31,
        "D" to 32, "E" to 33, "F" to 34, "G" to 35, "H" to 36,
        "I" to 37, "J" to 38, "K" to 39, "L" to 40, "M" to 41,
        "N" to 42, "O" to 43, "P" to 44, "Q" to 45, "R" to 46,
        "S" to 47, "T" to 48, "U" to 49, "V" to 50, "W" to 51,
        "X" to 52, "Y" to 53, "Z" to 54, "'" to 75
    )

    fun getXKeyScanCodes(key: String): MutableList<Int> {
        val scanCode = xScanCodes[key] ?: 0
        val keyCode = xKeyCodes[key] ?: 0
        return mutableListOf(scanCode, keyCode)
    }
}
