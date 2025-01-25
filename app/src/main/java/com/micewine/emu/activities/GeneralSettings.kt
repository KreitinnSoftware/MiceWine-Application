package com.micewine.emu.activities

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.KeyEvent
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import com.micewine.emu.R
import com.micewine.emu.activities.MainActivity.Companion.setSharedVars
import com.micewine.emu.databinding.ActivityGeneralSettingsBinding
import com.micewine.emu.fragments.Box64SettingsFragment
import com.micewine.emu.fragments.DisplaySettingsFragment
import com.micewine.emu.fragments.DriversSettingsFragment
import com.micewine.emu.fragments.EnvironmentVarsSettingsFragment
import com.micewine.emu.fragments.GeneralSettingsFragment
import com.micewine.emu.fragments.WineSettingsFragment

class GeneralSettings : AppCompatActivity() {
    private var binding: ActivityGeneralSettingsBinding? = null
    private var backButton: ImageButton? = null
    private val box64SettingsFragment = Box64SettingsFragment()
    private val wineSettingsFragment = WineSettingsFragment()
    private val displaySettingsFragment = DisplaySettingsFragment()
    private val driversSettingsFragment = DriversSettingsFragment()
    private val environmentVariablesSettings = EnvironmentVarsSettingsFragment()
    private val receiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val preference = intent.getStringExtra("preference")

