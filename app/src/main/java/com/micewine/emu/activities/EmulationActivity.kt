package com.micewine.emu.activities

import android.Manifest.permission
import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.content.res.Configuration.KEYBOARD_QWERTY
import android.media.AudioManager
import android.os.Build.VERSION
import android.os.Build.VERSION_CODES
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.RemoteException
import android.util.Log
import android.view.InputDevice
import android.view.KeyEvent
import android.view.MenuItem
import android.view.MotionEvent
import android.view.PointerIcon
import android.view.Surface
import android.view.View
import android.view.Window
import android.view.WindowInsets
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.isVisible
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.preference.PreferenceManager
import com.google.android.material.button.MaterialButton
import com.google.android.material.navigation.NavigationView
import com.micewine.emu.CmdEntryPoint.Companion.ACTION_START
import com.micewine.emu.CmdEntryPoint.Companion.requestConnection
import com.micewine.emu.ICmdEntryInterface
import com.micewine.emu.LorieView
import com.micewine.emu.R
import com.micewine.emu.activities.GeneralSettings.Companion.ACTION_PREFERENCES_CHANGED
import com.micewine.emu.activities.MainActivity.Companion.ACTION_STOP_ALL
import com.micewine.emu.activities.MainActivity.Companion.enableCpuCounter
import com.micewine.emu.activities.MainActivity.Companion.enableRamCounter
import com.micewine.emu.activities.MainActivity.Companion.getCpuInfo
import com.micewine.emu.activities.MainActivity.Companion.getMemoryInfo
import com.micewine.emu.activities.MainActivity.Companion.setSharedVars
import com.micewine.emu.controller.ControllerUtils.checkControllerAxis
import com.micewine.emu.controller.ControllerUtils.checkControllerButtons
import com.micewine.emu.controller.ControllerUtils.controllerMouseEmulation
import com.micewine.emu.controller.ControllerUtils.prepareButtonsAxisValues
import com.micewine.emu.core.ShellLoader
import com.micewine.emu.core.ShellLoader.runCommand
import com.micewine.emu.input.InputEventSender
import com.micewine.emu.input.InputStub
import com.micewine.emu.input.TouchInputHandler
import com.micewine.emu.input.TouchInputHandler.RenderStub.NullStub
import com.micewine.emu.views.OverlayView
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch


