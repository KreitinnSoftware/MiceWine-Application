package com.micewine.emu.core

import android.annotation.SuppressLint
import android.os.Build
import android.util.Log
import com.micewine.emu.activities.MainActivity.Companion.appRootDir
import com.micewine.emu.activities.MainActivity.Companion.ratPackagesInfoDir
import com.micewine.emu.core.ShellLoader.runCommand
import com.micewine.emu.fragments.SetupFragment.Companion.progressBarIsIndeterminate
import com.micewine.emu.fragments.SetupFragment.Companion.progressBarValue
import net.lingala.zip4j.ZipFile
import net.lingala.zip4j.progress.ProgressMonitor
import java.io.File

object RatPackageManager {
    @SuppressLint("SetTextI18n")
    fun installRat(ratPackage: RatPackage) {
        progressBarIsIndeterminate = false

        ratPackage.ratFile.use { ratFile ->
            ratFile.isRunInThread = true
            ratFile.extractAll(appRootDir.parent)

            while (!ratFile.progressMonitor.state.equals(ProgressMonitor.State.READY)) {
                progressBarValue = ratFile.progressMonitor.percentDone

                Thread.sleep(100)
            }
        }

        progressBarValue = 0

        runCommand("chmod -R 700 $appRootDir")
        runCommand("sh ${appRootDir.parent}/makeSymlinks.sh").also {
            File("${appRootDir.parent}/makeSymlinks.sh").delete()
        }

        File("${appRootDir.parent}/pkg-header").copyTo(File("$ratPackagesInfoDir/${ratPackage.name}"), overwrite = true)
    }

    class RatPackage(ratPath: String) {
        var name: String? = null
        var category: String? = null
        var version: String? = null
        var architecture: String? = null
        var ratFile: ZipFile = ZipFile(ratPath)

        init {
            ratFile.getInputStream(ratFile.getFileHeader("pkg-header")).use { inputStream ->
                val lines = inputStream.reader().readLines()

                name = lines[0].substringAfter("=")
                category = lines[1].substringAfter("=")
                version = lines[2].substringAfter("=")
                architecture = lines[3].substringAfter("=")
            }
        }
    }
}
