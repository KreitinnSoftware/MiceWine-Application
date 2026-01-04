package com.micewine.emu.activities;

import static android.provider.Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION;

import static com.micewine.emu.adapters.AdapterRatPackage.selectedItemId;
import static com.micewine.emu.core.RootFSDownloaderService.DOWNLOAD_DONE;
import static com.micewine.emu.core.RootFSDownloaderService.DOWNLOAD_FAILED;
import static com.micewine.emu.core.RootFSDownloaderService.DOWNLOAD_START;
import static com.micewine.emu.fragments.RootFSDownloaderFragment.downloadingRootFS;
import static com.micewine.emu.fragments.RootFSDownloaderFragment.rootFSIsDownloaded;

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
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.micewine.emu.R;
import com.micewine.emu.adapters.AdapterWelcomeFragments;
import com.micewine.emu.core.RootFSDownloaderService;
import com.micewine.emu.databinding.ActivityWelcomeBinding;
import com.micewine.emu.fragments.RootFSDownloaderFragment;

public class WelcomeActivity extends AppCompatActivity {
    private ViewPager2 viewPager;
    private FloatingActionButton button;
    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (DOWNLOAD_DONE.equals(intent.getAction())) {
                runOnUiThread(() -> button.setVisibility(View.VISIBLE));
            } else if (DOWNLOAD_FAILED.equals(intent.getAction())) {
                runOnUiThread(() -> button.setVisibility(View.VISIBLE));
            } else if (DOWNLOAD_START.equals(intent.getAction())) {
                downloadingRootFS = true;
                runOnUiThread(() -> button.setVisibility(View.GONE));
            }
        }
    };

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivityWelcomeBinding binding = ActivityWelcomeBinding.inflate(getLayoutInflater());

        setContentView(binding.getRoot());

        viewPager = findViewById(R.id.viewPager);
        viewPager.setUserInputEnabled(false);
        viewPager.setAdapter(new AdapterWelcomeFragments(this));

        button = findViewById(R.id.continueButton);
        button.setOnClickListener((v) -> {
            if (viewPager.getCurrentItem() == 0) {
                if (isStoragePermissionDenied()) {
                    requestPermission();
                }
            } else if (viewPager.getCurrentItem() == 2) {
                RootFSDownloaderFragment downloader = (RootFSDownloaderFragment) getCurrentFragment();
                boolean selectCustomRootFS = (selectedItemId == downloader.rootFsList.size() - 1);

                if (rootFSIsDownloaded || selectCustomRootFS || selectedItemId == -1) {
                    finishedWelcomeScreen = true;
                    finish();
                    return;
                }

                Intent serviceIntent = new Intent(this, RootFSDownloaderService.class);
                serviceIntent.putExtra("commit", downloader.rootFsList.get(selectedItemId).itemFolderId);

                startForegroundService(serviceIntent);

                return;
            }

            viewPager.setCurrentItem(viewPager.getCurrentItem() + 1);
        });

        registerReceiver(broadcastReceiver, new IntentFilter() {{
            addAction(DOWNLOAD_DONE);
            addAction(DOWNLOAD_START);
            addAction(DOWNLOAD_FAILED);
        }});
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(broadcastReceiver);
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
        if (keyCode == KeyEvent.KEYCODE_BACK && !downloadingRootFS) {
            viewPager.setCurrentItem(viewPager.getCurrentItem() - 1);
            button.setEnabled(true);
        }

        return true;
    }

    private Fragment getCurrentFragment() {
        int currentItem = viewPager.getCurrentItem();
        return getSupportFragmentManager().findFragmentByTag("f" + currentItem);
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