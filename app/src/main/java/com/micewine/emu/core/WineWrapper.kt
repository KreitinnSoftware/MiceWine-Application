package com.micewine.emu.core

import com.micewine.emu.activities.MainActivity.Companion.deviceArch
import com.micewine.emu.activities.MainActivity.Companion.selectedCpuAffinity
import com.micewine.emu.activities.MainActivity.Companion.usrDir
import com.micewine.emu.activities.MainActivity.Companion.wineDisksFolder
import com.micewine.emu.activities.MainActivity.Companion.winePrefix
import com.micewine.emu.activities.MainActivity.Companion.winePrefixesDir
import com.micewine.emu.core.EnvVars.getEnv
import com.micewine.emu.core.ShellLoader.runCommand
import com.micewine.emu.core.ShellLoader.runCommandWithOutput
import com.micewine.emu.fragments.DebugSettingsFragment.Companion.availableCPUs
import java.io.File
import kotlin.math.abs

object WineWrapper {
    private var IS_BOX64 = if (deviceArch == "x86_64") "" else "box64"

    fun getCpuHexMask(cpuAffinityMask: String = selectedCpuAffinity!!): String {
        val availCpus = Runtime.getRuntime().availableProcessors()
        val cpuMask = MutableList(availCpus) { '0' }
        val cpuAffinity = cpuAffinityMask.replace(",", "")

        for (element in cpuAffinity) {
            cpuMask[abs(element.toString().toInt() - availCpus) - 1] = '1'
        }

        return Integer.toHexString(cpuMask.joinToString("").toInt(2))
    }

    fun maskToCpuAffinity(mask: Long): BooleanArray {
        val availCpus = Runtime.getRuntime().availableProcessors()
        val affinity = BooleanArray(availCpus) { false }
        for (i in 0 until availCpus) {
            if ((mask shr i) and 1L == 1L) {
                affinity[i] = true
            }
        }
        return affinity
    }


    fun waitForProcess(name: String) {
        while (true) {
            val wineProcesses = getExeProcesses()
            if (wineProcesses.firstOrNull { it.name == name } != null) break
            Thread.sleep(125)
        }
    }

    fun wine(args: String) {
        runCommand(
            getEnv() + "WINEPREFIX='$winePrefixesDir/$winePrefix' $IS_BOX64 wine $args"
        )
    }

    fun wine(args: String, retLog: Boolean): String {
        if (retLog) {
            return runCommandWithOutput(
                getEnv() + "BOX64_LOG=0 WINEPREFIX='$winePrefixesDir/$winePrefix' $IS_BOX64 wine $args"
            )
        }
        return ""
    }

    fun wine(args: String, cwd: String) {
        runCommand(
            "cd $cwd;" + getEnv() + "WINEPREFIX='$winePrefixesDir/$winePrefix' $IS_BOX64 wine $args"
        )
    }

    fun killAll() {
        runCommand(getEnv() + "WINEPREFIX='$winePrefixesDir/$winePrefix' $IS_BOX64 wineserver -k", false)
        runCommand("pkill -SIGINT -f .exe")
        runCommand("pkill -SIGINT -f wineserver")
    }

    fun clearDrives() {
        var letter = 'e'

        while (letter <= 'y') {
            val disk = File("$wineDisksFolder/$letter:")
            if (disk.exists()) {
                disk.delete()
            }
            letter++
        }
    }

    fun addDrive(path: String) {
        runCommand("ln -sf $path $wineDisksFolder/${getAvailableDisks()[0]}:")
    }

    private fun getAvailableDisks(): List<String> {
        var letter = 'c'
        val availableDisks = mutableListOf<String>()

        while (letter <= 'z') {
            if (!File("$wineDisksFolder/$letter:").exists()) {
                availableDisks.add("$letter")
            }
            letter++
        }

        return availableDisks
    }

    fun extractIcon(exePath: String, output: String) {
        if (exePath.endsWith(".exe", true)) {
            runCommand(
                getEnv() + "wrestool -x -t 14 '${getSanitizedPath(exePath)}' > '$output'", false
            )
        }
    }

    fun getSanitizedPath(filePath: String) : String {
        return filePath.replace("'", "'\\''")
    }

    private fun getProcessPath(processName: String, processCwd: String): String {
        val file = File(processCwd, processName)
        if (file.exists()) return file.path

        val searchPaths = listOf(
            "$wineDisksFolder/c:/windows/system32",
            "$wineDisksFolder/c:/windows"
        )
        searchPaths.forEach { path ->
            val exeFile = File(path, processName)
            if (exeFile.exists()) return "$path/$processName"
        }

        return ""
    }

    private fun getProcessRamUsageKB(pid: Int): Int {
        val statusFile = File("/proc/$pid/status")
        if (!statusFile.exists()) return 0

        statusFile.readLines().forEach {
            if (it.startsWith("VmRSS:")) {
                return it.substringAfter(":").replace("kB", "").trim().toIntOrNull() ?: 0
            }
        }
        return 0
    }

    fun getProcessCPUAffinity(pid: Int): String {
        val statusFile = File("/proc/$pid/status")
        if (!statusFile.exists()) return "0"

        statusFile.readLines().forEach {
            if (it.startsWith("Cpus_allowed:")) {
                return it.substringAfter(":").replace(",", "").trim()
            }
        }
        return "0"
    }

    fun getExeProcesses(): List<ExeProcess> {
        val exeProcesses = mutableListOf<ExeProcess>()
        File("/proc").listFiles()?.forEach {
            val unixPid = it.name.toIntOrNull()
            if (it.isDirectory && unixPid != null) {
                val cmdlineFile = File(it, "cmdline")
                if (cmdlineFile.exists()) {
                    val cmdline = cmdlineFile.readText().trim()
                    val processName = cmdline.split("\u0000").firstOrNull()
                    if (processName != null && processName.endsWith(".exe", true)) {
                        val cwd = runCommandWithOutput("readlink $it/cwd").trim()
                        val path = getProcessPath(processName, cwd)
                        val iconPath = "$usrDir/icons/${processName.substringBefore(".exe")}-icon"
                        val ramUsageKB = getProcessRamUsageKB(unixPid)
                        val cpuUsage = runCommandWithOutput("ps -p $unixPid -o %cpu=").trim().toFloatOrNull() ?: 0F

                        extractIcon(path, iconPath)

                        exeProcesses.add(
                            ExeProcess(processName, unixPid, cwd, path, iconPath, ramUsageKB, cpuUsage / availableCPUs.size)
                        )
                    }
                }
            }
        }
        return exeProcesses
    }

    class ExeProcess(
        val name: String,
        val unixPid: Int,
        val cwd: String,
        val path: String,
        val iconPath: String,
        val ramUsageKB: Int,
        val cpuUsage: Float
    )
}