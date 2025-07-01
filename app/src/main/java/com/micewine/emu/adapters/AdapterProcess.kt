package com.micewine.emu.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.PopupMenu
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import com.micewine.emu.R
import com.micewine.emu.core.ShellLoader.runCommand
import com.micewine.emu.core.WineWrapper
import com.micewine.emu.core.WineWrapper.getCpuHexMask
import com.micewine.emu.core.WineWrapper.getProcessCPUAffinity
import com.micewine.emu.core.WineWrapper.maskToCpuAffinity
import com.micewine.emu.fragments.DebugSettingsFragment.Companion.availableCPUs
import java.io.File


class AdapterProcess(private val processList: List<WineWrapper.ExeProcess>, private val context: Context) : RecyclerView.Adapter<AdapterProcess.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.adapter_process_item, parent, false)
        return ViewHolder(itemView)
    }

    private fun drawableToBitmap(drawable: Drawable?): Bitmap {
        if (drawable is BitmapDrawable) {
            return drawable.bitmap
        }

        val width = drawable?.intrinsicWidth ?: 1
        val height = drawable?.intrinsicHeight ?: 1

        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        drawable?.setBounds(0, 0, canvas.width, canvas.height)
        drawable?.draw(canvas)
        return bitmap
    }


    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val sList = processList[position]

        holder.processName.text = sList.name
        holder.processRamUsage.text = "%.2f MB".format(sList.ramUsageKB / 1024F)
        holder.processCPUUsage.text = "%.2f %%".format(sList.cpuUsage)

        val iconFile = File(sList.iconPath)
        val pIcon = if (iconFile.exists() && iconFile.length() > 0) {
            BitmapFactory.decodeFile(sList.iconPath)
        } else {
            drawableToBitmap(ResourcesCompat.getDrawable(context.resources, R.drawable.unknown_exe, null))
        }

        holder.icon.setImageBitmap(pIcon)

        holder.moreButton.setOnClickListener {
            val popup = PopupMenu(context, holder.moreButton)
            val inflater: MenuInflater = popup.menuInflater
            inflater.inflate(R.menu.process_more_menu, popup.menu)

            popup.setOnMenuItemClickListener { item: MenuItem ->
                when (item.itemId) {
                    R.id.killProcess -> {
                        runCommand("kill -SIGINT ${sList.unixPid}")
                        true
                    }
                    R.id.setAffinity -> {
                        val items = availableCPUs
                        val checkedItems = maskToCpuAffinity(getProcessCPUAffinity(sList.unixPid).toLong(16))

                        AlertDialog.Builder(context, R.style.Theme_MiceWine).apply {
                            setTitle(sList.name)
                            setIcon(
                                BitmapDrawable(context.resources, pIcon)
                            )
                            setMultiChoiceItems(items, checkedItems) { _, which, isChecked ->
                                checkedItems[which] = isChecked
                            }
                            setPositiveButton(context.getText(R.string.confirm_text)) { dialog, _ ->
                                val strAffinity: StringBuilder = StringBuilder()

                                for (i in checkedItems.indices) {
                                    if (checkedItems[i]) {
                                        strAffinity.append(",")
                                        strAffinity.append(items[i])
                                    }
                                }

                                if (strAffinity.isNotEmpty()) {
                                    strAffinity.deleteCharAt(0)
                                }

                                runCommand("taskset -p ${getCpuHexMask(strAffinity.toString())} ${sList.unixPid}")

                                dialog.dismiss()
                            }
                            setNegativeButton(context.getText(R.string.cancel_text), null)
                            show()
                        }
                        true
                    }
                    else -> false
                }
            }

            popup.show()
        }
    }

    override fun getItemCount(): Int {
        return processList.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
        val processName: TextView = itemView.findViewById(R.id.processName)
        val processRamUsage: TextView = itemView.findViewById(R.id.processRamUsage)
        val processCPUUsage: TextView = itemView.findViewById(R.id.processCPUUsage)
        val moreButton: ImageButton = itemView.findViewById(R.id.moreButton)
        val icon: ImageView = itemView.findViewById(R.id.processIcon)

        init {
            itemView.setOnClickListener(this)
        }

        override fun onClick(v: View) {}
    }
}