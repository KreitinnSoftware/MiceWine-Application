package com.micewine.emu.activities;

import static android.provider.Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION;

import static com.micewine.emu.activities.MainActivity.ACTION_SETUP;
import static com.micewine.emu.activities.MainActivity.appBuiltinRootfs;

import android.Manifest;
import android.content.Intent;
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

public class WelcomeActivity extends AppCompatActivity {
    private ViewPager2 viewPager;
    private FloatingActionButton button;

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
            } else if (viewPager.getCurrentItem() == 1) {
                if (appBuiltinRootfs) {
                    sendBroadcast(new Intent(ACTION_SETUP));
                }

                finishedWelcomeScreen = true;
                finish();
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
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            viewPager.setCurrentItem(viewPager.getCurrentItem() - 1);
            button.setEnabled(true);

            return true;
        }

        finish();

        return super.onKeyDown(keyCode, event);
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