package com.micewine.emu.activities

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
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
    private val init = Init()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding!!.root)

        findViewById<Toolbar>(R.id.mainActivityToolbar).title = getString(R.string.app_name)

        val bottomNavigation = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNavigation.setOnItemSelectedListener { item: MenuItem ->
            val id = item.itemId

            if (id == R.id.nav_home) {
                fragmentLoader(HomeFragment(), false)
            } else if (id == R.id.nav_settings) {
                fragmentLoader(SettingsFragment(), false)
            }

            true
        }

        val fab = findViewById<FloatingActionButton>(R.id.addItemFAB)

        fab.setOnClickListener {
            openFilePicker()
        }

        if (!usrDir.exists()) {
            val intent = Intent(this, WelcomeActivity::class.java)
            this.startActivity(intent)
        } else {
            extractedAssets = true
        }

        fragmentLoader(HomeFragment(), true)
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)

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

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(
        requestCode: Int, resultCode: Int, data: Intent?
    ) {
        if (requestCode == 1 && resultCode == Activity.RESULT_OK) {
            data?.data?.also { uri ->
                saveToGameList(this, uriParser(uri), File(uriParser(uri)).nameWithoutExtension)
            }
        }

        setSharedVars(this)

        super.onActivityResult(requestCode, resultCode, data)
    }


    @Suppress("DEPRECATION")
    private fun openFilePicker() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.setType("*/*")
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        startActivityForResult(
            Intent.createChooser(intent, "Escolha um arquivo .exe"),
            1
        )
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

    override fun onDestroy() {
        super.onDestroy()
        init.stopAll()
        binding = null
    }

    override fun onResume() {
        super.onResume()

        fragmentLoader(HomeFragment(), false)
    }

    companion object {
        const val PERMISSION_REQUEST_CODE = 123

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
            box64DynarecX87double =
                booleanToString(preferences.getBoolean(BOX64_DYNAREC_X87DOUBLE_KEY, false))
            box64DynarecFastnan =
                booleanToString(preferences.getBoolean(BOX64_DYNAREC_FASTNAN_KEY, true))
            box64DynarecFastround =
                booleanToString(preferences.getBoolean(BOX64_DYNAREC_FASTROUND_KEY, true))
            box64DynarecSafeflags = preferences.getString(BOX64_DYNAREC_SAFEFLAGS_KEY, "1")
            box64DynarecCallret =
                booleanToString(preferences.getBoolean(BOX64_DYNAREC_CALLRET_KEY, true))
            selectedDriver = preferences.getString(SELECTED_DRIVER_KEY, "Turnip/Zink")
            selectedTheme = preferences.getString(SELECTED_THEME_KEY, "DarkBlue")
            d3dxRenderer = preferences.getString(SELECTED_D3DX_RENDERER_KEY, "DXVK")
            selectedWineD3D = preferences.getString(SELECTED_WINED3D_KEY, "WineD3D-9.0")
            selectedDXVK = preferences.getString(SELECTED_DXVK_KEY, "DXVK-1.10.3-async")
            selectedIbVersion = preferences.getString(SELECTED_IB_KEY, "0.1.8")
            selectedVirGLProfile = preferences.getString(SELECTED_VIRGL_PROFILE_KEY, "GL 3.3")
            selectedDXVKHud =
                preferences.getString(SELECTED_DXVK_HUD_PRESET_KEY, "fps,devinfo,gpuload")
        }

        fun copyAssets(context: Context, filename: String, outputPath: String, textView: TextView) {
            textView.text = context.getString(R.string.extracting_from_assets)

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

        private fun getLibsPath(context: Context): String {
            return context.applicationContext.applicationInfo.nativeLibraryDir
        }

        private fun saveToGameList(context: Context, path: String, prettyName: String) {
            val preferences = PreferenceManager.getDefaultSharedPreferences(context)
            val editor = preferences.edit()

            val currentList = loadGameList(context)

            currentList.add(arrayOf(prettyName, path))

            val gson = Gson()
            val json = gson.toJson(currentList)

            editor.putString("gameList", json)
            editor.apply()
        }

        fun loadGameList(context: Context): MutableList<Array<String>> {
            val preferences = PreferenceManager.getDefaultSharedPreferences(context)
            val gson = Gson()

            val json = preferences.getString("gameList", "")

            val listType = object : TypeToken<MutableList<Array<String>>>() {}.type

            return gson.fromJson(json, listType) ?: mutableListOf()
        }

        private fun uriParser(uri: Uri): String {
            var path = uri.path.toString()

            if (path.contains("primary")) {
                path = "/storage/emulated/0/" + path.split(":")[1]
            }

            return path
        }
    }
}
