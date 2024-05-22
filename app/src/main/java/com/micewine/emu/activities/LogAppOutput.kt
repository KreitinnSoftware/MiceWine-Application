package com.micewine.emu.activities

import android.os.Bundle
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.ViewModelProvider
import com.micewine.emu.R
import com.micewine.emu.core.ShellExecutorCmd.stdErrOut
import com.micewine.emu.databinding.LogViewerBinding
import com.micewine.emu.viewmodels.ViewModelAppLogs

class LogAppOutput : AppCompatActivity() {
    private var binding: LogViewerBinding? = null
    private var sharedLogs: ViewModelAppLogs? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = LogViewerBinding.inflate(layoutInflater)
        setContentView(binding!!.getRoot())

        val toolBar = findViewById<Toolbar>(R.id.logViewerToolbar)

        toolBar.title = resources.getString(R.string.logTitle)

        val backButton = findViewById<ImageButton>(R.id.backButton)

        backButton?.setOnClickListener {
            finish()
        }

        val clearButton = findViewById<ImageButton>(R.id.clearButton)

        clearButton?.setOnClickListener {
            stdErrOut = ""
            sharedLogs!!.setText("")
        }

        sharedLogs = ViewModelProvider(this)[ViewModelAppLogs::class.java]
        sharedLogs!!.setText(stdErrOut)
        sharedLogs!!.textLiveData.observe(this) { out: String? -> binding!!.logShell.text = out }
    }

    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }
}
