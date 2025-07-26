package com.micewine.emu.fragments;

import static com.micewine.emu.activities.MainActivity.getNativeResolution;
import static com.micewine.emu.activities.MainActivity.gson;
import static com.micewine.emu.activities.MainActivity.preferences;
import static com.micewine.emu.activities.PresetManagerActivity.SELECTED_VIRTUAL_CONTROLLER_PRESET;
import static com.micewine.emu.adapters.AdapterPreset.selectedPresetId;
import static com.micewine.emu.fragments.CreatePresetFragment.VIRTUAL_CONTROLLER_PRESET;
import static com.micewine.emu.views.VirtualKeyboardInputCreatorView.GRID_SIZE;

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
import com.micewine.emu.views.VirtualKeyboardInputView;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

public class VirtualControllerPresetManagerFragment extends Fragment {
    public static boolean editShortcut;

    public VirtualControllerPresetManagerFragment(boolean editShortcut) {
        initialize(editShortcut);
    }

    private static RecyclerView recyclerView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_general_settings, container, false);
        recyclerView = rootView.findViewById(R.id.recyclerViewGeneralSettings);

        initialize(editShortcut);
        setAdapter();

        return rootView;
    }

    private void setAdapter() {
        recyclerView.setAdapter(new AdapterPreset(presetsAdapterList, requireContext(), requireActivity().getSupportFragmentManager()));

        presetsAdapterList.clear();
        presetsList.forEach((i) -> addToAdapter(i.name));
    }

    private void addToAdapter(String name) {
        presetsAdapterList.add(
                new AdapterPreset.Item(name, VIRTUAL_CONTROLLER_PRESET, true, false)
        );
    }

    private static ArrayList<VirtualControllerPreset> presetsList;
    private static final ArrayList<AdapterPreset.Item> presetsAdapterList = new ArrayList<>();
    private final static Type listType = new TypeToken<VirtualControllerPreset>() {}.getType();

    public static void initialize(boolean editable) {
        presetsList = getVirtualControllerPresets();
        editShortcut = editable;
    }

    public static VirtualControllerPreset getVirtualControllerPreset(String name) {
        int index = IntStream.range(0, presetsList.size()).filter(i -> presetsList.get(i).name.equals(name)).findFirst().orElse(-1);
        if (index == -1) return null;

        return presetsList.get(index);
    }

    public static void putVirtualControllerPreset(String name, String resolution,
                                                  ArrayList<VirtualKeyboardInputView.VirtualButton> buttonList,
                                                  ArrayList<VirtualKeyboardInputView.VirtualAnalog> analogList,
                                                  ArrayList<VirtualKeyboardInputView.VirtualDPad> dpadList
    ) {
        int index = IntStream.range(0, presetsList.size()).filter(i -> presetsList.get(i).name.equals(name)).findFirst().orElse(-1);
        if (index == -1) return;

        presetsList.set(index, new VirtualControllerPreset(name, resolution));

        buttonList.forEach((i) -> presetsList.get(index).buttons.add(i));
        analogList.forEach((i) -> presetsList.get(index).analogs.add(i));
        dpadList.forEach((i) -> presetsList.get(index).dpads.add(i));

        saveVirtualControllerPresets();
    }

    public static void addVirtualControllerPreset(Context context, String name) {
        boolean alreadyExists = presetsList.stream().anyMatch(i -> i.name.equals(name));
        if (alreadyExists) {
            Toast.makeText(context, context.getString(R.string.executable_already_added), Toast.LENGTH_SHORT).show();
            return;
        }

        presetsList.add(
                new VirtualControllerPreset(name)
        );
        presetsAdapterList.add(
                new AdapterPreset.Item(name, VIRTUAL_CONTROLLER_PRESET, true, editShortcut)
        );

        recyclerView.post(() -> {
            RecyclerView.Adapter<?> adapter = recyclerView.getAdapter();
            if (adapter != null) {
                adapter.notifyItemInserted(presetsList.size());
            }
        });

        saveVirtualControllerPresets();
    }

    public static void deleteVirtualControllerPreset(String name) {
        int index = IntStream.range(0, presetsList.size()).filter(i -> presetsList.get(i).name.equals(name)).findFirst().orElse(-1);

        presetsList.remove(index);
        presetsAdapterList.remove(index);

        if (index == selectedPresetId) {
            SharedPreferences.Editor editor = preferences.edit();

            editor.putString(SELECTED_VIRTUAL_CONTROLLER_PRESET, presetsList.get(0).name);
            editor.apply();
        }

        recyclerView.post(() -> {
            RecyclerView.Adapter<?> adapter = recyclerView.getAdapter();
            if (adapter != null) {
                adapter.notifyItemRemoved(index);

                if (index == selectedPresetId) {
                    adapter.notifyItemChanged(0);
                }
            }
        });

        saveVirtualControllerPresets();
    }

    public static void renameVirtualControllerPreset(String name, String newName) {
        int index = IntStream.range(0, presetsList.size()).filter(i -> presetsList.get(i).name.equals(name)).findFirst().orElse(-1);

        presetsList.get(index).name = newName;
        presetsAdapterList.get(index).setTitleSettings(newName);

        recyclerView.post(() -> {
            RecyclerView.Adapter<?> adapter = recyclerView.getAdapter();
            if (adapter != null) {
                adapter.notifyItemChanged(index);
            }
        });

        saveVirtualControllerPresets();
    }

    public static boolean importVirtualControllerPreset(Context context, File file) {
        List<String> lines;
        try {
            lines = Files.readAllLines(file.toPath());
        } catch (IOException e) {
            return false;
        }

        if (lines.size() < 2) return false;

        String type = lines.get(0);

        if (!type.equals("virtualControllerPreset")) return false;

        String json = lines.get(1);

        VirtualControllerPreset preset = gson.fromJson(json, listType);
        boolean autoAdjust = json.contains("resolution");

        String presetName = preset.name;
        int count = 1;

        while (true) {
            String currentName = presetName;
            if (presetsList.stream().anyMatch(p -> p.name.equals(currentName))) {
                presetName = preset.name + "-" + count++;
                continue;
            }
            break;
        }

        preset.name = presetName;

        if (autoAdjust && !preset.resolution.isEmpty()) {
            String nativeResolution = getNativeResolution(context);

            if (!nativeResolution.equals(preset.resolution)) {
                String[] nativeResolutionSplit = nativeResolution.split("x");
                String[] presetResolutionSplit = preset.resolution.split("x");

                float nativeResolutionWidth = Float.parseFloat(nativeResolutionSplit[0]);
                float nativeResolutionHeight = Float.parseFloat(nativeResolutionSplit[1]);

                float presetResolutionWidth = Float.parseFloat(presetResolutionSplit[0]);
                float presetResolutionHeight = Float.parseFloat(presetResolutionSplit[1]);

                float multiplierWidth = (nativeResolutionWidth / presetResolutionWidth) * 100F;
                float multiplierHeight = (nativeResolutionHeight / presetResolutionHeight) * 100F;

                preset.buttons.forEach((i) -> {
                    i.x = Math.round(i.x / 100F * multiplierWidth / GRID_SIZE) * (float) GRID_SIZE;
                    i.y = Math.round(i.y / 100F * multiplierHeight / GRID_SIZE) * (float) GRID_SIZE;
                });
                preset.analogs.forEach((i) -> {
                    i.x = Math.round(i.x / 100F * multiplierWidth / GRID_SIZE) * (float) GRID_SIZE;
                    i.y = Math.round(i.y / 100F * multiplierHeight / GRID_SIZE) * (float) GRID_SIZE;
                });
                preset.dpads.forEach((i) -> {
                    i.x = Math.round(i.x / 100F * multiplierWidth / GRID_SIZE) * (float) GRID_SIZE;
                    i.y = Math.round(i.y / 100F * multiplierHeight / GRID_SIZE) * (float) GRID_SIZE;
                });
            }
        }

        presetsList.add(preset);
        presetsAdapterList.add(
                new AdapterPreset.Item(preset.name, VIRTUAL_CONTROLLER_PRESET, true, false)
        );

        if (recyclerView != null) {
            recyclerView.post(() -> {
                RecyclerView.Adapter<?> adapter = recyclerView.getAdapter();
                if (adapter != null) {
                    adapter.notifyItemInserted(presetsList.size());
                }
            });
        }

        saveVirtualControllerPresets();

        return true;
    }

    public static void exportVirtualControllerPreset(String name, File file) {
        int index = IntStream.range(0, presetsList.size()).filter(i -> presetsList.get(i).name.equals(name)).findFirst().orElse(-1);

        try (FileWriter writer = new FileWriter(file)) {
            writer.write("virtualControllerPreset\n" + gson.toJson(presetsList.get(index)));
        } catch (IOException ignored) {
        }
    }

    private static void saveVirtualControllerPresets() {
        SharedPreferences.Editor editor = preferences.edit();

        editor.putString("virtualControllerPresetList", gson.toJson(presetsList));
        editor.apply();
    }

    public static ArrayList<VirtualControllerPreset> getVirtualControllerPresets() {
        String json = preferences.getString("virtualControllerPresetList", "");
        Type listType = new TypeToken<ArrayList<VirtualControllerPreset>>() {}.getType();
        ArrayList<VirtualControllerPreset> loadedList = gson.fromJson(json, listType);

        return (loadedList != null ? loadedList : new ArrayList<>());
    }

    public static class VirtualControllerPreset {
        public String name;
        public String resolution;
        public ArrayList<VirtualKeyboardInputView.VirtualAnalog> analogs;
        public ArrayList<VirtualKeyboardInputView.VirtualButton> buttons;
        public ArrayList<VirtualKeyboardInputView.VirtualDPad> dpads;

        public VirtualControllerPreset(String name) {
            this.name = name;
            this.buttons = new ArrayList<>();
            this.analogs = new ArrayList<>();
            this.dpads = new ArrayList<>();
        }

        public VirtualControllerPreset(String name, String resolution) {
            this.name = name;
            this.resolution = resolution;
            this.buttons = new ArrayList<>();
            this.analogs = new ArrayList<>();
            this.dpads = new ArrayList<>();
        }

        public String getName() {
            return name;
        }
    }
}