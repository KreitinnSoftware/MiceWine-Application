package com.micewine.emu.fragments;

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

import com.google.android.material.button.MaterialButton;
import com.micewine.emu.R;
import com.micewine.emu.core.ShellLoader;

import java.io.FileWriter;
import java.io.IOException;

public class LogViewerFragment extends Fragment implements ShellLoader.LogCallback {
    private TextView logTextView;
    private final StringBuilder logs = new StringBuilder();
    private ScrollView scrollView;
    private boolean logViewerIsOpened = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_log_viewer, container, false);

        logTextView = rootView.findViewById(R.id.logsTextView);
        scrollView = rootView.findViewById(R.id.scrollView);
        MaterialButton exportLogButton = rootView.findViewById(R.id.exportLogButton);

        ShellLoader.connectOutput(this);

        scrollView.fullScroll(ScrollView.FOCUS_DOWN);

        exportLogButton.setOnClickListener((v) -> {
            String logPath = "/storage/emulated/0/MiceWine/MiceWine-" + selectedGameName + "-Log-" + System.currentTimeMillis() / 1000 + ".txt";

            try (FileWriter writer = new FileWriter(logPath)) {
                writer.write(logs.toString());
            } catch (IOException ignored) {
            }

            exportLogButton.post(() -> Toast.makeText(getContext(), "Log Exported to " + logPath.substring(logPath.lastIndexOf("/") + 1), Toast.LENGTH_SHORT).show());
        });

        return rootView;
    }

    private String getLastLines(StringBuilder sb) {
        int count = 0;
        int i = sb.length() - 1;

        while (i >= 0 && count < 500) {
            if (sb.charAt(i) == '\n') {
                count++;
            }
            i--;
        }

        int start = Math.max(0, i + 2);

        return sb.substring(start);
    }

    public void populate() {
        logViewerIsOpened = true;
        logTextView.post(() -> logTextView.setText(getLastLines(logs)));
    }

    public void cleanup() {
        logViewerIsOpened = false;
        logTextView.post(() -> logTextView.setText(""));
    }

    @Override
    public void appendLogs(String text) {
        logs.append(text);

        if (!logViewerIsOpened) return;

        requireActivity().runOnUiThread(() -> {
            logTextView.append(text);
            scrollView.post(() -> scrollView.fullScroll(ScrollView.FOCUS_DOWN));
        });
    }
}