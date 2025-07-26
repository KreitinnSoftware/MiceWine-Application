package com.micewine.emu.activities;

import static android.os.Build.VERSION.SDK_INT;

import static com.micewine.emu.activities.MainActivity.setSharedVars;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.view.KeyEvent;
import android.widget.ImageButton;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.micewine.emu.R;
import com.micewine.emu.databinding.ActivityGeneralSettingsBinding;
import com.micewine.emu.fragments.Box64SettingsFragment;
import com.micewine.emu.fragments.DebugSettingsFragment;
import com.micewine.emu.fragments.DriverInfoFragment;
import com.micewine.emu.fragments.DriversSettingsFragment;
import com.micewine.emu.fragments.EnvVarsSettingsFragment;
import com.micewine.emu.fragments.GeneralSettingsFragment;
import com.micewine.emu.fragments.SoundSettingsFragment;
import com.micewine.emu.fragments.WineSettingsFragment;

public class GeneralSettingsActivity extends AppCompatActivity {
    private Toolbar generalSettingsToolbar;
    private final Box64SettingsFragment box64SettingsFragment = new Box64SettingsFragment();
    private final DebugSettingsFragment debugSettingsFragment = new DebugSettingsFragment();
    private final DriverInfoFragment driverInfoFragment = new DriverInfoFragment();
    private final DriversSettingsFragment driversSettingsFragment = new DriversSettingsFragment();
    private final EnvVarsSettingsFragment envVarsSettingsFragment = new EnvVarsSettingsFragment();
    private final SoundSettingsFragment soundSettingsFragment = new SoundSettingsFragment();
    private final WineSettingsFragment wineSettingsFragment = new WineSettingsFragment();
    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String preference = intent.getStringExtra("preference");
            if (preference == null) return;

            generalSettingsToolbar.setTitle(preference);

            if (ACTION_PREFERENCE_SELECT.equals(intent.getAction())) {
                if (preference.equals(getString(R.string.box64_settings_title))) {
                    fragmentLoader(box64SettingsFragment, false);
                } else if (preference.equals(getString(R.string.debug_settings_title))) {
                    fragmentLoader(debugSettingsFragment, false);
                } else if (preference.equals(getString(R.string.driver_settings_title))) {
                    fragmentLoader(driversSettingsFragment, false);
                } else if (preference.equals(getString(R.string.driver_info_title))) {
                    fragmentLoader(driverInfoFragment, false);
                } else if (preference.equals(getString(R.string.env_settings_title))) {
                    fragmentLoader(envVarsSettingsFragment, false);
                } else if (preference.equals(getString(R.string.sound_settings_title))) {
                    fragmentLoader(soundSettingsFragment, false);
                } else if (preference.equals(getString(R.string.wine_settings_title))) {
                    fragmentLoader(wineSettingsFragment, false);
                }
            }
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivityGeneralSettingsBinding binding = ActivityGeneralSettingsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        fragmentLoader(new GeneralSettingsFragment(), true);

        generalSettingsToolbar = findViewById(R.id.generalSettingsToolbar);
        generalSettingsToolbar.setTitle(R.string.general_settings);

        ImageButton backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener((v) -> onKeyDown(KeyEvent.KEYCODE_BACK, null));

