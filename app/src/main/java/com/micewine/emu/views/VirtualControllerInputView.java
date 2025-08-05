package com.micewine.emu.views;

import static com.micewine.emu.activities.MainActivity.getNativeResolution;
import static com.micewine.emu.controller.ControllerUtils.DOWN;
import static com.micewine.emu.controller.ControllerUtils.LEFT;
import static com.micewine.emu.controller.ControllerUtils.LEFT_DOWN;
import static com.micewine.emu.controller.ControllerUtils.LEFT_UP;
import static com.micewine.emu.controller.ControllerUtils.RIGHT;
import static com.micewine.emu.controller.ControllerUtils.RIGHT_DOWN;
import static com.micewine.emu.controller.ControllerUtils.RIGHT_UP;
import static com.micewine.emu.controller.ControllerUtils.UP;
import static com.micewine.emu.controller.ControllerUtils.connectedVirtualControllers;
import static com.micewine.emu.controller.ControllerUtils.getAxisStatus;
import static com.micewine.emu.input.InputStub.BUTTON_UNDEFINED;
import static com.micewine.emu.views.VirtualKeyboardInputCreatorView.GRID_SIZE;
import static com.micewine.emu.views.VirtualKeyboardInputView.SHAPE_CIRCLE;
import static com.micewine.emu.views.VirtualKeyboardInputView.SHAPE_DPAD;
import static com.micewine.emu.views.VirtualKeyboardInputView.SHAPE_RECTANGLE;
import static com.micewine.emu.views.VirtualKeyboardInputView.SHAPE_SQUARE;
import static com.micewine.emu.views.VirtualKeyboardInputView.detectClick;

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

import java.util.ArrayList;

public class VirtualControllerInputView extends View {
    public VirtualControllerInputView(Context context) {
        super(context);
        init();
    }

    public VirtualControllerInputView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public VirtualControllerInputView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private Paint paint;
    private Paint textPaint;
    private final LorieView lorieView = new LorieView(getContext());
    private final Path dpadUp = new Path();
    private final Path dpadDown = new Path();
    private final Path dpadLeft = new Path();
    private final Path dpadRight = new Path();
    private final Path startButton = new Path();
    private final Path selectButton = new Path();
    private final ArrayList<VirtualControllerButton> buttonList = new ArrayList<>();
    private VirtualXInputDPad dpad;
    private VirtualXInputAnalog leftAnalog;
    private VirtualXInputTouchPad rightTouchPad;

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

        addButton(A_BUTTON, 2065F, 910F, 180F, SHAPE_CIRCLE);
        addButton(B_BUTTON, 2205F, 735F, 180F, SHAPE_CIRCLE);
        addButton(X_BUTTON, 1925F, 735F, 180F, SHAPE_CIRCLE);
        addButton(Y_BUTTON, 2065F, 560F, 180F, SHAPE_CIRCLE);
        addButton(START_BUTTON, 1330F, 980F, 130F, SHAPE_CIRCLE);
        addButton(SELECT_BUTTON, 1120F, 980F, 130F, SHAPE_CIRCLE);
        addButton(LB_BUTTON, 280F, 300F, 260F, SHAPE_RECTANGLE);
        addButton(LT_BUTTON, 280F, 140F, 260F, SHAPE_RECTANGLE);
        addButton(RB_BUTTON, 2065F, 300F, 260F, SHAPE_RECTANGLE);
        addButton(RT_BUTTON, 2065F, 140F, 260F, SHAPE_RECTANGLE);
        addButton(LS_BUTTON, 880F, 980F, 180F, SHAPE_CIRCLE);
        addButton(RS_BUTTON, 1560F, 980F, 180F, SHAPE_CIRCLE);

        leftAnalog = new VirtualXInputAnalog(LEFT_ANALOG, 280F, 840F, 275F);
        dpad = new VirtualXInputDPad(0, 640F, 480F, 250F);
        rightTouchPad = new VirtualXInputTouchPad(0, 1750F, 480F, 275F);

