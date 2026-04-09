package com.micewine.emu.adapters;

import static com.micewine.emu.activities.MainActivity.deviceArch;
import static com.micewine.emu.adapters.AdapterRatPackage.DXVK;
import static com.micewine.emu.adapters.AdapterRatPackage.VKD3D;
import static com.micewine.emu.adapters.AdapterRatPackage.VK_DRIVER;
import static com.micewine.emu.adapters.AdapterRatPackage.BOX64;
import static com.micewine.emu.adapters.AdapterRatPackage.WINE;
import static com.micewine.emu.adapters.AdapterRatPackage.WINED3D;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.micewine.emu.fragments.RatDownloaderFragment;

public class AdapterTabPagerRatDownloader extends FragmentStateAdapter {
    public AdapterTabPagerRatDownloader(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        if (deviceArch.equals("x86_64")) {
            return switch (position) {
                case 0 -> new RatDownloaderFragment(VK_DRIVER);
                case 1 -> new RatDownloaderFragment(WINE);
                case 2 -> new RatDownloaderFragment(DXVK);
                case 3 -> new RatDownloaderFragment(WINED3D);
                case 4 -> new RatDownloaderFragment(VKD3D);
                default -> throw new IllegalArgumentException("Invalid Fragment for Position " + position);
            };
        } else {
            return switch (position) {
                case 0 -> new RatDownloaderFragment(VK_DRIVER, "AdrenoTools");
                case 1 -> new RatDownloaderFragment(BOX64);
                case 2 -> new RatDownloaderFragment(WINE);
                case 3 -> new RatDownloaderFragment(DXVK);
                case 4 -> new RatDownloaderFragment(WINED3D);
                case 5 -> new RatDownloaderFragment(VKD3D);
                default -> throw new IllegalArgumentException("Invalid Fragment for Position " + position);
            };
        }
    }

    @Override
    public int getItemCount() {
        return deviceArch.equals("x86_64") ? 5 : 6;
    }

    public String getItemName(int position) {
        if (deviceArch.equals("x86_64")) {
            return switch (position) {
                case 0 -> "Drivers";
                case 1 -> "Wine";
                case 2 -> "DXVK";
                case 3 -> "WineD3D";
                case 4 -> "VKD3D";
                default -> throw new IllegalArgumentException("Unexpected value: " + position);
            };
        } else {
            return switch (position) {
                case 0 -> "Drivers";
                case 1 -> "Box64";
                case 2 -> "Wine";
                case 3 -> "DXVK";
                case 4 -> "WineD3D";
                case 5 -> "VKD3D";
                default -> throw new IllegalArgumentException("Unexpected value: " + position);
            };
        }
    }
}