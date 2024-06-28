package com.micewine.emu.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.micewine.emu.R
import com.micewine.emu.activities.MainActivity.Companion.fileManagerCwd
import com.micewine.emu.adapters.AdapterFiles
import java.io.File

class FileManagerFragment: Fragment() {
    private val fileList: MutableList<AdapterFiles.FileList> = ArrayList()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_file_manager, container, false)

        val recyclerView = rootView.findViewById<RecyclerView>(R.id.recyclerViewFiles)
        setAdapter(recyclerView)

        return rootView
    }

    private fun setAdapter(recyclerView: RecyclerView) {
        val adapterFile = AdapterFiles(fileList, requireActivity())

        recyclerView.adapter = adapterFile

        addToAdapter(File(".."))

        File(fileManagerCwd).listFiles()?.forEach {
            if (it.isDirectory) {
                addToAdapter(it)
            }
        }

        File(fileManagerCwd).listFiles()?.forEach {
            if (it.isFile) {
                addToAdapter(it)
            }
        }
    }

    private fun addToAdapter(file: File) {
        fileList.add(AdapterFiles.FileList(file))
    }
}