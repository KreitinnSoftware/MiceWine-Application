package com.micewine.emu.adapters

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.TextView
import androidx.appcompat.widget.SwitchCompat
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.RecyclerView
import com.micewine.emu.R

class AdapterSettingsBoolean(private val settingsList: List<SettingsListBoolean>, private val context: Context) :
    RecyclerView.Adapter<AdapterSettingsBoolean.ViewHolder>() {

    val preferences = PreferenceManager.getDefaultSharedPreferences(context)!!

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView =
            LayoutInflater.from(parent.context).inflate(R.layout.settings_boolean_item, parent, false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val sList = settingsList[position]
        holder.settingsName.setText(sList.titleSettings)
        holder.settingsDescription.setText(sList.descriptionSettings)
        holder.settingsSwitch.isChecked = preferences.getBoolean(context.resources.getString(sList.titleSettings), false)

        holder.settingsSwitch.setOnClickListener {
            val editor = preferences.edit()

            editor.putBoolean(context.resources.getString(sList.titleSettings), !preferences.getBoolean(context.resources.getString(sList.titleSettings), false))

            editor.apply()
        }
    }

    override fun getItemCount(): Int {
        return settingsList.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
        View.OnClickListener {
        val settingsName: TextView
        val settingsDescription: TextView
        val settingsSwitch: SwitchCompat

        init {
            settingsName = itemView.findViewById(R.id.title_preferences_model)
            settingsDescription = itemView.findViewById(R.id.description_preferences_model)
            settingsSwitch = itemView.findViewById(R.id.optionSwitch)
            itemView.setOnClickListener(this)
        }

        override fun onClick(v: View) {
            val settingsModel = settingsList[getAdapterPosition()]
        }
    }

    class SettingsListBoolean(var titleSettings: Int, var descriptionSettings: Int)
}