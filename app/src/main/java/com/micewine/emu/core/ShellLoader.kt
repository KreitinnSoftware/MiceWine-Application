package com.micewine.emu.core

import android.util.Log
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.micewine.emu.activities.EmulationActivity.Companion.handler
import com.micewine.emu.activities.EmulationActivity.Companion.sharedLogs
import com.micewine.emu.fragments.InfoDialogFragment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import java.io.BufferedReader
import java.io.DataOutputStream
import java.io.InputStreamReader

object ShellLoader {
    fun runCommandWithOutput(cmd: String, enableStdErr: Boolean = true): String = runBlocking {
        val shell = Runtime.getRuntime().exec("/system/bin/sh")
        val os = DataOutputStream(shell.outputStream).apply {
            writeBytes("$cmd\nexit\n")
            flush()
        }

        val stdout = BufferedReader(InputStreamReader(shell.inputStream))
        val stderr = BufferedReader(InputStreamReader(shell.errorStream))
        val output = StringBuilder()

        val stdoutJob = async(Dispatchers.IO) {
            var stdOut: String?
            while (stdout.readLine().also { stdOut = it } != null) {
                output.append("$stdOut\n")
            }
            stdout.close()
        }
        val stderrJob = async(Dispatchers.IO) {
            if (enableStdErr) {
                var stdErr: String?
                while (stderr.readLine().also { stdErr = it } != null) {
                    output.append("$stdErr\n")
                }
                stderr.close()
            }
        }

        stdoutJob.await()
        stderrJob.await()

        os.close()

        shell.waitFor()
        shell.destroy()

        return@runBlocking "$output"
    }

    fun runCommand(cmd: String) {
        ShellLoader().runCommand(cmd, true)
    }

    fun runCommand(cmd: String, log: Boolean) {
        ShellLoader().runCommand(cmd, log)
    }

    private class ShellLoader {
        var shell: Process? = Runtime.getRuntime().exec("/system/bin/sh")
        var os: DataOutputStream? = DataOutputStream(shell?.outputStream)
        var stdout: BufferedReader? = BufferedReader(InputStreamReader(shell?.inputStream))
        var stderr: BufferedReader? = BufferedReader(InputStreamReader(shell?.errorStream))

        fun runCommand(cmd: String, log: Boolean) = runBlocking {
            if (log) {
                Log.i("ShellLoader", "Trying to exec: '$cmd'")
            }

            os?.writeBytes("$cmd\nexit\n")
            os?.flush()

            val stdOutJob = async(Dispatchers.IO) {
                var stdOut: String?
                while (stdout?.readLine().also { stdOut = it } != null) {
                    sharedLogs?.appendText("$stdOut")
                    Log.i("ShellLoader", "$stdOut")
                }
            }
            val stdErrJob = async(Dispatchers.IO) {
                var stdOut: String?
                while (stdout?.readLine().also { stdOut = it } != null) {
                    sharedLogs?.appendText("$stdOut")
                    Log.i("ShellLoader", "$stdOut")
                }
            }

            stdOutJob.await()
            stdErrJob.await()

            shell?.waitFor()

            os?.close()
            stdout?.close()
            stderr?.close()
            shell?.destroy()
        }
    }

    class ViewModelAppLogs(private val supportFragmentManager: FragmentManager) : ViewModel() {
        val logsTextHead = MutableLiveData<String>()

        fun appendText(text: String) {
            handler.post {
                logsTextHead.value = "$text\n"

                // Check for errors
                when {
                    text.contains("err:module:import_dll") -> {
                        val missingDllName = "${text.split("Library ")[1].split(".dll")[0]}.dll"

                        Log.v("DLL Import", "Error loading '$missingDllName'")

                        InfoDialogFragment(
                            "Missing DLL",
                            "Error loading '$missingDllName'"
                        ).show(supportFragmentManager, "")
                    }
                    text.contains("VK_ERROR_DEVICE_LOST") -> {
                        Log.v("VK Driver", "VK_ERROR_DEVICE_LOST")

                        InfoDialogFragment(
                            "VK_ERROR_DEVICE_LOST",
                            "Error on Vulkan Graphics Driver 'VK_ERROR_DEVICE_LOST'"
                        ).show(supportFragmentManager, "")
                    }
                    text.contains("X_CreateWindow") -> {
                        Log.v("X11 Driver", "BadWindow: X_CreateWindow")

                        InfoDialogFragment(
                            "X_CreateWindow",
                            "Error on Creating X Window 'X_CreateWindow'"
                        ).show(supportFragmentManager, "")
                    }
                }
            }
        }
    }
}
