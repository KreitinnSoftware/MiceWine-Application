package com.micewine.emu.coreutils;
import android.content.Context;
import android.content.Intent;

public class RunServiceClass {
    private Context ctx;
    private Intent service;
    private Intent especificService;
    
    public void runService(Class service , Context ctx){
       this.ctx = ctx;
        this.service = new Intent(ctx , service);
        ctx.startService(this.service);
    }
    
    public void runEspecificService(Class especificService , Context ctx){
       this.ctx = ctx;
        this.especificService = new Intent(ctx , especificService);
        ctx.startService(this.especificService);
    }
    
    public void stopAllServices() {
    	ctx.stopService(this.service);
    }

}
