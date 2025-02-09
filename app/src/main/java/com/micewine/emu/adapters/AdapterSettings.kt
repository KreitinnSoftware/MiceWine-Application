package com.micewine.emu.adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.micewine.emu.R
import com.micewine.emu.activities.Box64ManagerActivity
import com.micewine.emu.activities.ControllerMapperActivity
import com.micewine.emu.activities.GeneralSettingsActivity
import com.micewine.emu.activities.GeneralSettingsActivity.Companion.ACTION_PREFERENCE_SELECT
import com.micewine.emu.activities.DriverManagerActivity
import com.micewine.emu.activities.VirtualControllerOverlayMapper

class AdapterSettings(private val settingsList: List<SettingsList>, private val context: Context) :
    RecyclerView.Adapter<AdapterSettings.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.adapter_settings_item, parent, false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val sList = settingsList[position]
        holder.settingsName.text = sList.titleSettings
        holder.settingsDescription.text = sList.descriptionSettings
        holder.imageSettings.setImageResource(sList.imageSettings)
    }

    override fun getItemCount(): Int {
        return settingsList.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
        View.OnClickListener {
        val settingsName: TextView = itemView.findViewById(R.id.title_preferences_model)
        val settingsDescription: TextView = itemView.findViewById(R.id.description_preferences_model)
        val imageSettings: ImageView = itemView.findViewById(R.id.set_img)

        init {
            itemView.setOnClickListener(this)
        }

        override fun onClick(v: View) {
            val settingsModel = settingsList[getAdapterPosition()]

            when (settingsModel.titleSettings) {
                context.getString(R.string.settings_title) -> {
                    val intent = Intent(context, GeneralSettingsActivity::class.java)
                    context.startActivity(intent)
                }

                context.getString(R.string.controller_mapper_title) -> {
                    val intent = Intent(context, ControllerMapperActivity::class.java)
                    context.startActivity(intent)
                }

                context.getString(R.string.virtual_controller_mapper_title) -> {
                    val intent = Intent(context, VirtualControllerOverlayMapper::class.java)
                    context.startActivity(intent)
                }

                context.getString(R.string.box64_manager_title) -> {
                    val intent = Intent(context, Box64ManagerActivity::class.java)
                    context.startActivity(intent)
                }

                context.getString(R.string.driver_manager_title) -> {
                    val intent = Intent(context, DriverManagerActivity::class.java)
                    context.startActivity(intent)
                }

                else -> {
                    val intent = Intent(ACTION_PREFERENCE_SELECT)
                    intent.putExtra("preference", settingsModel.titleSettings)
                    context.sendBroadcast(intent)
                }
            }
        }
    }

    class SettingsList(var titleSettings: String, var descriptionSettings: String, var imageSettings: Int)
}