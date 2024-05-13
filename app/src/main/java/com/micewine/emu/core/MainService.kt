package com.micewine.emu.core

import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.micewine.emu.activities.MainActivity.Companion.usrDir

class MainService : Service() {
    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        EnvVars.setVariables()

        Thread {
            ShellExecutorCmd.ExecuteCMD(
                EnvVars.exportVariables() + ";" +
                        "unset LD_LIBRARY_PATH LIBGL_DRIVERS_PATH; " +
                        "chmod 400 \$CLASSPATH; " +
                        "/system/bin/app_process / com.micewine.emu.Loader :0", "XServer"
            )
        }.start()

        Thread {
            ShellExecutorCmd.ExecuteCMD(
                EnvVars.exportVariables() + ";" +
                        usrDir + "/bin/start-virglrenderer.sh", "VirGLServer"
            )
        }.start()

        Thread {
            ShellExecutorCmd.ExecuteCMD(
                EnvVars.exportVariables() + ";" +
                        usrDir + "/bin/start-wine.sh", "WineService"
            )
        }.start()
        return START_STICKY
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }
}
