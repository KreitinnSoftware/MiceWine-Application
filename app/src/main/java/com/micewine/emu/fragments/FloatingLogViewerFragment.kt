package com.micewine.emu.fragments

import android.app.AlertDialog
import android.app.Dialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.DialogInterface
import android.os.Bundle
import android.widget.ScrollView
import android.widget.TextView
import androidx.core.content.ContextCompat.getSystemService
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.Observer
import com.google.android.material.button.MaterialButton
import com.micewine.emu.R
import com.micewine.emu.activities.EmulationActivity.Companion.sharedLogs


class FloatingLogViewerFragment: DialogFragment() {
    private var observer: Observer<String>? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val inflater = requireActivity().layoutInflater
        val view = inflater.inflate(R.layout.fragment_floating_log_viewer, null)
        val logText = view.findViewById<TextView>(R.id.logShell)
        val scrollView = view.findViewById<ScrollView>(R.id.scrollView)
        val closeButton = view.findViewById<MaterialButton>(R.id.closeButton)
        val copyButton = view.findViewById<MaterialButton>(R.id.copyButton)
        val clipboard: ClipboardManager? = getSystemService(requireContext(), ClipboardManager::class.java)
        val dialog = AlertDialog.Builder(requireContext(), R.style.CustomAlertDialog)
            .setView(view)
            .create()

        observer = Observer { out: String? ->
            if (out != null) {
                logText.append("$out")
                scrollView.fullScroll(ScrollView.FOCUS_UP)
            }
        }

        logText.text = sharedLogs.logsText.value

        sharedLogs.logsTextHead.observe(this, observer!!)

        scrollView.fullScroll(ScrollView.FOCUS_UP)

        closeButton.setOnClickListener {
            dismiss()
        }

        copyButton.setOnClickListener {
            val clip = ClipData.newPlainText("MiceWine Logs", sharedLogs.logsText.value)
            clipboard?.setPrimaryClip(clip)
        }

        return dialog
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)

        sharedLogs.logsText.removeObserver(observer!!)
    }
}