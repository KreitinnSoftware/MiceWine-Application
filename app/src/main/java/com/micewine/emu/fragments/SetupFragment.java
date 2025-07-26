package com.micewine.emu.fragments;

import static com.micewine.emu.activities.MainActivity.customRootFSPath;
import static com.micewine.emu.activities.MainActivity.setupDone;
import static com.micewine.emu.fragments.FloatingFileManagerFragment.calledSetup;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.micewine.emu.R;

public class SetupFragment extends DialogFragment {
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.fragment_setup, null);

        TextView titleText = view.findViewById(R.id.titleText);
        TextView progressTextBar = view.findViewById(R.id.updateProgress);
        ProgressBar progressExtractBar = view.findViewById(R.id.progressBar);

        setCancelable(false);

        new Thread(() -> {
            while (!setupDone && !abortSetup) {
                String progressText;

                if (progressBarValue > 0) {
                    progressText = progressBarValue + "%";
                } else {
                    progressText = "";
                }

                view.post(() -> {
                    titleText.setText(dialogTitleText);
                    progressTextBar.setText(progressText);
                    progressExtractBar.setProgress(progressBarValue);
                    progressExtractBar.setIndeterminate(progressBarIsIndeterminate);
                });

                try {
                    Thread.sleep(16);
                } catch (InterruptedException ignored) {
                }

                if (abortSetup) {
                    abortSetup = false;
                    calledSetup = false;
                    customRootFSPath = null;
                }
            }

            dismiss();
        }).start();

        return new AlertDialog.Builder(requireActivity(), R.style.CustomAlertDialog).setView(view).create();
    }

    public static int progressBarValue = 0;
    public static boolean progressBarIsIndeterminate = false;
    public static String dialogTitleText = "";
    public static boolean abortSetup = false;
}