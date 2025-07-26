package com.micewine.emu.activities;

import static android.os.Build.VERSION.SDK_INT;
import static com.micewine.emu.fragments.CreatePresetFragment.BOX64_PRESET;
import static com.micewine.emu.fragments.CreatePresetFragment.CONTROLLER_PRESET;
import static com.micewine.emu.fragments.CreatePresetFragment.VIRTUAL_CONTROLLER_PRESET;
import static com.micewine.emu.fragments.CreatePresetFragment.WINE_PREFIX_PRESET;
import static com.micewine.emu.fragments.FloatingFileManagerFragment.OPERATION_IMPORT_PRESET;
import static com.micewine.emu.adapters.AdapterPreset.clickedPresetType;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.micewine.emu.R;
import com.micewine.emu.databinding.ActivityPresetManagerBinding;
import com.micewine.emu.fragments.Box64PresetManagerFragment;
import com.micewine.emu.fragments.Box64SettingsFragment;
import com.micewine.emu.fragments.ControllerMapperFragment;
import com.micewine.emu.fragments.ControllerPresetManagerFragment;
import com.micewine.emu.fragments.CreatePresetFragment;
import com.micewine.emu.fragments.FloatingFileManagerFragment;
import com.micewine.emu.fragments.VirtualControllerPresetManagerFragment;
import com.micewine.emu.fragments.WinePrefixManagerFragment;

public class PresetManagerActivity extends AppCompatActivity {
    private FloatingActionButton addPresetFAB;
    private FloatingActionButton importPresetFAB;
    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (ACTION_EDIT_CONTROLLER_MAPPING.equals(intent.getAction())) {
                fragmentLoader(new ControllerMapperFragment(), false);

                addPresetFAB.setVisibility(View.GONE);
                importPresetFAB.setVisibility(View.GONE);
            } else if (ACTION_EDIT_BOX64_PRESET.equals(intent.getAction())) {
                fragmentLoader(new Box64SettingsFragment(), false);

                addPresetFAB.setVisibility(View.GONE);
                importPresetFAB.setVisibility(View.GONE);
            }
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivityPresetManagerBinding binding = ActivityPresetManagerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ImageButton backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener((v) -> onKeyDown(KeyEvent.KEYCODE_BACK, null));

        Toolbar controllerMapperToolbar = findViewById(R.id.controllerMapperToolbar);

        addPresetFAB = findViewById(R.id.addPresetFAB);
        importPresetFAB = findViewById(R.id.importPresetFAB);

        int presetType = getIntent().getIntExtra("presetType", -1);
        boolean editShortcut = getIntent().getBooleanExtra("editShortcut", false);

        switch (presetType) {
            case CONTROLLER_PRESET: {
                fragmentLoader(new ControllerPresetManagerFragment(editShortcut), true);

                controllerMapperToolbar.setTitle(R.string.controller_mapper_title);

                addPresetFAB.setOnClickListener((v) -> new CreatePresetFragment(CONTROLLER_PRESET).show(getSupportFragmentManager(), ""));
                importPresetFAB.setOnClickListener((v) -> {
                    clickedPresetType = CONTROLLER_PRESET;
                    new FloatingFileManagerFragment(OPERATION_IMPORT_PRESET, "/storage/emulated/0").show(getSupportFragmentManager(), "");
                });
                break;
            }
            case VIRTUAL_CONTROLLER_PRESET: {
                fragmentLoader(new VirtualControllerPresetManagerFragment(editShortcut), true);

                controllerMapperToolbar.setTitle(R.string.virtual_controller_mapper_title);

                addPresetFAB.setOnClickListener((v) -> new CreatePresetFragment(VIRTUAL_CONTROLLER_PRESET).show(getSupportFragmentManager(), ""));
                importPresetFAB.setOnClickListener((v) -> {
                    clickedPresetType = VIRTUAL_CONTROLLER_PRESET;
                    new FloatingFileManagerFragment(OPERATION_IMPORT_PRESET, "/storage/emulated/0").show(getSupportFragmentManager(), "");
                });
                break;
            }
            case BOX64_PRESET: {
                fragmentLoader(new Box64PresetManagerFragment(), true);

                controllerMapperToolbar.setTitle(R.string.box64_preset_manager_title);

                addPresetFAB.setOnClickListener((v) -> new CreatePresetFragment(BOX64_PRESET).show(getSupportFragmentManager(), ""));
                importPresetFAB.setOnClickListener((v) -> {
                    clickedPresetType = BOX64_PRESET;
                    new FloatingFileManagerFragment(OPERATION_IMPORT_PRESET, "/storage/emulated/0").show(getSupportFragmentManager(), "");
                });
                break;
            }
            case WINE_PREFIX_PRESET: {
                fragmentLoader(new WinePrefixManagerFragment(), true);

                controllerMapperToolbar.setTitle(R.string.wine_prefix_manager_title);

                addPresetFAB.setOnClickListener((v) -> new CreatePresetFragment(WINE_PREFIX_PRESET).show(getSupportFragmentManager(), ""));
                importPresetFAB.setVisibility(View.GONE);
                break;
            }
        }

