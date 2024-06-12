package com.micewine.emu.core

import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.micewine.emu.activities.MainActivity.Companion.getClassPath
import com.micewine.emu.activities.MainActivity.Companion.usrDir
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class MainService : Service() {
    private val serviceJob = Job()
    private val serviceScope = CoroutineScope(Dispatchers.IO + serviceJob)

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        EnvVars.setVariables()

        val exePath = intent.getStringExtra("exePath")

        serviceScope.launch {
            ShellExecutorCmd.executeShell(
                EnvVars.exportVariables() + ";" +
                        "unset LD_LIBRARY_PATH LIBGL_DRIVERS_PATH; " +
                        "export CLASSPATH=" + getClassPath(this@MainService) + ";" +
                        "/system/bin/app_process / com.micewine.emu.CmdEntryPoint :0", "XServer"
            )
        }

        serviceScope.launch {
            ShellExecutorCmd.executeShell(
                "$usrDir/bin/virgl_test_server", "VirGLServer"
            )
        }

        serviceScope.launch {
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
        }

        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()

        EnvVars.setVariables()

        ShellExecutorCmd.executeShell(EnvVars.exportVariables() + ";" +
            "box64 wineserver -k", "WineKiller")

        serviceJob.cancel()
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }
}
