package com.micewine.emu.overlay

object XKeyCodes {
    const val XSCAN_CODES_DPAD_UP = 103
    const val XSCAN_CODES_DPAD_DOWN = 108
    const val XSCAN_CODES_DPAD_LEFT = 105
    const val XSCAN_CODES_DPAD_RIGHT = 106
    const val XSCAN_CODES_ESC = 1
    const val XSCAN_CODES_ENTER = 28
    const val XSCAN_CODES_Z = 44
    const val XSCAN_CODES_X = 45
    const val XSCAN_CODES_C = 46

    const val XKEY_CODES_DPAD_UP = 19
    const val XKEY_CODES_DPAD_DOWN = 20
    const val XKEY_CODES_DPAD_LEFT = 21
    const val XKEY_CODES_DPAD_RIGHT = 22
    const val XKEY_CODES_ESC = 111
    const val XKEY_CODES_ENTER = 66
    const val XKEY_CODES_Z = 54
    const val XKEY_CODES_X = 52
    const val XKEY_CODES_C = 31

    fun getXKeyScanCodes(key: String): MutableList<Int> {
        when (key) {
            "ESC" -> {
                return mutableListOf(XSCAN_CODES_ESC, XKEY_CODES_ESC)
            }

            "Enter" -> {
                return mutableListOf(XSCAN_CODES_ENTER, XKEY_CODES_ENTER)
            }

            "Up" -> {
                return mutableListOf(XSCAN_CODES_DPAD_UP, XKEY_CODES_DPAD_UP)
            }

            "Down" -> {
                return mutableListOf(XSCAN_CODES_DPAD_DOWN, XKEY_CODES_DPAD_DOWN)
            }

            "Left" -> {
                return mutableListOf(XSCAN_CODES_DPAD_LEFT, XKEY_CODES_DPAD_LEFT)
            }

            "Right" -> {
                return mutableListOf(XSCAN_CODES_DPAD_RIGHT, XKEY_CODES_DPAD_RIGHT)
            }

            "Z" -> {
                return mutableListOf(XSCAN_CODES_Z, XKEY_CODES_Z)
            }

            "X" -> {
                return mutableListOf(XSCAN_CODES_X, XKEY_CODES_X)
            }

            "C" -> {
                return mutableListOf(XSCAN_CODES_C, XKEY_CODES_C)
            }

            else -> return mutableListOf(0, 0)
        }
    }
}