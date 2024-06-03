package com.micewine.emu.core

import android.annotation.SuppressLint
import android.app.Activity
import android.widget.ProgressBar
import android.widget.TextView
import net.lingala.zip4j.ZipFile
import net.lingala.zip4j.progress.ProgressMonitor
import java.io.IOException

class ObbExtractor {
    @SuppressLint("SetTextI18n")
    fun extractZip(zipFilePath: String?, destinationPath: String, progressExtractBar: ProgressBar?, progressText: TextView?, activity: Activity) {
        try {
            activity.runOnUiThread {
                progressExtractBar?.isIndeterminate = false
            }

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