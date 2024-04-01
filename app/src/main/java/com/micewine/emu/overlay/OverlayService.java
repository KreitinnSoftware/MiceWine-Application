package com.micewine.emu.overlay;

import static android.view.KeyEvent.KEYCODE_DPAD_UP;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.SeekBar;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.micewine.emu.LorieView;
import com.micewine.emu.R;

public class OverlayService extends Service {
    private final Handler handler = new Handler();
    private WindowManager windowManager;
    private ConstraintLayout overlayLayout;
    private LorieView lorie;
    private boolean isLongClick = false;
    private final Runnable longClickStart = () -> {
        isLongClick = true;
        Log.v("longClickUpButtonLorie", "start");
    };

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @SuppressLint({"InflateParams", "ClickableViewAccessibility"})
    @Override
    public void onCreate() {
        super.onCreate();

        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);

        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
                PixelFormat.TRANSLUCENT
        );

        LayoutInflater inflater = LayoutInflater.from(this);

        overlayLayout = (ConstraintLayout) inflater.inflate(R.layout.overlay_layout, null);

        Button stopOverlayBtn = overlayLayout.findViewById(R.id.stopOverlayView);

        Button btn_up = overlayLayout.findViewById(R.id.button_up);
        Button btn_down = overlayLayout.findViewById(R.id.button_down);
        SeekBar mAlphaControls = overlayLayout.findViewById(R.id.changeAlphaControls);

        mAlphaControls.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        lorie = new LorieView(this);

        btn_up.setOnClickListener((v) -> {
            try {
                lorie.sendKeyEvent(0, KEYCODE_DPAD_UP, true);
                waitForEventSender(KEYCODE_DPAD_UP);
            } catch (Exception err) {
                Log.e("stderr for send key", String.valueOf(err));
            }
        });

        btn_up.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    // Ação quando o botão é pressionado
                    Log.v("inicio do btn", "btn pressionado");
                    handler.postDelayed(longClickStart, 200); // Inicia um atraso para detectar o início do clique longo
                    lorie.sendKeyEvent(0, XKeyCodes.DPAD_UP, true);
                    break;
                case MotionEvent.ACTION_UP:
                    // Ação quando o botão é liberado
                    if (isLongClick) {
                        // Ação quando o clique longo termina
                        isLongClick = false;
                        waitForEventSender(XKeyCodes.DPAD_UP);
                        handler.removeCallbacksAndMessages(null); // Remove todos os callbacks para evitar que o clique curto seja detectado após o clique longo
                    } else {
                        lorie.sendKeyEvent(0, XKeyCodes.DPAD_UP, true);
                        waitForEventSender(XKeyCodes.DPAD_UP);
                    }
                    handler.removeCallbacks(longClickStart); // Remove o callback para o clique longo se o botão for liberado antes do clique longo ser detectado
                    break;
            }

            return false;
        });
        windowManager.addView(overlayLayout, params);

        stopOverlayBtn.setOnClickListener(v -> {
            stopSelf();
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (overlayLayout != null && windowManager != null) {
            windowManager.removeView(overlayLayout);
            overlayLayout = null;
        }
    }

    private void waitForEventSender(int keyCode) {
        Runnable waitForEventSender = () -> lorie.sendKeyEvent(0, keyCode, false);
        try {
            Thread.sleep(50);
        } catch (InterruptedException err) {

        }
        waitForEventSender.run();
    }
}