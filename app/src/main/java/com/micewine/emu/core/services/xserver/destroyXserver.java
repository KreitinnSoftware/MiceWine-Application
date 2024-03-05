package com.micewine.emu.core.services.xserver;
import android.util.Log;
import java.io.DataOutputStream;
import java.io.IOException;

public class destroyXserver {
    
    public void stopXserver() {
    	stop( "pkill -f \"/system/bin/app_process\"" + "; " + "unset DISPLAY=:0" + "; ");
    }
    
   private void stop(String cmd) {
        try {
            Log.v("Xserver", "Stoping" + cmd);
            Process shell = Runtime.getRuntime().exec("/system/bin/sh");
            DataOutputStream os = new DataOutputStream(shell.getOutputStream());

            os.writeBytes(cmd + "\n");
            os.flush();
            
            shell.destroy();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
