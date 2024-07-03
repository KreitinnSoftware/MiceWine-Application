package com.micewine.emu.core

import android.annotation.SuppressLint
import com.micewine.emu.fragments.SetupFragment.Companion.progressBarIsIndeterminate
import com.micewine.emu.fragments.SetupFragment.Companion.progressBarValue
import net.lingala.zip4j.ZipFile
import net.lingala.zip4j.progress.ProgressMonitor
import java.io.IOException

object ObbExtractor {
    @SuppressLint("SetTextI18n")
    fun extractZip(zipFilePath: String?, destinationPath: String) {
        try {
            progressBarIsIndeterminate = false

            val zipFile = ZipFile(zipFilePath)

            zipFile.isRunInThread = true

            val progressMonitor = zipFile.progressMonitor
            zipFile.extractAll(destinationPath)

            while (!progressMonitor.state.equals(ProgressMonitor.State.READY)) {
                progressBarValue = progressMonitor.percentDone

                Thread.sleep(100)
            }

            progressBarValue = 0
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}
