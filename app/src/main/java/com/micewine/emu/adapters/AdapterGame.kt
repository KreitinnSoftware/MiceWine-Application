package com.micewine.emu.adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.micewine.emu.activities.EmulationActivity
import com.micewine.emu.R
import com.micewine.emu.activities.MainActivity.Companion.enableRamCounter
import java.io.File

class AdapterGame(private val gameList: List<GameList>, private val context: Context) :
    RecyclerView.Adapter<AdapterGame.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView =
            LayoutInflater.from(parent.context).inflate(R.layout.game_item, parent, false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val sList = gameList[position]
        holder.titleGame.text = sList.exeFile.nameWithoutExtension
        holder.gameImage.setImageResource(sList.imageGame)
    }

    override fun getItemCount(): Int {
        return gameList.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener, View.OnLongClickListener {
        val titleGame: TextView = itemView.findViewById(R.id.title_game_model)
        val gameImage: ImageView = itemView.findViewById(R.id.img_game)

        init {
            itemView.setOnClickListener(this)
            itemView.setOnLongClickListener(this)
        }

        override fun onClick(v: View) {
            val gameModel = gameList[getAdapterPosition()]
            val intent = Intent(context, EmulationActivity::class.java)

            if (gameModel.exeFile.path == context.getString(R.string.desktop_mode_init)) {
                intent.putExtra("exePath", "**wine-desktop**")
            } else {
                intent.putExtra("exePath", gameModel.exeFile.toString())
            }

            enableRamCounter = true
            context.startActivity(intent)
        }

        override fun onLongClick(v: View): Boolean {
            return true
        }
    }

    class GameList(var exeFile: File, var imageGame: Int)
}
