package com.micewine.emu.adapters

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.micewine.emu.R
import com.micewine.emu.activities.EmulationActivity
import com.micewine.emu.activities.GeneralSettingsActivity.Companion.SELECTED_BOX64
import com.micewine.emu.activities.GeneralSettingsActivity.Companion.SELECTED_VULKAN_DRIVER
import com.micewine.emu.activities.MainActivity.Companion.ACTION_RUN_WINE
import com.micewine.emu.activities.MainActivity.Companion.preferences
import com.micewine.emu.fragments.EditGamePreferencesFragment
import com.micewine.emu.fragments.EditGamePreferencesFragment.Companion.EDIT_GAME_PREFERENCES
import com.micewine.emu.fragments.ShortcutsFragment.Companion.ADRENO_TOOLS_DRIVER
import com.micewine.emu.fragments.ShortcutsFragment.Companion.MESA_DRIVER
import com.micewine.emu.fragments.ShortcutsFragment.Companion.getBox64Preset
import com.micewine.emu.fragments.ShortcutsFragment.Companion.getBox64Version
import com.micewine.emu.fragments.ShortcutsFragment.Companion.getCpuAffinity
import com.micewine.emu.fragments.ShortcutsFragment.Companion.getD3DXRenderer
import com.micewine.emu.fragments.ShortcutsFragment.Companion.getDXVKVersion
import com.micewine.emu.fragments.ShortcutsFragment.Companion.getDisplaySettings
import com.micewine.emu.fragments.ShortcutsFragment.Companion.getSelectedVirtualControllerPreset
import com.micewine.emu.fragments.ShortcutsFragment.Companion.getVKD3DVersion
import com.micewine.emu.fragments.ShortcutsFragment.Companion.getVulkanDriver
import com.micewine.emu.fragments.ShortcutsFragment.Companion.getWineD3DVersion
import com.micewine.emu.fragments.ShortcutsFragment.Companion.getWineESync
import com.micewine.emu.fragments.ShortcutsFragment.Companion.getWineServices
import com.micewine.emu.fragments.ShortcutsFragment.Companion.getWineVirtualDesktop
import java.io.File
import kotlin.math.roundToInt


