package com.micewine.emu.activities;

import static com.micewine.emu.activities.GeneralSettingsActivity.BOX64_LOG;
import static com.micewine.emu.activities.GeneralSettingsActivity.BOX64_LOG_DEFAULT_VALUE;
import static com.micewine.emu.activities.GeneralSettingsActivity.BOX64_NOSIGILL;
import static com.micewine.emu.activities.GeneralSettingsActivity.BOX64_NOSIGILL_DEFAULT_VALUE;
import static com.micewine.emu.activities.GeneralSettingsActivity.BOX64_NOSIGSEGV;
import static com.micewine.emu.activities.GeneralSettingsActivity.BOX64_NOSIGSEGV_DEFAULT_VALUE;
import static com.micewine.emu.activities.GeneralSettingsActivity.BOX64_SHOWBT;
import static com.micewine.emu.activities.GeneralSettingsActivity.BOX64_SHOWBT_DEFAULT_VALUE;
import static com.micewine.emu.activities.GeneralSettingsActivity.BOX64_SHOWSEGV;
import static com.micewine.emu.activities.GeneralSettingsActivity.BOX64_SHOWSEGV_DEFAULT_VALUE;
import static com.micewine.emu.activities.GeneralSettingsActivity.ENABLE_DRI3;
import static com.micewine.emu.activities.GeneralSettingsActivity.ENABLE_DRI3_DEFAULT_VALUE;
import static com.micewine.emu.activities.GeneralSettingsActivity.ENABLE_MANGOHUD;
import static com.micewine.emu.activities.GeneralSettingsActivity.ENABLE_MANGOHUD_DEFAULT_VALUE;
import static com.micewine.emu.activities.GeneralSettingsActivity.FPS_LIMIT;
import static com.micewine.emu.activities.GeneralSettingsActivity.PA_SINK;
import static com.micewine.emu.activities.GeneralSettingsActivity.PA_SINK_DEFAULT_VALUE;
import static com.micewine.emu.activities.GeneralSettingsActivity.SELECTED_BOX64;
import static com.micewine.emu.activities.GeneralSettingsActivity.SELECTED_DXVK_HUD_PRESET;
import static com.micewine.emu.activities.GeneralSettingsActivity.SELECTED_DXVK_HUD_PRESET_DEFAULT_VALUE;
import static com.micewine.emu.activities.GeneralSettingsActivity.SELECTED_GL_PROFILE;
import static com.micewine.emu.activities.GeneralSettingsActivity.SELECTED_GL_PROFILE_DEFAULT_VALUE;
import static com.micewine.emu.activities.GeneralSettingsActivity.SELECTED_MESA_VK_WSI_PRESENT_MODE;
import static com.micewine.emu.activities.GeneralSettingsActivity.SELECTED_MESA_VK_WSI_PRESENT_MODE_DEFAULT_VALUE;
import static com.micewine.emu.activities.GeneralSettingsActivity.SELECTED_TU_DEBUG_PRESET;
import static com.micewine.emu.activities.GeneralSettingsActivity.SELECTED_TU_DEBUG_PRESET_DEFAULT_VALUE;
import static com.micewine.emu.activities.GeneralSettingsActivity.SELECTED_VULKAN_DRIVER;
import static com.micewine.emu.activities.GeneralSettingsActivity.WINE_DPI;
import static com.micewine.emu.activities.GeneralSettingsActivity.WINE_DPI_APPLIED;
import static com.micewine.emu.activities.GeneralSettingsActivity.WINE_DPI_APPLIED_DEFAULT_VALUE;
import static com.micewine.emu.activities.GeneralSettingsActivity.WINE_DPI_DEFAULT_VALUE;
import static com.micewine.emu.activities.GeneralSettingsActivity.WINE_LOG_LEVEL;
import static com.micewine.emu.activities.GeneralSettingsActivity.WINE_LOG_LEVEL_DEFAULT_VALUE;
import static com.micewine.emu.activities.PresetManagerActivity.SELECTED_BOX64_PRESET;
import static com.micewine.emu.activities.RatManagerActivity.generateICDFile;
import static com.micewine.emu.activities.RatManagerActivity.generateMangoHUDConfFile;
import static com.micewine.emu.activities.WelcomeActivity.finishedWelcomeScreen;
import static com.micewine.emu.adapters.AdapterGame.selectedGameName;
import static com.micewine.emu.controller.ControllerUtils.connectedPhysicalControllers;
import static com.micewine.emu.controller.ControllerUtils.disconnectController;
import static com.micewine.emu.controller.ControllerUtils.prepareControllersMappings;
import static com.micewine.emu.core.EnvVars.getEnv;
import static com.micewine.emu.core.RatPackageManager.checkPackageInstalled;
import static com.micewine.emu.core.RatPackageManager.getPackageById;
import static com.micewine.emu.core.RatPackageManager.installADToolsDriver;
import static com.micewine.emu.core.RatPackageManager.installRat;
import static com.micewine.emu.core.RatPackageManager.installablePackagesCategories;
import static com.micewine.emu.core.RatPackageManager.listRatPackages;
import static com.micewine.emu.core.ShellLoader.runCommand;
import static com.micewine.emu.core.ShellLoader.runCommandWithOutput;
import static com.micewine.emu.core.WineWrapper.getCpuHexMask;
import static com.micewine.emu.core.WineWrapper.getSanitizedPath;
import static com.micewine.emu.fragments.AskInstallPackageFragment.ADTOOLS_DRIVER_PACKAGE;
import static com.micewine.emu.fragments.AskInstallPackageFragment.MWP_PRESET_PACKAGE;
import static com.micewine.emu.fragments.AskInstallPackageFragment.RAT_PACKAGE;
import static com.micewine.emu.fragments.AskInstallPackageFragment.adToolsDriverCandidate;
import static com.micewine.emu.fragments.AskInstallPackageFragment.mwpPresetCandidate;
import static com.micewine.emu.fragments.AskInstallPackageFragment.ratCandidate;
import static com.micewine.emu.fragments.Box64PresetManagerFragment.getBox64AlignedAtomics;
import static com.micewine.emu.fragments.Box64PresetManagerFragment.getBox64Avx;
import static com.micewine.emu.fragments.Box64PresetManagerFragment.getBox64BigBlock;
import static com.micewine.emu.fragments.Box64PresetManagerFragment.getBox64CallRet;
import static com.micewine.emu.fragments.Box64PresetManagerFragment.getBox64DF;
import static com.micewine.emu.fragments.Box64PresetManagerFragment.getBox64Dirty;
import static com.micewine.emu.fragments.Box64PresetManagerFragment.getBox64FastNan;
import static com.micewine.emu.fragments.Box64PresetManagerFragment.getBox64FastRound;
import static com.micewine.emu.fragments.Box64PresetManagerFragment.getBox64Forward;
import static com.micewine.emu.fragments.Box64PresetManagerFragment.getBox64MMap32;
import static com.micewine.emu.fragments.Box64PresetManagerFragment.getBox64NativeFlags;
import static com.micewine.emu.fragments.Box64PresetManagerFragment.getBox64Pause;
import static com.micewine.emu.fragments.Box64PresetManagerFragment.getBox64SafeFlags;
import static com.micewine.emu.fragments.Box64PresetManagerFragment.getBox64Sse42;
import static com.micewine.emu.fragments.Box64PresetManagerFragment.getBox64StrongMem;
import static com.micewine.emu.fragments.Box64PresetManagerFragment.getBox64Wait;
import static com.micewine.emu.fragments.Box64PresetManagerFragment.getBox64WeakBarrier;
import static com.micewine.emu.fragments.Box64PresetManagerFragment.getBox64X87Double;
import static com.micewine.emu.fragments.ControllerSettingsFragment.ACTION_UPDATE_CONTROLLERS_STATUS;
import static com.micewine.emu.fragments.CreatePresetFragment.BOX64_PRESET;
import static com.micewine.emu.fragments.CreatePresetFragment.CONTROLLER_PRESET;
import static com.micewine.emu.fragments.CreatePresetFragment.VIRTUAL_CONTROLLER_PRESET;
import static com.micewine.emu.fragments.DebugSettingsFragment.availableCPUs;
import static com.micewine.emu.fragments.EditGamePreferencesFragment.FILE_MANAGER_START_PREFERENCES;
import static com.micewine.emu.fragments.FileManagerFragment.refreshFiles;
import static com.micewine.emu.fragments.FloatingFileManagerFragment.OPERATION_SELECT_EXE;
import static com.micewine.emu.fragments.FloatingFileManagerFragment.OPERATION_SELECT_ICON;
import static com.micewine.emu.fragments.FloatingFileManagerFragment.OPERATION_SELECT_RAT;
import static com.micewine.emu.fragments.SetupFragment.abortSetup;
import static com.micewine.emu.fragments.ShortcutsFragment.ADRENO_TOOLS_DRIVER;
import static com.micewine.emu.fragments.ShortcutsFragment.MESA_DRIVER;
import static com.micewine.emu.fragments.ShortcutsFragment.addGameToList;
import static com.micewine.emu.fragments.ShortcutsFragment.getBox64Preset;
import static com.micewine.emu.fragments.ShortcutsFragment.getBox64Version;
import static com.micewine.emu.fragments.ShortcutsFragment.getCpuAffinity;
import static com.micewine.emu.fragments.ShortcutsFragment.getD3DXRenderer;
import static com.micewine.emu.fragments.ShortcutsFragment.getDXVKVersion;
import static com.micewine.emu.fragments.ShortcutsFragment.getDisplaySettings;
import static com.micewine.emu.fragments.ShortcutsFragment.getEnableDInput;
import static com.micewine.emu.fragments.ShortcutsFragment.getEnableXInput;
import static com.micewine.emu.fragments.ShortcutsFragment.getExeArguments;
import static com.micewine.emu.fragments.ShortcutsFragment.getExePath;
import static com.micewine.emu.fragments.ShortcutsFragment.getSelectedVirtualControllerPreset;
import static com.micewine.emu.fragments.ShortcutsFragment.getVKD3DVersion;
import static com.micewine.emu.fragments.ShortcutsFragment.getVulkanDriver;
import static com.micewine.emu.fragments.ShortcutsFragment.getVulkanDriverType;
import static com.micewine.emu.fragments.ShortcutsFragment.getWineD3DVersion;
import static com.micewine.emu.fragments.ShortcutsFragment.getWineESync;
import static com.micewine.emu.fragments.ShortcutsFragment.getWineServices;
import static com.micewine.emu.fragments.ShortcutsFragment.getWineVirtualDesktop;
import static com.micewine.emu.fragments.ShortcutsFragment.setIconToGame;
import static com.micewine.emu.fragments.ShortcutsFragment.updateShortcuts;
import static com.micewine.emu.fragments.SoundSettingsFragment.generatePAFile;
import static com.micewine.emu.fragments.WinePrefixManagerFragment.createWinePrefix;
import static com.micewine.emu.fragments.WinePrefixManagerFragment.getSelectedWinePrefix;
import static com.micewine.emu.fragments.SetupFragment.progressBarIsIndeterminate;
import static com.micewine.emu.fragments.SetupFragment.dialogTitleText;
import static com.micewine.emu.fragments.WinePrefixManagerFragment.getWinePrefixFile;
import static com.micewine.emu.utils.FileUtils.copyRecursively;
import static com.micewine.emu.utils.FileUtils.deleteDirectoryRecursively;
import static com.micewine.emu.utils.FileUtils.getFileExtension;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.hardware.input.InputManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;
import android.util.DisplayMetrics;
import android.view.InputDevice;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.gson.Gson;
import com.micewine.emu.BuildConfig;
import com.micewine.emu.R;
import com.micewine.emu.adapters.AdapterBottomNavigation;
import com.micewine.emu.controller.ControllerUtils;
import com.micewine.emu.core.RatPackageManager;
import com.micewine.emu.core.WineWrapper;
import com.micewine.emu.databinding.ActivityMainBinding;
import com.micewine.emu.fragments.AskInstallPackageFragment;
import com.micewine.emu.fragments.Box64PresetManagerFragment;
import com.micewine.emu.fragments.ControllerPresetManagerFragment;
import com.micewine.emu.fragments.EditGamePreferencesFragment;
import com.micewine.emu.fragments.FloatingFileManagerFragment;
import com.micewine.emu.fragments.SetupFragment;
import com.micewine.emu.fragments.ShortcutsFragment;
import com.micewine.emu.fragments.VirtualControllerPresetManagerFragment;
import com.micewine.emu.utils.DriveUtils;
import com.micewine.emu.utils.FilePathResolver;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import io.ByteWriter;
import mslinks.LinkTargetIDList;
import mslinks.ShellLink;
import mslinks.ShellLinkException;
import mslinks.data.ItemID;
import mslinks.data.VolumeID;

