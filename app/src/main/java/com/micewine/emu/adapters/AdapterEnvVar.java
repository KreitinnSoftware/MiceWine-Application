package com.micewine.emu.adapters;

import static com.micewine.emu.adapters.AdapterGame.selectedGameName;
import static com.micewine.emu.fragments.EditGamePreferencesFragment.envVars;
import static com.micewine.emu.fragments.EditGamePreferencesFragment.recyclerViewEnvVars;
import static com.micewine.emu.fragments.EnvVarsSettingsFragment.deleteCustomEnvVar;
import static com.micewine.emu.fragments.ShortcutsFragment.getEnvVars;
import static com.micewine.emu.fragments.ShortcutsFragment.removeEnvVar;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.micewine.emu.R;
import com.micewine.emu.fragments.EditEnvVarFragment;

import java.util.ArrayList;

public class AdapterEnvVar extends RecyclerView.Adapter<AdapterEnvVar.ViewHolder> {
    private final ArrayList<EnvVar> envVarsList;
    private final FragmentManager supportFragmentManager;
    private final int mode;

    public AdapterEnvVar(ArrayList<EnvVar> envVarsList, FragmentManager supportFragmentManager, int mode) {
        this.envVarsList = envVarsList;
        this.supportFragmentManager = supportFragmentManager;
        this.mode = mode;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_env_var, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        EnvVar item = envVarsList.get(position);

        holder.textViewKey.setText(item.key);
        holder.textViewValue.setText(item.value);

        holder.deleteButton.setOnClickListener((v) -> {
            if (mode == EditEnvVarFragment.MODE_EDIT_GLOBAL_VARS) {
                deleteCustomEnvVar(item.key);
            } else if (mode == EditEnvVarFragment.MODE_EDIT_GAME) {
                removeEnvVar(selectedGameName, item.key);

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
        });
    }

    @Override
    public int getItemCount() {
        return envVarsList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private final TextView textViewKey = itemView.findViewById(R.id.tvEnvVarKey);
        private final TextView textViewValue = itemView.findViewById(R.id.tvEnvVarValue);
        private final ImageButton deleteButton = itemView.findViewById(R.id.btnDeleteEnvVar);

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            new EditEnvVarFragment(EditEnvVarFragment.OPERATION_EDIT_ENV_VAR, mode, envVarsList.get(getAdapterPosition())).show(supportFragmentManager, "");
        }
    }

    public static class EnvVar {
        public String key;
        public String value;

        public EnvVar(String key, String value) {
            this.key = key;
            this.value = value;
        }
    }
}
