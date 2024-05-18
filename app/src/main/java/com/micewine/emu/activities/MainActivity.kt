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
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.preference.PreferenceManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.micewine.emu.R
import com.micewine.emu.core.Init
import com.micewine.emu.core.ObbExtractor
import com.micewine.emu.core.ShellExecutorCmd
import com.micewine.emu.databinding.MainActivityBinding
import com.micewine.emu.fragments.HomeFragment
import com.micewine.emu.fragments.SettingsFragment
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.nio.file.Files

class MainActivity : AppCompatActivity() {
    private var binding: MainActivityBinding? = null
    private var progressExtractBar: ProgressBar? = null
    private var progressTextBar: TextView? = null
    private val init = Init()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = MainActivityBinding.inflate(layoutInflater)
        setContentView(binding!!.root)
        progressExtractBar = findViewById(R.id.progressBar)
        findViewById<View>(R.id.updateProgress)

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
        progressTextBar = findViewById(R.id.updateProgress)
        progressExtractBar?.isIndeterminate = true
        progressExtractBar?.visibility = View.VISIBLE

        Thread {
            if (!usrDir.exists()) {
                copyAssets(this, "rootfs.zip", appRootDir.toString())

                ObbExtractor().extractZip("$appRootDir/rootfs.zip", "$appRootDir", progressExtractBar, progressTextBar, this)

                ShellExecutorCmd.ExecuteCMD("rm $appRootDir/rootfs.zip", "ExtractUtility")
                ShellExecutorCmd.ExecuteCMD("chmod 775 -R $appRootDir", "ExtractUtility")
                ShellExecutorCmd.ExecuteCMD("$usrDir/generateSymlinks.sh", "ExtractUtility")
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
            }

            extractedAssets = true

            fragmentLoader(HomeFragment(), false)
        }.start()

        setSharedVars(this)
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
        var usrDir = File("$appRootDir/usr")
        var tmpDir = File("$usrDir/tmp")
        var homeDir = File("$appRootDir/home")
        var extractedAssets: Boolean = false
        var box64_dynarec_bigblock: String? = null
        var box64_dynarec_strongmem: String? = null
        var box64_dynarec_x87double: String? = null
        var box64_dynarec_fastnan: String? = null
        var box64_dynarec_fastround: String? = null
        var box64_dynarec_safeflags: String? = null
        var box64_dynarec_callret: String? = null

        private fun booleanToString(boolean: Boolean): String {
            return if (boolean) {
                "1"
            } else {
                "0"
            }
        }

        fun setSharedVars(context: Context) {
            val preferences = PreferenceManager.getDefaultSharedPreferences(context)!!

            box64_dynarec_bigblock = preferences.getString(context.resources.getString(R.string.box64_bigblock_title), "1")!!
            box64_dynarec_strongmem = preferences.getString(context.resources.getString(R.string.box64_strongmem_title), "0")!!
            box64_dynarec_x87double = booleanToString(preferences.getBoolean(context.resources.getString(R.string.box64_x87double_title), false))
            box64_dynarec_fastnan = booleanToString(preferences.getBoolean(context.resources.getString(R.string.box64_fastnan_title), true))
            box64_dynarec_fastround = booleanToString(preferences.getBoolean(context.resources.getString(R.string.box64_fastround_title), true))
            box64_dynarec_safeflags = preferences.getString(context.resources.getString(R.string.box64_safeflags_title), "1")!!
            box64_dynarec_callret = booleanToString(preferences.getBoolean(context.resources.getString(R.string.box64_callret_title), true))
        }
        
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

        fun getClassPath(context: Context): String {
            return File(getLibsPath(context)).parentFile?.parentFile?.absolutePath + "/base.apk"
        }

        fun getLibsPath(context: Context): String {
            return context.applicationContext.applicationInfo.nativeLibraryDir
        }
    }
}
