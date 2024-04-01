package com.micewine.emu.core.services.pulseaudio;

import static com.micewine.emu.activities.MainActivity.homeDir;
import static com.micewine.emu.activities.MainActivity.pulseAudio;
import static com.micewine.emu.coreutils.EnvVars.exportVariables;
import static com.micewine.emu.coreutils.EnvVars.setVariables;
import static com.micewine.emu.coreutils.ShellExecutorCmd.ExecuteCMD;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import androidx.annotation.Nullable;

public class PulseAudioService extends Service {
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        setVariables();

        new Thread(() -> ExecuteCMD("mkdir -p " + homeDir + " ;" + exportVariables() + ";sleep 4; " +
                pulseAudio + " --start --load=\"module-native-protocol-tcp auth-ip-acl=127.0.0.1 auth-anonymous=1\" --exit-idle-time=-1 ", "PulseAudio")).start();
        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}