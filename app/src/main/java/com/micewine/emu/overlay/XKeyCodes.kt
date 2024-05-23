package com.micewine.emu.overlay

object XKeyCodes {
    private const val XSCAN_CODES_DPAD_UP = 103
    private const val XSCAN_CODES_DPAD_DOWN = 108
    private const val XSCAN_CODES_DPAD_LEFT = 105
    private const val XSCAN_CODES_DPAD_RIGHT = 106
    private const val XSCAN_CODES_ESC = 1
    private const val XSCAN_CODES_ENTER = 28
    private const val XSCAN_CODES_A = 30
    private const val XSCAN_CODES_B = 48
    private const val XSCAN_CODES_C = 46
    private const val XSCAN_CODES_D = 32
    private const val XSCAN_CODES_E = 18
    private const val XSCAN_CODES_F = 33
    private const val XSCAN_CODES_G = 34
    private const val XSCAN_CODES_H = 35
    private const val XSCAN_CODES_I = 23
    private const val XSCAN_CODES_J = 36
    private const val XSCAN_CODES_K = 37
    private const val XSCAN_CODES_L = 38
    private const val XSCAN_CODES_M = 50
    private const val XSCAN_CODES_N = 49
    private const val XSCAN_CODES_O = 24
    private const val XSCAN_CODES_P = 25
    private const val XSCAN_CODES_Q = 16
    private const val XSCAN_CODES_R = 19
    private const val XSCAN_CODES_S = 47
    private const val XSCAN_CODES_T = 20
    private const val XSCAN_CODES_U = 22
    private const val XSCAN_CODES_V = 47
    private const val XSCAN_CODES_W = 17
    private const val XSCAN_CODES_X = 45
    private const val XSCAN_CODES_Y = 21
    private const val XSCAN_CODES_Z = 44
    private const val XSCAN_CODES_QUOTES = 40

    private const val XKEY_CODES_DPAD_UP = 19
    private const val XKEY_CODES_DPAD_DOWN = 20
    private const val XKEY_CODES_DPAD_LEFT = 21
    private const val XKEY_CODES_DPAD_RIGHT = 22
    private const val XKEY_CODES_ESC = 111
    private const val XKEY_CODES_ENTER = 66
    private const val XKEY_CODES_A = 29
    private const val XKEY_CODES_B = 30
    private const val XKEY_CODES_C = 31
    private const val XKEY_CODES_D = 32
    private const val XKEY_CODES_E = 33
    private const val XKEY_CODES_F = 34
    private const val XKEY_CODES_G = 35
    private const val XKEY_CODES_H = 36
    private const val XKEY_CODES_I = 37
    private const val XKEY_CODES_J = 38
    private const val XKEY_CODES_K = 39
    private const val XKEY_CODES_L = 40
    private const val XKEY_CODES_M = 41
    private const val XKEY_CODES_N = 42
    private const val XKEY_CODES_O = 43
    private const val XKEY_CODES_P = 44
    private const val XKEY_CODES_Q = 45
    private const val XKEY_CODES_R = 46
    private const val XKEY_CODES_S = 47
    private const val XKEY_CODES_T = 48
    private const val XKEY_CODES_U = 49
    private const val XKEY_CODES_V = 50
    private const val XKEY_CODES_W = 51
    private const val XKEY_CODES_X = 52
    private const val XKEY_CODES_Y = 53
    private const val XKEY_CODES_Z = 54
    private const val XKEY_CODES_QUOTES = 75

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

            "A" -> {
                return mutableListOf(XSCAN_CODES_A, XKEY_CODES_A)
            }

            "B" -> {
                return mutableListOf(XSCAN_CODES_B, XKEY_CODES_B)
            }

            "C" -> {
                return mutableListOf(XSCAN_CODES_C, XKEY_CODES_C)
            }

            "D" -> {
                return mutableListOf(XSCAN_CODES_D, XKEY_CODES_D)
            }

            "E" -> {
                return mutableListOf(XSCAN_CODES_E, XKEY_CODES_E)
            }

            "F" -> {
                return mutableListOf(XSCAN_CODES_F, XKEY_CODES_F)
            }

            "G" -> {
                return mutableListOf(XSCAN_CODES_G, XKEY_CODES_G)
            }

            "H" -> {
                return mutableListOf(XSCAN_CODES_H, XKEY_CODES_H)
            }

            "I" -> {
                return mutableListOf(XSCAN_CODES_I, XKEY_CODES_I)
            }

            "J" -> {
                return mutableListOf(XSCAN_CODES_J, XKEY_CODES_J)
            }

            "K" -> {
                return mutableListOf(XSCAN_CODES_K, XKEY_CODES_K)
            }

            "L" -> {
                return mutableListOf(XSCAN_CODES_L, XKEY_CODES_L)
            }

            "M" -> {
                return mutableListOf(XSCAN_CODES_M, XKEY_CODES_M)
            }

            "N" -> {
                return mutableListOf(XSCAN_CODES_N, XKEY_CODES_N)
            }

            "O" -> {
                return mutableListOf(XSCAN_CODES_O, XKEY_CODES_O)
            }

            "P" -> {
                return mutableListOf(XSCAN_CODES_P, XKEY_CODES_P)
            }

            "Q" -> {
                return mutableListOf(XSCAN_CODES_Q, XKEY_CODES_Q)
            }

            "R" -> {
                return mutableListOf(XSCAN_CODES_R, XKEY_CODES_R)
            }

            "S" -> {
                return mutableListOf(XSCAN_CODES_S, XKEY_CODES_S)
            }

            "T" -> {
                return mutableListOf(XSCAN_CODES_T, XKEY_CODES_T)
            }

            "U" -> {
                return mutableListOf(XSCAN_CODES_U, XKEY_CODES_U)
            }

            "V" -> {
                return mutableListOf(XSCAN_CODES_V, XKEY_CODES_V)
            }

            "W" -> {
                return mutableListOf(XSCAN_CODES_W, XKEY_CODES_W)
            }

            "X" -> {
                return mutableListOf(XSCAN_CODES_X, XKEY_CODES_X)
            }

            "Y" -> {
                return mutableListOf(XSCAN_CODES_Y, XKEY_CODES_Y)
            }

            "Z" -> {
                return mutableListOf(XSCAN_CODES_Z, XKEY_CODES_Z)
            }

            "'" -> {
                return mutableListOf(XSCAN_CODES_QUOTES, XKEY_CODES_QUOTES)
            }

            else -> return mutableListOf(0, 0)
        }
    }
}