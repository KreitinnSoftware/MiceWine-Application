package com.micewine.emu.core.services.wine;

import static com.micewine.emu.activities.MainActivity.box64;
import static com.micewine.emu.activities.MainActivity.usrDir;
import static com.micewine.emu.activities.MainActivity.wine;
import static com.micewine.emu.coreutils.EnvVars.exportVariables;
import static com.micewine.emu.coreutils.EnvVars.setVariables;
import static com.micewine.emu.coreutils.ShellExecutorCmd.ExecuteCMD;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import androidx.annotation.Nullable;

public class WineService extends Service {
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        setVariables();

        new Thread(() -> ExecuteCMD(exportVariables() + "; wait-xserver; sleep 1;" +
                usrDir + "/bin/start-wine.sh", "WineService")).start();
        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}