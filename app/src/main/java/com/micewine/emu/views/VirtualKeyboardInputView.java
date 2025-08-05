package com.micewine.emu.views;

import static com.micewine.emu.activities.MainActivity.preferences;
import static com.micewine.emu.activities.PresetManagerActivity.SELECTED_VIRTUAL_CONTROLLER_PRESET;
import static com.micewine.emu.controller.ControllerUtils.DOWN;
import static com.micewine.emu.controller.ControllerUtils.LEFT;
import static com.micewine.emu.controller.ControllerUtils.LEFT_DOWN;
import static com.micewine.emu.controller.ControllerUtils.LEFT_UP;
import static com.micewine.emu.controller.ControllerUtils.MOUSE;
import static com.micewine.emu.controller.ControllerUtils.RIGHT;
import static com.micewine.emu.controller.ControllerUtils.RIGHT_DOWN;
import static com.micewine.emu.controller.ControllerUtils.RIGHT_UP;
import static com.micewine.emu.controller.ControllerUtils.SCROLL_DOWN;
import static com.micewine.emu.controller.ControllerUtils.SCROLL_UP;
import static com.micewine.emu.controller.ControllerUtils.UP;
import static com.micewine.emu.controller.ControllerUtils.getAxisStatus;
import static com.micewine.emu.controller.ControllerUtils.handleAxis;
import static com.micewine.emu.controller.ControllerUtils.handleKey;
import static com.micewine.emu.controller.XKeyCodes.getMapping;
import static com.micewine.emu.fragments.VirtualControllerPresetManagerFragment.*;
import static com.micewine.emu.fragments.VirtualControllerPresetManagerFragment.getVirtualControllerPreset;
import static com.micewine.emu.input.InputStub.BUTTON_LEFT;
import static com.micewine.emu.input.InputStub.BUTTON_MIDDLE;
import static com.micewine.emu.input.InputStub.BUTTON_RIGHT;
import static com.micewine.emu.input.InputStub.BUTTON_UNDEFINED;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;

import com.micewine.emu.LorieView;
import com.micewine.emu.R;
import com.micewine.emu.controller.ControllerUtils;
import com.micewine.emu.controller.XKeyCodes.ButtonMapping;

import java.util.ArrayList;

public class VirtualKeyboardInputView extends View {
    public VirtualKeyboardInputView(Context context) {
        super(context);
        init();
    }

    public VirtualKeyboardInputView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public VirtualKeyboardInputView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private final LorieView lorieView = new LorieView(getContext());
    private Paint paint;
    private Paint textPaint;
    private final Path dpadUp = new Path();
    private final Path dpadDown = new Path();
    private final Path dpadLeft = new Path();
    private final Path dpadRight = new Path();

    private void init() {
        paint = new Paint();
        paint.setStrokeWidth(16F);
        paint.setColor(Color.WHITE);
        paint.setStyle(Paint.Style.STROKE);

        textPaint = new Paint();
        textPaint.setColor(Color.WHITE);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setTextSize(120F);
        textPaint.setTypeface(getContext().getResources().getFont(R.font.quicksand));
    }

