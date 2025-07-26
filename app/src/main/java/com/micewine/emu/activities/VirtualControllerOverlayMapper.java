package com.micewine.emu.activities;

import static android.os.Build.VERSION.SDK_INT;
import static com.micewine.emu.controller.XKeyCodes.getMapping;
import static com.micewine.emu.views.VirtualKeyboardInputView.SHAPE_CIRCLE;
import static com.micewine.emu.views.VirtualKeyboardInputView.analogList;
import static com.micewine.emu.views.VirtualKeyboardInputView.buttonList;
import static com.micewine.emu.views.VirtualKeyboardInputView.dpadList;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.os.Build;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.WindowManager;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;
import com.micewine.emu.R;
import com.micewine.emu.controller.XKeyCodes.ButtonMapping;
import com.micewine.emu.databinding.ActivityVirtualControllerMapperBinding;
import com.micewine.emu.fragments.EditVirtualButtonFragment;
import com.micewine.emu.views.VirtualKeyboardInputCreatorView;
import com.micewine.emu.views.VirtualKeyboardInputView;

public class VirtualControllerOverlayMapper extends AppCompatActivity {
    private VirtualKeyboardInputCreatorView virtualKeyboardInputCreatorView;
    private DrawerLayout drawerLayout;
    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (ACTION_EDIT_VIRTUAL_BUTTON.equals(intent.getAction())) {
                new EditVirtualButtonFragment().show(getSupportFragmentManager(), "");
            } else if (ACTION_INVALIDATE.equals(intent.getAction())) {
                virtualKeyboardInputCreatorView.invalidate();
            }
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivityVirtualControllerMapperBinding binding = ActivityVirtualControllerMapperBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        virtualKeyboardInputCreatorView = findViewById(R.id.overlayView);

        drawerLayout = findViewById(R.id.virtualControllerMapperDrawerLayout);
        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);

        NavigationView navigationView = findViewById(R.id.navigationView);
        navigationView.setNavigationItemSelectedListener(item -> {
            drawerLayout.closeDrawers();

            if (item.getItemId() == R.id.addButton) {
                virtualKeyboardInputCreatorView.addButton(
                        new VirtualKeyboardInputView.VirtualButton(
                                virtualKeyboardInputCreatorView.getWidth() / 2F,
                                virtualKeyboardInputCreatorView.getHeight() / 2F,
                                180F,
                                "--",
                                SHAPE_CIRCLE
                        )
                );
            } else if (item.getItemId() == R.id.addVAxis) {
                virtualKeyboardInputCreatorView.addAnalog(
                        new VirtualKeyboardInputView.VirtualAnalog(
                                virtualKeyboardInputCreatorView.getWidth() / 2F,
                                virtualKeyboardInputCreatorView.getHeight() / 2F,
                                275F,
                                "--",
                                "--",
                                "--",
                                "--",
                                0.75F
                        )
                );
            } else if (item.getItemId() == R.id.addDPad) {
                virtualKeyboardInputCreatorView.addDPad(
                        new VirtualKeyboardInputView.VirtualDPad(
                                virtualKeyboardInputCreatorView.getWidth() / 2F,
                                virtualKeyboardInputCreatorView.getHeight() / 2F,
                                275F,
                                "--",
                                "--",
                                "--",
                                "--",
                                0F
                        )
                );
            } else if (item.getItemId() == R.id.exitButton) {
                virtualKeyboardInputCreatorView.saveOnPreferences();
                finish();
            }

            return true;
        });

        registerReceiver(receiver, new IntentFilter(ACTION_EDIT_VIRTUAL_BUTTON) {{
            addAction(ACTION_INVALIDATE);
        }}, SDK_INT >= Build.VERSION_CODES.TIRAMISU ? RECEIVER_EXPORTED : 0);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (drawerLayout.isOpen()) {
                drawerLayout.closeDrawers();
            } else {
                drawerLayout.openDrawer(GravityCompat.START);
            }

            return true;
        }

        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);

        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        WindowInsetsControllerCompat controller = new WindowInsetsControllerCompat(getWindow(), getWindow().getDecorView());

        controller.hide(WindowInsetsCompat.Type.systemBars());
        controller.setSystemBarsBehavior(WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);

        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        );
    }

    public final static String ACTION_EDIT_VIRTUAL_BUTTON = "com.micewine.emu.ACTION_EDIT_VIRTUAL_BUTTON";
    public final static String ACTION_INVALIDATE = "com.micewine.emu.ACTION_INVALIDATE";
}