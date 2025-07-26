package com.micewine.emu.fragments;

import static com.micewine.emu.fragments.EnvVarsSettingsFragment.addCustomEnvVar;
import static com.micewine.emu.fragments.EnvVarsSettingsFragment.editCustomEnvVar;

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

import com.micewine.emu.R;
import com.micewine.emu.adapters.AdapterEnvVar;

public class EditEnvVarFragment extends DialogFragment {
    private final int operation;
    private AdapterEnvVar.EnvVar envVar = null;

    public EditEnvVarFragment(int operation, AdapterEnvVar.EnvVar envVar) {
        this.operation = operation;
        this.envVar = envVar;
    }

    public EditEnvVarFragment(int operation) {
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

            if (operation == OPERATION_ADD_ENV_VAR) {
                addCustomEnvVar(key, value);
            } else if (operation == OPERATION_EDIT_ENV_VAR) {
                editCustomEnvVar(key, value);
            }

            dismiss();
        });

        buttonCancel.setOnClickListener((v) -> dismiss());

        return new AlertDialog.Builder(requireActivity(), R.style.CustomAlertDialog).setView(view).create();
    }

    public final static int OPERATION_ADD_ENV_VAR = 0;
    public final static int OPERATION_EDIT_ENV_VAR = 1;
}
