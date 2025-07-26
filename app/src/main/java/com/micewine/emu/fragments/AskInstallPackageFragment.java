package com.micewine.emu.fragments;

import static com.micewine.emu.activities.MainActivity.ACTION_INSTALL_ADTOOLS_DRIVER;
import static com.micewine.emu.activities.MainActivity.ACTION_INSTALL_RAT;
import static com.micewine.emu.fragments.Box64PresetManagerFragment.importBox64Preset;
import static com.micewine.emu.fragments.ControllerPresetManagerFragment.importControllerPreset;
import static com.micewine.emu.fragments.CreatePresetFragment.BOX64_PRESET;
import static com.micewine.emu.fragments.CreatePresetFragment.CONTROLLER_PRESET;
import static com.micewine.emu.fragments.CreatePresetFragment.VIRTUAL_CONTROLLER_PRESET;
import static com.micewine.emu.fragments.VirtualControllerPresetManagerFragment.importVirtualControllerPreset;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.micewine.emu.R;
import com.micewine.emu.core.RatPackageManager.RatPackage;
import com.micewine.emu.core.RatPackageManager.AdrenoToolsPackage;

import java.io.File;

public class AskInstallPackageFragment extends DialogFragment {
    private final int packageType;

    public AskInstallPackageFragment(int packageType) {
        this.packageType = packageType;
    }

    @SuppressLint("SetTextI18n")
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.fragment_ask_install_rat, null);

        Button buttonContinue = view.findViewById(R.id.buttonContinue);
        Button buttonCancel = view.findViewById(R.id.buttonCancel);

        TextView askInstallText = view.findViewById(R.id.askInstallText);

        Dialog dialog = new AlertDialog.Builder(requireContext(), R.style.CustomAlertDialog).setView(view).create();

        switch (packageType) {
            case RAT_PACKAGE: {
                askInstallText.setText(getString(R.string.install_rat_package_warning) + " " + ratCandidate.getName() + " " + ratCandidate.getVersion() + "?");
                break;
            }
            case ADTOOLS_DRIVER_PACKAGE: {
                askInstallText.setText(getString(R.string.install_rat_package_warning) + " " + adToolsDriverCandidate.getName() + " " + adToolsDriverCandidate.getVersion() + "?");
                break;
            }
            case MWP_PRESET_PACKAGE: {
                askInstallText.setText(getString(R.string.install_rat_package_warning) + " " + mwpPresetCandidate.file.getName() + "?");
                break;
            }
        }

        buttonContinue.setOnClickListener((v) -> {
            switch (packageType) {
                case RAT_PACKAGE -> requireContext().sendBroadcast(new Intent(ACTION_INSTALL_RAT));
                case ADTOOLS_DRIVER_PACKAGE -> requireContext().sendBroadcast(new Intent(ACTION_INSTALL_ADTOOLS_DRIVER));
                case MWP_PRESET_PACKAGE -> {
                    switch (mwpPresetCandidate.type) {
                        case VIRTUAL_CONTROLLER_PRESET -> {
                            boolean ret = importVirtualControllerPreset(requireActivity(), mwpPresetCandidate.file);
                            if (!ret) {
                                Toast.makeText(requireContext(), R.string.invalid_virtual_controller_preset_file, Toast.LENGTH_SHORT).show();
                            }
                        }
                        case CONTROLLER_PRESET -> {
                            boolean ret = importControllerPreset(mwpPresetCandidate.file);
                            if (!ret) {
                                Toast.makeText(requireContext(), R.string.invalid_controller_preset_file, Toast.LENGTH_SHORT).show();
                            }
                        }
                        case BOX64_PRESET -> {
                            boolean ret = importBox64Preset(mwpPresetCandidate.file);
                            if (!ret) {
                                Toast.makeText(requireContext(), R.string.invalid_box64_preset_file, Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                }
            }

            dismiss();
        });

        buttonCancel.setOnClickListener((v) -> dismiss());

        return dialog;
    }

    public static RatPackage ratCandidate = null;
    public static AdrenoToolsPackage adToolsDriverCandidate = null;
    public static MwpPreset mwpPresetCandidate = null;

    public static class MwpPreset {
        public int type;
        public File file;

        public MwpPreset(int type, File file) {
            this.type = type;
            this.file = file;
        }
    }

    public static final int RAT_PACKAGE = 0;
    public static final int ADTOOLS_DRIVER_PACKAGE = 1;
    public static final int MWP_PRESET_PACKAGE = 2;
}