package com.micewine.emu.activities

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.MenuItem
import android.widget.ImageButton
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import com.micewine.emu.R
import com.micewine.emu.activities.MainActivity.Companion.setSharedVars
import com.micewine.emu.databinding.GeneralSettingsBinding
import com.micewine.emu.fragments.Box64SettingsFragment
import com.micewine.emu.fragments.DisplaySettingsFragment
import com.micewine.emu.fragments.DriversSettingsFragment
import com.micewine.emu.fragments.GeneralSettingsFragment

class GeneralSettings : AppCompatActivity() {
    private var binding: GeneralSettingsBinding? = null
    private var backButton: ImageButton? = null
    private val receiver: BroadcastReceiver = object : BroadcastReceiver() {
        @SuppressLint("UnspecifiedRegisterReceiverFlag")
        override fun onReceive(context: Context, intent: Intent) {
            if (ACTION_PREFERENCE_SELECT == intent.action) {
                if (intent.getStringExtra("preference") == context.resources.getString(R.string.box64_settings_title)) {
                    fragmentLoader(Box64SettingsFragment(), false)
                    findViewById<Toolbar>(R.id.generalSettingsToolbar).title = context.resources.getString(R.string.box64_settings_title)
                } else if (intent.getStringExtra("preference") == context.resources.getString(R.string.display_settings_title)) {
                    fragmentLoader(DisplaySettingsFragment(), false)
                    findViewById<Toolbar>(R.id.generalSettingsToolbar).title = context.resources.getString(R.string.display_settings_title)
                } else if (intent.getStringExtra("preference") == context.resources.getString(R.string.driver_settings_title)) {
                    fragmentLoader(DriversSettingsFragment(), false)
                    findViewById<Toolbar>(R.id.generalSettingsToolbar).title = context.resources.getString(R.string.driver_settings_title)
                }
            }
        }
    }

    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = GeneralSettingsBinding.inflate(layoutInflater)
        setContentView(binding!!.root)

        fragmentLoader(GeneralSettingsFragment(), true)

        findViewById<Toolbar>(R.id.generalSettingsToolbar).title = this.resources.getString(R.string.general_settings)

        backButton = findViewById(R.id.backButton)

        backButton?.setOnClickListener {
            if (supportFragmentManager.backStackEntryCount > 0) {
                supportFragmentManager.popBackStack()
                findViewById<Toolbar>(R.id.generalSettingsToolbar).title = this.resources.getString(R.string.general_settings)
            } else {
                finish()
            }
        }

        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val fragmentManager = supportFragmentManager
                if (fragmentManager.backStackEntryCount > 0) {
                    findViewById<Toolbar>(R.id.generalSettingsToolbar).title = resources.getString(R.string.general_settings)
                    fragmentManager.popBackStack()
                } else {
                    finish()
                }
            }
        }

        onBackPressedDispatcher.addCallback(this, callback)

        registerReceiver(receiver, object : IntentFilter(ACTION_PREFERENCE_SELECT) {})
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId

        if (id == android.R.id.home) {
            finish()
            return true
        }

        return super.onOptionsItemSelected(item)
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

        const val BOX64_DYNAREC_BIGBLOCK_KEY = "BOX64_DYNAREC_BIGBLOCK"
        const val BOX64_DYNAREC_STRONGMEM_KEY = "BOX64_DYNAREC_STRONGMEM"
        const val BOX64_DYNAREC_X87DOUBLE_KEY = "BOX64_DYNAREC_X87DOUBLE"
        const val BOX64_DYNAREC_FASTNAN_KEY = "BOX64_DYNAREC_FASTNAN"
        const val BOX64_DYNAREC_FASTROUND_KEY = "BOX64_DYNAREC_FASTROUND"
        const val BOX64_DYNAREC_SAFEFLAGS_KEY = "BOX64_DYNAREC_SAFEFLAGS"
        const val BOX64_DYNAREC_CALLRET_KEY = "BOX64_DYNAREC_CALLRET"
        const val BOX64_DYNAREC_ALIGNED_ATOMICS_KEY = "BOX64_DYNAREC_ALIGNED_ATOMICS"
        const val SELECTED_DRIVER_KEY = "selectedDriver"
        const val SELECTED_THEME_KEY = "selectedTheme"
        const val SELECTED_D3DX_RENDERER_KEY = "d3dxRenderer"
        const val SELECTED_WINED3D_KEY = "selectedWineD3D"
        const val SELECTED_DXVK_KEY = "selectedDXVK"
        const val SELECTED_IB_KEY = "selectedIb"
        const val SELECTED_VIRGL_PROFILE_KEY = "selectedVirGLProfile"
        const val SELECTED_DXVK_HUD_PRESET_KEY = "selectedDXVKHudPreset"
        const val DISPLAY_RESOLUTION_KEY = "displayResolution"
        const val DEAD_ZONE_KEY = "deadZone"
        const val MOUSE_SENSIBILITY_KEY = "mouseSensibility"
    }
}