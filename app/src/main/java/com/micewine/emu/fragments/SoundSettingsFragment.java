package com.micewine.emu.fragments;

import static com.micewine.emu.activities.GeneralSettingsActivity.PA_SINK;
import static com.micewine.emu.activities.GeneralSettingsActivity.PA_SINK_DEFAULT_VALUE;
import static com.micewine.emu.activities.GeneralSettingsActivity.SPINNER;
import static com.micewine.emu.activities.MainActivity.paSink;
import static com.micewine.emu.activities.MainActivity.usrDir;

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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class SoundSettingsFragment extends Fragment {
    private final ArrayList<SettingsListSpinner> settingsList = new ArrayList<>();
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

        addToAdapter(R.string.select_audio_sink, R.string.null_desc, new String[] { "SLES", "AAudio" }, SPINNER, PA_SINK_DEFAULT_VALUE, PA_SINK);
    }

    private void addToAdapter(int titleId, int descriptionId, String[] values, int type, String defaultValue, String keyId) {
        settingsList.add(
                new SettingsListSpinner(titleId, descriptionId, values, null, type, defaultValue, keyId)
        );
    }

    public static void generatePAFile() {
        File paFile = new File(usrDir, "etc/pulse/default.pa");

        try (FileWriter writer = new FileWriter(paFile)) {
            writer.write(
                    "#!" + usrDir + "/bin/pulseaudio -nF\n" +
                    ".fail\n" +
                    "\n" +
                    "load-module module-device-restore\n" +
                    "load-module module-stream-restore\n" +
                    "load-module module-card-restore\n" +
                    "load-module module-augment-properties\n" +
                    "load-module module-switch-on-port-available\n" +
                    "\n" +
                    ".ifexists module-esound-protocol-unix.so\n" +
                    "load-module module-esound-protocol-unix\n" +
                    ".endif\n" +
                    "load-module module-native-protocol-unix\n" +
                    "load-module module-default-device-restore\n" +
                    "load-module module-always-sink\n" +
                    "load-module module-intended-roles\n" +
                    "load-module module-position-event-sounds\n" +
                    "load-module module-role-cork\n" +
                    "load-module module-filter-heuristics\n" +
                    "load-module module-filter-apply\n" +
                    "\n" +
                    ".nofail\n" +
                    ".include " + usrDir + "/etc/pulse/default.pa.d\n" +
                    "\n" +
                    "load-module module-" + paSink + "-sink\n");
        } catch (IOException ignored) {
        }
    }
}