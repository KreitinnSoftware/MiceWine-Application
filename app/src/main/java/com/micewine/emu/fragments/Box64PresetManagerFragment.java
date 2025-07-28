package com.micewine.emu.fragments;

import static com.micewine.emu.activities.GeneralSettingsActivity.BOX64_AVX_DEFAULT_VALUE;
import static com.micewine.emu.activities.GeneralSettingsActivity.BOX64_DYNAREC_ALIGNED_ATOMICS_DEFAULT_VALUE;
import static com.micewine.emu.activities.GeneralSettingsActivity.BOX64_DYNAREC_BIGBLOCK_DEFAULT_VALUE;
import static com.micewine.emu.activities.GeneralSettingsActivity.BOX64_DYNAREC_CALLRET_DEFAULT_VALUE;
import static com.micewine.emu.activities.GeneralSettingsActivity.BOX64_DYNAREC_DF_DEFAULT_VALUE;
import static com.micewine.emu.activities.GeneralSettingsActivity.BOX64_DYNAREC_DIRTY_DEFAULT_VALUE;
import static com.micewine.emu.activities.GeneralSettingsActivity.BOX64_DYNAREC_FASTNAN_DEFAULT_VALUE;
import static com.micewine.emu.activities.GeneralSettingsActivity.BOX64_DYNAREC_FASTROUND_DEFAULT_VALUE;
import static com.micewine.emu.activities.GeneralSettingsActivity.BOX64_DYNAREC_FORWARD_DEFAULT_VALUE;
import static com.micewine.emu.activities.GeneralSettingsActivity.BOX64_DYNAREC_NATIVEFLAGS_DEFAULT_VALUE;
import static com.micewine.emu.activities.GeneralSettingsActivity.BOX64_DYNAREC_PAUSE_DEFAULT_VALUE;
import static com.micewine.emu.activities.GeneralSettingsActivity.BOX64_DYNAREC_SAFEFLAGS_DEFAULT_VALUE;
import static com.micewine.emu.activities.GeneralSettingsActivity.BOX64_DYNAREC_STRONGMEM_DEFAULT_VALUE;
import static com.micewine.emu.activities.GeneralSettingsActivity.BOX64_DYNAREC_WAIT_DEFAULT_VALUE;
import static com.micewine.emu.activities.GeneralSettingsActivity.BOX64_DYNAREC_WEAKBARRIER_DEFAULT_VALUE;
import static com.micewine.emu.activities.GeneralSettingsActivity.BOX64_DYNAREC_X87DOUBLE_DEFAULT_VALUE;
import static com.micewine.emu.activities.GeneralSettingsActivity.BOX64_MMAP32_DEFAULT_VALUE;
import static com.micewine.emu.activities.GeneralSettingsActivity.BOX64_SSE42_DEFAULT_VALUE;
import static com.micewine.emu.activities.MainActivity.gson;
import static com.micewine.emu.activities.MainActivity.preferences;
import static com.micewine.emu.activities.PresetManagerActivity.SELECTED_BOX64_PRESET;
import static com.micewine.emu.adapters.AdapterPreset.selectedPresetId;
import static com.micewine.emu.fragments.CreatePresetFragment.BOX64_PRESET;

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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

