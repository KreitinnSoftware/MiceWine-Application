package com.micewine.emu.adapters

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.micewine.emu.R
import com.micewine.emu.activities.EmulationActivity
import com.micewine.emu.activities.MainActivity.Companion.ACTION_RUN_WINE
import com.micewine.emu.activities.MainActivity.Companion.selectedGameArray
import java.io.File

class AdapterGame(private val gameList: List<GameList>, private val activity: Activity) : RecyclerView.Adapter<AdapterGame.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.adapter_game_item, parent, false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val sList = gameList[position]
        holder.titleGame.text = sList.name

        if (sList.imageGame == "" || !File(sList.imageGame).exists()) {
            holder.gameImage.setImageBitmap(resizeBitmap(
                BitmapFactory.decodeResource(activity.resources, R.drawable.default_icon), holder.gameImage.layoutParams.width, holder.gameImage.layoutParams.height)
            )
        } else {
            holder.gameImage.setImageBitmap(resizeBitmap(
                BitmapFactory.decodeFile(sList.imageGame), holder.gameImage.layoutParams.width, holder.gameImage.layoutParams.height)
            )
        }
    }

    override fun getItemCount(): Int {
        return gameList.size
    }

    private fun resizeBitmap(originalBitmap: Bitmap, width: Int, height: Int): Bitmap {
        return Bitmap.createScaledBitmap(originalBitmap, width, height, false)
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

            val intent = Intent(activity, EmulationActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
            }

            val runWineIntent = Intent(ACTION_RUN_WINE).apply {
                if (gameModel.exeFile.path == activity.getString(R.string.desktop_mode_init)) {
                    putExtra("exePath", "")
                } else {
                    putExtra("exePath", gameModel.exeFile.toString())
                }
            }

            activity.sendBroadcast(runWineIntent)
            activity.startActivityIfNeeded(intent, 0)
        }

        override fun onLongClick(v: View): Boolean {
            if (adapterPosition == 0) {
                return true
            }

            val gameModel = gameList[adapterPosition]

            selectedGameArray = arrayOf(gameModel.name, gameModel.exeFile.path, gameModel.imageGame)

            return false
        }
    }

    class GameList(var exeFile: File, var name: String, var imageGame: String)
}
