package com.micewine.emu.core

import android.util.Log
import java.io.BufferedReader
import java.io.DataOutputStream
import java.io.IOException
import java.io.InputStreamReader

object ShellExecutorCmd {
    var stdErrOut = ""

    fun executeShell(cmd: String, msg: String?) {
        try {
            Log.e(msg, "Trying to exec: $cmd")
            val shell = Runtime.getRuntime().exec("/system/bin/sh")
            val os = DataOutputStream(shell.outputStream)

            os.writeBytes("$cmd\nexit\n")
            os.flush()

            val stdout = BufferedReader(InputStreamReader(shell.inputStream))
            val stderr = BufferedReader(InputStreamReader(shell.errorStream))

            val stdoutThread = Thread {
                try {
                    var stdOut: String?
                    while (stdout.readLine().also { stdOut = it } != null) {
                        stdErrOut += stdOut + "\n"
                        Log.v(msg, "$stdOut")
                    }
                } catch (e: IOException) {
                    Log.e(msg, "Error reading stdout", e)
                } finally {
                    try {
                        stdout.close()
                    } catch (e: IOException) {
                        Log.e(msg, "Error closing stdout", e)
                    }
                }
            }

            val stderrThread = Thread {
                try {
                    var stdErr: String?
                    while (stderr.readLine().also { stdErr = it } != null) {
                        stdErrOut += stdErr + "\n"
                        Log.v(msg, "$stdErr")
                    }
                } catch (e: IOException) {
                    Log.e(msg, "Error reading stderr", e)
                } finally {
                    try {
                        stderr.close()
                    } catch (e: IOException) {
                        Log.e(msg, "Error closing stderr", e)
                    }
                }
            }

            stdoutThread.start()
            stderrThread.start()

            stdoutThread.join()
            stderrThread.join()

            os.close()

            shell.waitFor()
            shell.destroy()
        } catch (e: IOException) {
            Log.e(msg, "IOException occurred", e)
            throw RuntimeException(e)
        } catch (e: InterruptedException) {
            Log.e(msg, "InterruptedException occurred", e)
            Thread.currentThread().interrupt()
        }
    }
}
