package com.micewine.emu;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Color; 
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import androidx.annotation.NonNull;

import com.micewine.emu.input.InputStub;

import com.micewine.emu.systemutils.SystemMemoryInfo;
import java.util.regex.PatternSyntaxException;

@SuppressLint("WrongConstant")
@SuppressWarnings("deprecation")
public class LorieView extends SurfaceView implements InputStub {

    private long totalMemory = SystemMemoryInfo.getTotalRAM(getContext());
    private long freeMemory = SystemMemoryInfo.getFreeRAM(getContext());
    private Handler handler = new Handler();
    public static long BYTES_FOR_MEGABYTES = (1024*1024);
    
    
    interface Callback {
        void changed(Surface sfc, int surfaceWidth, int surfaceHeight, int screenWidth, int screenHeight);
    }

    interface PixelFormat {
        int BGRA_8888 = 5; // Stands for HAL_PIXEL_FORMAT_BGRA_8888
    }

    private Callback mCallback;
    private final Point p = new Point();
    private final SurfaceHolder.Callback mSurfaceCallback = new SurfaceHolder.Callback() {
        @Override public void surfaceCreated(@NonNull SurfaceHolder holder) {
           updateRamCounter();
            holder.setFormat(PixelFormat.BGRA_8888);
        }

        @Override public void surfaceChanged(@NonNull SurfaceHolder holder, int f, int width, int height) {
            width = getMeasuredWidth();
            height = getMeasuredHeight();

            Log.d("SurfaceChangedListener", "Surface was changed: " + width + "x" + height);
            if (mCallback == null)
                return;

            
            
            getDimensionsFromSettings();
            mCallback.changed(holder.getSurface(), width, height, p.x, p.y);
        }

        @Override public void surfaceDestroyed(@NonNull SurfaceHolder holder) {
            if (mCallback != null)
                mCallback.changed(holder.getSurface(), 0, 0, 0, 0);
        }
    };

    public LorieView(Context context) { super(context); init(); }
    public LorieView(Context context, AttributeSet attrs) { super(context, attrs); init(); }
    public LorieView(Context context, AttributeSet attrs, int defStyleAttr) { super(context, attrs, defStyleAttr); init(); }
    @SuppressWarnings("unused")
    public LorieView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) { super(context, attrs, defStyleAttr, defStyleRes); init(); }

    private void init() {
        getHolder().addCallback(mSurfaceCallback);
    }

    public void setCallback(Callback callback) {
        mCallback = callback;
        triggerCallback();
    }

    public void regenerate() {
        Callback callback = mCallback;
        mCallback = null;
        getHolder().setFormat(android.graphics.PixelFormat.RGBA_8888);
        mCallback = callback;

        triggerCallback();
    }

    public void triggerCallback() {
        setFocusable(true);
        setFocusableInTouchMode(true);
        requestFocus();

        setBackground(new ColorDrawable(Color.TRANSPARENT) {
            public boolean isStateful() {
                return true;
            }
            public boolean hasFocusStateSpecified() {
                return true;
            }
        });

        Rect r = getHolder().getSurfaceFrame();
        getActivity().runOnUiThread(() -> mSurfaceCallback.surfaceChanged(getHolder(), PixelFormat.BGRA_8888, r.width(), r.height()));
    }

    private Activity getActivity() {
        Context context = getContext();
        while (context instanceof ContextWrapper) {
            if (context instanceof Activity) {
                return (Activity) context;
            }
            context = ((ContextWrapper) context).getBaseContext();
        }

        throw new NullPointerException();
    }

    void getDimensionsFromSettings() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        int width = getMeasuredWidth();
        int height = getMeasuredHeight();
        int w = width;
        int h = height;

        String[] resolution = preferences.getString("displayResolutionExact", "1280x720").split("x");
        w = Integer.parseInt(resolution[0]);
        h = Integer.parseInt(resolution[1]);

        if ((width < height && w > h) || (width > height && w < h))
            p.set(h, w);
        else
            p.set(w, h);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        if (preferences.getBoolean("displayStretch", false)) {
            getHolder().setSizeFromLayout();
            return;
        }

        getDimensionsFromSettings();

        if (p.x <= 0 || p.y <= 0)
            return;

        int width = getMeasuredWidth();
        int height = getMeasuredHeight();

        if ((width < height && p.x > p.y) || (width > height && p.x < p.y))
            //noinspection SuspiciousNameCombination
            p.set(p.y, p.x);

        if (width > height * p.x / p.y)
            width = height * p.x / p.y;
        else
            height = width * p.y / p.x;

        getHolder().setFixedSize(p.x, p.y);
        setMeasuredDimension(width, height);
    }
    
    
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        // TODO: Implement this method
        ramCounter(canvas);
    }
    
    public void ramCounter(Canvas c) {
    Paint paint = new Paint();
        paint.setColor(Color.WHITE);
        paint.setTextSize(15);
        c.drawText("RAM: " + (totalMemory - freeMemory) / BYTES_FOR_MEGABYTES + "/" + (totalMemory / BYTES_FOR_MEGABYTES), 10, 40, paint);
    }
    
    
   private void updateRamCounter() {
    totalMemory = SystemMemoryInfo.getTotalRAM(getContext());
    freeMemory = SystemMemoryInfo.getFreeRAM(getContext());
    invalidate();
    handler.postDelayed(this::updateRamCounter, 50);
}
    

    @Override
    public void sendMouseWheelEvent(float deltaX, float deltaY) {
        sendMouseEvent(deltaX, deltaY, BUTTON_SCROLL, false, true);
    }

    @Override
    public boolean dispatchKeyEventPreIme(KeyEvent event) {
        Activity a = getActivity();
        return (a instanceof EmulationActivity) && ((EmulationActivity) a).handleKey(event);
    }

    // It is used in native code
    void setClipboardText(String text) {
        ClipboardManager clipboard = (ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE);
        clipboard.setPrimaryClip(ClipData.newPlainText("X11 clipboard", text));
    }

    static native void connect(int fd);
    native void handleXEvents();
    static native void startLogcat(int fd);
    static native void setClipboardSyncEnabled(boolean enabled);
    static native void sendWindowChange(int width, int height, int framerate);
    public native void sendMouseEvent(float x, float y, int whichButton, boolean buttonDown, boolean relative);
    public native void sendTouchEvent(int action, int id, int x, int y);
    public native boolean sendKeyEvent(int scanCode, int keyCode, boolean keyDown);
    public native void sendTextEvent(byte[] text);
    public native void sendUnicodeEvent(int code);

    static {
        System.loadLibrary("Xlorie");
    }
}
