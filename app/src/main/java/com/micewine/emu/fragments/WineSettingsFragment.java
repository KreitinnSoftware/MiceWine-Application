package com.micewine.emu.fragments;

import static com.micewine.emu.activities.GeneralSettingsActivity.SEEKBAR;
import static com.micewine.emu.activities.GeneralSettingsActivity.WINE_DPI;
import static com.micewine.emu.activities.GeneralSettingsActivity.WINE_DPI_DEFAULT_VALUE;

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
import com.micewine.emu.adapters.AdapterSettingsPreferences.SettingsListSpinner;

import java.util.ArrayList;

public class WineSettingsFragment extends Fragment {
    private RecyclerView recyclerView;
    private final ArrayList<SettingsListSpinner> settingsList = new ArrayList<>();

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

        addToAdapter(R.string.wine_dpi, R.string.null_desc, new int[] { 96, 480 }, SEEKBAR, String.valueOf(WINE_DPI_DEFAULT_VALUE), WINE_DPI);
    }

    private void addToAdapter(int titleId, int descriptionId, int[] seekBarValues, int type, String defaultValue, String keyId) {
        settingsList.add(
                new SettingsListSpinner(titleId, descriptionId, null, seekBarValues, type, defaultValue, keyId)
        );
    }
}