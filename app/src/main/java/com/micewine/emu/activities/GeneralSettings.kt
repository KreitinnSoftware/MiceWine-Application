package com.micewine.emu.activities

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import com.google.android.material.appbar.CollapsingToolbarLayout
import com.google.android.material.appbar.MaterialToolbar
import com.micewine.emu.R
import com.micewine.emu.databinding.GeneralSettingsBinding
import com.micewine.emu.fragments.GeneralSettingsFragment
import com.micewine.emu.fragments.HomeFragment
import com.micewine.emu.fragments.SettingsFragment

class GeneralSettings : AppCompatActivity() {
    private var binding: GeneralSettingsBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = GeneralSettingsBinding.inflate(layoutInflater)
        setContentView(binding!!.root)

        fragmentLoader(GeneralSettingsFragment())
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId

        if (id == android.R.id.home) {
            finish()
            return true
        }

        return super.onOptionsItemSelected(item)
    }

    private fun fragmentLoader(fragment: Fragment) {
        val fragmentTransaction = supportFragmentManager.beginTransaction()

        fragmentTransaction.replace(R.id.settings_content, fragment)

        fragmentTransaction.commit()
    }
}