package com.micewine.emu.activities;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;
import static android.os.Build.VERSION.SDK_INT;
import static android.view.WindowManager.LayoutParams.*;
import static com.micewine.emu.CmdEntryPoint.ACTION_START;
import static com.micewine.emu.activities.GeneralSettingsActivity.ENABLE_MANGOHUD;
import static com.micewine.emu.activities.GeneralSettingsActivity.ENABLE_MANGOHUD_DEFAULT_VALUE;
import static com.micewine.emu.activities.GeneralSettingsActivity.FPS_LIMIT;
import static com.micewine.emu.activities.MainActivity.enableCpuCounter;
import static com.micewine.emu.activities.MainActivity.enableRamCounter;
import static com.micewine.emu.activities.MainActivity.getCpuInfo;
import static com.micewine.emu.activities.MainActivity.getMemoryInfo;
import static com.micewine.emu.activities.MainActivity.preferences;
import static com.micewine.emu.activities.MainActivity.screenFpsLimit;
import static com.micewine.emu.activities.MainActivity.setSharedVars;
import static com.micewine.emu.activities.RatManagerActivity.generateMangoHUDConfFile;
import static com.micewine.emu.adapters.AdapterGame.selectedGameName;
import static com.micewine.emu.controller.ControllerUtils.connectController;
import static com.micewine.emu.controller.ControllerUtils.destroyInputServer;
import static com.micewine.emu.controller.ControllerUtils.disconnectController;
import static com.micewine.emu.controller.ControllerUtils.prepareControllersMappings;
import static com.micewine.emu.controller.ControllerUtils.updateAxisState;
import static com.micewine.emu.controller.ControllerUtils.updateButtonsState;
import static com.micewine.emu.core.ShellLoader.runCommand;
import static com.micewine.emu.fragments.CreatePresetFragment.CONTROLLER_PRESET;
import static com.micewine.emu.fragments.CreatePresetFragment.VIRTUAL_CONTROLLER_PRESET;
import static com.micewine.emu.fragments.ShortcutsFragment.getSelectedVirtualControllerPreset;
import static com.micewine.emu.fragments.ShortcutsFragment.getVirtualControllerXInput;
import static com.micewine.emu.views.VirtualControllerInputView.virtualXInputControllerId;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.media.AudioManager;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.ParcelFileDescriptor;
import android.os.RemoteException;
import android.util.Log;
import android.view.Display;
import android.view.InputDevice;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowInsets;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.view.GravityCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.navigation.NavigationView;
import com.micewine.emu.ICmdEntryInterface;
import com.micewine.emu.LorieView;
import com.micewine.emu.R;
import com.micewine.emu.controller.ControllerUtils;
import com.micewine.emu.core.ShellLoader;
import com.micewine.emu.core.WineWrapper;
import com.micewine.emu.fragments.ControllerSettingsFragment;
import com.micewine.emu.fragments.LogViewerFragment;
import com.micewine.emu.fragments.TaskManagerFragment;
import com.micewine.emu.fragments.VirtualControllerSettingsFragment;
import com.micewine.emu.input.InputEventSender;
import com.micewine.emu.input.TouchInputHandler;
import com.micewine.emu.views.VirtualControllerInputView;
import com.micewine.emu.views.VirtualKeyboardInputView;

import java.util.HashSet;
import java.util.Set;

