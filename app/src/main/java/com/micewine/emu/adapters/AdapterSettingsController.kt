package com.micewine.emu.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.Spinner
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.RecyclerView
import com.micewine.emu.R
import com.micewine.emu.activities.ControllerMapper.Companion.availableButtonMappings

class AdapterSettingsController(private val settingsControllerList: List<SettingsController>, private val context: Context) :
    RecyclerView.Adapter<AdapterSettingsController.ViewHolder>() {

    private val preferences = PreferenceManager.getDefaultSharedPreferences(context)!!
    private val spinnerEntries: Array<String> = availableButtonMappings

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.settings_controller_item, parent, false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val sList = settingsControllerList[position]

        holder.image.setImageResource(sList.image)

        holder.spinner.adapter = ArrayAdapter(context, android.R.layout.simple_spinner_item, spinnerEntries)

        holder.spinner.setSelection(spinnerEntries.indexOf(preferences.getString(sList.key, "Null")))

        holder.spinner.onItemSelectedListener =
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

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
        View.OnClickListener {
        val spinner: Spinner
        val image: ImageView

        init {
            spinner = itemView.findViewById(R.id.optionSpinner)
            image = itemView.findViewById(R.id.buttonImageView)
            itemView.setOnClickListener(this)
        }

        override fun onClick(v: View) {
        }
    }

    class SettingsController(var image: Int, var key: String)
}