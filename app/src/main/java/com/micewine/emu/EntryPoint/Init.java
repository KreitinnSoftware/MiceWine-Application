package com.micewine.emu.EntryPoint;
import android.content.Context;
import com.micewine.emu.core.services.wine.WineService;
import com.micewine.emu.coreutils.RunServiceClass;
import com.micewine.emu.overlay.OverlayService;
import com.micewine.emu.core.services.xserver.XserverLoader;

public class Init {
    private RunServiceClass runServices = new RunServiceClass();
    private Context ctx;
    
    public void run(Context calCtx) {
        this.ctx = calCtx;
    	runServices.runService(OverlayService.class , this.ctx);
        
    }
    
    public void runEspecificServices(Context callCtx) {
        this.ctx = callCtx;
        runServices.runEspecificService(WineService.class , this.ctx);
    }
    
    public void stop(){
        runServices.stopAllServices();
    }
}