        adjustButtons();
    }

    private void adjustButtons() {
        String nativeResolution = getNativeResolution(getContext());
        String baseResolution = "2400x1080"; // My Device Resolution

        if (!nativeResolution.equals(baseResolution)) {
            String[] nativeResolutionSplit = nativeResolution.split("x");
            String[] baseResolutionSplit = baseResolution.split("x");

            float nativeResolutionWidth = Float.parseFloat(nativeResolutionSplit[0]);
            float nativeResolutionHeight = Float.parseFloat(nativeResolutionSplit[1]);

            float baseResolutionWidth = Float.parseFloat(baseResolutionSplit[0]);
            float baseResolutionHeight = Float.parseFloat(baseResolutionSplit[1]);

            float multiplierWidth = (nativeResolutionWidth / baseResolutionWidth) * 100F;
            float multiplierHeight = (nativeResolutionHeight / baseResolutionHeight) * 100F;

            buttonList.forEach((i) -> {
                i.x = Math.round(i.x / 100F * multiplierWidth / GRID_SIZE) * (float) GRID_SIZE;
                i.y = Math.round(i.y / 100F * multiplierHeight / GRID_SIZE) * (float) GRID_SIZE;
            });

            leftAnalog.x = Math.round(leftAnalog.x / 100F * multiplierWidth / GRID_SIZE) * (float) GRID_SIZE;
            leftAnalog.y = Math.round(leftAnalog.y / 100F * multiplierHeight / GRID_SIZE) * (float) GRID_SIZE;

            rightTouchPad.x = Math.round(rightTouchPad.x / 100F * multiplierWidth / GRID_SIZE) * (float) GRID_SIZE;
            rightTouchPad.y = Math.round(rightTouchPad.y / 100F * multiplierHeight / GRID_SIZE) * (float) GRID_SIZE;

            dpad.x = Math.round(dpad.x / 100F * multiplierWidth / GRID_SIZE) * (float) GRID_SIZE;
            dpad.y = Math.round(dpad.y / 100F * multiplierHeight / GRID_SIZE) * (float) GRID_SIZE;
        }
    }

    private void addButton(int id, float x, float y, float radius, int shape) {
        buttonList.add(
                new VirtualControllerButton(id, x, y, radius, shape)
        );
    }

    private String getButtonName(int id) {
        return switch (id) {
            case A_BUTTON -> "A";
            case B_BUTTON -> "B";
            case X_BUTTON -> "X";
            case Y_BUTTON -> "Y";
            case RB_BUTTON -> "RB";
            case LB_BUTTON -> "LB";
            case RT_BUTTON -> "RT";
            case LT_BUTTON -> "LT";
            case RS_BUTTON -> "RS";
            case LS_BUTTON -> "LS";
            default -> "";
        };
    }

    private void drawDPad(Path path, boolean isPressed, Canvas canvas) {
        paint.setStyle(isPressed ? Paint.Style.FILL_AND_STROKE : Paint.Style.STROKE);
        paint.setAlpha(200);

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

            paint.setTextSize(i.radius / 4F);

            float offset = (textPaint.getFontMetrics().ascent + textPaint.getFontMetrics().descent) / 2F;

            switch (i.shape) {
                case SHAPE_CIRCLE -> canvas.drawCircle(i.x, i.y, i.radius / 2F, paint);
                case SHAPE_RECTANGLE -> canvas.drawRoundRect(i.x - i.radius / 2F,
                        i.y - i.radius / 4F,
                        i.x + i.radius / 2F,
                        i.y + i.radius / 4F,
                        32F,
                        32F,
                        paint
                );
            }

            switch (i.id) {
                case START_BUTTON -> {
                    paint.setStrokeWidth(12F);

                    startButton.reset();
                    startButton.moveTo(i.x - i.radius / 3, i.y - i.radius / 8);
                    startButton.lineTo(i.x - i.radius / 3 + i.radius - i.radius / 3, i.y - i.radius / 8);
                    startButton.moveTo(i.x - i.radius / 3, i.y);
                    startButton.lineTo(i.x - i.radius / 3 + i.radius - i.radius / 3, i.y);
                    startButton.moveTo(i.x - i.radius / 3, i.y + i.radius / 8);
                    startButton.lineTo(i.x - i.radius / 3 + i.radius - i.radius / 3, i.y + i.radius / 8);
                    startButton.close();

                    paint.setColor(i.isPressed ? Color.BLACK : Color.WHITE);
                    paint.setAlpha(200);

                    canvas.drawPath(startButton, paint);
                }
                case SELECT_BUTTON -> {
                    paint.setStrokeWidth(12F);

                    selectButton.reset();
                    selectButton.moveTo(i.x - i.radius / 4F + 4F, i.y - i.radius / 4 + 40F);
                    selectButton.lineTo(i.x - i.radius / 4F + 4F, i.y - i.radius / 4);
                    selectButton.lineTo(i.x - i.radius / 4F + 4F + 40F, i.y - i.radius / 4);
                    selectButton.lineTo(i.x - i.radius / 4F + 4F + 40F, i.y - i.radius / 4 + 20F);
                    selectButton.lineTo(i.x - i.radius / 4F + 4F + 40F, i.y - i.radius / 4);
                    selectButton.lineTo(i.x - i.radius / 4F + 4F, i.y - i.radius / 4);
                    selectButton.close();
                    selectButton.moveTo(i.x - i.radius / 4F + 20F, i.y - i.radius / 4 + 30F);
                    selectButton.lineTo(i.x - i.radius / 4F + 60F, i.y - i.radius / 4 + 30F);
                    selectButton.lineTo(i.x - i.radius / 4F + 60F, i.y - i.radius / 4 + 70F);
                    selectButton.lineTo(i.x - i.radius / 4F + 20F, i.y - i.radius / 4 + 70F);
                    selectButton.lineTo(i.x - i.radius / 4F + 20F, i.y - i.radius / 4 + 24F);
                    selectButton.lineTo(i.x - i.radius / 4F + 20F, i.y - i.radius / 4 + 70F);
                    selectButton.lineTo(i.x - i.radius / 4F + 60F, i.y - i.radius / 4 + 70F);
                    selectButton.lineTo(i.x - i.radius / 4F + 60F, i.y - i.radius / 4 + 30F);
                    selectButton.close();

                    paint.setColor(i.isPressed ? Color.BLACK : Color.WHITE);
                    paint.setAlpha(200);

                    canvas.drawPath(selectButton, paint);
                }
                default -> canvas.drawText(getButtonName(i.id), i.x, i.y - offset - 4, textPaint);
            }
        });

        // Left Analog
        float analogX = leftAnalog.x + leftAnalog.fingerX;
        float analogY = leftAnalog.y + leftAnalog.fingerY;

        float distSquared = (leftAnalog.fingerX * leftAnalog.fingerX) + (leftAnalog.fingerY * leftAnalog.fingerY);
        float maxDist = (leftAnalog.radius / 4F) * (leftAnalog.radius / 4F);

        if (distSquared > maxDist) {
            float dist = (float) Math.sqrt(distSquared);
            float scale = (leftAnalog.radius / 4F) / dist;
            analogX = leftAnalog.x + (leftAnalog.fingerX * scale);
            analogY = leftAnalog.y + (leftAnalog.fingerY * scale);
        }

        paint.setColor(Color.WHITE);
        paint.setAlpha(200);

        paint.setStyle(Paint.Style.STROKE);
        canvas.drawCircle(leftAnalog.x, leftAnalog.y, leftAnalog.radius / 2F, paint);

        paint.setStyle(Paint.Style.FILL);
        canvas.drawCircle(analogX, analogY, leftAnalog.radius / 4F, paint);

        paint.setColor(Color.WHITE);
        paint.setAlpha(200);
        paint.setStyle(Paint.Style.STROKE);

        canvas.drawCircle(leftAnalog.x, leftAnalog.y, leftAnalog.radius / 2F, paint);

        paint.setStyle(Paint.Style.FILL);

        canvas.drawCircle(analogX, analogY, leftAnalog.radius / 4F, paint);

        // Right Analog TouchPad
        paint.setStyle(Paint.Style.STROKE);
        canvas.drawRoundRect(rightTouchPad.x - rightTouchPad.radius / 2,
                rightTouchPad.y - rightTouchPad.radius / 2,
                rightTouchPad.x + rightTouchPad.radius / 2,
                rightTouchPad.y + rightTouchPad.radius / 2,
                32F,
                32F,
                paint
        );

        // D-Pad Left
        dpadLeft.reset();
        dpadLeft.moveTo(dpad.x - 20F, dpad.y);
        dpadLeft.lineTo(dpad.x - 20F - dpad.radius / 4F, dpad.y - dpad.radius / 4F);
        dpadLeft.lineTo(dpad.x - 20F - dpad.radius / 4F - dpad.radius / 2F, dpad.y - dpad.radius / 4F);
        dpadLeft.lineTo(
                dpad.x - 20F - dpad.radius / 4F - dpad.radius / 2F,
                dpad.y - dpad.radius / 4F + dpad.radius / 2F
        );
        dpadLeft.lineTo(dpad.x - 20F - dpad.radius / 4F, dpad.y - dpad.radius / 4F + dpad.radius / 2F);
        dpadLeft.lineTo(dpad.x - 20F, dpad.y);
        dpadLeft.close();

        // D-Pad Up
        dpadUp.reset();
        dpadUp.moveTo(dpad.x, dpad.y - 20F);
        dpadUp.lineTo(dpad.x - dpad.radius / 4F, dpad.y - 20F - dpad.radius / 4F);
        dpadUp.lineTo(dpad.x - dpad.radius / 4F, dpad.y - 20F - dpad.radius / 4F - dpad.radius / 2F);
        dpadUp.lineTo(
                dpad.x - dpad.radius / 4F + dpad.radius / 2F,
                dpad.y - 20F - dpad.radius / 4F - dpad.radius / 2F
        );
        dpadUp.lineTo(dpad.x - dpad.radius / 4 + dpad.radius / 2F, dpad.y - 20F - dpad.radius / 4F);
        dpadUp.lineTo(dpad.x, dpad.y - 20F);
        dpadUp.close();

        // D-Pad Right
        dpadRight.reset();
        dpadRight.moveTo(dpad.x + 20F, dpad.y);
        dpadRight.lineTo(dpad.x + 20F + dpad.radius / 4F, dpad.y - dpad.radius / 4F);
        dpadRight.lineTo(dpad.x + 20F + dpad.radius / 4F + dpad.radius / 2F, dpad.y - dpad.radius / 4F);
        dpadRight.lineTo(
                dpad.x + 20 + dpad.radius / 4 + dpad.radius / 2,
                dpad.y - dpad.radius / 4 + dpad.radius / 2
        );
        dpadRight.lineTo(dpad.x + 20F + dpad.radius / 4F, dpad.y - dpad.radius / 4F + dpad.radius / 2F);
        dpadRight.lineTo(dpad.x + 20F, dpad.y);
        dpadRight.close();

        // D-Pad Down
        dpadDown.reset();
        dpadDown.moveTo(dpad.x, dpad.y + 20F);
        dpadDown.lineTo(dpad.x - dpad.radius / 4F, dpad.y + 20F + dpad.radius / 4F);
        dpadDown.lineTo(dpad.x - dpad.radius / 4F, dpad.y + 20F + dpad.radius / 4F + dpad.radius / 2F);
        dpadDown.lineTo(
                dpad.x - dpad.radius / 4F + dpad.radius / 2F,
                dpad.y + 20F + dpad.radius / 4F + dpad.radius / 2F
        );
        dpadDown.lineTo(dpad.x - dpad.radius / 4F + dpad.radius / 2F, dpad.y + 20F + dpad.radius / 4F);
        dpadDown.lineTo(dpad.x, dpad.y + 20F);
        dpadDown.close();

        drawDPad(dpadUp, dpad.dpadStatus == UP || dpad.dpadStatus == RIGHT_UP || dpad.dpadStatus == LEFT_UP,  canvas);
        drawDPad(dpadDown, dpad.dpadStatus == DOWN || dpad.dpadStatus == RIGHT_DOWN || dpad.dpadStatus == LEFT_DOWN, canvas);
        drawDPad(dpadLeft, dpad.dpadStatus == LEFT || dpad.dpadStatus == LEFT_DOWN || dpad.dpadStatus == LEFT_UP, canvas);
        drawDPad(dpadRight, dpad.dpadStatus == RIGHT || dpad.dpadStatus == RIGHT_DOWN || dpad.dpadStatus == RIGHT_UP, canvas);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (virtualXInputControllerId == -1) return true;

        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_POINTER_DOWN, MotionEvent.ACTION_DOWN -> {
                for (VirtualControllerButton i : buttonList) {
                    if (detectClick(event, event.getActionIndex(), i.x, i.y, i.radius, i.shape)) {
                        i.fingerId = event.getPointerId(event.getActionIndex());
                        i.isPressed = true;
                        handleButton(i, true);
                        break;
                    }
                }

                if (detectClick(event, event.getActionIndex(), leftAnalog.x, leftAnalog.y, leftAnalog.radius, SHAPE_CIRCLE)) {
                    float posX = event.getX(event.getActionIndex()) - leftAnalog.x;
                    float posY = event.getY(event.getActionIndex()) - leftAnalog.y;

                    leftAnalog.fingerId = event.getPointerId(event.getActionIndex());
                    leftAnalog.fingerX = posX;
                    leftAnalog.fingerY = posY;
                    leftAnalog.isPressed = true;

                    float lx = (posX / (leftAnalog.radius / 4));
                    float ly = (posY / (leftAnalog.radius / 4));

                    connectedVirtualControllers[virtualXInputControllerId].state.lx = Math.max(-1F, Math.min(1F, lx));
                    connectedVirtualControllers[virtualXInputControllerId].state.ly = Math.max(-1F, Math.min(1F, ly));
                }

                if (detectClick(event, event.getActionIndex(), rightTouchPad.x, rightTouchPad.y, rightTouchPad.radius, SHAPE_SQUARE)) {
                    rightTouchPad.fingerId = event.getPointerId(event.getActionIndex());
                    rightTouchPad.isPressed = true;
                }

                if (detectClick(event, event.getActionIndex(), dpad.x, dpad.y, dpad.radius, SHAPE_DPAD)) {
                    float posX = event.getX(event.getActionIndex()) - dpad.x;
                    float posY = event.getY(event.getActionIndex()) - dpad.y;

                    dpad.fingerId = event.getPointerId(event.getActionIndex());
                    dpad.fingerX = posX;
                    dpad.fingerY = posY;
                    dpad.isPressed = true;
                    dpad.dpadStatus = getAxisStatus(posX / dpad.radius, posY / dpad.radius, 0.25F);

                    connectedVirtualControllers[virtualXInputControllerId].state.dpadX = Math.max(-1F, Math.min(1F, posX / dpad.radius));
                    connectedVirtualControllers[virtualXInputControllerId].state.dpadY = Math.max(-1F, Math.min(1F, posY / dpad.radius));
                }

                invalidate();
            }
            case MotionEvent.ACTION_MOVE -> {
                for (int i = 0; i < event.getPointerCount(); i++) {
                    boolean isFingerPressingButton = false;

                    for (VirtualControllerButton v : buttonList) {
                        if (v.fingerId == event.getPointerId(i)) {
                            isFingerPressingButton = true;
                            break;
                        }
                    }

                    if (leftAnalog.isPressed && leftAnalog.fingerId == event.getPointerId(i)) {
                        float posX = event.getX(i) - leftAnalog.x;
                        float posY = event.getY(i) - leftAnalog.y;

                        leftAnalog.fingerX = posX;
                        leftAnalog.fingerY = posY;

                        float lx = (posX / (leftAnalog.radius / 4));
                        float ly = (posY / (leftAnalog.radius / 4));

                        connectedVirtualControllers[virtualXInputControllerId].state.lx = Math.max(-1F, Math.min(1F, lx));
                        connectedVirtualControllers[virtualXInputControllerId].state.ly = Math.max(-1F, Math.min(1F, ly));

                        isFingerPressingButton = true;
                    }

                    if (rightTouchPad.isPressed && rightTouchPad.fingerId == event.getPointerId(i)) {
                        if (event.getHistorySize() > 0) {
                            float dx = (event.getX(i) - event.getHistoricalX(i, event.getHistorySize() - 1));
                            float dy = (event.getY(i) - event.getHistoricalY(i, event.getHistorySize() - 1));

                            isFingerPressingButton = true;

                            connectedVirtualControllers[virtualXInputControllerId].state.rx = Math.max(-1F, Math.min(1F, dx));
                            connectedVirtualControllers[virtualXInputControllerId].state.ry = Math.max(-1F, Math.min(1F, dy));
                        }
                    }

                    if (dpad.isPressed && dpad.fingerId == event.getPointerId(i)) {
                        float posX = event.getX(i) - dpad.x;
                        float posY = event.getY(i) - dpad.y;

                        dpad.fingerX = posX;
                        dpad.fingerY = posY;
                        dpad.dpadStatus = getAxisStatus(posX / dpad.radius, posY / dpad.radius, 0.25F);

                        connectedVirtualControllers[virtualXInputControllerId].state.dpadX = Math.max(-1F, Math.min(1F, posX / dpad.radius));
                        connectedVirtualControllers[virtualXInputControllerId].state.dpadY = Math.max(-1F, Math.min(1F, posY / dpad.radius));

                        isFingerPressingButton = true;
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
                for (VirtualControllerButton i : buttonList) {
                    if (i.fingerId == event.getPointerId(event.getActionIndex())) {
                        i.fingerId = -1;
                        handleButton(i, false);
                    }
                }

                if (leftAnalog.fingerId == event.getPointerId(event.getActionIndex())) {
                    leftAnalog.fingerId = -1;
                    leftAnalog.fingerX = 0F;
                    leftAnalog.fingerY = 0F;
                    leftAnalog.isPressed = false;

                    connectedVirtualControllers[virtualXInputControllerId].state.lx = 0F;
                    connectedVirtualControllers[virtualXInputControllerId].state.ly = 0F;
                }

                if (rightTouchPad.fingerId == event.getPointerId(event.getActionIndex())) {
                    rightTouchPad.fingerId = -1;
                    rightTouchPad.isPressed = false;

                    connectedVirtualControllers[virtualXInputControllerId].state.rx = 0F;
                    connectedVirtualControllers[virtualXInputControllerId].state.ry = 0F;
                }

                if (dpad.fingerId == event.getPointerId(event.getActionIndex())) {
                    dpad.fingerId = -1;
                    dpad.fingerX = 0F;
                    dpad.fingerY = 0F;
                    dpad.isPressed = false;
                    dpad.dpadStatus = 0;

                    connectedVirtualControllers[virtualXInputControllerId].state.dpadX = 0F;
                    connectedVirtualControllers[virtualXInputControllerId].state.dpadY = 0F;
                }

                invalidate();
            }
            case MotionEvent.ACTION_UP -> {
                for (VirtualControllerButton i : buttonList) {
                    if (i.isPressed) {
                        i.fingerId = -1;
                        handleButton(i, false);
                    }
                }

                // Left Analog
                leftAnalog.fingerId = -1;
                leftAnalog.fingerX = 0F;
                leftAnalog.fingerY = 0F;
                leftAnalog.isPressed = false;

                connectedVirtualControllers[virtualXInputControllerId].state.lx = 0F;
                connectedVirtualControllers[virtualXInputControllerId].state.ly = 0F;

                // Right Analog TouchPad
                rightTouchPad.fingerId = -1;
                rightTouchPad.isPressed = false;

                connectedVirtualControllers[virtualXInputControllerId].state.rx = 0F;
                connectedVirtualControllers[virtualXInputControllerId].state.ry = 0F;

                // D-Pad
                dpad.fingerId = -1;
                dpad.fingerX = 0F;
                dpad.fingerY = 0F;
                dpad.isPressed = false;
                dpad.dpadStatus = 0;

                connectedVirtualControllers[virtualXInputControllerId].state.dpadX = 0F;
                connectedVirtualControllers[virtualXInputControllerId].state.dpadY = 0F;

                invalidate();
            }
        }

        return true;
    }

    private void handleButton(VirtualControllerButton button, boolean isPressed) {
        button.isPressed = isPressed;

        switch (button.id) {
            case A_BUTTON -> connectedVirtualControllers[virtualXInputControllerId].state.aPressed = isPressed;
            case B_BUTTON -> connectedVirtualControllers[virtualXInputControllerId].state.bPressed = isPressed;
            case X_BUTTON -> connectedVirtualControllers[virtualXInputControllerId].state.xPressed = isPressed;
            case Y_BUTTON -> connectedVirtualControllers[virtualXInputControllerId].state.yPressed = isPressed;
            case START_BUTTON -> connectedVirtualControllers[virtualXInputControllerId].state.startPressed = isPressed;
            case SELECT_BUTTON -> connectedVirtualControllers[virtualXInputControllerId].state.selectPressed = isPressed;
            case LB_BUTTON -> connectedVirtualControllers[virtualXInputControllerId].state.lbPressed = isPressed;
            case LT_BUTTON -> connectedVirtualControllers[virtualXInputControllerId].state.lt = isPressed ? 1F : 0F;
            case RB_BUTTON -> connectedVirtualControllers[virtualXInputControllerId].state.rbPressed = isPressed;
            case RT_BUTTON -> connectedVirtualControllers[virtualXInputControllerId].state.rt = isPressed ? 1F : 0F;
            case LS_BUTTON -> connectedVirtualControllers[virtualXInputControllerId].state.lsPressed = isPressed;
            case RS_BUTTON -> connectedVirtualControllers[virtualXInputControllerId].state.rsPressed = isPressed;
        }
    }

    public static class VirtualControllerButton {
        public int id;
        public float x;
        public float y;
        public float radius;
        public int shape;
        public int fingerId = -1;
        public boolean isPressed = false;

        public VirtualControllerButton(int id, float x, float y, float radius, int shape) {
            this.id = id;
            this.x = x;
            this.y = y;
            this.radius = radius;
            this.shape = shape;
        }
    }

    public static class VirtualXInputDPad {
        public int id;
        public float x;
        public float y;
        public float radius;
        public int shape;
        public int fingerId = -1;
        public boolean isPressed = false;
        public float fingerX = 0F;
        public float fingerY = 0F;
        public int dpadStatus = 0;

        public VirtualXInputDPad(int id, float x, float y, float radius) {
            this.id = id;
            this.x = x;
            this.y = y;
            this.radius = radius;
        }
    }

    public static class VirtualXInputAnalog {
        public int id;
        public float x;
        public float y;
        public float radius;
        public int shape;
        public int fingerId = -1;
        public boolean isPressed = false;
        public float fingerX = 0F;
        public float fingerY = 0F;

        public VirtualXInputAnalog(int id, float x, float y, float radius) {
            this.id = id;
            this.x = x;
            this.y = y;
            this.radius = radius;
        }
    }

    public static class VirtualXInputTouchPad {
        public int id;
        public float x;
        public float y;
        public float radius;
        public int shape;
        public int fingerId = -1;
        public boolean isPressed = false;

        public VirtualXInputTouchPad(int id, float x, float y, float radius) {
            this.id = id;
            this.x = x;
            this.y = y;
            this.radius = radius;
        }
    }

    private final static int A_BUTTON = 1;
    private final static int B_BUTTON = 2;
    private final static int X_BUTTON = 3;
    private final static int Y_BUTTON = 4;
    private final static int START_BUTTON = 5;
    private final static int SELECT_BUTTON = 6;
    private final static int LB_BUTTON = 7;
    private final static int LT_BUTTON = 8;
    private final static int RB_BUTTON = 9;
    private final static int RT_BUTTON = 10;
    private final static int LEFT_ANALOG = 11;
    private final static int LS_BUTTON = 12;
    private final static int RS_BUTTON = 13;

    public static int virtualXInputControllerId = -1;
}