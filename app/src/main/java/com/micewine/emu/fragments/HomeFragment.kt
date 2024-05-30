package com.micewine.emu.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.micewine.emu.R
import com.micewine.emu.activities.MainActivity.Companion.extractedAssets
import com.micewine.emu.adapters.AdapterGame
import com.micewine.emu.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {
    private var binding: FragmentHomeBinding? = null
    private var rootView: View? = null
    private val gameList: MutableList<AdapterGame.GameList> = ArrayList()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        rootView = binding!!.getRoot()

        if (extractedAssets) {
            val recyclerView = rootView?.findViewById<RecyclerView>(R.id.recyclerViewGame)
            setAdapter(recyclerView!!)
        }

        return rootView
    }

    private fun setAdapter(recyclerView: RecyclerView) {
        val adapterGame = context?.let { AdapterGame(gameList, it) }
        recyclerView.setAdapter(adapterGame)

        gameList.clear()

        addToAdapter(R.string.desktop_mode_init, R.drawable.default_icon)
    }

    private fun addToAdapter(titleId: Int, icon: Int) {
        gameList.add(AdapterGame.GameList(titleId, icon))
    }
}
