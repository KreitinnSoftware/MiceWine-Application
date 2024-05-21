package com.micewine.emu.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.widget.SwitchCompat
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.RecyclerView
import com.micewine.emu.R
import com.micewine.emu.activities.GeneralSettings.Companion.SPINNER
import com.micewine.emu.activities.GeneralSettings.Companion.SWITCH

class AdapterSettingsPreferences(private val settingsList: List<SettingsListSpinner>, private val context: Context) :
    RecyclerView.Adapter<AdapterSettingsPreferences.ViewHolder>() {

    val preferences = PreferenceManager.getDefaultSharedPreferences(context)!!

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.settings_preferences_item, parent, false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val sList = settingsList[position]
        holder.settingsName.setText(sList.titleSettings)
        holder.settingsDescription.setText(sList.descriptionSettings)

        if (context.getString(sList.descriptionSettings)  == " ") {
            holder.settingsDescription.visibility = View.GONE
            holder.settingsName.gravity = com.google.android.material.R.id.center
        }

        if (sList.type == SWITCH) {
            holder.spinnerOptions.visibility = View.GONE

            holder.settingsSwitch.isChecked = preferences.getBoolean(sList.key, sList.defaultValue.toBoolean())

            holder.settingsSwitch.setOnClickListener {
                val editor = preferences.edit()

                editor.putBoolean(context.resources.getString(sList.titleSettings), !preferences.getBoolean(sList.key, false))

                editor.apply()
            }
        } else if (sList.type == SPINNER) {
            holder.settingsSwitch.visibility = View.GONE

            holder.spinnerOptions.adapter = ArrayAdapter(context, android.R.layout.simple_spinner_item, sList.spinnerOptions)

            holder.spinnerOptions.setSelection(sList.spinnerOptions.indexOf(preferences.getString(sList.key, sList.defaultValue)))

            holder.spinnerOptions.onItemSelectedListener =
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
    }

    override fun getItemCount(): Int {
        return settingsList.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
        View.OnClickListener {
        val settingsName: TextView
        val settingsDescription: TextView
        val spinnerOptions: Spinner
        val settingsSwitch: SwitchCompat

        init {
            settingsName = itemView.findViewById(R.id.title_preferences_model)
            settingsDescription = itemView.findViewById(R.id.description_preferences_model)
            spinnerOptions = itemView.findViewById(R.id.optionSpinner)
            settingsSwitch = itemView.findViewById(R.id.optionSwitch)
            itemView.setOnClickListener(this)
        }

        override fun onClick(v: View) {
            // val settingsModel = settingsList[getAdapterPosition()]

            // TODO Small Dialog that shows description of preference
        }
    }

    class SettingsListSpinner(var titleSettings: Int, var descriptionSettings: Int, var spinnerOptions: Array<String>, var type: Int, var defaultValue: String, var key: String)
}