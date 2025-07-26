package com.micewine.emu.adapters;

import static com.micewine.emu.activities.GeneralSettingsActivity.SELECTED_BOX64;
import static com.micewine.emu.activities.GeneralSettingsActivity.SELECTED_VULKAN_DRIVER;
import static com.micewine.emu.activities.MainActivity.preferences;
import static com.micewine.emu.activities.MainActivity.tmpDir;
import static com.micewine.emu.core.RatPackageManager.deleteRatPackageById;
import static com.micewine.emu.core.RatPackageManager.installRat;
import static com.micewine.emu.core.RatPackageManager.listRatPackages;
import static com.micewine.emu.fragments.RatDownloaderFragment.downloadPackage;
import static com.micewine.emu.utils.FileUtils.deleteDirectoryRecursively;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.micewine.emu.R;
import com.micewine.emu.core.RatPackageManager;

import java.io.File;
import java.util.ArrayList;

public class AdapterRatPackage extends RecyclerView.Adapter<AdapterRatPackage.ViewHolder> {
    private final ArrayList<Item> ratPackagesList;
    private final Context context;
    private final boolean isRepositoryPackage;
    private int selectedItemId = -1;

    public AdapterRatPackage(ArrayList<Item> ratPackagesList, Context context, boolean isRepositoryPackage) {
        this.ratPackagesList = ratPackagesList;
        this.context = context;
        this.isRepositoryPackage = isRepositoryPackage;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_rat_package_item, parent, false);
        return new ViewHolder(itemView);
    }

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
        } else {
            holder.downloadRatPackageButton.setVisibility(View.GONE);
        }

        holder.progressBar.setVisibility(View.GONE);

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
                        textAsBitmap("DXVK", 80F, Color.WHITE)
                );
            }
            case WINED3D -> {
                holder.radioButton.setVisibility(View.GONE);
                holder.imageView.setImageBitmap(
                        textAsBitmap("WineD3D", 80F, Color.WHITE)
                );
            }
            case VKD3D -> {
                holder.radioButton.setVisibility(View.GONE);
                holder.imageView.setImageBitmap(
                        textAsBitmap("VKD3D", 80F, Color.WHITE)
                );
            }
        }

        holder.deleteRatPackageButton.setVisibility(item.isExternalPackage ? View.VISIBLE : View.GONE);

        if (item.itemFolderId.equals(selectedItem)) {
            selectedItemId = holder.getAdapterPosition();
        }

        holder.settingsName.setText(item.titleSettings);
        holder.settingsDescription.setText(item.descriptionSettings);

        holder.radioButton.setChecked(position == selectedItemId);
        holder.radioButton.setOnClickListener((v) -> {
            SharedPreferences.Editor editor = preferences.edit();

            switch (item.type) {
                case BOX64 -> editor.putString(SELECTED_BOX64, item.itemFolderId);
                case VK_DRIVER -> editor.putString(SELECTED_VULKAN_DRIVER, item.itemFolderId);
            }

            editor.apply();

            selectedItemId = holder.getAdapterPosition();
            notifyItemRangeChanged(0, ratPackagesList.size());
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

                notifyItemChanged(0);
            }

            deleteRatPackageById(item.itemFolderId);

            ratPackagesList.remove(holder.getAdapterPosition());
            notifyItemRemoved(holder.getAdapterPosition());
        });

        holder.downloadRatPackageButton.setOnClickListener((v) -> {
            deleteDirectoryRecursively(tmpDir.toPath());
            tmpDir.mkdirs();

            new Thread(() -> {
                boolean status = downloadPackage(item.repoRatName, holder.progressBar);
                File file = new File(tmpDir, item.repoRatName);

                if (status && file.exists()) {
                    holder.progressBar.post(() -> {
                        holder.progressBar.setVisibility(View.VISIBLE);
                        holder.progressBar.setIndeterminate(true);
                    });

                    installRat(new RatPackageManager.RatPackage(file.getPath()), context);

                    holder.progressBar.post(() -> {
                        holder.downloadRatPackageButton.setEnabled(false);
                        holder.downloadRatPackageButton.setImageResource(android.R.drawable.checkbox_on_background);

                        holder.progressBar.setVisibility(View.GONE);
                        holder.progressBar.setIndeterminate(false);
                    });
                }

                deleteDirectoryRecursively(tmpDir.toPath());
                tmpDir.mkdirs();
            }).start();
        });
    }

    @Override
    public int getItemCount() {
        return ratPackagesList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        RadioButton radioButton = itemView.findViewById(R.id.radio_button);
        TextView settingsName = itemView.findViewById(R.id.preset_title);
        TextView settingsDescription = itemView.findViewById(R.id.rat_package_desc);
        ImageView imageView = itemView.findViewById(R.id.image_view);
        ImageButton deleteRatPackageButton = itemView.findViewById(R.id.rat_package_delete);
        ImageView downloadRatPackageButton = itemView.findViewById(R.id.rat_package_download);
        ProgressBar progressBar = itemView.findViewById(R.id.progressBar);

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

    private Bitmap textAsBitmap(String text, float size, int color) {
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

        paint.setTextSize(size);
        paint.setColor(color);
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

    public static final int VK_DRIVER = 1;
    public static final int BOX64 = 2;
    public static final int WINE = 3;
    public static final int DXVK = 4;
    public static final int WINED3D = 5;
    public static final int VKD3D = 6;
}