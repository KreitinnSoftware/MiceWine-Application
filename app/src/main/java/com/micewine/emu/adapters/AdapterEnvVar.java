package com.micewine.emu.adapters;

import static com.micewine.emu.fragments.EnvVarsSettingsFragment.deleteCustomEnvVar;

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

    public AdapterEnvVar(ArrayList<EnvVar> envVarsList, FragmentManager supportFragmentManager) {
        this.envVarsList = envVarsList;
        this.supportFragmentManager = supportFragmentManager;
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
            deleteCustomEnvVar(item.key);
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
            new EditEnvVarFragment(EditEnvVarFragment.OPERATION_EDIT_ENV_VAR, envVarsList.get(getAdapterPosition())).show(supportFragmentManager, "");
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
