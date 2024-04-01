package com.micewine.emu;

import static android.Manifest.permission.WRITE_SECURE_SETTINGS;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;
import static android.os.Build.VERSION.SDK_INT;
import static android.view.InputDevice.KEYBOARD_TYPE_ALPHABETIC;
import static android.view.KeyEvent.ACTION_UP;
import static android.view.KeyEvent.KEYCODE_BACK;
import static android.view.WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS;
import static android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN;
import static android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
import static android.view.WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS;
import static android.view.WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_DEFAULT;
import static android.view.WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN;
import static android.view.WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN;
import static com.micewine.emu.CmdEntryPoint.ACTION_START;
import static com.micewine.emu.LoriePreferences.ACTION_PREFERENCES_CHANGED;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.FragmentManager;
import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.ParcelFileDescriptor;
import android.os.RemoteException;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.DragEvent;
import android.view.InputDevice;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.PointerIcon;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowInsets;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.math.MathUtils;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;
import com.micewine.emu.EntryPoint.Init;
import com.micewine.emu.activities.MainActivity;
import com.micewine.emu.input.InputEventSender;
import com.micewine.emu.input.InputStub;
import com.micewine.emu.input.TouchInputHandler;
import com.micewine.emu.input.TouchInputHandler.RenderStub;
import com.micewine.emu.utils.FullscreenWorkaround;
import com.micewine.emu.utils.KeyInterceptor;

import java.util.Map;
import java.util.Objects;