        registerReceiver(receiver, new IntentFilter(ACTION_PREFERENCE_SELECT), SDK_INT >= Build.VERSION_CODES.TIRAMISU ? RECEIVER_EXPORTED : 0);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        setSharedVars(this);
        unregisterReceiver(receiver);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
                getSupportFragmentManager().popBackStack();
                generalSettingsToolbar.setTitle(R.string.general_settings);
            } else {
                finish();
            }
        }

        return true;
    }

    private void fragmentLoader(Fragment fragment, boolean appInit) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

        transaction.setCustomAnimations(
                R.anim.slide_in,
                R.anim.fade_out,
                R.anim.fade_in,
                R.anim.slide_out
        );

        transaction.replace(R.id.settings_content, fragment);
        if (!appInit) transaction.addToBackStack(null);
        transaction.commit();
    }

    public final static String ACTION_PREFERENCE_SELECT = "com.micewine.emu.ACTION_PREFERENCE_SELECT";
    public final static int SWITCH = 1;
    public final static int SPINNER = 2;
    public final static int CHECKBOX = 3;
    public final static int SEEKBAR = 4;

    public final static String BOX64_LOG = "BOX64_LOG";
    public final static int BOX64_LOG_DEFAULT_VALUE = 1;
    public final static String BOX64_MMAP32 = "BOX64_MMAP32";
    public final static int BOX64_MMAP32_DEFAULT_VALUE = 1;
    public final static String BOX64_AVX = "BOX64_AVX";
    public final static int BOX64_AVX_DEFAULT_VALUE = 2;
    public final static String BOX64_SSE42 = "BOX64_SSE42";
    public final static int BOX64_SSE42_DEFAULT_VALUE = 1;
    public final static String BOX64_DYNAREC_BIGBLOCK = "BOX64_DYNAREC_BIGBLOCK";
    public final static int BOX64_DYNAREC_BIGBLOCK_DEFAULT_VALUE = 1;
    public final static String BOX64_DYNAREC_STRONGMEM = "BOX64_DYNAREC_STRONGMEM";
    public final static int BOX64_DYNAREC_STRONGMEM_DEFAULT_VALUE = 1;
    public final static String BOX64_DYNAREC_WEAKBARRIER = "BOX64_DYNAREC_WEAKBARRIER";
    public final static int BOX64_DYNAREC_WEAKBARRIER_DEFAULT_VALUE = 1;
    public final static String BOX64_DYNAREC_PAUSE = "BOX64_DYNAREC_PAUSE";
    public final static int BOX64_DYNAREC_PAUSE_DEFAULT_VALUE = 0;
    public final static String BOX64_DYNAREC_X87DOUBLE = "BOX64_DYNAREC_X87DOUBLE";
    public final static int BOX64_DYNAREC_X87DOUBLE_DEFAULT_VALUE = 0;
    public final static String BOX64_DYNAREC_FASTNAN = "BOX64_DYNAREC_FASTNAN";
    public final static int BOX64_DYNAREC_FASTNAN_DEFAULT_VALUE = 1;
    public final static String BOX64_DYNAREC_FASTROUND = "BOX64_DYNAREC_FASTROUND";
    public final static int BOX64_DYNAREC_FASTROUND_DEFAULT_VALUE = 1;
    public final static String BOX64_DYNAREC_SAFEFLAGS = "BOX64_DYNAREC_SAFEFLAGS";
    public final static int BOX64_DYNAREC_SAFEFLAGS_DEFAULT_VALUE = 1;
    public final static String BOX64_DYNAREC_CALLRET = "BOX64_DYNAREC_CALLRET";
    public final static int BOX64_DYNAREC_CALLRET_DEFAULT_VALUE = 1;
    public final static String BOX64_DYNAREC_DF = "BOX64_DYNAREC_DF";
    public final static int BOX64_DYNAREC_DF_DEFAULT_VALUE = 1;
    public final static String BOX64_DYNAREC_ALIGNED_ATOMICS = "BOX64_DYNAREC_ALIGNED_ATOMICS";
    public final static int BOX64_DYNAREC_ALIGNED_ATOMICS_DEFAULT_VALUE = 0;
    public final static String BOX64_DYNAREC_NATIVEFLAGS = "BOX64_DYNAREC_NATIVEFLAGS";
    public final static int BOX64_DYNAREC_NATIVEFLAGS_DEFAULT_VALUE = 1;
    public final static String BOX64_DYNAREC_WAIT = "BOX64_DYNAREC_WAIT";
    public final static int BOX64_DYNAREC_WAIT_DEFAULT_VALUE = 1;
    public final static String BOX64_DYNAREC_DIRTY = "BOX64_DYNAREC_DIRTY";
    public final static int BOX64_DYNAREC_DIRTY_DEFAULT_VALUE = 0;
    public final static String BOX64_DYNAREC_FORWARD = "BOX64_DYNAREC_FORWARD";
    public final static int BOX64_DYNAREC_FORWARD_DEFAULT_VALUE = 128;
    public final static String BOX64_SHOWSEGV = "BOX64_SHOWSEGV";
    public final static boolean BOX64_SHOWSEGV_DEFAULT_VALUE = false;
    public final static String BOX64_SHOWBT = "BOX64_SHOWBT";
    public final static boolean BOX64_SHOWBT_DEFAULT_VALUE = false;
    public final static String BOX64_NOSIGSEGV = "BOX64_NOSIGSEGV";
    public final static boolean BOX64_NOSIGSEGV_DEFAULT_VALUE = false;
    public final static String BOX64_NOSIGILL = "BOX64_NOSIGILL";
    public final static boolean BOX64_NOSIGILL_DEFAULT_VALUE = false;

    public final static String SELECTED_BOX64 = "selectedBox64";
    public final static String SELECTED_VULKAN_DRIVER = "selectedVulkanDriver";
    public final static String SELECTED_WINE_PREFIX = "selectedWinePrefix";
    public final static String SELECTED_TU_DEBUG_PRESET = "selectedTuDebugPreset";
    public final static String SELECTED_TU_DEBUG_PRESET_DEFAULT_VALUE = "noconform,sysmem";
    public final static String ENABLE_DRI3 = "enableDRI3";
    public final static boolean ENABLE_DRI3_DEFAULT_VALUE = true;
    public final static String ENABLE_MANGOHUD = "enableMangoHUD";
    public final static boolean ENABLE_MANGOHUD_DEFAULT_VALUE = true;
    public final static String WINE_LOG_LEVEL = "wineLogLevel";
    public final static String WINE_LOG_LEVEL_DEFAULT_VALUE = "default";
    public final static String SELECTED_GL_PROFILE = "selectedGLProfile";
    public final static String SELECTED_GL_PROFILE_DEFAULT_VALUE = "GL 3.2";

    public final static String SELECTED_DXVK_HUD_PRESET = "selectedDXVKHudPreset";
    public final static String SELECTED_DXVK_HUD_PRESET_DEFAULT_VALUE = "";
    public final static String SELECTED_MESA_VK_WSI_PRESENT_MODE = "MESA_VK_WSI_PRESENT_MODE";
    public final static String SELECTED_MESA_VK_WSI_PRESENT_MODE_DEFAULT_VALUE = "mailbox";

    public final static String DEAD_ZONE = "deadZone";
    public final static String MOUSE_SENSIBILITY = "mouseSensibility";
    public final static String FPS_LIMIT = "fpsLimit";
    public final static String PA_SINK = "pulseAudioSink";
    public final static String PA_SINK_DEFAULT_VALUE = "SLES";
    public final static String WINE_DPI = "wineDpi";
    public final static int WINE_DPI_DEFAULT_VALUE = 96;
    public final static String WINE_DPI_APPLIED = "wineDpiApplied";
    public final static boolean WINE_DPI_APPLIED_DEFAULT_VALUE = false;
}