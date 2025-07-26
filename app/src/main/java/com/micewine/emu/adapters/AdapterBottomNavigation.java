package com.micewine.emu.adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.micewine.emu.fragments.AboutFragment;
import com.micewine.emu.fragments.FileManagerFragment;
import com.micewine.emu.fragments.SettingsFragment;
import com.micewine.emu.fragments.ShortcutsFragment;

public class AdapterBottomNavigation extends FragmentStateAdapter {
    public AdapterBottomNavigation(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        return switch (position) {
            case 0 -> new ShortcutsFragment();
            case 1 -> new SettingsFragment();
            case 2 -> new FileManagerFragment();
            case 3 -> new AboutFragment();
            default -> throw new IllegalArgumentException("Invalid Fragment for Position " + position);
        };
    }

    @Override
    public int getItemCount() {
        return 4;
    }
}