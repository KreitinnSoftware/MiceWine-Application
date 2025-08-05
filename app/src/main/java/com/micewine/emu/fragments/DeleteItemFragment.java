package com.micewine.emu.fragments;

import static com.micewine.emu.activities.MainActivity.selectedFilePath;
import static com.micewine.emu.activities.MainActivity.selectedFragmentId;
import static com.micewine.emu.adapters.AdapterGame.selectedGameName;
import static com.micewine.emu.adapters.AdapterPreset.clickedPresetName;
import static com.micewine.emu.adapters.AdapterPreset.clickedPresetType;
import static com.micewine.emu.fragments.Box64PresetManagerFragment.deleteBox64Preset;
import static com.micewine.emu.fragments.ControllerPresetManagerFragment.deleteControllerPreset;
import static com.micewine.emu.fragments.CreatePresetFragment.BOX64_PRESET;
import static com.micewine.emu.fragments.CreatePresetFragment.CONTROLLER_PRESET;
import static com.micewine.emu.fragments.CreatePresetFragment.VIRTUAL_CONTROLLER_PRESET;
import static com.micewine.emu.fragments.CreatePresetFragment.WINE_PREFIX_PRESET;
//import static com.micewine.emu.fragments.FileManagerFragment.deleteFile;
import static com.micewine.emu.fragments.FileManagerFragment.deleteFile;
import static com.micewine.emu.fragments.ShortcutsFragment.removeGameFromList;
import static com.micewine.emu.fragments.VirtualControllerPresetManagerFragment.deleteVirtualControllerPreset;
import static com.micewine.emu.fragments.WinePrefixManagerFragment.deleteWinePrefix;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.micewine.emu.R;

public class DeleteItemFragment extends DialogFragment {
    private final int deleteType;

    public DeleteItemFragment(int deleteType) {
        this.deleteType = deleteType;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        LayoutInflater inflater = getLayoutInflater();
        View view = inflater.inflate(R.layout.fragment_delete_item, null);

        Button buttonContinue = view.findViewById(R.id.buttonContinue);
        Button buttonCancel = view.findViewById(R.id.buttonCancel);

        buttonContinue.setOnClickListener((v) -> {
            switch (deleteType) {
                case DELETE_GAME_ITEM: {
                    if (selectedFragmentId == 0) {
                        removeGameFromList(selectedGameName);
                    } else if (selectedFragmentId == 2) {
                        deleteFile(selectedFilePath);
                    }
                    break;
                }
                case DELETE_PRESET: {
                    switch (clickedPresetType) {
                        case CONTROLLER_PRESET: {
                            deleteControllerPreset(clickedPresetName);
                            break;
                        }
                        case VIRTUAL_CONTROLLER_PRESET: {
                            deleteVirtualControllerPreset(clickedPresetName);
                            break;
                        }
                        case BOX64_PRESET: {
                            if (!deleteBox64Preset(clickedPresetName)) {
                                Toast.makeText(requireContext(), R.string.remove_last_preset_error, Toast.LENGTH_SHORT).show();
                            }
                            break;
                        }
                        case WINE_PREFIX_PRESET: {
                            if (!deleteWinePrefix(clickedPresetName)) {
                                Toast.makeText(requireContext(), R.string.remove_last_wine_prefix_error, Toast.LENGTH_SHORT).show();
                            }
                            break;
                        }
                    }
                }
            }
            dismiss();
        });

        buttonCancel.setOnClickListener((v) -> dismiss());

        return new AlertDialog.Builder(requireActivity(), R.style.CustomAlertDialog).setView(view).create();
    }

    public final static int DELETE_GAME_ITEM = 0;
    public final static int DELETE_PRESET = 1;
}