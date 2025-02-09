package com.micewine.emu.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.micewine.emu.R
import com.micewine.emu.activities.MainActivity.Companion.appRootDir
import com.micewine.emu.adapters.AdapterRatPackage
import java.io.File

class RatManagerFragment(private val prefix: String, private val type: Int) : Fragment() {
    private val ratList: MutableList<AdapterRatPackage.Item> = mutableListOf()
    private var rootView: View? = null
    private var recyclerView: RecyclerView? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        rootView = inflater.inflate(R.layout.fragment_general_settings, container, false)
        recyclerView = rootView?.findViewById(R.id.recyclerViewGeneralSettings)

        setAdapter()

        return rootView
    }

    private fun setAdapter() {
        recyclerView?.setAdapter(AdapterRatPackage(ratList, requireContext()))

        ratList.clear()

        File("$appRootDir/packages").listFiles()?.forEach { file ->
            if (file.isDirectory && file.name.startsWith(prefix)) {
                val lines = File("$file/pkg-header").readLines()

                val name = lines[0].substringAfter("=")
                val version = lines[2].substringAfter("=")
                val canDelete = File("$file/pkg-external").exists()

                addToAdapter(name, version, file.name, canDelete)
            }
        }
    }

    private fun addToAdapter(title: String, description: String, driverFolderId: String, canDelete: Boolean) {
        ratList.add(AdapterRatPackage.Item(title, description, driverFolderId, type, canDelete))
    }
}
