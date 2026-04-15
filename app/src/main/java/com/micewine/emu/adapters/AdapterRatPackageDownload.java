package com.micewine.emu.adapters;

import static com.micewine.emu.adapters.AdapterFiles.MEGABYTE;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.micewine.emu.R;

import java.util.ArrayList;
import java.util.List;

public class AdapterRatPackageDownload extends RecyclerView.Adapter<AdapterRatPackageDownload.ViewHolder> {
    private final ArrayList<Item> ratPackagesList;
    private final Context context;

    public AdapterRatPackageDownload(ArrayList<Item> ratPackagesList, Context context) {
        this.ratPackagesList = ratPackagesList;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_rat_package_item, parent, false);
        return new ViewHolder(itemView);
    }

    @SuppressLint("DefaultLocale")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Item item = ratPackagesList.get(position);

        holder.progressBar.setVisibility(item.installed ? View.GONE : View.VISIBLE);
        holder.progressText.setVisibility(item.installed ? View.GONE : View.VISIBLE);

        holder.checkBox.setVisibility(View.GONE);

        holder.downloadRatPackageButton.setVisibility(item.installed ? View.VISIBLE : View.GONE);
        holder.downloadRatPackageButton.setImageResource(android.R.drawable.checkbox_on_background);

        holder.radioButton.setVisibility(View.GONE);
        holder.imageView.setVisibility(View.GONE);

        holder.deleteRatPackageButton.setVisibility(View.GONE);

        holder.settingsName.setText(item.titleSettings);

        if (item.descriptionSettings.isEmpty()) {
            holder.settingsDescription.setVisibility(View.GONE);
        } else {
            holder.settingsDescription.setText(item.descriptionSettings);
        }

        if (item.installed) {
            holder.downloadRatPackageButton.setVisibility(View.VISIBLE);
            holder.downloadRatPackageButton.setImageResource(android.R.drawable.checkbox_on_background);

            holder.progressText.setVisibility(View.GONE);
            holder.progressBar.setVisibility(View.GONE);

            return;
        }

        String progressBarText;

        if (item.installing) {
            progressBarText = context.getString(R.string.installing) + " - " + item.progress + "%";
        } else {
            if (item.contentLength == 0) {
                progressBarText = context.getString(R.string.waiting);
            } else {
                progressBarText = String.format("%s%% - %.2fM/%.2fM - %.2f MB/s", item.progress, (float) item.bytesRead / MEGABYTE, (float) item.contentLength / MEGABYTE, item.megabytesPerSecond);
            }
        }

        holder.progressText.setText(progressBarText);
        holder.progressBar.setProgress(item.progress);
    }

    @SuppressLint({"SetTextI18n", "DefaultLocale"})
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position, @NonNull List<Object> payloads) {
        Item item = ratPackagesList.get(position);

        if (!payloads.isEmpty() && payloads.contains(PAYLOAD_UPDATE_PROGRESS)) {
            if (item.installed) return;

            String progressBarText;

            if (item.installing) {
                progressBarText = context.getString(R.string.installing) + " - " + item.progress + "%";
            } else {
                progressBarText = String.format("%s%% - %.2fM/%.2fM - %.2f MB/s", item.progress, (float) item.bytesRead / MEGABYTE, (float) item.contentLength / MEGABYTE, item.megabytesPerSecond);
            }

            holder.progressText.setText(progressBarText);
            holder.progressBar.setProgress(item.progress);

            return;
        } else if (!payloads.isEmpty() && payloads.contains(PAYLOAD_MARK_INSTALLED)) {
            holder.downloadRatPackageButton.setVisibility(View.VISIBLE);
            holder.downloadRatPackageButton.setImageResource(android.R.drawable.checkbox_on_background);

            holder.progressText.setVisibility(View.GONE);
            holder.progressBar.setVisibility(View.GONE);

            return;
        }

        onBindViewHolder(holder, position);
    }

    @Override
    public int getItemCount() {
        return ratPackagesList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        RadioButton radioButton = itemView.findViewById(R.id.radio_button);
        CheckBox checkBox = itemView.findViewById(R.id.checkbox);
        TextView settingsName = itemView.findViewById(R.id.preset_title);
        TextView settingsDescription = itemView.findViewById(R.id.rat_package_desc);
        ImageView imageView = itemView.findViewById(R.id.image_view);
        ImageButton deleteRatPackageButton = itemView.findViewById(R.id.rat_package_delete);
        ImageView downloadRatPackageButton = itemView.findViewById(R.id.rat_package_download);
        ProgressBar progressBar = itemView.findViewById(R.id.progressBar);
        TextView progressText = itemView.findViewById(R.id.progressText);

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
        }

        @Override
        public void onClick(View view) {
        }
    }

    public static class Item {
        public String titleSettings;
        public String descriptionSettings;
        public boolean installing = false;
        public boolean installed = false;
        public int progress = 0;
        public float megabytesPerSecond = 0;
        public long bytesRead = 0;
        public long contentLength = 0;

        public Item(String titleSettings, String descriptionSettings) {
            this.titleSettings = titleSettings;
            this.descriptionSettings = descriptionSettings;
        }
    }

    public final static int PAYLOAD_UPDATE_PROGRESS = 0;
    public final static int PAYLOAD_MARK_INSTALLED = 1;
}
