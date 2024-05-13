package com.micewine.emu

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceGroup

class Preferences : AppCompatActivity() {
    private var loriePreferenceFragment: LoriePreferenceFragment? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        loriePreferenceFragment = LoriePreferenceFragment()
        supportFragmentManager.beginTransaction()
            .replace(android.R.id.content, loriePreferenceFragment!!).commit()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    class LoriePreferenceFragment : PreferenceFragmentCompat(),
        Preference.OnPreferenceChangeListener, Preference.OnPreferenceClickListener {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            addPreferencesFromResource(R.xml.preferences)
        }

        private fun updatePreferencesLayout() {
            val p = preferenceManager.getSharedPreferences()

            findPreference<Preference>("displayResolutionExact")!!.isVisible = true

            findPreference<Preference>("displayResolutionExact")!!.setSummary(
                p?.getString(
                    "displayResolutionExact",
                    "1280x720"
                )
            )

            findPreference<Preference>("displayStretch")!!.isEnabled = true
        }

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            setListeners(preferenceScreen)
            updatePreferencesLayout()
        }

        private fun setListeners(g: PreferenceGroup) {
            for (i in 0 until g.preferenceCount) {
                g.getPreference(i).onPreferenceChangeListener = this
                g.getPreference(i).onPreferenceClickListener = this
                g.getPreference(i).setSingleLineTitle(false)
                if (g.getPreference(i) is PreferenceGroup) setListeners(g.getPreference(i) as PreferenceGroup)
            }
        }

        override fun onPreferenceClick(preference: Preference): Boolean {
            updatePreferencesLayout()
            return false
        }

        override fun onPreferenceChange(preference: Preference, newValue: Any): Boolean {
            val key = preference.key
            Log.e("Preferences", "changed preference: $key")
            Handler(Looper.getMainLooper()).postDelayed({ updatePreferencesLayout() }, 100)

            val intent = Intent(ACTION_PREFERENCES_CHANGED)
            intent.putExtra("key", key)
            intent.setPackage("com.micewine.emu")
            requireContext().sendBroadcast(intent)
            Handler(Looper.getMainLooper()).postDelayed({ updatePreferencesLayout() }, 100)

            return true
        }
    }

    companion object {
        const val ACTION_PREFERENCES_CHANGED = "com.micewine.emu.ACTION_PREFERENCES_CHANGED"
    }
}
