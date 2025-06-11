package com.micewine.emu.fragments

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.widget.Button
import androidx.fragment.app.DialogFragment
import com.micewine.emu.R
import com.micewine.emu.activities.MainActivity.Companion.selectedFile
import com.micewine.emu.activities.MainActivity.Companion.selectedFragmentId
import com.micewine.emu.adapters.AdapterGame.Companion.selectedGameName
import com.micewine.emu.adapters.AdapterPreset.Companion.clickedPresetName
import com.micewine.emu.adapters.AdapterPreset.Companion.clickedPresetType
import com.micewine.emu.fragments.Box64PresetManagerFragment.Companion.deleteBox64Preset
import com.micewine.emu.fragments.ControllerPresetManagerFragment.Companion.deleteControllerPreset
import com.micewine.emu.fragments.CreatePresetFragment.Companion.BOX64_PRESET
import com.micewine.emu.fragments.CreatePresetFragment.Companion.CONTROLLER_PRESET
import com.micewine.emu.fragments.CreatePresetFragment.Companion.VIRTUAL_CONTROLLER_PRESET
import com.micewine.emu.fragments.CreatePresetFragment.Companion.WINEPREFIX_PRESET
import com.micewine.emu.fragments.FileManagerFragment.Companion.deleteFile
import com.micewine.emu.fragments.ShortcutsFragment.Companion.removeGameFromList
import com.micewine.emu.fragments.VirtualControllerPresetManagerFragment.Companion.deleteVirtualControllerPreset
import com.micewine.emu.fragments.WinePrefixManagerFragment.Companion.deleteWinePrefix

class DeleteItemFragment(private val deleteType: Int) : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val inflater = requireActivity().layoutInflater
        val view = inflater.inflate(R.layout.fragment_delete_item, null)

        val buttonContinue = view.findViewById<Button>(R.id.buttonContinue)
        val buttonCancel = view.findViewById<Button>(R.id.buttonCancel)

        val dialog = AlertDialog.Builder(requireContext(), R.style.CustomAlertDialog).setView(view).create()

        buttonContinue.setOnClickListener {
            when (deleteType) {
                DELETE_GAME_ITEM -> {
                    if (selectedFragmentId == 0) {
                        removeGameFromList(selectedGameName)
                    } else if (selectedFragmentId == 2) {
                        deleteFile(selectedFile)
                    }
                }
                DELETE_PRESET -> {
                    when (clickedPresetType) {
                        CONTROLLER_PRESET -> deleteControllerPreset(clickedPresetName)
                        VIRTUAL_CONTROLLER_PRESET -> deleteVirtualControllerPreset(clickedPresetName)
                        BOX64_PRESET -> deleteBox64Preset(clickedPresetName)
                        WINEPREFIX_PRESET -> deleteWinePrefix(clickedPresetName)
                    }
                }
            }

            dismiss()
        }

        buttonCancel.setOnClickListener {
            dismiss()
        }

        return dialog
    }

    companion object {
        const val DELETE_GAME_ITEM = 0
        const val DELETE_PRESET = 1
    }
}