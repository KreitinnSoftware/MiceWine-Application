package com.micewine.emu.activities

import android.annotation.SuppressLint
import android.app.Activity
import android.app.ActivityManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.hardware.input.InputManager
import android.os.Build
import android.os.Bundle
import android.os.storage.StorageManager
import android.util.DisplayMetrics
import android.view.InputDevice
import android.view.KeyEvent
import android.view.Menu
import android.view.MenuItem
import android.view.MotionEvent
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.preference.PreferenceManager
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.gson.GsonBuilder
import com.micewine.emu.BuildConfig
import com.micewine.emu.R
import com.micewine.emu.activities.EmulationActivity.Companion.sharedLogs
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
import com.micewine.emu.activities.GeneralSettingsActivity.Companion.BOX64_NOSIGILL
import com.micewine.emu.activities.GeneralSettingsActivity.Companion.BOX64_NOSIGILL_DEFAULT_VALUE
import com.micewine.emu.activities.GeneralSettingsActivity.Companion.BOX64_NOSIGSEGV
import com.micewine.emu.activities.GeneralSettingsActivity.Companion.BOX64_NOSIGSEGV_DEFAULT_VALUE
import com.micewine.emu.activities.GeneralSettingsActivity.Companion.BOX64_SHOWBT
import com.micewine.emu.activities.GeneralSettingsActivity.Companion.BOX64_SHOWBT_DEFAULT_VALUE
import com.micewine.emu.activities.GeneralSettingsActivity.Companion.BOX64_SHOWSEGV
import com.micewine.emu.activities.GeneralSettingsActivity.Companion.BOX64_SHOWSEGV_DEFAULT_VALUE
import com.micewine.emu.activities.GeneralSettingsActivity.Companion.BOX64_SSE42
import com.micewine.emu.activities.GeneralSettingsActivity.Companion.ENABLE_DRI3
import com.micewine.emu.activities.GeneralSettingsActivity.Companion.ENABLE_DRI3_DEFAULT_VALUE
import com.micewine.emu.activities.GeneralSettingsActivity.Companion.ENABLE_MANGOHUD
import com.micewine.emu.activities.GeneralSettingsActivity.Companion.ENABLE_MANGOHUD_DEFAULT_VALUE
import com.micewine.emu.activities.GeneralSettingsActivity.Companion.FPS_LIMIT
import com.micewine.emu.activities.GeneralSettingsActivity.Companion.PA_SINK
import com.micewine.emu.activities.GeneralSettingsActivity.Companion.PA_SINK_DEFAULT_VALUE
import com.micewine.emu.activities.GeneralSettingsActivity.Companion.SELECTED_BOX64
import com.micewine.emu.activities.GeneralSettingsActivity.Companion.SELECTED_DXVK_HUD_PRESET
import com.micewine.emu.activities.GeneralSettingsActivity.Companion.SELECTED_DXVK_HUD_PRESET_DEFAULT_VALUE
import com.micewine.emu.activities.GeneralSettingsActivity.Companion.SELECTED_GL_PROFILE
import com.micewine.emu.activities.GeneralSettingsActivity.Companion.SELECTED_GL_PROFILE_DEFAULT_VALUE
import com.micewine.emu.activities.GeneralSettingsActivity.Companion.SELECTED_MESA_VK_WSI_PRESENT_MODE
import com.micewine.emu.activities.GeneralSettingsActivity.Companion.SELECTED_MESA_VK_WSI_PRESENT_MODE_DEFAULT_VALUE
import com.micewine.emu.activities.GeneralSettingsActivity.Companion.SELECTED_TU_DEBUG_PRESET
import com.micewine.emu.activities.GeneralSettingsActivity.Companion.SELECTED_TU_DEBUG_PRESET_DEFAULT_VALUE
import com.micewine.emu.activities.GeneralSettingsActivity.Companion.SELECTED_VULKAN_DRIVER
import com.micewine.emu.activities.GeneralSettingsActivity.Companion.WINE_LOG_LEVEL
import com.micewine.emu.activities.GeneralSettingsActivity.Companion.WINE_LOG_LEVEL_DEFAULT_VALUE
import com.micewine.emu.activities.PresetManagerActivity.Companion.SELECTED_BOX64_PRESET
import com.micewine.emu.activities.RatManagerActivity.Companion.generateICDFile
import com.micewine.emu.activities.RatManagerActivity.Companion.generateMangoHUDConfFile
import com.micewine.emu.adapters.AdapterBottomNavigation
import com.micewine.emu.adapters.AdapterGame.Companion.selectedGameName
import com.micewine.emu.controller.ControllerUtils
import com.micewine.emu.controller.ControllerUtils.connectedPhysicalControllers
import com.micewine.emu.controller.ControllerUtils.controllerMouseEmulation
import com.micewine.emu.controller.ControllerUtils.disconnectController
import com.micewine.emu.controller.ControllerUtils.prepareControllersMappings
import com.micewine.emu.core.EnvVars.getEnv
import com.micewine.emu.core.RatPackageManager
import com.micewine.emu.core.RatPackageManager.checkPackageInstalled
import com.micewine.emu.core.RatPackageManager.installADToolsDriver
import com.micewine.emu.core.RatPackageManager.installRat
import com.micewine.emu.core.RatPackageManager.installablePackagesCategories
import com.micewine.emu.core.RatPackageManager.listRatPackages
import com.micewine.emu.core.ShellLoader.runCommand
import com.micewine.emu.core.ShellLoader.runCommandWithOutput
import com.micewine.emu.core.WineWrapper
import com.micewine.emu.core.WineWrapper.getCpuHexMask
import com.micewine.emu.core.WineWrapper.getSanitizedPath
import com.micewine.emu.databinding.ActivityMainBinding
import com.micewine.emu.fragments.AskInstallPackageFragment
import com.micewine.emu.fragments.AskInstallPackageFragment.Companion.ADTOOLS_DRIVER_PACKAGE
import com.micewine.emu.fragments.AskInstallPackageFragment.Companion.MWP_PRESET_PACKAGE
import com.micewine.emu.fragments.AskInstallPackageFragment.Companion.RAT_PACKAGE
import com.micewine.emu.fragments.AskInstallPackageFragment.Companion.adToolsDriverCandidate
import com.micewine.emu.fragments.AskInstallPackageFragment.Companion.mwpPresetCandidate
import com.micewine.emu.fragments.AskInstallPackageFragment.Companion.ratCandidate
import com.micewine.emu.fragments.Box64PresetManagerFragment
import com.micewine.emu.fragments.Box64PresetManagerFragment.Companion.getBox64Mapping
import com.micewine.emu.fragments.ControllerPresetManagerFragment
import com.micewine.emu.fragments.ControllerSettingsFragment.Companion.ACTION_UPDATE_CONTROLLERS_STATUS
import com.micewine.emu.fragments.CreatePresetFragment.Companion.BOX64_PRESET
import com.micewine.emu.fragments.CreatePresetFragment.Companion.CONTROLLER_PRESET
import com.micewine.emu.fragments.CreatePresetFragment.Companion.VIRTUAL_CONTROLLER_PRESET
import com.micewine.emu.fragments.DebugSettingsFragment.Companion.availableCPUs
import com.micewine.emu.fragments.EditGamePreferencesFragment
import com.micewine.emu.fragments.EditGamePreferencesFragment.Companion.FILE_MANAGER_START_PREFERENCES
import com.micewine.emu.fragments.FileManagerFragment.Companion.refreshFiles
import com.micewine.emu.fragments.FloatingFileManagerFragment
import com.micewine.emu.fragments.FloatingFileManagerFragment.Companion.OPERATION_SELECT_EXE
import com.micewine.emu.fragments.FloatingFileManagerFragment.Companion.OPERATION_SELECT_RAT
import com.micewine.emu.fragments.SetupFragment
import com.micewine.emu.fragments.SetupFragment.Companion.abortSetup
import com.micewine.emu.fragments.SetupFragment.Companion.dialogTitleText
import com.micewine.emu.fragments.SetupFragment.Companion.progressBarIsIndeterminate
import com.micewine.emu.fragments.ShortcutsFragment
import com.micewine.emu.fragments.ShortcutsFragment.Companion.ACTION_UPDATE_WINE_PREFIX_SPINNER
import com.micewine.emu.fragments.ShortcutsFragment.Companion.ADRENO_TOOLS_DRIVER
import com.micewine.emu.fragments.ShortcutsFragment.Companion.MESA_DRIVER
import com.micewine.emu.fragments.ShortcutsFragment.Companion.addGameToList
import com.micewine.emu.fragments.ShortcutsFragment.Companion.getBox64Preset
import com.micewine.emu.fragments.ShortcutsFragment.Companion.getBox64Version
import com.micewine.emu.fragments.ShortcutsFragment.Companion.getCpuAffinity
import com.micewine.emu.fragments.ShortcutsFragment.Companion.getD3DXRenderer
import com.micewine.emu.fragments.ShortcutsFragment.Companion.getDXVKVersion
import com.micewine.emu.fragments.ShortcutsFragment.Companion.getDisplaySettings
import com.micewine.emu.fragments.ShortcutsFragment.Companion.getExeArguments
import com.micewine.emu.fragments.ShortcutsFragment.Companion.getExePath
import com.micewine.emu.fragments.ShortcutsFragment.Companion.getSelectedVirtualControllerPreset
import com.micewine.emu.fragments.ShortcutsFragment.Companion.getVKD3DVersion
import com.micewine.emu.fragments.ShortcutsFragment.Companion.getVulkanDriver
import com.micewine.emu.fragments.ShortcutsFragment.Companion.getWineD3DVersion
import com.micewine.emu.fragments.ShortcutsFragment.Companion.getWineESync
import com.micewine.emu.fragments.ShortcutsFragment.Companion.getWineServices
import com.micewine.emu.fragments.ShortcutsFragment.Companion.getWineVirtualDesktop
import com.micewine.emu.fragments.ShortcutsFragment.Companion.setIconToGame
import com.micewine.emu.fragments.SoundSettingsFragment.Companion.generatePAFile
import com.micewine.emu.fragments.VirtualControllerPresetManagerFragment
import com.micewine.emu.fragments.WinePrefixManagerFragment.Companion.createWinePrefix
import com.micewine.emu.fragments.WinePrefixManagerFragment.Companion.getSelectedWinePrefix
import com.micewine.emu.fragments.WinePrefixManagerFragment.Companion.getWinePrefixFile
import com.micewine.emu.utils.DriveUtils
import com.micewine.emu.utils.FilePathResolver
import io.ByteWriter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
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
import java.util.Locale