class EmulationActivity : AppCompatActivity(), View.OnApplyWindowInsetsListener {
    private var mInputHandler: TouchInputHandler? = null
    private var service: ICmdEntryInterface? = null
    private var mClientConnected = false
    private val receiver: BroadcastReceiver = object : BroadcastReceiver() {
        @SuppressLint("UnspecifiedRegisterReceiverFlag")
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                ACTION_START -> {
                    try {
                        Log.v("LorieBroadcastReceiver", "Got new ACTION_START intent")
                        val b = intent.getBundleExtra("")?.getBinder("")
                        service = ICmdEntryInterface.Stub.asInterface(b)
                        service?.asBinder()?.linkToDeath(
                            {
                                service = null
                                requestConnection()
                                Log.v("Lorie", "Disconnected")
                                runOnUiThread { clientConnectedStateChanged(false) }
                            }, 0
                        )
                        onReceiveConnection()
                    } catch (e: Exception) {
                        Log.e("MainActivity", "Something went wrong while we extracted connection details from binder.", e)
                    }
                }
                ACTION_PREFERENCES_CHANGED -> {
                    Log.d("MainActivity", "preference: " + intent.getStringExtra("key"))
                    onPreferencesChanged()
                }
                ACTION_STOP_ALL -> {
                    finishAffinity()
                }
            }
        }
    }
    private var mLorieKeyListener: View.OnKeyListener? = null
    private var drawerLayout: DrawerLayout? = null
    private var logsNavigationView: NavigationView? = null

    init {
        instance = this
    }
    @SuppressLint(
        "ClickableViewAccessibility",
        "UnspecifiedRegisterReceiverFlag"
    )
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val preferences = PreferenceManager.getDefaultSharedPreferences(this)
        preferences.registerOnSharedPreferenceChangeListener { _: SharedPreferences?, _: String? ->
            onPreferencesChanged()
        }

        initSharedLogs(supportFragmentManager)

        if (enableCpuCounter) {
            lifecycleScope.launch {
                getCpuInfo()
            }
        }

        if (enableRamCounter) {
            lifecycleScope.launch {
                getMemoryInfo(this@EmulationActivity)
            }
        }

        prepareButtonsAxisValues(this)
        val audioManager = getSystemService(AUDIO_SERVICE) as AudioManager
        window.setFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS or WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, 0)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.activity_emulation)

        drawerLayout = findViewById(R.id.DrawerLayout)
        drawerLayout?.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)

        logsNavigationView = findViewById(R.id.NavigationViewLogs)

        val headerView: View = logsNavigationView!!.getHeaderView(0)

        val observer: Observer<String>
        val logTextView = headerView.findViewById<TextView>(R.id.logsTextView)
        val scrollView = headerView.findViewById<ScrollView>(R.id.scrollView)
        val closeButton = headerView.findViewById<MaterialButton>(R.id.closeButton)
        val copyButton = headerView.findViewById<MaterialButton>(R.id.copyButton)
        val clipboard: ClipboardManager? = ContextCompat.getSystemService(this, ClipboardManager::class.java)

        observer = Observer { out: String? ->
            if (out != null) {
                logTextView.append("$out")
                scrollView.fullScroll(ScrollView.FOCUS_UP)
            }
        }

        sharedLogs?.logsTextHead?.observe(this, observer)

        scrollView.fullScroll(ScrollView.FOCUS_UP)

        closeButton.setOnClickListener {
            drawerLayout?.closeDrawers()
        }

        copyButton.setOnClickListener {
            val clip = ClipData.newPlainText("MiceWine Logs", logTextView.text)
            clipboard?.setPrimaryClip(clip)
        }

        val lorieView = findViewById<LorieView>(R.id.lorieView)
        val lorieParent = lorieView.parent as View

        val overlayView: OverlayView = findViewById(R.id.overlayView)

        overlayView.visibility = View.INVISIBLE

        lifecycleScope.launch {
            controllerMouseEmulation(lorieView)
        }

        val nav = findViewById<NavigationView>(R.id.NavigationView)
        nav.setNavigationItemSelectedListener { item: MenuItem ->
            when (item.itemId) {
                R.id.exit -> {
                    drawerLayout?.closeDrawers()

                    runCommand("pkill -9 wineserver")
                    runCommand("pkill -9 .exe")
                    runCommand("pkill -9 pulseaudio")

                    logTextView.text = ""

                    val intent = Intent(this, MainActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                    }

                    startActivityIfNeeded(intent, 0)
                }

                R.id.openCloseKeyboard -> {
                    val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                    @Suppress("DEPRECATION")
                    imm.showSoftInput(lorieView, InputMethodManager.SHOW_FORCED)
                    drawerLayout?.closeDrawers()
                }

                R.id.setScreenStretch -> {
                    preferences.edit().apply {
                        putBoolean("displayStretch", !preferences.getBoolean("displayStretch", false))
                        apply()
                    }

                    lorieView.requestLayout()
                }

                R.id.openLogViewer -> {
                    drawerLayout?.openDrawer(GravityCompat.END)
                    drawerLayout?.closeDrawer(GravityCompat.START)
                }

                R.id.openCloseOverlay -> {
                    if (overlayView.isVisible) {
                        overlayView.visibility = View.INVISIBLE
                    } else {
                        overlayView.visibility = View.VISIBLE
                    }

                    drawerLayout?.closeDrawers()
                }

                R.id.editControllerPreferences -> {
                    startActivity(Intent(this, ControllerMapper::class.java))
                }
            }
            true
        }
        mInputHandler = TouchInputHandler(this, object : NullStub() {
            override fun swipeDown() {}
        }, InputEventSender(lorieView))
        mLorieKeyListener = View.OnKeyListener { v: View?, k: Int, e: KeyEvent ->
            if (k == KeyEvent.KEYCODE_BACK) {
                if (e.isFromSource(InputDevice.SOURCE_MOUSE) || e.isFromSource(InputDevice.SOURCE_MOUSE_RELATIVE)) {
                    if (e.repeatCount != 0) // ignore auto-repeat
                        return@OnKeyListener true
                    if (e.action == KeyEvent.ACTION_UP || e.action == KeyEvent.ACTION_DOWN) lorieView.sendMouseEvent(
                        -1f,
                        -1f,
                        InputStub.BUTTON_RIGHT,
                        e.action == KeyEvent.ACTION_DOWN,
                        true
                    )
                    return@OnKeyListener true
                }
                if (e.scanCode == KEY_BACK && e.device.keyboardType != InputDevice.KEYBOARD_TYPE_ALPHABETIC || e.scanCode == 0) {
                    if (e.action == KeyEvent.ACTION_UP) if (!drawerLayout?.isDrawerOpen(GravityCompat.START)!!) {
                        drawerLayout?.openDrawer(GravityCompat.START)
                    } else {
                        drawerLayout?.closeDrawers()
                    }

                    lorieView.releasePointerCapture()
                    return@OnKeyListener true
                }
            } else if (k == KeyEvent.KEYCODE_VOLUME_DOWN) {
                audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_LOWER, AudioManager.FLAG_SHOW_UI)
                return@OnKeyListener true
            } else if (k == KeyEvent.KEYCODE_VOLUME_UP) {
                audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_RAISE, AudioManager.FLAG_SHOW_UI)
                return@OnKeyListener true
            }

            checkControllerButtons(lorieView, e)
            mInputHandler!!.sendKeyEvent(v, e)
        }
        lorieParent.setOnTouchListener { _: View?, e: MotionEvent? ->
            mInputHandler!!.handleTouchEvent(lorieParent, lorieView, e)
        }
        lorieParent.setOnHoverListener { _: View?, e: MotionEvent? ->
            mInputHandler!!.handleTouchEvent(lorieParent, lorieView, e)
        }
        lorieParent.setOnGenericMotionListener { _: View?, e: MotionEvent? ->
            mInputHandler!!.handleTouchEvent(lorieParent, lorieView, e)
        }
        lorieView.setOnCapturedPointerListener { _: View?, e: MotionEvent? ->
            mInputHandler!!.handleTouchEvent(lorieView, lorieView, e)
        }
        lorieParent.setOnCapturedPointerListener { _: View?, e: MotionEvent? ->
            mInputHandler!!.handleTouchEvent(lorieView, lorieView, e)
        }
        lorieView.setOnKeyListener(mLorieKeyListener)

        val callback = object : LorieView.Callback {
            override fun changed(
                sfc: Surface?,
                surfaceWidth: Int,
                surfaceHeight: Int,
                screenWidth: Int,
                screenHeight: Int
            ) {
                val framerate = (lorieView.display?.refreshRate ?: 30).toInt()
                mInputHandler?.handleHostSizeChanged(surfaceWidth, surfaceHeight)
                mInputHandler?.handleClientSizeChanged(screenWidth, screenHeight)
                LorieView.sendWindowChange(screenWidth, screenHeight, framerate)
                service?.let {
                    try {
                        it.windowChanged(sfc)
                    } catch (e: RemoteException) {
                        e.printStackTrace()
                    }
                }
            }
        }

        lorieView.setCallback(callback)

        registerReceiver(receiver, object : IntentFilter(ACTION_START) {
            init {
                addAction(ACTION_PREFERENCES_CHANGED)
                addAction(ACTION_STOP_ALL)
            }
        })

        requestConnection()
        onPreferencesChanged()
        checkXEvents()

        setSharedVars(this)

        if (VERSION.SDK_INT >= VERSION_CODES.TIRAMISU && checkSelfPermission(permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED && !shouldShowRequestPermissionRationale(
                permission.POST_NOTIFICATIONS
            )
        ) {
            requestPermissions(arrayOf(permission.POST_NOTIFICATIONS), 0)
        }
    }

    override fun onGenericMotionEvent(event: MotionEvent?): Boolean {
        checkControllerAxis(lorieView, event!!)

        return true
    }

    override fun onDestroy() {
        unregisterReceiver(receiver)
        super.onDestroy()
    }

    fun onReceiveConnection() {
        try {
            if (service != null && service!!.asBinder().isBinderAlive) {
                Log.v("LorieBroadcastReceiver", "Extracting logcat fd.")
                val logcatOutput = service!!.getLogcatOutput()

                if (logcatOutput != null) {
                    LorieView.startLogcat(logcatOutput.detachFd())
                }

                tryConnect()
            }
        } catch (e: Exception) {
            Log.e("MainActivity", "Something went wrong while we were establishing connection", e)
        }
    }

    private fun isKeyboardConnected(): Boolean {
        return resources.configuration.keyboard == KEYBOARD_QWERTY
    }

    private fun tryConnect() {
        if (mClientConnected) {
            return
        }
        try {
            Log.v("LorieBroadcastReceiver", "Extracting X connection socket.")
            val fd = if (service == null) {
                null
            } else {
                service!!.getXConnection()
            }

            if (fd != null) {
                LorieView.connect(fd.detachFd())
                lorieView.triggerCallback()
                clientConnectedStateChanged(true)
                LorieView.setClipboardSyncEnabled(true)
            } else {
                handler.postDelayed({ tryConnect() }, 500)
            }
        } catch (e: Exception) {
            Log.e("MainActivity", "Something went wrong while we were establishing connection", e)
            service = null

            // We should reset the View for the case if we have sent it's surface to the client.
            lorieView.regenerate()
        }
    }

    fun onPreferencesChanged() {
        mInputHandler!!.setInputMode(TouchInputHandler.InputMode.TRACKPAD)
        mInputHandler!!.setTapToMove(false)
        mInputHandler!!.setPreferScancodes(isKeyboardConnected())
        mInputHandler!!.setPointerCaptureEnabled(true)

        lorieView.releasePointerCapture()

        onWindowFocusChanged(true)
        LorieView.setClipboardSyncEnabled(false)
        lorieView.triggerCallback()

        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE

        //Reset default input back to normal
        TouchInputHandler.STYLUS_INPUT_HELPER_MODE = 1
    }

    public override fun onResume() {
        super.onResume()
        lorieView.requestFocus()
        prepareButtonsAxisValues(this)

        lifecycleScope.cancel()

        if (enableCpuCounter) {
            lifecycleScope.launch {
                getCpuInfo()
            }
        }

        if (enableRamCounter) {
            lifecycleScope.launch {
                getMemoryInfo(this@EmulationActivity)
            }
        }
    }

    public override fun onPause() {
        super.onPause()
    }

    private val lorieView: LorieView get() = findViewById(R.id.lorieView)

    fun handleKey(e: KeyEvent): Boolean {
        mLorieKeyListener!!.onKey(lorieView, e.keyCode, e)
        return true
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)

        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, window.decorView).let { controller ->
            controller.hide(WindowInsetsCompat.Type.systemBars())
            controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }

        window.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        )

        if (hasFocus) {
            lorieView.regenerate()
        }

        lorieView.requestFocus()
    }

    public override fun onUserLeaveHint() {
        super.onUserLeaveHint()
        PreferenceManager.getDefaultSharedPreferences(this)
    }

    override fun onApplyWindowInsets(v: View, insets: WindowInsets): WindowInsets {
        handler.postDelayed({ lorieView.triggerCallback() }, 100)
        return insets
    }

    fun clientConnectedStateChanged(connected: Boolean) {
        runOnUiThread {
            PreferenceManager.getDefaultSharedPreferences(this)
            mClientConnected = connected
            lorieView.visibility = if (connected) View.VISIBLE else View.INVISIBLE
            lorieView.regenerate()

            // We should recover connection in the case if file descriptor for some reason was broken...
            if (!connected) {
                tryConnect()
            }

            if (connected) {
                lorieView.pointerIcon = PointerIcon.getSystemIcon(this, PointerIcon.TYPE_NULL)
            }
        }
    }

    private fun checkXEvents() {
        lorieView.handleXEvents()
        handler.postDelayed({ checkXEvents() }, 300)
    }

    companion object {
        const val KEY_BACK = 158
        var handler = Handler(Looper.getMainLooper())

        @JvmStatic
        @SuppressLint("StaticFieldLeak")
        lateinit var instance: EmulationActivity private set

        var sharedLogs: ShellLoader.ViewModelAppLogs? = null

        fun initSharedLogs(supportFragmentManager: FragmentManager) {
            sharedLogs = ShellLoader.ViewModelAppLogs(supportFragmentManager)
        }
    }
}
