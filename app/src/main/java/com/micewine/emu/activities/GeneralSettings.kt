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
import com.micewine.emu.fragments.GeneralSettingsFragment
import com.micewine.emu.fragments.WineSettingsFragment

class GeneralSettings : AppCompatActivity() {
    private var binding: ActivityGeneralSettingsBinding? = null
    private var backButton: ImageButton? = null
    private val box64SettingsFragment = Box64SettingsFragment()
    private val wineSettingsFragment = WineSettingsFragment()
    private val displaySettingsFragment = DisplaySettingsFragment()
    private val driversSettingsFragment = DriversSettingsFragment()
    private val receiver: BroadcastReceiver = object : BroadcastReceiver() {
        @SuppressLint("UnspecifiedRegisterReceiverFlag")
        override fun onReceive(context: Context, intent: Intent) {
            val preference = intent.getStringExtra("preference")

            if (intent.action == ACTION_PREFERENCE_SELECT) {
                when (preference) {
                    getString(R.string.box64_settings_title) -> {
                        generalSettingsToolbar?.title = context.resources.getString(R.string.box64_settings_title)

                        fragmentLoader(box64SettingsFragment, false)
                    }

                    getString(R.string.wine_settings_title) -> {
                        generalSettingsToolbar?.title = getString(R.string.wine_settings_title)

                        fragmentLoader(wineSettingsFragment, false)
                    }

                    getString(R.string.display_settings_title) -> {
                        generalSettingsToolbar?.title = context.resources.getString(R.string.display_settings_title)

                        fragmentLoader(displaySettingsFragment, false)
                    }

                    context.resources.getString(R.string.driver_settings_title) -> {
                        generalSettingsToolbar?.title = context.resources.getString(R.string.driver_settings_title)

                        fragmentLoader(driversSettingsFragment, false)
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
        val fragmentTransaction = supportFragmentManager.beginTransaction()

        fragmentTransaction.replace(R.id.settings_content, fragment)

        if (!appInit) {
            fragmentTransaction.addToBackStack(null)
        }

        fragmentTransaction.commit()
    }

    companion object {
        const val ACTION_PREFERENCE_SELECT = "com.micewine.emu.ACTION_PREFERENCE_SELECT"
        const val ACTION_PREFERENCES_CHANGED = "com.micewine.emu.ACTION_PREFERENCES_CHANGED"
        const val SWITCH = 1
        const val SPINNER = 2
        const val CHECKBOX = 3

        const val BOX64_LOG_KEY = "BOX64_LOG"
        const val BOX64_AVX_KEY = "BOX64_AVX"
        const val BOX64_DYNAREC_BIGBLOCK_KEY = "BOX64_DYNAREC_BIGBLOCK"
        const val BOX64_DYNAREC_STRONGMEM_KEY = "BOX64_DYNAREC_STRONGMEM"
        const val BOX64_DYNAREC_X87DOUBLE_KEY = "BOX64_DYNAREC_X87DOUBLE"
        const val BOX64_DYNAREC_FASTNAN_KEY = "BOX64_DYNAREC_FASTNAN"
        const val BOX64_DYNAREC_FASTROUND_KEY = "BOX64_DYNAREC_FASTROUND"
        const val BOX64_DYNAREC_SAFEFLAGS_KEY = "BOX64_DYNAREC_SAFEFLAGS"
        const val BOX64_DYNAREC_CALLRET_KEY = "BOX64_DYNAREC_CALLRET"
        const val BOX64_DYNAREC_ALIGNED_ATOMICS_KEY = "BOX64_DYNAREC_ALIGNED_ATOMICS"
        const val BOX64_DYNAREC_BLEEDING_EDGE_KEY = "BOX64_DYNAREC_BLEEDING_EDGE"
        const val BOX64_DYNAREC_WAIT_KEY = "BOX64_DYNAREC_WAIT"
        const val SELECTED_TU_DEBUG_PRESET_KEY = "selectedTuDebugPreset"
        const val SELECTED_DRIVER_KEY = "selectedDriver"
        const val SELECTED_D3DX_RENDERER_KEY = "d3dxRenderer"
        const val SELECTED_WINED3D_KEY = "selectedWineD3D"
        const val SELECTED_DXVK_KEY = "selectedDXVK"
        const val SELECTED_VKD3D_KEY = "selectedVKD3D"
        const val WINE_ESYNC_KEY = "wineEsync"
        const val WINE_LOG_LEVEL_KEY = "wineLogLevel"
        const val SELECTED_GL_PROFILE_KEY = "selectedGLProfile"
        const val SELECTED_DXVK_HUD_PRESET_KEY = "selectedDXVKHudPreset"
        const val SELECTED_MESA_VK_WSI_PRESENT_MODE_KEY = "MESA_VK_WSI_PRESENT_MODE"
        const val DISPLAY_RESOLUTION_KEY = "displayResolution"
        const val DEAD_ZONE_KEY = "deadZone"
        const val MOUSE_SENSIBILITY_KEY = "mouseSensibility"
    }
}