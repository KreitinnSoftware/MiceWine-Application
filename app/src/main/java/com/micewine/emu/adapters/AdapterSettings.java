package com.micewine.emu.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.micewine.emu.LoriePreferences;
import com.micewine.emu.R;
import com.micewine.emu.activities.logAppOutput;
import com.micewine.emu.models.SettingsList;

import java.util.List;

public class AdapterSettings extends RecyclerView.Adapter<AdapterSettings.ViewHolder> {

    private final Context context;
    private final List<SettingsList> SettingsList;

    public AdapterSettings(List<SettingsList> SettingsList, Context context) {
        this.SettingsList = SettingsList;
        this.context = context;
    }

    @NonNull
    @Override
    public AdapterSettings.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.settings_item, parent, false);
        return new ViewHolder(itemView);
    }


    @Override
    public void onBindViewHolder(@NonNull AdapterSettings.ViewHolder holder, int position) {

        SettingsList slist = SettingsList.get(position);
        holder.settinsName.setText(slist.getTitleSettings());
        holder.settingsDescription.setText(slist.getDescriptionSettings());
        holder.imageSettings.setImageResource(slist.getImageSettings());
//        Picasso.get().load(slist.getImageSettings()).into(holder.imageSettings);

        // implementation 'com.squareup.picasso:picasso:2.71828'
    }

    @Override
    public int getItemCount() {
        return SettingsList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private final TextView settinsName;
        private final TextView settingsDescription;
        private final ImageView imageSettings;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            settinsName = itemView.findViewById(R.id.title_preferences_model);
            settingsDescription = itemView.findViewById(R.id.description_preferences_model);
            imageSettings = itemView.findViewById(R.id.set_img);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            SettingsList settingsModel = SettingsList.get(getAdapterPosition());
            if (R.string.x11_preference_title == settingsModel.getTitleSettings()) {
                Intent intent = new Intent(context, LoriePreferences.class);
                context.startActivity(intent);
            } else if (R.string.log_level_normal == settingsModel.getTitleSettings()) {
                Intent intent = new Intent(context, logAppOutput.class);
                context.startActivity(intent);
            } else if (R.string.about_preferences_title == settingsModel.getTitleSettings()) {
                //dispois
            }
        }
    }
}