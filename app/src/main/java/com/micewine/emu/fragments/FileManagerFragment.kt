package com.micewine.emu.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.micewine.emu.R
import com.micewine.emu.activities.MainActivity.Companion.fileManagerCwd
import com.micewine.emu.activities.MainActivity.Companion.fileManagerDefaultDir
import com.micewine.emu.adapters.AdapterFiles
import com.micewine.emu.databinding.FragmentFileManagerBinding
import java.io.File
import java.nio.file.Files

class FileManagerFragment: Fragment() {
    private var binding: FragmentFileManagerBinding? = null
    private var rootView: View? = null
    private val fileList: MutableList<AdapterFiles.FileList> = ArrayList()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentFileManagerBinding.inflate(inflater, container, false)
        rootView = binding!!.root

        val recyclerView = rootView?.findViewById<RecyclerView>(R.id.recyclerViewFiles)
        setAdapter(recyclerView!!)

        return rootView
    }

    private fun setAdapter(recyclerView: RecyclerView) {
        val adapterFile = AdapterFiles(fileList, requireActivity())

        recyclerView.adapter = adapterFile

        registerForContextMenu(recyclerView)

        if (fileManagerCwd != fileManagerDefaultDir) {
            addToAdapter(File(".."))
        }

        File(fileManagerCwd).listFiles()?.sorted()?.forEach {
            if (it.isDirectory) {
                addToAdapter(it)
            }
        }

        File(fileManagerCwd).listFiles()?.sorted()?.forEach {
            if (it.isFile) {
                addToAdapter(it)
            }
        }
    }

    private fun addToAdapter(file: File) {
        fileList.add(AdapterFiles.FileList(file))
    }
}