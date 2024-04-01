package com.micewine.emu.utils;

import android.accessibilityservice.AccessibilityService;
import android.util.Log;
import android.view.KeyEvent;
import android.view.accessibility.AccessibilityEvent;

import com.micewine.emu.EmulationActivity;

import java.util.LinkedHashSet;

public class KeyInterceptor extends AccessibilityService {
    private static KeyInterceptor self;
    LinkedHashSet<Integer> pressedKeys = new LinkedHashSet<>();

    public KeyInterceptor() {
        self = this;
    }

    public static void shutdown() {
        if (self != null) {
            self.disableSelf();
            self.pressedKeys.clear();
            self = null;
        }
    }

    @Override
    public boolean onKeyEvent(KeyEvent event) {
        boolean ret = false;
        EmulationActivity instance = EmulationActivity.getInstance();

        if (instance == null)
            return false;

        else
            // We should send key releases to activity for the case if user was pressing some keys when Activity lost focus.
            // I.e. if user switched window with Win+Tab or if he was pressing Ctrl while switching activity.
            if (event.getAction() == KeyEvent.ACTION_UP)
                pressedKeys.remove(event.getKeyCode());

        Log.d("KeyInterceptor", (event.getUnicodeChar() != 0 ? (char) event.getUnicodeChar() : "") + " " + (event.getCharacters() != null ? event.getCharacters() : "") + " " + (ret ? " " : " not ") + "intercepted event " + event);

        return ret;
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent e) {
        // Disable self if it is automatically started on device boot or when activity finishes.
        if (EmulationActivity.getInstance() == null || EmulationActivity.getInstance().isFinishing()) {
            android.util.Log.d("KeyInterceptor", "finishing");
            shutdown();
        }
    }

    @Override
    public void onInterrupt() {
    }
}
