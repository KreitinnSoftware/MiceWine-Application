package com.micewine.emu.fragments;

import static com.micewine.emu.activities.MainActivity.gson;
import static com.micewine.emu.activities.MainActivity.preferences;
import static com.micewine.emu.activities.PresetManagerActivity.AXIS_HAT_X_MINUS_KEY;
import static com.micewine.emu.activities.PresetManagerActivity.AXIS_HAT_X_PLUS_KEY;
import static com.micewine.emu.activities.PresetManagerActivity.AXIS_HAT_Y_MINUS_KEY;
import static com.micewine.emu.activities.PresetManagerActivity.AXIS_HAT_Y_PLUS_KEY;
import static com.micewine.emu.activities.PresetManagerActivity.AXIS_RZ_MINUS_KEY;
import static com.micewine.emu.activities.PresetManagerActivity.AXIS_RZ_PLUS_KEY;
import static com.micewine.emu.activities.PresetManagerActivity.AXIS_X_MINUS_KEY;
import static com.micewine.emu.activities.PresetManagerActivity.AXIS_X_PLUS_KEY;
import static com.micewine.emu.activities.PresetManagerActivity.AXIS_Y_MINUS_KEY;
import static com.micewine.emu.activities.PresetManagerActivity.AXIS_Y_PLUS_KEY;
import static com.micewine.emu.activities.PresetManagerActivity.AXIS_Z_MINUS_KEY;
import static com.micewine.emu.activities.PresetManagerActivity.AXIS_Z_PLUS_KEY;
import static com.micewine.emu.activities.PresetManagerActivity.BUTTON_A_KEY;
import static com.micewine.emu.activities.PresetManagerActivity.BUTTON_B_KEY;
import static com.micewine.emu.activities.PresetManagerActivity.BUTTON_L1_KEY;
import static com.micewine.emu.activities.PresetManagerActivity.BUTTON_L2_KEY;
import static com.micewine.emu.activities.PresetManagerActivity.BUTTON_R1_KEY;
import static com.micewine.emu.activities.PresetManagerActivity.BUTTON_R2_KEY;
import static com.micewine.emu.activities.PresetManagerActivity.BUTTON_SELECT_KEY;
import static com.micewine.emu.activities.PresetManagerActivity.BUTTON_START_KEY;
import static com.micewine.emu.activities.PresetManagerActivity.BUTTON_THUMBL_KEY;
import static com.micewine.emu.activities.PresetManagerActivity.BUTTON_THUMBR_KEY;
import static com.micewine.emu.activities.PresetManagerActivity.BUTTON_X_KEY;
import static com.micewine.emu.activities.PresetManagerActivity.BUTTON_Y_KEY;
import static com.micewine.emu.activities.PresetManagerActivity.SELECTED_CONTROLLER_PRESET;
import static com.micewine.emu.adapters.AdapterPreset.selectedPresetId;
import static com.micewine.emu.fragments.CreatePresetFragment.CONTROLLER_PRESET;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.reflect.TypeToken;
import com.micewine.emu.R;
import com.micewine.emu.adapters.AdapterPreset;
import com.micewine.emu.controller.XKeyCodes;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

public class ControllerPresetManagerFragment extends Fragment {
    public ControllerPresetManagerFragment(boolean editShortcut) {
        initialize(editShortcut);
    }

