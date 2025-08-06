package com.micewine.emu.fragments;

import static com.micewine.emu.adapters.AdapterGame.selectedGameName;
import static com.micewine.emu.fragments.EditGamePreferencesFragment.envVars;
import static com.micewine.emu.fragments.EditGamePreferencesFragment.recyclerViewEnvVars;
import static com.micewine.emu.fragments.EnvVarsSettingsFragment.addCustomEnvVar;
import static com.micewine.emu.fragments.EnvVarsSettingsFragment.editCustomEnvVar;
import static com.micewine.emu.fragments.ShortcutsFragment.addEnvVar;
import static com.micewine.emu.fragments.ShortcutsFragment.editEnvVar;
import static com.micewine.emu.fragments.ShortcutsFragment.getEnvVars;

import android.app.Dialog;
import android.os.Bundle;
import android.text.InputFilter;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.RecyclerView;

import com.micewine.emu.R;
import com.micewine.emu.adapters.AdapterEnvVar;

public class EditEnvVarFragment extends DialogFragment {
    private final int operation;
    private final int mode;
    private AdapterEnvVar.EnvVar envVar = null;

    public EditEnvVarFragment(int operation, int mode, AdapterEnvVar.EnvVar envVar) {
        this.operation = operation;
        this.mode = mode;
        this.envVar = envVar;
    }

    public EditEnvVarFragment(int operation, int mode) {
        this.mode = mode;
        this.operation = operation;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        LayoutInflater inflater = getLayoutInflater();
        View view = inflater.inflate(R.layout.fragment_edit_env_vars, null);

        TextView dialogTitle = view.findViewById(R.id.envDialogTitle);

        if (operation == OPERATION_ADD_ENV_VAR) {
            dialogTitle.setText(R.string.env_add_action);
        } else if (operation == OPERATION_EDIT_ENV_VAR) {
            dialogTitle.setText(R.string.env_edit_action);
        }

        EditText keyInput = view.findViewById(R.id.etDialogKey);
        EditText valueInput = view.findViewById(R.id.etDialogValue);

        keyInput.setFilters(
                new InputFilter[] {
                        (charSequence, i, i1, spanned, i2, i3) -> {
                            if (charSequence.toString().matches(".*\\s+.*")) {
                                return "";
                            }
                            return null;
                        }
                }
        );

        if (operation == OPERATION_EDIT_ENV_VAR) {
            if (envVar != null) {
                keyInput.setText(envVar.key);
                valueInput.setText(envVar.value);
            }
        }

        Button buttonSave = view.findViewById(R.id.buttonSave);
        Button buttonCancel = view.findViewById(R.id.buttonCancel);

        buttonSave.setOnClickListener((v) -> {
            String key = keyInput.getText().toString().trim();
            String value = valueInput.getText().toString().trim();

            if (key.isEmpty() || value.isEmpty()) {
                Toast.makeText(requireContext(), R.string.invalid_variable_name, Toast.LENGTH_SHORT).show();
                return;
            }

            if (mode == MODE_EDIT_GLOBAL_VARS) {
                if (operation == OPERATION_ADD_ENV_VAR) {
                    addCustomEnvVar(key, value);
                } else if (operation == OPERATION_EDIT_ENV_VAR) {
                    editCustomEnvVar(envVar.key, key, value);
                }
            } else if (mode == MODE_EDIT_GAME) {
                if (operation == OPERATION_ADD_ENV_VAR) {
                    addEnvVar(selectedGameName, new AdapterEnvVar.EnvVar(key, value));
                } else if (operation == OPERATION_EDIT_ENV_VAR) {
                    editEnvVar(selectedGameName, envVar.key, key, value);
                }

                envVars.clear();
                envVars.addAll(getEnvVars(selectedGameName));

                if (recyclerViewEnvVars != null) {
                    recyclerViewEnvVars.post(() -> {
                        RecyclerView.Adapter<?> adapter = recyclerViewEnvVars.getAdapter();
                        if (adapter != null) {
                            adapter.notifyDataSetChanged();
                        }
                    });
                }
            }

            dismiss();
        });

        buttonCancel.setOnClickListener((v) -> dismiss());

        return new AlertDialog.Builder(requireActivity(), R.style.CustomAlertDialog).setView(view).create();
    }

    public final static int OPERATION_ADD_ENV_VAR = 0;
    public final static int OPERATION_EDIT_ENV_VAR = 1;
    public final static int MODE_EDIT_GLOBAL_VARS = 2;
    public final static int MODE_EDIT_GAME = 3;
}
