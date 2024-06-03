package com.micewine.emu.activities

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.micewine.emu.R
import com.micewine.emu.activities.MainActivity.Companion.extractedAssets
import com.micewine.emu.activities.MainActivity.Companion.appRootDir
import com.micewine.emu.activities.MainActivity.Companion.homeDir
import com.micewine.emu.activities.MainActivity.Companion.tmpDir
import com.micewine.emu.activities.MainActivity.Companion.usrDir
import com.micewine.emu.core.ObbExtractor
import com.micewine.emu.core.ShellExecutorCmd
import com.micewine.emu.databinding.ActivityWelcomeBinding
import com.micewine.emu.fragments.Welcome2Fragment
import com.micewine.emu.fragments.Welcome3Fragment
import com.micewine.emu.fragments.WelcomeFragment

class WelcomeActivity : AppCompatActivity() {
    private var binding: ActivityWelcomeBinding? = null
    private var button: Button? = null
    private val fragmentList: List<Fragment> = listOf(WelcomeFragment(), Welcome2Fragment(), Welcome3Fragment())
    private var selectedFragment = 0
    private var progressExtractBar: ProgressBar? = null
    private var progressTextBar: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityWelcomeBinding.inflate(layoutInflater)
        setContentView(binding!!.root)

        fragmentLoader(true)

        progressExtractBar = findViewById(R.id.progressBar)
        progressTextBar = findViewById(R.id.updateProgress)
        progressExtractBar?.isIndeterminate = true
        progressExtractBar?.visibility = View.INVISIBLE

        val extractThread = Thread {
            if (!usrDir.exists()) {
                MainActivity.copyAssets(this, "rootfs.zip", appRootDir.toString(), progressTextBar!!)

                ObbExtractor().extractZip("$appRootDir/rootfs.zip", "$appRootDir", progressExtractBar, progressTextBar, this)

                ShellExecutorCmd.executeShell("rm $appRootDir/rootfs.zip", "ExtractUtility")
                ShellExecutorCmd.executeShell("chmod 775 -R $appRootDir", "ExtractUtility")
                ShellExecutorCmd.executeShell("$usrDir/generateSymlinks.sh", "ExtractUtility")
            }

            if (!tmpDir.exists()) {
                tmpDir.mkdirs()
            }

            if (!homeDir.exists()) {
                homeDir.mkdirs()
            }

            runOnUiThread {
                progressExtractBar?.visibility = View.GONE
                progressTextBar?.visibility = View.GONE
                button?.visibility = View.VISIBLE
            }

            extractedAssets = true
        }

        button = findViewById(R.id.continueButton)

        button?.setOnClickListener {
            if (selectedFragment == 1) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    checkPermission()
                } else {
                    selectedFragment++
                    fragmentLoader(false)
                }
            } else {
                selectedFragment++
                fragmentLoader(false)
            }

            when (selectedFragment) {
                1 -> {
                    checkPermission()
                }

                2 -> {
                    progressExtractBar?.visibility = View.VISIBLE
                    button?.visibility = View.INVISIBLE
                    extractThread.start()
                }

                3 -> {
                    finish()
                }
            }
        }
    }

    private fun fragmentLoader(appInit: Boolean) {
        val fragmentTransaction = supportFragmentManager.beginTransaction()

        if (selectedFragment < fragmentList.size) {
            fragmentTransaction.replace(R.id.welcomeContent, fragmentList[selectedFragment])

            if (!appInit) {
                fragmentTransaction.addToBackStack(null)
            }

            fragmentTransaction.commit()
        }
    }

    private fun checkPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                    this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), MainActivity.PERMISSION_REQUEST_CODE
            )
        }
    }
}