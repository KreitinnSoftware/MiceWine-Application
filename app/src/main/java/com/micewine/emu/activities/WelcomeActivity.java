package com.micewine.emu.activities;

import static android.provider.Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION;

import static com.micewine.emu.activities.MainActivity.deviceArch;
import static com.micewine.emu.adapters.AdapterRatPackage.BOX64;
import static com.micewine.emu.adapters.AdapterRatPackage.CORE;
import static com.micewine.emu.adapters.AdapterRatPackage.DXVK;
import static com.micewine.emu.adapters.AdapterRatPackage.VKD3D;
import static com.micewine.emu.adapters.AdapterRatPackage.VK_DRIVER;
import static com.micewine.emu.adapters.AdapterRatPackage.WINE;
import static com.micewine.emu.adapters.AdapterRatPackage.WINED3D;
import static com.micewine.emu.adapters.AdapterRatPackage.selectedItemsId;
import static com.micewine.emu.core.RatPackageManager.getRatCategoryString;
import static com.micewine.emu.fragments.CoreComponentsDownloaderFragment.ACTION_END_DOWNLOAD;
import static com.micewine.emu.fragments.CoreComponentsDownloaderFragment.ACTION_START_DOWNLOAD;
import static com.micewine.emu.fragments.RatDownloaderFragment.fetchPackages;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.KeyEvent;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.micewine.emu.R;
import com.micewine.emu.adapters.AdapterWelcomeFragments;
import com.micewine.emu.databinding.ActivityWelcomeBinding;
import com.micewine.emu.fragments.RatDownloaderFragment.RepoRatPackage;

import java.util.ArrayList;
import java.util.List;

