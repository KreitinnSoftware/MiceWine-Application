package com.micewine.emu.activities

import android.annotation.SuppressLint
import android.app.Activity
import android.app.ActivityManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.graphics.BitmapFactory
import android.graphics.drawable.Icon
import android.os.Build
import android.os.Bundle
import android.view.ContextMenu
import android.view.KeyEvent
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.preference.PreferenceManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.micewine.emu.BuildConfig
import com.micewine.emu.R
import com.micewine.emu.activities.DriverManagerActivity.Companion.generateICDFile
import com.micewine.emu.activities.EmulationActivity.Companion.sharedLogs
import com.micewine.emu.activities.GeneralSettings.Companion.BOX64_AVX_KEY
import com.micewine.emu.activities.GeneralSettings.Companion.BOX64_DYNAREC_ALIGNED_ATOMICS_KEY
import com.micewine.emu.activities.GeneralSettings.Companion.BOX64_DYNAREC_BIGBLOCK_KEY
import com.micewine.emu.activities.GeneralSettings.Companion.BOX64_DYNAREC_BLEEDING_EDGE_KEY
import com.micewine.emu.activities.GeneralSettings.Companion.BOX64_DYNAREC_CALLRET_KEY
import com.micewine.emu.activities.GeneralSettings.Companion.BOX64_DYNAREC_FASTNAN_KEY
import com.micewine.emu.activities.GeneralSettings.Companion.BOX64_DYNAREC_FASTROUND_KEY
import com.micewine.emu.activities.GeneralSettings.Companion.BOX64_DYNAREC_NATIVEFLAGS_KEY
import com.micewine.emu.activities.GeneralSettings.Companion.BOX64_DYNAREC_PAUSE_KEY
import com.micewine.emu.activities.GeneralSettings.Companion.BOX64_DYNAREC_SAFEFLAGS_KEY
import com.micewine.emu.activities.GeneralSettings.Companion.BOX64_DYNAREC_STRONGMEM_KEY
import com.micewine.emu.activities.GeneralSettings.Companion.BOX64_DYNAREC_WAIT_KEY
import com.micewine.emu.activities.GeneralSettings.Companion.BOX64_DYNAREC_WEAKBARRIER_KEY
import com.micewine.emu.activities.GeneralSettings.Companion.BOX64_DYNAREC_X87DOUBLE_KEY
import com.micewine.emu.activities.GeneralSettings.Companion.BOX64_LOG_KEY
import com.micewine.emu.activities.GeneralSettings.Companion.BOX64_NOSIGILL_KEY
import com.micewine.emu.activities.GeneralSettings.Companion.BOX64_NOSIGSEGV_KEY
import com.micewine.emu.activities.GeneralSettings.Companion.BOX64_SHOWBT_KEY
import com.micewine.emu.activities.GeneralSettings.Companion.BOX64_SHOWSEGV_KEY
import com.micewine.emu.activities.GeneralSettings.Companion.DISPLAY_RESOLUTION_KEY
import com.micewine.emu.activities.GeneralSettings.Companion.ENABLE_DRI3
import com.micewine.emu.activities.GeneralSettings.Companion.ENABLE_MANGOHUD
import com.micewine.emu.activities.GeneralSettings.Companion.SELECTED_D3DX_RENDERER_KEY
import com.micewine.emu.activities.GeneralSettings.Companion.SELECTED_DRIVER_KEY
import com.micewine.emu.activities.GeneralSettings.Companion.SELECTED_DXVK_HUD_PRESET_KEY
import com.micewine.emu.activities.GeneralSettings.Companion.SELECTED_DXVK_KEY
import com.micewine.emu.activities.GeneralSettings.Companion.SELECTED_GL_PROFILE_KEY
import com.micewine.emu.activities.GeneralSettings.Companion.SELECTED_MESA_VK_WSI_PRESENT_MODE_KEY
import com.micewine.emu.activities.GeneralSettings.Companion.SELECTED_TU_DEBUG_PRESET_KEY
import com.micewine.emu.activities.GeneralSettings.Companion.SELECTED_VKD3D_KEY
import com.micewine.emu.activities.GeneralSettings.Companion.SELECTED_WINED3D_KEY
import com.micewine.emu.activities.GeneralSettings.Companion.WINE_ESYNC_KEY
import com.micewine.emu.activities.GeneralSettings.Companion.WINE_LOG_LEVEL_KEY
import com.micewine.emu.core.RatPackageManager
import com.micewine.emu.core.RatPackageManager.installRat
import com.micewine.emu.core.ShellLoader.runCommand
import com.micewine.emu.core.ShellLoader.runCommandWithOutput
import com.micewine.emu.core.WineWrapper
import com.micewine.emu.databinding.ActivityMainBinding
import com.micewine.emu.fragments.AskInstallRatPackageFragment
import com.micewine.emu.fragments.AskInstallRatPackageFragment.Companion.ratCandidate
import com.micewine.emu.fragments.DeleteGameItemFragment
import com.micewine.emu.fragments.FileManagerFragment
import com.micewine.emu.fragments.FileManagerFragment.Companion.refreshFiles
import com.micewine.emu.fragments.FloatingFileManagerFragment
import com.micewine.emu.fragments.HomeFragment
import com.micewine.emu.fragments.HomeFragment.Companion.saveToGameList
import com.micewine.emu.fragments.HomeFragment.Companion.setIconToGame
import com.micewine.emu.fragments.RenameGameItemFragment
import com.micewine.emu.fragments.RenameGameItemFragment.Companion.initialTextRenameGameFragment
import com.micewine.emu.fragments.SettingsFragment
import com.micewine.emu.fragments.SetupFragment
import com.micewine.emu.fragments.SetupFragment.Companion.abortSetup
import com.micewine.emu.fragments.SetupFragment.Companion.dialogTitleText
import com.micewine.emu.fragments.SetupFragment.Companion.progressBarIsIndeterminate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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
            when (intent.action) {
                ACTION_RUN_WINE -> {
                    val exePath = intent.getStringExtra("exePath")!!

                    tmpDir.deleteRecursively()
                    tmpDir.mkdirs()

                    setSharedVars(this@MainActivity)

                    val driverLibPath = File("$ratPackagesDir/$selectedDriver/pkg-header").readLines()[4].substringAfter("=")

                    generateICDFile(driverLibPath, File("$appRootDir/vulkan_icd.json"))

                    lifecycleScope.launch { runXServer(":0") }
                    lifecycleScope.launch { runWine(exePath, File("$homeDir/.wine")) }
                }

                ACTION_SELECT_FILE_MANAGER -> {
                    val fileName = intent.getStringExtra("selectedFile")

                    if (fileName == "..") {
                        fileManagerCwd = File(fileManagerCwd).parent!!

                        refreshFiles()

                        return
                    }

                    val file = File(fileName!!)

                    if (file.isFile) {
                        if (file.name.endsWith(".exe") || file.name.endsWith(".bat") || file.name.endsWith(".msi")) {
                            val runWineIntent = Intent(ACTION_RUN_WINE).apply {
                                putExtra("exePath", file.path)
                            }

                            sendBroadcast(runWineIntent)

                            val emulationActivityIntent = Intent(this@MainActivity, EmulationActivity::class.java).apply {
                                flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                            }

                            startActivityIfNeeded(emulationActivityIntent, 0)
                        } else if (file.name.endsWith(".rat")) {
                            ratCandidate = RatPackageManager.RatPackage(file.path)
                            AskInstallRatPackageFragment().show(supportFragmentManager, "")
                        }
                    } else if (file.isDirectory) {
                        fileManagerCwd = file.path

                        refreshFiles()
                    }
                }

                ACTION_SETUP -> {
                    lifecycleScope.launch {
                        var rootFSPath = ""

                        if (!appBuiltinRootfs) {
                            SetupFragment().show(supportFragmentManager, "")

                            rootFSPath = customRootFSPath!!
                        }

                        setupMiceWine(rootFSPath)
                    }
                }

                ACTION_INSTALL_RAT -> {
                    lifecycleScope.launch {
                        val ratFile: RatPackageManager.RatPackage = if (intent.getStringExtra("ratFile") == "") {
                            ratCandidate!!
                        } else {
                            RatPackageManager.RatPackage(intent.getStringExtra("ratFile")!!)
                        }

                        if (ratFile.architecture != Build.SUPPORTED_ABIS[0].replace("arm64-v8a", "aarch64")) {
                            Toast.makeText(context, "Invalid Architecture Rat File.", Toast.LENGTH_SHORT).show()
                            return@launch
                        }

                        if (ratFile.category == "rootfs") {
                            Toast.makeText(context, "You cannot install rootfs after installation.", Toast.LENGTH_SHORT).show()
                            return@launch
                        }

                        if (ratFile.category != "VulkanDriver") {
                            Toast.makeText(context, "You cannot install other packages than Vulkan Drivers.", Toast.LENGTH_SHORT).show()
                            return@launch
                        }

                        installRat(ratFile, context)

                        Toast.makeText(context, "Rat Package Installed!", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

    private var bottomNavigation: BottomNavigationView? = null
    private var runningXServer = false
    private val homeFragment: HomeFragment = HomeFragment()
    private val settingsFragment: SettingsFragment = SettingsFragment()
    private val fileManagerFragment: FileManagerFragment = FileManagerFragment()
    private var preferences: SharedPreferences? = null

    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding!!.root)

        setSharedVars(this)

        preferences = PreferenceManager.getDefaultSharedPreferences(this)

        bottomNavigation = findViewById(R.id.bottom_navigation)

        bottomNavigation?.setOnItemSelectedListener { item: MenuItem ->
            when (item.itemId) {
                R.id.nav_home -> {
                    selectedFragment = "HomeFragment"
                    fragmentLoader(homeFragment, false)
                }

                R.id.nav_settings -> {
                    selectedFragment = "SettingsFragment"
                    fragmentLoader(settingsFragment, false)
                }

                R.id.nav_file_manager -> {
                    selectedFragment = "FileManagerFragment"
                    fragmentLoader(fileManagerFragment, false)
                }
            }

            true
        }

        selectedFragment = "HomeFragment"
        fragmentLoader(homeFragment, true)

        registerReceiver(receiver, object : IntentFilter(ACTION_RUN_WINE) {
            init {
                addAction(ACTION_SETUP)
                addAction(ACTION_INSTALL_RAT)
                addAction(ACTION_SELECT_FILE_MANAGER)
            }
        })

        val exePath = intent.getStringExtra("exePath")

        if (exePath != null) {
            val intent = Intent(this, EmulationActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
            }

            WineWrapper.wineServer("--kill")

            sendBroadcast(
                Intent(ACTION_RUN_WINE).apply {
                    putExtra("exePath", exePath)
                }
            )

            startActivityIfNeeded(intent, 0)
        }
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)

        if (!usrDir.exists()) {
            val intent = Intent(this, WelcomeActivity::class.java)

            startActivity(intent)
        } else {
            setupDone = true
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (selectedFragment == "FileManagerFragment") {
                if (fileManagerCwd != fileManagerDefaultDir) {
                    fileManagerCwd = File(fileManagerCwd).parent!!

                    refreshFiles()

                    return true
                }
            }

            if (selectedFragment != "HomeFragment") {
                bottomNavigation?.selectedItemId = R.id.nav_home

                return true
            }

            sendBroadcast(Intent(ACTION_STOP_ALL))
        }

        return super.onKeyDown(keyCode, event)
    }

    override fun onCreateContextMenu(
        menu: ContextMenu?,
        v: View?,
        menuInfo: ContextMenu.ContextMenuInfo?
    ) {
        if (selectedFragment == "HomeFragment") {
            menuInflater.inflate(R.menu.game_list_context_menu, menu)
        } else if (selectedFragment == "FileManagerFragment") {
            menuInflater.inflate(R.menu.file_list_context_menu, menu)
        }
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.addToLauncher -> {
                addGameToLauncher(this, selectedGameArray)
            }

            R.id.editGameIcon -> {
                openFilePicker()
            }

            R.id.removeGameItem -> {
                DeleteGameItemFragment().show(supportFragmentManager, "")
            }

            R.id.renameGameItem -> {
                initialTextRenameGameFragment = selectedGameArray[0]
                RenameGameItemFragment().show(supportFragmentManager, "")
            }

            R.id.addToHome -> {
                if (selectedFile.endsWith(".exe")) {
                    val output = "$usrDir/icons/${File(selectedFile).nameWithoutExtension}-icon"

                    WineWrapper.extractIcon(File(selectedFile), output)

                    saveToGameList(preferences!!, selectedFile, File(selectedFile).nameWithoutExtension, output)
                } else if (selectedFile.endsWith(".bat") || selectedFile.endsWith(".msi")) {
                    saveToGameList(preferences!!, selectedFile, File(selectedFile).nameWithoutExtension, "")
                } else {
                    Toast.makeText(this, getString(R.string.incompatibleSelectedFile), Toast.LENGTH_SHORT).show()
                }
            }

            R.id.executeExe -> {
                if (selectedFile.endsWith(".exe") || selectedFile.endsWith(".bat") || selectedFile.endsWith(".msi")) {
                    val runWineIntent = Intent(ACTION_RUN_WINE).apply {
                        putExtra("exePath", selectedFile)
                    }

                    sendBroadcast(runWineIntent)

                    val emulationActivityIntent = Intent(this@MainActivity, EmulationActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                    }

                    startActivityIfNeeded(emulationActivityIntent, 0)
                }
            }

            R.id.deleteFile -> {
                DeleteGameItemFragment().show(supportFragmentManager, "")
            }
        }

        return super.onContextItemSelected(item)
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(
        requestCode: Int, resultCode: Int, data: Intent?
    ) {
        if (resultCode == Activity.RESULT_OK) {
            data?.data?.also { uri ->
                setIconToGame(this, preferences!!, uri, selectedGameArray)
            }
        }

        setSharedVars(this)

        super.onActivityResult(requestCode, resultCode, data)
    }


    @Suppress("DEPRECATION")
    private fun openFilePicker() {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "image/*"
            addCategory(Intent.CATEGORY_OPENABLE)
        }

        startActivityForResult(
            Intent.createChooser(intent, ""), 0
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
        binding = null
        unregisterReceiver(receiver)
    }

    override fun onResume() {
        super.onResume()

        if (!setupDone) {
            if (appBuiltinRootfs) {
                SetupFragment().show(supportFragmentManager , "")
            } else {
                FloatingFileManagerFragment().show(supportFragmentManager, "")
            }
        }
    }

    private fun installDXWrapper(winePrefix: File) {
        val driveC = File("$winePrefix/drive_c")
        val wineUtils = File("$appRootDir/wine-utils")
        val system32 = File("$driveC/windows/system32")
        val syswow64 = File("$driveC/windows/syswow64")
        val selectedDXVK = File("$wineUtils/DXVK/$selectedDXVK")
        val selectedVKD3D = File("$wineUtils/VKD3D/$selectedVKD3D")
        val selectedWineD3D = File("$wineUtils/WineD3D/$selectedWineD3D")

        when (d3dxRenderer) {
            "DXVK" -> {
                if (selectedDXVK.exists()) {
                    File("$selectedDXVK/x64").copyRecursively(system32, true)
                    File("$selectedDXVK/x32").copyRecursively(syswow64, true)
                }
            }

            "WineD3D" -> {
                if (selectedWineD3D.exists()) {
                    File("$selectedWineD3D/x64").copyRecursively(system32, true)
                    File("$selectedWineD3D/x32").copyRecursively(syswow64, true)
                }
            }
        }

        if (selectedVKD3D.exists()) {
            File("$selectedVKD3D/x64").copyRecursively(system32, true)
            File("$selectedVKD3D/x32").copyRecursively(syswow64, true)
        }
    }

    private suspend fun runWine(exePath: String, winePrefix: File) {
        withContext(Dispatchers.Default) {
            sharedLogs?.clear()

            installDXWrapper(winePrefix)

            runCommand("pkill -9 wineserver")
            runCommand("pkill -9 .exe")
            runCommand("pkill -9 pulseaudio")

            if (exePath == "") {
                WineWrapper.wine("explorer /desktop=shell,$selectedResolution TFM", winePrefix)
            } else {
                WineWrapper.wine("'$exePath'", winePrefix, "'${File(exePath).parent!!}'")
            }

            runCommand("pkill -9 wineserver")
            runCommand("pkill -9 .exe")
            runCommand("pkill -9 pulseaudio")

            runOnUiThread {
                Toast.makeText(this@MainActivity, getString(R.string.wine_is_closed), Toast.LENGTH_SHORT).show()
            }
        }
    }

    private suspend fun runXServer(display: String) {
        withContext(Dispatchers.IO) {
            if (runningXServer) {
                return@withContext
            }

            runningXServer = true

            runCommand(
                "env CLASSPATH=${getClassPath(this@MainActivity)} /system/bin/app_process / com.micewine.emu.CmdEntryPoint $display &> /dev/null"
            )
        }
    }

    private suspend fun setupMiceWine(rootfs: String) {
        withContext(Dispatchers.IO) {
            appRootDir.mkdirs()
            ratPackagesDir.mkdirs()

            progressBarIsIndeterminate = true

            val ratFile: RatPackageManager.RatPackage

            if (appBuiltinRootfs && rootfs == "") {
                dialogTitleText = getString(R.string.extracting_from_assets)

                copyAssets(this@MainActivity, "rootfs.rat", "$appRootDir")

                dialogTitleText = getString(R.string.checking_rat_type)

                ratFile = RatPackageManager.RatPackage("$appRootDir/rootfs.rat")
            } else {
                dialogTitleText = getString(R.string.checking_rat_type)

                ratFile = RatPackageManager.RatPackage("$customRootFSPath")
            }

            if (ratFile.category != "rootfs") {
                abortSetup = true

                runOnUiThread {
                    Toast.makeText(this@MainActivity, getString(R.string.invalid_rootfs_rat_file), Toast.LENGTH_SHORT).show()
                }

                FloatingFileManagerFragment().show(supportFragmentManager, "")

                return@withContext
            }

            dialogTitleText = getString(R.string.extracting_resources_text)

            installRat(ratFile, this@MainActivity)

            if (appBuiltinRootfs && rootfs == "") {
                File("$appRootDir/rootfs.rat").delete()
            }

            tmpDir.mkdirs()
            homeDir.mkdirs()

            File("$appRootDir/wine-utils/CoreFonts").copyRecursively(File("$appRootDir/wine/share/wine/fonts"), true)

            runCommand("chmod 700 -R $appRootDir")

            File("$usrDir/icons").mkdirs()

            dialogTitleText = getString(R.string.creatingWinePrefix)
            progressBarIsIndeterminate = true

            lifecycleScope.launch { runXServer(":0") }

            setupWinePrefix(File("$homeDir/.wine"))

            fileManagerCwd = fileManagerDefaultDir
            setupDone = true
        }
    }

    companion object {
        @SuppressLint("SdCardPath")
        val appRootDir = File("/data/data/com.micewine.emu/files")
        var ratPackagesDir = File("$appRootDir/packages")
        var appBuiltinRootfs: Boolean = false
        private var unixUsername = runCommandWithOutput("whoami").replace("\n", "")
        var customRootFSPath: String? = null
        var usrDir = File("$appRootDir/usr")
        var tmpDir = File("$usrDir/tmp")
        var homeDir = File("$appRootDir/home")
        var setupDone: Boolean = false
        var enableRamCounter: Boolean = false
        var enableCpuCounter: Boolean = false
        var enableDebugInfo: Boolean = false
        var enableDRI3: Boolean = false
        var enableMangoHUD: Boolean = false
        var appLang: String? = null
        var box64LogLevel: String? = null
        var box64Avx: String? = null
        var box64DynarecBigblock: String? = null
        var box64DynarecStrongmem: String? = null
        var box64DynarecWeakbarrier: String? = null
        var box64DynarecPause: String? = null
        var box64DynarecX87double: String? = null
        var box64DynarecFastnan: String? = null
        var box64DynarecFastround: String? = null
        var box64DynarecSafeflags: String? = null
        var box64DynarecCallret: String? = null
        var box64DynarecAlignedAtomics: String? = null
        var box64DynarecNativeflags: String? = null
        var box64DynarecBleedingEdge: String? = null
        var box64DynarecWait: String? = null
        var box64ShowSegv: String? = null
        var box64ShowBt: String? = null
        var box64NoSigSegv: String? = null
        var box64NoSigill: String? = null
        var wineESync: String? = null
        var wineLogLevel: String? = null
        var selectedDriver: String? = null
        var d3dxRenderer: String? = null
        var selectedWineD3D: String? = null
        var selectedDXVK: String? = null
        var selectedVKD3D: String? = null
        var selectedGLProfile: String? = null
        var selectedDXVKHud: String? = null
        var selectedMesaVkWsiPresentMode: String? = null
        var selectedTuDebugPreset: String? = null
        var selectedGameArray: Array<String> = arrayOf()
        var memoryStats = "??/??"
        var totalCpuUsage = "???%"
        var fileManagerDefaultDir: String = "$homeDir/.wine/dosdevices"
        var fileManagerCwd: String = fileManagerDefaultDir
        var selectedFile: String = ""
        var miceWineVersion: String = "MiceWine (git-${BuildConfig.GIT_SHORT_SHA})"
        private var selectedResolution: String? = ""

        var selectedFragment = "HomeFragment"

        const val ACTION_RUN_WINE = "com.micewine.emu.ACTION_RUN_WINE"
        const val ACTION_SETUP = "com.micewine.emu.ACTION_SETUP"
        const val ACTION_INSTALL_RAT = "com.micewine.emu.ACTION_INSTALL_RAT"
        const val ACTION_STOP_ALL = "com.micewine.emu.ACTION_STOP_ALL"
        const val ACTION_SELECT_FILE_MANAGER = "com.micewine.emu.ACTION_SELECT_FILE_MANAGER"
        const val RAM_COUNTER_KEY = "ramCounter"
        const val CPU_COUNTER_KEY = "cpuCounter"
        const val ENABLE_DEBUG_INFO_KEY = "debugInfo"

        fun setupWinePrefix(winePrefix: File) {
            if (!winePrefix.exists()) {
                val driveC = File("$winePrefix/drive_c")
                val wineUtils = File("$appRootDir/wine-utils")
                val startMenu = File("$driveC/ProgramData/Microsoft/Windows/Start Menu")
                val userSharedFolder = File("/storage/emulated/0/MiceWine")
                val localAppData = File("$driveC/users/$unixUsername/AppData")
                val system32 = File("$driveC/windows/system32")
                val syswow64 = File("$driveC/windows/syswow64")

                WineWrapper.wine("wineboot -i", winePrefix)

                localAppData.copyRecursively(File("$userSharedFolder/AppData"))
                localAppData.deleteRecursively()

                File("$userSharedFolder/AppData").mkdirs()

                runCommand("ln -sf $userSharedFolder/AppData $localAppData")

                startMenu.deleteRecursively()

                File("$wineUtils/Start Menu").copyRecursively(File("$startMenu"), true)
                File("$wineUtils/Addons").copyRecursively(File("$driveC/Addons"), true)
                File("$wineUtils/Addons/Windows").copyRecursively(File("$driveC/windows"), true)
                File("$wineUtils/DirectX/x64").copyRecursively(system32, true)
                File("$wineUtils/DirectX/x32").copyRecursively(syswow64, true)
                File("$wineUtils/OpenAL/x64").copyRecursively(system32, true)
                File("$wineUtils/OpenAL/x32").copyRecursively(syswow64, true)

                WineWrapper.wine("regedit $driveC/Addons/DefaultDLLsOverrides.reg", winePrefix)
                WineWrapper.wine("regedit $driveC/Addons/Themes/DarkBlue/DarkBlue.reg", winePrefix)
                WineWrapper.wine("reg add HKCU\\\\Software\\\\Wine\\\\X11\\ Driver /t REG_SZ /v Decorated /d N /f", winePrefix)
                WineWrapper.wine("reg add HKCU\\\\Software\\\\Wine\\\\X11\\ Driver /t REG_SZ /v Managed /d N /f", winePrefix)
                WineWrapper.wine("reg delete HKCU\\\\Software\\\\Microsoft\\\\Windows\\\\CurrentVersion\\\\ThemeManager -v DllName /f", winePrefix)
            }
        }

        private fun booleanToString(boolean: Boolean): String {
            return if (boolean) {
                "1"
            } else {
                "0"
            }
        }

        fun setSharedVars(activity: Activity) {
            val preferences = PreferenceManager.getDefaultSharedPreferences(activity)

            appLang = activity.resources.getString(R.string.app_lang)
            appBuiltinRootfs = activity.assets.list("")?.contains("rootfs.zip")!!

            box64LogLevel = preferences.getString(BOX64_LOG_KEY, "1")
            box64Avx = preferences.getString(BOX64_AVX_KEY, "2")
            box64DynarecBigblock = preferences.getString(BOX64_DYNAREC_BIGBLOCK_KEY, "1")
            box64DynarecStrongmem = preferences.getString(BOX64_DYNAREC_STRONGMEM_KEY, "0")
            box64DynarecWeakbarrier = preferences.getString(BOX64_DYNAREC_WEAKBARRIER_KEY, "0")
            box64DynarecPause = preferences.getString(BOX64_DYNAREC_PAUSE_KEY, "0")
            box64DynarecX87double = booleanToString(preferences.getBoolean(BOX64_DYNAREC_X87DOUBLE_KEY, false))
            box64DynarecFastnan = booleanToString(preferences.getBoolean(BOX64_DYNAREC_FASTNAN_KEY, true))
            box64DynarecFastround = booleanToString(preferences.getBoolean(BOX64_DYNAREC_FASTROUND_KEY, true))
            box64DynarecSafeflags = preferences.getString(BOX64_DYNAREC_SAFEFLAGS_KEY, "1")
            box64DynarecCallret = booleanToString(preferences.getBoolean(BOX64_DYNAREC_CALLRET_KEY, true))
            box64DynarecAlignedAtomics = booleanToString(preferences.getBoolean(BOX64_DYNAREC_ALIGNED_ATOMICS_KEY, false))
            box64DynarecNativeflags = booleanToString(preferences.getBoolean(BOX64_DYNAREC_NATIVEFLAGS_KEY, true))
            box64DynarecBleedingEdge = booleanToString(preferences.getBoolean(BOX64_DYNAREC_BLEEDING_EDGE_KEY, true))
            box64DynarecWait = booleanToString(preferences.getBoolean(BOX64_DYNAREC_WAIT_KEY, true))
            box64ShowSegv = booleanToString(preferences.getBoolean(BOX64_SHOWSEGV_KEY, true))
            box64ShowBt = booleanToString(preferences.getBoolean(BOX64_SHOWBT_KEY, false))
            box64NoSigSegv = booleanToString(preferences.getBoolean(BOX64_NOSIGSEGV_KEY, false))
            box64NoSigill = booleanToString(preferences.getBoolean(BOX64_NOSIGILL_KEY, false))
            enableDRI3 = preferences.getBoolean(ENABLE_DRI3, true)
            enableMangoHUD = preferences.getBoolean(ENABLE_MANGOHUD, true)
            wineESync = booleanToString(preferences.getBoolean(WINE_ESYNC_KEY, false))
            wineLogLevel = preferences.getString(WINE_LOG_LEVEL_KEY, "default")
            selectedDriver = preferences.getString(SELECTED_DRIVER_KEY, "")
            d3dxRenderer = preferences.getString(SELECTED_D3DX_RENDERER_KEY, "DXVK")
            selectedWineD3D = preferences.getString(SELECTED_WINED3D_KEY, "WineD3D-9.0")
            selectedDXVK = preferences.getString(SELECTED_DXVK_KEY, "DXVK-1.10.3-async")
            selectedVKD3D = preferences.getString(SELECTED_VKD3D_KEY, "VKD3D-2.13")
            selectedGLProfile = preferences.getString(SELECTED_GL_PROFILE_KEY, "GL 4.6")
            selectedDXVKHud = preferences.getString(SELECTED_DXVK_HUD_PRESET_KEY, "fps")
            selectedMesaVkWsiPresentMode = preferences.getString(SELECTED_MESA_VK_WSI_PRESENT_MODE_KEY, "mailbox")
            selectedTuDebugPreset = preferences.getString(SELECTED_TU_DEBUG_PRESET_KEY, "noconform")
            selectedResolution = preferences.getString(DISPLAY_RESOLUTION_KEY, "1280x720")
            enableRamCounter = preferences.getBoolean(RAM_COUNTER_KEY, true)
            enableCpuCounter = preferences.getBoolean(CPU_COUNTER_KEY, false)
            enableDebugInfo = preferences.getBoolean(ENABLE_DEBUG_INFO_KEY, true)
        }

        fun copyAssets(activity: Activity, filename: String, outputPath: String) {
            dialogTitleText = activity.getString(R.string.extracting_from_assets)

            val assetManager = activity.assets

            if (appBuiltinRootfs) {
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
        }

        @Throws(IOException::class)
        fun copyFile(input: InputStream, out: OutputStream?) {
            val buffer = ByteArray(1024)
            var read: Int
            while (input.read(buffer).also { read = it } != -1) {
                out!!.write(buffer, 0, read)
            }
        }

        private fun getClassPath(context: Context): String {
            return File(getLibsPath(context)).parentFile?.parentFile?.absolutePath + "/base.apk"
        }

        private fun getLibsPath(context: Context): String {
            return context.applicationInfo.nativeLibraryDir
        }

        suspend fun getMemoryInfo(context: Context) {
            withContext(Dispatchers.IO) {
                val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
                val memoryInfo = ActivityManager.MemoryInfo()
                var totalMemory: Long
                var availableMemory: Long
                var usedMemory: Long

                while (enableRamCounter) {
                    activityManager.getMemoryInfo(memoryInfo)

                    totalMemory = memoryInfo.totalMem / (1024 * 1024)
                    availableMemory = memoryInfo.availMem / (1024 * 1024)
                    usedMemory = totalMemory - availableMemory

                    memoryStats = "$usedMemory/$totalMemory"
                }
            }
        }

        suspend fun getCpuInfo() {
            withContext(Dispatchers.IO) {
                while (enableCpuCounter) {
                    val usageInfo = runCommandWithOutput("top -bn 1 -u \$(whoami) -o %CPU -q | head -n 1").toFloat() / Runtime.getRuntime().availableProcessors()

                    totalCpuUsage = "$usageInfo%"

                    Thread.sleep(750)
                }
            }
        }

        fun addGameToLauncher(context: Context, selectedGameArray: Array<String>) {
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
                val successCallback = PendingIntent.getBroadcast(context, 0, pinnedShortcutCallbackIntent, PendingIntent.FLAG_IMMUTABLE)

                shortcutManager.requestPinShortcut(pinShortcutInfo, successCallback.intentSender)
            }
        }
    }
}
