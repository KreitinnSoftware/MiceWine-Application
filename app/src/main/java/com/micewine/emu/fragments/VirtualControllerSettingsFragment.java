package com.micewine.emu.fragments;

import static com.micewine.emu.adapters.AdapterGame.selectedGameName;
import static com.micewine.emu.controller.ControllerUtils.prepareControllersMappings;
import static com.micewine.emu.fragments.ShortcutsFragment.getSelectedVirtualControllerPreset;
import static com.micewine.emu.fragments.ShortcutsFragment.getVirtualControllerXInput;
import static com.micewine.emu.fragments.ShortcutsFragment.putSelectedVirtualControllerPreset;
import static com.micewine.emu.fragments.ShortcutsFragment.putVirtualControllerXInput;
import static com.micewine.emu.fragments.VirtualControllerPresetManagerFragment.getVirtualControllerPresets;
import static com.micewine.emu.fragments.VirtualControllerPresetManagerFragment.VirtualControllerPreset;


import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.button.MaterialButton;
import com.micewine.emu.R;

import java.util.List;
import java.util.stream.Collectors;

public class VirtualControllerSettingsFragment extends DialogFragment {
    private final List<String> mappingTypes = List.of("MiceWine Controller", "Keyboard/Mouse");
    private final List<String> virtualControllerProfilesNames =
            getVirtualControllerPresets().stream()
                    .map(VirtualControllerPreset::getName)
                    .collect(Collectors.toList());


    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.fragment_virtual_controller_settings, null);

        MaterialButton buttonConfirm = view.findViewById(R.id.buttonContinue);
        MaterialButton buttonCancel = view.findViewById(R.id.buttonCancel);

        TextView controllerKeyboardPresetText = view.findViewById(R.id.controllerKeyboardPresetText);
        Spinner controllerKeyboardPresetSpinner = view.findViewById(R.id.controllerKeyboardPresetSpinner);
        Spinner controllerMappingTypeSpinner = view.findViewById(R.id.controllerMappingTypeSpinner);

        controllerMappingTypeSpinner.setAdapter(new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, mappingTypes));
        controllerMappingTypeSpinner.setSelection(getVirtualControllerXInput(selectedGameName) ? 0 : 1);
        controllerMappingTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                boolean isXInput = (i == 0);
                controllerKeyboardPresetText.setVisibility(isXInput ? View.GONE : View.VISIBLE);
                controllerKeyboardPresetSpinner.setVisibility(isXInput ? View.GONE : View.VISIBLE);

                putVirtualControllerXInput(selectedGameName, isXInput);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        controllerKeyboardPresetSpinner.setAdapter(new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, virtualControllerProfilesNames));
        controllerKeyboardPresetSpinner.setSelection(virtualControllerProfilesNames.indexOf(getSelectedVirtualControllerPreset(selectedGameName)));
        controllerKeyboardPresetSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                putSelectedVirtualControllerPreset(selectedGameName, virtualControllerProfilesNames.get(i));
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        buttonConfirm.setOnClickListener((v) -> dismiss());
        buttonCancel.setOnClickListener((v) -> dismiss());

        return new AlertDialog.Builder(requireActivity(), R.style.CustomAlertDialog).setView(view).create();
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);
        getParentFragmentManager().setFragmentResult("invalidateControllerType", new Bundle());
        prepareControllersMappings();
    }
}