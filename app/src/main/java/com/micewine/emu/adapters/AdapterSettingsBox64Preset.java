package com.micewine.emu.adapters;

import static com.micewine.emu.activities.GeneralSettingsActivity.BOX64_AVX;
import static com.micewine.emu.activities.GeneralSettingsActivity.BOX64_DYNAREC_ALIGNED_ATOMICS;
import static com.micewine.emu.activities.GeneralSettingsActivity.BOX64_DYNAREC_BIGBLOCK;
import static com.micewine.emu.activities.GeneralSettingsActivity.BOX64_DYNAREC_CALLRET;
import static com.micewine.emu.activities.GeneralSettingsActivity.BOX64_DYNAREC_DF;
import static com.micewine.emu.activities.GeneralSettingsActivity.BOX64_DYNAREC_DIRTY;
import static com.micewine.emu.activities.GeneralSettingsActivity.BOX64_DYNAREC_FASTNAN;
import static com.micewine.emu.activities.GeneralSettingsActivity.BOX64_DYNAREC_FASTROUND;
import static com.micewine.emu.activities.GeneralSettingsActivity.BOX64_DYNAREC_FORWARD;
import static com.micewine.emu.activities.GeneralSettingsActivity.BOX64_DYNAREC_NATIVEFLAGS;
import static com.micewine.emu.activities.GeneralSettingsActivity.BOX64_DYNAREC_PAUSE;
import static com.micewine.emu.activities.GeneralSettingsActivity.BOX64_DYNAREC_SAFEFLAGS;
import static com.micewine.emu.activities.GeneralSettingsActivity.BOX64_DYNAREC_STRONGMEM;
import static com.micewine.emu.activities.GeneralSettingsActivity.BOX64_DYNAREC_WAIT;
import static com.micewine.emu.activities.GeneralSettingsActivity.BOX64_DYNAREC_WEAKBARRIER;
import static com.micewine.emu.activities.GeneralSettingsActivity.BOX64_DYNAREC_X87DOUBLE;
import static com.micewine.emu.activities.GeneralSettingsActivity.BOX64_MMAP32;
import static com.micewine.emu.activities.GeneralSettingsActivity.BOX64_SSE42;
import static com.micewine.emu.activities.GeneralSettingsActivity.SELECTED_BOX64;
import static com.micewine.emu.activities.GeneralSettingsActivity.SELECTED_VULKAN_DRIVER;
import static com.micewine.emu.activities.GeneralSettingsActivity.SPINNER;
import static com.micewine.emu.activities.GeneralSettingsActivity.SWITCH;
import static com.micewine.emu.activities.MainActivity.preferences;
import static com.micewine.emu.activities.MainActivity.tmpDir;
import static com.micewine.emu.adapters.AdapterPreset.clickedPresetName;
import static com.micewine.emu.core.RatPackageManager.deleteRatPackageById;
import static com.micewine.emu.core.RatPackageManager.installRat;
import static com.micewine.emu.core.RatPackageManager.listRatPackages;
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
import static com.micewine.emu.fragments.Box64PresetManagerFragment.putBox64AlignedAtomics;
import static com.micewine.emu.fragments.Box64PresetManagerFragment.putBox64Avx;
import static com.micewine.emu.fragments.Box64PresetManagerFragment.putBox64BigBlock;
import static com.micewine.emu.fragments.Box64PresetManagerFragment.putBox64CallRet;
import static com.micewine.emu.fragments.Box64PresetManagerFragment.putBox64DF;
import static com.micewine.emu.fragments.Box64PresetManagerFragment.putBox64Dirty;
import static com.micewine.emu.fragments.Box64PresetManagerFragment.putBox64FastNan;
import static com.micewine.emu.fragments.Box64PresetManagerFragment.putBox64FastRound;
import static com.micewine.emu.fragments.Box64PresetManagerFragment.putBox64Forward;
import static com.micewine.emu.fragments.Box64PresetManagerFragment.putBox64MMap32;
import static com.micewine.emu.fragments.Box64PresetManagerFragment.putBox64NativeFlags;
import static com.micewine.emu.fragments.Box64PresetManagerFragment.putBox64Pause;
import static com.micewine.emu.fragments.Box64PresetManagerFragment.putBox64SafeFlags;
import static com.micewine.emu.fragments.Box64PresetManagerFragment.putBox64Sse42;
import static com.micewine.emu.fragments.Box64PresetManagerFragment.putBox64StrongMem;
import static com.micewine.emu.fragments.Box64PresetManagerFragment.putBox64Wait;
import static com.micewine.emu.fragments.Box64PresetManagerFragment.putBox64WeakBarrier;
import static com.micewine.emu.fragments.Box64PresetManagerFragment.putBox64X87Double;
import static com.micewine.emu.fragments.RatDownloaderFragment.downloadPackage;
import static com.micewine.emu.utils.FileUtils.deleteDirectoryRecursively;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.micewine.emu.R;
import com.micewine.emu.core.RatPackageManager;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

