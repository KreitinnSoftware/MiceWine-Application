package com.micewine.emu.adapters;

import static com.micewine.emu.activities.GeneralSettingsActivity.SELECTED_BOX64;
import static com.micewine.emu.activities.GeneralSettingsActivity.SELECTED_CORE;
import static com.micewine.emu.activities.GeneralSettingsActivity.SELECTED_VULKAN_DRIVER;
import static com.micewine.emu.activities.MainActivity.preferences;
import static com.micewine.emu.activities.MainActivity.tmpDir;
import static com.micewine.emu.core.RatPackageManager.deleteRatPackageById;
import static com.micewine.emu.core.RatPackageManager.installRat;
import static com.micewine.emu.core.RatPackageManager.listRatPackages;
import static com.micewine.emu.fragments.RatDownloaderFragment.downloadPackage;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
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
import com.micewine.emu.core.RatPackageManager;
import com.micewine.emu.fragments.SetupFragment;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class AdapterRatPackage extends RecyclerView.Adapter<AdapterRatPackage.ViewHolder> {
    private final ArrayList<Item> ratPackagesList;
    private final Context context;
    private final boolean isRepositoryPackage;
    public static int selectedItemId = -1;
    public boolean isInitialSetup;
    public static List<HashSet<Integer>> selectedItemsId = new ArrayList<>(7);

    static {
        for (int i = 0; i < 7; i++) {
            selectedItemsId.add(new HashSet<>());
        }
    }

    public AdapterRatPackage(ArrayList<Item> ratPackagesList, Context context, boolean isRepositoryPackage, boolean isInitialSetup) {
        this.ratPackagesList = ratPackagesList;
        this.context = context;
        this.isRepositoryPackage = isRepositoryPackage;
        this.isInitialSetup = isInitialSetup;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_rat_package_item, parent, false);
        return new ViewHolder(itemView);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Item item = ratPackagesList.get(position);

        if (isRepositoryPackage) {
            if (item.isInstalled) {
                holder.downloadRatPackageButton.setEnabled(false);
                holder.downloadRatPackageButton.setImageResource(android.R.drawable.checkbox_on_background);
            } else {
                holder.downloadRatPackageButton.setEnabled(true);
                holder.downloadRatPackageButton.setImageResource(R.drawable.ic_download);
            }
        }

        holder.progressBar.setVisibility(View.GONE);
        holder.progressText.setVisibility(View.GONE);

        holder.checkBox.setVisibility(isInitialSetup ? View.VISIBLE : View.GONE);
        holder.downloadRatPackageButton.setVisibility(isInitialSetup || !isRepositoryPackage ? View.GONE : View.VISIBLE);

        String selectedItem = "";

        switch (item.type) {
            case VK_DRIVER -> {
                selectedItem = preferences.getString(SELECTED_VULKAN_DRIVER, "");

                if (isRepositoryPackage) {
                    holder.radioButton.setVisibility(View.GONE);
                }
                holder.imageView.setImageResource(R.drawable.ic_gpu);
            }
            case BOX64 -> {
                selectedItem = preferences.getString(SELECTED_BOX64, "");

                if (isRepositoryPackage) {
                    holder.radioButton.setVisibility(View.GONE);
                }
                holder.imageView.setImageResource(R.drawable.ic_box64);
            }
            case WINE -> {
                holder.radioButton.setVisibility(View.GONE);
                holder.imageView.setImageResource(R.drawable.ic_wine);
            }
            case DXVK -> {
                holder.radioButton.setVisibility(View.GONE);
                holder.imageView.setImageBitmap(
                        textAsBitmap("DXVK")
                );
            }
            case WINED3D -> {
                holder.radioButton.setVisibility(View.GONE);
                holder.imageView.setImageBitmap(
                        textAsBitmap("WineD3D")
                );
            }
            case VKD3D -> {
                holder.radioButton.setVisibility(View.GONE);
                holder.imageView.setImageBitmap(
                        textAsBitmap("VKD3D")
                );
            }
            case CORE -> {
                selectedItem = preferences.getString(SELECTED_CORE, "");

                if (isRepositoryPackage) {
                    holder.radioButton.setVisibility(View.GONE);
                }
                holder.imageView.setImageResource(R.drawable.ic_rat_package);
            }
        }

        holder.deleteRatPackageButton.setVisibility(item.isExternalPackage ? View.VISIBLE : View.GONE);

        if (item.itemFolderId.equals(selectedItem)) {
            selectedItemId = holder.getAdapterPosition();
        }

        holder.settingsName.setText(item.titleSettings);

        if (item.descriptionSettings.isEmpty()) {
            holder.settingsDescription.setVisibility(View.GONE);
        } else {
            holder.settingsDescription.setText(item.descriptionSettings);
        }

        holder.checkBox.setChecked(selectedItemsId.get(item.type).contains(holder.getAdapterPosition()));
        holder.checkBox.setOnCheckedChangeListener((compoundButton, isChecked) -> {
            if (isChecked) {
                selectedItemsId.get(item.type).add(holder.getAdapterPosition());
            } else {
                selectedItemsId.get(item.type).remove(holder.getAdapterPosition());
            }
        });

        holder.radioButton.setChecked(holder.getAdapterPosition() == selectedItemId);
        holder.radioButton.setOnCheckedChangeListener((compoundButton, isChecked) -> {
            SharedPreferences.Editor editor = preferences.edit();

            switch (item.type) {
                case BOX64 -> editor.putString(SELECTED_BOX64, item.itemFolderId);
                case VK_DRIVER -> editor.putString(SELECTED_VULKAN_DRIVER, item.itemFolderId);
            }

            editor.apply();

            selectedItemId = holder.getAdapterPosition();

            compoundButton.post(() ->
                    notifyItemRangeChanged(0, ratPackagesList.size(), PAYLOAD_UPDATE_CHECKED)
            );
        });

        holder.itemView.setOnClickListener((view) -> {
            if (holder.radioButton.getVisibility() == View.VISIBLE) holder.radioButton.setChecked(!holder.radioButton.isChecked());
            if (holder.checkBox.getVisibility() == View.VISIBLE) holder.checkBox.setChecked(!holder.checkBox.isChecked());
        });

        holder.deleteRatPackageButton.setOnClickListener((v) -> {
            if (holder.getAdapterPosition() == -1) return;

            if (selectedItemId == holder.getAdapterPosition()) {
                selectedItemId = 0;

                SharedPreferences.Editor editor = preferences.edit();

                switch (item.type) {
                    case BOX64 -> editor.putString(SELECTED_BOX64, listRatPackages("Box64").get(0).getFolderName());
                    case VK_DRIVER -> editor.putString(SELECTED_VULKAN_DRIVER, listRatPackages("VulkanDriver").get(0).getFolderName());
                }

                editor.apply();

                notifyItemChanged(0, PAYLOAD_UPDATE_CHECKED);
            }

            deleteRatPackageById(item.itemFolderId);
            ratPackagesList.remove(holder.getAdapterPosition());
            notifyItemRemoved(holder.getAdapterPosition());
        });

        holder.downloadRatPackageButton.setOnClickListener((v) -> {
            holder.downloadRatPackageButton.setVisibility(View.INVISIBLE);

            new Thread(() -> {
                tmpDir.mkdirs();

                boolean status = downloadPackage(item.repoRatName, holder.progressBar, holder.progressText);
                File file = new File(tmpDir, item.repoRatName);

                if (status && file.exists()) {
                    holder.progressBar.post(() -> {
                        holder.progressText.setVisibility(View.VISIBLE);
                        holder.progressBar.setVisibility(View.VISIBLE);
                    });

                    SetupFragment.ProgressCallback callback = new SetupFragment.ProgressCallback() {
                        @Override
                        public void onProgressChanged(int progress) {
                            holder.progressText.post(() -> holder.progressText.setText(context.getText(R.string.installing) + " - " + progress + "%"));
                            holder.progressBar.post(() -> holder.progressBar.setProgress(progress));
                        }

                        @Override
                        public void setProgressBarIndeterminate(boolean indeterminate) {
                            holder.progressBar.post(() -> holder.progressBar.setIndeterminate(indeterminate));
                        }

                        @Override
                        public void setDialogText(String text) {
                        }
                    };

                    installRat(new RatPackageManager.RatPackage(file.getPath()), callback);

                    file.delete();

                    holder.progressBar.post(() -> {
                        holder.downloadRatPackageButton.setVisibility(View.VISIBLE);
                        holder.downloadRatPackageButton.setEnabled(false);
                        holder.downloadRatPackageButton.setImageResource(android.R.drawable.checkbox_on_background);

                        holder.progressText.setVisibility(View.GONE);
                        holder.progressBar.setVisibility(View.GONE);
                    });
                }
            }).start();
        });
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position, @NonNull List<Object> payloads) {
        Item item = ratPackagesList.get(position);

        if (!payloads.isEmpty() && payloads.contains(PAYLOAD_UPDATE_CHECKED)) {
            holder.checkBox.setChecked(selectedItemsId.get(item.type).contains(holder.getAdapterPosition()));
            holder.radioButton.setChecked(holder.getAdapterPosition() == selectedItemId);
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
        public String itemFolderId;
        public int type;
        public boolean isExternalPackage;
        public String repoRatName;
        public boolean isInstalled;

        public Item(String titleSettings, String descriptionSettings, String itemFolderId, int type, boolean isExternalPackage, String repoRatName, boolean isInstalled) {
            this.titleSettings = titleSettings;
            this.descriptionSettings = descriptionSettings;
            this.itemFolderId = itemFolderId;
            this.type = type;
            this.isExternalPackage = isExternalPackage;
            this.repoRatName = repoRatName;
            this.isInstalled = isInstalled;
        }
    }

    private Bitmap textAsBitmap(String text) {
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

        paint.setTextSize((float) 80.0);
        paint.setColor(Color.WHITE);
        paint.setTextAlign(Paint.Align.LEFT);
        paint.setTypeface(context.getResources().getFont(R.font.quicksand));

        float baseline = -paint.ascent();
        int width = Math.round(paint.measureText(text) + 0.5F);
        int height = Math.round(baseline + paint.descent() + 0.5F);

        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(bitmap);

        canvas.drawText(text, 0F, baseline, paint);

        return bitmap;
    }

    public static final int VK_DRIVER = 0;
    public static final int BOX64 = 1;
    public static final int WINE = 2;
    public static final int DXVK = 3;
    public static final int WINED3D = 4;
    public static final int VKD3D = 5;
    public static final int CORE = 6;
    public static final int PAYLOAD_UPDATE_CHECKED = 7;
}