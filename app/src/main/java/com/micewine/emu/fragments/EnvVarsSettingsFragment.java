package com.micewine.emu.fragments;

import static com.micewine.emu.activities.MainActivity.gson;
import static com.micewine.emu.activities.MainActivity.preferences;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.reflect.TypeToken;
import com.micewine.emu.R;
import com.micewine.emu.adapters.AdapterEnvVar;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.stream.IntStream;

public class EnvVarsSettingsFragment extends Fragment {
    private static final ArrayList<AdapterEnvVar.EnvVar> envVarsList = getCustomEnvVars();
    private static RecyclerView recyclerView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_env_vars_settings, container, false);

        FloatingActionButton addEnvButton = rootView.findViewById(R.id.addEnvVar);
        recyclerView = rootView.findViewById(R.id.rvEnvVars);

        setAdapter();

        addEnvButton.setOnClickListener((v) -> {
            new EditEnvVarFragment(EditEnvVarFragment.OPERATION_ADD_ENV_VAR).show(requireActivity().getSupportFragmentManager(), "");
        });

        return rootView;
    }

    private void setAdapter() {
        recyclerView.setAdapter(new AdapterEnvVar(envVarsList, requireActivity().getSupportFragmentManager()));
    }

    public final static String ENV_VARS_KEY = "environmentVariables";

    public static void addCustomEnvVar(String key, String value) {
        AdapterEnvVar.EnvVar envVar = new AdapterEnvVar.EnvVar(key, value);

        envVarsList.add(envVar);

        recyclerView.post(() -> {
            RecyclerView.Adapter<?> adapter = recyclerView.getAdapter();
            if (adapter != null) {
                adapter.notifyItemInserted(envVarsList.size());
            }
        });

        saveCustomEnvVars();
    }

    public static void deleteCustomEnvVar(String key) {
        int index = IntStream.range(0, envVarsList.size()).filter(i -> envVarsList.get(i).key.equals(key)).findFirst().orElse(-1);
        if (index == -1) return;

        envVarsList.remove(index);

        recyclerView.post(() -> {
            RecyclerView.Adapter<?> adapter = recyclerView.getAdapter();
            if (adapter != null) {
                adapter.notifyItemRemoved(envVarsList.size() + 1);
            }
        });

        saveCustomEnvVars();
    }

    public static void editCustomEnvVar(String key, String value) {
        int index = IntStream.range(0, envVarsList.size()).filter(i -> envVarsList.get(i).key.equals(key)).findFirst().orElse(-1);
        if (index == -1) return;

        envVarsList.get(index).key = key;
        envVarsList.get(index).value = value;

        recyclerView.post(() -> {
            RecyclerView.Adapter<?> adapter = recyclerView.getAdapter();
            if (adapter != null) {
                adapter.notifyItemChanged(index);
            }
        });

        saveCustomEnvVars();
    }

    public static void saveCustomEnvVars() {
        SharedPreferences.Editor editor = preferences.edit();

        editor.putString(ENV_VARS_KEY, gson.toJson(envVarsList));
        editor.apply();
    }

    public static ArrayList<AdapterEnvVar.EnvVar> getCustomEnvVars() {
        String json = preferences.getString(ENV_VARS_KEY, "");
        Type listType = new TypeToken<ArrayList<AdapterEnvVar.EnvVar>>() {}.getType();
        ArrayList<AdapterEnvVar.EnvVar> envVarsList = gson.fromJson(json, listType);

        return (envVarsList != null ? envVarsList : new ArrayList<>());
    }
}