package com.micewine.emu.adapters

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.RadioButton
import android.widget.TextView
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.RecyclerView
import com.micewine.emu.R
import com.micewine.emu.activities.GeneralSettingsActivity.Companion.SELECTED_BOX64
import com.micewine.emu.activities.MainActivity.Companion.ratPackagesDir
import java.io.File


class AdapterRatPackage(private val settingsList: MutableList<Item>, private val context: Context) :
    RecyclerView.Adapter<AdapterRatPackage.ViewHolder>() {

    val preferences = PreferenceManager.getDefaultSharedPreferences(context)!!
    private var selectedItemId = -1

    private fun textAsBitmap(text: String, size: Float, textColor: Int): Bitmap {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = size
            color = textColor
            textAlign = Paint.Align.LEFT
            typeface = context.resources.getFont(R.font.quicksand)
        }
        val baseline = -paint.ascent()
        val width = (paint.measureText(text) + 0.5F).toInt()
        val height = (baseline + paint.descent() + 0.5F).toInt()
        val image = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

        Canvas(image).apply {
            drawText(text, 0F, baseline, paint)
        }
        return image
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.adapter_rat_package_item, parent, false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val sList = settingsList[position]
        var packagePrefix: String? = null
        var selectedItem: String? = null

        when (sList.type) {
            VK_DRIVER -> {
                holder.radioButton.visibility = View.GONE
                holder.imageView.setImageResource(R.drawable.ic_gpu)
            }
            BOX64 -> {
                selectedItem = preferences.getString(SELECTED_BOX64, "")
                packagePrefix = "Box64-"

                holder.imageView.setImageResource(R.drawable.ic_box64)
            }
            WINE -> {
                holder.radioButton.visibility = View.GONE
                holder.imageView.setImageResource(R.drawable.ic_wine)
            }
            DXVK -> {
                holder.radioButton.visibility = View.GONE
                holder.imageView.setImageBitmap(
                    textAsBitmap("DXVK", 80F, Color.WHITE)
                )
            }
            WINED3D -> {
                holder.radioButton.visibility = View.GONE
                holder.imageView.setImageBitmap(
                    textAsBitmap("WineD3D", 80F, Color.WHITE)
                )
            }
            VKD3D -> {
                holder.radioButton.visibility = View.GONE
                holder.imageView.setImageBitmap(
                    textAsBitmap("VKD3D", 80F, Color.WHITE)
                )
            }
        }

        if (sList.externalPackage) {
            holder.deleteRatPackageButton.visibility = View.VISIBLE
        } else {
            holder.deleteRatPackageButton.visibility = View.GONE
        }

        if (sList.itemFolderId == selectedItem) {
            selectedItemId = holder.adapterPosition
        }

        holder.apply {
            settingsName.text = sList.titleSettings
            settingsDescription.text = sList.descriptionSettings

            radioButton.isChecked = position == selectedItemId
            radioButton.setOnClickListener {
                preferences.edit().apply {
                    when (sList.type) {
                        BOX64 -> {
                            putString(SELECTED_BOX64, sList.itemFolderId)
                        }
                    }

                    apply()

                    selectedItemId = holder.adapterPosition
                    notifyItemRangeChanged(0, settingsList.size)
                }
            }

            deleteRatPackageButton.setOnClickListener {
                if (selectedItemId == holder.adapterPosition) {
                    selectedItemId = 0

                    val firstPackage = File("$ratPackagesDir").listFiles()?.first { it.name.startsWith(packagePrefix!!) }?.name

                    preferences.edit().apply {
                        when (sList.type) {
                            BOX64 -> {
                                putString(SELECTED_BOX64, firstPackage)
                            }
                        }

                        apply()
                    }

                    notifyItemChanged(0)
                }

                File("$ratPackagesDir/${sList.itemFolderId}").deleteRecursively()

                settingsList.removeAt(holder.adapterPosition)
                notifyItemRemoved(holder.adapterPosition)
            }
        }
    }

    override fun getItemCount(): Int {
        return settingsList.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
        val radioButton: RadioButton = itemView.findViewById(R.id.radio_button)
        val settingsName: TextView = itemView.findViewById(R.id.preset_title)
        val settingsDescription: TextView = itemView.findViewById(R.id.rat_package_description)
        val imageView: ImageView = itemView.findViewById(R.id.image_view)
        val deleteRatPackageButton: ImageButton = itemView.findViewById(R.id.rat_package_delete)

        init {
            itemView.setOnClickListener(this)
        }

        override fun onClick(v: View?) {
        }
    }

    class Item(var titleSettings: String, var descriptionSettings: String, var itemFolderId: String, var type: Int, var externalPackage: Boolean)

    companion object {
        const val VK_DRIVER = 1
        const val BOX64 = 2
        const val WINE = 3
        const val DXVK = 4
        const val WINED3D = 5
        const val VKD3D = 6
    }
}