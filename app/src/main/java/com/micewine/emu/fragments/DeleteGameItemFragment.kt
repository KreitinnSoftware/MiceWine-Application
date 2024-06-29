package com.micewine.emu.fragments

import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.fragment.app.DialogFragment
import com.micewine.emu.R
import com.micewine.emu.activities.MainActivity.Companion.ACTION_UPDATE_FILE_MANAGER
import com.micewine.emu.activities.MainActivity.Companion.removeGameFromList
import com.micewine.emu.activities.MainActivity.Companion.selectedFile
import com.micewine.emu.activities.MainActivity.Companion.selectedFragment
import com.micewine.emu.activities.MainActivity.Companion.selectedGameArray
import java.io.File

class DeleteGameItemFragment : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val inflater = requireActivity().layoutInflater
        val view = inflater.inflate(R.layout.fragment_delete_game_item, null)

        val buttonContinue = view.findViewById<Button>(R.id.buttonContinue)
        val buttonCancel = view.findViewById<Button>(R.id.buttonCancel)

        val dialog = AlertDialog.Builder(requireContext(), R.style.CustomAlertDialog)
            .setView(view)
            .create()

        buttonContinue.setOnClickListener {
            if (selectedFragment == "HomeFragment") {
                removeGameFromList(requireContext(), selectedGameArray)
            } else if (selectedFragment == "FileManagerFragment") {
                File(selectedFile).delete()

                context?.sendBroadcast(
                    Intent(ACTION_UPDATE_FILE_MANAGER)
                )
            }

            dialog.dismiss()
        }

        buttonCancel.setOnClickListener {
            dialog.dismiss()
        }

        return dialog
    }
}