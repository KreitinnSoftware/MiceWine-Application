package com.micewine.emu.fragments

import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.fragment.app.DialogFragment
import androidx.preference.PreferenceManager
import com.micewine.emu.R
import com.micewine.emu.activities.ControllerMapperActivity.Companion.addControllerPreset
import com.micewine.emu.activities.GeneralSettingsActivity.Companion.WINE_PREFIX
import com.micewine.emu.activities.MainActivity.Companion.ACTION_CREATE_WINEPREFIX
import com.micewine.emu.activities.MainActivity.Companion.setSharedVars
import com.micewine.emu.activities.MainActivity.Companion.winePrefix

class CreatePresetFragment(private val presetType: Int) : DialogFragment() {
    var preferences: SharedPreferences? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val inflater = requireActivity().layoutInflater
        val view = inflater.inflate(R.layout.fragment_create_preset, null)

        val editTextNewName = view.findViewById<EditText>(R.id.editTextNewName)
        val buttonContinue = view.findViewById<Button>(R.id.buttonContinue)

        val dialog = AlertDialog.Builder(requireContext()).setView(view).create()

        preferences = PreferenceManager.getDefaultSharedPreferences(requireContext())!!

        buttonContinue.setOnClickListener {
            val newName = editTextNewName.text.toString().replace(" ", "_")

            if (newName == "") {
                dismiss()
            }

            if (presetType == WINEPREFIX_PRESET) {
                preferences!!.edit().apply {
                    putString(WINE_PREFIX, newName)
                    apply()
                }

                setSharedVars(requireActivity())

                val createWinePrefixIntent = Intent(ACTION_CREATE_WINEPREFIX).apply {
                    putExtra("winePrefix", winePrefix?.path)
                }

                requireContext().sendBroadcast(createWinePrefixIntent)
            } else if (presetType == CONTROLLER_PRESET) {
                addControllerPreset(requireContext(), newName)
            }

            dismiss()
        }

        return dialog
    }

    companion object {
        const val WINEPREFIX_PRESET = 1
        const val CONTROLLER_PRESET = 2
    }
}