            if (intent.action == ACTION_PREFERENCE_SELECT) {
                when (preference) {
                    getString(R.string.box64_settings_title) -> {
                        generalSettingsToolbar?.title = getString(R.string.box64_settings_title)

                        fragmentLoader(box64SettingsFragment, false)
                    }

                    getString(R.string.wine_settings_title) -> {
                        generalSettingsToolbar?.title = getString(R.string.wine_settings_title)

                        fragmentLoader(wineSettingsFragment, false)
                    }

                    getString(R.string.display_settings_title) -> {
                        generalSettingsToolbar?.title = getString(R.string.display_settings_title)

                        fragmentLoader(displaySettingsFragment, false)
                    }

                    context.resources.getString(R.string.driver_settings_title) -> {
                        generalSettingsToolbar?.title = getString(R.string.driver_settings_title)

                        fragmentLoader(driversSettingsFragment, false)
                    }

                    context.resources.getString(R.string.env_settings_title) -> {
                        generalSettingsToolbar?.title = getString(R.string.env_settings_title)

                        fragmentLoader(environmentVariablesSettings, false)
                    }
                }
            }
        }
    }

    private var generalSettingsToolbar: Toolbar? = null

    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityGeneralSettingsBinding.inflate(layoutInflater)
        setContentView(binding!!.root)

        fragmentLoader(GeneralSettingsFragment(), true)

        generalSettingsToolbar = findViewById(R.id.generalSettingsToolbar)
        generalSettingsToolbar?.title = this.resources.getString(R.string.general_settings)

        backButton = findViewById(R.id.backButton)
        backButton?.setOnClickListener {
            onKeyDown(KeyEvent.KEYCODE_BACK, null)
        }

        registerReceiver(receiver, object : IntentFilter(ACTION_PREFERENCE_SELECT) {})
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (supportFragmentManager.backStackEntryCount > 0) {
                supportFragmentManager.popBackStack()
                generalSettingsToolbar?.title = resources.getString(R.string.general_settings)
            } else {
                finish()
            }
        }

        return true
    }

    override fun onDestroy() {
        super.onDestroy()
        setSharedVars(this)
        unregisterReceiver(receiver)
    }

    private fun fragmentLoader(fragment: Fragment, appInit: Boolean) {
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.settings_content, fragment)

            if (!appInit) {
                addToBackStack(null)
            }

            commit()
        }
    }

    companion object {
        const val ACTION_PREFERENCE_SELECT = "com.micewine.emu.ACTION_PREFERENCE_SELECT"
        const val SWITCH = 1
        const val SPINNER = 2
        const val CHECKBOX = 3

        const val BOX64_LOG = "BOX64_LOG"
        const val BOX64_LOG_DEFAULT_VALUE = "1"
        const val BOX64_AVX = "BOX64_AVX"
        const val BOX64_AVX_DEFAULT_VALUE = "2"
        const val BOX64_DYNAREC_BIGBLOCK = "BOX64_DYNAREC_BIGBLOCK"
        const val BOX64_DYNAREC_BIGBLOCK_DEFAULT_VALUE = "0"
        const val BOX64_DYNAREC_STRONGMEM = "BOX64_DYNAREC_STRONGMEM"
        const val BOX64_DYNAREC_STRONGMEM_DEFAULT_VALUE = "2"
        const val BOX64_DYNAREC_WEAKBARRIER = "BOX64_DYNAREC_WEAKBARRIER"
        const val BOX64_DYNAREC_WEAKBARRIER_DEFAULT_VALUE = "0"
        const val BOX64_DYNAREC_PAUSE = "BOX64_DYNAREC_PAUSE"
        const val BOX64_DYNAREC_PAUSE_DEFAULT_VALUE = "0"
        const val BOX64_DYNAREC_X87DOUBLE = "BOX64_DYNAREC_X87DOUBLE"
        const val BOX64_DYNAREC_X87DOUBLE_DEFAULT_VALUE = false
        const val BOX64_DYNAREC_FASTNAN = "BOX64_DYNAREC_FASTNAN"
        const val BOX64_DYNAREC_FASTNAN_DEFAULT_VALUE = true
        const val BOX64_DYNAREC_FASTROUND = "BOX64_DYNAREC_FASTROUND"
        const val BOX64_DYNAREC_FASTROUND_DEFAULT_VALUE = true
        const val BOX64_DYNAREC_SAFEFLAGS = "BOX64_DYNAREC_SAFEFLAGS"
        const val BOX64_DYNAREC_SAFEFLAGS_DEFAULT_VALUE = "1"
        const val BOX64_DYNAREC_CALLRET = "BOX64_DYNAREC_CALLRET"
        const val BOX64_DYNAREC_CALLRET_DEFAULT_VALUE = true
        const val BOX64_DYNAREC_ALIGNED_ATOMICS = "BOX64_DYNAREC_ALIGNED_ATOMICS"
        const val BOX64_DYNAREC_ALIGNED_ATOMICS_DEFAULT_VALUE = false
        const val BOX64_DYNAREC_NATIVEFLAGS = "BOX64_DYNAREC_NATIVEFLAGS"
        const val BOX64_DYNAREC_NATIVEFLAGS_DEFAULT_VALUE = true
        const val BOX64_DYNAREC_BLEEDING_EDGE = "BOX64_DYNAREC_BLEEDING_EDGE"
        const val BOX64_DYNAREC_BLEEDING_EDGE_DEFAULT_VALUE = true
        const val BOX64_DYNAREC_WAIT = "BOX64_DYNAREC_WAIT"
        const val BOX64_DYNAREC_WAIT_DEFAULT_VALUE = true
        const val BOX64_DYNAREC_DIRTY = "BOX64_DYNAREC_DIRTY"
        const val BOX64_DYNAREC_DIRTY_DEFAULT_VALUE = false
        const val BOX64_SHOWSEGV = "BOX64_SHOWSEGV"
        const val BOX64_SHOWSEGV_DEFAULT_VALUE = false
        const val BOX64_SHOWBT = "BOX64_SHOWBT"
        const val BOX64_SHOWBT_DEFAULT_VALUE = false
        const val BOX64_NOSIGSEGV = "BOX64_NOSIGSEGV"
        const val BOX64_NOSIGSEGV_DEFAULT_VALUE = false
        const val BOX64_NOSIGILL = "BOX64_NOSIGILL"
        const val BOX64_NOSIGILL_DEFAULT_VALUE = false

        const val SELECTED_BOX64 = "selectedBox64"
        const val SELECTED_BOX64_DEFAULT_VALUE = ""
        const val SELECTED_TU_DEBUG_PRESET = "selectedTuDebugPreset"
        const val SELECTED_TU_DEBUG_PRESET_DEFAULT_VALUE = "noconform"
        const val SELECTED_DRIVER = "selectedDriver"
        const val SELECTED_DRIVER_DEFAULT_VALUE = ""
        const val SELECTED_D3DX_RENDERER = "d3dxRenderer"
        const val SELECTED_D3DX_RENDERER_DEFAULT_VALUE = "DXVK"
        const val SELECTED_WINED3D = "selectedWineD3D"
        const val SELECTED_WINED3D_DEFAULT_VALUE = "WineD3D-9.0"
        const val SELECTED_DXVK = "selectedDXVK"
        const val SELECTED_DXVK_DEFAULT_VALUE = "DXVK-1.10.3-async"
        const val SELECTED_VKD3D = "selectedVKD3D"
        const val SELECTED_VKD3D_DEFAULT_VALUE = "VKD3D-2.13"
        const val ENABLE_DRI3 = "enableDRI3"
        const val ENABLE_DRI3_DEFAULT_VALUE = true
        const val ENABLE_MANGOHUD = "enableMangoHUD"
        const val ENABLE_MANGOHUD_DEFAULT_VALUE = true
        const val ENABLE_SERVICES = "enableServices"
        const val ENABLE_SERVICES_DEFAULT_VALUE = false
        const val WINE_ESYNC = "wineEsync"
        const val WINE_ESYNC_DEFAULT_VALUE = false
        const val WINE_LOG_LEVEL = "wineLogLevel"
        const val WINE_LOG_LEVEL_DEFAULT_VALUE = "default"
        const val SELECTED_GL_PROFILE = "selectedGLProfile"
        const val SELECTED_GL_PROFILE_DEFAULT_VALUE = "GL 4.6"
        const val SELECTED_DXVK_HUD_PRESET = "selectedDXVKHudPreset"
        const val SELECTED_DXVK_HUD_PRESET_DEFAULT_VALUE = ""
        const val SELECTED_MESA_VK_WSI_PRESENT_MODE = "MESA_VK_WSI_PRESENT_MODE"
        const val SELECTED_MESA_VK_WSI_PRESENT_MODE_DEFAULT_VALUE = "mailbox"
        const val DISPLAY_RESOLUTION = "displayResolution"
        const val DISPLAY_RESOLUTION_DEFAULT_VALUE = "1280x720"
        const val DEAD_ZONE = "deadZone"
        const val MOUSE_SENSIBILITY = "mouseSensibility"
    }
}