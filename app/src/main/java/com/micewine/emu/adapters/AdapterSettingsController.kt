package com.micewine.emu.adapters

import android.content.Context
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
import com.micewine.emu.activities.ControllerMapper
import com.micewine.emu.activities.ControllerMapper.Companion.editControllerPreset
import com.micewine.emu.activities.ControllerMapper.Companion.getMapping
import com.micewine.emu.controller.XKeyCodes

class AdapterSettingsController(private val settingsControllerList: List<SettingsController>, private val context: Context) :
    RecyclerView.Adapter<AdapterSettingsController.ViewHolder>() {

    private val preferences = PreferenceManager.getDefaultSharedPreferences(context)!!
    private val keyboardEntries: List<String> = XKeyCodes.getKeyNames()
    private val mouseEntries: Array<String> = arrayOf("Null", "Left", "Middle", "Right")

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.adapter_settings_controller_item, parent, false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val sList = settingsControllerList[position]

        holder.image.setImageResource(sList.image)
        
        var mapping = getMapping(context, preferences.getString(ControllerMapper.SELECTED_CONTROLLER_PRESET_KEY, "default")!!, sList.key)

        holder.mappingType.isChecked = mapping[1].toBoolean()

        if (mapping[1].toBoolean()) {
            holder.mappingTypeText.text = context.getString(R.string.mouse)
            holder.keyBindSpinner.adapter = ArrayAdapter(context, android.R.layout.simple_spinner_dropdown_item, mouseEntries)
            holder.keyBindSpinner.setSelection(mouseEntries.indexOf(mapping[0]))
        } else {
            holder.mappingTypeText.text = context.getString(R.string.keyboard)
            holder.keyBindSpinner.adapter = ArrayAdapter(context, android.R.layout.simple_spinner_dropdown_item, keyboardEntries)
            holder.keyBindSpinner.setSelection(keyboardEntries.indexOf(mapping[0]))
        }

        holder.mappingType.setOnClickListener {
            val selectedItem = holder.keyBindSpinner.selectedItem.toString()

            val mappingType = if (holder.mappingType.isChecked) {
                "true"
            } else {
                ""
            }

            editControllerPreset(context, preferences.getString(ControllerMapper.SELECTED_CONTROLLER_PRESET_KEY, "default")!!, sList.key, selectedItem, mappingType)

            mapping = getMapping(context, preferences.getString(ControllerMapper.SELECTED_CONTROLLER_PRESET_KEY, "default")!!, sList.key)

            if (mapping[1].toBoolean()) {
                holder.mappingTypeText.text = context.getString(R.string.mouse)
                holder.keyBindSpinner.adapter = ArrayAdapter(context, android.R.layout.simple_spinner_dropdown_item, mouseEntries)
                holder.keyBindSpinner.setSelection(mouseEntries.indexOf(mapping[0]))
            } else {
                holder.mappingTypeText.text = context.getString(R.string.keyboard)
                holder.keyBindSpinner.adapter = ArrayAdapter(context, android.R.layout.simple_spinner_dropdown_item, keyboardEntries)
                holder.keyBindSpinner.setSelection(keyboardEntries.indexOf(mapping[0]))
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
                    val selectedItem = parent?.selectedItem.toString()

                    val mappingType = if (holder.mappingType.isChecked) {
                        "true"
                    } else {
                        ""
                    }

                    editControllerPreset(context, preferences.getString(ControllerMapper.SELECTED_CONTROLLER_PRESET_KEY, "default")!!, sList.key, selectedItem, mappingType)
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                }
            }
    }

    override fun getItemCount(): Int {
        return settingsControllerList.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
        val keyBindSpinner: Spinner = itemView.findViewById(R.id.keyBindSpinner)
        val image: ImageView = itemView.findViewById(R.id.buttonImageView)
        val mappingType: SwitchCompat = itemView.findViewById(R.id.mappingType)
        val mappingTypeText: TextView = itemView.findViewById(R.id.mappingTypeText)

        init {
            itemView.setOnClickListener(this)
        }

        override fun onClick(v: View) {
        }
    }

    class SettingsController(var image: Int, var key: String)
}