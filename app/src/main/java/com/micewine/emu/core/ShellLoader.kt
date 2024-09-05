package com.micewine.emu.core

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.micewine.emu.activities.EmulationActivity.Companion.handler
import com.micewine.emu.activities.EmulationActivity.Companion.sharedLogs
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
                        sharedLogs.appendText("$stdOut")
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
                        sharedLogs.appendText("$stdErr")
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

    class ViewModelAppLogs : ViewModel() {
        val logsText = MutableLiveData<String>()
        val logsTextHead = MutableLiveData<String>()

        fun appendText(text: String) {
            handler.post {
                logsTextHead.value = "$text\n"
                logsText.value += "$text\n"
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