class MainActivity : AppCompatActivity() {
    private var binding: ActivityMainBinding? = null
    private val receiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                ACTION_RUN_WINE -> {
                    val exePath = intent.getStringExtra("exePath")!!
                    val exeArguments = intent.getStringExtra("exeArguments") ?: ""
                    var driverName = intent.getStringExtra("driverName") ?: "Global"
                    var driverType = intent.getIntExtra("driverType", MESA_DRIVER)
                    var box64Version = intent.getStringExtra("box64Version") ?: "Global"
                    val box64Preset = intent.getStringExtra("box64Preset") ?: "default"
                    val displayResolution = intent.getStringExtra("displayResolution") ?: "1280x720"
                    val d3dxRenderer = intent.getStringExtra("d3dxRenderer") ?: "DXVK"
                    val wineD3D = intent.getStringExtra("wineD3D") ?: listRatPackages("WineD3D-").map { it.name + " " + it.version }.first()
                    val dxvk = intent.getStringExtra("dxvk") ?: listRatPackages("DXVK-").map { it.name + " " + it.version }.first()
                    val vkd3d = intent.getStringExtra("vkd3d") ?: listRatPackages("VKD3D-").map { it.name + " " + it.version }.first()
                    val esync = intent.getBooleanExtra("esync", true)
                    val services = intent.getBooleanExtra("services", false)
                    val virtualDesktop = intent.getBooleanExtra("virtualDesktop", false)
                    val cpuAffinity = intent.getStringExtra("cpuAffinity") ?: availableCPUs.joinToString(",")

                    tmpDir.deleteRecursively()
                    tmpDir.mkdirs()

                    if (driverName == "Global") {
                        driverName = preferences?.getString(SELECTED_VULKAN_DRIVER, "").toString()
                        driverType = if (driverName.startsWith("AdrenoToolsDriver-")) ADRENO_TOOLS_DRIVER else MESA_DRIVER
                    }
                    if (box64Version == "Global") {
                        box64Version = preferences?.getString(SELECTED_BOX64, "").toString()
                    }

                    setSharedVars(
                        this@MainActivity,
                        box64Version,
                        box64Preset,
                        d3dxRenderer,
                        wineD3D,
                        dxvk,
                        vkd3d,
                        displayResolution,
                        esync,
                        services,
                        virtualDesktop
                    )

                    val driverLibPath: String = when (driverType) {
                        MESA_DRIVER -> {
                            File("$ratPackagesDir/${driverName.ifEmpty { File("$appRootDir/packages").listFiles()?.filter { it.name.startsWith("VulkanDriver-") }?.map { it.name }!![0] }}/pkg-header").readLines()[4].substringAfter("=")
                        }
                        ADRENO_TOOLS_DRIVER -> {
                            File("$ratPackagesDir/${File("$appRootDir/packages").listFiles()?.first { it.name.startsWith("AdrenoTools-") }?.name}/pkg-header").readLines()[4].substringAfter("=")
                        }
                        else -> ""
                    }

                    generateICDFile(driverLibPath)
                    generateMangoHUDConfFile()
                    generatePAFile()

                    val adrenoToolsDriverPath = File("$ratPackagesDir/$driverName/pkg-header").readLines()[4].substringAfter("=")

                    setSharedVars(
                        this@MainActivity,
                        box64Version,
                        box64Preset,
                        d3dxRenderer,
                        wineD3D,
                        dxvk,
                        vkd3d,
                        displayResolution,
                        esync,
                        services,
                        virtualDesktop,
                        cpuAffinity,
                        if (driverType == ADRENO_TOOLS_DRIVER) adrenoToolsDriverPath else null
                    )

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
                            var exeFile: File? = null

                            if (fileExtension == "lnk") {
                                try {
                                    val drive = DriveUtils.parseWindowsPath(ShellLink(file).resolveTarget())

                                    if (drive == null || !File(drive.getUnixPath()).exists()) {
                                        Toast.makeText(this@MainActivity, getString(R.string.lnk_read_fail), Toast.LENGTH_SHORT).show()
                                        return
                                    }

                                    exeFile = File(drive.getUnixPath())
                                } catch(_: ShellLinkException) {
                                    Toast.makeText(this@MainActivity, getString(R.string.lnk_read_fail), Toast.LENGTH_SHORT).show()
                                }
                            } else {
                                exeFile = file
                            }

                            EditGamePreferencesFragment(FILE_MANAGER_START_PREFERENCES, exeFile!!).show(supportFragmentManager, "")
                        } else if (file.name.endsWith("rat")) {
                            ratCandidate = RatPackageManager.RatPackage(file.path)

                            if (ratCandidate?.name != null) {
                                AskInstallPackageFragment(RAT_PACKAGE).show(supportFragmentManager, "")
                            }
                        } else if (file.name.endsWith(".zip")) {
                            adToolsDriverCandidate = RatPackageManager.AdrenoToolsPackage(file.path)

                            if (adToolsDriverCandidate?.name != null) {
                                AskInstallPackageFragment(ADTOOLS_DRIVER_PACKAGE).show(supportFragmentManager, "")
                            }
                        } else if (file.name.endsWith(".mwp")) {
                            val mwpLines = file.readLines()
                            if (mwpLines.isNotEmpty()) {
                                when (mwpLines[0]) {
                                    "controllerPreset" -> {
                                        mwpPresetCandidate = Pair(CONTROLLER_PRESET, file.path)
                                    }
                                    "virtualControllerPreset" -> {
                                        mwpPresetCandidate = Pair(VIRTUAL_CONTROLLER_PRESET, file.path)
                                    }
                                    "box64Preset" -> {
                                        mwpPresetCandidate = Pair(BOX64_PRESET, file.path)
                                    }
                                }

                                AskInstallPackageFragment(MWP_PRESET_PACKAGE).show(supportFragmentManager, "")
                            }
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
                        val ratFile = ratCandidate!!

                        if (!(ratFile.architecture == deviceArch || ratFile.architecture == "any") && ratFile.category != "Wine") {
                            Toast.makeText(context, R.string.invalid_architecture_rat_file, Toast.LENGTH_SHORT).show()
                            return@launch
                        }

                        if (ratFile.category == "rootfs") {
                            Toast.makeText(context, R.string.error_install_rootfs_file_manager, Toast.LENGTH_SHORT).show()
                            return@launch
                        }

                        if (ratFile.category !in installablePackagesCategories) {
                            Toast.makeText(context, R.string.unknown_package_category, Toast.LENGTH_SHORT).show()
                            return@launch
                        }

                        if (checkPackageInstalled(ratFile.name!!, ratFile.category!!, ratFile.version!!)) {
                            Toast.makeText(context, R.string.package_already_installed, Toast.LENGTH_SHORT).show()
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
                ACTION_INSTALL_ADTOOLS_DRIVER -> {
                    val adToolsDriverFile = adToolsDriverCandidate!!

                    lifecycleScope.launch {
                        if (checkPackageInstalled(adToolsDriverFile.name + " (AdrenoTools)", "AdrenoToolsDriver", adToolsDriverFile.version!!)) {
                            Toast.makeText(context, R.string.package_already_installed, Toast.LENGTH_SHORT).show()
                            return@launch
                        }

                        withContext(Dispatchers.Default) {
                            setupDone = false

                            SetupFragment().show(supportFragmentManager, "")

                            dialogTitleText = "Installing ${adToolsDriverFile.name} (${adToolsDriverFile.version})..."
                            progressBarIsIndeterminate = true

                            installADToolsDriver(adToolsDriverFile)

                            setupDone = true
                        }
                    }
                }
                ACTION_SELECT_ICON -> {
                    openFilePicker()
                }
                ACTION_SELECT_EXE_PATH -> {
                    FloatingFileManagerFragment(OPERATION_SELECT_EXE, wineDisksFolder!!.path).show(supportFragmentManager, "")
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
                            createWinePrefix(winePrefix, wine)

                            fileManagerCwd = fileManagerDefaultDir
                            setupDone = true

                            val updateWinePrefixSpinnerIntent = Intent(ACTION_UPDATE_WINE_PREFIX_SPINNER).apply {
                                putExtra("prefixName", File(winePrefix).name)
                            }

                            sendBroadcast(
                                updateWinePrefixSpinnerIntent
                            )
                        }
                    }
                }
            }
        }
    }
    private var inputManager: InputManager? = null
    private val inputDeviceListener: InputManager.InputDeviceListener = object : InputManager.InputDeviceListener {
        override fun onInputDeviceAdded(deviceId: Int) {
            InputDevice.getDevice(deviceId)
        }

        override fun onInputDeviceChanged(deviceId: Int) {
            val inputDevice = InputDevice.getDevice(deviceId) ?: return

            if (((inputDevice.sources and InputDevice.SOURCE_GAMEPAD == InputDevice.SOURCE_GAMEPAD) ||
                (inputDevice.sources and InputDevice.SOURCE_JOYSTICK == InputDevice.SOURCE_JOYSTICK)) &&
                (!inputDevice.name.contains("uinput"))
            ) {
                if (connectedPhysicalControllers.indexOfFirst { it.id == deviceId } == -1) {
                    connectedPhysicalControllers.add(
                        ControllerUtils.PhysicalController(
                            inputDevice.name,
                            deviceId,
                            -1,
                            -1,
                            inputDevice.motionRanges.any { it.axis == MotionEvent.AXIS_LTRIGGER } && inputDevice.motionRanges.any { it.axis == MotionEvent.AXIS_RTRIGGER },
                            inputDevice.motionRanges.find { it.axis == MotionEvent.AXIS_X }!!.flat
                        )
                    )
                    sharedLogs?.appendText(getDeviceInfoJson(inputDevice))
                }
                prepareControllersMappings()
                sendBroadcast(
                    Intent(ACTION_UPDATE_CONTROLLERS_STATUS)
                )
            }
        }

        override fun onInputDeviceRemoved(deviceId: Int) {
            val index = connectedPhysicalControllers.indexOfFirst { it.id == deviceId }
            if (index == -1) return

            disconnectController(connectedPhysicalControllers[index].virtualControllerID)
            connectedPhysicalControllers.removeAt(index)

            sendBroadcast(
                Intent(ACTION_UPDATE_CONTROLLERS_STATUS)
            )
        }
    }

    private fun getDeviceInfoJson(device: InputDevice): String {
        val gson = GsonBuilder().setPrettyPrinting().create()

        val motionRanges = device.motionRanges.map { range ->
            mapOf(
                "axisName" to MotionEvent.axisToString(range.axis),
                "axisId" to range.axis,
                "source" to range.source,
                "min" to range.min,
                "max" to range.max,
                "flat" to range.flat,
                "fuzz" to range.fuzz,
                "resolution" to range.resolution
            )
        }

        val jsonMap = mapOf(
            "id" to device.id,
            "name" to device.name,
            "isVirtual" to device.isVirtual,
            "keyboardType" to device.keyboardType,
            "controllerNumber" to device.controllerNumber,
            "sources" to device.sources,
            "vendorId" to device.vendorId,
            "productId" to device.productId,
            "hasMicrophone" to device.hasMicrophone(),
            "motionRanges" to motionRanges
        )

        return gson.toJson(jsonMap)
    }

    private var bottomNavigation: BottomNavigationView? = null
    private var viewPager: ViewPager2? = null
    private var runningXServer = false

    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        preferences = PreferenceManager.getDefaultSharedPreferences(this)

        ControllerPresetManagerFragment.initialize()
        VirtualControllerPresetManagerFragment.initialize()
        Box64PresetManagerFragment.initialize()
        ShortcutsFragment.initialize()
        ControllerUtils.initialize(this)

        lifecycleScope.launch {
            controllerMouseEmulation()
        }

        inputManager = getSystemService(INPUT_SERVICE) as InputManager
        inputManager?.registerInputDeviceListener(inputDeviceListener, null)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding!!.root)

        setSharedVars(this)

        // On future here will have a code for check if app is updated and do specific data conversion if needed

        preferences?.edit()?.apply {
            putString(APP_VERSION, BuildConfig.VERSION_NAME)
            apply()
        }

        bottomNavigation = findViewById(R.id.bottom_navigation)
        bottomNavigation?.setOnItemSelectedListener { item: MenuItem ->
            when (item.itemId) {
                R.id.nav_shortcuts -> {
                    selectedFragmentId = 0
                    viewPager?.currentItem = 0
                }
                R.id.nav_settings -> {
                    selectedFragmentId = 1
                    viewPager?.currentItem = 1
                }
                R.id.nav_file_manager -> {
                    selectedFragmentId = 2
                    viewPager?.currentItem = 2
                }
                R.id.nav_about -> {
                    selectedFragmentId = 3
                    viewPager?.currentItem = 3
                }
            }

            true
        }

        viewPager = findViewById(R.id.viewPager)
        viewPager?.adapter = AdapterBottomNavigation(this)
        viewPager?.isUserInputEnabled = false

        bottomNavigation?.post {
            bottomNavigation?.selectedItemId = R.id.nav_shortcuts
        }

        registerReceiver(receiver, object : IntentFilter() {
            init {
                addAction(ACTION_RUN_WINE)
                addAction(ACTION_SETUP)
                addAction(ACTION_INSTALL_RAT)
                addAction(ACTION_INSTALL_ADTOOLS_DRIVER)
                addAction(ACTION_SELECT_FILE_MANAGER)
                addAction(ACTION_SELECT_ICON)
                addAction(ACTION_SELECT_EXE_PATH)
                addAction(ACTION_CREATE_WINE_PREFIX)
            }
        })

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (getWinePrefixFile(winePrefix!!).exists()) {
                WineWrapper.clearDrives()

                (application.getSystemService(Context.STORAGE_SERVICE) as StorageManager).storageVolumes.forEach { volume ->
                    if (volume.isRemovable) {
                        WineWrapper.addDrive("${volume.directory}")
                    }
                }
            }
        }

        if (savedInstanceState == null) {
            onNewIntent(intent)
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
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (selectedFragmentId == 2) {
                if (fileManagerCwd != fileManagerDefaultDir) {
                    fileManagerCwd = File(fileManagerCwd!!).parent!!

                    refreshFiles()

                    return true
                }
            }

            if (selectedFragmentId > 0) {
                bottomNavigation?.selectedItemId = R.id.nav_shortcuts

                return true
            }

            sendBroadcast(
                Intent(ACTION_STOP_ALL)
            )
        }

        return super.onKeyDown(keyCode, event)
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
        } else if (resultCode == Activity.RESULT_OK) {
            data?.data?.also { uri ->
                setIconToGame(selectedGameName, this, uri)
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

    override fun onDestroy() {
        super.onDestroy()
        binding = null
        unregisterReceiver(receiver)
        inputManager?.unregisterInputDeviceListener(inputDeviceListener)
    }

    override fun onResume() {
        super.onResume()

        lifecycleScope.launch { runXServer(":0") }

        if (!setupDone) {
            if (appBuiltinRootfs) {
                SetupFragment().show(supportFragmentManager , "")
            } else {
                FloatingFileManagerFragment(OPERATION_SELECT_RAT).show(supportFragmentManager, "")
            }
        }
    }

    private fun installDXWrapper(prefixName: String) {
        val winePrefix = getWinePrefixFile(prefixName)

        val driveC = File("$winePrefix/drive_c")
        val system32 = File("$driveC/windows/system32")
        val syswow64 = File("$driveC/windows/syswow64")
        val selectedDXVK = File("$ratPackagesDir/$selectedDXVK")
        val selectedVKD3D = File("$ratPackagesDir/$selectedVKD3D")
        val selectedWineD3D = File("$ratPackagesDir/$selectedWineD3D")

        when (selectedD3DXRenderer) {
            "DXVK" -> {
                if (selectedDXVK.exists()) {
                    val x64Folder = File("$selectedDXVK/files/x64")
                    val x32Folder = File("$selectedDXVK/files/x32")

                    if (x64Folder.exists() && x32Folder.exists()) {
                        x64Folder.copyRecursively(system32, true)
                        x32Folder.copyRecursively(syswow64, true)
                    }
                }
            }

            "WineD3D" -> {
                if (selectedWineD3D.exists()) {
                    val x64Folder = File("$selectedWineD3D/files/x64")
                    val x32Folder = File("$selectedWineD3D/files/x32")

                    if (x64Folder.exists() && x32Folder.exists()) {
                        x64Folder.copyRecursively(system32, true)
                        x32Folder.copyRecursively(syswow64, true)
                    }
                }
            }
        }

        if (selectedVKD3D.exists()) {
            File("$selectedVKD3D/files/x64").copyRecursively(system32, true)
            File("$selectedVKD3D/files/x32").copyRecursively(syswow64, true)
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

            if (!wineServices) {
                lifecycleScope.launch {
                    withContext(Dispatchers.IO) {
                        val processName = if (exePath == "") "TFM.exe" else File(exePath).name

                        // Wait for Wine Successfully Start and Execute Specified Program and Kill Services
                        WineWrapper.waitForProcess(processName)

                        runCommand("pkill -9 services.exe", false)
                    }
                }
            }

            if (exePath == "") {
                WineWrapper.wine("explorer /desktop=shell,$selectedResolution window_handler ${getCpuHexMask()} TFM")
            } else {
                if (enableWineVirtualDesktop) {
                    WineWrapper.wine("explorer /desktop=shell,$selectedResolution window_handler ${getCpuHexMask()} '${getSanitizedPath(exePath)}' $exeArguments", "'${getSanitizedPath(File(exePath).parent!!)}'")
                } else {
                    lifecycleScope.launch {
                        withContext(Dispatchers.IO) {
                            WineWrapper.wine("start /unix C:\\\\windows\\\\window_handler.exe ${getCpuHexMask()}")
                        }
                    }

                    if (exePath.endsWith(".lnk")) {
                        val drive = DriveUtils.parseWindowsPath(ShellLink(exePath).resolveTarget())
                        if (drive == null) {
                            Toast.makeText(this@MainActivity, getString(R.string.lnk_read_fail), Toast.LENGTH_SHORT).show()
                            return@withContext
                        }

                        WineWrapper.wine("'${getSanitizedPath(drive.getUnixPath())}' $exeArguments", "'${getSanitizedPath(File(drive.getUnixPath()).parent!!)}'")
                    }

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
            if (runningXServer && !setupDone) {
                return@withContext
            }

            runningXServer = true

            runCommand(
                "env CLASSPATH=${getClassPath(this@MainActivity)} /system/bin/app_process / com.micewine.emu.CmdEntryPoint $display &> /dev/null"
            )

            runningXServer = false
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

            if (ratFile.architecture != deviceArch) {
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

            addGameToList(getString(R.string.desktop_mode_init), getString(R.string.desktop_mode_init), "")

            dialogTitleText = getString(R.string.creating_wine_prefix)
            progressBarIsIndeterminate = true

            lifecycleScope.launch { runXServer(":0") }

            setSharedVars(this@MainActivity)

            val wine = File("$ratPackagesDir").listFiles()?.first { it.isDirectory && it.name.startsWith("Wine-") }?.name
            val createWinePrefixIntent = Intent(ACTION_CREATE_WINE_PREFIX).apply {
                putExtra("winePrefix", winePrefix)
                putExtra("wine", wine!!)
            }

            sendBroadcast(createWinePrefixIntent)
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)

        val shortcutName = intent.getStringExtra("shortcutName")
        if (shortcutName != null) {
            val emulationActivityIntent = Intent(this, EmulationActivity::class.java)

            selectedGameName = shortcutName

            var driverName = getVulkanDriver(shortcutName)
            if (driverName == "Global") {
                driverName = preferences?.getString(SELECTED_VULKAN_DRIVER, "").toString()
            }
            var box64Version = getBox64Version(shortcutName)
            if (box64Version == "Global") {
                box64Version = preferences?.getString(SELECTED_BOX64, "").toString()
            }

            val driverType = if (driverName.startsWith("AdrenoToolsDriver-")) ADRENO_TOOLS_DRIVER else MESA_DRIVER

            val runWineIntent = Intent(ACTION_RUN_WINE).apply {
                putExtra("exePath", getExePath(shortcutName))
                putExtra("exeArguments", getExeArguments(shortcutName))
                putExtra("driverName", driverName)
                putExtra("driverType", driverType)
                putExtra("box64Version", box64Version)
                putExtra("box64Preset", getBox64Preset(shortcutName))
                putExtra("displayResolution", getDisplaySettings(shortcutName)[1])
                putExtra("virtualControllerPreset", getSelectedVirtualControllerPreset(shortcutName))
                putExtra("d3dxRenderer", getD3DXRenderer(shortcutName))
                putExtra("wineD3D", getWineD3DVersion(shortcutName))
                putExtra("dxvk", getDXVKVersion(shortcutName))
                putExtra("vkd3d", getVKD3DVersion(shortcutName))
                putExtra("esync", getWineESync(shortcutName))
                putExtra("services", getWineServices(shortcutName))
                putExtra("virtualDesktop", getWineVirtualDesktop(shortcutName))
                putExtra("cpuAffinity", getCpuAffinity(shortcutName))
            }

            sendBroadcast(runWineIntent)
            startActivity(emulationActivityIntent)

            return
        }

        intent.data?.let { uri ->
            val filePath = FilePathResolver.resolvePath(this, uri)

            if (filePath != null) {
                if (File(filePath).exists()) {
                    EditGamePreferencesFragment(FILE_MANAGER_START_PREFERENCES, File(filePath)).show(supportFragmentManager, "")
                }
            }
        }
    }

    companion object {
        @SuppressLint("SdCardPath")
        val appRootDir = File("/data/data/com.micewine.emu/files")
        var ratPackagesDir = File("$appRootDir/packages")
        var appBuiltinRootfs: Boolean = false
        var deviceArch = Build.SUPPORTED_ABIS[0].replace("arm64-v8a", "aarch64")
        val unixUsername = runCommandWithOutput("whoami").replace("\n", "")
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
        var box64ShowSegv: Boolean = false
        var box64ShowBt: Boolean = false
        var box64NoSigSegv: Boolean = false
        var box64NoSigill: Boolean = false
        var wineLogLevel: String? = null
        var selectedBox64: String? = null
        var selectedD3DXRenderer: String? = null
        var selectedWineD3D: String? = null
        var selectedDXVK: String? = null
        var selectedVKD3D: String? = null
        var selectedGLProfile: String? = null
        var selectedDXVKHud: String? = null
        var selectedMesaVkWsiPresentMode: String? = null
        var selectedTuDebugPreset: String? = null
        var selectedFragmentId = 0
        var memoryStats = "??/??"
        var totalCpuUsage = "???%"
        var winePrefixesDir: File = File("$appRootDir/winePrefixes")
        var wineDisksFolder: File? = null
        var winePrefix: String? = null
        var wineESync: Boolean = false
        var wineServices: Boolean = false
        var selectedCpuAffinity: String? = null
        var enableWineVirtualDesktop: Boolean = false
        var selectedWine: String? = null
        var fileManagerDefaultDir: String = ""
        var fileManagerCwd: String? = null
        var selectedFile: String = ""
        var miceWineVersion: String = "MiceWine ${BuildConfig.VERSION_NAME}" + if (BuildConfig.DEBUG) " (git-${BuildConfig.GIT_SHORT_SHA})" else ""
        var vulkanDriverDeviceName: String? = null
        var vulkanDriverDriverVersion: String? = null
        var screenFpsLimit: Int = 60
        var fpsLimit: Int = 0
        var paSink: String? = null
        var selectedResolution: String? = null
        var useAdrenoTools: Boolean = false
        var adrenoToolsDriverFile: File? = null
        var preferences: SharedPreferences? = null

        const val ACTION_RUN_WINE = "com.micewine.emu.ACTION_RUN_WINE"
        const val ACTION_SETUP = "com.micewine.emu.ACTION_SETUP"
        const val ACTION_INSTALL_RAT = "com.micewine.emu.ACTION_INSTALL_RAT"
        const val ACTION_INSTALL_ADTOOLS_DRIVER = "com.micewine.emu.ACTION_INSTALL_ADTOOLS_DRIVER"
        const val ACTION_STOP_ALL = "com.micewine.emu.ACTION_STOP_ALL"
        const val ACTION_SELECT_FILE_MANAGER = "com.micewine.emu.ACTION_SELECT_FILE_MANAGER"
        const val ACTION_SELECT_ICON = "com.micewine.emu.ACTION_SELECT_ICON"
        const val ACTION_SELECT_EXE_PATH = "com.micewine.emu.ACTION_SELECT_EXE_PATH"
        const val ACTION_CREATE_WINE_PREFIX = "com.micewine.emu.ACTION_CREATE_WINE_PREFIX"
        const val RAM_COUNTER = "ramCounter"
        const val RAM_COUNTER_DEFAULT_VALUE = true
        const val CPU_COUNTER = "cpuCounter"
        const val CPU_COUNTER_DEFAULT_VALUE = false
        const val ENABLE_DEBUG_INFO = "debugInfo"
        const val ENABLE_DEBUG_INFO_DEFAULT_VALUE = true
        const val APP_VERSION = "appVersion"
        private const val ADRENOTOOLS_LD_PRELOAD = "adrenoToolsLdPreload"

        private fun strBoolToNumStr(strBool: String): String {
            return strBoolToNumStr(strBool.toBoolean())
        }

        fun strBoolToNumStr(strBool: Boolean): String {
            return if (strBool) "1" else "0"
        }

        @Suppress("DEPRECATION")
        fun setSharedVars(
            activity: Activity,
            box64Version: String? = null,
            box64Preset: String? = null,
            d3dxRenderer: String? = null,
            wineD3D: String? = null,
            dxvk: String? = null,
            vkd3d: String? = null,
            displayResolution: String? = null,
            esync: Boolean? = null,
            services: Boolean? = null,
            virtualDesktop: Boolean? = null,
            cpuAffinity: String? = null,
            adrenoToolsDriverPath: String? = null
        ) {
            useAdrenoTools = adrenoToolsDriverPath != null
            adrenoToolsDriverFile = adrenoToolsDriverPath?.let { File(it) }

            appLang = activity.resources.getString(R.string.app_lang)
            appBuiltinRootfs = activity.assets.list("")?.contains("rootfs.zip")!!

            selectedBox64 = box64Version ?: getBox64Version(selectedGameName)
            box64LogLevel = preferences?.getString(BOX64_LOG, BOX64_LOG_DEFAULT_VALUE)

            box64ShowSegv = preferences?.getBoolean(BOX64_SHOWSEGV, BOX64_SHOWSEGV_DEFAULT_VALUE) ?: BOX64_SHOWSEGV_DEFAULT_VALUE
            box64ShowBt = preferences?.getBoolean(BOX64_SHOWBT, BOX64_SHOWBT_DEFAULT_VALUE) ?: BOX64_SHOWBT_DEFAULT_VALUE
            box64NoSigill = preferences?.getBoolean(BOX64_NOSIGILL, BOX64_NOSIGILL_DEFAULT_VALUE) ?: BOX64_NOSIGILL_DEFAULT_VALUE
            box64NoSigSegv = preferences?.getBoolean(BOX64_NOSIGSEGV, BOX64_NOSIGSEGV_DEFAULT_VALUE) ?: BOX64_NOSIGSEGV_DEFAULT_VALUE

            setBox64Preset(box64Preset)

            enableDRI3 = preferences?.getBoolean(ENABLE_DRI3, ENABLE_DRI3_DEFAULT_VALUE) ?: ENABLE_DRI3_DEFAULT_VALUE
            enableMangoHUD = preferences?.getBoolean(ENABLE_MANGOHUD, ENABLE_MANGOHUD_DEFAULT_VALUE) ?: ENABLE_MANGOHUD_DEFAULT_VALUE
            wineLogLevel = preferences?.getString(WINE_LOG_LEVEL, WINE_LOG_LEVEL_DEFAULT_VALUE)

            selectedD3DXRenderer = d3dxRenderer ?: getD3DXRenderer(selectedGameName)
            selectedWineD3D = wineD3D ?: getWineD3DVersion(selectedGameName)
            selectedDXVK = dxvk ?: getDXVKVersion(selectedGameName)
            selectedVKD3D = vkd3d ?: getVKD3DVersion(selectedGameName)

            selectedResolution = displayResolution ?: getDisplaySettings(selectedGameName)[1]
            wineESync = esync ?: getWineESync(selectedGameName)
            wineServices = services ?: getWineServices(selectedGameName)
            enableWineVirtualDesktop = virtualDesktop ?: getWineVirtualDesktop(selectedGameName)
            selectedCpuAffinity = cpuAffinity ?: getCpuAffinity(selectedGameName)

            selectedGLProfile = preferences?.getString(SELECTED_GL_PROFILE, SELECTED_GL_PROFILE_DEFAULT_VALUE)
            selectedDXVKHud = preferences?.getString(SELECTED_DXVK_HUD_PRESET, SELECTED_DXVK_HUD_PRESET_DEFAULT_VALUE)
            selectedMesaVkWsiPresentMode = preferences?.getString(SELECTED_MESA_VK_WSI_PRESENT_MODE, SELECTED_MESA_VK_WSI_PRESENT_MODE_DEFAULT_VALUE)
            selectedTuDebugPreset = preferences?.getString(SELECTED_TU_DEBUG_PRESET, SELECTED_TU_DEBUG_PRESET_DEFAULT_VALUE)

            enableRamCounter = preferences?.getBoolean(RAM_COUNTER, RAM_COUNTER_DEFAULT_VALUE) ?: CPU_COUNTER_DEFAULT_VALUE
            enableCpuCounter = preferences?.getBoolean(CPU_COUNTER, CPU_COUNTER_DEFAULT_VALUE) ?: CPU_COUNTER_DEFAULT_VALUE
            enableDebugInfo = preferences?.getBoolean(ENABLE_DEBUG_INFO, ENABLE_DEBUG_INFO_DEFAULT_VALUE) ?: ENABLE_DEBUG_INFO_DEFAULT_VALUE

            screenFpsLimit = (activity.getSystemService(WINDOW_SERVICE) as WindowManager).defaultDisplay.refreshRate.toInt()
            fpsLimit = preferences?.getInt(FPS_LIMIT, screenFpsLimit) ?: screenFpsLimit

            vulkanDriverDeviceName = getVulkanDriverInfo("deviceName") + if (useAdrenoTools) " (AdrenoTools)" else ""
            vulkanDriverDriverVersion = getVulkanDriverInfo("driverVersion").split(" ")[0]

            winePrefix = getSelectedWinePrefix()
            wineDisksFolder = File("$winePrefixesDir/$winePrefix/dosdevices/")

            val winePrefixConfigFile = File("$winePrefixesDir/$winePrefix/config")
            if (winePrefixConfigFile.exists()) {
                selectedWine = winePrefixConfigFile.readLines()[0]
            }

            fileManagerDefaultDir = wineDisksFolder!!.path

            paSink = preferences?.getString(PA_SINK, PA_SINK_DEFAULT_VALUE)?.toLowerCase(Locale.getDefault())
        }

        private fun setBox64Preset(name: String?) {
            var selectedBox64Preset = name ?: preferences?.getString(SELECTED_BOX64_PRESET, "default") ?: "default"
            if (name == "--") selectedBox64Preset = preferences?.getString(SELECTED_BOX64_PRESET, "default") ?: "default"

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

        private fun getVulkanDriverInfo(info: String, stdErr: Boolean = false): String {
            return runCommandWithOutput("echo $(${getEnv()} DISPLAY= vulkaninfo | grep $info | cut -d '=' -f 2)", stdErr)
        }

        private val driverWorkaroundLdPreload = StringBuilder()
        private var findingLdPreloadWorkaround = false

        private fun locateLibraryBySymbol(symbol: String): String {
            return runBlocking {
                val libFiles = File("/system/lib64").listFiles()?.filter { it.isFile && it.extension == "so" } ?: return@runBlocking ""
                libFiles.forEach {
                    val readElf = runCommandWithOutput("echo $(readelf --dyn-syms $it | grep $symbol)")
                    if (readElf.contains(symbol)) {
                        val implementsSymbol = !readElf.contains("FUNC GLOBAL DEFAULT UND")
                        if (implementsSymbol) return@runBlocking "${it.name}:"
                    }
                }
                ""
            }
        }

        fun getLdPreloadWorkaround(): String {
            if (findingLdPreloadWorkaround) {
                return "LD_PRELOAD=$driverWorkaroundLdPreload"
            }

            val savedLdPreload = preferences?.getString(ADRENOTOOLS_LD_PRELOAD, "")
            if (!savedLdPreload.isNullOrEmpty()) {
                return "LD_PRELOAD=$savedLdPreload"
            }

            driverWorkaroundLdPreload.clear()

            findingLdPreloadWorkaround = true

            while (true) {
                val res = getVulkanDriverInfo("", true)
                if (res.contains("cannot locate symbol")) {
                    val symbolName = res.split("\"")[1]
                    driverWorkaroundLdPreload.append(locateLibraryBySymbol(symbolName))
                } else if (res.contains("cannot find")) {
                    val libName = res.split("\"")[1]
                    driverWorkaroundLdPreload.append("/system/lib64/$libName:")
                } else {
                    break
                }
            }

            findingLdPreloadWorkaround = false

            preferences?.edit()?.apply {
                putString(ADRENOTOOLS_LD_PRELOAD, "$driverWorkaroundLdPreload")
                apply()
            }

            return "LD_PRELOAD=$driverWorkaroundLdPreload"
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

        val resolutions16_9 = arrayOf(
            "640x360", "854x480",
            "960x540", "1280x720",
            "1366x768", "1600x900",
            "1920x1080", "2560x1440",
            "3840x2160", "7680x4320"
        )

        val resolutions4_3 = arrayOf(
            "640x480", "800x600",
            "1024x768", "1280x960",
            "1400x1050", "1600x1200"
        )

        @Suppress("DEPRECATION")
        fun getNativeResolution(context: Context): String {
            val windowManager = context.getSystemService(WindowManager::class.java)
            val displayMetrics = DisplayMetrics()
            windowManager.defaultDisplay.getRealMetrics(displayMetrics)

            return if (displayMetrics.widthPixels > displayMetrics.heightPixels) {
                "${displayMetrics.widthPixels}x${displayMetrics.heightPixels}"
            } else {
                "${displayMetrics.heightPixels}x${displayMetrics.widthPixels}"
            }
        }

        private fun getPercentOfResolution(original: String, percent: Int): String {
            val resolution = original.split("x")
            val width = resolution[0].toInt() * percent / 100
            val height = resolution[1].toInt() * percent / 100

            return "${width}x${height}"
        }

        fun getNativeResolutions(activity: Activity): List<String> {
            val parsedResolutions = mutableListOf<String>()
            val nativeResolution = getNativeResolution(activity)

            parsedResolutions.add(nativeResolution)
            parsedResolutions.add(getPercentOfResolution(nativeResolution, 90))
            parsedResolutions.add(getPercentOfResolution(nativeResolution, 80))
            parsedResolutions.add(getPercentOfResolution(nativeResolution, 70))
            parsedResolutions.add(getPercentOfResolution(nativeResolution, 60))
            parsedResolutions.add(getPercentOfResolution(nativeResolution, 50))
            parsedResolutions.add(getPercentOfResolution(nativeResolution, 40))
            parsedResolutions.add(getPercentOfResolution(nativeResolution, 30))

            return parsedResolutions
        }

        const val EXPORT_LNK_ACTION = 1
    }
}
