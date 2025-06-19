package com.micewine.emu.fragments

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import com.micewine.emu.R
import com.micewine.emu.activities.MainActivity.Companion.customRootFSPath
import com.micewine.emu.activities.MainActivity.Companion.setupDone
import com.micewine.emu.fragments.FloatingFileManagerFragment.Companion.calledSetup
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SetupFragment : DialogFragment() {
    @SuppressLint("SetTextI18n")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val inflater = requireActivity().layoutInflater
        val view = inflater.inflate(R.layout.fragment_setup, null)

        val titleText = view.findViewById<TextView>(R.id.titleText)
        val progressTextBar = view.findViewById<TextView>(R.id.updateProgress)
        val progressExtractBar = view.findViewById<ProgressBar>(R.id.progressBar)

        isCancelable = false

        lifecycleScope.launch(Dispatchers.Main) {
            while (!setupDone && !abortSetup) {
                progressTextBar.text = if (progressBarValue > 0) "$progressBarValue%" else ""
                progressExtractBar.progress = progressBarValue
                progressExtractBar.isIndeterminate = progressBarIsIndeterminate
                titleText.text = dialogTitleText
                delay(16)
            }

            if (abortSetup) {
                abortSetup = false
                calledSetup = false
                customRootFSPath = null
            }

            dismiss()
        }

        return AlertDialog.Builder(requireActivity(), R.style.CustomAlertDialog).setView(view).create()
    }

    companion object {
        var progressBarValue: Int = 0
        var progressBarIsIndeterminate: Boolean = false
        var dialogTitleText: String = ""
        var abortSetup: Boolean = false
    }
}