    public void loadPreset(String name) {
        String globalPreset = preferences.getString(SELECTED_VIRTUAL_CONTROLLER_PRESET, "default");
        String presetName = name.equals("--") ? globalPreset : name;
        VirtualControllerPreset preset = getVirtualControllerPreset(presetName);

        if (preset == null) return;

        buttonList.clear();
        analogList.clear();
        dpadList.clear();

        preset.buttons.forEach((i) -> {
            switch (i.keyName) {
                case "M_Left" -> i.buttonMapping = new ButtonMapping(i.keyName, BUTTON_LEFT, BUTTON_LEFT, MOUSE);
                case "M_Middle" -> i.buttonMapping = new ButtonMapping(i.keyName, BUTTON_MIDDLE, BUTTON_MIDDLE, MOUSE);
                case "M_Right" -> i.buttonMapping = new ButtonMapping(i.keyName, BUTTON_RIGHT, BUTTON_RIGHT, MOUSE);
                case "M_WheelUp" -> i.buttonMapping = new ButtonMapping(i.keyName, SCROLL_UP, SCROLL_UP, MOUSE);
                case "M_WheelDown" -> i.buttonMapping = new ButtonMapping(i.keyName, SCROLL_DOWN, SCROLL_DOWN, MOUSE);
                case "Mouse" -> i.buttonMapping = new ButtonMapping(i.keyName, MOUSE, MOUSE, MOUSE);
                default -> i.buttonMapping = getMapping(i.keyName);
            }

            buttonList.add(i);
        });
        preset.analogs.forEach((i) -> {
            i.upKeyCodes = getMapping(i.upKeyName);
            i.downKeyCodes = getMapping(i.downKeyName);
            i.leftKeyCodes = getMapping(i.leftKeyName);
            i.rightKeyCodes = getMapping(i.rightKeyName);

            analogList.add(i);
        });
        preset.dpads.forEach((i) -> {
            i.upKeyCodes = getMapping(i.upKeyName);
            i.downKeyCodes = getMapping(i.downKeyName);
            i.leftKeyCodes = getMapping(i.leftKeyName);
            i.rightKeyCodes = getMapping(i.rightKeyName);

            dpadList.add(i);
        });
    }

    private void drawDPad(Path path, boolean isPressed, Canvas canvas) {
        if (isPressed) {
            paint.setStyle(Paint.Style.FILL_AND_STROKE);
            textPaint.setColor(Color.BLACK);
        } else {
            paint.setStyle(Paint.Style.STROKE);
            textPaint.setColor(Color.WHITE);
        }
        paint.setAlpha(200);
        textPaint.setAlpha(200);

        canvas.drawPath(path, paint);
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);

