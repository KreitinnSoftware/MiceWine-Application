package com.micewine.emu.activities

import android.annotation.SuppressLint
import android.app.Activity
import android.app.ActivityManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.graphics.BitmapFactory
import android.graphics.drawable.Icon
import android.net.Uri
import android.os.Bundle
import android.view.ContextMenu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
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
import com.micewine.emu.databinding.ActivityMainBinding
import com.micewine.emu.fragments.DeleteGameItemFragment
import com.micewine.emu.fragments.HomeFragment
import com.micewine.emu.fragments.RenameGameItemFragment
import com.micewine.emu.fragments.SettingsFragment
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.nio.file.Files

class MainActivity : AppCompatActivity() {
    private var binding: ActivityMainBinding? = null
    private val receiver: BroadcastReceiver = object : BroadcastReceiver() {
        @SuppressLint("UnspecifiedRegisterReceiverFlag")
        override fun onReceive(context: Context, intent: Intent) {
            if (ACTION_UPDATE_HOME == intent.action) {
                fragmentLoader(HomeFragment(), false)
            }
        }
    }

    private var selectedFragment = "HomeFragment"
    private var fab: FloatingActionButton? = null
    private var bottomNavigation: BottomNavigationView? = null

    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding!!.root)

        setSharedVars(this)

        fab = findViewById(R.id.addItemFAB)
        bottomNavigation = findViewById(R.id.bottom_navigation)

        bottomNavigation?.setOnItemSelectedListener { item: MenuItem ->
            val id = item.itemId

            if (id == R.id.nav_home) {
                selectedFragment = "HomeFragment"
                fragmentLoader(HomeFragment(), false)
            } else if (id == R.id.nav_settings) {
                selectedFragment = "SettingsFragment"
                fragmentLoader(SettingsFragment(), false)
            }

            true
        }

        fab?.setOnClickListener {
            openFilePicker(SELECT_EXE)
        }

        if (!usrDir.exists()) {
            val intent = Intent(this, WelcomeActivity::class.java)
            this.startActivity(intent)
        } else {
            extractedAssets = true
        }

        selectedFragment = "HomeFragment"
        fragmentLoader(HomeFragment(), true)

        registerReceiver(receiver, object : IntentFilter(ACTION_UPDATE_HOME) {})
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)

        val exePath = intent.getStringExtra("exePath")

        if (exePath != null) {
            val intent = Intent(this, EmulationActivity::class.java)

            intent.putExtra("exePath", exePath)

            enableRamCounter = true

            intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
            startActivityIfNeeded(intent, 0)
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

    override fun onCreateContextMenu(
        menu: ContextMenu?,
        v: View?,
        menuInfo: ContextMenu.ContextMenuInfo?
    ) {
        val inflater = menuInflater
        inflater.inflate(R.menu.game_list_context_menu, menu)
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        when (item.title) {
            getString(R.string.addGameToHome) -> {
                addGameToHome(this, selectedGameArray)
            }

            getString(R.string.editGameIcon) -> {
                openFilePicker(SELECT_ICON)
            }

            getString(R.string.removeGameItem) -> {
                DeleteGameItemFragment().show(supportFragmentManager, "")
            }

            getString(R.string.renameGameItem) -> {
                RenameGameItemFragment().show(supportFragmentManager, "")
            }
        }

        return super.onContextItemSelected(item)
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(
        requestCode: Int, resultCode: Int, data: Intent?
    ) {
        if (requestCode == SELECT_EXE && resultCode == Activity.RESULT_OK) {
            data?.data?.also { uri ->
                uriParser(uri).also {
                    if (it.endsWith(".exe") || it.endsWith(".bat")) {
                        saveToGameList(this, it, File(it).nameWithoutExtension, "")
                    } else {
                        Toast.makeText(this, getString(R.string.incompatibleSelectedFile), Toast.LENGTH_SHORT).show()
                    }
                }
            }
        } else if (requestCode == SELECT_ICON && resultCode == Activity.RESULT_OK) {
            data?.data?.also { uri ->
                uriParser(uri).also {
                    setIconToGame(this, it, selectedGameArray[0])
                }
            }
        }

        setSharedVars(this)

        super.onActivityResult(requestCode, resultCode, data)
    }


    @Suppress("DEPRECATION")
    private fun openFilePicker(requestCode: Int) {
        var intent: Intent? = null

        if (requestCode == SELECT_EXE) {
            intent = Intent(Intent.ACTION_GET_CONTENT).apply {
                type = "application/*"
                addCategory(Intent.CATEGORY_OPENABLE)
            }
        } else if (requestCode == SELECT_ICON) {
            intent = Intent(Intent.ACTION_GET_CONTENT).apply {
                type = "image/*"
                addCategory(Intent.CATEGORY_OPENABLE)
            }
        }

        startActivityForResult(
            Intent.createChooser(intent, getString(R.string.selectExecutableFile)),  requestCode
        )
    }

    private fun fragmentLoader(fragment: Fragment, appInit: Boolean) {
        val fragmentTransaction = supportFragmentManager.beginTransaction()

        if (appInit) {
            fragmentTransaction.add(R.id.content, fragment)
        } else {
            fragmentTransaction.replace(R.id.content, fragment)
        }

        fab?.isVisible = selectedFragment == "HomeFragment"

        fragmentTransaction.commit()
    }

    override fun onDestroy() {
        super.onDestroy()
        binding = null
        unregisterReceiver(receiver)
    }

    override fun onResume() {
        super.onResume()

        if (selectedFragment == "HomeFragment") {
            fragmentLoader(HomeFragment(), false)
        } else if (selectedFragment == "SettingsFragment") {
            fragmentLoader(SettingsFragment(), false)
        }
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
        var selectedGameArray: Array<String> = arrayOf()
        var classPath: String? = null

        const val ACTION_UPDATE_HOME = "com.micewine.emu.ACTION_UPDATE_HOME"
        const val RAM_COUNTER_KEY = "ramCounter"
        const val SELECT_EXE = 1
        const val SELECT_ICON = 2

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
            selectedDXVK = preferences.getString(SELECTED_DXVK_KEY, "DXVK-1.10.3-async")
            selectedIbVersion = preferences.getString(SELECTED_IB_KEY, "0.1.8")
            selectedVirGLProfile = preferences.getString(SELECTED_VIRGL_PROFILE_KEY, "GL 3.3")
            selectedDXVKHud = preferences.getString(SELECTED_DXVK_HUD_PRESET_KEY, "FPS/GPU Load")
            enableRamCounter = preferences.getBoolean(RAM_COUNTER_KEY, false)
            classPath = getClassPath(context)
        }

        fun copyAssets(activity: Activity, filename: String, outputPath: String, textView: TextView) {
            activity.runOnUiThread {
                textView.text = activity.getString(R.string.extracting_from_assets)
            }

            val assetManager = activity.assets
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

        private fun saveToGameList(context: Context, path: String, prettyName: String, icon: String) {
            val preferences = PreferenceManager.getDefaultSharedPreferences(context)
            val editor = preferences.edit()

            val currentList = loadGameList(context)

            val game = arrayOf(prettyName, path, icon)

            if (!checkIfExists(context, game)) {
                currentList.add(game)
            } else {
                Toast.makeText(context, context.getString(R.string.executableAlreadyAdded), Toast.LENGTH_SHORT).show()
            }

            val gson = Gson()
            val json = gson.toJson(currentList)

            editor.putString("gameList", json)
            editor.apply()

            val intent = Intent(ACTION_UPDATE_HOME)
            context.sendBroadcast(intent)
        }

        fun loadGameList(context: Context): MutableList<Array<String>> {
            val preferences = PreferenceManager.getDefaultSharedPreferences(context)
            val gson = Gson()

            val json = preferences.getString("gameList", "")

            val listType = object : TypeToken<MutableList<Array<String>>>() {}.type

            return gson.fromJson(json, listType) ?: mutableListOf()
        }

        fun removeGameFromList(context: Context, array: Array<String>) {
            val preferences = PreferenceManager.getDefaultSharedPreferences(context)
            val editor = preferences.edit()

            val currentList = loadGameList(context)

            currentList.removeIf { it[0] == array[0] && it[1] == array[1] }

            val gson = Gson()
            val json = gson.toJson(currentList)

            editor.putString("gameList", json)
            editor.apply()

            val intent = Intent(ACTION_UPDATE_HOME)
            context.sendBroadcast(intent)
        }

        fun renameGameFromList(context: Context, array: Array<String>, newName: String) {
            val preferences = PreferenceManager.getDefaultSharedPreferences(context)
            val editor = preferences.edit()

            val currentList = loadGameList(context)

            val index = currentList.indexOfFirst { it[0] == array[0] }

            currentList[index][0] = newName

            val gson = Gson()
            val json = gson.toJson(currentList)

            editor.putString("gameList", json)
            editor.apply()

            val intent = Intent(ACTION_UPDATE_HOME)
            context.sendBroadcast(intent)
        }

        fun setIconToGame(context: Context, icon: String, gameName: String) {
            val preferences = PreferenceManager.getDefaultSharedPreferences(context)
            val editor = preferences.edit()

            val currentList = loadGameList(context)

            val index = currentList.indexOfFirst { it[0] == gameName }

            currentList[index][2] = icon

            val gson = Gson()
            val json = gson.toJson(currentList)

            editor.putString("gameList", json)
            editor.apply()

            val intent = Intent(ACTION_UPDATE_HOME)
            context.sendBroadcast(intent)
        }

        private fun checkIfExists(context: Context, array: Array<String>): Boolean {
            val currentList = loadGameList(context)

            return currentList.any { it[0] == array[0] && it[1] == array[1] }
        }

        private fun uriParser(uri: Uri): String {
            var path = uri.path.toString()

            if (path.contains("primary")) {
                path = "/storage/emulated/0/" + path.split(":")[1]
            }

            return path
        }

        fun getMemoryInfo(context: Context): String {
            val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            val memoryInfo = ActivityManager.MemoryInfo()
            activityManager.getMemoryInfo(memoryInfo)

            val totalMemory = memoryInfo.totalMem / (1024 * 1024)
            val availableMemory = memoryInfo.availMem / (1024 * 1024)
            val usedMemory = totalMemory - availableMemory

            return "RAM: $usedMemory/$totalMemory"
        }

        fun addGameToHome(context: Context, selectedGameArray: Array<String>) {
            val shortcutManager = context.getSystemService(ShortcutManager::class.java)

            if (shortcutManager!!.isRequestPinShortcutSupported) {
                val intent = Intent(context, MainActivity::class.java).apply {
                    action = Intent.ACTION_VIEW
                    putExtra("exePath", selectedGameArray[1])
                }

                val pinShortcutInfo = ShortcutInfo.Builder(context, selectedGameArray[0])
                    .setShortLabel(selectedGameArray[0])
                    .setIcon(
                        if (selectedGameArray[2] == "" || !File(selectedGameArray[2]).exists()) {
                            Icon.createWithResource(context, R.drawable.default_icon)
                        } else {
                            Icon.createWithBitmap(BitmapFactory.decodeFile(selectedGameArray[2]))
                        })

                    .setIntent(intent)
                    .build()

                val pinnedShortcutCallbackIntent = shortcutManager.createShortcutResultIntent(pinShortcutInfo)

                val successCallback = PendingIntent.getBroadcast(context, 0,
                    pinnedShortcutCallbackIntent, PendingIntent.FLAG_IMMUTABLE)

                shortcutManager.requestPinShortcut(pinShortcutInfo,
                    successCallback.intentSender)
            }
        }
    }
}
