package com.micewine.emu.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.TextView
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.RecyclerView
import com.micewine.emu.R
import com.micewine.emu.activities.GeneralSettingsActivity.Companion.SELECTED_BOX64
import com.micewine.emu.activities.GeneralSettingsActivity.Companion.SELECTED_DRIVER
import com.micewine.emu.activities.MainActivity.Companion.selectedDriver

class AdapterRatPackage(private val settingsList: List<Item>, context: Context) :
    RecyclerView.Adapter<AdapterRatPackage.ViewHolder>() {

    val preferences = PreferenceManager.getDefaultSharedPreferences(context)!!
    var selectedItem = -1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.adapter_selectable_item, parent, false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val sList = settingsList[position]

        if (sList.type == DRIVER) {
            selectedDriver = preferences.getString(SELECTED_DRIVER, "")
        } else if (sList.type == BOX64) {
            selectedDriver = preferences.getString(SELECTED_BOX64, "")
        }

        if (sList.itemFolderId == selectedDriver) {
            selectedItem = position
        }

        holder.apply {
            radioButton.isChecked = position == selectedItem
            radioButton.setOnClickListener {
                preferences.edit().apply {
                    if (sList.type == DRIVER) {
                        putString(SELECTED_DRIVER, sList.itemFolderId)
                    } else if (sList.type == BOX64) {
                        putString(SELECTED_BOX64, sList.itemFolderId)
                    }

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

    class Item(var titleSettings: String, var descriptionSettings: String, var itemFolderId: String, var type: Int)

    companion object {
        const val DRIVER = 1
        const val BOX64 = 2
    }
}