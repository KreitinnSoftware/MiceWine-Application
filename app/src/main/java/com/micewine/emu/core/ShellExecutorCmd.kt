package com.micewine.emu.core

import android.util.Log
import java.io.BufferedReader
import java.io.DataOutputStream
import java.io.IOException
import java.io.InputStreamReader

object ShellExecutorCmd {
    var stdOut = ""
    fun ExecuteCMD(cmd: String, msg: String?) {
        try {
            Log.e(msg, "Trying to exec: $cmd")
            val shell = Runtime.getRuntime().exec("/system/bin/sh")
            val os = DataOutputStream(shell.outputStream)
            os.writeBytes(cmd + "\n")
            os.flush()
            os.writeBytes("exit\n")
            os.flush()
            val stdout = BufferedReader(InputStreamReader(shell.inputStream))
            val stderr = BufferedReader(InputStreamReader(shell.errorStream))
            try {
                while (stdout.readLine().also { stdOut = it } != null) Log.v(
                    msg,
                    "stdout: $stdOut"
                )
            } catch (ignored: IOException) {
            }
            try {
                while (stderr.readLine().also { stdOut = it } != null) Log.v(
                    msg,
                    "stderr: $stdOut"
                )
            } catch (ignored: IOException) {
            }
            shell.destroy()
        } catch (e: IOException) {
            throw RuntimeException(e)
        }
    }
}
