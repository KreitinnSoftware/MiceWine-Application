package com.micewine.emu.fragments;

import static com.micewine.emu.activities.MainActivity.selectedFile;
import static com.micewine.emu.adapters.AdapterPreset.clickedPresetName;
import static com.micewine.emu.adapters.AdapterPreset.clickedPresetType;
import static com.micewine.emu.fragments.Box64PresetManagerFragment.renameBox64Preset;
import static com.micewine.emu.fragments.ControllerPresetManagerFragment.renameControllerPreset;
import static com.micewine.emu.fragments.CreatePresetFragment.BOX64_PRESET;
import static com.micewine.emu.fragments.CreatePresetFragment.CONTROLLER_PRESET;
import static com.micewine.emu.fragments.CreatePresetFragment.VIRTUAL_CONTROLLER_PRESET;
import static com.micewine.emu.fragments.FileManagerFragment.renameFile;
import static com.micewine.emu.fragments.VirtualControllerPresetManagerFragment.renameVirtualControllerPreset;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.micewine.emu.R;

import java.io.File;

public class RenameFragment extends DialogFragment {
    private final int type;
    private final String initialText;

    public RenameFragment(int type, String initialText) {
        this.type = type;
        this.initialText = initialText;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.fragment_rename_game_item, null);
        EditText editTextNewName = view.findViewById(R.id.editTextNewName);
        Button buttonContinue = view.findViewById(R.id.buttonContinue);
        Button buttonCancel = view.findViewById(R.id.buttonCancel);

        editTextNewName.setText(initialText);

        buttonContinue.setOnClickListener((v) -> {
            String newName = editTextNewName.getText().toString().trim();
            if (newName.isEmpty()) {
                dismiss();
            }

            switch (type) {
                case RENAME_PRESET -> {
                    switch (clickedPresetType) {
                        case CONTROLLER_PRESET -> renameControllerPreset(clickedPresetName, newName);
                        case VIRTUAL_CONTROLLER_PRESET -> renameVirtualControllerPreset(clickedPresetName, newName);
                        case BOX64_PRESET -> renameBox64Preset(clickedPresetName, newName);
                    }
                }
                case RENAME_FILE -> {
                    File file = new File(selectedFile);
                    File newFile = new File(file.getParentFile(), newName);

                    renameFile(selectedFile, newFile.getPath(), requireContext());
                }
            }

            dismiss();
        });

        buttonCancel.setOnClickListener((v) -> dismiss());

        return new AlertDialog.Builder(requireContext(), R.style.CustomAlertDialog).setView(view).create();
    }

    public final static int RENAME_PRESET = 0;
    public final static int RENAME_FILE = 1;
}