package com.micewine.emu.adapters

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.widget.SwitchCompat
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.RecyclerView
import com.micewine.emu.R
import com.micewine.emu.controller.XKeyCodes

class AdapterSettingsController(private val settingsControllerList: List<SettingsController>, private val context: Context) :
    RecyclerView.Adapter<AdapterSettingsController.ViewHolder>() {

    private val preferences: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
    private val keyboardEntries: List<String> = XKeyCodes.getKeyNames()
    private val mouseEntries: Array<String> = arrayOf("Null", "Left", "Middle", "Right")
    private val mappingTypes: Array<String> = arrayOf("Keyboard", "Mouse")

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.settings_controller_item, parent, false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val sList = settingsControllerList[position]

        holder.image.setImageResource(sList.image)

        holder.mappingType.isChecked = preferences.getBoolean("${sList.key}_mappingType", false)

        if (!preferences.getBoolean("${sList.key}_mappingType", false)) {
            // Keyboard
            holder.mappingTypeText.text = "Keyboard"
            holder.keyBindSpinner.adapter = ArrayAdapter(context, android.R.layout.simple_spinner_dropdown_item, keyboardEntries)
            holder.keyBindSpinner.setSelection(keyboardEntries.indexOf(preferences.getString(sList.key, "Null")))
        } else {
            // Mouse
            holder.mappingTypeText.text = "Mouse"
            holder.keyBindSpinner.adapter = ArrayAdapter(context, android.R.layout.simple_spinner_dropdown_item, mouseEntries)
            holder.keyBindSpinner.setSelection(mouseEntries.indexOf(preferences.getString(sList.key, "Null")))
        }

        holder.mappingType.setOnClickListener {
            val editor = preferences.edit()

            editor.putBoolean("${sList.key}_mappingType", !preferences.getBoolean("${sList.key}_mappingType", false))

            editor.apply()

            if (!preferences.getBoolean("${sList.key}_mappingType", false)) {
                // Keyboard
                holder.mappingTypeText.text = "Keyboard"
                holder.keyBindSpinner.adapter = ArrayAdapter(context, android.R.layout.simple_spinner_dropdown_item, keyboardEntries)
                holder.keyBindSpinner.setSelection(0)
            } else {
                // Mouse
                holder.mappingTypeText.text = "Mouse"
                holder.keyBindSpinner.adapter = ArrayAdapter(context, android.R.layout.simple_spinner_dropdown_item, mouseEntries)
                holder.keyBindSpinner.setSelection(0)
            }
        }

        holder.keyBindSpinner.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    val selectedItem = parent?.getItemAtPosition(position).toString()

                    val editor = preferences.edit()

                    editor.putString(sList.key, selectedItem)

                    editor.apply()
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                }
            }
    }

    override fun getItemCount(): Int {
        return settingsControllerList.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
        val keyBindSpinner: Spinner
        val image: ImageView
        val mappingType: SwitchCompat
        val mappingTypeText: TextView

        init {
            mappingType = itemView.findViewById(R.id.mappingType)
            mappingTypeText = itemView.findViewById(R.id.mappingTypeText)
            keyBindSpinner = itemView.findViewById(R.id.keyBindSpinner)
            image = itemView.findViewById(R.id.buttonImageView)
            itemView.setOnClickListener(this)
        }

        override fun onClick(v: View) {
        }
    }

    class SettingsController(var image: Int, var key: String)
}