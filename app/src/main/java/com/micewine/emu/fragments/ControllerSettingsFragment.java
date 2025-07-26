package com.micewine.emu.fragments;

import static com.micewine.emu.adapters.AdapterGame.selectedGameName;
import static com.micewine.emu.controller.ControllerUtils.connectedPhysicalControllers;
import static com.micewine.emu.controller.ControllerUtils.prepareControllersMappings;
import static com.micewine.emu.fragments.ControllerPresetManagerFragment.getControllerPresets;
import static com.micewine.emu.fragments.ControllerPresetManagerFragment.ControllerPreset;
import static com.micewine.emu.fragments.ShortcutsFragment.getControllerPreset;
import static com.micewine.emu.fragments.ShortcutsFragment.getControllerXInput;
import static com.micewine.emu.fragments.ShortcutsFragment.getControllerXInputSwapAnalogs;
import static com.micewine.emu.fragments.ShortcutsFragment.putControllerPreset;
import static com.micewine.emu.fragments.ShortcutsFragment.putControllerXInput;
import static com.micewine.emu.fragments.ShortcutsFragment.putControllerXInputSwapAnalogs;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
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
import com.google.android.material.materialswitch.MaterialSwitch;
import com.micewine.emu.R;

import java.util.List;
import java.util.stream.Collectors;

