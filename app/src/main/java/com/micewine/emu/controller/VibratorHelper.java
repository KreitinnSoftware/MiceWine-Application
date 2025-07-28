package com.micewine.emu.controller;

import android.content.Context;
import android.os.VibrationEffect;
import android.os.Vibrator;

public class VibratorHelper {
    private static final long[] PATTERN = {0, 16};
    private static final int[] AMPLITUDES = {0, 255};
    private static Vibrator vibrator;

    public static void initialize(Context context) {
        if (vibrator == null) {
            vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        }
    }

    public static void startVibration(Context context, int intensity) {
        initialize(context);

        if (vibrator != null) {
            vibrator.cancel();
            long[] pattern = {0, 100};
            int[] amplitudes = {0, intensity};
            VibrationEffect effect = VibrationEffect.createWaveform(pattern, amplitudes, 1);
            vibrator.vibrate(effect);
        }
    }

    public static void stopVibration() {
        if (vibrator != null) {
            vibrator.cancel();
        }
    }
}