class AdapterGame(
    private val gameList: MutableList<GameItem>,
    private val size: Float,
    private val activity: FragmentActivity,
) : RecyclerView.Adapter<AdapterGame.ViewHolder>() {
    private var filteredList: MutableList<GameItem> = gameList

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.adapter_game_item, parent, false)
        itemView.layoutParams.width = (itemView.layoutParams.width * size).roundToInt()
        itemView.layoutParams.height = (itemView.layoutParams.height * size).roundToInt()
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val sList = filteredList[position]
        holder.titleGame.text = sList.name

        val imageFile = File(sList.iconPath)

        if (imageFile.exists() && imageFile.length() > 0) {
            val imageBitmap = BitmapFactory.decodeFile(sList.iconPath)
            if (imageBitmap != null) {
                val newBitmap: Bitmap = if (File(sList.exePath).exists()) {
                    resizeBitmap(
                        imageBitmap, holder.itemView.layoutParams.width - 10, holder.itemView.layoutParams.width - 10
                    )
                } else {
                    toGrayscale(
                        resizeBitmap(
                            imageBitmap, holder.itemView.layoutParams.width - 10, holder.itemView.layoutParams.width - 10
                        )
                    )
                }
                holder.gameImage.setImageBitmap(newBitmap)
            }
        } else if (sList.iconPath == "") {
            holder.gameImage.setImageBitmap(resizeBitmap(
                BitmapFactory.decodeResource(activity.resources, R.drawable.default_icon), holder.itemView.layoutParams.width - 10, holder.itemView.layoutParams.width - 10)
            )
        } else {
            holder.gameImage.setImageBitmap(
                Bitmap.createBitmap(2, 2, Bitmap.Config.ARGB_8888),
            )
        }
    }

    override fun getItemCount(): Int {
        return filteredList.size
    }

    @SuppressLint("NotifyDataSetChanged")
    fun filterList(name: String) {
        filteredList = gameList.filter {
            it.name.contains(name, true)
        }.toMutableList()

        notifyDataSetChanged()
    }

    private fun resizeBitmap(originalBitmap: Bitmap, width: Int, height: Int): Bitmap {
        return Bitmap.createScaledBitmap(originalBitmap, width, height, false)
    }

    private fun toGrayscale(bmpOriginal: Bitmap): Bitmap {
        val bmpGrayscale = Bitmap.createBitmap(bmpOriginal.width, bmpOriginal.height, Bitmap.Config.ARGB_8888)

        Canvas(bmpGrayscale).apply {
            drawBitmap(bmpOriginal, 0f, 0f,
                Paint().apply {
                    colorFilter = ColorMatrixColorFilter(
                        ColorMatrix().apply {
                            setSaturation(0f)
                        }
                    )
                }
            )
            drawRect(0f, 0f, bmpOriginal.width.toFloat(), bmpOriginal.height.toFloat(),
                Paint().apply {
                    color = Color.argb(25, 128, 128, 128)
                }
            )
        }

        return bmpGrayscale
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener, View.OnLongClickListener {
        val titleGame: TextView = itemView.findViewById(R.id.title_game_model)
        val gameImage: ImageView = itemView.findViewById(R.id.img_game)

        init {
            itemView.setOnClickListener(this)
            itemView.setOnLongClickListener(this)
        }

        override fun onClick(v: View) {
            val gameModel = gameList[adapterPosition]

            selectedGameName = gameModel.name

            val exeFile = File(gameModel.exePath)
            var exePath = gameModel.exePath
            var exeArguments = gameModel.exeArguments

            if (!exeFile.exists()) {
                if (exeFile.path == activity.getString(R.string.desktop_mode_init)) {
                    exePath = ""
                    exeArguments = ""
                } else {
                    activity.runOnUiThread {
                        Toast.makeText(activity, activity.getString(R.string.executable_file_not_found), Toast.LENGTH_SHORT).show()
                    }

                    EditGamePreferencesFragment(EDIT_GAME_PREFERENCES).show(activity.supportFragmentManager, "")
                    return
                }
            }

            val intent = Intent(activity, EmulationActivity::class.java)

            var driverName = getVulkanDriver(selectedGameName)
            if (driverName == "Global") {
                driverName = preferences?.getString(SELECTED_VULKAN_DRIVER, "").toString()
            }
            var box64Version = getBox64Version(selectedGameName)
            if (box64Version == "Global") {
                box64Version = preferences?.getString(SELECTED_BOX64, "").toString()
            }

            val driverType = if (driverName.startsWith("AdrenoToolsDriver-")) ADRENO_TOOLS_DRIVER else MESA_DRIVER

            val runWineIntent = Intent(ACTION_RUN_WINE).apply {
                putExtra("exePath", exePath)
                putExtra("exeArguments", exeArguments)
                putExtra("driverName", driverName)
                putExtra("driverType", driverType)
                putExtra("box64Version", box64Version)
                putExtra("box64Preset", getBox64Preset(selectedGameName))
                putExtra("displayResolution", getDisplaySettings(selectedGameName)[1])
                putExtra("virtualControllerPreset", getSelectedVirtualControllerPreset(selectedGameName))
                putExtra("d3dxRenderer", getD3DXRenderer(selectedGameName))
                putExtra("wineD3D", getWineD3DVersion(selectedGameName))
                putExtra("dxvk", getDXVKVersion(selectedGameName))
                putExtra("vkd3d", getVKD3DVersion(selectedGameName))
                putExtra("esync", getWineESync(selectedGameName))
                putExtra("services", getWineServices(selectedGameName))
                putExtra("virtualDesktop", getWineVirtualDesktop(selectedGameName))
                putExtra("cpuAffinity", getCpuAffinity(selectedGameName))
            }

            activity.sendBroadcast(runWineIntent)
            activity.startActivity(intent)
        }

        override fun onLongClick(v: View): Boolean {
            val gameModel = gameList[adapterPosition]

            selectedGameName = gameModel.name

            return false
        }
    }

    class GameItem(
        var name: String,
        var exePath: String,
        var exeArguments: String,
        var iconPath: String
    )

    companion object {
        var selectedGameName = ""
    }
}
