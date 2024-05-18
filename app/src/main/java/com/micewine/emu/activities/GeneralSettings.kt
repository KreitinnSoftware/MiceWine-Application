package com.micewine.emu.activities

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.ImageButton
import android.widget.Toolbar
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.micewine.emu.R
import com.micewine.emu.activities.MainActivity.Companion.setSharedVars
import com.micewine.emu.databinding.GeneralSettingsBinding
import com.micewine.emu.fragments.Box64SettingsFragment
import com.micewine.emu.fragments.GeneralSettingsFragment

class GeneralSettings : AppCompatActivity() {
    private var binding: GeneralSettingsBinding? = null
    private var backButton: ImageButton? = null
    private val receiver: BroadcastReceiver = object : BroadcastReceiver() {
        @SuppressLint("UnspecifiedRegisterReceiverFlag")
        override fun onReceive(context: Context, intent: Intent) {
            if (ACTION_PREF_TEST == intent.action) {
                Log.v("A", buttonPressed)

                if (buttonPressed == context.resources.getString(R.string.box64_settings_title)) {
                    fragmentLoader(Box64SettingsFragment(), false)
                    findViewById<Toolbar>(R.id.generalSettingsToolbar).title = context.resources.getString(R.string.box64_settings_title)
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

        registerReceiver(receiver, object : IntentFilter(ACTION_PREF_TEST) {})
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
        const val ACTION_PREF_TEST = "com.micewine.emu.ACTION_PREF_TEST"
        const val SWITCH = 1
        const val SPINNER = 2

        var buttonPressed = ""
    }
}