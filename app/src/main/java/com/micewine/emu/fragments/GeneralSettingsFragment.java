package com.micewine.emu.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import com.micewine.emu.R;
import com.micewine.emu.adapters.AdapterSettings;
import com.micewine.emu.adapters.AdapterSettings.SettingsList;

import java.util.ArrayList;

public class GeneralSettingsFragment extends Fragment {
    private final ArrayList<SettingsList> settingsList = new ArrayList<>();
    private RecyclerView recyclerView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_general_settings, container, false);
        recyclerView = rootView.findViewById(R.id.recyclerViewGeneralSettings);

        setAdapter();

        return rootView;
    }

    private void setAdapter() {
        recyclerView.setAdapter(new AdapterSettings(settingsList, requireContext()));

        settingsList.clear();

        addToAdapter(R.string.debug_settings_title, R.string.debug_settings_desc, R.drawable.ic_settings_outline);
        addToAdapter(R.string.sound_settings_title, R.string.sound_settings_desc, R.drawable.ic_sound);
        addToAdapter(R.string.driver_settings_title, R.string.driver_settings_desc, R.drawable.ic_gpu);
        addToAdapter(R.string.driver_info_title, R.string.driver_info_desc, R.drawable.ic_gpu);
        addToAdapter(R.string.env_settings_title, R.string.env_settings_desc, R.drawable.ic_globe);
        addToAdapter(R.string.wine_settings_title, R.string.wine_settings_desc, R.drawable.ic_wine);
    }

    private void addToAdapter(int titleId, int descriptionId, int iconId) {
        settingsList.add(
                new SettingsList(getString(titleId), getString(descriptionId), iconId)
        );
    }
}
