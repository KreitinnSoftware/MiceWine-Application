package com.micewine.emu.fragments

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.fragment.app.DialogFragment
import com.micewine.emu.R
import com.micewine.emu.activities.MainActivity.Companion.renameGameFromList
import com.micewine.emu.activities.MainActivity.Companion.selectedGameArray

class RenameGameItemFragment : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val inflater = requireActivity().layoutInflater
        val view = inflater.inflate(R.layout.fragment_rename_game_item, null)

        val editTextNewName = view.findViewById<EditText>(R.id.editTextNewName)
        val buttonContinue = view.findViewById<Button>(R.id.buttonContinue)

        val dialog = AlertDialog.Builder(requireContext(), R.style.CustomAlertDialog)
            .setView(view)
            .create()

        buttonContinue.setOnClickListener {
            val newName = editTextNewName.text.toString()

            if (newName == "") {
                dialog.dismiss()
            }

            renameGameFromList(requireContext(), selectedGameArray, newName)

            dialog.dismiss()
        }

        return dialog
    }
}