package com.micewine.emu.adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.micewine.emu.R;
import com.micewine.emu.fragments.WelcomeFragment;

public class AdapterWelcomeFragments extends FragmentStateAdapter {
    public AdapterWelcomeFragments(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        return switch (position) {
            case 0 -> new WelcomeFragment(R.layout.fragment_welcome);
            case 1 -> new WelcomeFragment(R.layout.fragment_welcome_2);
            default -> throw new IllegalArgumentException("Invalid Fragment for Position " + position);
        };
    }

    @Override
    public int getItemCount() {
        return 2;
    }
}