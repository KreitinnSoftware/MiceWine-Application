package com.micewine.emu.core.services.xserver;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import com.micewine.emu.coreutils.EnvVars;
import static com.micewine.emu.activities.MainActivity.*;
import static com.micewine.emu.coreutils.EnvVars.*;
import com.micewine.emu.coreutils.ShellExecutorCmd;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

public class XserverLoader extends Service {
    

    private ShellExecutorCmd shellExecClass = new ShellExecutorCmd();
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        
        
        new Thread(() ->  {
                    setVariables();
            shellExecClass.executeCommand(new StringBuilder()
                    .append(exportVariables())
                    .append("; ")
                    .append("chmod 400 $CLASSPATH; mkdir -p $TMPDIR; mkdir -p $HOME; chmod 700 -R /data/data/com.micewine.emu/files/usr;")
                    .append(" ")
                    .append("/system/bin/app_process / com.micewine.emu.Loader :0")
                    .toString());
                
                }).start();
        return START_STICKY;
    }

    

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
