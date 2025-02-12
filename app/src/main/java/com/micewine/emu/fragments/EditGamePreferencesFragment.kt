package com.micewine.emu.fragments

import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import androidx.fragment.app.DialogFragment
import androidx.preference.PreferenceManager
import com.micewine.emu.R
import com.micewine.emu.activities.MainActivity.Companion.ACTION_SELECT_ICON
import com.micewine.emu.activities.MainActivity.Companion.selectedGameArray
import com.micewine.emu.fragments.Box64PresetManagerFragment.Companion.getBox64Presets
import com.micewine.emu.fragments.ControllerPresetManagerFragment.Companion.getControllerPresets
import com.micewine.emu.fragments.ShortcutsFragment.Companion.editGameFromList
import com.micewine.emu.fragments.VirtualControllerPresetManagerFragment.Companion.getVirtualControllerPresets

class EditGamePreferencesFragment : DialogFragment() {
    private var preferences: SharedPreferences? = null
    private var imageView: ImageView? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val inflater = requireActivity().layoutInflater
        val view = inflater.inflate(R.layout.fragment_edit_game_preferences, null)

        val editTextNewName = view.findViewById<EditText>(R.id.editTextNewName)
        val editTextArguments = view.findViewById<EditText>(R.id.appArgumentsEditText)
        val buttonContinue = view.findViewById<Button>(R.id.buttonContinue)
        val buttonCancel = view.findViewById<Button>(R.id.buttonCancel)
        val selectedControllerProfileSpinner = view.findViewById<Spinner>(R.id.selectedControllerProfile)
        val selectedVirtualControllerProfileSpinner = view.findViewById<Spinner>(R.id.selectedVirtualControllerProfile)
        val selectedBox64ProfileSpinner = view.findViewById<Spinner>(R.id.selectedBox64Profile)

        imageView = view.findViewById(R.id.imageView)

        val imageBitmap = BitmapFactory.decodeFile(selectedGameArray[2])

        if (imageBitmap != null) {
            imageView?.setImageBitmap(
                resizeBitmap(
                    imageBitmap, imageView?.layoutParams?.width!!, imageView?.layoutParams?.height!!
                )
            )
        }

        val dialog = AlertDialog.Builder(requireContext(), R.style.CustomAlertDialog).setView(view).create()

        preferences = PreferenceManager.getDefaultSharedPreferences(requireContext())

        editTextNewName.setText(selectedGameArray[0])
        editTextArguments.setText(selectedGameArray[3])

        imageView?.setOnClickListener {
            requireActivity().sendBroadcast(
                Intent(ACTION_SELECT_ICON)
            )
        }

        val controllerProfilesNames: MutableList<String> = mutableListOf("--")
        val virtualControllerProfilesNames: MutableList<String> = mutableListOf("--")
        val box64ProfilesNames: MutableList<String> = mutableListOf("--")

        getControllerPresets().forEach {
            controllerProfilesNames.add(it[0])
        }

        getVirtualControllerPresets().forEach {
            virtualControllerProfilesNames.add(it.name)
        }

        getBox64Presets().forEach {
            box64ProfilesNames.add(it[0])
        }

        selectedControllerProfileSpinner.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, controllerProfilesNames)
        selectedVirtualControllerProfileSpinner.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, virtualControllerProfilesNames)
        selectedBox64ProfileSpinner.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, box64ProfilesNames)

        buttonContinue.setOnClickListener {
            val newName = editTextNewName.text.toString()
            val newArguments = editTextArguments.text.toString()

            if (newName == "") {
                return@setOnClickListener
            }

            editGameFromList(preferences!!, selectedGameArray, newName, newArguments)

            dismiss()
        }

        buttonCancel.setOnClickListener {
            dismiss()
        }

        return dialog
    }

    override fun onResume() {
        val imageBitmap = BitmapFactory.decodeFile(selectedGameArray[2])

        if (imageBitmap != null) {
            imageView?.setImageBitmap(
                resizeBitmap(
                    imageBitmap, imageView?.layoutParams?.width!!, imageView?.layoutParams?.height!!
                )
            )
        }

        super.onResume()
    }

    private fun resizeBitmap(originalBitmap: Bitmap, width: Int, height: Int): Bitmap {
        return Bitmap.createScaledBitmap(originalBitmap, width, height, false)
    }
}