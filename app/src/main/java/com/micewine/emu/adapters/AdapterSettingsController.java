package com.micewine.emu.adapters;

import static com.micewine.emu.adapters.AdapterPreset.clickedPresetName;
import static com.micewine.emu.controller.XKeyCodes.getMapping;
import static com.micewine.emu.fragments.ControllerPresetManagerFragment.editControllerPreset;
import static com.micewine.emu.fragments.ControllerPresetManagerFragment.getControllerPreset;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.micewine.emu.R;
import com.micewine.emu.controller.XKeyCodes;

import java.util.List;

public class AdapterSettingsController extends RecyclerView.Adapter<AdapterSettingsController.ViewHolder> {
    private final List<SettingsController> settingsControllerList;
    private final Context context;

    public AdapterSettingsController(List<SettingsController> settingsControllerList, Context context) {
        this.settingsControllerList = settingsControllerList;
        this.context = context;
    }

    private final List<String> allEntries = XKeyCodes.getKeyNames(true);
    private final List<String> keyEntries = XKeyCodes.getKeyNames(false);

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_settings_controller_item, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        SettingsController item = settingsControllerList.get(position);

        holder.image.setImageResource(item.imageId);

        XKeyCodes.ButtonMapping mapping = getControllerPreset(clickedPresetName, item.key);

        if (item.imageId == R.drawable.l_up || item.imageId == R.drawable.l_down ||
            item.imageId == R.drawable.l_left || item.imageId == R.drawable.l_right ||
            item.imageId == R.drawable.r_up || item.imageId == R.drawable.r_down ||
            item.imageId == R.drawable.r_left || item.imageId == R.drawable.r_right
        ) {
            holder.keyBindSpinner.setAdapter(new ArrayAdapter<>(context, android.R.layout.simple_spinner_dropdown_item, keyEntries));
            holder.keyBindSpinner.setSelection(keyEntries.indexOf(mapping != null ? mapping.getName() : null));
        } else {
            holder.keyBindSpinner.setAdapter(new ArrayAdapter<>(context, android.R.layout.simple_spinner_dropdown_item, allEntries));
            holder.keyBindSpinner.setSelection(allEntries.indexOf(mapping != null ? mapping.getName() : null));
        }

        holder.keyBindSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                editControllerPreset(clickedPresetName, item.key, getMapping(adapterView.getSelectedItem().toString()));
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });
    }

    @Override
    public int getItemCount() {
        return settingsControllerList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        Spinner keyBindSpinner = itemView.findViewById(R.id.keyBindSpinner);
        ImageView image = itemView.findViewById(R.id.buttonImageView);

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
        }
    }

    public static class SettingsController {
        public int imageId;
        public String key;

        public SettingsController(int imageId, String key) {
            this.imageId = imageId;
            this.key = key;
        }
    }
}