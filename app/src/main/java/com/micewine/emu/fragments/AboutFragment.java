package com.micewine.emu.fragments;

import static com.micewine.emu.activities.MainActivity.miceWineVersion;
import static com.micewine.emu.activities.MainActivity.ratPackagesDir;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.micewine.emu.R;
import com.micewine.emu.databinding.FragmentAboutBinding;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

public class AboutFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        FragmentAboutBinding binding = FragmentAboutBinding.inflate(inflater, container, false);
        View rootView = binding.getRoot();

        TextView appVersion = rootView.findViewById(R.id.ApplicationVersion);
        TextView rootFsVersion = rootView.findViewById(R.id.RootfsVersion);

        appVersion.setText(miceWineVersion);

        new Thread(() -> {
            File rootFsVersionFile = new File(ratPackagesDir, "rootfs-pkg-header");
            String versionText = "???";

            if (rootFsVersionFile.exists()) {
                try {
                    List<String> lines = Files.readAllLines(rootFsVersionFile.toPath());
                    String versionStr = lines.get(2);

                    if (versionStr != null) {
                        versionText = versionStr.substring(versionStr.indexOf("=") + 1).replace("(", "(git-");
                    }
                } catch (IOException ignored) {
                }
            }

            String finalVersionText = versionText;
            rootFsVersion.post(() -> rootFsVersion.setText(finalVersionText));
        }).start();

        return rootView;
    }
}