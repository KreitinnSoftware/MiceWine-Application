package com.micewine.emu.fragments;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.button.MaterialButton;
import com.micewine.emu.R;

public class InfoDialogFragment extends DialogFragment {
    private final String titleText;
    private final String descriptionText;

    public InfoDialogFragment(String titleText, String descriptionText) {
        this.titleText = titleText;
        this.descriptionText = descriptionText;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        LayoutInflater inflater = getLayoutInflater();
        View view = inflater.inflate(R.layout.fragment_info, null);

        TextView titleTextView = view.findViewById(R.id.titleText);
        titleTextView.setText(titleText);

        TextView descriptionTextView = view.findViewById(R.id.descriptionText);
        descriptionTextView.setText(descriptionText);

        MaterialButton button = view.findViewById(R.id.okButton);
        button.setText(android.R.string.ok);
        button.setOnClickListener((v) -> dismiss());

        return new AlertDialog.Builder(requireActivity(), R.style.CustomAlertDialog).setView(view).create();
    }
}