public class AdapterSettingsBox64Preset extends RecyclerView.Adapter<AdapterSettingsBox64Preset.ViewHolder> {
    private final ArrayList<Box64ListSpinner> presetsList;
    private final FragmentActivity activity;

    public AdapterSettingsBox64Preset(ArrayList<Box64ListSpinner> presetsList, FragmentActivity activity) {
        this.presetsList = presetsList;
        this.activity = activity;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_settings_preferences_item, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Box64ListSpinner item = presetsList.get(position);

        holder.settingsName.setText(item.titleSettings);
        holder.settingsDescription.setText(item.descriptionSettings);

        if (activity.getString(item.descriptionSettings).equals(" ")) {
            holder.settingsDescription.setVisibility(View.GONE);
        }

        switch (item.type) {
            case SWITCH -> {
                holder.spinnerOptions.setVisibility(View.GONE);
                holder.settingsSwitch.setVisibility(View.VISIBLE);
                holder.seekBar.setVisibility(View.GONE);
                holder.seekBarValue.setVisibility(View.GONE);

                boolean element = switch (item.key) {
                    case BOX64_DYNAREC_FASTNAN -> getBox64FastNan(clickedPresetName);
                    case BOX64_DYNAREC_FASTROUND -> getBox64FastRound(clickedPresetName);
                    case BOX64_DYNAREC_ALIGNED_ATOMICS -> getBox64AlignedAtomics(clickedPresetName);
                    case BOX64_DYNAREC_NATIVEFLAGS -> getBox64NativeFlags(clickedPresetName);
                    case BOX64_DYNAREC_WAIT -> getBox64Wait(clickedPresetName);
                    case BOX64_SSE42 -> getBox64Sse42(clickedPresetName);
                    case BOX64_MMAP32 -> getBox64MMap32(clickedPresetName);
                    case BOX64_DYNAREC_DF -> getBox64DF(clickedPresetName);
                    default -> false;
                };

                holder.settingsSwitch.setChecked(element);
                holder.settingsSwitch.setOnClickListener((v) -> {
                    switch (item.key) {
                        case BOX64_DYNAREC_FASTNAN -> putBox64FastNan(clickedPresetName, holder.settingsSwitch.isChecked());
                        case BOX64_DYNAREC_FASTROUND -> putBox64FastRound(clickedPresetName, holder.settingsSwitch.isChecked());
                        case BOX64_DYNAREC_ALIGNED_ATOMICS -> putBox64AlignedAtomics(clickedPresetName, holder.settingsSwitch.isChecked());
                        case BOX64_DYNAREC_NATIVEFLAGS -> putBox64NativeFlags(clickedPresetName, holder.settingsSwitch.isChecked());
                        case BOX64_DYNAREC_WAIT -> putBox64Wait(clickedPresetName, holder.settingsSwitch.isChecked());
                        case BOX64_SSE42 -> putBox64Sse42(clickedPresetName, holder.settingsSwitch.isChecked());
                        case BOX64_MMAP32 -> putBox64MMap32(clickedPresetName, holder.settingsSwitch.isChecked());
                        case BOX64_DYNAREC_DF -> putBox64DF(clickedPresetName, holder.settingsSwitch.isChecked());
                    };
                });
            }
            case SPINNER -> {
                holder.spinnerOptions.setVisibility(View.VISIBLE);
                holder.settingsSwitch.setVisibility(View.GONE);
                holder.seekBar.setVisibility(View.GONE);
                holder.seekBarValue.setVisibility(View.GONE);

                holder.spinnerOptions.setAdapter(new ArrayAdapter<>(activity, android.R.layout.simple_spinner_dropdown_item, item.spinnerOptions));

                int element = switch (item.key) {
                    case BOX64_DYNAREC_BIGBLOCK -> getBox64BigBlock(clickedPresetName);
                    case BOX64_DYNAREC_STRONGMEM -> getBox64StrongMem(clickedPresetName);
                    case BOX64_DYNAREC_WEAKBARRIER -> getBox64WeakBarrier(clickedPresetName);
                    case BOX64_DYNAREC_PAUSE -> getBox64Pause(clickedPresetName);
                    case BOX64_DYNAREC_X87DOUBLE -> getBox64X87Double(clickedPresetName);
                    case BOX64_DYNAREC_SAFEFLAGS -> getBox64SafeFlags(clickedPresetName);
                    case BOX64_DYNAREC_CALLRET -> getBox64CallRet(clickedPresetName);
                    case BOX64_DYNAREC_DIRTY -> getBox64Dirty(clickedPresetName);
                    case BOX64_DYNAREC_FORWARD -> getBox64Forward(clickedPresetName);
                    case BOX64_AVX -> getBox64Avx(clickedPresetName);
                    default -> -1;
                };

                int index = Arrays.asList(item.spinnerOptions).indexOf(String.valueOf(element));
                holder.spinnerOptions.setSelection(index);

                holder.spinnerOptions.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                        String selectedItem = adapterView.getItemAtPosition(i).toString();

                        switch (item.key) {
                            case BOX64_DYNAREC_BIGBLOCK -> putBox64BigBlock(clickedPresetName, Integer.parseInt(selectedItem));
                            case BOX64_DYNAREC_STRONGMEM -> putBox64StrongMem(clickedPresetName, Integer.parseInt(selectedItem));
                            case BOX64_DYNAREC_WEAKBARRIER -> putBox64WeakBarrier(clickedPresetName, Integer.parseInt(selectedItem));
                            case BOX64_DYNAREC_PAUSE -> putBox64Pause(clickedPresetName, Integer.parseInt(selectedItem));
                            case BOX64_DYNAREC_X87DOUBLE -> putBox64X87Double(clickedPresetName, Integer.parseInt(selectedItem));
                            case BOX64_DYNAREC_SAFEFLAGS -> putBox64SafeFlags(clickedPresetName, Integer.parseInt(selectedItem));
                            case BOX64_DYNAREC_CALLRET -> putBox64CallRet(clickedPresetName, Integer.parseInt(selectedItem));
                            case BOX64_DYNAREC_DIRTY -> putBox64Dirty(clickedPresetName, Integer.parseInt(selectedItem));
                            case BOX64_DYNAREC_FORWARD -> putBox64Forward(clickedPresetName, Integer.parseInt(selectedItem));
                            case BOX64_AVX -> putBox64Avx(clickedPresetName, Integer.parseInt(selectedItem));
                        };
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> adapterView) {
                    }
                });
            }
        }
    }

    @Override
    public int getItemCount() {
        return presetsList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView settingsName = itemView.findViewById(R.id.title_preferences_model);
        TextView settingsDescription = itemView.findViewById(R.id.description_preferences_model);
        Spinner spinnerOptions = itemView.findViewById(R.id.keyBindSpinner);
        SwitchCompat settingsSwitch = itemView.findViewById(R.id.optionSwitch);
        SeekBar seekBar = itemView.findViewById(R.id.seekBar);
        TextView seekBarValue = itemView.findViewById(R.id.seekBarValue);

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
        }
    }

    public static class Box64ListSpinner {
        public int titleSettings;
        public int descriptionSettings;
        public String[] spinnerOptions;
        public int type;
        public String key;

        public Box64ListSpinner(int titleSettings, int descriptionSettings, String[] spinnerOptions, int type, String key) {
            this.titleSettings = titleSettings;
            this.descriptionSettings = descriptionSettings;
            this.spinnerOptions = spinnerOptions;
            this.type = type;
            this.key = key;
        }
    }
}