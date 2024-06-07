package com.micewine.emu.core

import android.annotation.SuppressLint
import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.micewine.emu.activities.MainActivity.Companion.getClassPath
import com.micewine.emu.activities.MainActivity.Companion.usrDir
import java.io.File

class MainService : Service() {
    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        EnvVars.setVariables()

        val exePath = intent.getStringExtra("exePath")

        Thread {
            ShellExecutorCmd.executeShell(
                EnvVars.exportVariables() + ";" +
                        "unset LD_LIBRARY_PATH LIBGL_DRIVERS_PATH; " +
                        "export CLASSPATH=" + getClassPath(this) + ";" +
                        "/system/bin/app_process / com.micewine.emu.CmdEntryPoint :0", "XServer"
            )
        }.start()

        Thread {
            ShellExecutorCmd.executeShell(
                "$usrDir/bin/virgl_test_server", "VirGLServer"
            )
        }.start()

        Thread {
            if (exePath!!.contains("**wine-desktop**")) {
                ShellExecutorCmd.executeShell(
                    EnvVars.exportVariables() + ";" +
                            "$usrDir/bin/start-wine.sh", "WineService"
                )
            } else {
                ShellExecutorCmd.executeShell(
                    EnvVars.exportVariables() + ";" +
                            "$usrDir/bin/start-wine.sh \"$exePath\"", "WineService"
                )
            }
        }.start()

        return START_STICKY
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }
}
