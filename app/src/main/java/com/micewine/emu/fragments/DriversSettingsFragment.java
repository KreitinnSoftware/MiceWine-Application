package com.micewine.emu.fragments;

import static com.micewine.emu.activities.GeneralSettingsActivity.CHECKBOX;
import static com.micewine.emu.activities.GeneralSettingsActivity.ENABLE_DRI3;
import static com.micewine.emu.activities.GeneralSettingsActivity.ENABLE_DRI3_DEFAULT_VALUE;
import static com.micewine.emu.activities.GeneralSettingsActivity.SELECTED_DXVK_HUD_PRESET;
import static com.micewine.emu.activities.GeneralSettingsActivity.SELECTED_DXVK_HUD_PRESET_DEFAULT_VALUE;
import static com.micewine.emu.activities.GeneralSettingsActivity.SELECTED_GL_PROFILE;
import static com.micewine.emu.activities.GeneralSettingsActivity.SELECTED_GL_PROFILE_DEFAULT_VALUE;
import static com.micewine.emu.activities.GeneralSettingsActivity.SELECTED_MESA_VK_WSI_PRESENT_MODE;
import static com.micewine.emu.activities.GeneralSettingsActivity.SELECTED_MESA_VK_WSI_PRESENT_MODE_DEFAULT_VALUE;
import static com.micewine.emu.activities.GeneralSettingsActivity.SELECTED_TU_DEBUG_PRESET;
import static com.micewine.emu.activities.GeneralSettingsActivity.SELECTED_TU_DEBUG_PRESET_DEFAULT_VALUE;
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
import com.micewine.emu.adapters.AdapterSettingsPreferences;

import java.util.ArrayList;

public class DriversSettingsFragment extends Fragment {
    private final ArrayList<AdapterSettingsPreferences.SettingsListSpinner> settingsList = new ArrayList<>();
    private RecyclerView recyclerView;

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
        recyclerView.setAdapter(new AdapterSettingsPreferences(settingsList, requireActivity()));

        settingsList.clear();

        addToAdapter(R.string.enable_dri3, R.string.null_desc, null, SWITCH, String.valueOf(ENABLE_DRI3_DEFAULT_VALUE), ENABLE_DRI3);
        addToAdapter(R.string.select_dxvk_hud_preset_title, R.string.null_desc, new String[] { "fps", "gpuload", "devinfo", "version", "api", "memory", "cs", "compiler", "allocations", "pipelines", "frametimes", "descriptors", "drawcalls", "submissions" }, CHECKBOX, SELECTED_DXVK_HUD_PRESET_DEFAULT_VALUE, SELECTED_DXVK_HUD_PRESET);
        addToAdapter(R.string.mesa_vk_wsi_present_mode_title, R.string.null_desc, new String[] { "fifo", "relaxed", "mailbox", "immediate" }, SPINNER, SELECTED_MESA_VK_WSI_PRESENT_MODE_DEFAULT_VALUE, SELECTED_MESA_VK_WSI_PRESENT_MODE);
        addToAdapter(R.string.tu_debug_title, R.string.null_desc, new String[] { "noconform", "flushall", "syncdraw", "sysmem", "gmem", "nolrz", "noubwc", "nomultipos", "forcebin" }, CHECKBOX, SELECTED_TU_DEBUG_PRESET_DEFAULT_VALUE, SELECTED_TU_DEBUG_PRESET);
        addToAdapter(R.string.select_gl_profile_title, R.string.null_desc,
                new String[] {
                        "GL 2.1", "GL 3.0",
                        "GL 3.1", "GL 3.2",
                        "GL 3.3", "GL 4.0",
                        "GL 4.1", "GL 4.2",
                        "GL 4.3", "GL 4.4",
                        "GL 4.5", "GL 4.6"
                },
                SPINNER, SELECTED_GL_PROFILE_DEFAULT_VALUE, SELECTED_GL_PROFILE
        );
    }

    private void addToAdapter(int titleId, int descriptionId, String[] values, int type, String defaultValue, String keyId) {
        settingsList.add(
                new AdapterSettingsPreferences.SettingsListSpinner(titleId, descriptionId, values, null, type, defaultValue, keyId)
        );
    }
}