package com.micewine.emu.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.micewine.emu.R
import com.micewine.emu.activities.MainActivity
import com.micewine.emu.activities.MainActivity.Companion.loadGameList
import com.micewine.emu.activities.MainActivity.Companion.setSharedVars
import com.micewine.emu.adapters.AdapterGame
import com.micewine.emu.databinding.FragmentHomeBinding
import java.io.File

class HomeFragment : Fragment() {
    private var binding: FragmentHomeBinding? = null
    private var rootView: View? = null
    private val gameList: MutableList<AdapterGame.GameList> = ArrayList()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        rootView = binding!!.getRoot()

        setSharedVars(requireContext())

        val recyclerView = rootView?.findViewById<RecyclerView>(R.id.recyclerViewGame)
        setAdapter(recyclerView!!)

        return rootView
    }

    private fun setAdapter(recyclerView: RecyclerView) {
        val adapterGame = context?.let { AdapterGame(gameList, it) }
        recyclerView.setAdapter(adapterGame)

        registerForContextMenu(recyclerView)

        gameList.clear()

        addToAdapter(requireContext().getString(R.string.desktop_mode_init), requireContext().getString(R.string.desktop_mode_init), R.drawable.default_icon)

        for (game in loadGameList(requireContext())) {
            val name = game[0]
            val exePath = game[1]

            addToAdapter(exePath, name, R.drawable.default_icon)
        }
    }

    private fun addToAdapter(exeFile: String, name: String, icon: Int) {
        gameList.add(AdapterGame.GameList(File(exeFile), name,  icon))
    }
}
