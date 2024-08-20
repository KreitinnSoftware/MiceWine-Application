package com.micewine.emu.fragments

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import com.micewine.emu.R
import com.micewine.emu.activities.MainActivity.Companion.setupDone

class SetupFragment : DialogFragment() {
    @SuppressLint("SetTextI18n")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val inflater = requireActivity().layoutInflater
        val view = inflater.inflate(R.layout.fragment_setup, null)

        val titleText = view.findViewById<TextView>(R.id.titleText)
        val progressTextBar = view.findViewById<TextView>(R.id.updateProgress)
        val progressExtractBar = view.findViewById<ProgressBar>(R.id.progressBar)

        isCancelable = false

        Thread {
            while (!setupDone) {
                requireActivity().runOnUiThread {
                    if (progressBarValue > 0) {
                        progressTextBar.text = "$progressBarValue%"
                    } else {
                        progressTextBar.text = ""
                    }

                    progressExtractBar.progress = progressBarValue
                    progressExtractBar.isIndeterminate = progressBarIsIndeterminate
                    titleText.text = dialogTitleText
                }

                Thread.sleep(16)
            }

            dismiss()
        }.start()

        return AlertDialog.Builder(requireActivity(), R.style.CustomAlertDialog).setView(view).create()
    }

    companion object {
        var progressBarValue: Int = 0
        var progressBarIsIndeterminate: Boolean = false
        var dialogTitleText: String = ""
    }
}