public class MainActivity extends AppCompatActivity {
    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction() == null) return;
            switch (intent.getAction()) {
                case ACTION_RUN_WINE -> {
                    String exePath = intent.getStringExtra("exePath");
                    String exeArguments = intent.getStringExtra("exeArguments");
                    String driverName = intent.getStringExtra("driverName");
                    int driverType = intent.getIntExtra("driverType", MESA_DRIVER);
                    String box64Version = intent.getStringExtra("box64Version");
                    String box64Preset = intent.getStringExtra("box64Preset");
                    String displayResolution = intent.getStringExtra("displayResolution");
                    String d3dxRenderer = intent.getStringExtra("d3dxRenderer");
                    String wineD3D = intent.getStringExtra("wineD3D");
                    String dxvk = intent.getStringExtra("dxvk");
                    String vkd3d = intent.getStringExtra("vkd3d");
                    boolean esync = intent.getBooleanExtra("esync", true);
                    boolean services = intent.getBooleanExtra("services", false);
                    boolean virtualDesktop = intent.getBooleanExtra("virtualDesktop", false);
                    boolean enableXInput = intent.getBooleanExtra("enableXInput", true);
                    boolean enableDInput = intent.getBooleanExtra("enableDInput", true);
                    String cpuAffinity = intent.getStringExtra("cpuAffinity");

                    if (exeArguments == null) exeArguments = "";
                    if (driverName == null) driverName = "Global";
                    if (box64Version == null) box64Version = "Global";
                    if (box64Preset == null) box64Preset = "default";
                    if (displayResolution == null) displayResolution = "1280x720";
                    if (d3dxRenderer == null) d3dxRenderer = "DXVK";
                    if (wineD3D == null) wineD3D = listRatPackages("WineD3D").get(0).getFolderName();
                    if (dxvk == null) dxvk = listRatPackages("DXVK").get(0).getFolderName();
                    if (vkd3d == null) vkd3d = listRatPackages("VKD3D").get(0).getFolderName();
                    if (cpuAffinity == null) cpuAffinity = String.join(",", availableCPUs);

                    deleteDirectoryRecursively(tmpDir.toPath());
                    tmpDir.mkdirs();

                    if (driverName.equals("Global")) {
                        driverName = preferences.getString(SELECTED_VULKAN_DRIVER, "");
                        driverType = getVulkanDriverType(driverName);
                    }
                    if (box64Version.equals("Global")) {
                        box64Version = preferences.getString(SELECTED_BOX64, "");
                    }

                    String driverLibPath;
                    String adrenoToolsDriverPath = null;

                    if (driverType == MESA_DRIVER) {
                        driverLibPath = getPackageById(driverName).getDriverLib();
                    } else if (driverType == ADRENO_TOOLS_DRIVER) {
                        driverLibPath = listRatPackages("AdrenoTools").get(0).getDriverLib();
                        adrenoToolsDriverPath = getPackageById(driverName).getDriverLib();
                    } else {
                        driverLibPath = "";
                    }

                    generateICDFile(driverLibPath);
                    generateMangoHUDConfFile();
                    generatePAFile();

                    setSharedVars(
                            MainActivity.this,
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
                            enableXInput,
                            enableDInput,
                            cpuAffinity,
                            adrenoToolsDriverPath
                    );

                    runXServer();

                    String finalExeArguments = exeArguments;
                    new Thread(() -> runWine(exePath, finalExeArguments)).start();
                }
                case ACTION_SELECT_FILE_MANAGER -> {
                    String fileName = intent.getStringExtra("selectedFile");
                    if (fileName == null) return;

                    if (fileName.equals("..")) {
                        fileManagerCwd = new File(fileManagerCwd).getParent();
                        refreshFiles(MainActivity.this);
                        return;
                    }

                    File file = new File(fileName);
                    if (file.isFile()) {
                        String fileExtension = getFileExtension(file).toLowerCase();
                        switch (fileExtension) {
                            case "exe", "bat", "msi", "lnk" -> {
                                if (fileExtension.equals("lnk")) {
                                    try {
                                        DriveUtils.DriveInfo drive = DriveUtils.parseWindowsPath(new ShellLink(file).resolveTarget());

                                        if (drive != null) {
                                            file = new File(drive.getUnixPath());
                                        }
                                    } catch (IOException ignored) {
                                    } catch (ShellLinkException e) {
                                        Toast.makeText(MainActivity.this, getString(R.string.lnk_read_fail), Toast.LENGTH_SHORT).show();
                                    }
                                }

                                new EditGamePreferencesFragment(FILE_MANAGER_START_PREFERENCES, file).show(getSupportFragmentManager(), "");
                            }
                            case "rat" -> {
                                ratCandidate = new RatPackageManager.RatPackage(file.getPath());

                                if (ratCandidate.getName() != null) {
                                    new AskInstallPackageFragment(RAT_PACKAGE).show(getSupportFragmentManager(), "");
                                }
                            }
                            case "zip" -> {
                                adToolsDriverCandidate = new RatPackageManager.AdrenoToolsPackage(file.getPath());

                                if (adToolsDriverCandidate.getName() != null) {
                                    new AskInstallPackageFragment(ADTOOLS_DRIVER_PACKAGE).show(getSupportFragmentManager(), "");
                                }
                            }
                            case "mwp" -> {
                                List<String> mwpLines;

                                try {
                                    mwpLines = Files.readAllLines(file.toPath());
                                } catch (IOException ignored) {
                                    mwpLines = null;
                                }

                                if (mwpLines != null && !mwpLines.isEmpty()) {
                                    switch (mwpLines.get(0)) {
                                        case "controllerPreset" -> mwpPresetCandidate = new AskInstallPackageFragment.MwpPreset(CONTROLLER_PRESET, file);
                                        case "virtualControllerPreset" -> mwpPresetCandidate = new AskInstallPackageFragment.MwpPreset(VIRTUAL_CONTROLLER_PRESET, file);
                                        case "box64Preset", "box64PresetV2" -> mwpPresetCandidate = new AskInstallPackageFragment.MwpPreset(BOX64_PRESET, file);
                                    }

                                    new AskInstallPackageFragment(MWP_PRESET_PACKAGE).show(getSupportFragmentManager(), "");
                                }
                            }
                        }
                    } else if (file.isDirectory()) {
                        fileManagerCwd = file.getPath();
                        refreshFiles(MainActivity.this);
                    }
                }
                case ACTION_SETUP -> new Thread(() -> {
                    String rootFSPath = "";

                    if (!appBuiltinRootfs) {
                        new SetupFragment().show(getSupportFragmentManager(), "");
                        rootFSPath = customRootFSPath;
                    }

                    setupMiceWine(rootFSPath);
                }).start();
                case ACTION_INSTALL_RAT -> {
                    if (!(ratCandidate.getArchitecture().equals(deviceArch) || ratCandidate.getArchitecture().equals("any")) && !ratCandidate.getCategory().equals("Wine")) {
                        Toast.makeText(context, R.string.invalid_architecture_rat_file, Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (ratCandidate.getCategory().equals("rootfs")) {
                        Toast.makeText(context, R.string.error_install_rootfs_file_manager, Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (!installablePackagesCategories.contains(ratCandidate.getCategory())) {
                        Toast.makeText(context, R.string.unknown_package_category, Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (checkPackageInstalled(ratCandidate.getName(), ratCandidate.getCategory(), ratCandidate.getVersion())) {
                        Toast.makeText(context, R.string.package_already_installed, Toast.LENGTH_SHORT).show();
                        return;
                    }

                    new Thread(() -> {
                        setupDone = false;

                        new SetupFragment().show(getSupportFragmentManager(), "");

                        dialogTitleText = "Installing " + ratCandidate.getName() + " (" + ratCandidate.getVersion() + ")...";
                        progressBarIsIndeterminate = true;

                        installRat(ratCandidate, context);

                        setupDone = true;
                    }).start();
                }
                case ACTION_INSTALL_ADTOOLS_DRIVER -> {
                    boolean isPackageInstalled = checkPackageInstalled(adToolsDriverCandidate.getName() + " (AdrenoTools)", "AdrenoToolsDriver", adToolsDriverCandidate.getVersion());

                    if (isPackageInstalled) {
                        Toast.makeText(context, R.string.package_already_installed, Toast.LENGTH_SHORT).show();
                        return;
                    }

                    new Thread(() -> {
                        setupDone = false;

                        new SetupFragment().show(getSupportFragmentManager(), "");

                        dialogTitleText = "Installing " + adToolsDriverCandidate.getName() + " (" + adToolsDriverCandidate.getVersion() + ")...";
                        progressBarIsIndeterminate = true;

                        installADToolsDriver(adToolsDriverCandidate);

                        setupDone = true;
                    }).start();
                }
                case ACTION_SELECT_ICON -> new FloatingFileManagerFragment(OPERATION_SELECT_ICON, wineDisksFolder.getPath()).show(getSupportFragmentManager(), "");
                case ACTION_SELECT_EXE_PATH -> new FloatingFileManagerFragment(OPERATION_SELECT_EXE, wineDisksFolder.getPath()).show(getSupportFragmentManager(), "");
                case ACTION_CREATE_WINE_PREFIX -> {
                    String winePrefix = intent.getStringExtra("winePrefix");
                    String wine = intent.getStringExtra("wine");

                    runXServer();

                    setupDone = false;

                    new SetupFragment().show(getSupportFragmentManager(), "");

                    dialogTitleText = getString(R.string.creating_wine_prefix);
                    progressBarIsIndeterminate = true;

                    new Thread(() -> {
                        createWinePrefix(winePrefix, wine);
                        setSharedVars(MainActivity.this);

                        fileManagerCwd = fileManagerDefaultDir;
                        setupDone = true;
                    }).start();
                }
            }
        }
    };
    private InputManager inputManager;
    private final InputManager.InputDeviceListener inputDeviceListener = new InputManager.InputDeviceListener() {
        @Override
        public void onInputDeviceAdded(int deviceId) {
            InputDevice.getDevice(deviceId);
        }

        @Override
        public void onInputDeviceChanged(int deviceId) {
            InputDevice device = InputDevice.getDevice(deviceId);
            if (device == null) return;

            if (connectedPhysicalControllers.stream().anyMatch(c -> c.id == deviceId)) return;
            if (((device.getSources() & InputDevice.SOURCE_GAMEPAD) == InputDevice.SOURCE_GAMEPAD)
                    || ((device.getSources() & InputDevice.SOURCE_JOYSTICK) == InputDevice.SOURCE_JOYSTICK)) {
                if (!device.getName().contains("uinput")) {
                    connectedPhysicalControllers.add(new ControllerUtils.PhysicalController(device.getName(), deviceId));
                    prepareControllersMappings();
                    sendBroadcast(new Intent(ACTION_UPDATE_CONTROLLERS_STATUS));
                }
            }
        }

        @Override
        public void onInputDeviceRemoved(int deviceId) {
            int index = -1;
            for (int i = 0; i < connectedPhysicalControllers.size(); i++) {
                if (connectedPhysicalControllers.get(i).id == deviceId) {
                    index = i;
                    break;
                }
            }

            if (index == -1) return;

            disconnectController(connectedPhysicalControllers.get(index).virtualControllerID);
            connectedPhysicalControllers.remove(index);
            sendBroadcast(new Intent(ACTION_UPDATE_CONTROLLERS_STATUS));
        }
    };

    private BottomNavigationView bottomNavigation;
    private ViewPager2 viewPager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        preferences = PreferenceManager.getDefaultSharedPreferences(this);

        ControllerPresetManagerFragment.initialize(false);
        VirtualControllerPresetManagerFragment.initialize(false);
        Box64PresetManagerFragment.initialize();
        ShortcutsFragment.initialize();
        ControllerUtils.initialize(this);

        inputManager = (InputManager) getSystemService(INPUT_SERVICE);
        inputManager.registerInputDeviceListener(inputDeviceListener, null);

        ActivityMainBinding binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSharedVars(this);

        // On future here will have a code for check if app is updated and do specific data conversion if needed

        SharedPreferences.Editor editor = preferences.edit();

        editor.putString(APP_VERSION, BuildConfig.VERSION_NAME);
        editor.apply();

        bottomNavigation = findViewById(R.id.bottom_navigation);
        bottomNavigation.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.nav_shortcuts) {
                selectedFragmentId = 0;
                updateShortcuts();
            } else if (item.getItemId() == R.id.nav_settings) {
                selectedFragmentId = 1;
            } else if (item.getItemId() == R.id.nav_file_manager) {
                selectedFragmentId = 2;
            } else if (item.getItemId() == R.id.nav_about) {
                selectedFragmentId = 3;
            }
            viewPager.setCurrentItem(selectedFragmentId);
            return true;
        });

        viewPager = findViewById(R.id.viewPager);
        viewPager.setAdapter(new AdapterBottomNavigation(this));
        viewPager.setUserInputEnabled(false);

        bottomNavigation.post(() -> bottomNavigation.setSelectedItemId(R.id.nav_shortcuts));

        registerReceiver(receiver, new IntentFilter() {{
            addAction(ACTION_RUN_WINE);
            addAction(ACTION_SETUP);
            addAction(ACTION_INSTALL_RAT);
            addAction(ACTION_INSTALL_ADTOOLS_DRIVER);
            addAction(ACTION_SELECT_FILE_MANAGER);
            addAction(ACTION_SELECT_ICON);
            addAction(ACTION_SELECT_EXE_PATH);
            addAction(ACTION_CREATE_WINE_PREFIX);
        }});

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (getWinePrefixFile(winePrefix).exists()) {
                WineWrapper.clearDrives();

                List<StorageVolume> storageVolumes = ((StorageManager) getSystemService(Context.STORAGE_SERVICE)).getStorageVolumes();

                for (StorageVolume volume : storageVolumes) {
                    if (volume.isRemovable()) {
                        WineWrapper.addDrive(volume.getDirectory().getPath());
                    }
                }
            }
        }

        if (savedInstanceState == null) {
            onNewIntent(getIntent());
        }
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        if (!usrDir.exists()) {
            startActivity(new Intent(this, WelcomeActivity.class));
        } else {
            setupDone = true;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        runXServer();

        updateShortcuts();

        if (!setupDone && finishedWelcomeScreen) {
            if (appBuiltinRootfs) {
                new SetupFragment().show(getSupportFragmentManager() , "");
            } else {
                new FloatingFileManagerFragment(OPERATION_SELECT_RAT, "/storage/emulated/0").show(getSupportFragmentManager(), "");
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
        inputManager.unregisterInputDeviceListener(inputDeviceListener);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (selectedFragmentId == 2) {
                if (!fileManagerCwd.equals(fileManagerDefaultDir)) {
                    fileManagerCwd = new File(fileManagerCwd).getParent();
                    refreshFiles(this);
                    return true;
                }
            }
            if (selectedFragmentId > 0) {
                bottomNavigation.setSelectedItemId(R.id.nav_shortcuts);
                return true;
            }
        }

        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == EXPORT_LNK_ACTION && resultCode == Activity.RESULT_OK) {
            if (data == null) return;
            Uri uri = data.getData();

            DriveUtils.DriveInfo driveInfo = DriveUtils.parseUnixPath(selectedFile);
            if (driveInfo == null) return;

            ShellLink shellLink = new ShellLink();

            shellLink.getHeader().getLinkFlags().setHasLinkTargetIDList();

            String target = driveInfo.getWindowsPath().replace("\\", "/");

            LinkTargetIDList idList = new LinkTargetIDList();

            String[] pathSegments = target.split("/");

            try {
                idList.add(new ItemID().setType(ItemID.TYPE_CLSID));
                idList.add(new ItemID().setType(ItemID.TYPE_DRIVE).setName(pathSegments[0]));
                for (int i = 1; i < pathSegments.length; i++) {
                    idList.add(new ItemID().setType(ItemID.TYPE_DIRECTORY).setName(pathSegments[i]));
                }
                idList.add(new ItemID().setType(ItemID.TYPE_FILE).setName(new File(driveInfo.getUnixPath()).getName()));
            } catch (ShellLinkException ignored) {
                return;
            }

            try {
                shellLink.createLinkInfo();
                shellLink.getLinkInfo().createVolumeID().setDriveType(VolumeID.DRIVE_FIXED);
                shellLink.getLinkInfo().setLocalBasePath(driveInfo.getUnixPath());
            } catch (ShellLinkException e) {
                return;
            }

            if (uri == null) return;

            try {
                OutputStream outputStream = getContentResolver().openOutputStream(uri);

                if (outputStream == null) return;

                ByteWriter byteWriter = new ByteWriter(outputStream);

                shellLink.getHeader().serialize(byteWriter);

                if (shellLink.getHeader().getLinkFlags().hasLinkTargetIDList()) {
                    idList.serialize(byteWriter);
                }
                if (shellLink.getHeader().getLinkFlags().hasLinkInfo()) {
                    shellLink.getLinkInfo().serialize(byteWriter);
                }
                if (shellLink.getHeader().getLinkFlags().hasName()) {
                    byteWriter.writeUnicodeString(new File(selectedFile).getName());
                }
                if (shellLink.getHeader().getLinkFlags().hasWorkingDir()) {
                    byteWriter.writeUnicodeString(driveInfo.getWindowsPath());
                }

                byteWriter.write4bytes(0);

                outputStream.close();
            } catch (IOException e) {
                return;
            }
        }

        setSharedVars(this);

        super.onActivityResult(requestCode, resultCode, data);
    }

    private void installDXWrapper(String winePrefixName) {
        File winePrefix = getWinePrefixFile(winePrefixName);

        File driveC = new File(winePrefix, "drive_c");
        File system32 = new File(driveC, "windows/system32");
        File syswow64 = new File(driveC, "windows/syswow64");
        File selectedDXVKDir = new File(ratPackagesDir, selectedDXVK);
        File selectedVKD3DDir = new File(ratPackagesDir, selectedVKD3D);
        File selectedWineD3DDir = new File(ratPackagesDir, selectedWineD3D);

        switch (selectedD3DXRenderer) {
            case "DXVK" -> {
                if (selectedDXVKDir.exists()) {
                    File x64Folder = new File(selectedDXVKDir, "files/x64");
                    File x32Folder = new File(selectedDXVKDir, "files/x32");

                    if (x64Folder.exists() && x32Folder.exists()) {
                        copyRecursively(x64Folder, system32);
                        copyRecursively(x32Folder, syswow64);
                    }
                }
            }
            case "WineD3D" -> {
                if (selectedWineD3DDir.exists()) {
                    File x64Folder = new File(selectedWineD3DDir, "files/x64");
                    File x32Folder = new File(selectedWineD3DDir, "files/x32");

                    if (x64Folder.exists() && x32Folder.exists()) {
                        copyRecursively(x64Folder, system32);
                        copyRecursively(x32Folder, syswow64);
                    }
                }
            }
        }

        if (selectedVKD3DDir.exists()) {
            File x64Folder = new File(selectedVKD3DDir, "files/x64");
            File x32Folder = new File(selectedVKD3DDir, "files/x32");

            if (x64Folder.exists() && x32Folder.exists()) {
                copyRecursively(x64Folder, system32);
                copyRecursively(x32Folder, syswow64);
            }
        }
    }

    private void runWine(String exePath, String exeArguments) {
        installDXWrapper(winePrefix);

        boolean changedDpi = !(preferences.getBoolean(WINE_DPI_APPLIED, WINE_DPI_APPLIED_DEFAULT_VALUE));
        if (changedDpi) {
            int newDpi = preferences.getInt(WINE_DPI, WINE_DPI_DEFAULT_VALUE);
            WineWrapper.wine("reg add HKCU\\\\Control\\\\ Panel\\\\Desktop /t REG_DWORD /v LogPixels /d " + newDpi + " /f");

            SharedPreferences.Editor editor = preferences.edit();

            editor.putBoolean(WINE_DPI_APPLIED, true);
            editor.apply();
        }

        WineWrapper.killAll();

        File skCodec = new File("/system/lib64/libskcodec.so");
        if (skCodec.exists()) {
            runCommand(getEnv() + "LD_PRELOAD=$skCodec $usrDir/bin/pulseaudio --start --exit-idle=-1", true);
        }

        if (!wineServices) {
            new Thread(() -> {
                WineWrapper.waitForProcess("window_handler.exe");
                runCommand("pkill -9 services.exe", false);
            }).start();
        }

        if (exePath.isEmpty()) {
            WineWrapper.wine("explorer /desktop=shell," + selectedResolution + " window_handler.exe " + getCpuHexMask(selectedCpuAffinity) + " TFM");
        } else {
            if (enableWineVirtualDesktop) {
                WineWrapper.wine("explorer /desktop=shell," + selectedResolution + " window_handler.exe " + getCpuHexMask(selectedCpuAffinity) + " '" + getSanitizedPath(exePath) + "' " + exeArguments, "'" + getSanitizedPath(Objects.requireNonNull(new File(exePath).getParent())) + "'");
            } else {
                new Thread(() -> WineWrapper.wine("start /unix C:\\\\windows\\\\window_handler.exe " + getCpuHexMask(selectedCpuAffinity))).start();

                if (exePath.endsWith(".lnk")) {
                    try {
                        DriveUtils.DriveInfo drive = DriveUtils.parseWindowsPath(new ShellLink(exePath).resolveTarget());
                        if (drive != null) {
                            Toast.makeText(this, getString(R.string.lnk_read_fail), Toast.LENGTH_SHORT).show();
                            return;
                        }
                    } catch (IOException | ShellLinkException ignored) {
                    }
                } else {
                    WineWrapper.wine("'" + getSanitizedPath(exePath) + "' " + exeArguments, "'" + getSanitizedPath(new File(exePath).getParent()) + "'");
                }
            }
        }

        WineWrapper.killAll();

        runOnUiThread(() -> Toast.makeText(this, getString(R.string.wine_is_closed), Toast.LENGTH_SHORT).show());
    }

    private boolean runningXServer = false;

    private void runXServer() {
        if (runningXServer || !setupDone) return;

        runningXServer = true;

        new Thread(() -> runCommand(
                "env CLASSPATH=" + getClassPath() + " /system/bin/app_process / com.micewine.emu.CmdEntryPoint :0 &> /dev/null", true
        )).start();
    }

    private String getClassPath() {
        return new File(getLibsPath()).getParentFile().getParentFile().getAbsolutePath() + "/base.apk";
    }

    private String getLibsPath() {
        return getApplicationInfo().nativeLibraryDir;
    }

    private void setupMiceWine(String rootFs) {
        appRootDir.mkdirs();
        ratPackagesDir.mkdirs();

        progressBarIsIndeterminate = true;

        RatPackageManager.RatPackage ratFile;

        if (appBuiltinRootfs && rootFs.isEmpty()) {
            dialogTitleText = getString(R.string.extracting_from_assets);

            copyAssets(this, "rootfs.rat", appRootDir.getPath());

            dialogTitleText = getString(R.string.checking_rat_type);

            ratFile = new RatPackageManager.RatPackage(appRootDir.getPath() + "/rootfs.rat");
        } else {
            dialogTitleText = getString(R.string.checking_rat_type);

            ratFile = new RatPackageManager.RatPackage(customRootFSPath);
        }

        if (!ratFile.getCategory().equals("rootfs")) {
            abortSetup = true;

            runOnUiThread(() -> Toast.makeText(this, R.string.invalid_rootfs_rat_file, Toast.LENGTH_SHORT).show());

            new FloatingFileManagerFragment(OPERATION_SELECT_RAT, "/storage/emulated/0").show(getSupportFragmentManager(), "");

            return;
        }

        if (!ratFile.getArchitecture().equals(deviceArch)) {
            abortSetup = true;

            runOnUiThread(() -> Toast.makeText(this, R.string.invalid_architecture_rat_file, Toast.LENGTH_SHORT).show());

            new FloatingFileManagerFragment(OPERATION_SELECT_RAT, "/storage/emulated/0").show(getSupportFragmentManager(), "");

            return;
        }

        dialogTitleText = getString(R.string.extracting_resources_text);

        installRat(ratFile, this);

        if (appBuiltinRootfs && rootFs.isEmpty()) {
            new File(appRootDir.getPath() + "/rootfs.rat").delete();
        }

        tmpDir.mkdirs();
        homeDir.mkdirs();
        winePrefixesDir.mkdirs();

        runCommand("chmod 700 -R " + appRootDir.getPath(), false);

        new File(usrDir.getPath() + "/icons").mkdirs();

        addGameToList(getString(R.string.desktop_mode_init), getString(R.string.desktop_mode_init), "");

        dialogTitleText = getString(R.string.creating_wine_prefix);
        progressBarIsIndeterminate = true;

        runXServer();

        setSharedVars(this);

        String wine = listRatPackages("Wine").get(0).getFolderName();
        Intent createWinePrefixIntent = new Intent(ACTION_CREATE_WINE_PREFIX);

        createWinePrefixIntent.putExtra("wine", wine);
        createWinePrefixIntent.putExtra("winePrefix", winePrefix);

        sendBroadcast(createWinePrefixIntent);
    }

    @Override
    protected void onNewIntent(@NonNull Intent intent) {
        super.onNewIntent(intent);

        String shortcutName = intent.getStringExtra("shortcutName");
        if (shortcutName != null) {
            selectedGameName = shortcutName;

            String driverName = getVulkanDriver(selectedGameName);
            if (driverName.equals("Global")) {
                driverName = (preferences != null ? preferences.getString(SELECTED_VULKAN_DRIVER, "") : "");
            }

            String box64Version = getBox64Version(selectedGameName);
            if (box64Version.equals("Global")) {
                box64Version = (preferences != null ? preferences.getString(SELECTED_BOX64, "") : "");
            }

            int driverType = getVulkanDriverType(selectedGameName);

            Intent runActivityIntent = new Intent(this, EmulationActivity.class);
            Intent runWineIntent = new Intent(ACTION_RUN_WINE);

            runWineIntent.putExtra("exePath", getExePath(shortcutName));
            runWineIntent.putExtra("exeArguments", getExeArguments(shortcutName));
            runWineIntent.putExtra("driverName", driverName);
            runWineIntent.putExtra("driverType", driverType);
            runWineIntent.putExtra("box64Version", box64Version);
            runWineIntent.putExtra("box64Preset", getBox64Preset(selectedGameName));
            runWineIntent.putExtra("displayResolution", getDisplaySettings(selectedGameName).get(1));
            runWineIntent.putExtra("virtualControllerPreset", getSelectedVirtualControllerPreset(selectedGameName));
            runWineIntent.putExtra("d3dxRenderer", getD3DXRenderer(selectedGameName));
            runWineIntent.putExtra("wineD3D", getWineD3DVersion(selectedGameName));
            runWineIntent.putExtra("dxvk", getDXVKVersion(selectedGameName));
            runWineIntent.putExtra("vkd3d", getVKD3DVersion(selectedGameName));
            runWineIntent.putExtra("esync", getWineESync(selectedGameName));
            runWineIntent.putExtra("services", getWineServices(selectedGameName));
            runWineIntent.putExtra("virtualDesktop", getWineVirtualDesktop(selectedGameName));
            runWineIntent.putExtra("enableXInput", getEnableXInput(selectedGameName));
            runWineIntent.putExtra("enableDInput", getEnableDInput(selectedGameName));
            runWineIntent.putExtra("cpuAffinity", getCpuAffinity(selectedGameName));

            sendBroadcast(runWineIntent);
            startActivity(runActivityIntent);

            return;
        }

        Uri uri = intent.getData();

        if (uri != null) {
            String filePath = FilePathResolver.resolvePath(this, uri);

            if (filePath != null) {
                new EditGamePreferencesFragment(FILE_MANAGER_START_PREFERENCES, new File(filePath)).show(getSupportFragmentManager(), "");
            }
        }
    }

    @SuppressLint("SdCardPath")
    public static final File appRootDir = new File("/data/data/com.micewine.emu/files");
    public static File ratPackagesDir = new File(appRootDir + "/packages");
    public static boolean appBuiltinRootfs = false;
    public static String deviceArch = Build.SUPPORTED_ABIS[0].replace("arm64-v8a", "aarch64");
    public static final String unixUsername = runCommandWithOutput("whoami", false).replace("\n", "");
    public static String customRootFSPath = null;
    public static File usrDir = new File(appRootDir + "/usr");
    public static File tmpDir = new File(usrDir + "/tmp");
    public static File homeDir = new File(appRootDir + "/home");
    public static boolean setupDone = false;
    public static boolean enableRamCounter = false;
    public static boolean enableCpuCounter = false;
    public static boolean enableDebugInfo = false;
    public static boolean enableDRI3 = false;
    public static boolean enableMangoHUD = false;
    public static String appLang = null;
    public static String box64LogLevel = null;
    public static Integer box64MMap32 = null;
    public static Integer box64Avx = null;
    public static Integer box64Sse42 = null;
    public static Integer box64DynarecBigBlock = null;
    public static Integer box64DynarecStrongMem = null;
    public static Integer box64DynarecWeakBarrier = null;
    public static Integer box64DynarecPause = null;
    public static Integer box64DynarecX87Double = null;
    public static Integer box64DynarecFastNan = null;
    public static Integer box64DynarecFastRound = null;
    public static Integer box64DynarecSafeFlags = null;
    public static Integer box64DynarecCallRet = null;
    public static Integer box64DynarecAlignedAtomics = null;
    public static Integer box64DynarecNativeFlags = null;
    public static Integer box64DynarecBleedingEdge = null;
    public static Integer box64DynarecWait = null;
    public static Integer box64DynarecDirty = null;
    public static Integer box64DynarecForward = null;
    public static Integer box64DynarecDF = null;
    public static Integer box64ShowSegv = null;
    public static Integer box64ShowBt = null;
    public static Integer box64NoSigSegv = null;
    public static Integer box64NoSigill = null;
    public static String wineLogLevel = null;
    public static String selectedBox64 = null;
    public static String selectedD3DXRenderer = null;
    public static String selectedWineD3D = null;
    public static String selectedDXVK = null;
    public static String selectedVKD3D = null;
    public static String selectedGLProfile = null;
    public static String selectedDXVKHud = null;
    public static String selectedMesaVkWsiPresentMode = null;
    public static String selectedTuDebugPreset = null;
    public static int selectedFragmentId = 0;
    public static String memoryStats = "??/??";
    public static String totalCpuUsage = "???%";
    public static File winePrefixesDir = new File(appRootDir + "/winePrefixes");
    public static File wineDisksFolder = null;
    public static String winePrefix = null;
    public static boolean wineESync = false;
    public static boolean wineServices = false;
    public static String selectedCpuAffinity = null;
    public static boolean enableWineVirtualDesktop = false;
    public static String selectedWine = null;
    public static String fileManagerDefaultDir = "";
    public static String fileManagerCwd = null;
    public static String selectedFile = "";
    public static String miceWineVersion = "MiceWine " + BuildConfig.VERSION_NAME + (BuildConfig.DEBUG ? " (git-" + BuildConfig.GIT_SHORT_SHA + ")" : "");
    public static String vulkanDriverDeviceName = null;
    public static String vulkanDriverDriverVersion = null;
    public static int screenFpsLimit = 60;
    public static int fpsLimit = 0;
    public static String paSink = null;
    public static String selectedResolution = null;
    public static boolean useAdrenoTools = false;
    public static boolean enableXInput = true;
    public static boolean enableDInput = true;
    public static File adrenoToolsDriverFile = null;
    public static SharedPreferences preferences = null;
    public static final Gson gson = new Gson();

    public static final String ACTION_RUN_WINE = "com.micewine.emu.ACTION_RUN_WINE";
    public static final String ACTION_SETUP = "com.micewine.emu.ACTION_SETUP";
    public static final String ACTION_INSTALL_RAT = "com.micewine.emu.ACTION_INSTALL_RAT";
    public static final String ACTION_INSTALL_ADTOOLS_DRIVER = "com.micewine.emu.ACTION_INSTALL_ADTOOLS_DRIVER";
    public static final String ACTION_SELECT_FILE_MANAGER = "com.micewine.emu.ACTION_SELECT_FILE_MANAGER";
    public static final String ACTION_SELECT_ICON = "com.micewine.emu.ACTION_SELECT_ICON";
    public static final String ACTION_SELECT_EXE_PATH = "com.micewine.emu.ACTION_SELECT_EXE_PATH";
    public static final String ACTION_CREATE_WINE_PREFIX = "com.micewine.emu.ACTION_CREATE_WINE_PREFIX";

    public static final String RAM_COUNTER = "ramCounter";
    public static final boolean RAM_COUNTER_DEFAULT_VALUE = true;

    public static final String CPU_COUNTER = "cpuCounter";
    public static final boolean CPU_COUNTER_DEFAULT_VALUE = false;

    public static final String ENABLE_DEBUG_INFO = "debugInfo";
    public static final boolean ENABLE_DEBUG_INFO_DEFAULT_VALUE = true;

    public static final String APP_VERSION = "appVersion";

    private static final String ADRENOTOOLS_LD_PRELOAD = "adrenoToolsLdPreload";

    public static int strBoolToNum(boolean strBool) {
        return (strBool ? 1 : 0);
    }

    public static void setSharedVars(Activity activity, String adrenoToolsDriverPath) {
        setSharedVars(activity, null, null, null, null, null, null, null, null, null, null, null, null, null, adrenoToolsDriverPath);
    }

    public static void setSharedVars(Activity activity) {
        setSharedVars(activity, null, null, null, null, null, null, null, null, null, null, null, null, null, null);
    }

    public static void setSharedVars(
            Activity activity,
            String box64Version,
            String box64Preset,
            String d3dxRenderer,
            String wineD3D,
            String dxvk,
            String vkd3d,
            String displayResolution,
            Boolean esync,
            Boolean services,
            Boolean virtualDesktop,
            Boolean enableXInputController,
            Boolean enableDInputController,
            String cpuAffinity,
            String adrenoToolsDriverPath
    ) {
        useAdrenoTools = (adrenoToolsDriverPath != null);
        adrenoToolsDriverFile = (adrenoToolsDriverPath != null ? new File(adrenoToolsDriverPath) : null);

        appLang = activity.getResources().getString(R.string.app_lang);

        try {
            String[] assetList = activity.getAssets().list("");
            appBuiltinRootfs = (assetList != null && Arrays.asList(assetList).contains("rootfs.rat"));
        } catch (IOException ignored) {
            appBuiltinRootfs = false;
        }

        selectedBox64 = (box64Version != null ? box64Version : getBox64Version(selectedGameName));
        box64LogLevel = preferences.getString(BOX64_LOG, String.valueOf(BOX64_LOG_DEFAULT_VALUE));

        box64ShowSegv = strBoolToNum(preferences.getBoolean(BOX64_SHOWSEGV, BOX64_SHOWSEGV_DEFAULT_VALUE));
        box64ShowBt = strBoolToNum(preferences.getBoolean(BOX64_SHOWBT, BOX64_SHOWBT_DEFAULT_VALUE));
        box64NoSigill = strBoolToNum(preferences.getBoolean(BOX64_NOSIGILL, BOX64_NOSIGILL_DEFAULT_VALUE));
        box64NoSigSegv = strBoolToNum(preferences.getBoolean(BOX64_NOSIGSEGV, BOX64_NOSIGSEGV_DEFAULT_VALUE));

        setBox64Preset(box64Preset);

        enableDRI3 = preferences.getBoolean(ENABLE_DRI3, ENABLE_DRI3_DEFAULT_VALUE);
        enableMangoHUD = preferences.getBoolean(ENABLE_MANGOHUD, ENABLE_MANGOHUD_DEFAULT_VALUE);
        wineLogLevel = preferences.getString(WINE_LOG_LEVEL, WINE_LOG_LEVEL_DEFAULT_VALUE);

        selectedD3DXRenderer = (d3dxRenderer != null ? d3dxRenderer : getD3DXRenderer(selectedGameName));
        selectedWineD3D = (wineD3D != null ? wineD3D : getWineD3DVersion(selectedGameName));
        selectedDXVK = (dxvk != null ? dxvk : getDXVKVersion(selectedGameName));
        selectedVKD3D = (vkd3d != null ? vkd3d : getVKD3DVersion(selectedGameName));

        selectedResolution = (displayResolution != null ? displayResolution : getDisplaySettings(selectedGameName).get(1));
        wineESync = (esync != null ? esync : getWineESync(selectedGameName));
        wineServices = (services != null ? services : getWineServices(selectedGameName));
        enableWineVirtualDesktop = (virtualDesktop != null ? virtualDesktop : getWineVirtualDesktop(selectedGameName));
        enableXInput = (enableXInputController != null ? enableXInputController : getEnableXInput(selectedGameName));
        enableDInput = (enableDInputController != null ? enableDInputController : getEnableDInput(selectedGameName));
        selectedCpuAffinity = (cpuAffinity != null ? cpuAffinity : getCpuAffinity(selectedGameName));

        selectedGLProfile = preferences.getString(SELECTED_GL_PROFILE, SELECTED_GL_PROFILE_DEFAULT_VALUE);
        selectedDXVKHud = preferences.getString(SELECTED_DXVK_HUD_PRESET, SELECTED_DXVK_HUD_PRESET_DEFAULT_VALUE);
        selectedMesaVkWsiPresentMode = preferences.getString(SELECTED_MESA_VK_WSI_PRESENT_MODE, SELECTED_MESA_VK_WSI_PRESENT_MODE_DEFAULT_VALUE);
        selectedTuDebugPreset = preferences.getString(SELECTED_TU_DEBUG_PRESET, SELECTED_TU_DEBUG_PRESET_DEFAULT_VALUE);

        enableRamCounter = preferences.getBoolean(RAM_COUNTER, RAM_COUNTER_DEFAULT_VALUE);
        enableCpuCounter = preferences.getBoolean(CPU_COUNTER, CPU_COUNTER_DEFAULT_VALUE);
        enableDebugInfo = preferences.getBoolean(ENABLE_DEBUG_INFO, ENABLE_DEBUG_INFO_DEFAULT_VALUE);

        screenFpsLimit = (int) ((WindowManager) activity.getSystemService(WINDOW_SERVICE)).getDefaultDisplay().getRefreshRate();
        fpsLimit = preferences.getInt(FPS_LIMIT, screenFpsLimit);

        vulkanDriverDeviceName = getVulkanDriverInfo("deviceName", false) + (useAdrenoTools ? " (AdrenoTools)" : "");
        vulkanDriverDriverVersion = getVulkanDriverInfo("driverVersion", false).split(" ")[0];

        winePrefix = getSelectedWinePrefix();
        wineDisksFolder = new File(winePrefixesDir + "/" + winePrefix + "/dosdevices/");

        File winePrefixConfigFile = new File(winePrefixesDir + "/" + winePrefix + "/config");
        if (winePrefixConfigFile.exists()) {
            try {
                List<String> lines = Files.readAllLines(winePrefixConfigFile.toPath());
                selectedWine = lines.get(0);
            } catch (IOException ignored) {
            }

            fileManagerDefaultDir = wineDisksFolder.getPath();

            paSink = preferences.getString(PA_SINK, PA_SINK_DEFAULT_VALUE).toLowerCase();
        }
    }

    public static void setBox64Preset(String box64Preset) {
        String selectedBox64Preset = ((box64Preset != null && !box64Preset.equals("--")) ? box64Preset : preferences.getString(SELECTED_BOX64_PRESET, "default"));

        box64MMap32 = strBoolToNum(getBox64MMap32(selectedBox64Preset));
        box64Avx = getBox64Avx(selectedBox64Preset);
        box64Sse42 = strBoolToNum(getBox64Sse42(selectedBox64Preset));
        box64DynarecBigBlock = getBox64BigBlock(selectedBox64Preset);
        box64DynarecStrongMem = getBox64StrongMem(selectedBox64Preset);
        box64DynarecWeakBarrier = getBox64WeakBarrier(selectedBox64Preset);
        box64DynarecPause = getBox64Pause(selectedBox64Preset);
        box64DynarecX87Double = getBox64X87Double(selectedBox64Preset);
        box64DynarecFastNan = strBoolToNum(getBox64FastNan(selectedBox64Preset));
        box64DynarecFastRound = strBoolToNum(getBox64FastRound(selectedBox64Preset));
        box64DynarecSafeFlags = getBox64SafeFlags(selectedBox64Preset);
        box64DynarecCallRet = getBox64CallRet(selectedBox64Preset);
        box64DynarecAlignedAtomics = strBoolToNum(getBox64AlignedAtomics(selectedBox64Preset));
        box64DynarecNativeFlags = strBoolToNum(getBox64NativeFlags(selectedBox64Preset));
        box64DynarecWait = strBoolToNum(getBox64Wait(selectedBox64Preset));
        box64DynarecDirty = getBox64Dirty(selectedBox64Preset);
        box64DynarecForward = getBox64Forward(selectedBox64Preset);
        box64DynarecDF = strBoolToNum(getBox64DF(selectedBox64Preset));
    }

    public static void copyAssets(Activity activity, String filename, String outputPath) {
        dialogTitleText = activity.getString(R.string.extracting_from_assets);

        AssetManager assetManager = activity.getAssets();

        if (appBuiltinRootfs) {
            InputStream input = null;
            OutputStream out = null;
            try {
                input = assetManager.open(filename);
                File outFile = new File(outputPath, filename);
                out = Files.newOutputStream(outFile.toPath());
                copyFile(input, out);
            } catch (IOException ignored) {
            } finally {
                try {
                    if (input != null) input.close();
                    if (out != null) out.close();
                } catch (IOException ignored) {
                }
            }
        }
    }

    public static void copyFile(InputStream input, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        int read;
        while ((read = input.read(buffer)) != -1) {
            out.write(buffer, 0, read);
        }
    }

    private static String getVulkanDriverInfo(String info, boolean stdErr) {
        return runCommandWithOutput("echo $(" + getEnv() + " DISPLAY= vulkaninfo | grep " + info + " | cut -d '=' -f 2)", stdErr);
    }

    public static void getMemoryInfo(Context context) {
        ActivityManager activityManager = (ActivityManager) context.getSystemService(ACTIVITY_SERVICE);
        ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
        long totalMemory;
        long availableMemory;
        long usedMemory;

        while (enableRamCounter) {
            activityManager.getMemoryInfo(memoryInfo);

            totalMemory = memoryInfo.totalMem / (1024 * 1024);
            availableMemory = memoryInfo.availMem / (1024 * 1024);
            usedMemory = totalMemory - availableMemory;

            memoryStats = usedMemory + "/" + totalMemory;

            try {
                Thread.sleep(800);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static void getCpuInfo() {
        int availProcessors = Runtime.getRuntime().availableProcessors();

        while (enableCpuCounter) {
            String[] usageInfo = runCommandWithOutput("top -bqn 1 -o %CPU", false).split("\n");
            float usagePercentage = 0F;

            for (String usage : usageInfo) {
                usagePercentage += Float.parseFloat(usage.trim());
            }

            totalCpuUsage = usagePercentage / availProcessors + "%";

            try {
                Thread.sleep(800);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private static final StringBuilder driverWorkaroundLdPreload = new StringBuilder();
    private static boolean findingLdPreloadWorkaround = false;

    private static String locateLibraryBySymbol(String symbol) {
        File[] files = new File("/system/lib64").listFiles();

        if (files != null) {
            for (File file : files) {
                if (file.isFile() && file.getName().endsWith(".so")) {
                    String readElf = runCommandWithOutput("echo $(readelf --dyn-syms " + file.getPath() + " | grep " + symbol + ")", true);
                    if (readElf.contains(symbol) && !(readElf.contains("FUNC GLOBAL DEFAULT UND"))) {
                        return file.getPath();
                    }
                }
            }
        }

        return "";
    }

    public static String getLdPreloadWorkaround() {
        if (findingLdPreloadWorkaround) return "LD_PRELOAD=" + driverWorkaroundLdPreload;

        String savedLdPreload = preferences.getString(ADRENOTOOLS_LD_PRELOAD, "");
        if (!savedLdPreload.isEmpty()) return "LD_PRELOAD=" + savedLdPreload;

        driverWorkaroundLdPreload.setLength(0);

        findingLdPreloadWorkaround = true;

        while (true) {
            String res = getVulkanDriverInfo("", true);
            if (res.contains("cannot locate symbol")) {
                String symbolName = res.split("\"")[1];
                driverWorkaroundLdPreload.append(locateLibraryBySymbol(symbolName)).append(":");
            } else if (res.contains("cannot find")) {
                String libName = res.split("\"")[1];
                driverWorkaroundLdPreload.append("/system/lib64/").append(libName).append(":");
            } else {
                break;
            }
        }

        findingLdPreloadWorkaround = false;

        SharedPreferences.Editor editor = preferences.edit();

        editor.putString(ADRENOTOOLS_LD_PRELOAD, driverWorkaroundLdPreload.toString());
        editor.apply();

        return "LD_PRELOAD=" + driverWorkaroundLdPreload;
    }

    public static String[] resolutions16_9 = new String[] {
            "640x360", "854x480",
            "960x540", "1280x720",
            "1366x768", "1600x900",
            "1920x1080", "2560x1440",
            "3840x2160", "7680x4320"
    };

    public static String[] resolutions4_3 = new String[] {
            "640x480", "800x600",
            "1024x768", "1280x960",
            "1400x1050", "1600x1200"
    };

    public static String getNativeResolution(Context context) {
        WindowManager windowManager = (WindowManager) context.getSystemService(WINDOW_SERVICE);
        DisplayMetrics displayMetrics = new DisplayMetrics();

        windowManager.getDefaultDisplay().getRealMetrics(displayMetrics);

        if (displayMetrics.widthPixels > displayMetrics.heightPixels) {
            return displayMetrics.widthPixels + "x" + displayMetrics.heightPixels;
        } else {
            return displayMetrics.heightPixels + "x" + displayMetrics.widthPixels;
        }
    }

    public static String getPercentOfResolution(String originalResolution, int percent) {
        String[] resolution = originalResolution.split("x");
        int width = Integer.parseInt(resolution[0]) * percent / 100;
        int height = Integer.parseInt(resolution[1]) * percent / 100;

        return width + "x" + height;
    }

    public static List<String> getNativeResolutions(Activity activity) {
        ArrayList<String> parsedResolutions = new ArrayList<>();
        String nativeResolution = getNativeResolution(activity);

        parsedResolutions.add(nativeResolution);
        parsedResolutions.add(getPercentOfResolution(nativeResolution, 90));
        parsedResolutions.add(getPercentOfResolution(nativeResolution, 80));
        parsedResolutions.add(getPercentOfResolution(nativeResolution, 70));
        parsedResolutions.add(getPercentOfResolution(nativeResolution, 60));
        parsedResolutions.add(getPercentOfResolution(nativeResolution, 50));
        parsedResolutions.add(getPercentOfResolution(nativeResolution, 40));
        parsedResolutions.add(getPercentOfResolution(nativeResolution, 30));

        return parsedResolutions;
    }

    public static final int EXPORT_LNK_ACTION = 1;
}