    private static RecyclerView recyclerView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_general_settings, container, false);
        recyclerView = rootView.findViewById(R.id.recyclerViewGeneralSettings);

        setAdapter();

        return rootView;
    }

    private void setAdapter() {
        recyclerView.setAdapter(new AdapterPreset(presetListAdapters, requireContext(), requireActivity().getSupportFragmentManager()));

        presetListAdapters.clear();
        presetList.forEach((i) -> presetListAdapters.add(
                new AdapterPreset.Item(i.name, CONTROLLER_PRESET, true)
        ));
    }

    private final static ArrayList<AdapterPreset.Item> presetListAdapters = new ArrayList<>();
    private static ArrayList<ControllerPreset> presetList = new ArrayList<>();
    private static boolean editShortcut = false;
    private final static Type listType = new TypeToken<ArrayList<ControllerPreset>>() {}.getType();

    public static void initialize(boolean editable) {
        editShortcut = editable;
        presetList = getControllerPresets();
    }

    public static int getMouseSensibility(String name) {
        int index = IntStream.range(0, presetList.size()).filter(i -> presetList.get(i).name.equals(name)).findFirst().orElse(-1);
        if (index == -1) return 100;

        return presetList.get(index).mouseSensibility;
    }

    public static void putMouseSensibility(String name, int value) {
        int index = IntStream.range(0, presetList.size()).filter(i -> presetList.get(i).name.equals(name)).findFirst().orElse(-1);
        if (index == -1) return;

        presetList.get(index).mouseSensibility = value;

        saveControllerPresets();
    }

    public static int getDeadZone(String name) {
        int index = IntStream.range(0, presetList.size()).filter(i -> presetList.get(i).name.equals(name)).findFirst().orElse(-1);
        if (index == -1) return 25;

        return presetList.get(index).deadZone;
    }

    public static void putDeadZone(String name, int value) {
        int index = IntStream.range(0, presetList.size()).filter(i -> presetList.get(i).name.equals(name)).findFirst().orElse(-1);
        if (index == -1) return;

        presetList.get(index).deadZone = value;

        saveControllerPresets();
    }

    public static XKeyCodes.ButtonMapping getControllerPreset(String name, String key) {
        int index = IntStream.range(0, presetList.size()).filter(i -> presetList.get(i).name.equals(name)).findFirst().orElse(-1);
        if (index == -1) return null;

        return switch (key) {
            case BUTTON_A_KEY -> presetList.get(index).aButton;
            case BUTTON_B_KEY -> presetList.get(index).bButton;
            case BUTTON_X_KEY -> presetList.get(index).xButton;
            case BUTTON_Y_KEY -> presetList.get(index).yButton;
            case BUTTON_START_KEY -> presetList.get(index).startButton;
            case BUTTON_SELECT_KEY -> presetList.get(index).selectButton;
            case BUTTON_R1_KEY -> presetList.get(index).rbButton;
            case BUTTON_R2_KEY -> presetList.get(index).rtButton;
            case BUTTON_L1_KEY -> presetList.get(index).lbButton;
            case BUTTON_L2_KEY -> presetList.get(index).ltButton;
            case BUTTON_THUMBL_KEY -> presetList.get(index).thumbLButton;
            case BUTTON_THUMBR_KEY -> presetList.get(index).thumbRButton;
            case AXIS_X_PLUS_KEY -> presetList.get(index).axisXPlus;
            case AXIS_X_MINUS_KEY -> presetList.get(index).axisXMinus;
            case AXIS_Y_PLUS_KEY -> presetList.get(index).axisYPlus;
            case AXIS_Y_MINUS_KEY -> presetList.get(index).axisYMinus;
            case AXIS_Z_PLUS_KEY -> presetList.get(index).axisZPlus;
            case AXIS_Z_MINUS_KEY -> presetList.get(index).axisZMinus;
            case AXIS_RZ_PLUS_KEY -> presetList.get(index).axisRZPlus;
            case AXIS_RZ_MINUS_KEY -> presetList.get(index).axisRZMinus;
            case AXIS_HAT_X_PLUS_KEY -> presetList.get(index).axisHatXPlus;
            case AXIS_HAT_X_MINUS_KEY -> presetList.get(index).axisHatXMinus;
            case AXIS_HAT_Y_PLUS_KEY -> presetList.get(index).axisHatYPlus;
            case AXIS_HAT_Y_MINUS_KEY -> presetList.get(index).axisHatYMinus;
            default -> null;
        };
    }

    public static void editControllerPreset(String name, String key, XKeyCodes.ButtonMapping buttonMapping) {
        int index = IntStream.range(0, presetList.size()).filter(i -> presetList.get(i).name.equals(name)).findFirst().orElse(-1);
        if (index == -1) return;

        switch (key) {
            case BUTTON_A_KEY -> presetList.get(index).aButton = buttonMapping;
            case BUTTON_B_KEY -> presetList.get(index).bButton = buttonMapping;
            case BUTTON_X_KEY -> presetList.get(index).xButton = buttonMapping;
            case BUTTON_Y_KEY -> presetList.get(index).yButton = buttonMapping;
            case BUTTON_START_KEY -> presetList.get(index).startButton = buttonMapping;
            case BUTTON_SELECT_KEY -> presetList.get(index).selectButton = buttonMapping;
            case BUTTON_R1_KEY -> presetList.get(index).rbButton = buttonMapping;
            case BUTTON_R2_KEY -> presetList.get(index).rtButton = buttonMapping;
            case BUTTON_L1_KEY -> presetList.get(index).lbButton = buttonMapping;
            case BUTTON_L2_KEY -> presetList.get(index).ltButton = buttonMapping;
            case BUTTON_THUMBL_KEY -> presetList.get(index).thumbLButton = buttonMapping;
            case BUTTON_THUMBR_KEY -> presetList.get(index).thumbRButton = buttonMapping;
            case AXIS_X_PLUS_KEY -> presetList.get(index).axisXPlus = buttonMapping;
            case AXIS_X_MINUS_KEY -> presetList.get(index).axisXMinus = buttonMapping;
            case AXIS_Y_PLUS_KEY -> presetList.get(index).axisYPlus = buttonMapping;
            case AXIS_Y_MINUS_KEY -> presetList.get(index).axisYMinus = buttonMapping;
            case AXIS_Z_PLUS_KEY -> presetList.get(index).axisZPlus = buttonMapping;
            case AXIS_Z_MINUS_KEY -> presetList.get(index).axisZMinus = buttonMapping;
            case AXIS_RZ_PLUS_KEY -> presetList.get(index).axisRZPlus = buttonMapping;
            case AXIS_RZ_MINUS_KEY -> presetList.get(index).axisRZMinus = buttonMapping;
            case AXIS_HAT_X_PLUS_KEY -> presetList.get(index).axisHatXPlus = buttonMapping;
            case AXIS_HAT_X_MINUS_KEY -> presetList.get(index).axisHatXMinus = buttonMapping;
            case AXIS_HAT_Y_PLUS_KEY -> presetList.get(index).axisHatYPlus = buttonMapping;
            case AXIS_HAT_Y_MINUS_KEY -> presetList.get(index).axisHatYMinus = buttonMapping;
        }

        saveControllerPresets();
    }

    public static void addControllerPreset(Context context, String name) {
        int index = IntStream.range(0, presetList.size()).filter(i -> presetList.get(i).name.equals(name)).findFirst().orElse(-1);
        if (index != -1) {
            Toast.makeText(context, R.string.executable_already_added, Toast.LENGTH_SHORT).show();
            return;
        }

        presetList.add(
                new ControllerPreset(name)
        );
        presetListAdapters.add(
                new AdapterPreset.Item(name, CONTROLLER_PRESET, true, editShortcut)
        );

        saveControllerPresets();

        if (recyclerView == null) return;
        RecyclerView.Adapter<?> adapter = recyclerView.getAdapter();
        if (adapter != null) {
            adapter.notifyItemInserted(presetList.size());
        }
    }

    public static void deleteControllerPreset(String name) {
        int index = IntStream.range(0, presetList.size()).filter(i -> presetList.get(i).name.equals(name)).findFirst().orElse(-1);
        if (index == -1) return;

        presetList.remove(index);
        presetListAdapters.remove(index);

        saveControllerPresets();

        if (index == selectedPresetId && !presetList.isEmpty()) {
            SharedPreferences.Editor editor = preferences.edit();

            editor.putString(SELECTED_CONTROLLER_PRESET, presetList.get(0).name);
            editor.apply();
        }

        if (recyclerView == null) return;

        RecyclerView.Adapter<?> adapter = recyclerView.getAdapter();
        if (adapter != null) {
            adapter.notifyItemRemoved(index);
            if (!presetList.isEmpty()) {
                adapter.notifyItemChanged(0);
            }
        }
    }

    public static void renameControllerPreset(String name, String newName) {
        int index = IntStream.range(0, presetList.size()).filter(i -> presetList.get(i).name.equals(name)).findFirst().orElse(-1);
        if (index == -1) return;

        presetList.get(index).name = newName;
        presetListAdapters.get(index).titleSettings = newName;

        saveControllerPresets();

        if (recyclerView == null) return;

        recyclerView.post(() -> {
            RecyclerView.Adapter<?> adapter = recyclerView.getAdapter();
            if (adapter != null) {
                adapter.notifyItemChanged(index);
            }
        });
    }

    public static boolean importControllerPreset(File file) {
        List<String> lines;

        try {
            lines = Files.readAllLines(file.toPath());
        } catch (IOException ignored) {
            return false;
        }

        if (lines.size() < 2) return false;

        String type = lines.get(0);
        if (!type.equals("controllerPreset")) return false;

        String json = lines.get(1);
        ControllerPreset preset = gson.fromJson(json, listType);

        String presetName = preset.name;
        int count = 1;

        while (true) {
            String currentName = presetName;
            if (presetList.stream().anyMatch(p -> p.name.equals(currentName))) {
                presetName = preset.name + "-" + count++;
                continue;
            }
            break;
        }

        preset.name = presetName;

        presetList.add(preset);
        presetListAdapters.add(
                new AdapterPreset.Item(preset.name, CONTROLLER_PRESET, true)
        );

        saveControllerPresets();

        if (recyclerView == null) return true;

        recyclerView.post(() -> {
            RecyclerView.Adapter<?> adapter = recyclerView.getAdapter();
            if (adapter != null) {
                adapter.notifyItemInserted(presetList.size());
            }
        });

        return true;
    }

    public static void exportControllerPreset(String name, File file) {
        int index = IntStream.range(0, presetList.size()).filter(i -> presetList.get(i).name.equals(name)).findFirst().orElse(-1);
        if (index == -1) return;

        try (FileWriter writer = new FileWriter(file)) {
            writer.write("controllerPreset\n" + gson.toJson(presetList.get(index)));
        } catch (Exception ignored) {
        }
    }

    private static void saveControllerPresets() {
        SharedPreferences.Editor editor = preferences.edit();

        editor.putString("controllerPresetList", gson.toJson(presetList));
        editor.apply();
    }

    public static ArrayList<ControllerPreset> getControllerPresets() {
        String json = preferences.getString("controllerPresetList", "");
        Type listType = new TypeToken<ArrayList<ControllerPreset>>() {}.getType();
        ArrayList<ControllerPreset> controllerPresetList = gson.fromJson(json, listType);

        return controllerPresetList != null ? controllerPresetList : new ArrayList<>();
    }


    public static class ControllerPreset {
        String name;
        XKeyCodes.ButtonMapping aButton = new XKeyCodes.ButtonMapping();
        XKeyCodes.ButtonMapping bButton = new XKeyCodes.ButtonMapping();
        XKeyCodes.ButtonMapping xButton = new XKeyCodes.ButtonMapping();
        XKeyCodes.ButtonMapping yButton = new XKeyCodes.ButtonMapping();
        XKeyCodes.ButtonMapping startButton = new XKeyCodes.ButtonMapping();
        XKeyCodes.ButtonMapping selectButton = new XKeyCodes.ButtonMapping();
        XKeyCodes.ButtonMapping rbButton = new XKeyCodes.ButtonMapping();
        XKeyCodes.ButtonMapping rtButton = new XKeyCodes.ButtonMapping();
        XKeyCodes.ButtonMapping lbButton = new XKeyCodes.ButtonMapping();
        XKeyCodes.ButtonMapping ltButton = new XKeyCodes.ButtonMapping();
        XKeyCodes.ButtonMapping thumbLButton = new XKeyCodes.ButtonMapping();
        XKeyCodes.ButtonMapping thumbRButton = new XKeyCodes.ButtonMapping();
        XKeyCodes.ButtonMapping axisXPlus = new XKeyCodes.ButtonMapping();
        XKeyCodes.ButtonMapping axisXMinus = new XKeyCodes.ButtonMapping();
        XKeyCodes.ButtonMapping axisYPlus = new XKeyCodes.ButtonMapping();
        XKeyCodes.ButtonMapping axisYMinus = new XKeyCodes.ButtonMapping();
        XKeyCodes.ButtonMapping axisZPlus = new XKeyCodes.ButtonMapping();
        XKeyCodes.ButtonMapping axisZMinus = new XKeyCodes.ButtonMapping();
        XKeyCodes.ButtonMapping axisRZPlus = new XKeyCodes.ButtonMapping();
        XKeyCodes.ButtonMapping axisRZMinus = new XKeyCodes.ButtonMapping();
        XKeyCodes.ButtonMapping axisHatXPlus = new XKeyCodes.ButtonMapping();
        XKeyCodes.ButtonMapping axisHatXMinus = new XKeyCodes.ButtonMapping();
        XKeyCodes.ButtonMapping axisHatYPlus = new XKeyCodes.ButtonMapping();
        XKeyCodes.ButtonMapping axisHatYMinus = new XKeyCodes.ButtonMapping();
        int deadZone = 25;
        int mouseSensibility = 100;

        public ControllerPreset(String name) {
            this.name = name;
        }

        public void putName(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }
}