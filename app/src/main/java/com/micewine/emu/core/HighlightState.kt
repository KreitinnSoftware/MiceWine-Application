package com.micewine.emu.core

sealed class HighlightState(val ordinal: Int) {
    data object HIGHLIGHT_SHORTCUTS : HighlightState(0)
    data object HIGHLIGHT_SETTINGS : HighlightState(1)
    data object HIGHLIGHT_FILES : HighlightState(2)
    data object HIGHLIGHT_DONE : HighlightState(3)


    companion object {
        const val HIGHLIGHT_PREFERENCE_KEY = "tutorial_state"
        fun fromOrdinal(int: Int?): HighlightState = when (int) {
            0 -> HIGHLIGHT_SHORTCUTS
            1 -> HIGHLIGHT_SETTINGS
            2 -> HIGHLIGHT_FILES
            else -> HIGHLIGHT_DONE
        }
    }
}