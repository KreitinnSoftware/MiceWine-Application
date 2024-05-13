package com.micewine.emu.activities

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.appbar.CollapsingToolbarLayout
import com.google.android.material.appbar.MaterialToolbar
import com.micewine.emu.R
import com.micewine.emu.core.ShellExecutorCmd
import com.micewine.emu.databinding.LayoutlogShellOutputBinding
import com.micewine.emu.viewmodels.ViewModelAppLogs

class LogAppOutput : AppCompatActivity() {
    private var binding: LayoutlogShellOutputBinding? = null
    private var sharedLogs: ViewModelAppLogs? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = LayoutlogShellOutputBinding.inflate(layoutInflater)
        setContentView(binding!!.getRoot())
        val action = supportActionBar
        action?.hide()
        val toolBar = findViewById<MaterialToolbar>(R.id.toolbar_log)
        setSupportActionBar(toolBar)
        val collapsingToolBar = findViewById<CollapsingToolbarLayout>(R.id.toolbar_log_layout)
        collapsingToolBar.title = "Logs"
        sharedLogs = ViewModelProvider(this)[ViewModelAppLogs::class.java]
        sharedLogs!!.setText(ShellExecutorCmd.stdOut)
        sharedLogs!!.textLiveData.observe(this) { out: String? -> binding!!.logShell.text = out }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_clear, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == android.R.id.home) {
            finish()
            return true
        } else if (id == R.id.clear) {
            binding!!.logShell.text = ""
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }
}
