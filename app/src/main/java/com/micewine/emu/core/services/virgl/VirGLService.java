package com.micewine.emu.core.services.virgl;

import static com.micewine.emu.activities.MainActivity.virgl_test_server;

import static com.micewine.emu.coreutils.EnvVars.exportVariables;
import static com.micewine.emu.coreutils.EnvVars.setVariables;
import static com.micewine.emu.coreutils.ShellExecutorCmd.ExecuteCMD;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import androidx.annotation.Nullable;

public class VirGLService extends Service {
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        setVariables();

        new Thread(() -> ExecuteCMD(exportVariables() + "; echo VirGL; start-virglrenderer.sh", "VirGLRenderer")).start();
        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}