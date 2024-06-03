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

class AdapterGame(private val gameList: List<GameList>, private val context: Context) :
    RecyclerView.Adapter<AdapterGame.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView =
            LayoutInflater.from(parent.context).inflate(R.layout.game_item, parent, false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val sList = gameList[position]
        holder.titleGame.setText(sList.titleGame)
        holder.gameImage.setImageResource(sList.imageGame)
    }

    override fun getItemCount(): Int {
        return gameList.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
        View.OnClickListener {
        val titleGame: TextView
        val gameImage: ImageView

        init {
            titleGame = itemView.findViewById(R.id.title_game_model)
            gameImage = itemView.findViewById(R.id.img_game)
            itemView.setOnClickListener(this)
        }

        override fun onClick(v: View) {
            val gameModel = gameList[getAdapterPosition()]

            if (R.string.desktop_mode_init == gameModel.titleGame) {
                enableRamCounter = true
                val intent = Intent(context, EmulationActivity::class.java)
                context.startActivity(intent)
            }
        }
    }

    class GameList(var titleGame: Int, var imageGame: Int)
}
