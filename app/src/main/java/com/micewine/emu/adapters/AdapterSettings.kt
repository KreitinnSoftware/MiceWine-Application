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
import com.micewine.emu.activities.ControllerMapper
import com.micewine.emu.activities.GeneralSettings
import com.micewine.emu.activities.GeneralSettings.Companion.ACTION_PREFERENCE_SELECT
import com.micewine.emu.activities.LogAppOutput
import com.micewine.emu.activities.VirtualControllerOverlayMapper

class AdapterSettings(private val settingsList: List<SettingsList>, private val context: Context) :
    RecyclerView.Adapter<AdapterSettings.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.adapter_settings_item, parent, false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val sList = settingsList[position]
        holder.settingsName.setText(sList.titleSettings)
        holder.settingsDescription.setText(sList.descriptionSettings)
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
                R.string.settingsTitle -> {
                    val intent = Intent(context, GeneralSettings::class.java)
                    context.startActivity(intent)
                }

                R.string.logTitle -> {
                    val intent = Intent(context, LogAppOutput::class.java)
                    context.startActivity(intent)
                }

                R.string.controllerMapperTitle -> {
                    val intent = Intent(context, ControllerMapper::class.java)
                    context.startActivity(intent)
                }

                R.string.virtualControllerMapperTitle -> {
                    val intent = Intent(context, VirtualControllerOverlayMapper::class.java)
                    context.startActivity(intent)
                }

                else -> {
                    val intent = Intent(ACTION_PREFERENCE_SELECT)
                    intent.putExtra("preference", context.resources.getString(settingsModel.titleSettings))
                    context.sendBroadcast(intent)
                }
            }
        }
    }

    class SettingsList(var titleSettings: Int, var descriptionSettings: Int, var imageSettings: Int)
}