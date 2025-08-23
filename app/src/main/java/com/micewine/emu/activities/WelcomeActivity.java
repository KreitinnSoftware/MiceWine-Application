package com.micewine.emu.activities;

import static android.provider.Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION;

import static com.micewine.emu.adapters.AdapterRatPackage.selectedItemId;
import static com.micewine.emu.fragments.RootFSDownloaderFragment.downloadingRootFS;
import static com.micewine.emu.fragments.RootFSDownloaderFragment.rootFSIsDownloaded;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
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
import com.micewine.emu.databinding.ActivityWelcomeBinding;
import com.micewine.emu.fragments.RootFSDownloaderFragment;

public class WelcomeActivity extends AppCompatActivity {
    private ViewPager2 viewPager;
    private FloatingActionButton button;

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

                if (rootFSIsDownloaded || selectCustomRootFS) {
                    finishedWelcomeScreen = true;
                    finish();
                    return;
                }

                button.setVisibility(View.GONE);

                downloader.imageView.setVisibility(View.VISIBLE);
                downloader.textView.setText(R.string.downloading_rootfs);
                downloadingRootFS = true;

                new Thread(() -> {
                    int status = downloader.downloadRootFS(downloader.rootFsList.get(selectedItemId).itemFolderId);
                    if (status != 0) {
                        runOnUiThread(() -> {
                            Toast.makeText(this, "Error on Downloading RootFS, Returned Error " + status, Toast.LENGTH_SHORT).show();
                            button.setVisibility(View.VISIBLE);
                        });
                        return;
                    }

                    rootFSIsDownloaded = true;
                    downloadingRootFS = false;

                    runOnUiThread(() -> {
                        button.setVisibility(View.VISIBLE);
                        downloader.progressBarProgress.setText("100%");
                        downloader.textView.setText(R.string.download_successful);
                    });
                }).start();
                return;
            }

            viewPager.setCurrentItem(viewPager.getCurrentItem() + 1);
        });
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

            return true;
        }

        finish();

        return super.onKeyDown(keyCode, event);
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