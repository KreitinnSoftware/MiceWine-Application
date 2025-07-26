package com.micewine.emu.activities;

import static com.micewine.emu.activities.MainActivity.appRootDir;
import static com.micewine.emu.activities.MainActivity.enableMangoHUD;
import static com.micewine.emu.activities.MainActivity.fpsLimit;
import static com.micewine.emu.activities.MainActivity.gson;
import static com.micewine.emu.activities.MainActivity.setSharedVars;
import static com.micewine.emu.activities.MainActivity.usrDir;

import android.os.Bundle;
import android.view.KeyEvent;
import android.widget.ImageButton;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.gson.JsonObject;
import com.micewine.emu.R;
import com.micewine.emu.adapters.AdapterTabPagerRatManager;
import com.micewine.emu.databinding.ActivityRatManagerBinding;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class RatManagerActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivityRatManagerBinding binding = ActivityRatManagerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ImageButton backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener((v) -> onKeyDown(KeyEvent.KEYCODE_BACK, null));

        Toolbar ratManagerToolBar = findViewById(R.id.ratManagerToolbar);
        ratManagerToolBar.setTitle(R.string.rat_manager_title);

        TabLayout tabLayout = findViewById(R.id.tabLayout);
        ViewPager2 viewPager = findViewById(R.id.viewPager);

        viewPager.setAdapter(new AdapterTabPagerRatManager(this));

        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            AdapterTabPagerRatManager adapter = (AdapterTabPagerRatManager) viewPager.getAdapter();
            if (adapter != null) {
                tab.setText(adapter.getItemName(position));
            }
        }).attach();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            finish();
        }

        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        setSharedVars(this);
    }

    public static void generateICDFile(String driverLib) {
        File icdFile = new File(appRootDir, "vulkan_icd.json");

        JsonObject icd = new JsonObject();
        icd.addProperty("api_version", "1.1.296");
        icd.addProperty("library_path", driverLib);

        JsonObject root = new JsonObject();
        root.add("ICD", icd);
        root.addProperty("file_format_version", "1.0.0");

        String json = gson.toJson(root);

        try (FileWriter writer = new FileWriter(icdFile)) {
            writer.write(json);
        } catch (IOException ignored) {
        }
    }

    public static void generateMangoHUDConfFile() {
        File mangoHudConfFile = new File(usrDir, "etc/MangoHud.conf");
        StringBuilder options = new StringBuilder();

        options.append("fps_limit=").append(fpsLimit).append("\n");

        if (!enableMangoHUD) {
            options.append("no_display\n");
        }

        try (FileWriter writer = new FileWriter(mangoHudConfFile)) {
            writer.write(options.toString());
        } catch (IOException ignored) {
        }
    }
}