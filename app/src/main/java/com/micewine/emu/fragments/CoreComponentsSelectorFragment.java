package com.micewine.emu.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.micewine.emu.R;
import com.micewine.emu.adapters.AdapterTabPagerCoreComponentsSelector;
import com.micewine.emu.databinding.FragmentCoreComponentsSelectorBinding;

public class CoreComponentsSelectorFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = FragmentCoreComponentsSelectorBinding.inflate(inflater, container, false).getRoot();

        TabLayout tabLayout = rootView.findViewById(R.id.tabLayout);
        ViewPager2 viewPager = rootView.findViewById(R.id.viewPager);

        AdapterTabPagerCoreComponentsSelector adapter = new AdapterTabPagerCoreComponentsSelector(requireActivity());

        viewPager.setAdapter(adapter);

        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> tab.setText(adapter.getItemName(position))).attach();

        return rootView;
    }
}