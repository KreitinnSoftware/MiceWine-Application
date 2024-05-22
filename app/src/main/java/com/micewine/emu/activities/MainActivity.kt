package com.micewine.emu.activities

import android.Manifest
import android.annotation.SuppressLint
import androidx.fragment.app.Fragment
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.InputDevice
import android.view.KeyEvent
import android.view.MenuItem
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.preference.PreferenceManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.micewine.emu.ControllerUtils.getGameControllerNames
import com.micewine.emu.R
import com.micewine.emu.activities.GeneralSettings.Companion.BOX64_DYNAREC_BIGBLOCK_KEY
import com.micewine.emu.activities.GeneralSettings.Companion.BOX64_DYNAREC_CALLRET_KEY
import com.micewine.emu.activities.GeneralSettings.Companion.BOX64_DYNAREC_FASTNAN_KEY
import com.micewine.emu.activities.GeneralSettings.Companion.BOX64_DYNAREC_FASTROUND_KEY
import com.micewine.emu.activities.GeneralSettings.Companion.BOX64_DYNAREC_SAFEFLAGS_KEY
import com.micewine.emu.activities.GeneralSettings.Companion.BOX64_DYNAREC_STRONGMEM_KEY
import com.micewine.emu.activities.GeneralSettings.Companion.BOX64_DYNAREC_X87DOUBLE_KEY
import com.micewine.emu.activities.GeneralSettings.Companion.SELECTED_D3DX_RENDERER_KEY
import com.micewine.emu.activities.GeneralSettings.Companion.SELECTED_DRIVER_KEY
import com.micewine.emu.activities.GeneralSettings.Companion.SELECTED_DXVK_HUD_PRESET_KEY
import com.micewine.emu.activities.GeneralSettings.Companion.SELECTED_DXVK_KEY
import com.micewine.emu.activities.GeneralSettings.Companion.SELECTED_IB_KEY
import com.micewine.emu.activities.GeneralSettings.Companion.SELECTED_THEME_KEY
import com.micewine.emu.activities.GeneralSettings.Companion.SELECTED_VIRGL_PROFILE_KEY
import com.micewine.emu.activities.GeneralSettings.Companion.SELECTED_WINED3D_KEY
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
            }

            extractedAssets = true

            fragmentLoader(HomeFragment(), false)
        }.start()

        setSharedVars(this)

        for (name in getGameControllerNames()) {
            Log.v("Controller", name)
        }
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
        var enableRamCounter: Boolean = false
        var appLang: String? = null
        var box64DynarecBigblock: String? = null
        var box64DynarecStrongmem: String? = null
        var box64DynarecX87double: String? = null
        var box64DynarecFastnan: String? = null
        var box64DynarecFastround: String? = null
        var box64DynarecSafeflags: String? = null
        var box64DynarecCallret: String? = null
        var selectedDriver: String? = null
        var selectedTheme: String? = null
        var d3dxRenderer: String? = null
        var selectedWineD3D: String? = null
        var selectedDXVK: String? = null
        var selectedIbVersion: String? = null
        var selectedVirGLProfile: String? = null
        var selectedDXVKHud: String? = null

        private fun booleanToString(boolean: Boolean): String {
            return if (boolean) {
                "1"
            } else {
                "0"
            }
        }

        fun setSharedVars(context: Context) {
            val preferences = PreferenceManager.getDefaultSharedPreferences(context)

            appLang = context.resources.getString(R.string.app_lang)

            box64DynarecBigblock = preferences.getString(BOX64_DYNAREC_BIGBLOCK_KEY, "1")
            box64DynarecStrongmem = preferences.getString(BOX64_DYNAREC_STRONGMEM_KEY, "0")
            box64DynarecX87double = booleanToString(preferences.getBoolean(BOX64_DYNAREC_X87DOUBLE_KEY, false))
            box64DynarecFastnan = booleanToString(preferences.getBoolean(BOX64_DYNAREC_FASTNAN_KEY, true))
            box64DynarecFastround = booleanToString(preferences.getBoolean(BOX64_DYNAREC_FASTROUND_KEY, true))
            box64DynarecSafeflags = preferences.getString(BOX64_DYNAREC_SAFEFLAGS_KEY, "1")
            box64DynarecCallret = booleanToString(preferences.getBoolean(BOX64_DYNAREC_CALLRET_KEY, true))
            selectedDriver = preferences.getString(SELECTED_DRIVER_KEY, "Turnip/Zink")
            selectedTheme = preferences.getString(SELECTED_THEME_KEY, "DarkBlue")
            d3dxRenderer = preferences.getString(SELECTED_D3DX_RENDERER_KEY, "DXVK")
            selectedWineD3D = preferences.getString(SELECTED_WINED3D_KEY, "WineD3D-9.0")
            selectedDXVK = preferences.getString(SELECTED_DXVK_KEY, "DXVK")
            selectedIbVersion = preferences.getString(SELECTED_IB_KEY, "0.1.8")
            selectedVirGLProfile = preferences.getString(SELECTED_VIRGL_PROFILE_KEY, "GL 3.3")
            selectedDXVKHud = preferences.getString(SELECTED_DXVK_HUD_PRESET_KEY, "fps,devinfo,gpuload")
        }
        
        private fun copyAssets(context: Context, filename: String, outputPath: String) {
            val assetManager = context.assets
            var input: InputStream? = null
            var out: OutputStream? = null
            try {
                input = assetManager.open(filename)
                val outFile = File(outputPath, filename)
                out = Files.newOutputStream(outFile.toPath())
                copyFile(input, out)
            } catch (e: IOException) {
                e.printStackTrace()
            } finally {
                try {
                    input?.close()
                    out?.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }

        @Throws(IOException::class)
        private fun copyFile(input: InputStream, out: OutputStream?) {
            val buffer = ByteArray(1024)
            var read: Int
            while (input.read(buffer).also { read = it } != -1) {
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
