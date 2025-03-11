package com.micewine.emu.core

import android.annotation.SuppressLint
import android.content.Context
import androidx.preference.PreferenceManager
import com.micewine.emu.R
import com.micewine.emu.activities.GeneralSettingsActivity.Companion.SELECTED_BOX64
import com.micewine.emu.activities.GeneralSettingsActivity.Companion.SELECTED_DRIVER
import com.micewine.emu.activities.MainActivity.Companion.appRootDir
import com.micewine.emu.activities.MainActivity.Companion.ratPackagesDir
import com.micewine.emu.activities.MainActivity.Companion.setupDone
import com.micewine.emu.core.ShellLoader.runCommand
import com.micewine.emu.fragments.SetupFragment.Companion.dialogTitleText
import com.micewine.emu.fragments.SetupFragment.Companion.progressBarIsIndeterminate
import com.micewine.emu.fragments.SetupFragment.Companion.progressBarValue
import net.lingala.zip4j.ZipFile
import net.lingala.zip4j.progress.ProgressMonitor
import java.io.File

object RatPackageManager {
    @SuppressLint("SetTextI18n")
    fun installRat(ratPackage: RatPackage, context: Context) {
        val preferences = PreferenceManager.getDefaultSharedPreferences(context)

        progressBarIsIndeterminate = false

        var extractDir = appRootDir.parent

        ratPackage.ratFile.use { ratFile ->
            ratFile.isRunInThread = true

            if (ratPackage.category == "rootfs") {
                installingRootFS = true
            } else {
                extractDir = "$ratPackagesDir/${ratPackage.category}-${java.util.UUID.randomUUID()}"
                File(extractDir!!).mkdirs()
            }

            ratFile.extractAll(extractDir)

            while (!ratFile.progressMonitor.state.equals(ProgressMonitor.State.READY)) {
                progressBarValue = ratFile.progressMonitor.percentDone

                Thread.sleep(100)
            }
        }

        progressBarValue = 0

        runCommand("chmod -R 700 $extractDir")
        runCommand("sh $extractDir/makeSymlinks.sh").also {
            File("$extractDir/makeSymlinks.sh").delete()
        }

        when (ratPackage.category) {
            "rootfs" -> {
                File("$extractDir/pkg-header").renameTo(File("$ratPackagesDir/rootfs-pkg-header"))

                val vulkanDriversFolder = File("$extractDir/vulkanDrivers")
                val box64Folder = File("$extractDir/box64")
                val wineFolder = File("$extractDir/wine")

                dialogTitleText = context.getString(R.string.installing_drivers)

                if (vulkanDriversFolder.exists()) {
                    vulkanDriversFolder.listFiles()?.sorted()?.forEach { ratFile ->
                        installRat(RatPackage(ratFile.path), context)
                    }

                    vulkanDriversFolder.deleteRecursively()
                }

                dialogTitleText = context.getString(R.string.installing_box64)

                if (box64Folder.exists()) {
                    box64Folder.listFiles()?.sorted()?.forEach { ratFile ->
                        installRat(RatPackage(ratFile.path), context)
                    }

                    box64Folder.deleteRecursively()
                }

                dialogTitleText = context.getString(R.string.installing_wine)

                if (wineFolder.exists()) {
                    wineFolder.listFiles()?.sorted()?.forEach { ratFile ->
                        installRat(RatPackage(ratFile.path), context)
                    }

                    wineFolder.deleteRecursively()
                }

                installingRootFS = false
            }
            "VulkanDriver" -> {
                val driverLibPath = "$extractDir/files/usr/lib/${ratPackage.driverLib}"

                File("$extractDir/pkg-header").writeText("name=${ratPackage.name}\ncategory=${ratPackage.category}\nversion=${ratPackage.version}\narchitecture=${ratPackage.architecture}\nvkDriverLib=$driverLibPath\n")

                if (!installingRootFS) {
                    File("$extractDir/pkg-external").writeText("")
                }
            }
            "Box64" -> {
                preferences.apply {
                    if (getString(SELECTED_BOX64, "") == "") {
                        edit().apply {
                            putString(SELECTED_BOX64, File(extractDir!!).name)
                            apply()
                        }
                    }
                }

                File("$extractDir/pkg-header").writeText("name=${ratPackage.name}\ncategory=${ratPackage.category}\nversion=${ratPackage.version}\narchitecture=${ratPackage.architecture}\nvkDriverLib=\n")

                if (!installingRootFS) {
                    File("$extractDir/pkg-external").writeText("")
                }
            }
            "Wine" -> {
                File("$extractDir/pkg-header").writeText("name=${ratPackage.name}\ncategory=${ratPackage.category}\nversion=${ratPackage.version}\narchitecture=${ratPackage.architecture}\nvkDriverLib=\n")

                if (!installingRootFS) {
                    File("$extractDir/pkg-external").writeText("")
                }
            }
        }
    }

    class RatPackage(ratPath: String) {
        var name: String? = null
        var category: String? = null
        var version: String? = null
        var architecture: String? = null
        var driverLib: String? = null
        var ratFile: ZipFile = ZipFile(ratPath)

        init {
            ratFile.getInputStream(ratFile.getFileHeader("pkg-header")).use { inputStream ->
                val lines = inputStream.reader().readLines()

                name = lines[0].substringAfter("=")
                category = lines[1].substringAfter("=")
                version = lines[2].substringAfter("=")
                architecture = lines[3].substringAfter("=")
                driverLib = lines[4].substringAfter("=")
            }
        }
    }

    private var installingRootFS: Boolean = false
}
