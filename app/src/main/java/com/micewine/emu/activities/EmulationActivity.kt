package com.micewine.emu.activities

import android.Manifest
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
import android.media.AudioManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.RemoteException
import android.util.DisplayMetrics
import android.util.Log
import android.view.Display
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
import com.micewine.emu.CmdEntryPoint
import com.micewine.emu.ICmdEntryInterface
import com.micewine.emu.LorieView
import com.micewine.emu.R
import com.micewine.emu.activities.MainActivity.Companion.enableCpuCounter
import com.micewine.emu.activities.MainActivity.Companion.enableRamCounter
import com.micewine.emu.activities.MainActivity.Companion.getCpuInfo
import com.micewine.emu.activities.MainActivity.Companion.getMemoryInfo
import com.micewine.emu.controller.ControllerUtils.checkControllerAxis
import com.micewine.emu.controller.ControllerUtils.checkControllerButtons
import com.micewine.emu.controller.ControllerUtils.controllerMouseEmulation
import com.micewine.emu.controller.ControllerUtils.prepareButtonsAxisValues
import com.micewine.emu.core.ShellLoader
import com.micewine.emu.core.ShellLoader.runCommand
import com.micewine.emu.input.InputEventSender
import com.micewine.emu.input.InputStub
import com.micewine.emu.input.TouchInputHandler
import com.micewine.emu.views.OverlayView
import kotlinx.coroutines.launch

@SuppressLint("ApplySharedPref")
class EmulationActivity : AppCompatActivity(), View.OnApplyWindowInsetsListener {
    private var mInputHandler: TouchInputHandler? = null
    var service: ICmdEntryInterface? = null
    private var mLorieKeyListener: View.OnKeyListener? = null

    private val preferencesChangedListener =
        SharedPreferences.OnSharedPreferenceChangeListener { _: SharedPreferences?, _: String? ->
            onPreferencesChanged()
        }

    private val receiver: BroadcastReceiver = object : BroadcastReceiver() {
        @SuppressLint("UnspecifiedRegisterReceiverFlag")
        override fun onReceive(context: Context, intent: Intent) {
            if (CmdEntryPoint.ACTION_START == intent.action) {
                try {
                    Log.v("LorieBroadcastReceiver", "Got new ACTION_START intent")
                    onReceiveConnection(intent)
                } catch (e: Exception) {
                    Log.e(
                        "EmulationActivity",
                        "Something went wrong while we extracted connection details from binder.",
                        e
                    )
                }
            } else if (ACTION_STOP == intent.action) {
                finishAffinity()
            }
        }
    }

    private var drawerLayout: DrawerLayout? = null
    private var logsNavigationView: NavigationView? = null
    private var overlayView: OverlayView? = null

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val preferences = PreferenceManager.getDefaultSharedPreferences(this).apply {
            registerOnSharedPreferenceChangeListener(preferencesChangedListener)
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
        val inputManager = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager

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

        overlayView = findViewById(R.id.overlayView)
        overlayView?.visibility = View.INVISIBLE

        lifecycleScope.launch {
            controllerMouseEmulation(lorieView)
        }

        findViewById<NavigationView>(R.id.NavigationView).setNavigationItemSelectedListener { item: MenuItem ->
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
                    inputManager.apply {
                        @Suppress("DEPRECATION")
                        toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, 0)
                    }

                    lorieView.requestFocus()

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
                    if (overlayView?.isVisible!!) {
                        overlayView?.visibility = View.INVISIBLE
                    } else {
                        overlayView?.visibility = View.VISIBLE
                    }

                    drawerLayout?.closeDrawers()
                }

                R.id.editVirtualControllerMapping -> {
                    startActivity(Intent(this, VirtualControllerOverlayMapper::class.java))
                }