        registerReceiver(receiver, new IntentFilter() {{
            addAction(ACTION_EDIT_CONTROLLER_MAPPING);
            addAction(ACTION_EDIT_BOX64_PRESET);
        }}, SDK_INT >= Build.VERSION_CODES.TIRAMISU ? RECEIVER_EXPORTED : 0);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
                getSupportFragmentManager().popBackStack();
                addPresetFAB.setVisibility(View.VISIBLE);
                importPresetFAB.setVisibility(View.VISIBLE);
            } else {
                finish();
            }
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void fragmentLoader(Fragment fragment, boolean appInit) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

        transaction.setCustomAnimations(
                R.anim.slide_in,
                R.anim.fade_out,
                R.anim.fade_in,
                R.anim.slide_out
        );

        transaction.replace(R.id.presetManagerContent, fragment);
        if (!appInit) transaction.addToBackStack(null);
        transaction.commit();
    }

    public final static String BUTTON_A_KEY = "buttonA";
    public final static String BUTTON_B_KEY = "buttonB";
    public final static String BUTTON_X_KEY = "buttonX";
    public final static String BUTTON_Y_KEY = "buttonY";
    public final static String BUTTON_START_KEY = "buttonStart";
    public final static String BUTTON_SELECT_KEY = "buttonSelect";
    public final static String BUTTON_R1_KEY = "buttonR1";
    public final static String BUTTON_R2_KEY = "buttonR2";
    public final static String BUTTON_L1_KEY = "buttonL1";
    public final static String BUTTON_L2_KEY = "buttonL2";
    public final static String BUTTON_THUMBL_KEY = "thumbLKey";
    public final static String BUTTON_THUMBR_KEY = "thumbRKey";
    public final  static String AXIS_X_PLUS_KEY = "axisX+";
    public final static String AXIS_X_MINUS_KEY = "axisX-";
    public final static String AXIS_Y_PLUS_KEY = "axisY+";
    public final static String AXIS_Y_MINUS_KEY = "axisY-";
    public final static String AXIS_Z_PLUS_KEY = "axisZ+";
    public final static String AXIS_Z_MINUS_KEY = "axisZ-";
    public final static String AXIS_RZ_PLUS_KEY = "axisRZ+";
    public final static String AXIS_RZ_MINUS_KEY = "axisRZ-";
    public final static String AXIS_HAT_X_PLUS_KEY = "axisHatX+";
    public final static String AXIS_HAT_X_MINUS_KEY = "axisHatX-";
    public final static String AXIS_HAT_Y_PLUS_KEY = "axisHatY+";
    public final static String AXIS_HAT_Y_MINUS_KEY = "axisHatY-";

    public static String SELECTED_CONTROLLER_PRESET = "selectedControllerPreset";
    public static String SELECTED_VIRTUAL_CONTROLLER_PRESET = "selectedVirtualControllerPreset";
    public static String SELECTED_BOX64_PRESET = "selectedBox64Preset";

    public static String ACTION_EDIT_CONTROLLER_MAPPING = "com.micewine.emu.ACTION_EDIT_CONTROLLER_MAPPING";
    public static String ACTION_EDIT_BOX64_PRESET = "com.micewine.emu.ACTION_EDIT_BOX64_PRESET";
}