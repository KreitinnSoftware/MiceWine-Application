package com.micewine.emu.core

import android.content.Context

class Init {
    private val runServices = RunServiceClass()
    private var ctx: Context? = null
    fun run(ctx: Context?) {
        this.ctx = ctx
        //runServices.runService(OverlayService.class, this.ctx);
        runServices.runService(MainService::class.java, this.ctx)
    }

    fun stopAll() {
        runServices.stopService()
    }
}
