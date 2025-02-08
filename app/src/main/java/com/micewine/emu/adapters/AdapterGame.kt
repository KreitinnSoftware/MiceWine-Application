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
import com.micewine.emu.activities.MainActivity.Companion.selectedGameArray
import java.io.File

class AdapterGame(private val gameList: MutableList<GameList>, private val activity: Activity) : RecyclerView.Adapter<AdapterGame.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.adapter_game_item, parent, false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val sList = gameList[position]
        holder.titleGame.text = sList.name

        val imageFile = File(sList.imageGame)

        if (imageFile.exists() && imageFile.length() > 0) {
            val imageBitmap = BitmapFactory.decodeFile(sList.imageGame)

            if (imageBitmap != null) {
                holder.gameImage.setImageBitmap(
                    resizeBitmap(
                        imageBitmap, holder.gameImage.layoutParams.width, holder.gameImage.layoutParams.height
                    )
                )
            }
        } else if (sList.imageGame == "") {
            holder.gameImage.setImageBitmap(resizeBitmap(
                BitmapFactory.decodeResource(activity.resources, R.drawable.default_icon), holder.gameImage.layoutParams.width, holder.gameImage.layoutParams.height)
            )
        } else {
            holder.gameImage.setImageBitmap(
                Bitmap.createBitmap(2, 2, Bitmap.Config.ARGB_8888),
            )
        }
    }

    override fun getItemCount(): Int {
        return gameList.size
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateList(newList: List<GameList>) {
        gameList.clear()
        gameList.addAll(newList)
        notifyDataSetChanged()
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

            var exePath = gameModel.exeFile.path
            var exeArguments = gameModel.exeArguments

            if (!gameModel.exeFile.exists()) {
                if (gameModel.exeFile.path == activity.getString(R.string.desktop_mode_init)) {
                    exePath = ""
                    exeArguments = ""
                } else {
                    activity.runOnUiThread {
                        Toast.makeText(activity, "", Toast.LENGTH_SHORT).show()
                    }
                    return
                }
            }

            val intent = Intent(activity, EmulationActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
            }

            val runWineIntent = Intent(ACTION_RUN_WINE).apply {
                putExtra("exePath", exePath)
                putExtra("exeArguments", exeArguments)
            }

            activity.sendBroadcast(runWineIntent)
            activity.startActivityIfNeeded(intent, 0)
        }

        override fun onLongClick(v: View): Boolean {
            if (adapterPosition == 0) {
                return true
            }

            val gameModel = gameList[adapterPosition]

            selectedGameArray = arrayOf(gameModel.name, gameModel.exeFile.path, gameModel.imageGame, gameModel.exeArguments)

            return false
        }
    }

    class GameList(var exeFile: File, var name: String, var imageGame: String, var exeArguments: String)
}
