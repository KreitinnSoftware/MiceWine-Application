package com.micewine.emu.core

import android.util.Log
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.micewine.emu.activities.EmulationActivity.Companion.handler
import com.micewine.emu.activities.EmulationActivity.Companion.sharedLogs
import com.micewine.emu.fragments.InfoDialogFragment
import com.micewine.emu.fragments.InfoDialogFragment.Companion.descriptionText
import com.micewine.emu.fragments.InfoDialogFragment.Companion.titleText
import java.io.BufferedReader
import java.io.DataOutputStream
import java.io.IOException
import java.io.InputStreamReader

object ShellLoader {
    fun runCommandWithOutput(cmd: String): String {
        try {
            val shell = Runtime.getRuntime().exec("/system/bin/sh")
            val os = DataOutputStream(shell.outputStream).apply {
                writeBytes("$cmd\nexit\n")
                flush()
            }

            val stdout = BufferedReader(InputStreamReader(shell.inputStream))
            val output = StringBuilder()

            val stdoutThread = Thread {
                try {
                    var stdOut: String?
                    while (stdout.readLine().also { stdOut = it } != null) {
                        output.append("$stdOut\n")
                    }
                } catch (_: IOException) {
                } finally {
                    try {
                        stdout.close()
                    } catch (_: IOException) {
                    }
                }
            }

            stdoutThread.start()

            stdoutThread.join()

            os.close()

            shell.waitFor()
            shell.destroy()

            return output.toString()
        } catch (_: IOException) {
        }

        return ""
    }

    fun runCommand(cmd: String) {
        ShellLoader().runCommand(cmd)
    }

    private class ShellLoader {
        var shell: Process? = Runtime.getRuntime().exec("/system/bin/sh")
        var os: DataOutputStream? = DataOutputStream(shell?.outputStream)
        var stdout: BufferedReader? = BufferedReader(InputStreamReader(shell?.inputStream))
        var stderr: BufferedReader? = BufferedReader(InputStreamReader(shell?.errorStream))

        init {
            Thread {
                try {
                    var stdOut: String?
                    while ( stdout?.readLine().also { stdOut = it } != null) {
                        sharedLogs?.appendText("$stdOut")
                        Log.v("ShellLoader", "$stdOut")
                    }
                } catch (_: IOException) {
                } finally {
                    try {
                        stdout?.close()
                    } catch (_: IOException) {
                    }
                }
            }.start()

            Thread {
                try {
                    var stdErr: String?
                    while (stderr?.readLine().also { stdErr = it } != null) {
                        sharedLogs?.appendText("$stdErr")
                        Log.v("ShellLoader", "$stdErr")
                    }
                } catch (_: IOException) {
                } finally {
                    try {
                        stderr?.close()
                    } catch (_: IOException) {
                    }
                }
            }.start()
        }

        fun runCommand(cmd: String) {
            Log.v("ShellLoader", "Trying to exec: '$cmd'")
            os?.writeBytes("$cmd\nexit\n")
            os?.flush()

            shell?.waitFor()
            shell?.destroy()

            os?.close()
        }
    }

    class ViewModelAppLogs(private val supportFragmentManager: FragmentManager) : ViewModel() {
        val logsText = MutableLiveData<String>()
        val logsTextHead = MutableLiveData<String>()

        fun appendText(text: String) {
            handler.post {
                logsTextHead.value = "$text\n"
                logsText.value += "$text\n"

                // Check for errors
                if (text.contains("err:module:import_dll")) {
                    val missingDllName = "${text.split("Library ")[1].split(".dll")[0]}.dll"

                    Log.v("DLL Import", "Error loading '$missingDllName'")
                    titleText = "Missing DLL"
                    descriptionText = "Error loading '$missingDllName'"
                    InfoDialogFragment().show(supportFragmentManager, "")
                } else if (text.contains("VK_ERROR_DEVICE_LOST")) {
                    Log.v("VK Driver", "VK_ERROR_DEVICE_LOST")
                    titleText = "VK_ERROR_DEVICE_LOST"
                    descriptionText = "Error on Vulkan Graphics Driver 'VK_ERROR_DEVICE_LOST'"
                    InfoDialogFragment().show(supportFragmentManager, "")
                } else if (text.contains("X_CreateWindow")) {
                    Log.v("X11 Driver", "BadWindow: X_CreateWindow")
                    titleText = "X_CreateWindow"
                    descriptionText = "Error on Creating X Window 'X_CreateWindow'"
                    InfoDialogFragment().show(supportFragmentManager, "")
                }
            }
        }

        fun clear() {
            handler.post {
                logsTextHead.value = ""
                logsText.value = ""
            }
        }
    }
}
