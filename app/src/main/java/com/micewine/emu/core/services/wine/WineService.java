package com.micewine.emu.core.services.wine;

import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.viewmodel.ViewModelFactoryDsl;
import static com.micewine.emu.activities.MainActivity.appRootDir;
import static com.micewine.emu.activities.MainActivity.box64;
import static com.micewine.emu.activities.MainActivity.usrDir;
import static com.micewine.emu.activities.MainActivity.wineFolder;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import com.micewine.emu.activities.logAppOutput;
import com.micewine.emu.coreutils.EnvVars;
import com.micewine.emu.coreutils.ShellExecutorCmd;
import com.micewine.emu.viewmodels.ViewModelAppLogs;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

public class WineService extends Service {
    private ShellExecutorCmd shell = new ShellExecutorCmd();
    private logAppOutput logApp = new logAppOutput();
    private static StringBuilder stdOutput = new StringBuilder();
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        EnvVars.setVariables();
        new Thread(() -> executeCommand(EnvVars.exportVariables() + "; " + "export DISPLAY=:0" + "; "+ "chmod -R 755 " + box64 + "; " + "chmod -R 755 " + usrDir +"/bin"+ "; " + "chmod -R 755 " + wineFolder + "; " + "sleep 4; "+  box64 + " " + wineFolder + "/x86_64/bin/wine explorer /desktop=shell,1280x720 explorer")).start();
        return START_STICKY;
    }

    
    
   public void executeCommand(String cmd) {
        try {
            Log.v("WineService", "Trying to exec: " + cmd);
            Process shell = Runtime.getRuntime().exec("/system/bin/sh");
            DataOutputStream os = new DataOutputStream(shell.getOutputStream());

            os.writeBytes(cmd + "\n");
            os.flush();

            os.writeBytes("exit\n");
            os.flush();

            final BufferedReader stdout = new BufferedReader(new InputStreamReader(shell.getInputStream()));
            final BufferedReader stderr = new BufferedReader(new InputStreamReader(shell.getErrorStream()));
            new Thread(() -> {
                try {
                   String out;     
                    while ((out = stdout.readLine()) != null){
                      stdOutput.append(out +"/n");
                    }
                } catch (IOException ignored) {
                }
            }).start();
            try {
                String s;
                while ((s = stderr.readLine()) != null)
                    stdOutput.append("Errors: " + s);
            } catch (IOException ignored) {
            }
            shell.destroy();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    

    public static String getStdOut() {
    	return stdOutput.toString();
    }
    
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}