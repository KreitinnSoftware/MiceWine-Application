package com.micewine.emu.activities;

import static com.micewine.emu.activities.MainActivity.setSharedVars;

import android.os.Bundle;
import android.view.KeyEvent;
import android.widget.ImageButton;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.micewine.emu.R;
import com.micewine.emu.adapters.AdapterTabPagerRatDownloader;
import com.micewine.emu.databinding.ActivityRatDownloaderBinding;

public class RatDownloaderActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivityRatDownloaderBinding binding = ActivityRatDownloaderBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ImageButton backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener((v) -> onKeyDown(KeyEvent.KEYCODE_BACK, null));

        Toolbar ratManagerToolBar = findViewById(R.id.ratManagerToolbar);
        ratManagerToolBar.setTitle(R.string.rat_downloader_title);

        TabLayout tabLayout = findViewById(R.id.tabLayout);
        ViewPager2 viewPager = findViewById(R.id.viewPager);

        viewPager.setAdapter(new AdapterTabPagerRatDownloader(this));

        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            AdapterTabPagerRatDownloader adapter = (AdapterTabPagerRatDownloader) viewPager.getAdapter();
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
}