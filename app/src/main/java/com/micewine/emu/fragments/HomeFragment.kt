package com.micewine.emu.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.micewine.emu.R
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
        rootView = binding!!.root

        setSharedVars(requireContext())

        val recyclerView = rootView?.findViewById<RecyclerView>(R.id.recyclerViewGame)
        setAdapter(recyclerView!!)

        return rootView
    }

    private fun setAdapter(recyclerView: RecyclerView) {
        val adapterGame = AdapterGame(gameList, requireActivity())

        recyclerView.adapter = adapterGame

        registerForContextMenu(recyclerView)

        gameList.clear()

        addToAdapter(requireContext().getString(R.string.desktop_mode_init), requireContext().getString(R.string.desktop_mode_init), "")

        for (game in loadGameList(requireContext())) {
            val name = game[0]
            val exePath = game[1]
            val icon = game[2]

            addToAdapter(exePath, name, icon)
        }
    }

    private fun addToAdapter(exeFile: String, name: String, icon: String) {
        gameList.add(AdapterGame.GameList(File(exeFile), name, icon))
    }
}
