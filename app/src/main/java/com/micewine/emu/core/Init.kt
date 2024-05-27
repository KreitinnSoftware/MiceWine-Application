package com.micewine.emu.core

import android.content.Context
import android.content.Intent

class Init {
    private var ctx: Context? = null
    private var service: Intent? = null
    fun run(ctx: Context?) {
        this.ctx = ctx

        service = Intent(ctx, MainService::class.java)

        ctx!!.startService(service)
    }

    fun stopAll() {
        if (service != null)
            ctx!!.stopService(service)
    }
}
