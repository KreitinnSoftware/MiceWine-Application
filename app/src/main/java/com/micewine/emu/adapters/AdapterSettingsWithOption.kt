package com.micewine.emu.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.micewine.emu.R


class AdapterSettingsWithOption(private val settingsList: List<SettingsListWithOption>, private val context: Context) :
    RecyclerView.Adapter<AdapterSettingsWithOption.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView =
            LayoutInflater.from(parent.context).inflate(R.layout.settings_with_option_item, parent, false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val sList = settingsList[position]
        holder.settingsName.setText(sList.titleSettings)
        holder.settingsDescription.setText(sList.descriptionSettings)
        holder.spinnerOptions.adapter = ArrayAdapter(context, android.R.layout.simple_spinner_item, sList.spinnerOptions)
    }

    override fun getItemCount(): Int {
        return settingsList.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
        View.OnClickListener {
        val settingsName: TextView
        val settingsDescription: TextView
        var spinnerOptions: Spinner

        init {
            settingsName = itemView.findViewById(R.id.title_preferences_model)
            settingsDescription = itemView.findViewById(R.id.description_preferences_model)
            spinnerOptions = itemView.findViewById(R.id.optionSpinner)
            itemView.setOnClickListener(this)
        }

        override fun onClick(v: View) {
            val settingsModel = settingsList[getAdapterPosition()]


        }
    }

    class SettingsListWithOption(var titleSettings: Int, var descriptionSettings: Int, var spinnerOptions: Array<String>)
}