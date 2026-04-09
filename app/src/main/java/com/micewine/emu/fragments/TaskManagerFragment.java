package com.micewine.emu.fragments;

import static com.micewine.emu.activities.EmulationActivity.handler;
import static com.micewine.emu.core.WineWrapper.getExeProcesses;

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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TaskManagerFragment extends DialogFragment {
    private RecyclerView recyclerView;
    private RecyclerView.Adapter<?> adapter;
    private final List<WineWrapper.ExeProcess> processList = new ArrayList<>();
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Runnable updateProcessesRunnable = new Runnable() {
        @Override
        public void run() {
            executor.execute(() -> {
                List<WineWrapper.ExeProcess> newList = getExeProcesses();

                int oldSize = processList.size();

                processList.clear();
                processList.addAll(newList);

                int newSize = processList.size();

                recyclerView.post(() -> {
                    if (newSize > oldSize) {
                        adapter.notifyItemRangeChanged(0, oldSize, AdapterProcess.PAYLOAD_UPDATE_CPU_RAM);
                        adapter.notifyItemRangeInserted(oldSize, newSize - oldSize);
                    } else if (newSize < oldSize) {
                        adapter.notifyItemRangeChanged(0, newSize, AdapterProcess.PAYLOAD_UPDATE_CPU_RAM);
                        adapter.notifyItemRangeRemoved(newSize, oldSize - newSize);
                    } else {
                        adapter.notifyItemRangeChanged(0, newSize, AdapterProcess.PAYLOAD_UPDATE_CPU_RAM);
                    }
                });
            });

            handler.postDelayed(this, 850);
        }
    };

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        View view = requireActivity().getLayoutInflater().inflate(R.layout.fragment_task_manager, null);

        MaterialButton buttonExit = view.findViewById(R.id.buttonExit);

        recyclerView = view.findViewById(R.id.recyclerViewTaskMgr);
        adapter = new AdapterProcess(processList, requireContext());

        recyclerView.setAdapter(adapter);

        handler.post(updateProcessesRunnable);

        buttonExit.setOnClickListener((i) -> dismiss());

        return new AlertDialog.Builder(requireActivity(), R.style.CustomAlertDialog).setView(view).create();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        handler.removeCallbacks(updateProcessesRunnable);
        executor.shutdownNow();
    }
}