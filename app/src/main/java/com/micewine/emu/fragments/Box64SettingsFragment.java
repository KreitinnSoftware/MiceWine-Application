package com.micewine.emu.fragments;

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
import static com.micewine.emu.activities.GeneralSettingsActivity.SPINNER;
import static com.micewine.emu.activities.GeneralSettingsActivity.SWITCH;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.micewine.emu.R;
import com.micewine.emu.adapters.AdapterSettingsBox64Preset;

import java.util.ArrayList;

public class Box64SettingsFragment extends Fragment {
    private RecyclerView recyclerView;
    private final ArrayList<AdapterSettingsBox64Preset.Box64ListSpinner> settingsList = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_settings_model, container, false);
        recyclerView = rootView.findViewById(R.id.recyclerViewSettingsModel);

        GridLayoutManager layoutManager = (GridLayoutManager) recyclerView.getLayoutManager();
        if (layoutManager != null) {
            layoutManager.setSpanCount(1);
        }

        setAdapter();

        return rootView;
    }

    private void setAdapter() {
        recyclerView.setAdapter(new AdapterSettingsBox64Preset(settingsList, requireActivity()));

        settingsList.clear();

        addToAdapter(R.string.box64_bigblock, R.string.box64_bigblock_desc, new String[]{ "0", "1", "2", "3" }, SPINNER, BOX64_DYNAREC_BIGBLOCK);
        addToAdapter(R.string.box64_strongmem, R.string.box64_strongmem_desc, new String[]{ "0", "1", "2", "3" }, SPINNER, BOX64_DYNAREC_STRONGMEM);
        addToAdapter(R.string.box64_weakbarrier, R.string.box64_weakbarrier_desc, new String[]{ "0", "1", "2" }, SPINNER, BOX64_DYNAREC_WEAKBARRIER);
        addToAdapter(R.string.box64_pause, R.string.box64_pause_desc, new String[]{ "0", "1", "2", "3" }, SPINNER, BOX64_DYNAREC_PAUSE);
        addToAdapter(R.string.box64_x87double, R.string.box64_x87double_desc, new String[]{ "0", "1", "2" }, SPINNER, BOX64_DYNAREC_X87DOUBLE);
        addToAdapter(R.string.box64_fastnan, R.string.box64_fastnan_desc, null, SWITCH, BOX64_DYNAREC_FASTNAN);
        addToAdapter(R.string.box64_fastround, R.string.box64_fastround_desc, null, SWITCH, BOX64_DYNAREC_FASTROUND);
        addToAdapter(R.string.box64_safeflags, R.string.box64_safeflags_desc, new String[]{ "0", "1", "2" }, SPINNER, BOX64_DYNAREC_SAFEFLAGS);
        addToAdapter(R.string.box64_callret, R.string.box64_callret_desc, new String[]{ "0", "1", "2" }, SPINNER, BOX64_DYNAREC_CALLRET);
        addToAdapter(R.string.box64_df, R.string.box64_df_desc, null, SWITCH, BOX64_DYNAREC_DF);
        addToAdapter(R.string.box64_aligned_atomics, R.string.box64_aligned_atomics_desc, null, SWITCH, BOX64_DYNAREC_ALIGNED_ATOMICS);
        addToAdapter(R.string.box64_nativeflags, R.string.box64_nativeflags_desc, null, SWITCH, BOX64_DYNAREC_NATIVEFLAGS);
        addToAdapter(R.string.box64_dynarec_wait, R.string.box64_dynarec_wait_desc, null, SWITCH, BOX64_DYNAREC_WAIT);
        addToAdapter(R.string.box64_dynarec_dirty, R.string.box64_dynarec_dirty_desc, new String[]{ "0", "1", "2" }, SPINNER, BOX64_DYNAREC_DIRTY);
        addToAdapter(R.string.box64_dynarec_forward, R.string.box64_dynarec_forward_desc, new String[]{ "0", "128", "256", "512", "1024" }, SPINNER, BOX64_DYNAREC_FORWARD);
        addToAdapter(R.string.box64_avx, R.string.box64_avx_desc, new String[]{ "0", "1", "2" }, SPINNER, BOX64_AVX);
        addToAdapter(R.string.box64_sse42, R.string.box64_sse42_desc, null, SWITCH, BOX64_SSE42);
        addToAdapter(R.string.box64_mmap32, R.string.box64_mmap32_desc, null, SWITCH, BOX64_MMAP32);
    }

    private void addToAdapter(int titleId, int descriptionId, String[] values, int type, String keyId) {
        settingsList.add(
                new AdapterSettingsBox64Preset.Box64ListSpinner(titleId, descriptionId, values, type, keyId)
        );
    }
}