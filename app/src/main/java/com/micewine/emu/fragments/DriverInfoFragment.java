package com.micewine.emu.fragments;

import static com.micewine.emu.activities.MainActivity.appRootDir;
import static com.micewine.emu.activities.MainActivity.ratPackagesDir;
import static com.micewine.emu.activities.MainActivity.setSharedVars;
import static com.micewine.emu.activities.RatManagerActivity.generateICDFile;
import static com.micewine.emu.core.EnvVars.getEnv;
import static com.micewine.emu.core.RatPackageManager.getPackageById;
import static com.micewine.emu.core.RatPackageManager.listRatPackages;
import static com.micewine.emu.core.RatPackageManager.listRatPackagesId;
import static com.micewine.emu.core.ShellLoader.runCommandWithOutput;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewPropertyAnimator;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.micewine.emu.R;
import com.micewine.emu.core.RatPackageManager.RatPackage;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

public class DriverInfoFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_driver_info, container, false);

        Spinner driverSpinner = rootView.findViewById(R.id.driverSpinner);
        TextView driverInfoText = rootView.findViewById(R.id.logsTextView);
        ScrollView scrollView = rootView.findViewById(R.id.scrollView);

        List<String> vulkanDriversId = listRatPackagesId("VulkanDriver", "AdrenoToolsDriver");
        List<RatPackage> vulkanDrivers = listRatPackages("VulkanDriver", "AdrenoToolsDriver");
        List<String> vulkanDriversStr = vulkanDrivers.stream().map(p -> p.getName() + " " + p.getVersion()).collect(Collectors.toList());

        driverSpinner.setAdapter(new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, vulkanDriversStr));
        driverSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String driverId = vulkanDriversId.get(driverSpinner.getSelectedItemPosition());

                String driverPath;
                File packagesDir = new File(ratPackagesDir, appRootDir.getPath() + "/packages");
                String adrenoToolsDriverPath = null;

                if (driverId.contains("AdrenoToolsDriver")) {
                    RatPackage adrenoToolsWrapper = listRatPackages("AdrenoTools").get(0);
                    RatPackage ratPackage = getPackageById(driverId);
                    driverPath = (adrenoToolsWrapper != null ? adrenoToolsWrapper.getDriverLib() : null);
                    adrenoToolsDriverPath = (ratPackage != null ? ratPackage.getDriverLib() : null);
                } else {
                    RatPackage ratPackage = getPackageById(driverId);
                    driverPath = (ratPackage != null ? ratPackage.getDriverLib() : null);
                }

                setSharedVars(requireActivity(), adrenoToolsDriverPath);

                generateICDFile(driverPath);

                new Thread(() -> {
                    String driverInfo = runCommandWithOutput(getEnv() + "vulkaninfo", true);

                    driverInfoText.post(() -> {
                        ViewPropertyAnimator animator = driverInfoText.animate();
                        animator.alpha(0F);
                        animator.setDuration(100L);
                        animator.withEndAction(() -> {
                           driverInfoText.setText(driverInfo);
                           animator.alpha(1F);
                           animator.setDuration(100L);
                           animator.start();
                        });
                        animator.start();

                        scrollView.scrollTo(0, 0);
                    });
                }).start();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        return rootView;
    }
}