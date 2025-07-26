package com.micewine.emu.fragments;

import static com.micewine.emu.controller.ControllerUtils.connectedPhysicalControllers;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.micewine.emu.R;
import com.micewine.emu.adapters.AdapterControllerView;
import com.micewine.emu.controller.ControllerUtils;

import java.util.ArrayList;

public class ControllerViewFragment extends Fragment {
    private final ArrayList<AdapterControllerView.ControllerViewList> controllerViewList = new ArrayList<>();
    private RecyclerView recyclerView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_controller_view, container, false);
        recyclerView = rootView.findViewById(R.id.recyclerViewControllerView);

        setAdapter();

        return rootView;
    }

    private void setAdapter() {
        recyclerView.setAdapter(new AdapterControllerView(controllerViewList, requireContext()));

        controllerViewList.clear();

        connectedPhysicalControllers.forEach((pController) -> controllerViewList.add(
                new AdapterControllerView.ControllerViewList(pController.getName(), pController.id)
        ));
    }

    @SuppressLint("NotifyDataSetChanged")
    public void invalidateControllerView() {
        RecyclerView.Adapter<?> adapter = recyclerView.getAdapter();
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }

    private static float[] getClampedAnalogPosition(float x, float y, float lx, float ly) {
        float dx = lx * 30F;
        float dy = ly * 30F;

        float distSquared = dx * dx + dy * dy;
        float maxDist = 30F * 30F;

        if (distSquared > maxDist) {
            float scale = (float) (30F / Math.sqrt(distSquared));
            dx *= scale;
            dy *= scale;
        }

        return new float[] { dx + x, dy + y };
    }

    private static void drawAnalog(float cx, float cy, float lx, float ly, boolean isPressed, Paint paint, Canvas canvas) {
        float[] analogPos = getClampedAnalogPosition(cx, cy, lx, ly);

        paint.setStyle(isPressed ? Paint.Style.FILL : Paint.Style.STROKE);
        paint.setColor(Color.WHITE);
        canvas.drawCircle(cx, cy, 60F, paint);

        paint.setStyle(Paint.Style.FILL);
        paint.setColor(isPressed ? Color.BLACK : Color.WHITE);
        canvas.drawCircle(analogPos[0], analogPos[1], 30F, paint);
    }

    private static void drawButton(float cx, float cy, String buttonName, boolean isPressed, Paint paint, Paint textPaint, Canvas canvas) {
        if (isPressed) {
            paint.setStyle(Paint.Style.FILL_AND_STROKE);
            textPaint.setColor(Color.BLACK);
        } else {
            paint.setStyle(Paint.Style.STROKE);
            textPaint.setColor(Color.WHITE);
        }
        paint.setColor(Color.WHITE);
        canvas.drawCircle(cx, cy, 30F, paint);
        canvas.drawText(buttonName, cx, cy + 14F, textPaint);
    }

    private static void drawRoundRectButton(float cx, float cy, String buttonName, boolean isPressed, Paint paint, Paint textPaint, Canvas canvas) {
        if (isPressed) {
            paint.setStyle(Paint.Style.FILL_AND_STROKE);
            textPaint.setColor(Color.BLACK);
        } else {
            paint.setStyle(Paint.Style.STROKE);
            textPaint.setColor(Color.WHITE);
        }
        paint.setColor(Color.WHITE);
        canvas.drawRoundRect(
                cx - 30F,
                cy - 15F,
                cx + 60F,
                cy + 30F,
                12F,
                12F,
                paint
        );
        canvas.drawText(buttonName, cx + 15F, cy + 22F, textPaint);
    }

    private static void drawDPad(Path path, boolean isPressed, Paint paint, Canvas canvas) {
        paint.setStyle(isPressed ? Paint.Style.FILL_AND_STROKE : Paint.Style.STROKE);
        canvas.drawPath(path, paint);
    }

    public static Bitmap getControllerBitmap(int width, int height, int controllerId, Context context) {
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        int index = -1;
        for (int i = 0; i < connectedPhysicalControllers.size(); i++) {
            if (connectedPhysicalControllers.get(i).id == controllerId) {
                index = i;
                break;
            }
        }
        ControllerUtils.PhysicalController pController = connectedPhysicalControllers.get(index);
        if (pController == null) return bitmap;

        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint();

        paint.setStrokeWidth(8F);
        paint.setColor(Color.WHITE);
        paint.setStyle(Paint.Style.STROKE);

        Paint textPaint = new Paint();

        textPaint.setTextSize(40F);
        textPaint.setColor(Color.WHITE);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setTypeface(context.getResources().getFont(R.font.quicksand));

        drawAnalog(80F, 320F, pController.state.lx, pController.state.ly, pController.state.lsPressed, paint, canvas);
        drawAnalog(500F, 190F, pController.state.rx, pController.state.ry, pController.state.rsPressed, paint, canvas);

        drawButton(600F, 240F, "Y", pController.state.yPressed, paint, textPaint, canvas);
        drawButton(540F, 300F, "X", pController.state.xPressed, paint, textPaint, canvas);
        drawButton(660F, 300F, "B", pController.state.bPressed, paint, textPaint, canvas);
        drawButton(600F, 360F, "A", pController.state.aPressed, paint, textPaint, canvas);

        drawRoundRectButton(585F, 100F, "RB", pController.state.rbPressed, paint, textPaint, canvas);
        drawRoundRectButton(65F, 100F, "LB", pController.state.lbPressed, paint, textPaint, canvas);
        drawRoundRectButton(585F, 40F, "RT", (pController.state.rt > 0.2F), paint, textPaint, canvas);
        drawRoundRectButton(65F, 40F, "LT", (pController.state.lt > 0.2F), paint, textPaint, canvas);

        float x = 190F;
        float y = 190F;
        float radius = 80F;

        Path dpadLeft = new Path();
        dpadLeft.reset();
        dpadLeft.moveTo(x - 10, y);
        dpadLeft.lineTo(x - 10 - radius / 4f, y - radius / 4f);
        dpadLeft.lineTo(x - 10 - radius / 4f - radius / 2f, y - radius / 4f);
        dpadLeft.lineTo(x - 10 - radius / 4f - radius / 2f, y - radius / 4f + radius / 2f);
        dpadLeft.lineTo(x - 10 - radius / 4f, y - radius / 4f + radius / 2f);
        dpadLeft.lineTo(x - 10, y);
        dpadLeft.close();

        Path dpadDown = new Path();
        dpadDown.reset();
        dpadDown.moveTo(x, y + 10);
        dpadDown.lineTo(x - radius / 4f, y + 10 + radius / 4f);
        dpadDown.lineTo(x - radius / 4f, y + 10 + radius / 4f + radius / 2f);
        dpadDown.lineTo(x - radius / 4f + radius / 2f, y + 10 + radius / 4f + radius / 2f);
        dpadDown.lineTo(x - radius / 4f + radius / 2f, y + 10 + radius / 4f);
        dpadDown.lineTo(x, y + 10);
        dpadDown.close();

        Path dpadRight = new Path();
        dpadRight.reset();
        dpadRight.moveTo(x + 10, y);
        dpadRight.lineTo(x + 10 + radius / 4f, y - radius / 4f);
        dpadRight.lineTo(x + 10 + radius / 4f + radius / 2f, y - radius / 4f);
        dpadRight.lineTo(x + 10 + radius / 4f + radius / 2f, y - radius / 4f + radius / 2f);
        dpadRight.lineTo(x + 10 + radius / 4f, y - radius / 4f + radius / 2f);
        dpadRight.lineTo(x + 10, y);
        dpadRight.close();

        Path dpadUp = new Path();
        dpadUp.reset();
        dpadUp.moveTo(x, y - 10);
        dpadUp.lineTo(x - radius / 4f, y - 10 - radius / 4f);
        dpadUp.lineTo(x - radius / 4f, y - 10 - radius / 4f - radius / 2f);
        dpadUp.lineTo(x - radius / 4f + radius / 2f, y - 10 - radius / 4f - radius / 2f);
        dpadUp.lineTo(x - radius / 4f + radius / 2f, y - 10 - radius / 4f);
        dpadUp.lineTo(x, y - 10);
        dpadUp.close();

        drawDPad(dpadLeft, (pController.state.dpadX < -0.2F), paint, canvas);
        drawDPad(dpadRight, (pController.state.dpadX > 0.2F), paint, canvas);
        drawDPad(dpadUp, (pController.state.dpadY < -0.2F), paint, canvas);
        drawDPad(dpadDown, (pController.state.dpadY > 0.2F), paint, canvas);

        Path startButton = new Path();
        startButton.reset();
        startButton.moveTo(380F, 350F);
        startButton.lineTo(420F, 350F);
        startButton.moveTo(380F, 360F);
        startButton.lineTo(420F, 360F);
        startButton.moveTo(380F, 370F);
        startButton.lineTo(420F, 370F);
        startButton.close();

        paint.setStyle(pController.state.startPressed ? Paint.Style.FILL_AND_STROKE : Paint.Style.STROKE);
        canvas.drawCircle(400F, 360F, 30F, paint);
        paint.setStrokeWidth(6F);
        paint.setColor(pController.state.startPressed ? Color.BLACK : Color.WHITE);
        canvas.drawPath(startButton, paint);

        Path selectButton = new Path();
        selectButton.reset();
        selectButton.moveTo(295F, 377F);
        selectButton.lineTo(315F, 377F);
        selectButton.lineTo(315F, 357F);
        selectButton.lineTo(295F, 357F);
        selectButton.lineTo(295F, 380F);
        selectButton.lineTo(295F, 357F);
        selectButton.lineTo(315F, 357F);
        selectButton.lineTo(315F, 377F);
        selectButton.close();
        selectButton.moveTo(305F, 352F);
        selectButton.lineTo(305F, 342F);
        selectButton.lineTo(285F, 342F);
        selectButton.lineTo(285F, 362F);
        selectButton.lineTo(285F, 342F);
        selectButton.lineTo(305F, 342F);
        selectButton.close();

        paint.setStyle(pController.state.selectPressed ? Paint.Style.FILL_AND_STROKE : Paint.Style.STROKE);
        paint.setStrokeWidth(8F);
        paint.setColor(Color.WHITE);
        canvas.drawCircle(300F, 360F, 30F, paint);
        paint.setStrokeWidth(6F);
        paint.setColor(pController.state.selectPressed ? Color.BLACK : Color.WHITE);
        canvas.drawPath(selectButton, paint);

        return bitmap;
    }
}