public class EmulationActivity extends AppCompatActivity implements View.OnApplyWindowInsetsListener {
    public static Handler handler = new Handler();
    private TouchInputHandler mInputHandler;
    protected ICmdEntryInterface service = null;
    static InputMethodManager inputMethodManager;
    public static boolean externalKeyboardConnected = false;
    private View.OnKeyListener mLorieKeyListener;
    private final SharedPreferences.OnSharedPreferenceChangeListener preferencesChangedListener = (__, key) -> onPreferencesChanged();

    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @SuppressLint("UnspecifiedRegisterReceiverFlag")
        @Override
        public void onReceive(Context context, Intent intent) {
            if (ACTION_START.equals(intent.getAction())) {
                try {
                    Log.v("LorieBroadcastReceiver", "Got new ACTION_START intent");
                    onReceiveConnection(intent);
                } catch (Exception e) {
                    Log.e("MainActivity", "Something went wrong while we extracted connection details from binder.", e);
                }
            }
        }
    };

    @SuppressLint("StaticFieldLeak")
    private static EmulationActivity instance;

    public EmulationActivity() {
        instance = this;
    }

    public static EmulationActivity getInstance() {
        return instance;
    }

    private boolean emulationPaused = false;
    private DrawerLayout drawerLayout;
    private VirtualKeyboardInputView virtualKeyboardInputView;
    private VirtualControllerInputView virtualControllerInputView;

    @Override
    @SuppressLint({"AppCompatMethod", "ObsoleteSdkInt", "ClickableViewAccessibility", "WrongConstant", "UnspecifiedRegisterReceiverFlag", "SetTextI18n"})
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (preferences != null) {
            preferences.registerOnSharedPreferenceChangeListener(preferencesChangedListener);
        }

        initSharedLogs(getSupportFragmentManager());

        new Thread(() -> {
            if (enableCpuCounter) getCpuInfo();
        }).start();

        new Thread(() -> {
            if (enableRamCounter) getMemoryInfo(this);
        }).start();

        new Thread(ControllerUtils::startInputServer).start();

        prepareControllersMappings();

        getWindow().setFlags(FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS | FLAG_KEEP_SCREEN_ON | FLAG_TRANSLUCENT_STATUS, 0);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_emulation);

        drawerLayout = findViewById(R.id.DrawerLayout);
        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);

        AudioManager audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

        transaction.replace(R.id.logViewerContent, new LogViewerFragment());
        transaction.commit();

        virtualKeyboardInputView = findViewById(R.id.overlayView);
        virtualKeyboardInputView.loadPreset(getSelectedVirtualControllerPreset(selectedGameName));
        virtualKeyboardInputView.setVisibility(View.INVISIBLE);

        virtualControllerInputView = findViewById(R.id.xInputOverlayView);
        virtualControllerInputView.setVisibility(View.INVISIBLE);

        NavigationView navigationView = findViewById(R.id.NavigationView);
        View headerViewMain = navigationView.getHeaderView(0);

        MaterialButton exitButton = headerViewMain.findViewById(R.id.exitButton);
        exitButton.setOnClickListener((v) -> {
            WineWrapper.killAll();

            disconnectController(virtualXInputControllerId);
            destroyInputServer();

            finishAffinity();
        });

        MaterialButton openKeyboardButton = headerViewMain.findViewById(R.id.openKeyboardButton);
        openKeyboardButton.setOnClickListener((v) -> {
            getLorieView().requestFocus();
            inputMethodManager.showSoftInput(getLorieView(), 0);
            drawerLayout.closeDrawers();
        });

        MaterialSwitch enableMangoHudSwitch = headerViewMain.findViewById(R.id.enableMangoHudSwitch);
        enableMangoHudSwitch.setChecked(preferences != null ? preferences.getBoolean(ENABLE_MANGOHUD, ENABLE_MANGOHUD_DEFAULT_VALUE) : ENABLE_MANGOHUD_DEFAULT_VALUE);
        enableMangoHudSwitch.setOnClickListener((v) -> {
            SharedPreferences.Editor editor = preferences.edit();
            editor.putBoolean(ENABLE_MANGOHUD, enableMangoHudSwitch.isChecked());
            editor.apply();

            setSharedVars(this);
            generateMangoHUDConfFile();
        });

        MaterialSwitch stretchDisplaySwitch = headerViewMain.findViewById(R.id.stretchDisplaySwitch);
        stretchDisplaySwitch.setChecked(preferences != null && preferences.getBoolean("displayStretch", false));
        stretchDisplaySwitch.setOnClickListener((v) -> {
            SharedPreferences.Editor editor = preferences.edit();
            editor.putBoolean("displayStretch", stretchDisplaySwitch.isChecked());
            editor.apply();

            getLorieView().requestLayout();
        });

        MaterialSwitch openCloseVirtualControllerSwitch = headerViewMain.findViewById(R.id.openCloseVirtualControllerSwitch);
        openCloseVirtualControllerSwitch.setOnClickListener((v) -> {
            boolean isControllerInput = getVirtualControllerXInput(selectedGameName);

            if (openCloseVirtualControllerSwitch.isChecked()) {
                if (isControllerInput) {
                    virtualKeyboardInputView.setVisibility(View.INVISIBLE);
                    virtualControllerInputView.setVisibility(View.VISIBLE);

                    if (virtualXInputControllerId == -1) {
                        virtualXInputControllerId = connectController();
                    }
                } else {
                    virtualKeyboardInputView.setVisibility(View.VISIBLE);
                    virtualControllerInputView.setVisibility(View.INVISIBLE);
                }
            } else {
                virtualKeyboardInputView.setVisibility(View.INVISIBLE);
                virtualControllerInputView.setVisibility(View.INVISIBLE);

                if (isControllerInput) {
                    disconnectController(virtualXInputControllerId);
                    virtualXInputControllerId = -1;
                }
            }
        });

        MaterialButton openLogView = headerViewMain.findViewById(R.id.openLogViewer);
        openLogView.setOnClickListener((v) -> {
            drawerLayout.openDrawer(GravityCompat.END);
            drawerLayout.closeDrawer(GravityCompat.START);
        });

        MaterialButton editVirtualControllerMappingButton = headerViewMain.findViewById(R.id.editVirtualControllerMapping);
        editVirtualControllerMappingButton.setOnClickListener((v) -> new VirtualControllerSettingsFragment().show(getSupportFragmentManager(), ""));

        MaterialButton editControllerMappingButton = headerViewMain.findViewById(R.id.editControllerMapping);
        editControllerMappingButton.setOnClickListener((v) -> new ControllerSettingsFragment().show(getSupportFragmentManager(), ""));

        MaterialButton startControllerPresetManagerButton = headerViewMain.findViewById(R.id.startControllerPresetManager);
        startControllerPresetManagerButton.setOnClickListener((v) -> {
            Intent intent = new Intent(this, PresetManagerActivity.class);
            intent.putExtra("presetType", CONTROLLER_PRESET);
            startActivity(intent);
        });

        MaterialButton startVirtualControllerPresetManagerButton = headerViewMain.findViewById(R.id.startVirtualControllerPresetManager);
        startVirtualControllerPresetManagerButton.setOnClickListener((v) -> {
            Intent intent = new Intent(this, PresetManagerActivity.class);
            intent.putExtra("presetType", VIRTUAL_CONTROLLER_PRESET);
            startActivity(intent);
        });

        MaterialButton pauseEmulationButton = headerViewMain.findViewById(R.id.pauseEmulation);
        pauseEmulationButton.setOnClickListener((v) -> {
            if (emulationPaused) {
                runCommand("pkill -SIGCONT -f .exe", false);
                pauseEmulationButton.setText(R.string.pause_emulation);
                pauseEmulationButton.setIcon(AppCompatResources.getDrawable(this, android.R.drawable.ic_media_pause));
            } else {
                runCommand("pkill -SIGSTOP -f .exe", false);
                pauseEmulationButton.setText(R.string.continue_emulation);
                pauseEmulationButton.setIcon(AppCompatResources.getDrawable(this, android.R.drawable.ic_media_play));
            }
            emulationPaused = !emulationPaused;
        });

        MaterialButton openTaskMgrButton = headerViewMain.findViewById(R.id.openTaskMgr);
        openTaskMgrButton.setOnClickListener((v) -> new TaskManagerFragment().show(getSupportFragmentManager(), ""));

        TextView fpsLimitText = headerViewMain.findViewById(R.id.fpsLimitText);
        SeekBar fpsLimitSeekbar = headerViewMain.findViewById(R.id.fpsLimitSeekbar);

        fpsLimitSeekbar.setMin(0);
        fpsLimitSeekbar.setMax(screenFpsLimit);
        fpsLimitSeekbar.setProgress(preferences != null ? preferences.getInt(FPS_LIMIT, screenFpsLimit) : 0);

        if (fpsLimitSeekbar.getProgress() == 0) {
            fpsLimitText.setText(R.string.unlimited);
        } else {
            fpsLimitText.setText(fpsLimitSeekbar.getProgress() + " FPS");
        }

        fpsLimitSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if (fpsLimitSeekbar.getProgress() == 0) {
                    fpsLimitText.setText(R.string.unlimited);
                } else {
                    fpsLimitText.setText(fpsLimitSeekbar.getProgress() + " FPS");
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                SharedPreferences.Editor editor = preferences.edit();

                editor.putInt(FPS_LIMIT, seekBar.getProgress());
                editor.apply();

                setSharedVars(EmulationActivity.this);
                generateMangoHUDConfFile();
            }
        });

        LorieView lorieView = findViewById(R.id.lorieView);
        View lorieParent = (View) lorieView.getParent();

        Set<Integer> pressedKeys = new HashSet<>();
        mInputHandler = new TouchInputHandler(this, new InputEventSender(lorieView));
        mLorieKeyListener = (v, k, e) -> {
            if (e.getAction() == KeyEvent.ACTION_DOWN) {
                pressedKeys.add(k);
            } else if (e.getAction() == KeyEvent.ACTION_UP) {
                pressedKeys.remove(k);
            }

            if (pressedKeys.contains(KeyEvent.KEYCODE_ALT_LEFT) && pressedKeys.contains(KeyEvent.KEYCODE_Q)) {
                if (lorieView.hasPointerCapture()) {
                    lorieView.releasePointerCapture();
                }
            }

            if (k == KeyEvent.KEYCODE_ESCAPE && !(lorieView.hasPointerCapture())) {
                if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    drawerLayout.openDrawer(GravityCompat.START);
                } else {
                    drawerLayout.closeDrawers();
                }
            }

            if (k == KeyEvent.KEYCODE_BACK) {
                if (e.getScanCode() == 153 && e.getDevice().getKeyboardType() != InputDevice.KEYBOARD_TYPE_ALPHABETIC || e.getScanCode() == 0) {
                    boolean pointerCaptured = lorieView.hasPointerCapture();
                    if (pointerCaptured) {
                        lorieView.releasePointerCapture();
                    }
                    if (e.getAction() == KeyEvent.ACTION_UP) {
                        if (!drawerLayout.isDrawerOpen(GravityCompat.START)) {
                            drawerLayout.openDrawer(GravityCompat.START);
                        } else {
                            drawerLayout.closeDrawers();
                        }
                    }

                    inputMethodManager.hideSoftInputFromWindow(getWindow().getDecorView().getWindowToken(), 0);

                    return true;
                }
            } else if (k == KeyEvent.KEYCODE_VOLUME_DOWN) {
                audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_LOWER, AudioManager.FLAG_SHOW_UI);
                return true;
            } else if (k == KeyEvent.KEYCODE_VOLUME_UP) {
                audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_RAISE, AudioManager.FLAG_SHOW_UI);
                return true;
            }

            updateButtonsState(e);

            return mInputHandler.sendKeyEvent(e);
        };

        lorieParent.setOnTouchListener((v, e) -> {
            // Avoid batched MotionEvent objects and reduce potential latency.
            // For reference: https://developer.android.com/develop/ui/views/touch-and-input/stylus-input/advanced-stylus-features#rendering.
            if (e.getAction() == MotionEvent.ACTION_DOWN) {
                lorieParent.requestUnbufferedDispatch(e);
            }

            switch (e.getButtonState()) {
                case MotionEvent.BUTTON_PRIMARY: {
                    lorieView.requestPointerCapture();
                    Toast.makeText(this, getString(R.string.mouse_captured), Toast.LENGTH_SHORT).show();
                    break;
                }
                case MotionEvent.BUTTON_SECONDARY: {
                    if (!lorieView.hasPointerCapture()) {
                        if (!drawerLayout.isDrawerOpen(GravityCompat.START)) {
                            drawerLayout.openDrawer(GravityCompat.START);
                        } else {
                            drawerLayout.closeDrawers();
                        }

                        inputMethodManager.hideSoftInputFromWindow(getWindow().getDecorView().getWindowToken(), 0);

                        return true;
                    }
                }
            }

            return mInputHandler.handleTouchEvent(lorieParent, lorieView, e);
        });
        lorieParent.setOnHoverListener((v, e) -> mInputHandler.handleTouchEvent(lorieParent, lorieView, e));
        lorieParent.setOnGenericMotionListener((v, e) -> mInputHandler.handleTouchEvent(lorieParent, lorieView, e));
        lorieView.setOnCapturedPointerListener((v, e) -> mInputHandler.handleTouchEvent(lorieView, lorieView, e));
        lorieParent.setOnCapturedPointerListener((v, e) -> mInputHandler.handleTouchEvent(lorieView, lorieView, e));
        lorieView.setOnKeyListener(mLorieKeyListener);

        lorieView.setCallback((sfc, surfaceWidth, surfaceHeight, screenWidth, screenHeight) -> {
            String name;
            int frameRate = (int) ((lorieView.getDisplay() != null) ? lorieView.getDisplay().getRefreshRate() : 30);

            mInputHandler.handleHostSizeChanged(surfaceWidth, surfaceHeight);
            mInputHandler.handleClientSizeChanged(screenWidth, screenHeight);
            if (lorieView.getDisplay() == null || lorieView.getDisplay().getDisplayId() == Display.DEFAULT_DISPLAY) {
                name = "Builtin Display";
            } else {
                name = "External Display";
            }
            LorieView.sendWindowChange(screenWidth, screenHeight, frameRate, name);

            if (service != null && !LorieView.renderingInActivity()) {
                try {
                    service.windowChanged(sfc);
                } catch (RemoteException e) {
                    Log.e("EmulationActivity", "failed to send windowChanged request", e);
                }
            }
        });

        lorieView.setOnFocusChangeListener((view, b) -> {
            if (!lorieView.isInLayout()) {
                lorieView.requestLayout();
            }
        });

        registerReceiver(receiver, new IntentFilter(ACTION_START), SDK_INT >= VERSION_CODES.TIRAMISU ? RECEIVER_EXPORTED : 0);

        inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

        tryConnect();
        onPreferencesChanged();

        if (SDK_INT >= VERSION_CODES.TIRAMISU
                && checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PERMISSION_GRANTED
                && !shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)) {
            requestPermissions(new String[] { Manifest.permission.POST_NOTIFICATIONS }, 0);
        }

        onReceiveConnection(getIntent());

        getSupportFragmentManager().setFragmentResultListener("invalidateControllerType", this, (requestKey, result) -> {
            boolean isControllerInput = getVirtualControllerXInput(selectedGameName);

            virtualKeyboardInputView.loadPreset(getSelectedVirtualControllerPreset(selectedGameName));
            virtualKeyboardInputView.invalidate();

            if (openCloseVirtualControllerSwitch.isChecked()) {
                if (isControllerInput) {
                    virtualKeyboardInputView.setVisibility(View.INVISIBLE);
                    virtualControllerInputView.setVisibility(View.VISIBLE);
                } else {
                    virtualKeyboardInputView.setVisibility(View.VISIBLE);
                    virtualControllerInputView.setVisibility(View.INVISIBLE);
                }
            }
        });
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        getLorieView().requestFocus();

        mLorieKeyListener.onKey(null, keyCode, event);

        return true;
    }

    @Override
    public boolean onGenericMotionEvent(MotionEvent event) {
        if (event != null) {
            updateAxisState(event);
        }
        return true;
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(receiver);
        super.onDestroy();
    }

    public void onReceiveConnection(Intent intent) {
        Bundle bundle = intent.getBundleExtra(null);
        if (bundle == null) return;
        IBinder ibinder = bundle.getBinder(null);
        if (ibinder == null) return;

        service = ICmdEntryInterface.Stub.asInterface(ibinder);
        try {
            service.asBinder().linkToDeath(() -> {
                service = null;

                Log.v("Lorie", "Disconnected");
                runOnUiThread(() -> { LorieView.connect(-1); clientConnectedStateChanged();} );
            }, 0);
        } catch (RemoteException ignored) {}

        try {
            if (service != null && service.asBinder().isBinderAlive()) {
                Log.v("LorieBroadcastReceiver", "Extracting logcat fd.");
                ParcelFileDescriptor logcatOutput = service.getLogcatOutput();
                if (logcatOutput != null)
                    LorieView.startLogcat(logcatOutput.detachFd());

                tryConnect();

                if (intent != getIntent())
                    getIntent().putExtra(null, bundle);
            }
        } catch (Exception e) {
            Log.e("EmulationActivity", "Something went wrong while we were establishing connection", e);
        }
    }

    public void tryConnect() {
        if (LorieView.connected())
            return;

        if (service == null) {
            LorieView.requestConnection();
            handler.postDelayed(this::tryConnect, 250);
            return;
        }

        try {
            ParcelFileDescriptor fd = service.getXConnection();
            if (fd != null) {
                Log.v("MainActivity", "Extracting X connection socket.");
                LorieView.connect(fd.detachFd());
                getLorieView().triggerCallback();
                clientConnectedStateChanged();
            } else
                handler.postDelayed(this::tryConnect, 250);
        } catch (Exception e) {
            Log.e("MainActivity", "Something went wrong while we were establishing connection", e);
            service = null;

            // We should reset the View for the case if we have sent it's surface to the client.
            getLorieView().regenerate();
            handler.postDelayed(this::tryConnect, 250);
        }
    }

    public void onPreferencesChanged() {
        handler.removeCallbacks(this::onPreferencesChangedCallback);
        handler.postDelayed(this::onPreferencesChangedCallback, 100);
    }

    @SuppressLint("UnsafeIntentLaunch")
    public void onPreferencesChangedCallback() {
        onWindowFocusChanged(hasWindowFocus());
        LorieView lorieView = getLorieView();

        lorieView.triggerCallback();

        lorieView.requestLayout();
        lorieView.invalidate();
    }

    @Override
    public void onResume() {
        super.onResume();

        getLorieView().requestFocus();

        prepareControllersMappings();
    }

    @Override
    public void onPause() {
        inputMethodManager.hideSoftInputFromWindow(getWindow().getDecorView().getRootView().getWindowToken(), 0);

        super.onPause();
    }

    public LorieView getLorieView() {
        return findViewById(R.id.lorieView);
    }

    public boolean handleKey(KeyEvent e) {
        return mLorieKeyListener.onKey(getLorieView(), e.getKeyCode(), e);
    }
    @SuppressLint("WrongConstant")
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

        if (hasFocus) {
            getLorieView().regenerate();
        }

        getLorieView().requestFocus();
    }

    @NonNull
    @Override
    public WindowInsets onApplyWindowInsets(@NonNull View v, @NonNull WindowInsets insets) {
        handler.postDelayed(() -> getLorieView().triggerCallback(), 100);
        return insets;
    }

    private void clientConnectedStateChanged() {
        runOnUiThread(()-> {
            boolean connected = LorieView.connected();

            getLorieView().setVisibility(connected ? View.VISIBLE : View.INVISIBLE);
            getLorieView().regenerate();

            // We should recover connection in the case if file descriptor for some reason was broken...
            if (!connected)
                tryConnect();
        });
    }

    public static ShellLoader.ViewModelAppLogs sharedLogs;
    public static void appendLogs(String text) {
        if (sharedLogs != null)
            sharedLogs.appendText(text);
    }
    private static void initSharedLogs(FragmentManager supportFragmentManager) {
        sharedLogs = new ShellLoader.ViewModelAppLogs(supportFragmentManager);
    }
}
