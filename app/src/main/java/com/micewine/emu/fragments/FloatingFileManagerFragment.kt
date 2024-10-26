package com.micewine.emu.fragments

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.RecyclerView
import com.micewine.emu.R
import com.micewine.emu.activities.MainActivity.Companion.ACTION_SETUP
import com.micewine.emu.activities.MainActivity.Companion.customRootFSPath
import com.micewine.emu.activities.MainActivity.Companion.fileManagerCwd
import com.micewine.emu.adapters.AdapterFiles
import java.io.File

class FloatingFileManagerFragment: DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val inflater = requireActivity().layoutInflater
        val view = inflater.inflate(R.layout.fragment_floating_file_manager, null)

        recyclerView  = view.findViewById(R.id.recyclerViewFiles)
        recyclerView?.adapter = AdapterFiles(fileList, requireContext(), true)

        fileManagerCwd = "/storage/emulated/0"

        refreshFiles()

        val dialog = AlertDialog.Builder(requireContext(), R.style.CustomAlertDialog).setView(view).create()

        isCancelable = false

        Thread {
            while (customRootFSPath == null) {
                Thread.sleep(16)
            }

            dismiss()
        }.start()

        return dialog
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)

        if (!calledSetup) {
            calledSetup = true

            requireContext().sendBroadcast(
                Intent(ACTION_SETUP)
            )
        }
    }

    companion object {
        private var recyclerView: RecyclerView? = null
        var calledSetup: Boolean = false
        private val fileList: MutableList<AdapterFiles.FileList> = ArrayList()

        fun refreshFiles() {
            recyclerView?.adapter?.notifyItemRangeRemoved(0, fileList.count())

            fileList.clear()

            if (fileManagerCwd != "/storage/emulated/0") {
                addToAdapter(File(".."))
            }

            File(fileManagerCwd).listFiles()?.sorted()?.forEach {
                if (it.isDirectory) {
                    addToAdapter(it)
                }
            }

            File(fileManagerCwd).listFiles()?.sorted()?.forEach {
                if (it.isFile && it.name.endsWith(".rat")) {
                    addToAdapter(it)
                }
            }

            recyclerView?.adapter?.notifyItemRangeInserted(0, fileList.count())
        }

        private fun addToAdapter(file: File) {
            fileList.add(AdapterFiles.FileList(file))
        }
    }
}