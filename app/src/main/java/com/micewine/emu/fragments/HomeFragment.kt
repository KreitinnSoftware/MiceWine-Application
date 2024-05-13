package com.micewine.emu.fragments

import androidx.fragment.app.Fragment
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.CollapsingToolbarLayout
import com.google.android.material.appbar.MaterialToolbar
import com.micewine.emu.R
import com.micewine.emu.adapters.AdapterGame
import com.micewine.emu.databinding.FragmentHomeBinding
import com.micewine.emu.models.GameList

class HomeFragment : Fragment() {
    private var binding: FragmentHomeBinding? = null
    private var rootView: View? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        rootView = binding!!.getRoot()

        val recyclerView = rootView?.findViewById<RecyclerView>(R.id.recyclerViewGame)
        setAdapter(recyclerView!!)

        return rootView
    }

    private fun setAdapter(recyclerView: RecyclerView) {
        val gameList: MutableList<GameList> = ArrayList()
        val adapterGame = context?.let { AdapterGame(gameList, it) }
        recyclerView.setAdapter(adapterGame)
        val game = GameList(R.string.desktop_mode_init, R.drawable.default_icon)
        gameList.add(game)
    }
}