public class Box64PresetManagerFragment extends Fragment {
    public static RecyclerView recyclerView;

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
                new AdapterPreset.Item(i.name, BOX64_PRESET, true)
        ));
    }

    private static final ArrayList<AdapterPreset.Item> presetListAdapters = new ArrayList<>();
    private static ArrayList<Box64Preset> presetList = new ArrayList<>();
    private static final Type listTypeV1 = new TypeToken<List<String>>() {}.getType();
    private static final Type listTypeV2 = new TypeToken<Box64Preset>() {}.getType();

    public static void initialize() {
        presetList = getBox64Presets();
    }

    public static void addBox64Preset(Context context, String name) {
        int index = IntStream.range(0, presetList.size()).filter(i -> presetList.get(i).name.equals(name)).findFirst().orElse(-1);
        if (index != -1) {
            Toast.makeText(context, R.string.executable_already_added, Toast.LENGTH_SHORT).show();
            return;
        }

        presetList.add(
                new Box64Preset(name)
        );
        presetListAdapters.add(
                new AdapterPreset.Item(name, BOX64_PRESET, true)
        );

        saveBox64Presets();

        if (recyclerView == null) return;
        RecyclerView.Adapter<?> adapter = recyclerView.getAdapter();
        if (adapter != null) {
            adapter.notifyItemInserted(presetList.size() - 1);
        }
    }

    public static boolean deleteBox64Preset(String name) {
        int index = IntStream.range(0, presetList.size()).filter(i -> presetList.get(i).name.equals(name)).findFirst().orElse(-1);
        if (index == -1 || presetList.size() == 1) return false;

        presetList.remove(index);
        presetListAdapters.remove(index);

        if (index == selectedPresetId && !presetList.isEmpty()) {
            SharedPreferences.Editor editor = preferences.edit();

            editor.putString(SELECTED_BOX64_PRESET, presetList.get(0).name);
            editor.apply();
        }

        saveBox64Presets();

        if (recyclerView == null) return true;
        RecyclerView.Adapter<?> adapter = recyclerView.getAdapter();
        if (adapter != null) {
            adapter.notifyItemRemoved(index);
            adapter.notifyItemChanged(0);
        }

        return true;
    }

    public static void renameBox64Preset(String name, String newName) {
        int index = IntStream.range(0, presetList.size()).filter(i -> presetList.get(i).name.equals(name)).findFirst().orElse(-1);
        if (index == -1) return;

        presetList.get(index).name = newName;
        presetListAdapters.get(index).titleSettings = newName;

        saveBox64Presets();

        if (recyclerView == null) return;

        recyclerView.post(() -> {
            RecyclerView.Adapter<?> adapter = recyclerView.getAdapter();
            if (adapter != null) {
                adapter.notifyItemChanged(index);
            }
        });
    }

    public static void putBox64BigBlock(String name, int value) {
        int index = IntStream.range(0, presetList.size()).filter(i -> presetList.get(i).name.equals(name)).findFirst().orElse(-1);
        if (index == -1) return;

        presetList.get(index).box64BigBlock = value;
        saveBox64Presets();
    }

    public static int getBox64BigBlock(String name) {
        int index = IntStream.range(0, presetList.size()).filter(i -> presetList.get(i).name.equals(name)).findFirst().orElse(-1);
        if (index == -1) return BOX64_DYNAREC_BIGBLOCK_DEFAULT_VALUE;

        return presetList.get(index).box64BigBlock;
    }

    public static void putBox64MMap32(String name, boolean value) {
        int index = IntStream.range(0, presetList.size()).filter(i -> presetList.get(i).name.equals(name)).findFirst().orElse(-1);
        if (index == -1) return;

        presetList.get(index).box64MMap32 = boolToInt(value);
        saveBox64Presets();
    }

    public static boolean getBox64MMap32(String name) {
        int index = IntStream.range(0, presetList.size()).filter(i -> presetList.get(i).name.equals(name)).findFirst().orElse(-1);
        if (index == -1) return intToBool(BOX64_MMAP32_DEFAULT_VALUE);

        return intToBool(presetList.get(index).box64MMap32);
    }

    public static void putBox64Avx(String name, int value) {
        int index = IntStream.range(0, presetList.size()).filter(i -> presetList.get(i).name.equals(name)).findFirst().orElse(-1);
        if (index == -1) return;

        presetList.get(index).box64Avx = value;
        saveBox64Presets();
    }

    public static int getBox64Avx(String name) {
        int index = IntStream.range(0, presetList.size()).filter(i -> presetList.get(i).name.equals(name)).findFirst().orElse(-1);
        if (index == -1) return BOX64_AVX_DEFAULT_VALUE;

        return presetList.get(index).box64Avx;
    }

    public static void putBox64Sse42(String name, boolean value) {
        int index = IntStream.range(0, presetList.size()).filter(i -> presetList.get(i).name.equals(name)).findFirst().orElse(-1);
        if (index == -1) return;

        presetList.get(index).box64Sse42 = boolToInt(value);
        saveBox64Presets();
    }

    public static boolean getBox64Sse42(String name) {
        int index = IntStream.range(0, presetList.size()).filter(i -> presetList.get(i).name.equals(name)).findFirst().orElse(-1);
        if (index == -1) return intToBool(BOX64_SSE42_DEFAULT_VALUE);

        return intToBool(presetList.get(index).box64Sse42);
    }

    public static void putBox64StrongMem(String name, int value) {
        int index = IntStream.range(0, presetList.size()).filter(i -> presetList.get(i).name.equals(name)).findFirst().orElse(-1);
        if (index == -1) return;

        presetList.get(index).box64StrongMem = value;
        saveBox64Presets();
    }

    public static int getBox64StrongMem(String name) {
        int index = IntStream.range(0, presetList.size()).filter(i -> presetList.get(i).name.equals(name)).findFirst().orElse(-1);
        if (index == -1) return BOX64_DYNAREC_STRONGMEM_DEFAULT_VALUE;

        return presetList.get(index).box64StrongMem;
    }

    public static void putBox64WeakBarrier(String name, int value) {
        int index = IntStream.range(0, presetList.size()).filter(i -> presetList.get(i).name.equals(name)).findFirst().orElse(-1);
        if (index == -1) return;

        presetList.get(index).box64WeakBarrier = value;
        saveBox64Presets();
    }

    public static int getBox64WeakBarrier(String name) {
        int index = IntStream.range(0, presetList.size()).filter(i -> presetList.get(i).name.equals(name)).findFirst().orElse(-1);
        if (index == -1) return BOX64_DYNAREC_WEAKBARRIER_DEFAULT_VALUE;

        return presetList.get(index).box64WeakBarrier;
    }

    public static void putBox64Pause(String name, int value) {
        int index = IntStream.range(0, presetList.size()).filter(i -> presetList.get(i).name.equals(name)).findFirst().orElse(-1);
        if (index == -1) return;

        presetList.get(index).box64Pause = value;
        saveBox64Presets();
    }

    public static int getBox64Pause(String name) {
        int index = IntStream.range(0, presetList.size()).filter(i -> presetList.get(i).name.equals(name)).findFirst().orElse(-1);
        if (index == -1) return BOX64_DYNAREC_PAUSE_DEFAULT_VALUE;

        return presetList.get(index).box64Pause;
    }

    public static void putBox64X87Double(String name, int value) {
        int index = IntStream.range(0, presetList.size()).filter(i -> presetList.get(i).name.equals(name)).findFirst().orElse(-1);
        if (index == -1) return;

        presetList.get(index).box64X87Double = value;
        saveBox64Presets();
    }

    public static int getBox64X87Double(String name) {
        int index = IntStream.range(0, presetList.size()).filter(i -> presetList.get(i).name.equals(name)).findFirst().orElse(-1);
        if (index == -1) return BOX64_DYNAREC_X87DOUBLE_DEFAULT_VALUE;

        return presetList.get(index).box64X87Double;
    }

    public static void putBox64FastNan(String name, boolean value) {
        int index = IntStream.range(0, presetList.size()).filter(i -> presetList.get(i).name.equals(name)).findFirst().orElse(-1);
        if (index == -1) return;

        presetList.get(index).box64FastNan = boolToInt(value);
        saveBox64Presets();
    }

    public static boolean getBox64FastNan(String name) {
        int index = IntStream.range(0, presetList.size()).filter(i -> presetList.get(i).name.equals(name)).findFirst().orElse(-1);
        if (index == -1) return intToBool(BOX64_DYNAREC_FASTNAN_DEFAULT_VALUE);

        return intToBool(presetList.get(index).box64FastNan);
    }

    public static void putBox64FastRound(String name, boolean value) {
        int index = IntStream.range(0, presetList.size()).filter(i -> presetList.get(i).name.equals(name)).findFirst().orElse(-1);
        if (index == -1) return;

        presetList.get(index).box64FastRound = boolToInt(value);
        saveBox64Presets();
    }

    public static boolean getBox64FastRound(String name) {
        int index = IntStream.range(0, presetList.size()).filter(i -> presetList.get(i).name.equals(name)).findFirst().orElse(-1);
        if (index == -1) return intToBool(BOX64_DYNAREC_FASTROUND_DEFAULT_VALUE);

        return intToBool(presetList.get(index).box64FastRound);
    }

    public static void putBox64SafeFlags(String name, int value) {
        int index = IntStream.range(0, presetList.size()).filter(i -> presetList.get(i).name.equals(name)).findFirst().orElse(-1);
        if (index == -1) return;

        presetList.get(index).box64SafeFlags = value;
        saveBox64Presets();
    }

    public static int getBox64SafeFlags(String name) {
        int index = IntStream.range(0, presetList.size()).filter(i -> presetList.get(i).name.equals(name)).findFirst().orElse(-1);
        if (index == -1) return BOX64_DYNAREC_SAFEFLAGS_DEFAULT_VALUE;

        return presetList.get(index).box64SafeFlags;
    }

    public static void putBox64CallRet(String name, int value) {
        int index = IntStream.range(0, presetList.size()).filter(i -> presetList.get(i).name.equals(name)).findFirst().orElse(-1);
        if (index == -1) return;

        presetList.get(index).box64CallRet = value;
        saveBox64Presets();
    }

    public static int getBox64CallRet(String name) {
        int index = IntStream.range(0, presetList.size()).filter(i -> presetList.get(i).name.equals(name)).findFirst().orElse(-1);
        if (index == -1) return BOX64_DYNAREC_CALLRET_DEFAULT_VALUE;

        return presetList.get(index).box64CallRet;
    }

    public static void putBox64AlignedAtomics(String name, boolean value) {
        int index = IntStream.range(0, presetList.size()).filter(i -> presetList.get(i).name.equals(name)).findFirst().orElse(-1);
        if (index == -1) return;

        presetList.get(index).box64AlignedAtomics = boolToInt(value);
        saveBox64Presets();
    }

    public static boolean getBox64AlignedAtomics(String name) {
        int index = IntStream.range(0, presetList.size()).filter(i -> presetList.get(i).name.equals(name)).findFirst().orElse(-1);
        if (index == -1) return intToBool(BOX64_DYNAREC_ALIGNED_ATOMICS_DEFAULT_VALUE);

        return intToBool(presetList.get(index).box64AlignedAtomics);
    }

    public static void putBox64NativeFlags(String name, boolean value) {
        int index = IntStream.range(0, presetList.size()).filter(i -> presetList.get(i).name.equals(name)).findFirst().orElse(-1);
        if (index == -1) return;

        presetList.get(index).box64NativeFlags = boolToInt(value);
        saveBox64Presets();
    }

    public static boolean getBox64NativeFlags(String name) {
        int index = IntStream.range(0, presetList.size()).filter(i -> presetList.get(i).name.equals(name)).findFirst().orElse(-1);
        if (index == -1) return intToBool(BOX64_DYNAREC_NATIVEFLAGS_DEFAULT_VALUE);

        return intToBool(presetList.get(index).box64NativeFlags);
    }

    public static void putBox64Wait(String name, boolean value) {
        int index = IntStream.range(0, presetList.size()).filter(i -> presetList.get(i).name.equals(name)).findFirst().orElse(-1);
        if (index == -1) return;

        presetList.get(index).box64Wait = boolToInt(value);
        saveBox64Presets();
    }

    public static boolean getBox64Wait(String name) {
        int index = IntStream.range(0, presetList.size()).filter(i -> presetList.get(i).name.equals(name)).findFirst().orElse(-1);
        if (index == -1) return intToBool(BOX64_DYNAREC_WAIT_DEFAULT_VALUE);

        return intToBool(presetList.get(index).box64Wait);
    }

    public static void putBox64Dirty(String name, int value) {
        int index = IntStream.range(0, presetList.size()).filter(i -> presetList.get(i).name.equals(name)).findFirst().orElse(-1);
        if (index == -1) return;

        presetList.get(index).box64Dirty = value;
        saveBox64Presets();
    }

    public static int getBox64Dirty(String name) {
        int index = IntStream.range(0, presetList.size()).filter(i -> presetList.get(i).name.equals(name)).findFirst().orElse(-1);
        if (index == -1) return BOX64_DYNAREC_DIRTY_DEFAULT_VALUE;

        return presetList.get(index).box64Dirty;
    }

    public static void putBox64Forward(String name, int value) {
        int index = IntStream.range(0, presetList.size()).filter(i -> presetList.get(i).name.equals(name)).findFirst().orElse(-1);
        if (index == -1) return;

        presetList.get(index).box64Forward = value;
        saveBox64Presets();
    }

    public static int getBox64Forward(String name) {
        int index = IntStream.range(0, presetList.size()).filter(i -> presetList.get(i).name.equals(name)).findFirst().orElse(-1);
        if (index == -1) return BOX64_DYNAREC_FORWARD_DEFAULT_VALUE;

        return presetList.get(index).box64Forward;
    }

    public static void putBox64DF(String name, boolean value) {
        int index = IntStream.range(0, presetList.size()).filter(i -> presetList.get(i).name.equals(name)).findFirst().orElse(-1);
        if (index == -1) return;

        presetList.get(index).box64DF = boolToInt(value);
        saveBox64Presets();
    }

    public static boolean getBox64DF(String name) {
        int index = IntStream.range(0, presetList.size()).filter(i -> presetList.get(i).name.equals(name)).findFirst().orElse(-1);
        if (index == -1) return intToBool(BOX64_DYNAREC_DF_DEFAULT_VALUE);

        return intToBool(presetList.get(index).box64DF);
    }

    private static Box64Preset v1PresetToV2(List<String> v1Preset) {
        Box64Preset preset = new Box64Preset(v1Preset.get(0));

        preset.box64MMap32 = boolToInt(Boolean.parseBoolean(v1Preset.get(1)));
        preset.box64Avx = Integer.parseInt(v1Preset.get(2));
        preset.box64Sse42 = boolToInt(Boolean.parseBoolean(v1Preset.get(3)));
        preset.box64BigBlock = Integer.parseInt(v1Preset.get(4));
        preset.box64StrongMem = Integer.parseInt(v1Preset.get(5));
        preset.box64WeakBarrier = Integer.parseInt(v1Preset.get(6));
        preset.box64Pause = Integer.parseInt(v1Preset.get(7));
        preset.box64X87Double = boolToInt(Boolean.parseBoolean(v1Preset.get(8)));
        preset.box64FastNan = boolToInt(Boolean.parseBoolean(v1Preset.get(9)));
        preset.box64FastRound = boolToInt(Boolean.parseBoolean(v1Preset.get(10)));
        preset.box64SafeFlags = Integer.parseInt(v1Preset.get(11));
        preset.box64CallRet = boolToInt(Boolean.parseBoolean(v1Preset.get(12)));
        preset.box64AlignedAtomics = boolToInt(Boolean.parseBoolean(v1Preset.get(13)));
        preset.box64NativeFlags = boolToInt(Boolean.parseBoolean(v1Preset.get(14)));
        preset.box64Wait = boolToInt(Boolean.parseBoolean(v1Preset.get(15)));
        preset.box64Dirty = boolToInt(Boolean.parseBoolean(v1Preset.get(16)));
        preset.box64Forward = Integer.parseInt(v1Preset.get(17));

        return preset;
    }

    public static boolean importBox64Preset(File file) {
        List<String> lines;
        try {
            lines = Files.readAllLines(file.toPath());
        } catch (IOException e) {
            return false;
        }

        if (lines.size() < 2) return false;

        String type = lines.get(0);
        String json = lines.get(1);

        Box64Preset preset = switch (type) {
            case "box64Preset" -> v1PresetToV2(gson.fromJson(json, listTypeV1));
            case "box64PresetV2" -> gson.fromJson(json, listTypeV2);
            default -> null;
        };

        if (preset == null) return false;

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
                new AdapterPreset.Item(preset.name, BOX64_PRESET, true)
        );

        if (recyclerView != null) {
            recyclerView.post(() -> {
                RecyclerView.Adapter<?> adapter = recyclerView.getAdapter();
                if (adapter != null) {
                    adapter.notifyItemInserted(presetList.size());
                }
            });
        }

        saveBox64Presets();

        return true;
    }

    public static void exportBox64Preset(String name, File file) {
        int index = IntStream.range(0, presetList.size()).filter(i -> presetList.get(i).name.equals(name)).findFirst().orElse(-1);
        if (index == -1) return;

        try (FileWriter writer = new FileWriter(file)) {
            writer.write("box64PresetV2\n" + gson.toJson(presetList.get(index)));
        } catch (Exception ignored) {
        }
    }

    private static void saveBox64Presets() {
        SharedPreferences.Editor editor = preferences.edit();

        editor.putString("box64PresetList", gson.toJson(presetList));
        editor.apply();
    }

    public static ArrayList<Box64Preset> getBox64Presets() {
        String json = preferences.getString("box64PresetList", "");
        Type listType = new TypeToken<ArrayList<Box64Preset>>() {}.getType();
        ArrayList<Box64Preset> box64PresetList = gson.fromJson(json, listType);

        return box64PresetList != null ? box64PresetList : new ArrayList<>(List.of(new Box64Preset("default")));
    }

    private static int boolToInt(boolean bool) {
        return (bool ? 1 : 0);
    }

    private static boolean intToBool(int value) {
        return (value == 1);
    }
    
    public static class Box64Preset {
        String name;
        int box64MMap32 = BOX64_MMAP32_DEFAULT_VALUE;
        int box64Avx = BOX64_AVX_DEFAULT_VALUE;
        int box64Sse42 = BOX64_SSE42_DEFAULT_VALUE;
        int box64BigBlock = BOX64_DYNAREC_BIGBLOCK_DEFAULT_VALUE;
        int box64StrongMem = BOX64_DYNAREC_STRONGMEM_DEFAULT_VALUE;
        int box64WeakBarrier = BOX64_DYNAREC_WEAKBARRIER_DEFAULT_VALUE;
        int box64Pause = BOX64_DYNAREC_PAUSE_DEFAULT_VALUE;
        int box64X87Double = BOX64_DYNAREC_X87DOUBLE_DEFAULT_VALUE;
        int box64FastNan = BOX64_DYNAREC_FASTNAN_DEFAULT_VALUE;
        int box64FastRound = BOX64_DYNAREC_FASTROUND_DEFAULT_VALUE;
        int box64SafeFlags = BOX64_DYNAREC_SAFEFLAGS_DEFAULT_VALUE;
        int box64CallRet = BOX64_DYNAREC_CALLRET_DEFAULT_VALUE;
        int box64AlignedAtomics = BOX64_DYNAREC_ALIGNED_ATOMICS_DEFAULT_VALUE;
        int box64NativeFlags = BOX64_DYNAREC_NATIVEFLAGS_DEFAULT_VALUE;
        int box64Wait = BOX64_DYNAREC_WAIT_DEFAULT_VALUE;
        int box64Dirty = BOX64_DYNAREC_DIRTY_DEFAULT_VALUE;
        int box64Forward = BOX64_DYNAREC_FORWARD_DEFAULT_VALUE;
        int box64DF = BOX64_DYNAREC_DF_DEFAULT_VALUE;

        public Box64Preset(String name) {
            this.name = name;
        }
    }
}
