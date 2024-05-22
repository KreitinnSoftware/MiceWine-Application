package com.micewine.emu.core

import android.content.Context
import android.content.Intent

class RunServiceClass {
    private var ctx: Context? = null
    private var service: Intent? = null
    fun runService(service: Class<*>?, ctx: Context?) {
        this.ctx = ctx
        this.service = Intent(ctx, service)
        ctx!!.startService(this.service)
    }

    fun stopService() {
        if (service != null) {
            ctx!!.stopService(service)
        }
    }
}
