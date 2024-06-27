package com.micewine.emu.activities

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION
import android.view.KeyEvent
import android.view.KeyEvent.KEYCODE_BACK
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.micewine.emu.R
import com.micewine.emu.activities.MainActivity.Companion.appRootDir
import com.micewine.emu.activities.MainActivity.Companion.extractedAssets
import com.micewine.emu.activities.MainActivity.Companion.homeDir
import com.micewine.emu.activities.MainActivity.Companion.tmpDir
import com.micewine.emu.activities.MainActivity.Companion.usrDir
import com.micewine.emu.core.ObbExtractor.extractZip
import com.micewine.emu.core.ShellExecutorCmd
import com.micewine.emu.databinding.ActivityWelcomeBinding
import com.micewine.emu.fragments.Welcome2Fragment
import com.micewine.emu.fragments.Welcome3Fragment
import com.micewine.emu.fragments.WelcomeFragment
import java.io.File

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
            if (!appRootDir.exists())
                appRootDir.mkdirs()

            if (!usrDir.exists()) {
                MainActivity.copyAssets(this, "rootfs.zip", appRootDir.toString(), progressTextBar!!)

                extractZip("$appRootDir/rootfs.zip", "$appRootDir", progressExtractBar, progressTextBar, this@WelcomeActivity)

                File("$appRootDir/rootfs.zip").delete()

                ShellExecutorCmd.executeShell("chmod 775 -R $appRootDir", "ExtractUtility")
                ShellExecutorCmd.executeShell("$usrDir/generateSymlinks.sh", "ExtractUtility")
            }

            if (!tmpDir.exists())
                tmpDir.mkdirs()

            if (!homeDir.exists())
                homeDir.mkdirs()

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
                if (fileManagementPermission) {
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

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KEYCODE_BACK && selectedFragment == 0) {
            return true
        }

        return super.onKeyDown(keyCode, event)
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                val intent = Intent(ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
                startActivity(intent)
            }
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(this,
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    REQUEST_CODE)
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE) {
            fileManagementPermission = grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED
        }
    }


    companion object {
        private const val REQUEST_CODE = 1000
        private var fileManagementPermission = false
    }
}