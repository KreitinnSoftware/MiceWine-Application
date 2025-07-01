package com.micewine.emu.fragments

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.micewine.emu.R
import com.micewine.emu.activities.EmulationActivity.Companion.handler
import com.micewine.emu.adapters.AdapterProcess
import com.micewine.emu.core.WineWrapper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class TaskManagerFragment : DialogFragment() {
    private lateinit var buttonExit: MaterialButton
    private lateinit var recyclerView: RecyclerView
    private val processList: MutableList<WineWrapper.ExeProcess> = mutableListOf()

    @SuppressLint("NotifyDataSetChanged")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val view = requireActivity().layoutInflater.inflate(R.layout.fragment_task_manager, null)

        buttonExit = view.findViewById(R.id.buttonExit)
        recyclerView = view.findViewById(R.id.recyclerViewTaskMgr)

        recyclerView.adapter = AdapterProcess(processList, requireContext())

        lifecycleScope.launch(Dispatchers.Default) {
            while (true) {
                val newList = withContext(Dispatchers.IO) {
                    WineWrapper.getExeProcesses()
                }

                withContext(Dispatchers.Main) {
                    processList.clear()
                    processList.addAll(newList)
                    (recyclerView.adapter as AdapterProcess).notifyDataSetChanged()
                }

                delay(750)
            }
        }

        buttonExit.setOnClickListener {
            dismiss()
        }

        return AlertDialog.Builder(requireActivity(), R.style.CustomAlertDialog).setView(view).create()
    }
}