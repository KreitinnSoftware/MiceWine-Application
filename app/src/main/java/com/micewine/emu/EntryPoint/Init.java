package com.micewine.emu.EntryPoint;

import android.content.Context;
import android.util.Log;

import com.micewine.emu.core.services.pulseaudio.PulseAudioService;
import com.micewine.emu.core.services.virgl.VirGLService;
import com.micewine.emu.core.services.wine.WineService;
import com.micewine.emu.core.services.xserver.XServerLoader;
import com.micewine.emu.coreutils.RunServiceClass;

public class Init {
    private final RunServiceClass runServices = new RunServiceClass();
    private Context ctx;

    public void run(Context ctx) {
        this.ctx = ctx;
        //runServices.runService(OverlayService.class, this.ctx);
        runServices.runService(XServerLoader.class, this.ctx);
        runServices.runService(PulseAudioService.class, this.ctx);
        runServices.runService(VirGLService.class, this.ctx);
        runServices.runService(WineService.class, this.ctx);
    }

    public void stopAll() {
        runServices.stopService();
    }
}
