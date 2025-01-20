package com.micewine.emu.activities

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION
import android.view.KeyEvent
import android.view.KeyEvent.KEYCODE_BACK
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.micewine.emu.R
import com.micewine.emu.activities.MainActivity.Companion.ACTION_SETUP
import com.micewine.emu.activities.MainActivity.Companion.appBuiltinRootfs
import com.micewine.emu.databinding.ActivityWelcomeBinding
import com.micewine.emu.fragments.WelcomeFragment

class WelcomeActivity : AppCompatActivity() {
    private var binding: ActivityWelcomeBinding? = null
    private var button: Button? = null
    private val fragmentList: List<Fragment> = listOf(WelcomeFragment(R.layout.fragment_welcome), WelcomeFragment(R.layout.fragment_welcome_2))
    private var selectedFragment = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityWelcomeBinding.inflate(layoutInflater)
        setContentView(binding!!.root)

        fragmentLoader(true)

        button = findViewById(R.id.continueButton)
        button?.setOnClickListener {
            if (selectedFragment == 1) {
                if (fileManagementPermission) {
                    checkPermission()
                }
            }

            selectedFragment++

            if (selectedFragment < fragmentList.size) {
                fragmentLoader(false)
            }

            when (selectedFragment) {
                1 -> {
                    checkPermission()
                }

                2 -> {
                    if (appBuiltinRootfs) {
                        sendBroadcast(
                            Intent(ACTION_SETUP)
                        )
                    }

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
                val intent = Intent(ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                val uri = Uri.fromParts("package", packageName, null)
                intent.setData(uri)
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