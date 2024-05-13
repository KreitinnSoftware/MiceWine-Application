package com.micewine.emu.core;

import android.content.Context;
import android.widget.Toast;

public class GeneralUtils {
    public static final int DEFAULT_DURATION_OF_TOAST = 1;
    private String text;
    private Context ctx;

    public void showToast(Context callableCtx, String text, int duration) {
        this.ctx = callableCtx;
        this.text = text;
        final Toast toast = new Toast(this.ctx);
        toast.setDuration(duration);
        toast.setText(this.text);
        toast.show();

    }
}
