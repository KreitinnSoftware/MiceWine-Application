package com.micewine.emu.core

import android.annotation.SuppressLint
import android.util.Log
import android.widget.ProgressBar
import android.widget.TextView
import com.micewine.emu.activities.MainActivity
import net.lingala.zip4j.ZipFile
import net.lingala.zip4j.progress.ProgressMonitor
import java.io.IOException
import android.widget.Toast
import java.io.File
import android.view.View
import android.content.Context
import android.content.Intent
import android.os.IBinder
import java.io.BufferedReader
import java.io.DataOutputStream
import java.io.InputStreamReader

class ObbExtractor {
    @SuppressLint("SetTextI18n")
    fun extractZip(zipFilePath: String?, destinationPath: String, progressExtractBar: ProgressBar?, progressText: TextView?, activity: MainActivity) {
        try {
            Log.v("ZipExtract", "Start Extract")

            progressExtractBar?.isIndeterminate = false

            val zipFile = ZipFile(zipFilePath)

            zipFile.isRunInThread = true

            val progressMonitor = zipFile.progressMonitor
            zipFile.extractAll(destinationPath)

            while (!progressMonitor.state.equals(ProgressMonitor.State.READY)) {
                activity.runOnUiThread {
                    progressText?.text = progressMonitor.percentDone.toString() + "%"
                    progressExtractBar?.progress = progressMonitor.percentDone
                }

                Thread.sleep(100)
            }

        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}