package com.micewine.emu.fragments;

import static com.micewine.emu.core.WineWrapper.getExeProcesses;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.micewine.emu.R;
import com.micewine.emu.adapters.AdapterProcess;
import com.micewine.emu.core.WineWrapper;

import java.util.ArrayList;
import java.util.List;

public class TaskManagerFragment extends DialogFragment {
    @SuppressLint("NotifyDataSetChanged")
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        View view = requireActivity().getLayoutInflater().inflate(R.layout.fragment_task_manager, null);

        RecyclerView recyclerView = view.findViewById(R.id.recyclerViewTaskMgr);
        MaterialButton buttonExit = view.findViewById(R.id.buttonExit);
        List<WineWrapper.ExeProcess> processList = new ArrayList<>();

        recyclerView.setAdapter(
                new AdapterProcess(processList, requireContext())
        );

        new Thread(() -> {
            while (true) {
                List<WineWrapper.ExeProcess> newList = getExeProcesses();

                recyclerView.post(() -> {
                    processList.clear();
                    processList.addAll(newList);

                    if (recyclerView.getAdapter() != null) {
                        recyclerView.getAdapter().notifyDataSetChanged();
                    }
                });

                try {
                    Thread.sleep(750);
                } catch (InterruptedException ignored) {
                }
            }
        }).start();

        buttonExit.setOnClickListener((i) -> dismiss());

        return new AlertDialog.Builder(requireActivity(), R.style.CustomAlertDialog).setView(view).create();
    }
}