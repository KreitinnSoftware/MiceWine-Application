package com.micewine.emu.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class JoystickView extends SurfaceView implements SurfaceHolder.Callback {
    private final int JOYSTICK_RADIUS = 100;
    private final int BUTTON_RADIUS = 50;
    private final int DPAD_SIZE = 150;

    private float joystickCenterX, joystickCenterY;
    private boolean isJoystickPressed = false;

    private float button1CenterX, button1CenterY;
    private boolean isButton1Pressed = false;

    private float button2CenterX, button2CenterY;
    private boolean isButton2Pressed = false;

    private Paint paint;

    public JoystickView(Context context) {
        super(context);
        init();
    }

    public JoystickView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public JoystickView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        getHolder().addCallback(this);
        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setColor(Color.BLUE);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        // Initialize control positions
        joystickCenterX = getWidth() / 2;
        joystickCenterY = getHeight() / 2;

        button1CenterX = getWidth() / 4;
        button1CenterY = getHeight() / 4;

        button2CenterX = getWidth() * 3 / 4;
        button2CenterY = getHeight() / 4;

        drawControls();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        // Not needed
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        // Not needed
    }

    private void drawControls() {
        Canvas canvas = getHolder().lockCanvas();
        if (canvas != null) {
            canvas.drawColor(Color.WHITE);
            // Draw joystick
            paint.setColor(Color.BLUE);
            canvas.drawCircle(joystickCenterX, joystickCenterY, JOYSTICK_RADIUS, paint);
            // Draw button 1
            paint.setColor(isButton1Pressed ? Color.RED : Color.GREEN);
            canvas.drawCircle(button1CenterX, button1CenterY, BUTTON_RADIUS, paint);
            // Draw button 2
            paint.setColor(isButton2Pressed ? Color.RED : Color.GREEN);
            canvas.drawCircle(button2CenterX, button2CenterY, BUTTON_RADIUS, paint);
            // Draw D-pad
            paint.setColor(Color.BLACK);
            canvas.drawRect(0, getHeight() - DPAD_SIZE, DPAD_SIZE, getHeight(), paint);
            getHolder().unlockCanvasAndPost(canvas);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_MOVE:
                float x = event.getX();
                float y = event.getY();

                // Joystick
                float dx = x - joystickCenterX;
                float dy = y - joystickCenterY;
                if (Math.sqrt(dx * dx + dy * dy) <= JOYSTICK_RADIUS) {
                    joystickCenterX = x;
                    joystickCenterY = y;
                    isJoystickPressed = true;
                } else {
                    isJoystickPressed = false;
                }

                // Button 1
                if (Math.sqrt(Math.pow(x - button1CenterX, 2) + Math.pow(y - button1CenterY, 2)) <= BUTTON_RADIUS) {
                    isButton1Pressed = true;
                } else {
                    isButton1Pressed = false;
                }

                // Button 2
                if (Math.sqrt(Math.pow(x - button2CenterX, 2) + Math.pow(y - button2CenterY, 2)) <= BUTTON_RADIUS) {
                    isButton2Pressed = true;
                } else {
                    isButton2Pressed = false;
                }

                drawControls();
                break;
            case MotionEvent.ACTION_UP:
                isJoystickPressed = false;
                isButton1Pressed = false;
                isButton2Pressed = false;
                drawControls();
                break;
        }
        return true;
    }
}