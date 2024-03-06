package com.micewine.emu.core.services.wine;

import static com.micewine.emu.activities.MainActivity.box64;
import static com.micewine.emu.activities.MainActivity.usrDir;
import static com.micewine.emu.activities.MainActivity.wine;
import static com.micewine.emu.activities.MainActivity.wineFolder;
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

        new Thread(() -> ExecuteCMD(exportVariables() + "; sleep 4;" +
                box64 + " " + wine + " " + "explorer /desktop=shell,1280x720 explorer")).start();
        return START_STICKY;
    }
    
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}