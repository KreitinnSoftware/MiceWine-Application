package com.micewine.emu.core

import android.annotation.SuppressLint
import android.content.Context
import androidx.preference.PreferenceManager
import com.micewine.emu.activities.GeneralSettings.Companion.SELECTED_DRIVER
import com.micewine.emu.activities.MainActivity.Companion.appRootDir
import com.micewine.emu.activities.MainActivity.Companion.ratPackagesDir
import com.micewine.emu.activities.MainActivity.Companion.usrDir
import com.micewine.emu.core.ShellLoader.runCommand
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

            if (ratPackage.category == "VulkanDriver") {
                extractDir = "$ratPackagesDir/${java.util.UUID.randomUUID()}"
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
        
        if (ratPackage.category == "rootfs") {
            File("$extractDir/pkg-header").renameTo(File("$ratPackagesDir/rootfs-pkg-header"))

            val builtInVulkanDrivers = File("$extractDir/builtInVulkanDrivers")

            if (builtInVulkanDrivers.exists()) {
                builtInVulkanDrivers.readLines().forEach { line ->
                    val name = line.split(":")[0]
                    val version = line.split(":")[1]
                    val libPath = line.split(":")[2]
                    val randUUID = java.util.UUID.randomUUID()

                    File("$ratPackagesDir/$randUUID").mkdirs()
                    File("$ratPackagesDir/$randUUID/pkg-header").apply {
                        writeText("name=$name\n\nversion=$version\n\nvkDriverLib=$usrDir/lib/$libPath\n")
                    }

                    if (preferences.getString(SELECTED_DRIVER, "") == "") {
                        preferences.edit().apply {
                            putString(SELECTED_DRIVER, "$randUUID")
                            apply()
                        }
                    }
                }

                builtInVulkanDrivers.delete()
            }
        } else if (ratPackage.category == "VulkanDriver") {
            val driverPkgHeader = File("$extractDir/pkg-header")

            val name = driverPkgHeader.readLines()[0].substringAfter("=")
            val category = driverPkgHeader.readLines()[1].substringAfter("=")
            val version = driverPkgHeader.readLines()[2].substringAfter("=")
            val architecture = driverPkgHeader.readLines()[3].substringAfter("=")
            val driverLib = driverPkgHeader.readLines()[4].substringAfter("=")
            val driverLibPath = "$extractDir/files/usr/lib/$driverLib"
            
            driverPkgHeader.writeText("name=$name\ncategory=$category\nversion=$version\narchitecture=$architecture\nvkDriverLib=$driverLibPath\n")
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
}
