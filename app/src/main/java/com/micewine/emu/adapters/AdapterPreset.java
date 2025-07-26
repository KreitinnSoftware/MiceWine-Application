package com.micewine.emu.adapters;

import static com.micewine.emu.activities.PresetManagerActivity.ACTION_EDIT_BOX64_PRESET;
import static com.micewine.emu.activities.PresetManagerActivity.ACTION_EDIT_CONTROLLER_MAPPING;
import static com.micewine.emu.adapters.AdapterGame.selectedGameName;
import static com.micewine.emu.fragments.CreatePresetFragment.BOX64_PRESET;
import static com.micewine.emu.fragments.CreatePresetFragment.CONTROLLER_PRESET;
import static com.micewine.emu.fragments.CreatePresetFragment.VIRTUAL_CONTROLLER_PRESET;
import static com.micewine.emu.fragments.CreatePresetFragment.WINE_PREFIX_PRESET;
import static com.micewine.emu.fragments.DeleteItemFragment.DELETE_PRESET;
import static com.micewine.emu.fragments.FloatingFileManagerFragment.OPERATION_EXPORT_PRESET;
import static com.micewine.emu.fragments.RenameFragment.RENAME_PRESET;
import static com.micewine.emu.fragments.ShortcutsFragment.getBox64Preset;
import static com.micewine.emu.fragments.ShortcutsFragment.getControllerPreset;
import static com.micewine.emu.fragments.ShortcutsFragment.getSelectedVirtualControllerPreset;
import static com.micewine.emu.fragments.ShortcutsFragment.putBox64Preset;
import static com.micewine.emu.fragments.ShortcutsFragment.putControllerPreset;
import static com.micewine.emu.fragments.ShortcutsFragment.putSelectedVirtualControllerPreset;
import static com.micewine.emu.fragments.WinePrefixManagerFragment.getSelectedWinePrefix;
import static com.micewine.emu.fragments.WinePrefixManagerFragment.putSelectedWinePrefix;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.PopupMenu;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.micewine.emu.R;
import com.micewine.emu.activities.VirtualControllerOverlayMapper;
import com.micewine.emu.fragments.DeleteItemFragment;
import com.micewine.emu.fragments.FloatingFileManagerFragment;
import com.micewine.emu.fragments.RenameFragment;

import java.util.ArrayList;

public class AdapterPreset extends RecyclerView.Adapter<AdapterPreset.ViewHolder> {
    private final ArrayList<Item> presetList;
    private final Context context;
    private final FragmentManager supportFragmentManager;

