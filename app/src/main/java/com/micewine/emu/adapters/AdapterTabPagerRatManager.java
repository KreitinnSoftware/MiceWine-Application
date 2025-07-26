package com.micewine.emu.adapters;

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

import com.micewine.emu.fragments.RatManagerFragment;

public class AdapterTabPagerRatManager extends FragmentStateAdapter {
    public AdapterTabPagerRatManager(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        return switch (position) {
            case 0 -> new RatManagerFragment("VulkanDriver", VK_DRIVER, "?");
            case 1 -> new RatManagerFragment("Box64", BOX64, "?");
            case 2 -> new RatManagerFragment("Wine", WINE, "?");
            case 3 -> new RatManagerFragment("DXVK", DXVK, "?");
            case 4 -> new RatManagerFragment("WineD3D", WINED3D, "?");
            case 5 -> new RatManagerFragment("VKD3D", VKD3D, "?");
            default -> throw new IllegalArgumentException("Invalid Fragment for Position " + position);
        };
    }

    @Override
    public int getItemCount() {
        return 6;
    }

    public String getItemName(int position) {
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