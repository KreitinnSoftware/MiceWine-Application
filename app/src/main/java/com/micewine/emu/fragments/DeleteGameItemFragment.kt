package com.micewine.emu.fragments

import android.app.AlertDialog
import android.app.Dialog
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import androidx.fragment.app.DialogFragment
import androidx.preference.PreferenceManager
import com.micewine.emu.R
import com.micewine.emu.activities.MainActivity.Companion.selectedFile
import com.micewine.emu.activities.MainActivity.Companion.selectedFragment
import com.micewine.emu.activities.MainActivity.Companion.selectedGameArray
import com.micewine.emu.fragments.FileManagerFragment.Companion.deleteFile
import com.micewine.emu.fragments.ShortcutsFragment.Companion.removeGameFromList

class DeleteGameItemFragment : DialogFragment() {
    private var preferences: SharedPreferences? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val inflater = requireActivity().layoutInflater
        val view = inflater.inflate(R.layout.fragment_delete_game_item, null)

        val buttonContinue = view.findViewById<Button>(R.id.buttonContinue)
        val buttonCancel = view.findViewById<Button>(R.id.buttonCancel)

        val dialog = AlertDialog.Builder(requireContext(), R.style.CustomAlertDialog).setView(view).create()

        preferences = PreferenceManager.getDefaultSharedPreferences(requireContext())

        buttonContinue.setOnClickListener {
            if (selectedFragment == "HomeFragment") {
                removeGameFromList(preferences!!, selectedGameArray)
            } else if (selectedFragment == "FileManagerFragment") {
                deleteFile(selectedFile)
            }

            dialog.dismiss()
        }

        buttonCancel.setOnClickListener {
            dialog.dismiss()
        }

        return dialog
    }
}