package com.micewine.emu.adapters;

import static com.micewine.emu.activities.GeneralSettingsActivity.SELECTED_BOX64;
import static com.micewine.emu.activities.GeneralSettingsActivity.SELECTED_VULKAN_DRIVER;
import static com.micewine.emu.activities.MainActivity.ACTION_RUN_WINE;
import static com.micewine.emu.activities.MainActivity.preferences;
import static com.micewine.emu.core.RatPackageManager.listRatPackagesId;
import static com.micewine.emu.fragments.DebugSettingsFragment.availableCPUs;
import static com.micewine.emu.fragments.ShortcutsFragment.MESA_DRIVER;
import static com.micewine.emu.fragments.ShortcutsFragment.getBox64Preset;
import static com.micewine.emu.fragments.ShortcutsFragment.getBox64Version;
import static com.micewine.emu.fragments.ShortcutsFragment.getCpuAffinity;
import static com.micewine.emu.fragments.ShortcutsFragment.getD3DXRenderer;
import static com.micewine.emu.fragments.ShortcutsFragment.getDXVKVersion;
import static com.micewine.emu.fragments.ShortcutsFragment.getDisplaySettings;
import static com.micewine.emu.fragments.ShortcutsFragment.getEnableDInput;
import static com.micewine.emu.fragments.ShortcutsFragment.getEnableXInput;
import static com.micewine.emu.fragments.ShortcutsFragment.getSelectedVirtualControllerPreset;
import static com.micewine.emu.fragments.ShortcutsFragment.getVKD3DVersion;
import static com.micewine.emu.fragments.ShortcutsFragment.getVulkanDriver;
import static com.micewine.emu.fragments.ShortcutsFragment.getVulkanDriverType;
import static com.micewine.emu.fragments.ShortcutsFragment.getWineD3DVersion;
import static com.micewine.emu.fragments.ShortcutsFragment.getWineESync;
import static com.micewine.emu.fragments.ShortcutsFragment.getWineServices;
import static com.micewine.emu.fragments.ShortcutsFragment.getWineVirtualDesktop;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.micewine.emu.R;
import com.micewine.emu.activities.EmulationActivity;
import com.micewine.emu.fragments.EditGamePreferencesFragment;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

public class AdapterGame extends RecyclerView.Adapter<AdapterGame.ViewHolder> {
    private final FragmentActivity activity;
    private final float size;
    private final ArrayList<GameItem> gameList;
    private final ArrayList<GameItem> filteredList = new ArrayList<>();

    public AdapterGame(ArrayList<GameItem> gameList, float size, FragmentActivity activity) {
        this.activity = activity;
        this.size = size;
        this.gameList = gameList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_game_item, parent, false);
        itemView.getLayoutParams().width = Math.round(itemView.getLayoutParams().width * size);
        itemView.getLayoutParams().height = Math.round(itemView.getLayoutParams().height * size);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        GameItem item = filteredList.get(position);

        holder.titleGame.setText(item.name);

