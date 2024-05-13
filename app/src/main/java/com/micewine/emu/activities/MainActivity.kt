package com.micewine.emu.activities

import android.Manifest
import android.annotation.SuppressLint
import androidx.fragment.app.Fragment
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.micewine.emu.R
import com.micewine.emu.core.Init
import com.micewine.emu.core.ShellExecutorCmd
import com.micewine.emu.databinding.ActivityMainBinding
import com.micewine.emu.fragments.HomeFragment
import com.micewine.emu.fragments.SettingsFragment
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.nio.file.Files

class MainActivity : AppCompatActivity() {
    private var binding: ActivityMainBinding? = null
    private var progressExtractBar: ProgressBar? = null
    private val init = Init()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding!!.getRoot())
        progressExtractBar = findViewById(R.id.progressBar)
        findViewById<View>(R.id.updateProgress)
        val actionBar = supportActionBar
        actionBar?.setTitle(R.string.app_name)
        val bottomNavigation = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNavigation.setOnItemSelectedListener { item: MenuItem ->
            val id = item.itemId
            if (id == R.id.nav_Home) {
                fragmentLoader(HomeFragment(), false)
            } else if (id == R.id.nav_settings) {
                fragmentLoader(SettingsFragment(), false)
            }
            true
        }
        manageFilesPath()
        checkPermission()
        fragmentLoader(HomeFragment(), true)
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        progressExtractBar = findViewById(R.id.progressBar)
        findViewById<View>(R.id.updateProgress)
        progressExtractBar?.isIndeterminate = true
        progressExtractBar?.visibility = View.VISIBLE

        Thread {
            if (!usrDir.exists()) {
                copyAssets(this, "rootfs.zip", appRootDir.toString())
                ShellExecutorCmd.ExecuteCMD("unzip -o $appRootDir/rootfs.zip -d $appRootDir", "ExtractUtility")
                ShellExecutorCmd.ExecuteCMD("rm $appRootDir/rootfs.zip", "ExtractUtility")
                ShellExecutorCmd.ExecuteCMD("chmod 775 -R $appRootDir", "ExtractUtility")
                ShellExecutorCmd.ExecuteCMD("$usrDir/generateSymlinks.sh", "ExtractUtility")
            }

            if (!shellLoader.exists()) {
                copyAssets(this, "loader.apk", appRootDir.toString())
            }

            if (!tmpDir.exists()) {
                tmpDir.mkdirs()
            }

            if (!homeDir.exists()) {
                homeDir.mkdirs()
            }

            runOnUiThread {
                progressExtractBar?.visibility = View.GONE
            }
        }.start()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun checkPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), PERMISSION_REQUEST_CODE
            )
        }
    }

    private fun fragmentLoader(fragment: Fragment, appInit: Boolean) {
        val fragmentTransaction = supportFragmentManager.beginTransaction()

        if (appInit) {
            fragmentTransaction.add(R.id.content, fragment)
        } else {
            fragmentTransaction.replace(R.id.content, fragment)
        }

        fragmentTransaction.commit()
    }

    private fun manageFilesPath() {
        if (!appRootDir.exists()) {
            appRootDir.mkdirs()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        init.stopAll()
        binding = null
    }

    companion object {
        private const val PERMISSION_REQUEST_CODE = 123

        @SuppressLint("SdCardPath")
        var appRootDir = File("/data/data/com.micewine.emu/files")
        var shellLoader = File("$appRootDir/loader.apk")
        var usrDir = File("$appRootDir/usr")
        var tmpDir = File("$usrDir/tmp")
        var homeDir = File("$appRootDir/home")
        private fun copyAssets(context: Context, filename: String, outputPath: String) {
            val assetManager = context.assets
            var `in`: InputStream? = null
            var out: OutputStream? = null
            try {
                `in` = assetManager.open(filename)
                val outFile = File(outputPath, filename)
                out = Files.newOutputStream(outFile.toPath())
                copyFile(`in`, out)
            } catch (e: IOException) {
                e.printStackTrace()
            } finally {
                try {
                    `in`?.close()
                    out?.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }

        @Throws(IOException::class)
        private fun copyFile(`in`: InputStream, out: OutputStream?) {
            val buffer = ByteArray(1024)
            var read: Int
            while (`in`.read(buffer).also { read = it } != -1) {
                out!!.write(buffer, 0, read)
            }
        }
    }
}
