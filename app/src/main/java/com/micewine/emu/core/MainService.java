package com.micewine.emu.core;

import static com.micewine.emu.activities.MainActivity.usrDir;
import static com.micewine.emu.core.EnvVars.exportVariables;
import static com.micewine.emu.core.EnvVars.setVariables;
import static com.micewine.emu.core.ShellExecutorCmd.ExecuteCMD;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import androidx.annotation.Nullable;

public class MainService extends Service {
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        setVariables();

        new Thread(() -> {
            ExecuteCMD(exportVariables() + ";" +
                    "unset LD_LIBRARY_PATH LIBGL_DRIVERS_PATH; " +
                    "chmod 400 $CLASSPATH; " +
                    "/system/bin/app_process / com.micewine.emu.Loader :0", "XServer");
        }).start();

        new Thread(() -> {
            ExecuteCMD(exportVariables() + ";" +
                    usrDir + "/bin/start-virglrenderer.sh", "VirGLServer");
        }).start();

        new Thread(() -> {
            ExecuteCMD(exportVariables() + ";" +
                    usrDir + "/bin/start-wine.sh", "WineService");
        }).start();
        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
