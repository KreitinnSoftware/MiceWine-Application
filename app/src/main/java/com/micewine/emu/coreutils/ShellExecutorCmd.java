package com.micewine.emu.coreutils;

import android.util.Log;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

public class ShellExecutorCmd {
    public static String stdOut = "";
    public static void ExecuteCMD(String cmd) {
        try {
            Log.e("ShellLoader", "Trying to exec: " + cmd);
            Process shell = Runtime.getRuntime().exec("/system/bin/sh");
            DataOutputStream os = new DataOutputStream(shell.getOutputStream());

            os.writeBytes(cmd + "\n");
            os.flush();

            os.writeBytes("exit\n");
            os.flush();

            final BufferedReader stdout = new BufferedReader(new InputStreamReader(shell.getInputStream()));
            final BufferedReader stderr = new BufferedReader(new InputStreamReader(shell.getErrorStream()));

            try {
                while ((stdOut = stdout.readLine()) != null)
                    Log.v("ShellLoader", "stdout: " + stdOut);
            } catch (IOException ignored) {
            }
            try {
                while ((stdOut = stderr.readLine()) != null)
                    Log.v("ShellLoader", "stderr: " + stdOut);
            } catch (IOException ignored) {
            }
            shell.destroy();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