        buttonList.forEach((i) -> {
            if (i.isPressed) {
                paint.setStyle(Paint.Style.FILL_AND_STROKE);
                textPaint.setColor(Color.BLACK);
            } else {
                paint.setStyle(Paint.Style.STROKE);
                textPaint.setColor(Color.WHITE);
            }
            paint.setColor(Color.WHITE);
            paint.setAlpha(200);
            paint.setStrokeWidth(16F);
            textPaint.setAlpha(200);

            switch (i.shape) {
                case SHAPE_CIRCLE -> canvas.drawCircle(i.x, i.y, i.radius / 2, paint);
                case SHAPE_RECTANGLE -> canvas.drawRoundRect(
                        i.x - i.radius / 2,
                        i.y - i.radius / 4,
                        i.x + i.radius / 2,
                        i.y + i.radius / 4,
                        32F,
                        32F,
                        paint
                );
                case SHAPE_SQUARE -> canvas.drawRoundRect(
                        i.x - i.radius / 2,
                        i.y - i.radius / 2,
                        i.x + i.radius / 2,
                        i.y + i.radius / 2,
                        32F,
                        32F,
                        paint
                );
            }

            textPaint.setTextSize(i.radius / 4F);
            float offset = (textPaint.getFontMetrics().ascent + textPaint.getFontMetrics().descent) / 2F;
            canvas.drawText(i.keyName, i.x, i.y - offset - 4F, textPaint);
        });
        analogList.forEach((i) -> {
            float analogX = i.x + i.fingerX;
            float analogY = i.y + i.fingerY;

            float distSquared = (i.fingerX * i.fingerX) + (i.fingerY * i.fingerY);
            float maxDist = (i.radius / 4F) * (i.radius / 4F);

            if (distSquared > maxDist) {
                float dist = (float) Math.sqrt(distSquared);
                float scale = (i.radius / 4F) / dist;
                analogX = i.x + (i.fingerX * scale);
                analogY = i.y + (i.fingerY * scale);
            }

            paint.setColor(Color.WHITE);
            paint.setAlpha(200);

            paint.setStyle(Paint.Style.STROKE);
            canvas.drawCircle(i.x, i.y, i.radius / 2F, paint);

            paint.setStyle(Paint.Style.FILL);
            canvas.drawCircle(analogX, analogY, i.radius / 4F, paint);
        });
        dpadList.forEach((i) -> {
            // D-Pad Left
            dpadLeft.reset();
            dpadLeft.moveTo(i.x - 20F, i.y);
            dpadLeft.lineTo(i.x - 20F - i.radius / 4F, i.y - i.radius / 4F);
            dpadLeft.lineTo(i.x - 20F - i.radius / 4F - i.radius / 2F, i.y - i.radius / 4F);
            dpadLeft.lineTo(
                    i.x - 20F - i.radius / 4F - i.radius / 2F,
                    i.y - i.radius / 4F + i.radius / 2F
            );
            dpadLeft.lineTo(i.x - 20F - i.radius / 4F, i.y - i.radius / 4F + i.radius / 2F);
            dpadLeft.lineTo(i.x - 20F, i.y);
            dpadLeft.close();

            // D-Pad Up
            dpadUp.reset();
            dpadUp.moveTo(i.x, i.y - 20F);
            dpadUp.lineTo(i.x - i.radius / 4F, i.y - 20F - i.radius / 4F);
            dpadUp.lineTo(i.x - i.radius / 4F, i.y - 20F - i.radius / 4F - i.radius / 2F);
            dpadUp.lineTo(
                    i.x - i.radius / 4F + i.radius / 2F,
                    i.y - 20F - i.radius / 4F - i.radius / 2F
            );
            dpadUp.lineTo(i.x - i.radius / 4 + i.radius / 2F, i.y - 20F - i.radius / 4F);
            dpadUp.lineTo(i.x, i.y - 20F);
            dpadUp.close();

            // D-Pad Right
            dpadRight.reset();
            dpadRight.moveTo(i.x + 20F, i.y);
            dpadRight.lineTo(i.x + 20F + i.radius / 4F, i.y - i.radius / 4F);
            dpadRight.lineTo(i.x + 20F + i.radius / 4F + i.radius / 2F, i.y - i.radius / 4F);
            dpadRight.lineTo(
                    i.x + 20 + i.radius / 4 + i.radius / 2,
                    i.y - i.radius / 4 + i.radius / 2
            );
            dpadRight.lineTo(i.x + 20F + i.radius / 4F, i.y - i.radius / 4F + i.radius / 2F);
            dpadRight.lineTo(i.x + 20F, i.y);
            dpadRight.close();

            // D-Pad Down
            dpadDown.reset();
            dpadDown.moveTo(i.x, i.y + 20F);
            dpadDown.lineTo(i.x - i.radius / 4F, i.y + 20F + i.radius / 4F);
            dpadDown.lineTo(i.x - i.radius / 4F, i.y + 20F + i.radius / 4F + i.radius / 2F);
            dpadDown.lineTo(
                    i.x - i.radius / 4F + i.radius / 2F,
                    i.y + 20F + i.radius / 4F + i.radius / 2F
            );
            dpadDown.lineTo(i.x - i.radius / 4F + i.radius / 2F, i.y + 20F + i.radius / 4F);
            dpadDown.lineTo(i.x, i.y + 20F);
            dpadDown.close();

            drawDPad(dpadUp, i.dpadStatus == UP || i.dpadStatus == RIGHT_UP || i.dpadStatus == LEFT_UP,  canvas);
            canvas.drawText(i.upKeyName, i.x, i.y - i.radius / 2F, textPaint);

            drawDPad(dpadDown, i.dpadStatus == DOWN || i.dpadStatus == RIGHT_DOWN || i.dpadStatus == LEFT_DOWN, canvas);
            canvas.drawText(i.downKeyName, i.x, i.y + i.radius / 2F + 26F, textPaint);

            drawDPad(dpadLeft, i.dpadStatus == LEFT || i.dpadStatus == LEFT_DOWN || i.dpadStatus == LEFT_UP, canvas);
            canvas.drawText(i.leftKeyName, i.x - i.radius / 2F - 20F, i.y + 16F, textPaint);

            drawDPad(dpadRight, i.dpadStatus == RIGHT || i.dpadStatus == RIGHT_DOWN || i.dpadStatus == RIGHT_UP, canvas);
            canvas.drawText(i.rightKeyName, i.x + i.radius / 2F + 20F, i.y + 16F, textPaint);
        });
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_POINTER_DOWN, MotionEvent.ACTION_DOWN -> {
                buttonList.forEach((i) -> {
                    if (detectClick(event, event.getActionIndex(), i.x, i.y, i.radius, i.shape)) {
                        i.isPressed = true;
                        i.fingerId = event.getPointerId(event.getActionIndex());
                        handleButton(i, true);
                    }
                });
                analogList.forEach((it) -> {
                    if (detectClick(event, event.getActionIndex(), it.x, it.y, it.radius, SHAPE_CIRCLE)) {
                        float posX = event.getX(event.getActionIndex()) - it.x;
                        float posY = event.getY(event.getActionIndex()) - it.y;

                        it.isPressed = true;
                        it.fingerId = event.getPointerId(event.getActionIndex());
                        it.fingerX = posX;
                        it.fingerY = posY;

                        float lx = posX / (it.radius / 4F);
                        float ly = posY / (it.radius / 4F);

                        virtualAxis(lx, ly, it);
                    }
                });
                dpadList.forEach((it) -> {
                    if (detectClick(event, event.getActionIndex(), it.x, it.y, it.radius, SHAPE_DPAD)) {
                        float posX = event.getX(event.getActionIndex()) - it.x;
                        float posY = event.getY(event.getActionIndex()) - it.y;

                        it.isPressed = true;
                        it.fingerId = event.getPointerId(event.getActionIndex());
                        it.fingerX = posX;
                        it.fingerY = posY;
                        it.dpadStatus = getAxisStatus(posX / it.radius, posY / it.radius, 0.25F);

                        float lx = posX / (it.radius / 4F);
                        float ly = posY / (it.radius / 4F);

                        virtualAxis(lx, ly, it);
                    }
                });

                invalidate();
            }
            case MotionEvent.ACTION_MOVE -> {
                for (int i = 0; i < event.getPointerCount(); i++) {
                    boolean isFingerPressingButton = false;

                    for (VirtualButton button : buttonList) {
                        if (button.isPressed && button.fingerId == event.getPointerId(i)) {
                            isFingerPressingButton = true;
                            break;
                        }
                    }

                    for (VirtualAnalog analog : analogList) {
                        if (analog.isPressed && analog.fingerId == event.getPointerId(i)) {
                            float posX = event.getX(i) - analog.x;
                            float posY = event.getY(i) - analog.y;

                            analog.fingerX = posX;
                            analog.fingerY = posY;

                            float lx = posX / (analog.radius / 4F);
                            float ly = posY / (analog.radius / 4F);

                            virtualAxis(lx, ly, analog);

                            isFingerPressingButton = true;
                            break;
                        }
                    }

                    for (VirtualDPad dpad : dpadList) {
                        if (dpad.isPressed && dpad.fingerId == event.getPointerId(i)) {
                            float posX = event.getX(i) - dpad.x;
                            float posY = event.getY(i) - dpad.y;

                            dpad.fingerX = posX;
                            dpad.fingerY = posY;
                            dpad.dpadStatus = getAxisStatus(posX / dpad.radius, posY / dpad.radius, 0.25F);

                            float lx = posX / (dpad.radius / 4F);
                            float ly = posY / (dpad.radius / 4F);

                            virtualAxis(lx, ly, dpad);

                            isFingerPressingButton = true;
                            break;
                        }
                    }

                    if (!isFingerPressingButton && event.getHistorySize() > 0) {
                        float deltaX = event.getX(i) - event.getHistoricalX(i, 0);
                        float deltaY = event.getY(i) - event.getHistoricalY(i, 0);

                        lorieView.sendMouseEvent(deltaX, deltaY, BUTTON_UNDEFINED, false, true);
                    }
                }

                invalidate();
            }
            case MotionEvent.ACTION_POINTER_UP -> {
                for (VirtualButton button : buttonList) {
                    if (button.fingerId == event.getPointerId(event.getActionIndex())) {
                        button.fingerId = -1;
                        handleButton(button, false);
                        break;
                    }
                }
                for (VirtualAnalog analog : analogList) {
                    if (analog.fingerId == event.getPointerId(event.getActionIndex())) {
                        analog.fingerId = -1;
                        analog.fingerX = 0F;
                        analog.fingerY = 0F;
                        analog.isPressed = false;
                        virtualAxis(0F, 0F, analog);
                        break;
                    }
                }
                for (VirtualDPad dpad : dpadList) {
                    if (dpad.fingerId == event.getPointerId(event.getActionIndex())) {
                        dpad.fingerId = -1;
                        dpad.fingerX = 0F;
                        dpad.fingerY = 0F;
                        dpad.isPressed = false;
                        dpad.dpadStatus = 0;
                        virtualAxis(0F, 0F, dpad);
                        break;
                    }
                }

                invalidate();
            }
            case MotionEvent.ACTION_UP -> {
                for (VirtualButton button : buttonList) {
                    if (button.isPressed) {
                        button.fingerId = -1;
                        handleButton(button, false);
                    }
                }
                for (VirtualAnalog analog : analogList) {
                    if (analog.isPressed) {
                        analog.fingerId = -1;
                        analog.fingerX = 0F;
                        analog.fingerY = 0F;
                        analog.isPressed = false;
                        virtualAxis(0F, 0F, analog);
                    }
                }
                for (VirtualDPad dpad : dpadList) {
                    if (dpad.isPressed) {
                        dpad.fingerId = -1;
                        dpad.fingerX = 0F;
                        dpad.fingerY = 0F;
                        dpad.isPressed = false;
                        dpad.dpadStatus = 0;
                        virtualAxis(0F, 0F, dpad);
                    }
                }

                invalidate();
            }
        }

        return true;
    }

    private void virtualAxis(float axisX, float axisY, VirtualDPad dpad) {
        handleAxis(axisX, axisY, new ControllerUtils.Analog(false, dpad.upKeyCodes, dpad.downKeyCodes, dpad.leftKeyCodes, dpad.rightKeyCodes), dpad.deadZone);
    }

    private void virtualAxis(float axisX, float axisY, VirtualAnalog analog) {
        handleAxis(axisX, axisY, new ControllerUtils.Analog(false, analog.upKeyCodes, analog.downKeyCodes, analog.leftKeyCodes, analog.rightKeyCodes), analog.deadZone);
    }

    private void handleButton(VirtualButton button, boolean isPressed) {
        button.isPressed = isPressed;
        handleKey(isPressed, button.buttonMapping);
    }

    public static class VirtualButton {
        public int id;
        public float x;
        public float y;
        public float radius;
        public String keyName;
        public ButtonMapping buttonMapping;
        public int fingerId;
        public boolean isPressed;
        public int shape;

        public VirtualButton(float x, float y, float radius, String keyName, int shape) {
            this.id = buttonList.size() + 1;
            this.x = x;
            this.y = y;
            this.radius = radius;
            this.keyName = keyName;
            this.buttonMapping = new ButtonMapping(keyName);
            this.fingerId = -1;
            this.isPressed = false;
            this.shape = shape;
        }
    }

    public static class VirtualAnalog {
        public int id;
        public float x;
        public float y;
        public float fingerX;
        public float fingerY;
        public float radius;
        public String upKeyName;
        public ButtonMapping upKeyCodes;
        public String downKeyName;
        public ButtonMapping downKeyCodes;
        public String leftKeyName;
        public ButtonMapping leftKeyCodes;
        public String rightKeyName;
        public ButtonMapping rightKeyCodes;
        public boolean isPressed;
        public int fingerId;
        public float deadZone;

        public VirtualAnalog(float x, float y, float radius, String upKeyName, String downKeyName, String leftKeyName, String rightKeyName, float deadZone) {
            this.id = analogList.size() + 1;
            this.x = x;
            this.y = y;
            this.fingerX = 0F;
            this.fingerY = 0F;
            this.fingerId = -1;
            this.radius = radius;
            this.upKeyName = upKeyName;
            this.upKeyCodes = new ButtonMapping(upKeyName);
            this.downKeyName = downKeyName;
            this.downKeyCodes = new ButtonMapping(downKeyName);
            this.leftKeyName = leftKeyName;
            this.leftKeyCodes = new ButtonMapping(leftKeyName);
            this.rightKeyName = rightKeyName;
            this.rightKeyCodes = new ButtonMapping(rightKeyName);
            this.isPressed = false;
            this.deadZone = deadZone;
        }
    }

    public static class VirtualDPad {
        public int id;
        public float x;
        public float y;
        public float fingerX;
        public float fingerY;
        public float radius;
        public String upKeyName;
        public ButtonMapping upKeyCodes;
        public String downKeyName;
        public ButtonMapping downKeyCodes;
        public String leftKeyName;
        public ButtonMapping leftKeyCodes;
        public String rightKeyName;
        public ButtonMapping rightKeyCodes;
        public boolean isPressed;
        public int fingerId;
        public float deadZone;
        public int dpadStatus;

        public VirtualDPad(float x, float y, float radius, String upKeyName, String downKeyName, String leftKeyName, String rightKeyName, float deadZone) {
            this.id = dpadList.size() + 1;
            this.x = x;
            this.y = y;
            this.fingerX = 0F;
            this.fingerY = 0F;
            this.fingerId = -1;
            this.radius = radius;
            this.upKeyName = upKeyName;
            this.upKeyCodes = new ButtonMapping(upKeyName);
            this.downKeyName = downKeyName;
            this.downKeyCodes = new ButtonMapping(downKeyName);
            this.leftKeyName = leftKeyName;
            this.leftKeyCodes = new ButtonMapping(leftKeyName);
            this.rightKeyName = rightKeyName;
            this.rightKeyCodes = new ButtonMapping(rightKeyName);
            this.isPressed = false;
            this.deadZone = deadZone;
        }
    }


    public final static int SHAPE_CIRCLE = 0;
    public final static int SHAPE_SQUARE = 1;
    public final static int SHAPE_RECTANGLE = 2;
    public final static int SHAPE_DPAD = 3;

    public final static ArrayList<VirtualButton> buttonList = new ArrayList<>();
    public final static ArrayList<VirtualAnalog> analogList = new ArrayList<>();
    public final static ArrayList<VirtualDPad> dpadList = new ArrayList<>();

    public static boolean detectClick(MotionEvent event, int index, float x, float y, float radius, int shape) {
        return switch (shape) {
            case SHAPE_RECTANGLE -> (event.getX(index) >= x - radius / 2 && event.getX(index) <= (x + (radius / 2))) &&
                    (event.getY(index) >= y - radius / 4 && event.getY(index) <= (y + (radius / 4)));
            case SHAPE_DPAD -> (event.getX(index) >= x - radius - 20 && event.getX(index) <= (x + (radius - 20))) &&
                    (event.getY(index) >= y - radius - 20 && event.getY(index) <= (y + (radius - 20)));
            default -> (event.getX(index) >= x - radius / 2 && event.getX(index) <= (x + (radius / 2))) &&
                    (event.getY(index) >= y - radius / 2 && event.getY(index) <= (y + (radius / 2)));
        };
    }
}