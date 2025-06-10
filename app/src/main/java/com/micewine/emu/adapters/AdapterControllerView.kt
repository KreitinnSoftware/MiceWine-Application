package com.micewine.emu.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.micewine.emu.R
import com.micewine.emu.fragments.ControllerViewFragment.Companion.getControllerBitmap

class AdapterControllerView(private val controllerViewList: List<ControllerViewList>, private val context: Context) :
    RecyclerView.Adapter<AdapterControllerView.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.adapter_controller_view, parent, false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val sList = controllerViewList[position]

        holder.settingsName.text = sList.controllerName
        holder.controllerImage.setImageBitmap(getControllerBitmap(780, 400, sList.controllerID, context))
    }

    override fun getItemCount(): Int {
        return controllerViewList.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
        View.OnClickListener {
        val settingsName: TextView = itemView.findViewById(R.id.title_preferences_model)
        val controllerImage: ImageView = itemView.findViewById(R.id.controllerImage)

        init {
            itemView.setOnClickListener(this)
        }

        override fun onClick(p0: View?) {}
    }

    class ControllerViewList(var controllerName: String, var controllerID: Int)
}