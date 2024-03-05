package com.micewine.emu.coreutils;
import android.content.Context;
import android.widget.Toast;

public class GeneralUtils {
    private String text;
    private Context ctx;
    public static final int DEFAULT_DURATION_OF_TOAST = 1;
    
    public void showToast(Context callableCtx , String text , int duration) {
        this.ctx =callableCtx;
        this.text = text;
        final Toast toast = new Toast(this.ctx);
                toast.setDuration(duration);
                toast.setText(this.text);
                toast.show();
        
    }
}