                R.id.editControllerMapping -> {
                    startActivity(Intent(this, ControllerMapper::class.java))
                }
            }
            true
        }

        mInputHandler = TouchInputHandler(this, InputEventSender(lorieView))
        mLorieKeyListener = View.OnKeyListener { _: View?, k: Int, e: KeyEvent ->
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

                    inputManager.apply {
                        hideSoftInputFromWindow(window.decorView.windowToken, 0)
                    }

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
            mInputHandler!!.sendKeyEvent(e)
        }

        lorieParent.setOnTouchListener { _: View?, e: MotionEvent ->
            // Avoid batched MotionEvent objects and reduce potential latency.
            // For reference: https://developer.android.com/develop/ui/views/touch-and-input/stylus-input/advanced-stylus-features#rendering.
            if (e.action == MotionEvent.ACTION_DOWN) lorieParent.requestUnbufferedDispatch(e)
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
        lorieView.setCallback(object : LorieView.Callback {
            override fun changed(
                sfc: Surface?,
                surfaceWidth: Int,
                surfaceHeight: Int,
                screenWidth: Int,
                screenHeight: Int
            ) {
                val frameRate = (if ((lorieView.display != null)) {
                    lorieView.display.refreshRate
                } else {
                    30F
                }).toInt()

                mInputHandler!!.handleHostSizeChanged(surfaceWidth, surfaceHeight)
                mInputHandler!!.handleClientSizeChanged(screenWidth, screenHeight)

                val name = if (lorieView.display == null || lorieView.display.displayId == Display.DEFAULT_DISPLAY) {
                    "Builtin Display"
                } else {
                    "External Display"
                }

                LorieView.sendWindowChange(screenWidth, screenHeight, frameRate, name)

                if (service != null && !LorieView.renderingInActivity()) {
                    try {
                        service!!.windowChanged(sfc)
                    } catch (e: RemoteException) {
                        Log.e("EmulationActivity", "failed to send windowChanged request", e)
                    }
                }
            }
        })

        registerReceiver(receiver, object : IntentFilter(CmdEntryPoint.ACTION_START) {
            init {
                addAction(ACTION_STOP)
            }
        }, if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) RECEIVER_EXPORTED else 0)

        inputMethodManager = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager

        tryConnect()
        onPreferencesChanged()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED && !shouldShowRequestPermissionRationale(
                Manifest.permission.POST_NOTIFICATIONS
            )
        ) {
            requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 0)
        }

        onReceiveConnection(intent)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        lorieView?.requestFocus()

        mLorieKeyListener?.onKey(null, keyCode, event)

        return true
    }

    override fun onGenericMotionEvent(event: MotionEvent?): Boolean {
        checkControllerAxis(lorieView!!, event!!)

        return true
    }


    override fun onDestroy() {
        unregisterReceiver(receiver)
        super.onDestroy()
    }

    fun onReceiveConnection(intent: Intent?) {
        val bundle = intent?.getBundleExtra(null)
        val ibinder = bundle?.getBinder(null) ?: return

        service = ICmdEntryInterface.Stub.asInterface(ibinder)
        try {
            service?.asBinder()?.linkToDeath({
                service = null
                Log.v("Lorie", "Disconnected")
                runOnUiThread {
                    LorieView.connect(-1)
                    clientConnectedStateChanged()
                }
            }, 0)
        } catch (ignored: RemoteException) {
        }

        try {
            if (service != null && service!!.asBinder().isBinderAlive) {
                Log.v("LorieBroadcastReceiver", "Extracting logcat fd.")
                val logcatOutput = service!!.logcatOutput
                if (logcatOutput != null) LorieView.startLogcat(logcatOutput.detachFd())

                tryConnect()

                if (intent !== getIntent()) getIntent().putExtra(null, bundle)
            }
        } catch (e: Exception) {
            Log.e("EmulationActivity", "Something went wrong while we were establishing connection", e)
        }
    }

    private fun tryConnect() {
        if (LorieView.connected()) return

        if (service == null) {
            LorieView.requestConnection()
            handler.postDelayed({ this.tryConnect() }, 250)
            return
        }

        try {
            val fd = service!!.xConnection
            if (fd != null) {
                Log.v("EmulationActivity", "Extracting X connection socket.")
                LorieView.connect(fd.detachFd())
                lorieView!!.triggerCallback()
                clientConnectedStateChanged()
                lorieView!!.reloadPreferences()
            } else handler.postDelayed({ this.tryConnect() }, 250)
        } catch (e: Exception) {
            Log.e("EmulationActivity", "Something went wrong while we were establishing connection", e)
            service = null

            // We should reset the View for the case if we have sent it's surface to the client.
            lorieView!!.regenerate()
            handler.postDelayed({ this.tryConnect() }, 250)
        }
    }

    private fun onPreferencesChanged() {
        handler.removeCallbacks { this.onPreferencesChangedCallback() }
        handler.postDelayed({ this.onPreferencesChangedCallback() }, 100)
    }

    @SuppressLint("UnsafeIntentLaunch")
    fun onPreferencesChangedCallback() {
        onWindowFocusChanged(hasWindowFocus())
        val lorieView = lorieView

        lorieView!!.reloadPreferences()

        lorieView.triggerCallback()

        showIMEWhileExternalConnected = false

        lorieView.requestLayout()
        lorieView.invalidate()
    }

    public override fun onResume() {
        super.onResume()

        lorieView!!.requestFocus()
        lorieView!!.requestLayout()

        overlayView?.loadFromPreferences()

        prepareButtonsAxisValues(this)
    }

    public override fun onPause() {
        inputMethodManager!!.hideSoftInputFromWindow(window.decorView.rootView.windowToken, 0)

        super.onPause()
    }

    val lorieView: LorieView?
        get() = findViewById(R.id.lorieView)

    fun handleKey(e: KeyEvent): Boolean {
        return mLorieKeyListener!!.onKey(lorieView, e.keyCode, e)
    }

    var orientation: Int = 0

    init {
        instance = this
    }

    @SuppressLint("WrongConstant")
    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE

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
            lorieView?.regenerate()
        }

        lorieView?.requestFocus()
    }

    /** @noinspection NullableProblems
     */
    @SuppressLint("WrongConstant")
    override fun onApplyWindowInsets(v: View, insets: WindowInsets): WindowInsets {
        handler.postDelayed({ lorieView!!.triggerCallback() }, 100)
        return insets
    }

    private fun clientConnectedStateChanged() {
        runOnUiThread {
            val connected = LorieView.connected()

            lorieView!!.visibility = if (connected) View.VISIBLE else View.INVISIBLE
            lorieView!!.regenerate()

            // We should recover connection in the case if file descriptor for some reason was broken...
            if (!connected) {
                tryConnect()
            } else {
                lorieView!!.pointerIcon = PointerIcon.getSystemIcon(this, PointerIcon.TYPE_NULL)
            }
        }
    }

    fun setExternalKeyboardConnected(connected: Boolean) {
        externalKeyboardConnected = connected
        lorieView!!.requestFocus()
    }

    companion object {
        const val KEY_BACK = 158
        const val ACTION_STOP: String = "com.micewine.emu.ACTION_STOP"

        var handler: Handler = Handler(Looper.getMainLooper())
        var inputMethodManager: InputMethodManager? = null
        private var showIMEWhileExternalConnected = false
        private var externalKeyboardConnected = false

        @SuppressLint("StaticFieldLeak")
        private lateinit var instance: EmulationActivity

        @JvmStatic
        fun getInstance(): EmulationActivity {
            return instance
        }

        @JvmStatic
        fun getRealMetrics(m: DisplayMetrics?) {
            if (getInstance().lorieView != null && getInstance().lorieView!!.display != null) getInstance().lorieView!!.display.getRealMetrics(m)
        }

        var sharedLogs: ShellLoader.ViewModelAppLogs? = null

        fun initSharedLogs(supportFragmentManager: FragmentManager) {
            sharedLogs = ShellLoader.ViewModelAppLogs(supportFragmentManager)
        }
    }
}