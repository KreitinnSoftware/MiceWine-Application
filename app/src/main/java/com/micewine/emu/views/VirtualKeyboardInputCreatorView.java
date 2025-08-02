package com.micewine.emu.views;

import static com.micewine.emu.activities.MainActivity.getNativeResolution;
import static com.micewine.emu.activities.VirtualControllerOverlayMapper.ACTION_EDIT_VIRTUAL_BUTTON;
import static com.micewine.emu.adapters.AdapterPreset.clickedPresetName;
import static com.micewine.emu.fragments.EditVirtualButtonFragment.selectedAnalogDownKeyName;
import static com.micewine.emu.fragments.EditVirtualButtonFragment.selectedAnalogLeftKeyName;
import static com.micewine.emu.fragments.EditVirtualButtonFragment.selectedAnalogRightKeyName;
import static com.micewine.emu.fragments.EditVirtualButtonFragment.selectedAnalogUpKeyName;
import static com.micewine.emu.fragments.EditVirtualButtonFragment.selectedButtonKeyName;
import static com.micewine.emu.fragments.EditVirtualButtonFragment.selectedButtonRadius;
import static com.micewine.emu.fragments.EditVirtualButtonFragment.selectedButtonShape;
import static com.micewine.emu.fragments.VirtualControllerPresetManagerFragment.*;
import static com.micewine.emu.fragments.VirtualControllerPresetManagerFragment.getVirtualControllerPreset;
import static com.micewine.emu.views.VirtualKeyboardInputView.SHAPE_CIRCLE;
import static com.micewine.emu.views.VirtualKeyboardInputView.SHAPE_DPAD;
import static com.micewine.emu.views.VirtualKeyboardInputView.SHAPE_RECTANGLE;
import static com.micewine.emu.views.VirtualKeyboardInputView.SHAPE_SQUARE;
import static com.micewine.emu.views.VirtualKeyboardInputView.analogList;
import static com.micewine.emu.views.VirtualKeyboardInputView.buttonList;
import static com.micewine.emu.views.VirtualKeyboardInputView.detectClick;
import static com.micewine.emu.views.VirtualKeyboardInputView.dpadList;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.drawable.VectorDrawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.micewine.emu.R;
import com.micewine.emu.views.VirtualKeyboardInputView.VirtualAnalog;
import com.micewine.emu.views.VirtualKeyboardInputView.VirtualButton;
import com.micewine.emu.views.VirtualKeyboardInputView.VirtualDPad;

public class VirtualKeyboardInputCreatorView extends View {
    public VirtualKeyboardInputCreatorView(Context context) {
        super(context);
        init();
    }

    public VirtualKeyboardInputCreatorView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public VirtualKeyboardInputCreatorView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private Paint paint;
    private Paint textPaint;
    private Paint gridPaint;
    private CircleButton editButton;
    private CircleButton removeButton;
    private final Bitmap editIcon = getBitmapFromVectorDrawable(R.drawable.ic_edit, 75, 75);
    private final Bitmap removeIcon = getBitmapFromVectorDrawable(R.drawable.ic_delete, 75, 75);
    private final Path dpadUp = new Path();
    private final Path dpadDown = new Path();
    private final Path dpadLeft = new Path();
    private final Path dpadRight = new Path();
    private int selectedButton = 0;
    private int selectedAxis = 0;
    private int selectedDPad = 0;

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

        gridPaint = new Paint();
        gridPaint.setColor(Color.BLACK);
        gridPaint.setStrokeWidth(10F);
        gridPaint.setAntiAlias(true);
        gridPaint.setAlpha(200);

        editButton = new CircleButton();
        editButton.radius = 150F;

        removeButton = new CircleButton();
        removeButton.radius = 150F;

        VirtualControllerPreset preset = getVirtualControllerPreset(clickedPresetName);

        if (preset == null) return;

        buttonList.clear();
        analogList.clear();
        dpadList.clear();