        File imgFile = new File(item.iconPath);
        if (imgFile.exists() && imgFile.length() > 0) {
            Bitmap imgBitmap = BitmapFactory.decodeFile(item.iconPath);
            if (imgBitmap != null) {
                Bitmap newBitmap;
                if (new File(item.exePath).exists()) {
                    newBitmap = Bitmap.createScaledBitmap(imgBitmap, holder.itemView.getLayoutParams().width - 10, holder.itemView.getLayoutParams().width - 10, false);
                } else {
                    newBitmap = toGrayScale(
                            Bitmap.createScaledBitmap(imgBitmap, holder.itemView.getLayoutParams().width - 10, holder.itemView.getLayoutParams().width - 10, false)
                    );
                }
                holder.gameImage.setImageBitmap(newBitmap);
            }
        } else if (item.iconPath.isEmpty()) {
            holder.gameImage.setImageBitmap(
                    BitmapFactory.decodeResource(activity.getResources(), R.drawable.default_icon)
            );
        } else {
            holder.gameImage.setImageResource(R.drawable.unknown_exe);
        }
    }

    @Override
    public int getItemCount() {
        return filteredList.size();
    }

    private Bitmap toGrayScale(Bitmap bitmap) {
        Bitmap grayScaleBitmap = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);

        Paint paint = new Paint();
        ColorMatrix colorMatrix = new ColorMatrix();
        colorMatrix.setSaturation(0F);

        ColorMatrixColorFilter filter = new ColorMatrixColorFilter(colorMatrix);
        paint.setColorFilter(filter);

        Canvas canvas = new Canvas(grayScaleBitmap);
        canvas.drawBitmap(bitmap, 0F, 0F, paint);

        return grayScaleBitmap;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void filterList(String name) {
        filteredList.clear();

        for (GameItem item : gameList) {
            if (item.name != null && item.name.toLowerCase().contains(name.toLowerCase())) {
                filteredList.add(item);
            }
        }

        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        public TextView titleGame = itemView.findViewById(R.id.title_game_model);
        public ImageView gameImage = itemView.findViewById(R.id.img_game);

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
        }

        @Override
        public void onClick(View view) {
            GameItem gameItem = gameList.get(getAdapterPosition());

            selectedGameName = gameItem.name;

            File exeFile =  new File(gameItem.exePath);

            String exePath = gameItem.exePath;
            String exeArguments = gameItem.exeArguments;

            if (!exeFile.exists()) {
                if (gameItem.exePath.equals(activity.getString(R.string.desktop_mode_init))) {
                    exePath = "";
                    exeArguments = "";
                } else {
                    activity.runOnUiThread(() -> Toast.makeText(activity, R.string.executable_file_not_found, Toast.LENGTH_SHORT).show());
                    new EditGamePreferencesFragment(EditGamePreferencesFragment.EDIT_GAME_PREFERENCES, null).show(activity.getSupportFragmentManager(), "");
                    return;
                }
            }

            String driverName = getVulkanDriver(selectedGameName);
            if (driverName.equals("Global")) {
                driverName = (preferences != null ? preferences.getString(SELECTED_VULKAN_DRIVER, "") : "");
            }

            String box64Version = getBox64Version(selectedGameName);
            if (box64Version.equals("Global")) {
                box64Version = (preferences != null ? preferences.getString(SELECTED_BOX64, "") : "");
            }

            int driverType = getVulkanDriverType(selectedGameName);

            Intent runActivityIntent = new Intent(activity, EmulationActivity.class);
            Intent runWineIntent = new Intent(ACTION_RUN_WINE);

            runWineIntent.putExtra("exePath", exePath);
            runWineIntent.putExtra("exeArguments", exeArguments);
            runWineIntent.putExtra("driverName", driverName);
            runWineIntent.putExtra("driverType", driverType);
            runWineIntent.putExtra("box64Version", box64Version);
            runWineIntent.putExtra("box64Preset", getBox64Preset(selectedGameName));
            runWineIntent.putExtra("displayResolution", getDisplaySettings(selectedGameName).get(1));
            runWineIntent.putExtra("virtualControllerPreset", getSelectedVirtualControllerPreset(selectedGameName));
            runWineIntent.putExtra("d3dxRenderer", getD3DXRenderer(selectedGameName));
            runWineIntent.putExtra("wineD3D", getWineD3DVersion(selectedGameName));
            runWineIntent.putExtra("dxvk", getDXVKVersion(selectedGameName));
            runWineIntent.putExtra("vkd3d", getVKD3DVersion(selectedGameName));
            runWineIntent.putExtra("esync", getWineESync(selectedGameName));
            runWineIntent.putExtra("services", getWineServices(selectedGameName));
            runWineIntent.putExtra("virtualDesktop", getWineVirtualDesktop(selectedGameName));
            runWineIntent.putExtra("enableXInput", getEnableXInput(selectedGameName));
            runWineIntent.putExtra("enableDInput", getEnableDInput(selectedGameName));
            runWineIntent.putExtra("cpuAffinity", getCpuAffinity(selectedGameName));

            activity.sendBroadcast(runWineIntent);
            activity.startActivity(runActivityIntent);
        }

        @Override
        public boolean onLongClick(View view) {
            selectedGameName = gameList.get(getAdapterPosition()).name;
            return false;
        }
    }

    public static String selectedGameName = "";

    public static class GameItem {
        public String name;
        public String exePath;
        public String exeArguments;
        public String iconPath;
        public String box64Version;
        public String box64Preset;
        public ArrayList<String> controllersPreset;
        public ArrayList<Boolean> controllersEnableXInput;
        public ArrayList<Boolean> controllersXInputSwapAnalogs;
        public String virtualControllerPreset;
        public boolean virtualControllerEnableXInput;
        public String displayMode;
        public String displayResolution;
        public String vulkanDriver;
        public int vulkanDriverType;
        public String d3dxRenderer;
        public String dxvkVersion;
        public String wineD3DVersion;
        public String vkd3dVersion;
        public boolean wineESync;
        public boolean wineServices;
        public String cpuAffinityCores;
        public boolean wineVirtualDesktop;
        public boolean enableXInput;
        public boolean enableDInput;

        public GameItem(String name, String exePath, String exeArguments, String iconPath) {
            this.name = name;
            this.exePath = exePath;
            this.exeArguments = exeArguments;
            this.iconPath = iconPath;
            this.box64Version = "Global";
            this.box64Preset = "default";
            this.controllersPreset = new ArrayList<>(Arrays.asList("default", "default", "default", "default"));
            this.controllersEnableXInput = new ArrayList<>(Arrays.asList(true, true, true, true));
            this.controllersXInputSwapAnalogs = new ArrayList<>(Arrays.asList(false, false, false, false));
            this.virtualControllerPreset = "default";
            this.virtualControllerEnableXInput = true;
            this.displayMode = "16:9";
            this.displayResolution = "1280x720";
            this.vulkanDriver = "Global";
            this.vulkanDriverType = MESA_DRIVER;
            this.d3dxRenderer = "DXVK";
            this.dxvkVersion = listRatPackagesId("DXVK").get(0);
            this.wineD3DVersion = listRatPackagesId("WineD3D").get(0);
            this.vkd3dVersion = listRatPackagesId("VKD3D").get(0);
            this.wineESync = true;
            this.wineServices = false;
            this.cpuAffinityCores = String.join(",", availableCPUs);
            this.wineVirtualDesktop = false;
            this.enableXInput = true;
            this.enableDInput = true;
        }
    }
}