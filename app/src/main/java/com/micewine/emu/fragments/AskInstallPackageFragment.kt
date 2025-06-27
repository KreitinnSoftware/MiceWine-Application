package com.micewine.emu.fragments

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.micewine.emu.R
import com.micewine.emu.activities.MainActivity.Companion.ACTION_INSTALL_ADTOOLS_DRIVER
import com.micewine.emu.activities.MainActivity.Companion.ACTION_INSTALL_RAT
import com.micewine.emu.core.RatPackageManager
import com.micewine.emu.fragments.Box64PresetManagerFragment.Companion.importBox64Preset
import com.micewine.emu.fragments.ControllerPresetManagerFragment.Companion.importControllerPreset
import com.micewine.emu.fragments.CreatePresetFragment.Companion.BOX64_PRESET
import com.micewine.emu.fragments.CreatePresetFragment.Companion.CONTROLLER_PRESET
import com.micewine.emu.fragments.CreatePresetFragment.Companion.VIRTUAL_CONTROLLER_PRESET
import com.micewine.emu.fragments.FloatingFileManagerFragment.Companion.outputFile
import com.micewine.emu.fragments.VirtualControllerPresetManagerFragment.Companion.importVirtualControllerPreset
import java.io.File

class AskInstallPackageFragment(private val packageType: Int) : DialogFragment() {

    @SuppressLint("SetTextI18n")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val inflater = requireActivity().layoutInflater
        val view = inflater.inflate(R.layout.fragment_ask_install_rat, null)

        val buttonContinue = view.findViewById<Button>(R.id.buttonContinue)
        val buttonCancel = view.findViewById<Button>(R.id.buttonCancel)

        val dialog = AlertDialog.Builder(requireContext(), R.style.CustomAlertDialog).setView(view).create()
        val askInstallText = view.findViewById<TextView>(R.id.askInstallText)

        when (packageType) {
            RAT_PACKAGE -> {
                askInstallText.text = "${activity?.getString(R.string.install_rat_package_warning)} ${ratCandidate?.name} (${ratCandidate?.version})?"
            }
            ADTOOLS_DRIVER_PACKAGE -> {
                askInstallText.text = "${activity?.getString(R.string.install_rat_package_warning)} ${adToolsDriverCandidate?.name} (${adToolsDriverCandidate?.version})?"
            }
            MWP_PRESET_PACKAGE -> {
                askInstallText.text = "${activity?.getString(R.string.install_rat_package_warning)} ${mwpPresetCandidate?.second?.path?.substringAfterLast("/")}?"
            }
        }

        buttonContinue.setOnClickListener {
            when (packageType) {
                RAT_PACKAGE -> {
                    requireContext().sendBroadcast(
                        Intent(ACTION_INSTALL_RAT)
                    )
                }
                ADTOOLS_DRIVER_PACKAGE -> {
                    requireContext().sendBroadcast(
                        Intent(ACTION_INSTALL_ADTOOLS_DRIVER)
                    )
                }
                MWP_PRESET_PACKAGE -> {
                    when (mwpPresetCandidate?.first) {
                        VIRTUAL_CONTROLLER_PRESET -> {
                            val ret = importVirtualControllerPreset(requireActivity(), mwpPresetCandidate?.second!!)
                            if (!ret) Toast.makeText(requireContext(), R.string.invalid_virtual_controller_preset_file, Toast.LENGTH_SHORT).show()
                        }
                        CONTROLLER_PRESET -> {
                            val ret = importControllerPreset(mwpPresetCandidate?.second!!)
                            if (!ret) Toast.makeText(requireContext(), R.string.invalid_controller_preset_file, Toast.LENGTH_SHORT).show()
                        }
                        BOX64_PRESET -> {
                            val ret = importBox64Preset(mwpPresetCandidate?.second!!)
                            if (!ret) Toast.makeText(requireContext(), R.string.invalid_box64_preset_file, Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }

            dismiss()
        }

        buttonCancel.setOnClickListener {
            dismiss()
        }

        return dialog
    }

    companion object {
        var ratCandidate: RatPackageManager.RatPackage? = null
        var adToolsDriverCandidate: RatPackageManager.AdrenoToolsPackage? = null
        var mwpPresetCandidate: Pair<Int, File>? = null

        const val RAT_PACKAGE = 0
        const val ADTOOLS_DRIVER_PACKAGE = 1
        const val MWP_PRESET_PACKAGE = 2
    }
}