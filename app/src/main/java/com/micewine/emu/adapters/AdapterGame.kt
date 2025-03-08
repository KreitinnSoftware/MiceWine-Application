package com.micewine.emu.adapters

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.micewine.emu.R
import com.micewine.emu.activities.EmulationActivity
import com.micewine.emu.activities.MainActivity.Companion.ACTION_RUN_WINE
import java.io.File

class AdapterGame(
    private val gameList: MutableList<GameItem>,
    private val activity: Activity
) : RecyclerView.Adapter<AdapterGame.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.adapter_game_item, parent, false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val game = gameList[position]
        holder.titleGame.text = game.name
        holder.setGameImage(game.iconPath)
    }

    override fun getItemCount(): Int = gameList.size

    @SuppressLint("NotifyDataSetChanged")
    fun updateList(newList: List<GameItem>) {
        gameList.clear()
        gameList.addAll(newList)
        notifyDataSetChanged()
    }

    inner class ViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView), View.OnClickListener, View.OnLongClickListener {

        val titleGame: TextView = itemView.findViewById(R.id.title_game_model)
        val gameImage: ImageView = itemView.findViewById(R.id.img_game)

        init {
            itemView.setOnClickListener(this)
            itemView.setOnLongClickListener(this)
        }

        fun setGameImage(iconPath: String) {
            val imageFile = File(iconPath)

            val bitmap = when {
                imageFile.exists() && imageFile.length() > 0 -> {
                    BitmapFactory.decodeFile(iconPath)?.let {
                        resizeBitmap(it, gameImage.layoutParams.width, gameImage.layoutParams.height)
                    }
                }
                iconPath.isEmpty() -> {
                    resizeBitmap(
                        BitmapFactory.decodeResource(activity.resources, R.drawable._363211_game_gaming_play_steam_valve_85503),
                        gameImage.layoutParams.width,
                        gameImage.layoutParams.height
                    )
                }
                else -> Bitmap.createBitmap(2, 2, Bitmap.Config.ARGB_8888)
            }

            gameImage.setImageBitmap(bitmap)
        }

        override fun onClick(v: View) {
            if (adapterPosition == RecyclerView.NO_POSITION) return

            val game = gameList[adapterPosition]
            selectedGameName = game.name

            val exeFile = File(game.exePath)

            if (!exeFile.exists() && game.exePath != activity.getString(R.string.desktop_mode_init)) {
                activity.runOnUiThread {
                    Toast.makeText(activity, "Arquivo executável não encontrado!", Toast.LENGTH_SHORT).show()
                }
                return
            }

            val intent = Intent(activity, EmulationActivity::class.java)
            val runWineIntent = Intent(ACTION_RUN_WINE).apply {
                putExtra("exePath", game.exePath)
                putExtra("exeArguments", game.exeArguments)
            }

            activity.sendBroadcast(runWineIntent)
            activity.startActivity(intent)
        }

        override fun onLongClick(v: View): Boolean {
            if (adapterPosition == RecyclerView.NO_POSITION || adapterPosition == 0) {
                return true
            }

            val game = gameList[adapterPosition]
            selectedGameName = game.name
            return false
        }
    }

    private fun resizeBitmap(originalBitmap: Bitmap, width: Int, height: Int): Bitmap {
        return Bitmap.createScaledBitmap(originalBitmap, width, height, false)
    }

    data class GameItem(
        var name: String,
        var exePath: String,
        var exeArguments: String,
        var iconPath: String
    )

    companion object {
        var selectedGameName = ""
    }
}
