package com.micewine.emu.fragments

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.micewine.emu.R
import com.micewine.emu.activities.MainActivity.Companion.fileManagerCwd
import com.micewine.emu.activities.MainActivity.Companion.fileManagerDefaultDir
import com.micewine.emu.adapters.AdapterFiles
import com.micewine.emu.databinding.FragmentFileManagerBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class FileManagerFragment : Fragment() {
    private var binding: FragmentFileManagerBinding? = null
    private var rootView: View? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentFileManagerBinding.inflate(inflater, container, false)
        rootView = binding!!.root

        recyclerView = rootView?.findViewById(R.id.recyclerViewFiles)
        recyclerView?.adapter = AdapterFiles(fileList, requireContext(), false)

        refreshFiles()
        registerForContextMenu(recyclerView!!)

        fragmentInstance = this

        return rootView
    }

    companion object {
        private var recyclerView: RecyclerView? = null
        private val fileList: MutableList<AdapterFiles.FileList> = ArrayList()
        private var _fragmentInstance: FileManagerFragment? = null
        private const val ANIMATION_DURATION = 300L

        var fragmentInstance
            get() = _fragmentInstance
            set(value) {
                _fragmentInstance = value
            }

        fun refreshFiles() {
            recyclerView?.let { rv ->
                if (rv.tag as? Boolean == true) return
                rv.tag = true
                rv.animate()
                    .alpha(0f)
                    .setDuration(ANIMATION_DURATION / 2)
                    .setInterpolator(AccelerateDecelerateInterpolator())
                    .setListener(object : AnimatorListenerAdapter() {
                        override fun onAnimationEnd(animation: Animator) {
                            fragmentInstance?.lifecycleScope?.launch {
                                val newFileList = withContext(Dispatchers.IO) {
                                    val filesList = mutableListOf<AdapterFiles.FileList>()
                                    if (fileManagerCwd != fileManagerDefaultDir) {
                                        filesList.add(AdapterFiles.FileList(File("..")))
                                    }
                                    File(fileManagerCwd).listFiles()
                                        ?.sorted()
                                        ?.filter { it.isDirectory }
                                        ?.forEach { filesList.add(AdapterFiles.FileList(it)) }
                                    File(fileManagerCwd).listFiles()
                                        ?.sorted()
                                        ?.filter { it.isFile }
                                        ?.forEach { filesList.add(AdapterFiles.FileList(it)) }
                                    filesList
                                }

                                withContext(Dispatchers.Main) {
                                    rv.post {
                                        fileList.clear()
                                        fileList.addAll(newFileList)

                                        rv.adapter?.notifyDataSetChanged()
                                        rv.animate()
                                            .alpha(1f)
                                            .setDuration(ANIMATION_DURATION / 2)
                                            .setInterpolator(AccelerateDecelerateInterpolator())
                                            .setListener(object : AnimatorListenerAdapter() {
                                                override fun onAnimationEnd(animation: Animator) {
                                                    rv.tag = false
                                                }
                                            })
                                    }
                                }
                            }
                        }
                    })
            }
        }

        fun deleteFile(filePath: String) {
            val index = fileList.indexOfFirst { it.file.path == filePath }

            if (File(filePath).delete()) {
                fileList.removeAt(index)

                recyclerView?.adapter?.notifyItemRemoved(index)
            }
        }
    }
}