@SuppressLint("ApplySharedPref")
@SuppressWarnings({"deprecation", "unused"})
public class EmulationActivity extends AppCompatActivity implements View.OnApplyWindowInsetsListener {
    static final String ACTION_STOP = "com.micewine.emu.ACTION_STOP";
    static final String REQUEST_LAUNCH_EXTERNAL_DISPLAY = "request_launch_external_display";
    private static final int KEY_BACK = 158;
    public static Handler handler = new Handler();
    @SuppressLint("StaticFieldLeak")
    private static EmulationActivity instance;
    FrameLayout frm;
    int orientation;
    private TouchInputHandler mInputHandler;
    private ICmdEntryInterface service = null;
    private boolean mClientConnected = false;
    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @SuppressLint("UnspecifiedRegisterReceiverFlag")
        @Override
        public void onReceive(Context context, Intent intent) {
            if (ACTION_START.equals(intent.getAction())) {
                try {
                    Log.v("LorieBroadcastReceiver", "Got new ACTION_START intent");
                    IBinder b = Objects.requireNonNull(intent.getBundleExtra("")).getBinder("");
                    service = ICmdEntryInterface.Stub.asInterface(b);
                    Objects.requireNonNull(service).asBinder().linkToDeath(() -> {
                        service = null;
                        CmdEntryPoint.requestConnection();

                        Log.v("Lorie", "Disconnected");
                        runOnUiThread(() -> clientConnectedStateChanged(false)); //recreate()); //onPreferencesChanged(""));
                    }, 0);

                    onReceiveConnection();
                } catch (Exception e) {
                    Log.e("MainActivity", "Something went wrong while we extracted connection details from binder.", e);
                }
            } else if (ACTION_STOP.equals(intent.getAction())) {
                finishAffinity();
            } else if (ACTION_PREFERENCES_CHANGED.equals(intent.getAction())) {
                Log.d("MainActivity", "preference: " + intent.getStringExtra("key"));
                if (!"additionalKbdVisible".equals(intent.getStringExtra("key")))
                    onPreferencesChanged("");
            }
        }
    };
    private View.OnKeyListener mLorieKeyListener;
    private Thread overlayThread;
    private Init init;
    private FragmentManager fragmentManager;
    private DrawerLayout drawerLayout;

    public EmulationActivity() {
        instance = this;
    }

    public static EmulationActivity getInstance() {
        return instance;
    }

    @Override
    @SuppressLint({"AppCompatMethod", "ObsoleteSdkInt", "ClickableViewAccessibility", "WrongConstant", "UnspecifiedRegisterReceiverFlag"})
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        int modeValue = 0;

        preferences.registerOnSharedPreferenceChangeListener((sharedPreferences, key) -> onPreferencesChanged(key));

        init = new Init();
        getWindow().setFlags(FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS | FLAG_KEEP_SCREEN_ON | FLAG_TRANSLUCENT_STATUS, 0);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.main_activity);
        frm = findViewById(R.id.frame);
        drawerLayout = findViewById(R.id.DrawerLayout);
        LorieView lorieView = findViewById(R.id.lorieView);
        View lorieParent = (View) lorieView.getParent();

        NavigationView nav = findViewById(R.id.NavigationView);

        nav.setNavigationItemSelectedListener((item) -> {
            int id = item.getItemId();
            if (id == R.id.exit_fromXserver) {
                Intent i = new Intent(this, MainActivity.class);
                startActivity(i);
            } else if (id == R.id.openKeyboard) {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
                drawerLayout.closeDrawers();
            }
            return true;
        });

        mInputHandler = new TouchInputHandler(this, new RenderStub.NullStub() {
            @Override
            public void swipeDown() {
            }
        }, new InputEventSender(lorieView));
        mLorieKeyListener = (v, k, e) -> {

            if (k == KEYCODE_BACK) {
                if (e.isFromSource(InputDevice.SOURCE_MOUSE) || e.isFromSource(InputDevice.SOURCE_MOUSE_RELATIVE)) {
                    if (e.getRepeatCount() != 0) // ignore auto-repeat
                        return true;
                    if (e.getAction() == KeyEvent.ACTION_UP || e.getAction() == KeyEvent.ACTION_DOWN)
                        lorieView.sendMouseEvent(-1, -1, InputStub.BUTTON_RIGHT, e.getAction() == KeyEvent.ACTION_DOWN, true);
                    return true;
                }

                if (e.getScanCode() == KEY_BACK && e.getDevice().getKeyboardType() != KEYBOARD_TYPE_ALPHABETIC || e.getScanCode() == 0) {
                    if (e.getAction() == ACTION_UP)
                        if (!drawerLayout.isDrawerOpen(GravityCompat.START)) {
                            drawerLayout.openDrawer(GravityCompat.START);
                        } else {
                            drawerLayout.closeDrawers();
                        };
                    return true;
                }
            }

            return mInputHandler.sendKeyEvent(v, e);
        };

        lorieParent.setOnTouchListener((v, e) -> mInputHandler.handleTouchEvent(lorieParent, lorieView, e));
        lorieParent.setOnHoverListener((v, e) -> mInputHandler.handleTouchEvent(lorieParent, lorieView, e));
        lorieParent.setOnGenericMotionListener((v, e) -> mInputHandler.handleTouchEvent(lorieParent, lorieView, e));
        lorieView.setOnCapturedPointerListener((v, e) -> mInputHandler.handleTouchEvent(lorieView, lorieView, e));
        lorieParent.setOnCapturedPointerListener((v, e) -> mInputHandler.handleTouchEvent(lorieView, lorieView, e));
        lorieView.setOnKeyListener(mLorieKeyListener);

        lorieView.setCallback((sfc, surfaceWidth, surfaceHeight, screenWidth, screenHeight) -> {
            int framerate = (int) ((lorieView.getDisplay() != null) ? lorieView.getDisplay().getRefreshRate() : 30);

            mInputHandler.handleHostSizeChanged(surfaceWidth, surfaceHeight);
            mInputHandler.handleClientSizeChanged(screenWidth, screenHeight);
            LorieView.sendWindowChange(screenWidth, screenHeight, framerate);

            if (service !=
                    null) {
                try {
                    service.windowChanged(sfc);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        });

        registerReceiver(receiver, new IntentFilter(ACTION_START) {{
            addAction(ACTION_PREFERENCES_CHANGED);
            addAction(ACTION_STOP);
        }});

        // Taken from Stackoverflow answer https://stackoverflow.com/questions/7417123/android-how-to-adjust-layout-in-full-screen-mode-when-softkeyboard-is-visible/7509285#
        FullscreenWorkaround.assistActivity(this);
        CmdEntryPoint.requestConnection();
        onPreferencesChanged("");
        checkXEvents();
        initStylusAuxButtons();
        initMouseAuxButtons();

        Log.v("?", "Asa delta");
        init.run(this);

        if (SDK_INT >= VERSION_CODES.TIRAMISU
                && checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PERMISSION_GRANTED
                && !shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)) {
            requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, 0);
        }
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(receiver);
        init.stopAll();
        super.onDestroy();
    }

    //Register the needed events to handle stylus as left, middle and right click
    @SuppressLint("ClickableViewAccessibility")
    private void initStylusAuxButtons() {
        SharedPreferences p = PreferenceManager.getDefaultSharedPreferences(this);
        boolean stylusMenuEnabled = false;
        final float menuUnselectedTrasparency = 0.66f;
        final float menuSelectedTrasparency = 1.0f;
        Button left = findViewById(R.id.button_left_click);
        Button right = findViewById(R.id.button_right_click);
        Button middle = findViewById(R.id.button_middle_click);
        Button visibility = findViewById(R.id.button_visibility);
        LinearLayout overlay = findViewById(R.id.mouse_helper_visibility);
        LinearLayout buttons = findViewById(R.id.mouse_helper_secondary_layer);
        overlay.setOnTouchListener((v, e) -> true);
        overlay.setOnHoverListener((v, e) -> true);
        overlay.setOnGenericMotionListener((v, e) -> true);
        overlay.setOnCapturedPointerListener((v, e) -> true);
        overlay.setVisibility(View.GONE);
        View.OnClickListener listener = view -> {
            TouchInputHandler.STYLUS_INPUT_HELPER_MODE = (view.equals(left) ? 1 : (view.equals(middle) ? 2 : (view.equals(right) ? 3 : 0)));
            left.setAlpha((TouchInputHandler.STYLUS_INPUT_HELPER_MODE == 1) ? menuSelectedTrasparency : menuUnselectedTrasparency);
            middle.setAlpha((TouchInputHandler.STYLUS_INPUT_HELPER_MODE == 2) ? menuSelectedTrasparency : menuUnselectedTrasparency);
            right.setAlpha((TouchInputHandler.STYLUS_INPUT_HELPER_MODE == 3) ? menuSelectedTrasparency : menuUnselectedTrasparency);
            visibility.setAlpha(menuUnselectedTrasparency);
        };

        left.setOnClickListener(listener);
        middle.setOnClickListener(listener);
        right.setOnClickListener(listener);

        visibility.setOnClickListener(view -> {
            if (buttons.getVisibility() == View.VISIBLE) {
                buttons.setVisibility(View.GONE);
                visibility.setAlpha(menuUnselectedTrasparency);
                int m = TouchInputHandler.STYLUS_INPUT_HELPER_MODE;
                visibility.setText(m == 1 ? "L" : (m == 2 ? "M" : (m == 3 ? "R" : "U")));
            } else {
                buttons.setVisibility(View.VISIBLE);
                visibility.setAlpha(menuUnselectedTrasparency);
                visibility.setText("X");

                //Calculate screen border making sure btn is fully inside the view
                float maxX = frm.getWidth() - 4 * left.getWidth();
                float maxY = frm.getHeight() - 4 * left.getHeight();

                //Make sure the Stylus menu is fully inside the screen
                overlay.setX(MathUtils.clamp(overlay.getX(), 0, maxX));
                overlay.setY(MathUtils.clamp(overlay.getY(), 0, maxY));

                int m = TouchInputHandler.STYLUS_INPUT_HELPER_MODE;
                listener.onClick(m == 1 ? left : (m == 2 ? middle : (m == 3 ? right : left)));
            }
        });
        //Simulated mouse click 1 = left , 2 = middle , 3 = right
        TouchInputHandler.STYLUS_INPUT_HELPER_MODE = 1;
        listener.onClick(left);

        visibility.setOnLongClickListener(v -> {
            v.startDragAndDrop(ClipData.newPlainText("", ""), new View.DragShadowBuilder(visibility) {
                public void onDrawShadow(Canvas canvas) {
                }
            }, null, View.DRAG_FLAG_GLOBAL);

            frm.setOnDragListener((v2, event) -> {
                //Calculate screen border making sure btn is fully inside the view
                float maxX = frm.getWidth() - visibility.getWidth();
                float maxY = frm.getHeight() - visibility.getHeight();

                switch (event.getAction()) {
                    case DragEvent.ACTION_DRAG_LOCATION:
                        //Center touch location with btn icon
                        float dX = event.getX() - visibility.getWidth() / 2.0f;
                        float dY = event.getY() - visibility.getHeight() / 2.0f;

                        //Make sure the dragged btn is inside the view with clamp
                        overlay.setX(MathUtils.clamp(dX, 0, maxX));
                        overlay.setY(MathUtils.clamp(dY, 0, maxY));
                        break;
                    case DragEvent.ACTION_DRAG_ENDED:
                        //Make sure the dragged btn is inside the view
                        overlay.setX(MathUtils.clamp(overlay.getX(), 0, maxX));
                        overlay.setY(MathUtils.clamp(overlay.getY(), 0, maxY));
                        break;
                }
                return true;
            });

            return true;
        });
    }

    void setSize(View v, int width, int height) {
        ViewGroup.LayoutParams p = v.getLayoutParams();
        p.width = (int) (width * getResources().getDisplayMetrics().density);
        p.height = (int) (height * getResources().getDisplayMetrics().density);
        v.setLayoutParams(p);
        v.setMinimumWidth((int) (width * getResources().getDisplayMetrics().density));
        v.setMinimumHeight((int) (height * getResources().getDisplayMetrics().density));
    }

    @SuppressLint("ClickableViewAccessibility")
    void initMouseAuxButtons() {
        Button left = findViewById(R.id.mouse_button_left_click);
        Button right = findViewById(R.id.mouse_button_right_click);
        Button middle = findViewById(R.id.mouse_button_middle_click);
        ImageButton pos = findViewById(R.id.mouse_buttons_position);
        LinearLayout primaryLayer = findViewById(R.id.mouse_buttons);
        LinearLayout secondaryLayer = findViewById(R.id.mouse_buttons_secondary_layer);

        SharedPreferences p = PreferenceManager.getDefaultSharedPreferences(this);
        boolean mouseHelperEnabled = false;
        primaryLayer.setVisibility(View.GONE);

        pos.setOnClickListener((v) -> {
            if (secondaryLayer.getOrientation() == LinearLayout.HORIZONTAL) {
                setSize(left, 48, 96);
                setSize(right, 48, 96);
                secondaryLayer.setOrientation(LinearLayout.VERTICAL);
            } else {
                setSize(left, 96, 48);
                setSize(right, 96, 48);
                secondaryLayer.setOrientation(LinearLayout.HORIZONTAL);
            }
            handler.postDelayed(() -> {
                int[] offset = new int[2];
                frm.getLocationOnScreen(offset);
                primaryLayer.setX(MathUtils.clamp(primaryLayer.getX(), offset[0], offset[0] + frm.getWidth() - primaryLayer.getWidth()));
                primaryLayer.setY(MathUtils.clamp(primaryLayer.getY(), offset[1], offset[1] + frm.getHeight() - primaryLayer.getHeight()));
            }, 10);
        });

        Map.of(left, InputStub.BUTTON_LEFT, middle, InputStub.BUTTON_MIDDLE, right, InputStub.BUTTON_RIGHT)
                .forEach((v, b) -> v.setOnTouchListener((__, e) -> {
                    switch (e.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                        case MotionEvent.ACTION_POINTER_DOWN:
                            getLorieView().sendMouseEvent(0, 0, b, true, true);
                            v.setPressed(true);
                            break;
                        case MotionEvent.ACTION_UP:
                        case MotionEvent.ACTION_POINTER_UP:
                            getLorieView().sendMouseEvent(0, 0, b, false, true);
                            v.setPressed(false);
                            break;
                    }
                    return true;
                }));

        pos.setOnTouchListener(new View.OnTouchListener() {
            final int touchSlop = (int) Math.pow(ViewConfiguration.get(EmulationActivity.this).getScaledTouchSlop(), 2);
            final int tapTimeout = ViewConfiguration.getTapTimeout();
            final float[] startOffset = new float[2];
            final int[] startPosition = new int[2];
            long startTime;

            @Override
            public boolean onTouch(View v, MotionEvent e) {
                switch (e.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        primaryLayer.getLocationOnScreen(startPosition);
                        startOffset[0] = e.getX();
                        startOffset[1] = e.getY();
                        startTime = SystemClock.uptimeMillis();
                        pos.setPressed(true);
                        break;
                    case MotionEvent.ACTION_MOVE: {
                        int[] offset = new int[2];
                        int[] offset2 = new int[2];
                        primaryLayer.getLocationOnScreen(offset);
                        frm.getLocationOnScreen(offset2);
                        primaryLayer.setX(MathUtils.clamp(offset[0] - startOffset[0] + e.getX(), offset2[0], offset2[0] + frm.getWidth() - primaryLayer.getWidth()));
                        primaryLayer.setY(MathUtils.clamp(offset[1] - startOffset[1] + e.getY(), offset2[1], offset2[1] + frm.getHeight() - primaryLayer.getHeight()));
                        break;
                    }
                    case MotionEvent.ACTION_UP: {
                        final int[] _pos = new int[2];
                        primaryLayer.getLocationOnScreen(_pos);
                        int deltaX = (int) (startOffset[0] - e.getX()) + (startPosition[0] - _pos[0]);
                        int deltaY = (int) (startOffset[1] - e.getY()) + (startPosition[1] - _pos[1]);
                        pos.setPressed(false);

                        if (deltaX * deltaX + deltaY * deltaY < touchSlop && SystemClock.uptimeMillis() - startTime <= tapTimeout) {
                            v.performClick();
                            return true;
                        }
                        break;
                    }
                }
                return true;
            }
        });
    }

    void onReceiveConnection() {
        try {
            if (service != null && service.asBinder().isBinderAlive()) {
                Log.v("LorieBroadcastReceiver", "Extracting logcat fd.");
                ParcelFileDescriptor logcatOutput = service.getLogcatOutput();
                if (logcatOutput != null)
                    LorieView.startLogcat(logcatOutput.detachFd());

                tryConnect();
            }
        } catch (Exception e) {
            Log.e("MainActivity", "Something went wrong while we were establishing connection", e);
        }
    }

    void tryConnect() {
        if (mClientConnected)
            return;
        try {
            Log.v("LorieBroadcastReceiver", "Extracting X connection socket.");
            ParcelFileDescriptor fd = service == null ? null : service.getXConnection();
            if (fd != null) {
                LorieView.connect(fd.detachFd());
                getLorieView().triggerCallback();
                clientConnectedStateChanged(true);
                LorieView.setClipboardSyncEnabled(false);
            } else
                handler.postDelayed(this::tryConnect, 500);
        } catch (Exception e) {
            Log.e("MainActivity", "Something went wrong while we were establishing connection", e);
            service = null;

            // We should reset the View for the case if we have sent it's surface to the client.
            getLorieView().regenerate();
        }
    }

    @SuppressLint("WrongConstant")
    void onPreferencesChanged(String key) {
        if ("additionalKbdVisible".equals(key))
            return;

        SharedPreferences p = PreferenceManager.getDefaultSharedPreferences(this);
        LorieView lorieView = getLorieView();

        int mode = 1;
        mInputHandler.setInputMode(mode);
        mInputHandler.setTapToMove(false);
        mInputHandler.setPreferScancodes(false);
        mInputHandler.setPointerCaptureEnabled(false);
        if (!p.getBoolean("pointerCapture", false) && lorieView.hasPointerCapture())
            lorieView.releasePointerCapture();

        //SamsungDexUtils.dexMetaKeyCapture(this, p.getBoolean("dexMetaKeyCapture", false));

        onWindowFocusChanged(true);
        LorieView.setClipboardSyncEnabled(false);

        lorieView.triggerCallback();

        boolean filterOutWinKey = false;

        if (checkSelfPermission(WRITE_SECURE_SETTINGS) == PERMISSION_GRANTED)
            KeyInterceptor.shutdown();

        int requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE;
        if (getRequestedOrientation() != requestedOrientation)
            setRequestedOrientation(requestedOrientation);

        LinearLayout buttons = findViewById(R.id.mouse_helper_visibility);

        //Reset default input back to normal
        TouchInputHandler.STYLUS_INPUT_HELPER_MODE = 1;
        final float menuUnselectedTrasparency = 0.66f;
        final float menuSelectedTrasparency = 1.0f;
        findViewById(R.id.button_left_click).setAlpha(menuSelectedTrasparency);
        findViewById(R.id.button_right_click).setAlpha(menuUnselectedTrasparency);
        findViewById(R.id.button_middle_click).setAlpha(menuUnselectedTrasparency);
        findViewById(R.id.button_visibility).setAlpha(menuUnselectedTrasparency);
        buttons.setVisibility(View.GONE);
    }

    @Override
    public void onResume() {
        super.onResume();
        getLorieView().requestFocus();
    }

    @Override
    public void onPause() {
        super.onPause();
        init.stopAll();
    }

    public LorieView getLorieView() {
        return findViewById(R.id.lorieView);
    }

    public boolean handleKey(KeyEvent e) {
        mLorieKeyListener.onKey(getLorieView(), e.getKeyCode(), e);
        return true;
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        if (newConfig.orientation != orientation) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
            View view = getCurrentFocus();
            if (view == null) {
                view = getLorieView();
                view.requestFocus();
            }
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }

        orientation = newConfig.orientation;
    }

    @SuppressLint("WrongConstant")
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        SharedPreferences p = PreferenceManager.getDefaultSharedPreferences(this);
        Window window = getWindow();
        View decorView = window.getDecorView();
        boolean fullscreen = true;
        boolean reseed = false;

        int requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE;
        if (getRequestedOrientation() != requestedOrientation)
            setRequestedOrientation(requestedOrientation);

        if (hasFocus) {
            if (SDK_INT >= VERSION_CODES.P) {
                getWindow().getAttributes().layoutInDisplayCutoutMode = LAYOUT_IN_DISPLAY_CUTOUT_MODE_DEFAULT;
            }

            window.setStatusBarColor(Color.BLACK);
            window.setNavigationBarColor(Color.BLACK);
        }

        window.setFlags(FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS | FLAG_KEEP_SCREEN_ON | FLAG_TRANSLUCENT_STATUS, 0);
        if (hasFocus) {
            window.addFlags(FLAG_FULLSCREEN);
            decorView.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }

        window.addFlags(FLAG_KEEP_SCREEN_ON);

        window.setSoftInputMode(SOFT_INPUT_ADJUST_PAN | SOFT_INPUT_STATE_HIDDEN);

        ((FrameLayout) findViewById(android.R.id.content)).getChildAt(0).setFitsSystemWindows(false);
        //SamsungDexUtils.dexMetaKeyCapture(this, hasFocus && p.getBoolean("dexMetaKeyCapture", false));

        if (hasFocus)
            getLorieView().regenerate();

        getLorieView().requestFocus();
    }

    @Override
    public void onBackPressed() {
    }


    @Override
    public void onUserLeaveHint() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
    }

    @Override
    public void onPictureInPictureModeChanged(boolean isInPictureInPictureMode, @NonNull Configuration newConfig) {
        frm.setPadding(0, 0, 0, 0);
        super.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig);
    }

    /**
     * @noinspection NullableProblems
     */
    @SuppressLint("WrongConstant")
    @Override
    public WindowInsets onApplyWindowInsets(View v, WindowInsets insets) {
        handler.postDelayed(() -> getLorieView().triggerCallback(), 100);
        return insets;
    }

    @SuppressWarnings("SameParameterValue")
    void clientConnectedStateChanged(boolean connected) {
        runOnUiThread(() -> {
            SharedPreferences p = PreferenceManager.getDefaultSharedPreferences(this);
            mClientConnected = connected;
            getLorieView().setVisibility(connected ? View.VISIBLE : View.INVISIBLE);
            getLorieView().regenerate();

            // We should recover connection in the case if file descriptor for some reason was broken...
            if (!connected)
                tryConnect();

            if (connected)
                getLorieView().setPointerIcon(PointerIcon.getSystemIcon(this, PointerIcon.TYPE_NULL));
        });
    }

    private void checkXEvents() {
        getLorieView().handleXEvents();
        handler.postDelayed(this::checkXEvents, 300);
    }

}
