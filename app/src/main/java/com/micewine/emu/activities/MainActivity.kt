package com.micewine.emu.activities

import android.annotation.SuppressLint
import android.app.Activity
import android.app.ActivityManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.os.storage.StorageManager
import android.view.ContextMenu
import android.view.KeyEvent
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.preference.PreferenceManager
import com.getkeepsafe.taptargetview.TapTarget
import com.getkeepsafe.taptargetview.TapTargetSequence
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.micewine.emu.BuildConfig
import com.micewine.emu.R
import com.micewine.emu.activities.GeneralSettingsActivity.Companion.BOX64_AVX
import com.micewine.emu.activities.GeneralSettingsActivity.Companion.BOX64_DYNAREC_ALIGNED_ATOMICS
import com.micewine.emu.activities.GeneralSettingsActivity.Companion.BOX64_DYNAREC_BIGBLOCK
import com.micewine.emu.activities.GeneralSettingsActivity.Companion.BOX64_DYNAREC_CALLRET
import com.micewine.emu.activities.GeneralSettingsActivity.Companion.BOX64_DYNAREC_DIRTY
import com.micewine.emu.activities.GeneralSettingsActivity.Companion.BOX64_DYNAREC_FASTNAN
import com.micewine.emu.activities.GeneralSettingsActivity.Companion.BOX64_DYNAREC_FASTROUND
import com.micewine.emu.activities.GeneralSettingsActivity.Companion.BOX64_DYNAREC_FORWARD
import com.micewine.emu.activities.GeneralSettingsActivity.Companion.BOX64_DYNAREC_NATIVEFLAGS
import com.micewine.emu.activities.GeneralSettingsActivity.Companion.BOX64_DYNAREC_PAUSE
import com.micewine.emu.activities.GeneralSettingsActivity.Companion.BOX64_DYNAREC_SAFEFLAGS
import com.micewine.emu.activities.GeneralSettingsActivity.Companion.BOX64_DYNAREC_STRONGMEM
import com.micewine.emu.activities.GeneralSettingsActivity.Companion.BOX64_DYNAREC_WAIT
import com.micewine.emu.activities.GeneralSettingsActivity.Companion.BOX64_DYNAREC_WEAKBARRIER
import com.micewine.emu.activities.GeneralSettingsActivity.Companion.BOX64_DYNAREC_X87DOUBLE
import com.micewine.emu.activities.GeneralSettingsActivity.Companion.BOX64_LOG
import com.micewine.emu.activities.GeneralSettingsActivity.Companion.BOX64_LOG_DEFAULT_VALUE
import com.micewine.emu.activities.GeneralSettingsActivity.Companion.BOX64_MMAP32
import com.micewine.emu.activities.GeneralSettingsActivity.Companion.BOX64_SSE42
import com.micewine.emu.activities.GeneralSettingsActivity.Companion.CPU_AFFINITY
import com.micewine.emu.activities.GeneralSettingsActivity.Companion.DISPLAY_RESOLUTION
import com.micewine.emu.activities.GeneralSettingsActivity.Companion.DISPLAY_RESOLUTION_DEFAULT_VALUE
import com.micewine.emu.activities.GeneralSettingsActivity.Companion.ENABLE_DRI3
import com.micewine.emu.activities.GeneralSettingsActivity.Companion.ENABLE_DRI3_DEFAULT_VALUE
import com.micewine.emu.activities.GeneralSettingsActivity.Companion.ENABLE_MANGOHUD
import com.micewine.emu.activities.GeneralSettingsActivity.Companion.ENABLE_MANGOHUD_DEFAULT_VALUE
import com.micewine.emu.activities.GeneralSettingsActivity.Companion.ENABLE_SERVICES
import com.micewine.emu.activities.GeneralSettingsActivity.Companion.ENABLE_SERVICES_DEFAULT_VALUE
import com.micewine.emu.activities.GeneralSettingsActivity.Companion.FPS_LIMIT
import com.micewine.emu.activities.GeneralSettingsActivity.Companion.SELECTED_BOX64
import com.micewine.emu.activities.GeneralSettingsActivity.Companion.SELECTED_D3DX_RENDERER
import com.micewine.emu.activities.GeneralSettingsActivity.Companion.SELECTED_D3DX_RENDERER_DEFAULT_VALUE
import com.micewine.emu.activities.GeneralSettingsActivity.Companion.SELECTED_DRIVER
import com.micewine.emu.activities.GeneralSettingsActivity.Companion.SELECTED_DRIVER_DEFAULT_VALUE
import com.micewine.emu.activities.GeneralSettingsActivity.Companion.SELECTED_DXVK
import com.micewine.emu.activities.GeneralSettingsActivity.Companion.SELECTED_DXVK_DEFAULT_VALUE
import com.micewine.emu.activities.GeneralSettingsActivity.Companion.SELECTED_DXVK_HUD_PRESET
import com.micewine.emu.activities.GeneralSettingsActivity.Companion.SELECTED_DXVK_HUD_PRESET_DEFAULT_VALUE
import com.micewine.emu.activities.GeneralSettingsActivity.Companion.SELECTED_GL_PROFILE
import com.micewine.emu.activities.GeneralSettingsActivity.Companion.SELECTED_GL_PROFILE_DEFAULT_VALUE
import com.micewine.emu.activities.GeneralSettingsActivity.Companion.SELECTED_MESA_VK_WSI_PRESENT_MODE
import com.micewine.emu.activities.GeneralSettingsActivity.Companion.SELECTED_MESA_VK_WSI_PRESENT_MODE_DEFAULT_VALUE
import com.micewine.emu.activities.GeneralSettingsActivity.Companion.SELECTED_TU_DEBUG_PRESET
import com.micewine.emu.activities.GeneralSettingsActivity.Companion.SELECTED_TU_DEBUG_PRESET_DEFAULT_VALUE
import com.micewine.emu.activities.GeneralSettingsActivity.Companion.SELECTED_VKD3D
import com.micewine.emu.activities.GeneralSettingsActivity.Companion.SELECTED_VKD3D_DEFAULT_VALUE
import com.micewine.emu.activities.GeneralSettingsActivity.Companion.SELECTED_WINED3D
import com.micewine.emu.activities.GeneralSettingsActivity.Companion.SELECTED_WINED3D_DEFAULT_VALUE
import com.micewine.emu.activities.GeneralSettingsActivity.Companion.SELECTED_WINE_PREFIX
import com.micewine.emu.activities.GeneralSettingsActivity.Companion.WINE_ESYNC
import com.micewine.emu.activities.GeneralSettingsActivity.Companion.WINE_ESYNC_DEFAULT_VALUE
import com.micewine.emu.activities.GeneralSettingsActivity.Companion.WINE_LOG_LEVEL
import com.micewine.emu.activities.GeneralSettingsActivity.Companion.WINE_LOG_LEVEL_DEFAULT_VALUE
import com.micewine.emu.activities.PresetManagerActivity.Companion.SELECTED_BOX64_PRESET_KEY
import com.micewine.emu.activities.RatManagerActivity.Companion.generateICDFile
import com.micewine.emu.activities.RatManagerActivity.Companion.generateMangoHUDConfFile
import com.micewine.emu.adapters.AdapterGame.Companion.selectedGameName
import com.micewine.emu.core.EnvVars
import com.micewine.emu.core.EnvVars.getEnv
import com.micewine.emu.core.HighlightState
import com.micewine.emu.core.RatPackageManager
import com.micewine.emu.core.RatPackageManager.installRat
import com.micewine.emu.core.ShellLoader.runCommand
import com.micewine.emu.core.ShellLoader.runCommandWithOutput
import com.micewine.emu.core.WineWrapper
import com.micewine.emu.core.WineWrapper.getCpuHexMask
import com.micewine.emu.core.WineWrapper.getSanitizedPath
import com.micewine.emu.databinding.ActivityMainBinding
import com.micewine.emu.fragments.AboutFragment
import com.micewine.emu.fragments.AskInstallRatPackageFragment
import com.micewine.emu.fragments.AskInstallRatPackageFragment.Companion.ratCandidate
import com.micewine.emu.fragments.Box64PresetManagerFragment
import com.micewine.emu.fragments.Box64PresetManagerFragment.Companion.getBox64Mapping
import com.micewine.emu.fragments.ControllerPresetManagerFragment
import com.micewine.emu.fragments.DeleteItemFragment
import com.micewine.emu.fragments.DeleteItemFragment.Companion.DELETE_GAME_ITEM
import com.micewine.emu.fragments.EditGamePreferencesFragment
import com.micewine.emu.fragments.FileManagerFragment
import com.micewine.emu.fragments.FileManagerFragment.Companion.refreshFiles
import com.micewine.emu.fragments.FloatingFileManagerFragment
import com.micewine.emu.fragments.FloatingFileManagerFragment.Companion.OPERATION_SELECT_RAT
import com.micewine.emu.fragments.RenameFragment
import com.micewine.emu.fragments.RenameFragment.Companion.RENAME_FILE
import com.micewine.emu.fragments.SettingsFragment
import com.micewine.emu.fragments.SetupFragment
import com.micewine.emu.fragments.SetupFragment.Companion.abortSetup
import com.micewine.emu.fragments.SetupFragment.Companion.dialogTitleText
import com.micewine.emu.fragments.SetupFragment.Companion.progressBarIsIndeterminate
import com.micewine.emu.fragments.ShortcutsFragment
import com.micewine.emu.fragments.ShortcutsFragment.Companion.ACTION_UPDATE_WINE_PREFIX_SPINNER
import com.micewine.emu.fragments.ShortcutsFragment.Companion.addGameToLauncher
import com.micewine.emu.fragments.ShortcutsFragment.Companion.addGameToList
import com.micewine.emu.fragments.ShortcutsFragment.Companion.getBox64Preset
import com.micewine.emu.fragments.ShortcutsFragment.Companion.setIconToGame
import com.micewine.emu.fragments.VirtualControllerPresetManagerFragment
import com.micewine.emu.fragments.WineSettingsFragment.Companion.availableCPUs
import com.micewine.emu.utils.DriveUtils
import com.micewine.emu.utils.FilePathResolver
import io.ByteWriter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import mslinks.LinkTargetIDList
import mslinks.ShellLink
import mslinks.ShellLinkException
import mslinks.data.ItemID
import mslinks.data.VolumeID
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.nio.file.Files

