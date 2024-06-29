package com.micewine.emu.fragments

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.micewine.emu.R
import com.micewine.emu.activities.MainActivity.Companion.appRootDir
import com.micewine.emu.activities.MainActivity.Companion.copyAssets
import com.micewine.emu.activities.MainActivity.Companion.homeDir
import com.micewine.emu.activities.MainActivity.Companion.setupDone
import com.micewine.emu.activities.MainActivity.Companion.setupWinePrefix
import com.micewine.emu.activities.MainActivity.Companion.tmpDir
import com.micewine.emu.activities.MainActivity.Companion.usrDir
import com.micewine.emu.core.ObbExtractor.extractZip
import com.micewine.emu.core.ShellExecutorCmd.executeShell
import com.micewine.emu.core.ShellExecutorCmd.executeShellWithOutput
import java.io.File

class ExtractingFilesFragment : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val inflater = requireActivity().layoutInflater
        val view = inflater.inflate(R.layout.fragment_extracting_files, null)

        val titleText = view.findViewById<TextView>(R.id.titleText)
        val progressTextBar = view.findViewById<TextView>(R.id.updateProgress)
        val progressExtractBar = view.findViewById<ProgressBar>(R.id.progressBar)

        val dialog = AlertDialog.Builder(requireActivity(), R.style.CustomAlertDialog)
            .setView(view)
            .create()

        isCancelable = false

        Thread {
            if (!appRootDir.exists()) {
                appRootDir.mkdirs()
            }

            progressExtractBar.isIndeterminate = true

            if (!usrDir.exists()) {
                copyAssets(requireActivity(), "rootfs.zip", appRootDir.toString(), progressTextBar!!)

                extractZip("$appRootDir/rootfs.zip", "$appRootDir", progressExtractBar, progressTextBar, requireActivity())

                File("$appRootDir/rootfs.zip").delete()

                executeShellWithOutput("chmod 700 -R $appRootDir")
                executeShellWithOutput("$usrDir/generateSymlinks.sh")

                File("$usrDir/icons").mkdirs()
            }

            if (!tmpDir.exists())
                tmpDir.mkdirs()

            if (!homeDir.exists())
                homeDir.mkdirs()

            requireActivity().runOnUiThread {
                titleText.text = getString(R.string.creatingWinePrefix)
            }

            progressTextBar.text = ""

            progressExtractBar.isIndeterminate = true

            setupWinePrefix(File("$homeDir/.wine"))

            setupDone = true

            dismiss()
        }.start()

        return dialog
    }
}