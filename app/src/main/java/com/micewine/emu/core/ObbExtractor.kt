package com.micewine.emu.core

import android.app.Activity
import android.content.Context
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import com.micewine.emu.activities.MainActivity
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

class ObbExtractor {
    private var ctx: Context? = null
    fun extractZip(
        ctx: Context?,
        zipFilePath: String?,
        destinationPath: String,
        progressBar: ProgressBar,
        tvProgress: TextView,
        atv: Activity
    ) {
        this.ctx = ctx
        try {
            val zipFile = File(zipFilePath)
            val destinationDir = File(destinationPath)
            val assignedPath: File = File(MainActivity.Companion.appRootDir.toString() + "/box64")
            if (!zipFile.exists() || !destinationDir.exists() || assignedPath.exists()) {
                atv.runOnUiThread {
                    Toast.makeText(
                        this.ctx,
                        "Arquives has been intalled, setup desktop",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                atv.runOnUiThread { progressBar.visibility = View.GONE }
                return
            }
            if (!destinationDir.exists()) {
                destinationDir.mkdirs()
            }
            var fis = FileInputStream(zipFile)
            var bis = BufferedInputStream(fis)
            var zis = ZipInputStream(bis)
            var totalEntries = 0
            var entry: ZipEntry
            while (zis.getNextEntry().also { entry = it } != null) {
                totalEntries++
            }
            zis.close()
            fis = FileInputStream(zipFile)
            bis = BufferedInputStream(fis)
            zis = ZipInputStream(bis)
            var processedEntries = 0
            while (zis.getNextEntry().also { entry = it } != null) {
                processedEntries++
                val entryPath = destinationPath + File.separator + entry.name
                if (entry.isDirectory) {
                    val dir = File(entryPath)
                    dir.mkdirs()
                    continue
                }
                val entryFile = File(entryPath)
                entryFile.getParentFile().mkdirs()
                val fos = FileOutputStream(entryFile)
                val bos = BufferedOutputStream(fos)
                val buffer = ByteArray(1024)
                var bytesRead: Int
                while (zis.read(buffer).also { bytesRead = it } != -1) {
                    bos.write(buffer, 0, bytesRead)
                }
                bos.close()
                val pEntries = processedEntries
                val tEntries = totalEntries
                val progress = (processedEntries / totalEntries.toFloat() * 100).toInt()
                progressBar.progress = progress
                atv.runOnUiThread { tvProgress.text = "$progress%($pEntries/$tEntries)" }
            }
            zis.close()
            atv.runOnUiThread {
                Toast.makeText(
                    this.ctx,
                    "Arquives has been intalled, setup desktop",
                    Toast.LENGTH_SHORT
                ).show()
            }
            atv.runOnUiThread { progressBar.visibility = View.GONE }
            atv.runOnUiThread { tvProgress.text = " " }
            atv.runOnUiThread { tvProgress.visibility = View.GONE }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}