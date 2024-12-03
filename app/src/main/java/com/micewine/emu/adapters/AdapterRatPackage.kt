package com.micewine.emu.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.RecyclerView
import com.micewine.emu.R
import com.micewine.emu.activities.GeneralSettings.Companion.SELECTED_DRIVER_KEY
import com.micewine.emu.activities.MainActivity.Companion.appRootDir
import com.micewine.emu.activities.MainActivity.Companion.selectedDriver
import java.io.File

class AdapterRatPackage(private val settingsList: List<DriverItem>, context: Context) :
    RecyclerView.Adapter<AdapterRatPackage.ViewHolder>() {

    val preferences = PreferenceManager.getDefaultSharedPreferences(context)!!
    var selectedItem = -1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.adapter_driver_item, parent, false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val sList = settingsList[position]

        selectedDriver = preferences.getString(SELECTED_DRIVER_KEY, "")

        if (sList.driverFolderId == selectedDriver) {
            selectedItem = position
        }

        holder.apply {
            radioButton.isChecked = position == selectedItem
            radioButton.setOnClickListener {
                preferences.edit().apply {
                    putString(SELECTED_DRIVER_KEY, sList.driverFolderId)
                    apply()

                    selectedItem = position
                    notifyItemRangeChanged(0, settingsList.size)
                }
            }

            settingsName.text = sList.titleSettings
            settingsDescription.text = sList.descriptionSettings
        }
    }

    override fun getItemCount(): Int {
        return settingsList.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
        val radioButton: RadioButton = itemView.findViewById(R.id.radio_button)
        val settingsName: TextView = itemView.findViewById(R.id.rat_package_title)
        val settingsDescription: TextView = itemView.findViewById(R.id.rat_package_description)

        init {
            itemView.setOnClickListener(this)
        }

        override fun onClick(v: View) {
            val settingsModel = settingsList[getAdapterPosition()]
        }
    }

    class DriverItem(var titleSettings: String, var descriptionSettings: String, var driverFolderId: String)
}