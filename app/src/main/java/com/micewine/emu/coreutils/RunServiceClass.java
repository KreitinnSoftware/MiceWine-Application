package com.micewine.emu.coreutils;

import android.content.Context;
import android.content.Intent;

public class RunServiceClass {
    private Context ctx;
    private Intent service;
    public void runService(Class service, Context ctx) {
        this.ctx = ctx;
        this.service = new Intent(ctx , service);
        ctx.startService(this.service);
    }
    public void stopService() {
        ctx.stopService(this.service);
    }
}
