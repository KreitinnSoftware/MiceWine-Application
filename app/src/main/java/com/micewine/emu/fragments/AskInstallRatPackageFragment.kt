package com.micewine.emu.fragments

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import androidx.preference.PreferenceManager
import com.micewine.emu.R
import com.micewine.emu.activities.MainActivity.Companion.ACTION_INSTALL_RAT
import com.micewine.emu.core.RatPackageManager

class AskInstallRatPackageFragment : DialogFragment() {
    private var preferences: SharedPreferences? = null

    @SuppressLint("SetTextI18n")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val inflater = requireActivity().layoutInflater
        val view = inflater.inflate(R.layout.fragment_ask_install_rat, null)

        val buttonContinue = view.findViewById<Button>(R.id.buttonContinue)
        val buttonCancel = view.findViewById<Button>(R.id.buttonCancel)

        val dialog = AlertDialog.Builder(requireContext(), R.style.CustomAlertDialog).setView(view).create()

        val askInstallText = view.findViewById<TextView>(R.id.askInstallText)
        askInstallText.text = "${activity?.getString(R.string.installRatPackageWarning)} ${ratCandidate?.name} (${ratCandidate?.version})?"

        preferences = PreferenceManager.getDefaultSharedPreferences(requireContext())

        buttonContinue.setOnClickListener {
            context?.sendBroadcast(
                Intent(ACTION_INSTALL_RAT).apply {
                    putExtra("ratFile", "")
                }
            )

            dismiss()
        }

        buttonCancel.setOnClickListener {
            dismiss()
        }

        return dialog
    }

    companion object {
        var ratCandidate: RatPackageManager.RatPackage? = null
    }
}