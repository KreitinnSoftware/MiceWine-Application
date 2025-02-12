package com.micewine.emu.adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.RadioButton
import android.widget.TextView
import androidx.fragment.app.FragmentManager
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.RecyclerView
import com.micewine.emu.R
import com.micewine.emu.activities.PresetManagerActivity.Companion.ACTION_EDIT_BOX64_PRESET
import com.micewine.emu.activities.PresetManagerActivity.Companion.ACTION_EDIT_CONTROLLER_MAPPING
import com.micewine.emu.activities.PresetManagerActivity.Companion.SELECTED_BOX64_PRESET_KEY
import com.micewine.emu.activities.PresetManagerActivity.Companion.SELECTED_CONTROLLER_PRESET_KEY
import com.micewine.emu.activities.PresetManagerActivity.Companion.SELECTED_VIRTUAL_CONTROLLER_PRESET_KEY
import com.micewine.emu.activities.VirtualControllerOverlayMapper
import com.micewine.emu.fragments.CreatePresetFragment.Companion.BOX64_PRESET
import com.micewine.emu.fragments.DeleteItemFragment
import com.micewine.emu.fragments.DeleteItemFragment.Companion.DELETE_BOX64_PRESET
import com.micewine.emu.fragments.DeleteItemFragment.Companion.DELETE_CONTROLLER_PRESET
import com.micewine.emu.fragments.DeleteItemFragment.Companion.DELETE_VIRTUAL_CONTROLLER_PRESET

class AdapterPreset(private val settingsList: MutableList<Item>, private val context: Context, private val supportFragmentManager: FragmentManager) :
    RecyclerView.Adapter<AdapterPreset.ViewHolder>() {

    val preferences = PreferenceManager.getDefaultSharedPreferences(context)!!

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.adapter_preset_item, parent, false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val sList = settingsList[position]

        holder.settingsName.text = sList.titleSettings

        if (sList.userPreset) {
            holder.editPresetButton.visibility = View.VISIBLE
            holder.deletePresetButton.visibility = View.VISIBLE
        } else {
            holder.editPresetButton.visibility = View.GONE
            holder.deletePresetButton.visibility = View.GONE
        }

        when (sList.type) {
            PHYSICAL_CONTROLLER -> {
                if (sList.titleSettings == preferences.getString(SELECTED_CONTROLLER_PRESET_KEY, "default")) {
                    selectedPresetId = position
                }
                holder.radioButton.setOnClickListener {
                    preferences.edit().apply {
                        putString(SELECTED_CONTROLLER_PRESET_KEY, holder.settingsName.text.toString())
                        apply()
                    }
                    selectedPresetId = holder.adapterPosition
                    notifyItemRangeChanged(0, settingsList.size)
                }
            }
            VIRTUAL_CONTROLLER -> {
                if (sList.titleSettings == preferences.getString(SELECTED_VIRTUAL_CONTROLLER_PRESET_KEY, "default")) {
                    selectedPresetId = position
                }
                holder.radioButton.setOnClickListener {
                    preferences.edit().apply {
                        putString(SELECTED_VIRTUAL_CONTROLLER_PRESET_KEY, holder.settingsName.text.toString())
                        apply()
                    }
                    selectedPresetId = holder.adapterPosition
                    notifyItemRangeChanged(0, settingsList.size)
                }
            }
            BOX64_PRESET -> {
                if (sList.titleSettings == preferences.getString(SELECTED_BOX64_PRESET_KEY, "default")) {
                    selectedPresetId = position
                }
                holder.radioButton.setOnClickListener {
                    preferences.edit().apply {
                        putString(SELECTED_BOX64_PRESET_KEY, holder.settingsName.text.toString())
                        apply()
                    }
                    selectedPresetId = holder.adapterPosition
                    notifyItemRangeChanged(0, settingsList.size)
                }
            }
        }

        holder.radioButton.isChecked = position == selectedPresetId

        holder.deletePresetButton.setOnClickListener {
            clickedPresetName = sList.titleSettings

            when (sList.type) {
                PHYSICAL_CONTROLLER -> {
                    DeleteItemFragment(DELETE_CONTROLLER_PRESET, context).show(supportFragmentManager, "")
                }
                VIRTUAL_CONTROLLER -> {
                    DeleteItemFragment(DELETE_VIRTUAL_CONTROLLER_PRESET, context).show(supportFragmentManager, "")
                }
                BOX64_PRESET -> {
                    DeleteItemFragment(DELETE_BOX64_PRESET, context).show(supportFragmentManager, "")
                }
            }
        }

        holder.editPresetButton.setOnClickListener {
            clickedPresetName = sList.titleSettings

            when (sList.type) {
                PHYSICAL_CONTROLLER -> {
                    context.sendBroadcast(
                        Intent(ACTION_EDIT_CONTROLLER_MAPPING)
                    )
                }
                VIRTUAL_CONTROLLER -> {
                    val intent = Intent(context, VirtualControllerOverlayMapper::class.java)
                    context.startActivity(intent)
                }
                BOX64_PRESET -> {
                    context.sendBroadcast(
                        Intent(ACTION_EDIT_BOX64_PRESET)
                    )
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return settingsList.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
        val radioButton: RadioButton = itemView.findViewById(R.id.radioButton)
        val settingsName: TextView = itemView.findViewById(R.id.presetTitle)
        val editPresetButton: ImageButton = itemView.findViewById(R.id.editPresetButton)
        val deletePresetButton: ImageButton = itemView.findViewById(R.id.deletePresetButton)

        init {
            itemView.setOnClickListener(this)
        }

        override fun onClick(v: View?) {
        }
    }

    class Item(var titleSettings: String, var type: Int, var userPreset: Boolean)

    companion object {
        const val PHYSICAL_CONTROLLER = 0
        const val VIRTUAL_CONTROLLER = 1
        var clickedPresetName = ""
        var selectedPresetId = -1
    }
}