public class ControllerSettingsFragment extends DialogFragment {
    private List<TextView> controllersMappingTypeTexts;
    private List<Spinner> controllersMappingTypeSpinners;
    private List<TextView> controllersSwapAnalogsTexts;
    private List<MaterialSwitch> controllersSwapAnalogsSwitches;
    private List<Spinner> controllersKeyboardPresetSpinners;
    private List<TextView> controllersKeyboardPresetTexts;
    private List<TextView> controllersNamesTexts;
    private final List<String> mappingTypes = List.of("MiceWine Controller", "Keyboard/Mouse");
    private final List<String> controllerProfilesNames =
            getControllerPresets().stream()
                    .map(ControllerPreset::getName)
                    .collect(Collectors.toList());

    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent == null) return;
            if (ACTION_UPDATE_CONTROLLERS_STATUS.equals(intent.getAction())) {
                requireActivity().runOnUiThread(() -> updateControllerStatus());
            }
        }
    };

    private void updateControllerStatus() {
        controllersNamesTexts.forEach((i) -> i.setVisibility(View.VISIBLE));
        controllersMappingTypeTexts.forEach((i) -> i.setVisibility(View.VISIBLE));
        controllersMappingTypeSpinners.forEach((i) -> i.setVisibility(View.VISIBLE));
        controllersKeyboardPresetSpinners.forEach((i) -> i.setVisibility(View.VISIBLE));
        controllersKeyboardPresetTexts.forEach((i) -> i.setVisibility(View.VISIBLE));
        controllersSwapAnalogsTexts.forEach((i) -> i.setVisibility(View.VISIBLE));
        controllersSwapAnalogsSwitches.forEach((i) -> i.setVisibility(View.VISIBLE));

        int connectedControllersCount = connectedPhysicalControllers.size();

        for (int i = connectedControllersCount; i < 4; i++) {
            controllersNamesTexts.get(i).setVisibility(View.GONE);
            controllersMappingTypeTexts.get(i).setVisibility(View.GONE);
            controllersMappingTypeSpinners.get(i).setVisibility(View.GONE);
            controllersKeyboardPresetSpinners.get(i).setVisibility(View.GONE);
            controllersKeyboardPresetTexts.get(i).setVisibility(View.GONE);
            controllersSwapAnalogsTexts.get(i).setVisibility(View.GONE);
            controllersSwapAnalogsSwitches.get(i).setVisibility(View.GONE);
        }

        if (connectedControllersCount == 0) {
            controllersMappingTypeTexts.get(0).setVisibility(View.VISIBLE);
            controllersMappingTypeTexts.get(0).setText(R.string.no_controllers_connected);
        }

        for (int i = 0; i < controllersNamesTexts.size(); i++) {
            if (i < connectedControllersCount) {
                controllersNamesTexts.get(i).setText(i + ": " + connectedPhysicalControllers.get(i).getName());
            }
        }

        for (int i = 0; i < controllersMappingTypeSpinners.size(); i++) {
            controllersMappingTypeSpinners.get(i).setAdapter(new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, mappingTypes));
            controllersMappingTypeSpinners.get(i).setSelection(getControllerXInput(selectedGameName, i) ? 0 : 1);
            int index = i;
            controllersMappingTypeSpinners.get(i).setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int pos, long l) {
                    boolean isXInput = (pos == 0);
                    controllersKeyboardPresetTexts.get(index).setVisibility(isXInput ? View.GONE : View.VISIBLE);
                    controllersKeyboardPresetSpinners.get(index).setVisibility(isXInput ? View.GONE : View.VISIBLE);

                    controllersSwapAnalogsTexts.get(index).setVisibility(isXInput ? View.VISIBLE : View.GONE);
                    controllersSwapAnalogsSwitches.get(index).setVisibility(isXInput ? View.VISIBLE : View.GONE);

                    putControllerXInput(selectedGameName, isXInput, index);
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {
                }
            });
        }

        for (int i = 0; i < controllersSwapAnalogsSwitches.size(); i++) {
            controllersSwapAnalogsSwitches.get(i).setChecked(getControllerXInputSwapAnalogs(selectedGameName, i));
            int index = i;
            controllersSwapAnalogsSwitches.get(i).setOnClickListener((v) -> putControllerXInputSwapAnalogs(selectedGameName, controllersSwapAnalogsSwitches.get(index).isChecked(), index));
        }

        for (int i = 0; i < controllersKeyboardPresetSpinners.size(); i++) {
            controllersKeyboardPresetSpinners.get(i).setAdapter(new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, controllerProfilesNames));
            controllersKeyboardPresetSpinners.get(i).setSelection(controllerProfilesNames.indexOf(getControllerPreset(selectedGameName, i)));
            controllersKeyboardPresetSpinners.get(i).setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int pos, long l) {
                    putControllerPreset(selectedGameName, controllerProfilesNames.get(pos), pos);
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {
                }
            });
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        LayoutInflater inflater = getLayoutInflater();
        View view = inflater.inflate(R.layout.fragment_controller_settings, null);

        MaterialButton buttonConfirm = view.findViewById(R.id.buttonContinue);
        MaterialButton buttonCancel = view.findViewById(R.id.buttonCancel);

        controllersMappingTypeTexts = List.of(
                view.findViewById(R.id.controller0MappingTypeText),
                view.findViewById(R.id.controller1MappingTypeText),
                view.findViewById(R.id.controller2MappingTypeText),
                view.findViewById(R.id.controller3MappingTypeText)
        );
        controllersMappingTypeSpinners = List.of(
                view.findViewById(R.id.controller0MappingTypeSpinner),
                view.findViewById(R.id.controller1MappingTypeSpinner),
                view.findViewById(R.id.controller2MappingTypeSpinner),
                view.findViewById(R.id.controller3MappingTypeSpinner)
        );
        controllersSwapAnalogsTexts = List.of(
                view.findViewById(R.id.controller0SwapAnalogsText),
                view.findViewById(R.id.controller1SwapAnalogsText),
                view.findViewById(R.id.controller2SwapAnalogsText),
                view.findViewById(R.id.controller3SwapAnalogsText)
        );
        controllersSwapAnalogsSwitches = List.of(
                view.findViewById(R.id.controller0SwapAnalogsSwitch),
                view.findViewById(R.id.controller1SwapAnalogsSwitch),
                view.findViewById(R.id.controller2SwapAnalogsSwitch),
                view.findViewById(R.id.controller3SwapAnalogsSwitch)
        );
        controllersKeyboardPresetSpinners = List.of(
                view.findViewById(R.id.controller0KeyboardPresetSpinner),
                view.findViewById(R.id.controller1KeyboardPresetSpinner),
                view.findViewById(R.id.controller2KeyboardPresetSpinner),
                view.findViewById(R.id.controller3KeyboardPresetSpinner)
        );
        controllersKeyboardPresetTexts = List.of(
                view.findViewById(R.id.controller0KeyboardPresetText),
                view.findViewById(R.id.controller1KeyboardPresetText),
                view.findViewById(R.id.controller2KeyboardPresetText),
                view.findViewById(R.id.controller3KeyboardPresetText)
        );
        controllersNamesTexts = List.of(
                view.findViewById(R.id.controller0Name),
                view.findViewById(R.id.controller1Name),
                view.findViewById(R.id.controller2Name),
                view.findViewById(R.id.controller3Name)
        );

        updateControllerStatus();

        buttonConfirm.setOnClickListener((v) -> dismiss());
        buttonCancel.setOnClickListener((v) -> dismiss());

        requireActivity().registerReceiver(receiver, new IntentFilter(ACTION_UPDATE_CONTROLLERS_STATUS));

        return new AlertDialog.Builder(requireActivity(), R.style.CustomAlertDialog).setView(view).create();
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);
        requireActivity().unregisterReceiver(receiver);
        prepareControllersMappings();
    }

    public final static String ACTION_UPDATE_CONTROLLERS_STATUS = "com.micewine.emu.ACTION_UPDATE_CONTROLLERS_STATUS";
}