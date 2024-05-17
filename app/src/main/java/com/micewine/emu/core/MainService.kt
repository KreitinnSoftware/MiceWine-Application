package com.micewine.emu.core

import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.micewine.emu.activities.MainActivity.Companion.getClassPath
import com.micewine.emu.activities.MainActivity.Companion.usrDir

class MainService : Service() {
    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        EnvVars.setVariables()

        Thread {
            ShellExecutorCmd.ExecuteCMD(
                EnvVars.exportVariables() + ";" +
                        "unset LD_LIBRARY_PATH LIBGL_DRIVERS_PATH; " +
                        "export CLASSPATH=" + getClassPath(this) + ";" +
                        "ls -la " + getClassPath(this) + ";" +
                        "/system/bin/app_process / com.micewine.emu.CmdEntryPoint :0", "XServer"
            )
        }.start()

        Thread {
            ShellExecutorCmd.ExecuteCMD(
                EnvVars.exportVariables() + ";" +
                        usrDir + "/bin/virgl_test_server", "VirGLServer"
            )
        }.start()

        Thread {
            ShellExecutorCmd.ExecuteCMD(
                EnvVars.exportVariables() + ";" +
                        usrDir + "/bin/start-wine.sh &> /sdcard/aaaa", "WineService"
            )
        }.start()
        return START_STICKY
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }
}