    public AdapterPreset(ArrayList<Item> settingsList, Context context, FragmentManager supportFragmentManager) {
        this.presetList = settingsList;
        this.context = context;
        this.supportFragmentManager = supportFragmentManager;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_preset_item, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Item item = presetList.get(position);

        holder.settingsName.setText(item.titleSettings);

        holder.moreButton.setVisibility(item.isUserPreset ? View.VISIBLE : View.GONE);

        if (item.showRadioButton) {
            switch (item.type) {
                case CONTROLLER_PRESET -> {
                    if (item.titleSettings.equals(getControllerPreset(selectedGameName, 0))) {
                        selectedPresetId = holder.getAdapterPosition();
                    }
                    holder.radioButton.setOnClickListener((v) -> {
                        putControllerPreset(selectedGameName, holder.settingsName.getText().toString(), 0);
                        selectedPresetId = holder.getAdapterPosition();
                        notifyItemRangeChanged(0, presetList.size());
                    });
                }
                case VIRTUAL_CONTROLLER_PRESET -> {
                    if (item.titleSettings.equals(getSelectedVirtualControllerPreset(selectedGameName))) {
                        selectedPresetId = holder.getAdapterPosition();
                    }
                    holder.radioButton.setOnClickListener((v) -> {
                        putSelectedVirtualControllerPreset(selectedGameName, holder.settingsName.getText().toString());
                        selectedPresetId = holder.getAdapterPosition();
                        notifyItemRangeChanged(0, presetList.size());
                    });
                }
                case BOX64_PRESET -> {
                    if (item.titleSettings.equals(getBox64Preset(selectedGameName))) {
                        selectedPresetId = holder.getAdapterPosition();
                    }
                    holder.radioButton.setOnClickListener((v) -> {
                        putBox64Preset(selectedGameName, holder.settingsName.getText().toString());
                        selectedPresetId = holder.getAdapterPosition();
                        notifyItemRangeChanged(0, presetList.size());
                    });
                }
                case WINE_PREFIX_PRESET -> {
                    if (item.titleSettings.equals(getSelectedWinePrefix())) {
                        selectedPresetId = holder.getAdapterPosition();
                    }
                    holder.radioButton.setOnClickListener((v) -> {
                        putSelectedWinePrefix(holder.settingsName.getText().toString());
                        selectedPresetId = holder.getAdapterPosition();
                        notifyItemRangeChanged(0, presetList.size());
                    });
                }
            }
            holder.radioButton.setChecked(position == selectedPresetId);
            holder.radioButton.setVisibility(View.VISIBLE);
        } else {
            holder.radioButton.setVisibility(View.GONE);
        }

        holder.moreButton.setOnClickListener((v) -> {
            PopupMenu popupMenu = new PopupMenu(context, holder.moreButton);
            MenuInflater inflater = popupMenu.getMenuInflater();
            inflater.inflate(R.menu.preset_more_options_menu, popupMenu.getMenu());

            popupMenu.setOnMenuItemClickListener(menuItem -> {
                if (menuItem.getItemId() == R.id.editPreset) {
                    clickedPresetName = item.titleSettings;
                    clickedPresetType = item.type;

                    switch (item.type) {
                        case CONTROLLER_PRESET -> context.sendBroadcast(new Intent(ACTION_EDIT_CONTROLLER_MAPPING));
                        case VIRTUAL_CONTROLLER_PRESET -> context.startActivity(new Intent(context, VirtualControllerOverlayMapper.class));
                        case BOX64_PRESET -> context.sendBroadcast(new Intent(ACTION_EDIT_BOX64_PRESET));
                    }
                } else if (menuItem.getItemId() == R.id.deletePreset) {
                    clickedPresetName = item.titleSettings;
                    clickedPresetType = item.type;

                    new DeleteItemFragment(DELETE_PRESET).show(supportFragmentManager, "");
                } else if (menuItem.getItemId() == R.id.renamePreset) {
                    clickedPresetName = item.titleSettings;
                    clickedPresetType = item.type;

                    new RenameFragment(RENAME_PRESET, clickedPresetName).show(supportFragmentManager, "");
                } else if (menuItem.getItemId() == R.id.exportPreset) {
                    clickedPresetName = item.titleSettings;
                    clickedPresetType = item.type;

                    new FloatingFileManagerFragment(OPERATION_EXPORT_PRESET, "/storage/emulated/0").show(supportFragmentManager, "");
                } else {
                    return false;
                }

                return true;
            });

            popupMenu.show();
        });
    }

    @Override
    public int getItemCount() {
        return presetList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private final RadioButton radioButton = itemView.findViewById(R.id.radioButton);
        private final TextView settingsName = itemView.findViewById(R.id.presetTitle);
        private final ImageButton moreButton = itemView.findViewById(R.id.moreButton);

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
        }
    }

    public static class Item {
        public String titleSettings;
        public int type;
        public boolean isUserPreset;
        public boolean showRadioButton;

        public Item(String titleSettings, int type, boolean isUserPreset) {
            this.titleSettings = titleSettings;
            this.type = type;
            this.isUserPreset = isUserPreset;
            this.showRadioButton = false;
        }

        public Item(String titleSettings, int type, boolean isUserPreset, boolean showRadioButton) {
            this.titleSettings = titleSettings;
            this.type = type;
            this.isUserPreset = isUserPreset;
            this.showRadioButton = showRadioButton;
        }

        public String getTitleSettings() {
            return this.titleSettings;
        }

        public void setTitleSettings(String text) {
            this.titleSettings = text;
        }
    }

    public static String clickedPresetName = "";
    public static int clickedPresetType = -1;
    public static int selectedPresetId = -1;
}