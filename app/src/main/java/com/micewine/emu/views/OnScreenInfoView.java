package com.micewine.emu.views;

import static com.micewine.emu.activities.EmulationActivity.handler;
import static com.micewine.emu.activities.MainActivity.enableCpuCounter;
import static com.micewine.emu.activities.MainActivity.enableDebugInfo;
import static com.micewine.emu.activities.MainActivity.enableRamCounter;
import static com.micewine.emu.activities.MainActivity.memoryStats;
import static com.micewine.emu.activities.MainActivity.miceWineVersion;
import static com.micewine.emu.activities.MainActivity.selectedD3DXRenderer;
import static com.micewine.emu.activities.MainActivity.selectedDXVK;
import static com.micewine.emu.activities.MainActivity.selectedVKD3D;
import static com.micewine.emu.activities.MainActivity.selectedWineD3D;
import static com.micewine.emu.activities.MainActivity.totalCpuUsage;
import static com.micewine.emu.activities.MainActivity.vulkanDriverDeviceName;
import static com.micewine.emu.activities.MainActivity.vulkanDriverDriverVersion;
import static com.micewine.emu.core.RatPackageManager.getPackageNameVersionById;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;

import com.micewine.emu.R;

public class OnScreenInfoView extends View {
    public OnScreenInfoView(Context context) {
        super(context);
        init(context);
    }

    public OnScreenInfoView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public OnScreenInfoView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private final Paint paint = new Paint();
    private final Runnable updateRunnable = new Runnable() {
        @Override
        public void run() {
            invalidate();
            handler.postDelayed(this, 800);
        }
    };

    private void init(Context context) {
        paint.setTextSize(40F);
        paint.setTypeface(context.getResources().getFont(R.font.quicksand));
        paint.setStrokeWidth(8F);

        handler.post(updateRunnable);
    }

    private int textCount = 0;
    private final String vkd3dVersion = getPackageNameVersionById(selectedVKD3D);
    private final String dxvkVersion = getPackageNameVersionById(selectedDXVK);
    private final String wineD3DVersion = getPackageNameVersionById(selectedWineD3D);

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);

        textCount = 0;

        if (enableRamCounter) {
            drawText("RAM: " + memoryStats, 20F, canvas);
        }
        if (enableCpuCounter) {
            drawText("CPU: " + totalCpuUsage, 20F, canvas);
        }
        if (enableDebugInfo) {
            onScreenInfo(canvas);
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        handler.removeCallbacks(updateRunnable);
    }

    private void onScreenInfo(Canvas canvas) {
        textCount = 0;

        drawText(miceWineVersion, getTextEndX(canvas, miceWineVersion), canvas);
        drawText(vkd3dVersion, getTextEndX(canvas, vkd3dVersion), canvas);

        if ("DXVK".equals(selectedD3DXRenderer)) {
            drawText(dxvkVersion, getTextEndX(canvas, dxvkVersion), canvas);
        } else if ("WineD3D".equals(selectedD3DXRenderer)) {
            drawText(wineD3DVersion, getTextEndX(canvas, wineD3DVersion), canvas);
        }

        drawText(vulkanDriverDeviceName, getTextEndX(canvas, vulkanDriverDeviceName), canvas);
        drawText(vulkanDriverDriverVersion, getTextEndX(canvas, vulkanDriverDriverVersion), canvas);
    }

    private void drawText(String text, float x, Canvas canvas) {
        textCount++;

        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(Color.BLACK);
        canvas.drawText(text, x, textCount * (paint.getTextSize() + 10F), paint);

        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.WHITE);
        canvas.drawText(text, x, textCount * (paint.getTextSize() + 10F), paint);
    }

    private float getTextEndX(Canvas canvas, String text) {
        return canvas.getWidth() - paint.measureText(text) - 20F;
    }
}