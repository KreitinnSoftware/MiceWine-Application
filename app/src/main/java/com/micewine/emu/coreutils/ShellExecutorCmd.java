package com.micewine.emu.coreutils;
import android.util.Log;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.channels.InterruptedByTimeoutException;

public class ShellExecutorCmd {
    public String CMD;
    private String out = "";
    private String err = "";
    
   public void executeCommand(String cmd) {
        try {
            this.CMD = cmd;
            Log.v("ShellLoader", "Trying to exec: " + cmd);
            Process shell = Runtime.getRuntime().exec("/system/bin/sh");
            DataOutputStream os = new DataOutputStream(shell.getOutputStream());

        
            os.writeBytes(cmd + "\n");
            os.flush();
       
            os.writeBytes(cmd + "\n");
            os.flush();
            

            final BufferedReader stdout = new BufferedReader(new InputStreamReader(shell.getInputStream()));
            final BufferedReader stderr = new BufferedReader(new InputStreamReader(shell.getErrorStream()));
            
                try {
                    while ((this.out = stdout.readLine()) != null)
                        Log.v("ShellLoader", "stdout: " + out);
                } catch (IOException ignored) {
                }
           
            try {
                while ((err = stderr.readLine()) != null)
                    Log.v("ShellLoader", "stderr: " + err);
            } catch (IOException ignored) {
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    
    public String getOut() {
    	return this.out;
    }
    
    public String getErr() {
    	return this.err;
    }
    
}
