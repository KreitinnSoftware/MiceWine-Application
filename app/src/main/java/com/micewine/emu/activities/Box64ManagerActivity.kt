package com.micewine.emu.activities

import android.os.Bundle
import android.view.KeyEvent
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import com.micewine.emu.R
import com.micewine.emu.activities.MainActivity.Companion.setSharedVars
import com.micewine.emu.databinding.ActivityBox64ManagerBinding
import com.micewine.emu.fragments.Box64ListFragment
import java.io.File

class Box64ManagerActivity : AppCompatActivity() {
    private var binding: ActivityBox64ManagerBinding? = null
    private var backButton: ImageButton? = null
    private var ratManagerToolBar: Toolbar? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityBox64ManagerBinding.inflate(layoutInflater)
        setContentView(binding!!.root)

        backButton = findViewById(R.id.backButton)
        backButton?.setOnClickListener {
            onKeyDown(KeyEvent.KEYCODE_BACK, null)
        }

        ratManagerToolBar = findViewById(R.id.box64ManagerToolbar)
        ratManagerToolBar?.setTitle(R.string.box64_manager_title)

        fragmentLoader(Box64ListFragment())
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            finish()
        }

        return true
    }

    override fun onDestroy() {
        super.onDestroy()
        setSharedVars(this)
    }

    private fun fragmentLoader(fragment: Fragment) {
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.box64_manager_content, fragment)
            commit()
        }
    }
}