public class WelcomeActivity extends AppCompatActivity {
    private ViewPager2 viewPager;
    private FloatingActionButton button;
    public static List<RepoRatPackage> selectedRatPackages = new ArrayList<>();
    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (ACTION_END_DOWNLOAD.equals(intent.getAction())) {
                button.setEnabled(true);
            }
        }
    };


    @SuppressLint({"SetTextI18n", "UnspecifiedRegisterReceiverFlag"})
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivityWelcomeBinding binding = ActivityWelcomeBinding.inflate(getLayoutInflater());

        setContentView(binding.getRoot());

        viewPager = findViewById(R.id.viewPager);
        viewPager.setUserInputEnabled(false);
        viewPager.setAdapter(new AdapterWelcomeFragments(this));

        List<RepoRatPackage> packagesList = new ArrayList<>();

        new Thread(() -> {
            packagesList.addAll(fetchPackages());

            selectedItemsId.get(CORE).add(getRepoRatIdByInfo(CORE, "", packagesList));

            if (!deviceArch.equals("x86_64")) {
                selectedItemsId.get(BOX64).add(getRepoRatIdByInfo(BOX64, "0.4.0", packagesList));
            }
            selectedItemsId.get(WINE).add(0);

            selectedItemsId.get(VK_DRIVER).add(getRepoRatIdByInfo(VK_DRIVER, "Wrapper", "25.1.4", packagesList));

            if (!deviceArch.equals("x86_64")) {
                selectedItemsId.get(VK_DRIVER).add(getRepoRatIdByInfo(VK_DRIVER, "Turnip", "25.1.4", packagesList));
                selectedItemsId.get(VK_DRIVER).add(getRepoRatIdByInfo(VK_DRIVER, "Wrapper", "adrenotools", packagesList));
            }

            selectedItemsId.get(DXVK).add(getRepoRatIdByInfo(DXVK, "2.4.1-1-gplasync", packagesList));
            selectedItemsId.get(DXVK).add(getRepoRatIdByInfo(DXVK, "1.10.3-async", packagesList));
            selectedItemsId.get(DXVK).add(getRepoRatIdByInfo(DXVK, "1.9.4", packagesList));

            selectedItemsId.get(WINED3D).add(getRepoRatIdByInfo(WINED3D, "10.0", packagesList));
            selectedItemsId.get(WINED3D).add(getRepoRatIdByInfo(WINED3D, "11.0", packagesList));

            selectedItemsId.get(VKD3D).add(getRepoRatIdByInfo(VKD3D, "3.0a", packagesList));
            selectedItemsId.get(VKD3D).add(getRepoRatIdByInfo(VKD3D, "2.8", packagesList));
        }).start();

        button = findViewById(R.id.continueButton);
        button.setOnClickListener((v) -> {
            if (viewPager.getCurrentItem() == 0) {
                if (isStoragePermissionDenied()) {
                    requestPermission();
                }
            } else if (viewPager.getCurrentItem() == 2) {
                selectedRatPackages.clear();

                for (int i = 0; i < 7; i++) {
                    for (int a : selectedItemsId.get(i)) {
                        selectedRatPackages.add(getRepoRat(i, a, packagesList));
                    }
                }

                boolean hasCore = haveSelectedAnyPackageByCategory(selectedRatPackages, CORE);
                boolean hasWine = haveSelectedAnyPackageByCategory(selectedRatPackages, WINE);
                boolean hasVulkanDriver = haveSelectedAnyPackageByCategory(selectedRatPackages, VK_DRIVER);
                boolean hasDXVK = haveSelectedAnyPackageByCategory(selectedRatPackages, DXVK);
                boolean hasVKD3D = haveSelectedAnyPackageByCategory(selectedRatPackages, VKD3D);
                boolean hasWineD3D = haveSelectedAnyPackageByCategory(selectedRatPackages, WINED3D);
                boolean hasBox64 = haveSelectedAnyPackageByCategory(selectedRatPackages, BOX64);
                boolean canProceed = (hasCore && hasWine && hasVulkanDriver && hasDXVK && hasVKD3D && hasWineD3D && (deviceArch.equals("x86_64") || hasBox64));

                if (!canProceed) {
                    Toast.makeText(this, "Select at least one package of each type", Toast.LENGTH_SHORT).show();
                    return;
                }

                button.setEnabled(false);

                sendBroadcast(new Intent(ACTION_START_DOWNLOAD));
            } else if (viewPager.getCurrentItem() == 3) {
                finishedWelcomeScreen = true;
                finish();
            }

            viewPager.setCurrentItem(viewPager.getCurrentItem() + 1);
        });

        registerReceiver(broadcastReceiver, new IntentFilter(ACTION_END_DOWNLOAD));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(broadcastReceiver);
    }

    private boolean haveSelectedAnyPackageByCategory(List<RepoRatPackage> ratPackages, int category) {
        return ratPackages.stream().anyMatch((ratPackage) -> ratPackage.ratPackage.category.equals(getRatCategoryString(category)));
    }
    private RepoRatPackage getRepoRat(int category, int index, List<RepoRatPackage> packageList) {
        List<RepoRatPackage> filteredList = new ArrayList<>();

        for (RepoRatPackage repoRatPackage : packageList) {
            if (repoRatPackage.ratPackage.category.equals(getRatCategoryString(category))) {
                filteredList.add(repoRatPackage);
            }
        }

        return filteredList.get(index);
    }

    private int getRepoRatIdByInfo(int category, String version, List<RepoRatPackage> packageList) {
        return getRepoRatIdByInfo(category, "", version, packageList);
    }

    private int getRepoRatIdByInfo(int category, String name, String version, List<RepoRatPackage> packageList) {
        List<RepoRatPackage> filteredList = new ArrayList<>();

        for (RepoRatPackage repoRatPackage : packageList) {
            if (category == VK_DRIVER && repoRatPackage.ratPackage.category.equals("AdrenoTools")) {
                filteredList.add(repoRatPackage);
            }
            if (repoRatPackage.ratPackage.category.equals(getRatCategoryString(category))) {
                filteredList.add(repoRatPackage);
            }
        }

        for (int i = 0; i < filteredList.size(); i++) {
            if (filteredList.get(i).ratPackage.version.contains(version) && filteredList.get(i).ratPackage.name.contains(name)) {
                return i;
            }
        }

        return -1;
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (viewPager.getCurrentItem() == 1 && isStoragePermissionDenied()) {
            button.setEnabled(false);

            requestPermission();

            Toast.makeText(this, getString(R.string.grant_files_permission_error), Toast.LENGTH_SHORT).show();
        } else {
            button.setEnabled(true);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (viewPager.getCurrentItem() == 3) return true;
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            viewPager.setCurrentItem(viewPager.getCurrentItem() - 1);
            button.setEnabled(true);
        }

        return true;
    }

    private void requestPermission() {
        if (isStoragePermissionDenied()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                Intent intent = new Intent(ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                Uri uri = Uri.fromParts("package", getPackageName(), null);
                intent.setData(uri);
                startActivity(intent);
            } else {
                String[] permissions = { Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE };

                ActivityCompat.requestPermissions(this, permissions, 1000);
            }
        }
    }

    private boolean isStoragePermissionDenied() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            return !Environment.isExternalStorageManager();
        } else {
            int readPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
            int writePermission = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);

            return (readPermission != PackageManager.PERMISSION_GRANTED) || (writePermission != PackageManager.PERMISSION_GRANTED);
        }
    }

    public static boolean finishedWelcomeScreen = false;
}