class MainActivity : AppCompatActivity() {
    private var binding: ActivityMainBinding? = null
    private val receiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                ACTION_RUN_WINE -> {
                    val exePath = intent.getStringExtra("exePath")!!
                    val exeArguments = intent.getStringExtra("exeArguments")!!

                    tmpDir.deleteRecursively()
                    tmpDir.mkdirs()

                    setSharedVars(this@MainActivity)

                    val driverLibPath = File("$ratPackagesDir/$selectedDriver/pkg-header").readLines()[4].substringAfter("=")

                    generateICDFile(driverLibPath, File("$appRootDir/vulkan_icd.json"))
                    generateMangoHUDConfFile()

                    setSharedVars(this@MainActivity)

                    if (exePath != getString(R.string.desktop_mode_init)) {
                        setBox64Preset(this@MainActivity, getBox64Preset(selectedGameName))
                    }

                    lifecycleScope.launch { runXServer(":0") }
                    lifecycleScope.launch { runWine(exePath, exeArguments) }
                }

                ACTION_SELECT_FILE_MANAGER -> {
                    val fileName = intent.getStringExtra("selectedFile")

                    if (fileName == "..") {
                        fileManagerCwd = File(fileManagerCwd!!).parent!!

                        refreshFiles()

                        return
                    }

                    val file = File(fileName!!)

                    if (file.isFile) {
                        val fileExtension = file.extension.lowercase()

                        if (fileExtension == "exe" || fileExtension == "bat" || fileExtension == "msi" || fileExtension == "lnk") {
                            val runWineIntent = Intent(ACTION_RUN_WINE).apply {
                                putExtra("exePath", file.path)
                                putExtra("exeArguments", "")
                            }

                            sendBroadcast(runWineIntent)

                            val emulationActivityIntent = Intent(this@MainActivity, EmulationActivity::class.java).apply {
                                flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                            }

                            startActivityIfNeeded(emulationActivityIntent, 0)
                        } else if (file.name.endsWith("rat")) {
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

                        if (ratFile.architecture != Build.SUPPORTED_ABIS[0].replace("arm64-v8a", "aarch64") && ratFile.category != "Wine") {
                            Toast.makeText(context, R.string.invalid_architecture_rat_file, Toast.LENGTH_SHORT).show()
                            return@launch
                        }

                        if (ratFile.category == "rootfs") {
                            Toast.makeText(context, "You cannot install rootfs after installation.", Toast.LENGTH_SHORT).show()
                            return@launch
                        }

                        if (ratFile.category != "VulkanDriver" && ratFile.category != "Box64" && ratFile.category != "Wine") {
                            Toast.makeText(context, "You cannot install other packages than Vulkan Drivers Box64 or Wine.", Toast.LENGTH_SHORT).show()
                            return@launch
                        }

                        withContext(Dispatchers.Default) {
                            setupDone = false

                            SetupFragment().show(supportFragmentManager, "")

                            dialogTitleText = "Installing ${ratFile.name} (${ratFile.version})..."
                            progressBarIsIndeterminate = true

                            installRat(ratFile, context)

                            setupDone = true
                        }
                    }
                }

                ACTION_SELECT_ICON -> {
                    openFilePicker()
                }

                ACTION_CREATE_WINE_PREFIX -> {
                    val winePrefix = intent.getStringExtra("winePrefix")!!
                    val wine = intent.getStringExtra("wine")!!

                    lifecycleScope.launch { runXServer(":0") }
                    lifecycleScope.launch {
                        setupDone = false

                        SetupFragment().show(supportFragmentManager, "")

                        dialogTitleText = getString(R.string.creating_wine_prefix)
                        progressBarIsIndeterminate = true

                        withContext(Dispatchers.IO) {
                            setupWinePrefix(
                                File(winePrefix), wine
                            )

                            fileManagerCwd = fileManagerDefaultDir
                            setupDone = true

                            val updateWinePrefixSpinnerIntent = Intent(ACTION_UPDATE_WINE_PREFIX_SPINNER).apply {
                                putExtra("prefixName", File(winePrefix).name)
                            }

                            sendBroadcast(
                                updateWinePrefixSpinnerIntent
                            )

                            withContext(Dispatchers.Main) {
                                runOnUiThread {
                                    showHighlightSequence()
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private var appToolbar: MaterialToolbar? = null
    private var bottomNavigation: BottomNavigationView? = null
    private var runningXServer = false
    private val shortcutsFragment: ShortcutsFragment = ShortcutsFragment()
    private val settingsFragment: SettingsFragment = SettingsFragment()
    private val fileManagerFragment: FileManagerFragment = FileManagerFragment()
    private val aboutFragment: AboutFragment = AboutFragment()
    private var preferences: SharedPreferences? = null
    private var currentState: HighlightState? = null

    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        EnvVars.initialize(this)
        ControllerPresetManagerFragment.initialize(this)
        VirtualControllerPresetManagerFragment.initialize(this)
        Box64PresetManagerFragment.initialize(this)
        ShortcutsFragment.initialize(this)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding!!.root)

        appToolbar = findViewById(R.id.appToolbar)

        setSupportActionBar(appToolbar)
        setSharedVars(this)

        preferences = PreferenceManager.getDefaultSharedPreferences(this)

        bottomNavigation = findViewById(R.id.bottom_navigation)
        bottomNavigation?.setOnItemSelectedListener { item: MenuItem ->
            when (item.itemId) {
                R.id.nav_shortcuts -> {
                    selectedFragment = "ShortcutsFragment"
                    fragmentLoader(shortcutsFragment, false)
                }

                R.id.nav_settings -> {
                    selectedFragment = "SettingsFragment"
                    fragmentLoader(settingsFragment, false)
                }

                R.id.nav_file_manager -> {
                    selectedFragment = "FileManagerFragment"
                    fragmentLoader(fileManagerFragment, false)
                }

                R.id.nav_about -> {
                    selectedFragment = "AboutFragment"
                    fragmentLoader(aboutFragment, false)
                }
            }

            true
        }

        selectedFragment = "ShortcutsFragment"
        fragmentLoader(shortcutsFragment, true)

        registerReceiver(receiver, object : IntentFilter() {
            init {
                addAction(ACTION_RUN_WINE)
                addAction(ACTION_SETUP)
                addAction(ACTION_INSTALL_RAT)
                addAction(ACTION_SELECT_FILE_MANAGER)
                addAction(ACTION_SELECT_ICON)
                addAction(ACTION_CREATE_WINE_PREFIX)
            }
        })

        onNewIntent(intent)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (winePrefix?.exists() == true) {
                WineWrapper.clearDrives()

                (application.getSystemService(Context.STORAGE_SERVICE) as StorageManager).storageVolumes.forEach { volume ->
                    if (volume.isRemovable) {
                        WineWrapper.addDrive("${volume.directory}")
                    }
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        return true
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)

        if (!usrDir.exists()) {
            startActivity(
                Intent(this, WelcomeActivity::class.java)
            )
        } else {
            setupDone = true
            showHighlightSequence()
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (selectedFragment == "FileManagerFragment") {
                if (fileManagerCwd != fileManagerDefaultDir) {
                    fileManagerCwd = File(fileManagerCwd!!).parent!!

                    refreshFiles()

                    return true
                }
            }

            if (selectedFragment != "ShortcutsFragment") {
                bottomNavigation?.selectedItemId = R.id.nav_shortcuts

                return true
            }

            sendBroadcast(
                Intent(ACTION_STOP_ALL)
            )
        }

        return super.onKeyDown(keyCode, event)
    }

    override fun onCreateContextMenu(
        menu: ContextMenu?,
        v: View?,
        menuInfo: ContextMenu.ContextMenuInfo?
    ) {
        if (selectedFragment == "ShortcutsFragment") {
            menuInflater.inflate(R.menu.game_list_context_menu, menu)
        } else if (selectedFragment == "FileManagerFragment") {
            menuInflater.inflate(R.menu.file_list_context_menu, menu)
        }
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.addToLauncher -> {
                addGameToLauncher(this, selectedGameName)
            }

            R.id.removeGameItem -> {
                DeleteItemFragment(DELETE_GAME_ITEM, this).show(supportFragmentManager, "")
            }

            R.id.editGameItem -> {
                EditGamePreferencesFragment().show(supportFragmentManager, "")
            }

            R.id.addToHome -> {
                if (selectedFile.endsWith("exe")) {
                    val output = "$usrDir/icons/${File(selectedFile).nameWithoutExtension}-icon"

                    WineWrapper.extractIcon(File(selectedFile), output)

                    addGameToList(selectedFile, File(selectedFile).nameWithoutExtension, output)
                } else if (selectedFile.endsWith(".bat") || selectedFile.endsWith(".msi")) {
                    addGameToList(selectedFile, File(selectedFile).nameWithoutExtension, "")
                } else {
                    Toast.makeText(this, getString(R.string.incompatible_selected_file), Toast.LENGTH_SHORT).show()
                }
            }

            R.id.createLnk -> {
                exportLnkAction(selectedFile)
            }

            R.id.executeExe -> {
                val fileExtension = File(selectedFile).extension.lowercase()

                if (fileExtension  == "exe" || fileExtension == "bat" || fileExtension == "msi"  || fileExtension == "lnk") {
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
                DeleteItemFragment(DELETE_GAME_ITEM, this).show(supportFragmentManager, "")
            }

            R.id.renameFile -> {
                RenameFragment(RENAME_FILE, File(selectedFile).name).show(supportFragmentManager, "")
            }
        }

        return super.onContextItemSelected(item)
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(
        requestCode: Int, resultCode: Int, data: Intent?
    ) {
        if (requestCode == EXPORT_LNK_ACTION && resultCode == Activity.RESULT_OK) {
            data?.data?.let { uri ->
                val driveInfo = DriveUtils.parseUnixPath(selectedFile)

                if (driveInfo != null) {
                    val shellLink = ShellLink().apply {
                        header.linkFlags.setHasLinkTargetIDList()
                    }

                    val target = driveInfo.getWindowsPath()

                    val idList = LinkTargetIDList()
                    val pathSegments = target.replace('\\', File.separatorChar)
                        .split(File.separatorChar)
                        .filter { it.isNotEmpty() }

                    idList.apply {
                        add(ItemID().setType(ItemID.TYPE_CLSID))
                        add(ItemID().setType(ItemID.TYPE_DRIVE).setName(pathSegments[0]))
                        pathSegments.drop(1).forEach { segment ->
                            add(ItemID().setType(ItemID.TYPE_DIRECTORY).setName(segment))
                        }
                        add(ItemID().setType(ItemID.TYPE_FILE).setName(File(driveInfo.getUnixPath()).name))
                    }

                    shellLink.apply {
                        createLinkInfo()
                        linkInfo.apply {
                            createVolumeID().setDriveType(VolumeID.DRIVE_FIXED)
                            setLocalBasePath(driveInfo.getUnixPath())
                        }
                    }

                    contentResolver.openOutputStream(uri)?.use {
                        val byteWriter = ByteWriter(it)

                        with(shellLink) {
                            header.serialize(byteWriter)

                            if (header.linkFlags.hasLinkTargetIDList()) {
                                idList.serialize(byteWriter)
                            }

                            if (header.linkFlags.hasLinkInfo()) {
                                linkInfo.serialize(byteWriter)
                            }

                            if (header.linkFlags.hasName()) {
                                byteWriter.writeUnicodeString(name ?: "")
                            }

                            if (header.linkFlags.hasWorkingDir()) {
                                val workingDir = driveInfo.getWindowsPath()
                                byteWriter.writeUnicodeString(workingDir)
                            }
                            byteWriter.write4bytes(0)
                        }
                    }
                }
            }
        }

        else if (resultCode == Activity.RESULT_OK) {
            data?.data?.also { uri ->
                setIconToGame(this, uri)
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

    @Suppress("DEPRECATION")
    private fun exportLnkAction(exePath: String) {
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "application/x-ms-shortcut"
            putExtra(Intent.EXTRA_TITLE, "${File(exePath).nameWithoutExtension}.lnk")
        }

        startActivityForResult(intent, EXPORT_LNK_ACTION)
    }

    private fun fragmentLoader(fragment: Fragment, appInit: Boolean) {
        supportFragmentManager.beginTransaction().apply {
            if (appInit) {
                add(R.id.content, fragment)
            } else {
                replace(R.id.content, fragment)
            }

            commit()
        }
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
                FloatingFileManagerFragment(OPERATION_SELECT_RAT).show(supportFragmentManager, "")
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
                    val x64Folder = File("$selectedDXVK/x64")
                    val x32Folder = File("$selectedDXVK/x32")

                    if (x64Folder.exists() && x32Folder.exists()) {
                        x64Folder.copyRecursively(system32, true)
                        x32Folder.copyRecursively(syswow64, true)
                    }
                }
            }

            "WineD3D" -> {
                if (selectedWineD3D.exists()) {
                    val x64Folder = File("$selectedWineD3D/x64")
                    val x32Folder = File("$selectedWineD3D/x32")

                    if (x64Folder.exists() && x32Folder.exists()) {
                        x64Folder.copyRecursively(system32, true)
                        x32Folder.copyRecursively(syswow64, true)
                    }
                }
            }
        }

        if (selectedVKD3D.exists()) {
            File("$selectedVKD3D/x64").copyRecursively(system32, true)
            File("$selectedVKD3D/x32").copyRecursively(syswow64, true)
        }
    }

    private suspend fun runWine(exePath: String, exeArguments: String) {
        withContext(Dispatchers.Default) {
            installDXWrapper(winePrefix!!)

            runCommand("pkill -9 wineserver")
            runCommand("pkill -9 .exe")

            val skCodec = File("/system/lib64/libskcodec.so")

            if (skCodec.exists()) {
                runCommand(getEnv() + "LD_PRELOAD=$skCodec $usrDir/bin/pulseaudio --start --exit-idle=-1")
            }

            if (!enableServices) {
                lifecycleScope.launch {
                    withContext(Dispatchers.IO) {
                        val processName = if (exePath == "") "TFM.exe" else File(exePath).name

                        // Wait for Wine Successfully Start and Execute Specified Program and Kill Services
                        WineWrapper.waitFor(processName)

                        runCommand("pkill -9 services.exe", false)
                    }
                }
            }

            if (exePath == "") {
                WineWrapper.wine("explorer /desktop=shell,$selectedResolution window_handler ${getCpuHexMask()} TFM")
            } else {
                WineWrapper.wine("start /unix C:\\\\windows\\\\window_handler.exe ${getCpuHexMask()}")

                if (exePath.endsWith(".lnk")) {
                    try {
                        val shell = ShellLink(exePath)
                        val drive = DriveUtils.parseWindowsPath(shell.resolveTarget())
                        if (drive != null) {
                            WineWrapper.wine("'${getSanitizedPath(drive.getUnixPath())}' $exeArguments", "'${getSanitizedPath(File(drive.getUnixPath()).parent!!)}'")
                        }
                    }
                    catch (e: ShellLinkException) {
                        runOnUiThread {
                            Toast.makeText(this@MainActivity, getString(R.string.lnk_read_fail), Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    WineWrapper.wine("'${getSanitizedPath(exePath)}' $exeArguments", "'${getSanitizedPath(File(exePath).parent!!)}'")
                }
            }

            runCommand("pkill -9 wineserver")
            runCommand("pkill -9 .exe")

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
                    Toast.makeText(this@MainActivity, R.string.invalid_rootfs_rat_file, Toast.LENGTH_SHORT).show()
                }

                FloatingFileManagerFragment(OPERATION_SELECT_RAT).show(supportFragmentManager, "")

                return@withContext
            }

            if (ratFile.architecture != Build.SUPPORTED_ABIS[0].replace("arm64-v8a", "aarch64")) {
                abortSetup = true

                runOnUiThread {
                    Toast.makeText(this@MainActivity, R.string.invalid_architecture_rat_file, Toast.LENGTH_SHORT).show()
                }

                FloatingFileManagerFragment(OPERATION_SELECT_RAT).show(supportFragmentManager, "")

                return@withContext
            }

            dialogTitleText = getString(R.string.extracting_resources_text)

            installRat(ratFile, this@MainActivity)

            if (appBuiltinRootfs && rootfs == "") {
                File("$appRootDir/rootfs.rat").delete()
            }

            tmpDir.mkdirs()
            homeDir.mkdirs()
            winePrefixesDir.mkdirs()

            runCommand("chmod 700 -R $appRootDir")

            File("$usrDir/icons").mkdirs()

            dialogTitleText = getString(R.string.creating_wine_prefix)
            progressBarIsIndeterminate = true

            lifecycleScope.launch { runXServer(":0") }

            setSharedVars(this@MainActivity)

            val wine = File("$ratPackagesDir").listFiles()?.first { it.isDirectory && it.name.startsWith("Wine-") }?.name

            val createWinePrefixIntent = Intent(ACTION_CREATE_WINE_PREFIX).apply {
                putExtra("winePrefix", winePrefix?.path)
                putExtra("wine", wine!!)
            }

            sendBroadcast(createWinePrefixIntent)
        }
    }

    override fun onNewIntent(intent: Intent) {
        val exePath = intent.getStringExtra("exePath")
        val exeArguments = intent.getStringExtra("exeArguments")

        val emulationActivityIntent = Intent(this, EmulationActivity::class.java)

        if (exePath != null) {
            if (File(exePath).exists()) {
                sendBroadcast(
                    Intent(ACTION_RUN_WINE).apply {
                        putExtra("exePath", exePath)
                        putExtra("exeArguments", exeArguments)
                    }
                )

                startActivity(emulationActivityIntent)
            }
        }

        intent.data?.let { uri ->
            val filePath = FilePathResolver.resolvePath(this, uri)

            if (filePath != null) {
                if (File(filePath).exists()) {
                    sendBroadcast(
                        Intent(ACTION_RUN_WINE).apply {
                            putExtra("exePath", filePath)
                            putExtra("exeArguments", exeArguments)
                        }
                    )
                }
            }

            startActivity(emulationActivityIntent)
        }

        super.onNewIntent(intent)
    }

    private fun showHighlightSequence() {
        currentState = HighlightState.fromOrdinal(preferences!!.getInt(HighlightState.HIGHLIGHT_PREFERENCE_KEY, 0))
        if (currentState == HighlightState.HIGHLIGHT_DONE) {
            return
        }
        TapTargetSequence(this).targets(
                TapTarget.forView(
                    findViewById(R.id.nav_shortcuts),
                    getString(R.string.highlight_nav_shortcuts)
                ),

                TapTarget.forView(
                    findViewById(R.id.nav_settings),
                    getString(R.string.highlight_nav_settings)
                ),

                TapTarget.forView(
                    findViewById(R.id.nav_file_manager),
                    getString(R.string.highlight_nav_files),
                    getString(R.string.highlight_nav_files_description)
                )
            ).listener(object : TapTargetSequence.Listener {
                override fun onSequenceFinish() {
                }

                override fun onSequenceStep(lastTarget: TapTarget, targetClicked: Boolean) {
                    if (targetClicked) {
                        when (currentState) {
                            HighlightState.HIGHLIGHT_SHORTCUTS -> {
                                bottomNavigation?.selectedItemId = R.id.nav_shortcuts
                                currentState = HighlightState.HIGHLIGHT_SETTINGS
                                preferences!!.edit().apply {
                                    putInt(HighlightState.HIGHLIGHT_PREFERENCE_KEY, currentState!!.ordinal)
                                    apply()
                                }
                            }
                            HighlightState.HIGHLIGHT_SETTINGS -> {
                                bottomNavigation?.selectedItemId = R.id.nav_settings
                                currentState = HighlightState.HIGHLIGHT_FILES
                                preferences!!.edit().apply {
                                    putInt(HighlightState.HIGHLIGHT_PREFERENCE_KEY, currentState!!.ordinal)
                                    apply()
                                }
                            }
                            HighlightState.HIGHLIGHT_FILES -> {
                                bottomNavigation?.selectedItemId = R.id.nav_file_manager
                                currentState = HighlightState.HIGHLIGHT_DONE
                                preferences!!.edit().apply {
                                    putInt(HighlightState.HIGHLIGHT_PREFERENCE_KEY, currentState!!.ordinal)
                                    apply()
                                }
                            }
                            else -> {
                                return
                            }
                        }
                    }
                }

                override fun onSequenceCanceled(lastTarget: TapTarget) {
                }
            })
            .start()
    }

    companion object {
        @SuppressLint("SdCardPath")
        val appRootDir = File("/data/data/com.micewine.emu/files")
        var ratPackagesDir = File("$appRootDir/packages")
        var appBuiltinRootfs: Boolean = false
        private val unixUsername = runCommandWithOutput("whoami").replace("\n", "")
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
        var enableServices: Boolean = false
        var appLang: String? = null
        var box64LogLevel: String? = null
        var box64Mmap32: String? = null
        var box64Avx: String? = null
        var box64Sse42: String? = null
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
        var box64DynarecDirty: String? = null
        var box64DynarecForward: String? = null
        var box64ShowSegv: String? = null
        var box64ShowBt: String? = null
        var box64NoSigSegv: String? = null
        var box64NoSigill: String? = null
        var wineESync: String? = null
        var wineLogLevel: String? = null
        var selectedBox64: String? = null
        var selectedDriver: String? = null
        var d3dxRenderer: String? = null
        var selectedWineD3D: String? = null
        var selectedDXVK: String? = null
        var selectedVKD3D: String? = null
        var selectedGLProfile: String? = null
        var selectedDXVKHud: String? = null
        var selectedMesaVkWsiPresentMode: String? = null
        var selectedTuDebugPreset: String? = null
        var memoryStats = "??/??"
        var totalCpuUsage = "???%"
        var winePrefixesDir: File = File("$appRootDir/winePrefixes")
        var wineDisksFolder: File? = null
        var winePrefix: File? = null
        var selectedWine: String? = null
        var fileManagerDefaultDir: String = ""
        var fileManagerCwd: String? = null
        var selectedFile: String = ""
        var miceWineVersion: String = "MiceWine ${BuildConfig.VERSION_NAME} (git-${BuildConfig.GIT_SHORT_SHA})"
        var vulkanDriverDeviceName: String? = null
        var cpuAffinity: String? = null
        var screenFpsLimit: Int = 60
        var fpsLimit: Int = 0
        private var selectedResolution: String? = ""

        var selectedFragment = "ShortcutsFragment"

        const val ACTION_RUN_WINE = "com.micewine.emu.ACTION_RUN_WINE"
        const val ACTION_SETUP = "com.micewine.emu.ACTION_SETUP"
        const val ACTION_INSTALL_RAT = "com.micewine.emu.ACTION_INSTALL_RAT"
        const val ACTION_STOP_ALL = "com.micewine.emu.ACTION_STOP_ALL"
        const val ACTION_SELECT_FILE_MANAGER = "com.micewine.emu.ACTION_SELECT_FILE_MANAGER"
        const val ACTION_SELECT_ICON = "com.micewine.emu.ACTION_SELECT_ICON"
        const val ACTION_CREATE_WINE_PREFIX = "com.micewine.emu.ACTION_CREATE_WINE_PREFIX"
        const val RAM_COUNTER = "ramCounter"
        const val RAM_COUNTER_DEFAULT_VALUE = true
        const val CPU_COUNTER = "cpuCounter"
        const val CPU_COUNTER_DEFAULT_VALUE = false
        const val ENABLE_DEBUG_INFO = "debugInfo"
        const val ENABLE_DEBUG_INFO_DEFAULT_VALUE = true

        fun setupWinePrefix(winePrefix: File, wine: String) {
            if (!winePrefix.exists()) {
                val driveC = File("$winePrefix/drive_c")
                val wineUtils = File("$appRootDir/wine-utils")
                val startMenu = File("$driveC/ProgramData/Microsoft/Windows/Start Menu")
                val userSharedFolder = File("/storage/emulated/0/MiceWine")
                val localAppData = File("$driveC/users/$unixUsername/AppData")
                val system32 = File("$driveC/windows/system32")
                val syswow64 = File("$driveC/windows/syswow64")
                val winePrefixConfigFile = File("$winePrefix/config")

                winePrefix.mkdirs()
                winePrefixConfigFile.writeText(wine + "\n")

                selectedWine = wine

                WineWrapper.wine("wineboot -i")

                File("$appRootDir/wine-utils/CoreFonts").copyRecursively(File("$winePrefix/drive_c/windows/Fonts"), true)

                localAppData.copyRecursively(File("$userSharedFolder/AppData"))
                localAppData.deleteRecursively()

                File("$userSharedFolder/AppData").mkdirs()

                runCommand("ln -sf $userSharedFolder/AppData '$localAppData'")

                startMenu.deleteRecursively()

                File("$wineUtils/Start Menu").copyRecursively(File("$startMenu"), true)
                File("$wineUtils/Addons").copyRecursively(File("$driveC/Addons"), true)
                File("$wineUtils/Addons/Windows").copyRecursively(File("$driveC/windows"), true)
                File("$wineUtils/DirectX/x64").copyRecursively(system32, true)
                File("$wineUtils/DirectX/x32").copyRecursively(syswow64, true)
                File("$wineUtils/OpenAL/x64").copyRecursively(system32, true)
                File("$wineUtils/OpenAL/x32").copyRecursively(syswow64, true)

                WineWrapper.wine("regedit '$driveC/Addons/DefaultDLLsOverrides.reg'")
                WineWrapper.wine("regedit '$driveC/Addons/Themes/DarkBlue/DarkBlue.reg'")
                WineWrapper.wine("reg add HKCU\\\\Software\\\\Wine\\\\X11\\ Driver /t REG_SZ /v Decorated /d N /f")
                WineWrapper.wine("reg add HKCU\\\\Software\\\\Wine\\\\X11\\ Driver /t REG_SZ /v Managed /d N /f")
            }
        }

        private fun strBoolToNumStr(strBool: String): String {
            return strBoolToNumStr(strBool.toBoolean())
        }

        private fun strBoolToNumStr(strBool: Boolean): String {
            return if (strBool) "1" else "0"
        }

        @Suppress("DEPRECATION")
        fun setSharedVars(activity: Activity) {
            val preferences = PreferenceManager.getDefaultSharedPreferences(activity)

            appLang = activity.resources.getString(R.string.app_lang)
            appBuiltinRootfs = activity.assets.list("")?.contains("rootfs.zip")!!

            selectedBox64 = preferences.getString(SELECTED_BOX64, "")

            box64LogLevel = preferences.getString(BOX64_LOG, BOX64_LOG_DEFAULT_VALUE)

            setBox64Preset(activity, null)

            enableDRI3 = preferences.getBoolean(ENABLE_DRI3, ENABLE_DRI3_DEFAULT_VALUE)
            enableMangoHUD = preferences.getBoolean(ENABLE_MANGOHUD, ENABLE_MANGOHUD_DEFAULT_VALUE)
            enableServices = preferences.getBoolean(ENABLE_SERVICES, ENABLE_SERVICES_DEFAULT_VALUE)
            wineESync = strBoolToNumStr(preferences.getBoolean(WINE_ESYNC, WINE_ESYNC_DEFAULT_VALUE))
            wineLogLevel = preferences.getString(WINE_LOG_LEVEL, WINE_LOG_LEVEL_DEFAULT_VALUE)
            selectedDriver = preferences.getString(SELECTED_DRIVER, SELECTED_DRIVER_DEFAULT_VALUE)
            d3dxRenderer = preferences.getString(SELECTED_D3DX_RENDERER, SELECTED_D3DX_RENDERER_DEFAULT_VALUE)
            selectedWineD3D = preferences.getString(SELECTED_WINED3D, SELECTED_WINED3D_DEFAULT_VALUE)
            selectedDXVK = preferences.getString(SELECTED_DXVK, SELECTED_DXVK_DEFAULT_VALUE)
            selectedVKD3D = preferences.getString(SELECTED_VKD3D, SELECTED_VKD3D_DEFAULT_VALUE)
            selectedGLProfile = preferences.getString(SELECTED_GL_PROFILE, SELECTED_GL_PROFILE_DEFAULT_VALUE)
            selectedDXVKHud = preferences.getString(SELECTED_DXVK_HUD_PRESET, SELECTED_DXVK_HUD_PRESET_DEFAULT_VALUE)
            selectedMesaVkWsiPresentMode = preferences.getString(SELECTED_MESA_VK_WSI_PRESENT_MODE, SELECTED_MESA_VK_WSI_PRESENT_MODE_DEFAULT_VALUE)
            selectedTuDebugPreset = preferences.getString(SELECTED_TU_DEBUG_PRESET, SELECTED_TU_DEBUG_PRESET_DEFAULT_VALUE)
            selectedResolution = preferences.getString(DISPLAY_RESOLUTION, DISPLAY_RESOLUTION_DEFAULT_VALUE)
            enableRamCounter = preferences.getBoolean(RAM_COUNTER, RAM_COUNTER_DEFAULT_VALUE)
            enableCpuCounter = preferences.getBoolean(CPU_COUNTER, CPU_COUNTER_DEFAULT_VALUE)
            enableDebugInfo = preferences.getBoolean(ENABLE_DEBUG_INFO, ENABLE_DEBUG_INFO_DEFAULT_VALUE)

            cpuAffinity = preferences.getString(CPU_AFFINITY, availableCPUs.joinToString(","))

            screenFpsLimit = (activity.getSystemService(WINDOW_SERVICE) as WindowManager).defaultDisplay.refreshRate.toInt()
            fpsLimit = preferences.getInt(FPS_LIMIT, screenFpsLimit)

            vulkanDriverDeviceName = getVulkanDeviceName()

            winePrefix = File("$winePrefixesDir/${preferences.getString(SELECTED_WINE_PREFIX, "default")}")
            wineDisksFolder = File("$winePrefix/dosdevices/")

            val winePrefixConfigFile = File("$winePrefix/config")
            if (winePrefixConfigFile.exists()) {
                selectedWine = winePrefixConfigFile.readLines()[0]
            }

            fileManagerDefaultDir = wineDisksFolder!!.path
        }

        fun setBox64Preset(activity: Activity, name: String?) {
            val preferences = PreferenceManager.getDefaultSharedPreferences(activity)!!
            val selectedBox64Preset = name ?: preferences.getString(SELECTED_BOX64_PRESET_KEY, "default")!!

            box64Mmap32 = strBoolToNumStr(getBox64Mapping(selectedBox64Preset, BOX64_MMAP32)[0])
            box64Avx = getBox64Mapping(selectedBox64Preset, BOX64_AVX)[0]
            box64Sse42 = strBoolToNumStr(getBox64Mapping(selectedBox64Preset, BOX64_SSE42)[0])
            box64DynarecBigblock = getBox64Mapping(selectedBox64Preset, BOX64_DYNAREC_BIGBLOCK)[0]
            box64DynarecStrongmem = getBox64Mapping(selectedBox64Preset, BOX64_DYNAREC_STRONGMEM)[0]
            box64DynarecWeakbarrier = getBox64Mapping(selectedBox64Preset, BOX64_DYNAREC_WEAKBARRIER)[0]
            box64DynarecPause = getBox64Mapping(selectedBox64Preset, BOX64_DYNAREC_PAUSE)[0]
            box64DynarecX87double = strBoolToNumStr(getBox64Mapping(selectedBox64Preset, BOX64_DYNAREC_X87DOUBLE)[0])
            box64DynarecFastnan = strBoolToNumStr(getBox64Mapping(selectedBox64Preset, BOX64_DYNAREC_FASTNAN)[0])
            box64DynarecFastround = strBoolToNumStr(getBox64Mapping(selectedBox64Preset, BOX64_DYNAREC_FASTROUND)[0])
            box64DynarecSafeflags = getBox64Mapping(selectedBox64Preset, BOX64_DYNAREC_SAFEFLAGS)[0]
            box64DynarecCallret = strBoolToNumStr(getBox64Mapping(selectedBox64Preset, BOX64_DYNAREC_CALLRET)[0])
            box64DynarecAlignedAtomics = strBoolToNumStr(getBox64Mapping(selectedBox64Preset, BOX64_DYNAREC_ALIGNED_ATOMICS)[0])
            box64DynarecNativeflags = strBoolToNumStr(getBox64Mapping(selectedBox64Preset, BOX64_DYNAREC_NATIVEFLAGS)[0])
            box64DynarecWait = strBoolToNumStr(getBox64Mapping(selectedBox64Preset, BOX64_DYNAREC_WAIT)[0])
            box64DynarecDirty = strBoolToNumStr(getBox64Mapping(selectedBox64Preset, BOX64_DYNAREC_DIRTY)[0])
            box64DynarecForward = getBox64Mapping(selectedBox64Preset, BOX64_DYNAREC_FORWARD)[0]
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

        private fun getVulkanDeviceName(): String {
            return runCommandWithOutput(getEnv() + "DISPLAY= vulkaninfo | grep deviceName | cut -d '=' -f 2")
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

                    Thread.sleep(800)
                }
            }
        }

        suspend fun getCpuInfo() {
            withContext(Dispatchers.IO) {
                val availProcessors = Runtime.getRuntime().availableProcessors()

                while (enableCpuCounter) {
                    val usageInfo = runCommandWithOutput("top -bn 1 -u \$(whoami) -o %CPU -q | head -n 1").toFloat() / availProcessors

                    totalCpuUsage = "$usageInfo%"

                    Thread.sleep(800)
                }
            }
        }

        const val EXPORT_LNK_ACTION = 1
    }
}
