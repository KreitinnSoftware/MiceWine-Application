package com.micewine.emu.fragments;

import static com.micewine.emu.activities.GeneralSettingsActivity.SELECTED_BOX64;
import static com.micewine.emu.activities.GeneralSettingsActivity.SELECTED_VULKAN_DRIVER;
import static com.micewine.emu.activities.MainActivity.ACTION_RUN_WINE;
import static com.micewine.emu.activities.MainActivity.ACTION_SELECT_EXE_PATH;
import static com.micewine.emu.activities.MainActivity.ACTION_SELECT_ICON;
import static com.micewine.emu.activities.MainActivity.getNativeResolutions;
import static com.micewine.emu.activities.MainActivity.preferences;
import static com.micewine.emu.activities.MainActivity.resolutions16_9;
import static com.micewine.emu.activities.MainActivity.resolutions4_3;
import static com.micewine.emu.activities.MainActivity.selectedCpuAffinity;
import static com.micewine.emu.activities.MainActivity.usrDir;
import static com.micewine.emu.activities.MainActivity.wineDisksFolder;
import static com.micewine.emu.adapters.AdapterGame.selectedGameName;
import static com.micewine.emu.controller.ControllerUtils.connectedPhysicalControllers;
import static com.micewine.emu.core.RatPackageManager.getPackageNameVersionById;
import static com.micewine.emu.core.RatPackageManager.listRatPackages;
import static com.micewine.emu.fragments.Box64PresetManagerFragment.getBox64Presets;
import static com.micewine.emu.fragments.ControllerPresetManagerFragment.getControllerPresets;
import static com.micewine.emu.fragments.ControllerSettingsFragment.ACTION_UPDATE_CONTROLLERS_STATUS;
import static com.micewine.emu.fragments.DebugSettingsFragment.availableCPUs;
import static com.micewine.emu.fragments.ShortcutsFragment.getBox64Preset;
import static com.micewine.emu.fragments.ShortcutsFragment.getBox64Version;
import static com.micewine.emu.fragments.ShortcutsFragment.getControllerPreset;
import static com.micewine.emu.fragments.ShortcutsFragment.getControllerXInput;
import static com.micewine.emu.fragments.ShortcutsFragment.getControllerXInputSwapAnalogs;
import static com.micewine.emu.fragments.ShortcutsFragment.getCpuAffinity;
import static com.micewine.emu.fragments.ShortcutsFragment.getD3DXRenderer;
import static com.micewine.emu.fragments.ShortcutsFragment.getDXVKVersion;
import static com.micewine.emu.fragments.ShortcutsFragment.getDisplaySettings;
import static com.micewine.emu.fragments.ShortcutsFragment.getEnableDInput;
import static com.micewine.emu.fragments.ShortcutsFragment.getEnableXInput;
import static com.micewine.emu.fragments.ShortcutsFragment.getExeArguments;
import static com.micewine.emu.fragments.ShortcutsFragment.getExePath;
import static com.micewine.emu.fragments.ShortcutsFragment.getGameExeArguments;
import static com.micewine.emu.fragments.ShortcutsFragment.getGameIcon;
import static com.micewine.emu.fragments.ShortcutsFragment.getSelectedVirtualControllerPreset;
import static com.micewine.emu.fragments.ShortcutsFragment.getVKD3DVersion;
import static com.micewine.emu.fragments.ShortcutsFragment.getVirtualControllerXInput;
import static com.micewine.emu.fragments.ShortcutsFragment.getVulkanDriver;
import static com.micewine.emu.fragments.ShortcutsFragment.getVulkanDriverType;
import static com.micewine.emu.fragments.ShortcutsFragment.getWineD3DVersion;
import static com.micewine.emu.fragments.ShortcutsFragment.getWineESync;
import static com.micewine.emu.fragments.ShortcutsFragment.getWineServices;
import static com.micewine.emu.fragments.ShortcutsFragment.getWineVirtualDesktop;
import static com.micewine.emu.fragments.ShortcutsFragment.putBox64Preset;
import static com.micewine.emu.fragments.ShortcutsFragment.putBox64Version;
import static com.micewine.emu.fragments.ShortcutsFragment.putControllerPreset;
import static com.micewine.emu.fragments.ShortcutsFragment.putControllerXInput;
import static com.micewine.emu.fragments.ShortcutsFragment.putCpuAffinity;
import static com.micewine.emu.fragments.ShortcutsFragment.putD3DXRenderer;
import static com.micewine.emu.fragments.ShortcutsFragment.putDXVKVersion;
import static com.micewine.emu.fragments.ShortcutsFragment.putDisplaySettings;
import static com.micewine.emu.fragments.ShortcutsFragment.putEnableDInput;
import static com.micewine.emu.fragments.ShortcutsFragment.putEnableXInput;
import static com.micewine.emu.fragments.ShortcutsFragment.putExeArguments;
import static com.micewine.emu.fragments.ShortcutsFragment.putSelectedVirtualControllerPreset;
import static com.micewine.emu.fragments.ShortcutsFragment.putVKD3DVersion;
import static com.micewine.emu.fragments.ShortcutsFragment.putVirtualControllerXInput;
import static com.micewine.emu.fragments.ShortcutsFragment.putVulkanDriver;
import static com.micewine.emu.fragments.ShortcutsFragment.putWineD3DVersion;
import static com.micewine.emu.fragments.ShortcutsFragment.putWineESync;
import static com.micewine.emu.fragments.ShortcutsFragment.putWineServices;
import static com.micewine.emu.fragments.ShortcutsFragment.putWineVirtualDesktop;
import static com.micewine.emu.fragments.ShortcutsFragment.setGameName;
import static com.micewine.emu.fragments.VirtualControllerPresetManagerFragment.getVirtualControllerPresets;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.DataSetObserver;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.materialswitch.MaterialSwitch;
import com.micewine.emu.R;
import com.micewine.emu.activities.EmulationActivity;
import com.micewine.emu.core.RatPackageManager;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class EditGamePreferencesFragment extends DialogFragment {
    private final int type;
    private final File exeFile;

    public EditGamePreferencesFragment(int type) {
        this.type = type;
        this.exeFile = null;
    }

    public EditGamePreferencesFragment(int type, File exeFile) {
        this.type = type;
        this.exeFile = exeFile;
    }

    private ImageView imageView;
    private TextView exePathText;
    private ImageButton selectExePath;
    private EditText editTextNewName;
    private EditText editTextArguments;
    private Button buttonContinue;
    private Button buttonCancel;
    private Spinner selectedDisplayModeSpinner;
    private Spinner selectedDisplayResolutionSpinner;
    private Spinner selectedDriverSpinner;
    private Spinner selectedD3DXRendererSpinner;
    private Spinner selectedDXVKSpinner;
    private Spinner selectedWineD3DSpinner;
    private Spinner selectedVKD3DSpinner;
    private MaterialSwitch wineESyncSwitch;
    private MaterialSwitch wineServicesSwitch;
    private MaterialSwitch enableWineVirtualDesktopSwitch;
    private MaterialSwitch enableXInputSwitch;
    private MaterialSwitch enableDInputSwitch;
    private Spinner cpuAffinitySpinner;
    private Spinner selectedBox64Spinner;
    private Spinner selectedBox64ProfileSpinner;
    private List<TextView> controllersMappingTypeTexts;
    private List<Spinner> controllersMappingTypeSpinners;
    private List<TextView> controllersSwapAnalogsTexts;
    private List<MaterialSwitch> controllersSwapAnalogsSwitches;
    private List<Spinner> controllersKeyboardPresetSpinners;
    private List<TextView> controllersKeyboardPresetTexts;
    private List<TextView> controllersNamesTexts;
    private TextView onScreenControllerMappingTypeText;
    private Spinner onScreenControllerMappingTypeSpinner;
    private TextView onScreenControllerKeyboardPresetText;
    private Spinner onScreenControllerKeyboardPresetSpinner;
    private final List<String> mappingTypes = List.of("MiceWine Controller", "Keyboard/Mouse");
    private final List<String> virtualControllerProfilesNames =
            getVirtualControllerPresets().stream()
                    .map(VirtualControllerPresetManagerFragment.VirtualControllerPreset::getName)
                    .collect(Collectors.toList());
    private final List<String> controllerProfilesNames =
            getControllerPresets().stream()
                    .map(ControllerPresetManagerFragment.ControllerPreset::getName)
                    .collect(Collectors.toList());

    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, @NonNull Intent intent) {
            if (ACTION_UPDATE_CONTROLLERS_STATUS.equals(intent.getAction())) {
                requireActivity().runOnUiThread(() -> updateControllersStatus());
            }
        }
    };

    @SuppressLint("SetTextI18n")
    private void updateControllersStatus() {
        controllersNamesTexts.forEach((i) -> i.setVisibility(View.VISIBLE));
        controllersMappingTypeTexts.forEach((i) -> i.setVisibility(View.VISIBLE));
        controllersMappingTypeSpinners.forEach((i) -> i.setVisibility(View.VISIBLE));
        controllersKeyboardPresetSpinners.forEach((i) -> i.setVisibility(View.VISIBLE));
        controllersKeyboardPresetTexts.forEach((i) -> i.setVisibility(View.VISIBLE));
        controllersSwapAnalogsTexts.forEach((i) -> i.setVisibility(View.VISIBLE));
        controllersSwapAnalogsSwitches.forEach((i) -> i.setVisibility(View.VISIBLE));

        for (int i = connectedPhysicalControllers.size(); i < 4; i++) {
            controllersNamesTexts.get(i).setVisibility(View.GONE);
            controllersMappingTypeTexts.get(i).setVisibility(View.GONE);
            controllersMappingTypeSpinners.get(i).setVisibility(View.GONE);
            controllersKeyboardPresetSpinners.get(i).setVisibility(View.GONE);
            controllersKeyboardPresetTexts.get(i).setVisibility(View.GONE);
            controllersSwapAnalogsTexts.get(i).setVisibility(View.GONE);
            controllersSwapAnalogsSwitches.get(i).setVisibility(View.GONE);
        }

        if (connectedPhysicalControllers.isEmpty()) {
            controllersMappingTypeTexts.get(0).setVisibility(View.VISIBLE);
            controllersMappingTypeTexts.get(0).setText(R.string.no_controllers_connected);
        }

        for (int i = 0; i < controllersNamesTexts.size(); i++) {
            if (i < connectedPhysicalControllers.size()) {
                controllersNamesTexts.get(i).setText(i + " " + connectedPhysicalControllers.get(i).getName());
            }
        }

        for (int i = 0; i < controllersMappingTypeSpinners.size(); i++) {
            controllersMappingTypeSpinners.get(i).setAdapter(new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, mappingTypes));
            controllersMappingTypeSpinners.get(i).setSelection(getControllerXInput(selectedGameName, i) ? 0 : 1);
            int index = i;
            controllersMappingTypeSpinners.get(i).setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int i1, long l) {
                    boolean isXInput = (i1 == 0);
                    controllersKeyboardPresetSpinners.get(index).setVisibility(isXInput ? View.GONE : View.VISIBLE);
                    controllersKeyboardPresetTexts.get(index).setVisibility(isXInput ? View.GONE : View.VISIBLE);

                    controllersSwapAnalogsSwitches.get(index).setVisibility(isXInput ? View.VISIBLE : View.GONE);
                    controllersSwapAnalogsTexts.get(index).setVisibility(isXInput ? View.VISIBLE : View.GONE);

                    temporarySettings.controllerXInput[index] = isXInput;
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {
                }
            });
        }

        for (int i = 0; i < controllersSwapAnalogsSwitches.size(); i++) {
            controllersSwapAnalogsSwitches.get(i).setChecked(getControllerXInputSwapAnalogs(selectedGameName, i));
            int index = i;
            controllersSwapAnalogsSwitches.get(i).setOnClickListener((v) -> {
                temporarySettings.controllerSwapAnalogs[index] = controllersSwapAnalogsSwitches.get(index).isChecked();
            });
        }

        for (int i = 0; i < controllersKeyboardPresetSpinners.size(); i++) {
            controllersKeyboardPresetSpinners.get(i).setAdapter(new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, controllerProfilesNames));
            controllersKeyboardPresetSpinners.get(i).setSelection(controllerProfilesNames.indexOf(getControllerPreset(selectedGameName, i)));
            int index = i;
            controllersKeyboardPresetSpinners.get(i).setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int i1, long l) {
                    temporarySettings.controllerPreset[index] = adapterView.getSelectedItem().toString();
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {
                }
            });
        }
    }


    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.fragment_edit_game_preferences, null);

        selectExePath = view.findViewById(R.id.selectExePath);
        editTextNewName = view.findViewById(R.id.editTextNewName);
        editTextArguments = view.findViewById(R.id.appArgumentsEditText);
        buttonContinue = view.findViewById(R.id.buttonContinue);
        buttonCancel = view.findViewById(R.id.buttonCancel);
        selectedDisplayModeSpinner = view.findViewById(R.id.selectedDisplayMode);
        selectedDisplayResolutionSpinner = view.findViewById(R.id.selectedDisplayResolution);
        selectedDriverSpinner = view.findViewById(R.id.selectedDriver);
        selectedD3DXRendererSpinner = view.findViewById(R.id.selectedD3DXRenderer);
        selectedDXVKSpinner = view.findViewById(R.id.selectedDXVK);
        selectedWineD3DSpinner = view.findViewById(R.id.selectedWineD3D);
        selectedVKD3DSpinner = view.findViewById(R.id.selectedVKD3D);
        wineESyncSwitch = view.findViewById(R.id.wineESync);
        wineServicesSwitch = view.findViewById(R.id.wineServices);
        enableWineVirtualDesktopSwitch = view.findViewById(R.id.enableWineVirtualDesktop);
        enableXInputSwitch = view.findViewById(R.id.enableXInput);
        enableDInputSwitch = view.findViewById(R.id.enableDInput);
        cpuAffinitySpinner = view.findViewById(R.id.cpuAffinity);

        controllersMappingTypeTexts = List.of(
                view.findViewById(R.id.controller0MappingTypeText),
                view.findViewById(R.id.controller1MappingTypeText),
                view.findViewById(R.id.controller2MappingTypeText),
                view.findViewById(R.id.controller3MappingTypeText)
        );
        controllersMappingTypeSpinners = List.of(
                view.findViewById(R.id.controller0MappingTypeSpinner),
                view.findViewById(R.id.controller1MappingTypeSpinner),
                view.findViewById(R.id.controller2MappingTypeSpinner),
                view.findViewById(R.id.controller3MappingTypeSpinner)
        );
        controllersSwapAnalogsTexts = List.of(
                view.findViewById(R.id.controller0SwapAnalogsText),
                view.findViewById(R.id.controller1SwapAnalogsText),
                view.findViewById(R.id.controller2SwapAnalogsText),
                view.findViewById(R.id.controller3SwapAnalogsText)
        );
        controllersSwapAnalogsSwitches = List.of(
                view.findViewById(R.id.controller0SwapAnalogsSwitch),
                view.findViewById(R.id.controller1SwapAnalogsSwitch),
                view.findViewById(R.id.controller2SwapAnalogsSwitch),
                view.findViewById(R.id.controller3SwapAnalogsSwitch)
        );
        controllersKeyboardPresetSpinners = List.of(
                view.findViewById(R.id.controller0KeyboardPresetSpinner),
                view.findViewById(R.id.controller1KeyboardPresetSpinner),
                view.findViewById(R.id.controller2KeyboardPresetSpinner),
                view.findViewById(R.id.controller3KeyboardPresetSpinner)
        );
        controllersKeyboardPresetTexts = List.of(
                view.findViewById(R.id.controller0KeyboardPresetText),
                view.findViewById(R.id.controller1KeyboardPresetText),
                view.findViewById(R.id.controller2KeyboardPresetText),
                view.findViewById(R.id.controller3KeyboardPresetText)
        );
        controllersNamesTexts = List.of(
                view.findViewById(R.id.controller0Name),
                view.findViewById(R.id.controller1Name),
                view.findViewById(R.id.controller2Name),
                view.findViewById(R.id.controller3Name)
        );

        updateControllersStatus();

        onScreenControllerMappingTypeText = view.findViewById(R.id.onScreenControllerMappingTypeText);
        onScreenControllerMappingTypeSpinner = view.findViewById(R.id.onScreenControllerMappingTypeSpinner);
        onScreenControllerKeyboardPresetText = view.findViewById(R.id.onScreenControllerKeyboardPresetText);
        onScreenControllerKeyboardPresetSpinner = view.findViewById(R.id.onScreenControllerKeyboardPresetSpinner);

        onScreenControllerMappingTypeSpinner.setAdapter(new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, mappingTypes));
        onScreenControllerMappingTypeSpinner.setSelection(getVirtualControllerXInput(selectedGameName) ? 0 : 1);
        onScreenControllerMappingTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                boolean isXInput = (i == 0);
                onScreenControllerKeyboardPresetSpinner.setVisibility(isXInput ? View.GONE : View.VISIBLE);
                onScreenControllerKeyboardPresetText.setVisibility(isXInput ? View.GONE : View.VISIBLE);

                temporarySettings.virtualXInputController = isXInput;
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        onScreenControllerKeyboardPresetSpinner.setAdapter(new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, virtualControllerProfilesNames));
        onScreenControllerKeyboardPresetSpinner.setSelection(virtualControllerProfilesNames.indexOf(getSelectedVirtualControllerPreset(selectedGameName)));
        onScreenControllerKeyboardPresetSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                temporarySettings.virtualControllerPreset = virtualControllerProfilesNames.get(i);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        selectedBox64Spinner = view.findViewById(R.id.selectedBox64);
        selectedBox64ProfileSpinner = view.findViewById(R.id.selectedBox64Profile);
        imageView = view.findViewById(R.id.imageView);
        exePathText = view.findViewById(R.id.appExePath);

        switch (type) {
            case EDIT_GAME_PREFERENCES -> {
                editTextNewName.setText(selectedGameName);
                editTextArguments.setText(getGameExeArguments(selectedGameName));

                String exePath = getExePath(selectedGameName);
                String prefix = wineDisksFolder + "/";
                exePath = exePath.startsWith(prefix) ? exePath.substring(prefix.length()) : exePath;
                exePath = !exePath.isEmpty() ? Character.toUpperCase(exePath.charAt(0)) + exePath.substring(1) : exePath;

                exePathText.setText(exePath);
                exePathText.setSelected(true);

                if (selectedGameName.equals(getString(R.string.desktop_mode_init))) {
                    editTextNewName.setEnabled(false);
                    editTextArguments.setEnabled(false);
                    enableWineVirtualDesktopSwitch.setEnabled(false);
                    enableWineVirtualDesktopSwitch.setChecked(true);

                    imageView.setImageBitmap(resizeBitmap(
                            BitmapFactory.decodeResource(requireActivity().getResources(), R.drawable.default_icon), imageView.getLayoutParams().width, imageView.getLayoutParams().height
                    ));
                } else {
                    Bitmap gameIcon = getGameIcon(selectedGameName);

                    if (gameIcon == null) {
                        imageView.setImageResource(R.drawable.unknown_exe);
                    } else {
                        imageView.setImageBitmap(resizeBitmap(
                                gameIcon, imageView.getLayoutParams().width, imageView.getLayoutParams().height
                        ));
                    }

                    imageView.setOnClickListener((v) -> requireContext().sendBroadcast(new Intent(ACTION_SELECT_ICON)));
                    selectExePath.setOnClickListener((v) -> requireContext().sendBroadcast(new Intent(ACTION_SELECT_EXE_PATH)));
                }
            }
            case FILE_MANAGER_START_PREFERENCES -> {
                String fileExtension = exeFile.getName().substring(exeFile.getName().lastIndexOf(".") + 1);
                String fileName = exeFile.getName().replace("." + fileExtension, "");
                File iconFile = new File(usrDir, "icons/" + fileName + "-icon");

                if (iconFile.exists() && iconFile.length() > 0) {
                    imageView.setImageBitmap(BitmapFactory.decodeFile(iconFile.getPath()));
                } else {
                    imageView.setImageResource(R.drawable.ic_log);
                }

                editTextNewName.setText(fileName);
                editTextNewName.setEnabled(false);
                selectExePath.setVisibility(View.GONE);
                editTextArguments.setText("");

                String path = exeFile.getPath();
                path = path.startsWith(wineDisksFolder + "/") ? path.substring((wineDisksFolder + "/").length()) : path;
                path = !path.isEmpty() ? Character.toUpperCase(path.charAt(0)) + path.substring(1) : path;

                exePathText.setText(path);
                exePathText.setSelected(true);

                selectedGameName = "";
            }
        }

        List<String> displaySettings = getDisplaySettings(selectedGameName);

        System.out.println(displaySettings.get(0));

        selectedDisplayModeSpinner.setAdapter(new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, aspectRatios));
        selectedDisplayModeSpinner.setSelection(aspectRatios.indexOf(displaySettings.get(0)));
        selectedDisplayModeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String selectedItem = adapterView.getItemAtPosition(i).toString();
                List<String> resolutions;

                switch (selectedItem) {
                    case "16:9" -> resolutions = Arrays.asList(resolutions16_9);
                    case "4:3" -> resolutions = Arrays.asList(resolutions4_3);
                    case "Native" -> resolutions = getNativeResolutions(requireActivity());
                    default -> resolutions = List.of();
                }

                selectedDisplayResolutionSpinner.setAdapter(new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, resolutions));
                selectedDisplayResolutionSpinner.setSelection(resolutions.indexOf(displaySettings.get(1)));

                temporarySettings.displayMode = selectedItem;
                temporarySettings.displayResolution = resolutions.get(0);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        selectedDisplayResolutionSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                temporarySettings.displayResolution = adapterView.getItemAtPosition(i).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        List<RatPackageManager.RatPackage> vulkanDriversPackages = listRatPackages("VulkanDriver", "AdrenoToolsDriver");
        ArrayList<String> vulkanDrivers = new ArrayList<>(vulkanDriversPackages.size() + 1);
        ArrayList<String> vulkanDriversId = new ArrayList<>(vulkanDriversPackages.size() + 1);

        vulkanDrivers.add("Global: " + getPackageNameVersionById(preferences.getString(SELECTED_VULKAN_DRIVER, "")));
        vulkanDriversId.add("Global");

        for (RatPackageManager.RatPackage ratPackage : vulkanDriversPackages) {
            vulkanDrivers.add(ratPackage.getName() + " " + ratPackage.getVersion());
            vulkanDriversId.add(ratPackage.getFolderName());
        }

        int vkIndex = vulkanDriversId.indexOf(getVulkanDriver(selectedGameName));
        if (vkIndex < 0) vkIndex = 0;

        selectedDriverSpinner.setAdapter(new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, vulkanDrivers));
        selectedDriverSpinner.setSelection(vkIndex);
        selectedDriverSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                temporarySettings.vulkanDriver = vulkanDriversId.get(i);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        selectedD3DXRendererSpinner.setAdapter(new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, d3dxRenderers));
        selectedD3DXRendererSpinner.setSelection(d3dxRenderers.indexOf(getD3DXRenderer(selectedGameName)));
        selectedD3DXRendererSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String selectedItem = d3dxRenderers.get(i);

                switch (selectedItem) {
                    case "DXVK" -> {
                        selectedWineD3DSpinner.setEnabled(false);
                        selectedDXVKSpinner.setEnabled(true);
                    }
                    case "WineD3D" -> {
                        selectedWineD3DSpinner.setEnabled(true);
                        selectedDXVKSpinner.setEnabled(false);
                    }
                }

                temporarySettings.d3dxRenderer = selectedItem;
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        List<RatPackageManager.RatPackage> dxvkPackages = listRatPackages("DXVK");
        ArrayList<String> dxvkVersions = new ArrayList<>(dxvkPackages.size());
        ArrayList<String> dxvkVersionsId = new ArrayList<>(dxvkPackages.size());

        for (RatPackageManager.RatPackage ratPackage : dxvkPackages) {
            dxvkVersions.add(ratPackage.getName() + " " + ratPackage.getVersion());
            dxvkVersionsId.add(ratPackage.getFolderName());
        }

        int dxvkIndex = dxvkVersionsId.indexOf(getDXVKVersion(selectedGameName));
        if (dxvkIndex < 0) dxvkIndex = 0;

        selectedDXVKSpinner.setAdapter(new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, dxvkVersions));
        selectedDXVKSpinner.setSelection(dxvkIndex);
        selectedDXVKSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                temporarySettings.dxvk = dxvkVersionsId.get(i);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        List<RatPackageManager.RatPackage> wined3dPackages = listRatPackages("WineD3D");
        ArrayList<String> wined3dVersions = new ArrayList<>(wined3dPackages.size());
        ArrayList<String> wined3dVersionsId = new ArrayList<>(wined3dPackages.size());

        for (RatPackageManager.RatPackage ratPackage : wined3dPackages) {
            wined3dVersions.add(ratPackage.getName() + " " + ratPackage.getVersion());
            wined3dVersionsId.add(ratPackage.getFolderName());
        }

        int wined3dIndex = wined3dVersionsId.indexOf(getWineD3DVersion(selectedGameName));
        if (wined3dIndex < 0) wined3dIndex = 0;

        selectedWineD3DSpinner.setAdapter(new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, wined3dVersions));
        selectedWineD3DSpinner.setSelection(wined3dIndex);
        selectedWineD3DSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                temporarySettings.wineD3D = wined3dVersionsId.get(i);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        List<RatPackageManager.RatPackage> vkd3dPackages = listRatPackages("VKD3D");
        ArrayList<String> vkd3dVersions = new ArrayList<>(vkd3dPackages.size());
        ArrayList<String> vkd3dVersionsId = new ArrayList<>(vkd3dPackages.size());

        for (RatPackageManager.RatPackage ratPackage : vkd3dPackages) {
            vkd3dVersions.add(ratPackage.getName() + " " + ratPackage.getVersion());
            vkd3dVersionsId.add(ratPackage.getFolderName());
        }

        int vkd3dIndex = vkd3dVersionsId.indexOf(getVKD3DVersion(selectedGameName));
        if (vkd3dIndex < 0) vkd3dIndex = 0;

        selectedVKD3DSpinner.setAdapter(new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, vkd3dVersions));
        selectedVKD3DSpinner.setSelection(vkd3dIndex);
        selectedVKD3DSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                temporarySettings.vkd3d = vkd3dVersionsId.get(i);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        wineESyncSwitch.setChecked(getWineESync(selectedGameName));
        wineESyncSwitch.setOnClickListener((v) -> {
            temporarySettings.wineESync = wineESyncSwitch.isChecked();
        });

        wineServicesSwitch.setChecked(getWineServices(selectedGameName));
        wineServicesSwitch.setOnClickListener((v) -> {
            temporarySettings.wineServices = wineESyncSwitch.isChecked();
        });

        if (!selectedGameName.equals(getString(R.string.desktop_mode_init))) {
            enableWineVirtualDesktopSwitch.setChecked(getWineVirtualDesktop(selectedGameName));
            enableWineVirtualDesktopSwitch.setOnClickListener((v) -> {
                temporarySettings.wineVirtualDesktop = enableWineVirtualDesktopSwitch.isChecked();
            });
        }

        enableXInputSwitch.setChecked(getEnableXInput(selectedGameName));
        enableXInputSwitch.setOnClickListener((v) -> {
            temporarySettings.enableXInput = enableXInputSwitch.isChecked();
        });

        enableDInputSwitch.setChecked(getEnableDInput(selectedGameName));
        enableDInputSwitch.setOnClickListener((v) -> {
            temporarySettings.enableDInput = enableDInputSwitch.isChecked();
        });

        cpuAffinitySpinner.setAdapter(new CPUAffinityAdapter(requireActivity(), availableCPUs, cpuAffinitySpinner, type));

        List<RatPackageManager.RatPackage> box64Packages = listRatPackages("Box64");
        ArrayList<String> box64Versions = new ArrayList<>(box64Packages.size() + 1);
        ArrayList<String> box64VersionsId = new ArrayList<>(box64Packages.size() + 1);

        box64Versions.add("Global: " + getPackageNameVersionById(preferences.getString(SELECTED_BOX64, "")));
        box64VersionsId.add("Global");

        for (RatPackageManager.RatPackage ratPackage : box64Packages) {
            box64Versions.add(ratPackage.getName() + " " + ratPackage.getVersion());
            box64VersionsId.add(ratPackage.getFolderName());
        }

        int box64Index = box64VersionsId.indexOf(getBox64Version(selectedGameName));
        if (box64Index < 0) box64Index = 0;

        selectedBox64Spinner.setAdapter(new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, box64Versions));
        selectedBox64Spinner.setSelection(box64Index);
        selectedBox64Spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                temporarySettings.box64Version = box64VersionsId.get(i);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        List<Box64PresetManagerFragment.Box64Preset> box64Presets = getBox64Presets();
        ArrayList<String> box64PresetsName = new ArrayList<>(box64Presets.size());

        for (Box64PresetManagerFragment.Box64Preset box64Preset : box64Presets) {
            box64PresetsName.add(box64Preset.name);
        }

        int box64PresetIndex = box64PresetsName.indexOf(getBox64Preset(selectedGameName));
        if (box64PresetIndex < 0) box64PresetIndex = 0;

        selectedBox64ProfileSpinner.setAdapter(new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, box64PresetsName));
        selectedBox64ProfileSpinner.setSelection(box64PresetIndex);
        selectedBox64ProfileSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                temporarySettings.box64Preset = box64PresetsName.get(i);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        buttonContinue.setOnClickListener((v) -> {
            switch (type) {
                case EDIT_GAME_PREFERENCES -> {
                    String newName = editTextNewName.getText().toString().trim();
                    String newArguments = editTextArguments.getText().toString().trim();

                    if (newName.isEmpty()) {
                        Toast.makeText(requireContext(), R.string.invalid_preset_name, Toast.LENGTH_SHORT).show();
                        return;
                    }

                    putDisplaySettings(selectedGameName, temporarySettings.displayMode, temporarySettings.displayResolution);
                    putVulkanDriver(selectedGameName, temporarySettings.vulkanDriver);
                    putD3DXRenderer(selectedGameName, temporarySettings.d3dxRenderer);
                    putDXVKVersion(selectedGameName, temporarySettings.dxvk);
                    putWineD3DVersion(selectedGameName, temporarySettings.wineD3D);
                    putVKD3DVersion(selectedGameName, temporarySettings.vkd3d);
                    putWineESync(selectedGameName, temporarySettings.wineESync);
                    putWineServices(selectedGameName, temporarySettings.wineServices);
                    putWineVirtualDesktop(selectedGameName, temporarySettings.wineVirtualDesktop);
                    putCpuAffinity(selectedGameName, temporarySettings.cpuAffinity);
                    putSelectedVirtualControllerPreset(selectedGameName, temporarySettings.virtualControllerPreset);
                    putVirtualControllerXInput(selectedGameName, temporarySettings.virtualXInputController);

                    putControllerXInput(selectedGameName, temporarySettings.controllerXInput[0], 0);
                    putControllerXInput(selectedGameName, temporarySettings.controllerXInput[1], 1);
                    putControllerXInput(selectedGameName, temporarySettings.controllerXInput[2], 2);
                    putControllerXInput(selectedGameName, temporarySettings.controllerXInput[3], 3);

                    putControllerPreset(selectedGameName, temporarySettings.controllerPreset[0], 0);
                    putControllerPreset(selectedGameName, temporarySettings.controllerPreset[1], 1);
                    putControllerPreset(selectedGameName, temporarySettings.controllerPreset[2], 2);
                    putControllerPreset(selectedGameName, temporarySettings.controllerPreset[3], 3);

                    putBox64Version(selectedGameName, temporarySettings.box64Version);
                    putBox64Preset(selectedGameName, temporarySettings.box64Preset);

                    putEnableXInput(selectedGameName, temporarySettings.enableXInput);
                    putEnableDInput(selectedGameName, temporarySettings.enableDInput);

                    putExeArguments(selectedGameName, newArguments);
                    setGameName(selectedGameName, newName);
                }
                case FILE_MANAGER_START_PREFERENCES -> {
                    String arguments = editTextArguments.getText().toString().trim();

                    Intent runActivityIntent = new Intent(requireContext(), EmulationActivity.class);
                    Intent runWineIntent = new Intent(ACTION_RUN_WINE);

                    runWineIntent.putExtra("exePath", exeFile.getPath());
                    runWineIntent.putExtra("exeArguments", arguments);
                    runWineIntent.putExtra("driverName", temporarySettings.vulkanDriver);
                    runWineIntent.putExtra("driverType", getVulkanDriverType(temporarySettings.vulkanDriver));
                    runWineIntent.putExtra("box64Version", temporarySettings.box64Version);
                    runWineIntent.putExtra("box64Preset", temporarySettings.box64Preset);
                    runWineIntent.putExtra("displayResolution", temporarySettings.displayResolution);
                    runWineIntent.putExtra("virtualControllerPreset", temporarySettings.virtualControllerPreset);
                    runWineIntent.putExtra("d3dxRenderer", temporarySettings.d3dxRenderer);
                    runWineIntent.putExtra("wineD3D", temporarySettings.wineD3D);
                    runWineIntent.putExtra("dxvk", temporarySettings.dxvk);
                    runWineIntent.putExtra("vkd3d", temporarySettings.vkd3d);
                    runWineIntent.putExtra("esync", temporarySettings.wineESync);
                    runWineIntent.putExtra("services", temporarySettings.wineServices);
                    runWineIntent.putExtra("virtualDesktop", temporarySettings.wineVirtualDesktop);
                    runWineIntent.putExtra("enableXInput", temporarySettings.enableXInput);
                    runWineIntent.putExtra("enableDInput", temporarySettings.enableDInput);
                    runWineIntent.putExtra("cpuAffinity", temporarySettings.cpuAffinity);

                    requireContext().sendBroadcast(runWineIntent);
                    startActivity(runActivityIntent);
                }
            }

            dismiss();
        });

        buttonCancel.setOnClickListener((v) -> dismiss());

        requireActivity().registerReceiver(receiver, new IntentFilter(ACTION_UPDATE_CONTROLLERS_STATUS), 0);

        getParentFragmentManager().setFragmentResultListener("invalidate", this, (requestKey, result) -> {
            exePathText.post(() -> {
                String exePath = getExePath(selectedGameName);
                String prefix = wineDisksFolder + "/";
                exePath = exePath.startsWith(prefix) ? exePath.substring(prefix.length()) : exePath;
                exePath = !exePath.isEmpty() ? Character.toUpperCase(exePath.charAt(0)) + exePath.substring(1) : exePath;

                exePathText.setText(exePath);
            });
        });

        return new AlertDialog.Builder(requireContext(), R.style.CustomAlertDialog).setView(view).create();
    }

    @Override
    public void onResume() {
        super.onResume();

        Bitmap gameIcon = getGameIcon(selectedGameName);

        if (gameIcon == null) return;

        imageView.setImageBitmap(resizeBitmap(
                gameIcon, imageView.getLayoutParams().width, imageView.getLayoutParams().height
        ));
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);
        requireActivity().unregisterReceiver(receiver);
    }

    private Bitmap resizeBitmap(Bitmap originalBitmap, int width, int height) {
        return Bitmap.createScaledBitmap(originalBitmap, width, height, false);
    }

    public final static int EDIT_GAME_PREFERENCES = 0;
    public final static int FILE_MANAGER_START_PREFERENCES = 1;

    public static List<String> aspectRatios = List.of("16:9", "4:3", "Native");
    public static List<String> d3dxRenderers = List.of("DXVK", "WineD3D");
    private static final TemporarySettings temporarySettings = new TemporarySettings();

    private static class TemporarySettings {
        String displayMode = getDisplaySettings(selectedGameName).get(0);
        String displayResolution = getDisplaySettings(selectedGameName).get(1);
        String vulkanDriver = getVulkanDriver(selectedGameName);
        String d3dxRenderer = getD3DXRenderer(selectedGameName);
        String dxvk = getDXVKVersion(selectedGameName);
        String wineD3D = getWineD3DVersion(selectedGameName);
        String vkd3d = getVKD3DVersion(selectedGameName);
        boolean wineESync = getWineESync(selectedGameName);
        boolean wineServices = getWineServices(selectedGameName);
        boolean wineVirtualDesktop = getWineVirtualDesktop(selectedGameName);
        String cpuAffinity = getCpuAffinity(selectedGameName);
        String virtualControllerPreset = getSelectedVirtualControllerPreset(selectedGameName);
        boolean virtualXInputController = getVirtualControllerXInput(selectedGameName);
        boolean[] controllerXInput = new boolean[] {
                getControllerXInput(selectedGameName, 0),
                getControllerXInput(selectedGameName, 1),
                getControllerXInput(selectedGameName, 2),
                getControllerXInput(selectedGameName, 3)
        };
        String[] controllerPreset = new String[] {
                getControllerPreset(selectedGameName, 0),
                getControllerPreset(selectedGameName, 1),
                getControllerPreset(selectedGameName, 2),
                getControllerPreset(selectedGameName, 3)
        };
        boolean[] controllerSwapAnalogs = {
                getControllerXInputSwapAnalogs(selectedGameName, 0),
                getControllerXInputSwapAnalogs(selectedGameName, 1),
                getControllerXInputSwapAnalogs(selectedGameName, 2),
                getControllerXInputSwapAnalogs(selectedGameName, 3)
        };
        String box64Version = getBox64Version(selectedGameName);
        String box64Preset = getBox64Preset(selectedGameName);
        boolean enableXInput = getEnableXInput(selectedGameName);
        boolean enableDInput = getEnableDInput(selectedGameName);
    }

    public static class CPUAffinityAdapter implements SpinnerAdapter {
        private final Activity activity;
        private final String[] arrayElements;
        private final Spinner spinner;
        private final int type;
        private final boolean[] checked;

        public CPUAffinityAdapter(Activity activity, String[] arrayElements, Spinner spinner, int type) {
            this.activity = activity;
            this.arrayElements = arrayElements;
            this.spinner = spinner;
            this.type = type;
            this.checked = new boolean[arrayElements.length];
        }

        @Override
        public View getDropDownView(int i, View view, ViewGroup viewGroup) {
            LayoutInflater inflater = activity.getLayoutInflater();
            View pView = inflater.inflate(R.layout.item_checkbox, viewGroup, false);
            CheckBox checkBox = pView.findViewById(R.id.checkbox);

            checked[i] = temporarySettings.cpuAffinity.contains(arrayElements[i]);

            checkBox.setChecked(checked[i]);
            checkBox.setText(arrayElements[i]);
            checkBox.setOnClickListener((v) -> {
                checked[i] = !checked[i];

                StringBuilder builder = new StringBuilder();

                for (int i1 = 0; i1 < checked.length; i1++) {
                    if (checked[i1]) {
                        builder.append(",");
                        builder.append(arrayElements[i1]);
                    }
                }

                if (builder.length() > 0) {
                    builder.deleteCharAt(0);
                }

                if (type == EDIT_GAME_PREFERENCES) {
                    temporarySettings.cpuAffinity = builder.toString();
                } else if (type == FILE_MANAGER_START_PREFERENCES) {
                    selectedCpuAffinity = builder.toString();
                }

                spinner.setAdapter(this);
            });

            return pView;
        }

        @Override
        public void registerDataSetObserver(DataSetObserver dataSetObserver) {
        }

        @Override
        public void unregisterDataSetObserver(DataSetObserver dataSetObserver) {
        }

        @Override
        public int getCount() {
            return arrayElements.length;
        }

        @Override
        public Object getItem(int i) {
            return arrayElements[i];
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            LayoutInflater inflater = activity.getLayoutInflater();
            View pView = view != null ? view : inflater.inflate(android.R.layout.simple_spinner_item, viewGroup, false);
            TextView textView = pView.findViewById(android.R.id.text1);

            textView.setText(temporarySettings.cpuAffinity);

            return pView;
        }

        @Override
        public int getItemViewType(int i) {
            return 0;
        }

        @Override
        public int getViewTypeCount() {
            return 1;
        }

        @Override
        public boolean isEmpty() {
            return arrayElements.length == 0;
        }
    }
}