        buttonList.addAll(preset.buttons);
        analogList.addAll(preset.analogs);
        dpadList.addAll(preset.dpads);

        reorderButtonsAnalogsIDs();
    }

    private Bitmap getBitmapFromVectorDrawable(int drawableId, int width, int height) {
        VectorDrawable drawable = (VectorDrawable) ContextCompat.getDrawable(getContext(), drawableId);
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        return bitmap;
    }

    public void saveOnPreferences() {
        putVirtualControllerPreset(clickedPresetName, getNativeResolution(getContext()), buttonList, analogList, dpadList);
    }

    private void reorderButtonsAnalogsIDs() {
        for (int i = 0; i < buttonList.size(); i++) {
            buttonList.get(i).id = i + 1;
        }
        for (int i = 0; i < analogList.size(); i++) {
            analogList.get(i).id = i + 1;
        }
        for (int i = 0; i < dpadList.size(); i++) {
            dpadList.get(i).id = i + 1;
        }
    }

    public void addButton(VirtualButton button) {
        buttonList.add(button);
        invalidate();
    }

    public void addAnalog(VirtualAnalog analog) {
        analogList.add(analog);
        invalidate();
    }

    public void addDPad(VirtualDPad dpad) {
        dpadList.add(dpad);
        invalidate();
    }

    private void drawDPad(Path path, boolean isPressed, Canvas canvas) {
        if (isPressed) {
            paint.setStyle(Paint.Style.FILL_AND_STROKE);
            textPaint.setColor(Color.BLACK);
        } else {
            paint.setStyle(Paint.Style.STROKE);
            textPaint.setColor(Color.WHITE);
        }
        paint.setColor(Color.WHITE);
        paint.setAlpha(200);
        textPaint.setAlpha(200);

        canvas.drawPath(path, paint);
    }

    private void adjustTextSize(String text, float maxWidth, Paint paint, VirtualDPad dpad) {
        paint.setTextSize(dpad.radius / 6F);
        while (paint.measureText(text) > maxWidth - (maxWidth / 5F)) {
            paint.setTextSize(paint.getTextSize() - 1F);
        }
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);

        for (int i = 0; i < getWidth(); i++) {
            if (i % GRID_SIZE == 0) {
                canvas.drawLine(i, 0F, i, getHeight(), gridPaint);
            }
        }
        for (int i = 0; i < getHeight(); i++) {
            if (i % GRID_SIZE == 0) {
                canvas.drawLine(0F, i, getWidth(), i, gridPaint);
            }
        }

        buttonList.forEach((i) -> {
            if (lastSelectedButton == i.id && lastSelectedType == BUTTON) {
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
            paint.setStyle(lastSelectedButton == i.id && lastSelectedType == ANALOG ? Paint.Style.FILL_AND_STROKE : Paint.Style.STROKE);
            paint.setColor(Color.WHITE);
            paint.setAlpha(200);
            canvas.drawCircle(i.x, i.y, i.radius / 2F, paint);

            paint.setStyle(Paint.Style.FILL);
            paint.setColor(lastSelectedButton == i.id && lastSelectedType == ANALOG ? Color.BLACK : Color.WHITE);
            canvas.drawCircle(i.x, i.y, i.radius / 4F, paint);
        });
        dpadList.forEach((i) -> {
            dpadLeft.reset();
            dpadLeft.moveTo(i.x - 20F, i.y);
            dpadLeft.lineTo(i.x - 20F - i.radius / 4F, i.y - i.radius / 4F);
            dpadLeft.lineTo(i.x - 20F - i.radius / 4F - i.radius / 2F, i.y - i.radius / 4F);
            dpadLeft.lineTo(i.x - 20F - i.radius / 4F - i.radius / 2F, i.y - i.radius / 4F + i.radius / 2F);
            dpadLeft.lineTo(i.x - 20F - i.radius / 4F, i.y - i.radius / 4F + i.radius / 2F);
            dpadLeft.lineTo(i.x - 20F, i.y);
            dpadLeft.close();

            dpadUp.reset();
            dpadUp.moveTo(i.x, i.y - 20F);
            dpadUp.lineTo(i.x - i.radius / 4F, i.y - 20F - i.radius / 4F);
            dpadUp.lineTo(i.x - i.radius / 4F, i.y - 20F - i.radius / 4F - i.radius / 2F);
            dpadUp.lineTo(i.x - i.radius / 4F + i.radius / 2F, i.y - 20F - i.radius / 4F - i.radius / 2F);
            dpadUp.lineTo(i.x - i.radius / 4F + i.radius / 2F, i.y - 20F - i.radius / 4F);
            dpadUp.lineTo(i.x, i.y - 20F);
            dpadUp.close();

            dpadRight.reset();
            dpadRight.moveTo(i.x + 20F, i.y);
            dpadRight.lineTo(i.x + 20F + i.radius / 4F, i.y - i.radius / 4F);
            dpadRight.lineTo(i.x + 20F + i.radius / 4F + i.radius / 2F, i.y - i.radius / 4F);
            dpadRight.lineTo(i.x + 20F + i.radius / 4F + i.radius / 2F, i.y - i.radius / 4F + i.radius / 2F);
            dpadRight.lineTo(i.x + 20F + i.radius / 4F, i.y - i.radius / 4F + i.radius / 2F);
            dpadRight.lineTo(i.x + 20F, i.y);
            dpadRight.close();

            dpadDown.reset();
            dpadDown.moveTo(i.x, i.y + 20F);
            dpadDown.lineTo(i.x - i.radius / 4F, i.y + 20F + i.radius / 4F);
            dpadDown.lineTo(i.x - i.radius / 4F, i.y + 20F + i.radius / 4F + i.radius / 2F);
            dpadDown.lineTo(i.x - i.radius / 4F + i.radius / 2F, i.y + 20F + i.radius / 4F + i.radius / 2F);
            dpadDown.lineTo(i.x - i.radius / 4F + i.radius / 2F, i.y + 20F + i.radius / 4F);
            dpadDown.lineTo(i.x, i.y + 20F);
            dpadDown.close();

            boolean isPressed = (lastSelectedButton == i.id && lastSelectedType == DPAD);
            float offset = (textPaint.getFontMetrics().ascent + textPaint.getFontMetrics().descent) / 2F;

            drawDPad(dpadUp, isPressed, canvas);
            adjustTextSize(i.upKeyName, i.radius / 2F, textPaint, i);
            canvas.drawText(i.upKeyName, i.x, i.y - offset - i.radius / 2F, textPaint);

            offset = (textPaint.getFontMetrics().ascent + textPaint.getFontMetrics().descent) / 2F;

            drawDPad(dpadDown, isPressed, canvas);
            adjustTextSize(i.downKeyName, i.radius / 2F, textPaint, i);
            canvas.drawText(i.downKeyName, i.x, i.y - offset + i.radius / 2F, textPaint);

            offset = (textPaint.getFontMetrics().ascent + textPaint.getFontMetrics().descent) / 2F;

            drawDPad(dpadLeft, isPressed, canvas);
            adjustTextSize(i.leftKeyName, i.radius / 2F, textPaint, i);
            canvas.drawText(i.leftKeyName, i.x - i.radius / 2F, i.y - offset, textPaint);

            offset = (textPaint.getFontMetrics().ascent + textPaint.getFontMetrics().descent) / 2F;

            drawDPad(dpadRight, isPressed, canvas);
            adjustTextSize(i.rightKeyName, i.radius / 2F, textPaint, i);
            canvas.drawText(i.rightKeyName, i.x + i.radius / 2F, i.y - offset, textPaint);
        });

        if (lastSelectedButton > 0) {
            editButton.x = getWidth() / 2F + editButton.radius / 2F;
            editButton.y = 20F + editButton.radius / 2F;

            removeButton.x = getWidth() / 2F - removeButton.radius;
            removeButton.y = 20F + removeButton.radius / 2F;

            paint.setStyle(Paint.Style.FILL_AND_STROKE);
            paint.setColor(Color.BLACK);
            paint.setAlpha(200);
            canvas.drawCircle(editButton.x, editButton.y, editButton.radius / 2F, paint);
            canvas.drawCircle(removeButton.x, removeButton.y, removeButton.radius / 2F, paint);

            paint.setColor(Color.WHITE);
            paint.setAlpha(200);
            canvas.drawBitmap(editIcon, editButton.x - editButton.radius / 4F, editButton.y - editButton.radius / 4F, paint);
            canvas.drawBitmap(removeIcon, removeButton.x - removeButton.radius / 4F, removeButton.y - removeButton.radius / 4F, paint);
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN, MotionEvent.ACTION_POINTER_DOWN -> {
                if (!detectClick(event, event.getActionIndex(), editButton.x, editButton.y, editButton.radius, SHAPE_CIRCLE) && !detectClick(event, event.getActionIndex(), removeButton.x, removeButton.y, removeButton.radius, SHAPE_CIRCLE)) {
                    lastSelectedButton = 0;
                }

                for (VirtualButton button : buttonList) {
                    if (detectClick(event, event.getActionIndex(), button.x, button.y, button.radius, button.shape)) {
                        if (selectedButton == 0) {
                            selectedButton = button.id;
                            lastSelectedType = BUTTON;
                            lastSelectedButton = button.id;
                        }
                    }
                }
                for (VirtualAnalog virtualAnalog : analogList) {
                    if (detectClick(event, event.getActionIndex(), virtualAnalog.x, virtualAnalog.y, virtualAnalog.radius, SHAPE_SQUARE)) {
                        if (selectedAxis == 0) {
                            selectedAxis = virtualAnalog.id;
                            lastSelectedType = ANALOG;
                            lastSelectedButton = virtualAnalog.id;
                        }
                    }
                }
                for (VirtualDPad i : dpadList) {
                    if (detectClick(event, event.getActionIndex(), i.x, i.y, i.radius, SHAPE_SQUARE)) {
                        if (selectedDPad == 0) {
                            selectedDPad = i.id;
                            lastSelectedType = DPAD;
                            lastSelectedButton = i.id;
                        }
                    }
                }

                invalidate();
            }
            case MotionEvent.ACTION_MOVE -> {
                for (VirtualButton button : buttonList) {
                    if (detectClick(event, event.getActionIndex(), button.x, button.y, button.radius, button.shape)) {
                        if (selectedButton == button.id) {
                            buttonList.get(button.id - 1).x = Math.round(event.getX(event.getActionIndex()) / GRID_SIZE) * GRID_SIZE;
                            buttonList.get(button.id - 1).y = Math.round(event.getY(event.getActionIndex()) / GRID_SIZE) * GRID_SIZE;
                            break;
                        }
                    }
                }
                for (VirtualAnalog virtualAnalog : analogList) {
                    if (detectClick(event, event.getActionIndex(), virtualAnalog.x, virtualAnalog.y, virtualAnalog.radius, SHAPE_SQUARE)) {
                        if (selectedAxis == virtualAnalog.id) {
                            analogList.get(virtualAnalog.id - 1).x = Math.round(event.getX(event.getActionIndex()) / GRID_SIZE) * GRID_SIZE;
                            analogList.get(virtualAnalog.id - 1).y = Math.round(event.getY(event.getActionIndex()) / GRID_SIZE) * GRID_SIZE;
                            break;
                        }
                    }
                }
                for (VirtualDPad dpad : dpadList) {
                    if (detectClick(event, event.getActionIndex(), dpad.x, dpad.y, dpad.radius, SHAPE_SQUARE)) {
                        if (selectedDPad == dpad.id) {
                            dpadList.get(dpad.id - 1).x = Math.round(event.getX(event.getActionIndex()) / GRID_SIZE) * GRID_SIZE;
                            dpadList.get(dpad.id - 1).y = Math.round(event.getY(event.getActionIndex()) / GRID_SIZE) * GRID_SIZE;
                            break;
                        }
                    }
                }

                invalidate();
            }
            case MotionEvent.ACTION_POINTER_UP, MotionEvent.ACTION_UP -> {
                buttonList.forEach((i) -> {
                    if (detectClick(event, event.getActionIndex(), i.x, i.y, i.radius, i.shape)) {
                        if (selectedButton == i.id) {
                            selectedButton = 0;
                        }
                    }
                });
                analogList.forEach((i) -> {
                    if (detectClick(event, event.getActionIndex(), i.x, i.y, i.radius, SHAPE_SQUARE)) {
                        if (selectedAxis == i.id) {
                            selectedAxis = 0;
                        }
                    }
                });
                dpadList.forEach((i) -> {
                    if (detectClick(event, event.getActionIndex(), i.x, i.y, i.radius, SHAPE_DPAD)) {
                        if (selectedDPad == i.id) {
                            selectedDPad = 0;
                        }
                    }
                });

                if (detectClick(event, event.getActionIndex(), editButton.x, editButton.y, editButton.radius, SHAPE_CIRCLE) && lastSelectedButton > 0) {
                    if (!buttonList.isEmpty() && lastSelectedType == BUTTON) {
                        selectedButtonKeyName = buttonList.get(lastSelectedButton - 1).keyName;
                        selectedButtonRadius = (int) buttonList.get(lastSelectedButton - 1).radius;
                        selectedButtonShape = buttonList.get(lastSelectedButton - 1).shape;
                    }
                    if (!analogList.isEmpty() && lastSelectedType == ANALOG) {
                        selectedAnalogUpKeyName = analogList.get(lastSelectedButton - 1).upKeyName;
                        selectedAnalogDownKeyName = analogList.get(lastSelectedButton - 1).downKeyName;
                        selectedAnalogLeftKeyName = analogList.get(lastSelectedButton - 1).leftKeyName;
                        selectedAnalogRightKeyName = analogList.get(lastSelectedButton - 1).rightKeyName;
                        selectedButtonRadius = (int) analogList.get(lastSelectedButton - 1).radius;
                    }
                    if (!dpadList.isEmpty() && lastSelectedType == DPAD) {
                        selectedAnalogUpKeyName = dpadList.get(lastSelectedButton - 1).upKeyName;
                        selectedAnalogDownKeyName = dpadList.get(lastSelectedButton - 1).downKeyName;
                        selectedAnalogLeftKeyName = dpadList.get(lastSelectedButton - 1).leftKeyName;
                        selectedAnalogRightKeyName = dpadList.get(lastSelectedButton - 1).rightKeyName;
                        selectedButtonRadius = (int) dpadList.get(lastSelectedButton - 1).radius;
                    }

                    getContext().sendBroadcast(
                            new Intent(ACTION_EDIT_VIRTUAL_BUTTON)
                    );
                }
                if (detectClick(event, event.getActionIndex(), removeButton.x, removeButton.y, removeButton.radius, SHAPE_CIRCLE) && lastSelectedButton > 0) {
                    if (!buttonList.isEmpty() && lastSelectedType == BUTTON) {
                        buttonList.remove(lastSelectedButton - 1);
                    }
                    if (!analogList.isEmpty() && lastSelectedType == ANALOG) {
                        analogList.remove(lastSelectedButton - 1);
                    }
                    if (!dpadList.isEmpty() && lastSelectedType == DPAD) {
                        dpadList.remove(lastSelectedButton - 1);
                    }

                    lastSelectedButton = 0;
                    reorderButtonsAnalogsIDs();
                }

                invalidate();
            }
        }

        return true;
    }

    private static class CircleButton {
        float x;
        float y;
        float radius;
    }

    public final static int BUTTON = 0;
    public final static int ANALOG = 1;
    public final static int DPAD = 2;
    public final static int GRID_SIZE = 30;

    public static int lastSelectedButton = 0;
    public static int lastSelectedType = BUTTON;
}