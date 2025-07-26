package com.micewine.emu.fragments;

import static com.micewine.emu.activities.EmulationActivity.sharedLogs;
import static com.micewine.emu.adapters.AdapterGame.selectedGameName;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;

import com.google.android.material.button.MaterialButton;
import com.micewine.emu.R;

import java.io.FileWriter;
import java.io.IOException;

public class LogViewerFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_log_viewer, container, false);

        TextView logTextView = rootView.findViewById(R.id.logsTextView);
        ScrollView scrollView = rootView.findViewById(R.id.scrollView);
        MaterialButton exportLogButton = rootView.findViewById(R.id.exportLogButton);

        Observer<String> observer = o -> {
            if (o != null) {
                logTextView.append(o);
                scrollView.post(() -> scrollView.fullScroll(ScrollView.FOCUS_DOWN));
            }
        };

        sharedLogs.logsTextHead.observe(requireActivity(), observer);
        scrollView.fullScroll(ScrollView.FOCUS_DOWN);

        exportLogButton.setOnClickListener((v) -> {
            String logPath = "/storage/emulated/0/MiceWine/MiceWine-" + selectedGameName + "-Log-" + System.currentTimeMillis() / 1000 + ".txt";

            try (FileWriter writer = new FileWriter(logPath)) {
                writer.write(logTextView.getText().toString());
            } catch (IOException ignored) {
            }

            exportLogButton.post(() -> Toast.makeText(getContext(), "Log Exported to " + logPath.substring(logPath.lastIndexOf("/") + 1), Toast.LENGTH_SHORT).show());
        });

        return rootView;
    }
}