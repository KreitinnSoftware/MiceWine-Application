package com.micewine.emu.fragments;

import android.annotation.SuppressLint;
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

import java.util.ArrayList;
import java.util.List;

public class SetupFragment extends DialogFragment {
    private TextView titleText = null;
    private TextView progressTextBar = null;
    private ProgressBar progressExtractBar = null;
    private Dialog dialog = null;

    private final List<Runnable> pendingQueue = new ArrayList<>();
    private boolean viewReady = false;

    private void enqueueOrRun(Runnable action) {
        if (viewReady) {
            action.run();
        } else {
            pendingQueue.add(action);
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.fragment_setup, null);

        titleText = view.findViewById(R.id.titleText);
        progressTextBar = view.findViewById(R.id.updateProgress);
        progressExtractBar = view.findViewById(R.id.progressBar);

        viewReady = true;

        for (Runnable r : pendingQueue) {
            r.run();
        }
        pendingQueue.clear();

        setCancelable(false);

        dialog = new AlertDialog.Builder(requireActivity(), R.style.CustomAlertDialog).setView(view).create();

        return dialog;
    }

    public interface ProgressCallback {
        void onProgressChanged(int progress);
        void setProgressBarIndeterminate(boolean indeterminate);
        void setDialogText(String text);
    }

    public ProgressCallback setupProgressCallback = new ProgressCallback() {
        @SuppressLint("SetTextI18n")
        @Override
        public void onProgressChanged(int progress) {
            enqueueOrRun(() -> {
                progressExtractBar.post(() -> progressExtractBar.setProgress(progress));
                progressTextBar.post(() -> progressTextBar.setText(progress + "%"));
            });
        }

        @Override
        public void setProgressBarIndeterminate(boolean indeterminate) {
            enqueueOrRun(() -> progressExtractBar.post(() -> progressExtractBar.setIndeterminate(indeterminate)));
        }

        @Override
        public void setDialogText(String text) {
            enqueueOrRun(() -> titleText.post(() -> titleText.setText(text)));
        }
    };
}