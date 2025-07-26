package com.micewine.emu.fragments;

import static com.micewine.emu.activities.MainActivity.setSharedVars;
import static com.micewine.emu.activities.MainActivity.setupDone;
import static com.micewine.emu.core.RatPackageManager.listRatPackages;
import static com.micewine.emu.fragments.Box64PresetManagerFragment.addBox64Preset;
import static com.micewine.emu.fragments.ControllerPresetManagerFragment.addControllerPreset;
import static com.micewine.emu.fragments.VirtualControllerPresetManagerFragment.addVirtualControllerPreset;
import static com.micewine.emu.fragments.WinePrefixManagerFragment.createWinePrefix;
import static com.micewine.emu.fragments.WinePrefixManagerFragment.putSelectedWinePrefix;
import static com.micewine.emu.fragments.SetupFragment.progressBarIsIndeterminate;
import static com.micewine.emu.fragments.SetupFragment.dialogTitleText;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.micewine.emu.R;
import com.micewine.emu.core.RatPackageManager;

import java.util.ArrayList;
import java.util.List;

public class CreatePresetFragment extends DialogFragment {
    private final int presetType;

    public CreatePresetFragment(int presetType) {
        this.presetType = presetType;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.fragment_create_preset, null);

        EditText editTextNewName = view.findViewById(R.id.editTextNewName);
        Button buttonContinue = view.findViewById(R.id.buttonContinue);
        Button buttonCancel = view.findViewById(R.id.buttonCancel);
        TextView wineVersionText = view.findViewById(R.id.wineVersionText);
        Spinner wineVersionSpinner = view.findViewById(R.id.wineVersionSpinner);
        List<RatPackageManager.RatPackage> winePackages = listRatPackages("Wine");

        switch (presetType) {
            case WINE_PREFIX_PRESET -> {
                wineVersionText.setVisibility(View.VISIBLE);
                wineVersionSpinner.setVisibility(View.VISIBLE);

                ArrayList<String> winePackagesNames = new ArrayList<>();

                for (RatPackageManager.RatPackage ratPackage : winePackages) {
                    winePackagesNames.add(ratPackage.getName());
                }

                wineVersionSpinner.setAdapter(new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, winePackagesNames));
            }
            case CONTROLLER_PRESET, VIRTUAL_CONTROLLER_PRESET, BOX64_PRESET -> {
                wineVersionText.setVisibility(View.GONE);
                wineVersionSpinner.setVisibility(View.GONE);
            }
        }

        buttonContinue.setOnClickListener((v) -> {
            String newName = editTextNewName.getText().toString().trim();
            if (newName.isEmpty()) {
                Toast.makeText(requireContext(), R.string.invalid_preset_name, Toast.LENGTH_SHORT).show();
                return;
            }

            switch (presetType) {
                case WINE_PREFIX_PRESET -> {
                    putSelectedWinePrefix(newName);
                    setSharedVars(requireActivity());

                    setupDone = false;

                    new SetupFragment().show(requireActivity().getSupportFragmentManager(), "");

                    dialogTitleText = getString(R.string.creating_wine_prefix);
                    progressBarIsIndeterminate = true;

                    new Thread(() -> {
                        createWinePrefix(newName, winePackages.get(wineVersionSpinner.getSelectedItemPosition()).getFolderName());
                        setupDone = true;
                    });
                }
                case CONTROLLER_PRESET -> addControllerPreset(requireContext(), newName);
                case VIRTUAL_CONTROLLER_PRESET -> addVirtualControllerPreset(requireContext(), newName);
                case BOX64_PRESET -> addBox64Preset(requireContext(), newName);
            }
            dismiss();
        });

        buttonCancel.setOnClickListener((v) -> dismiss());

        return new AlertDialog.Builder(requireContext(), R.style.CustomAlertDialog).setView(view).create();
    }

    public final static int WINE_PREFIX_PRESET = 1;
    public final static int CONTROLLER_PRESET = 2;
    public final static int VIRTUAL_CONTROLLER_PRESET = 3;
    public final static int BOX64_PRESET = 4;
}